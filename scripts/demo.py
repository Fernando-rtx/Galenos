#!/usr/bin/env python3
"""Arranque y verificación del backend académico. Solo Python estándar y CLI existentes."""
import argparse
from contextlib import contextmanager
from datetime import datetime, timezone
import fcntl
import hashlib
import json
import os
from pathlib import Path
import re
import secrets
import shutil
import socket
import subprocess
import sys
import time
from urllib.error import HTTPError, URLError
from urllib.request import urlopen
import xml.etree.ElementTree as ET


ROOT = Path(__file__).resolve().parents[1]
STATE = ROOT / ".galenos-demo"
SERVER = "GalenosSVDemo"
OWNER_LABEL = "org.galenos.demo.owner"
# Mismo PostgreSQL 12.22 comprobado en la entrega anterior; digest inmutable.
POSTGRES_IMAGE = "postgres@sha256:2f2a8c2a7d10862e7fba2602e304523554f9df8244c632dafe2628ccb398fb5c"
DEFAULTS = {
    "GALENOS_DEMO_HTTP_PORT": "19081", "GALENOS_DEMO_HTTPS_PORT": "19444",
    "GALENOS_DEMO_DB_PORT": "15432", "GALENOS_DEMO_CONTAINER": "galenos-demo-db",
    "GALENOS_CORS_ORIGINS": "http://localhost:3000",
    "GALENOS_CLINIC_TIME_ZONE": "America/El_Salvador",
    "GALENOS_JAVA_HOME": "", "GALENOS_MAVEN_REPO": str(Path.home() / ".m2/repository"),
}


class DemoError(RuntimeError):
    pass


def available_port(port):
    with socket.socket() as probe:
        # Igual que un servidor reiniciable: TIME_WAIT no equivale a un listener activo.
        probe.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        try:
            probe.bind(("127.0.0.1", int(port)))
        except OSError as error:
            raise DemoError("Puerto ocupado: " + str(port)) from error


def liberty_version(runtime):
    """Metadato de la distribución; productInfo traduce sus etiquetas según el idioma."""
    path = runtime / "lib/versions/openliberty.properties"
    if not path.is_file():
        raise DemoError("El runtime no incluye los metadatos de Open Liberty")
    for line in path.read_text(encoding="utf-8").splitlines():
        key, separator, value = line.partition("=")
        if separator and key.strip() == "com.ibm.websphere.productVersion":
            version = value.strip()
            if re.fullmatch(r"\d+(?:\.\d+){3}", version):
                return version
    raise DemoError("No se encontró una versión válida en los metadatos de Open Liberty")


def configuration():
    values = dict(DEFAULTS)
    path = ROOT / "demo.env"
    if path.exists():
        for line in path.read_text().splitlines():
            line = line.strip()
            if not line or line.startswith("#"):
                continue
            key, separator, value = line.partition("=")
            key = key.strip()
            if not separator or key not in DEFAULTS:
                raise DemoError("Configuración desconocida en demo.env; revisar demo.env.example")
            values[key] = value.strip()
    values.update({key: os.environ[key] for key in DEFAULTS if key in os.environ})
    if any("\n" in value or "\r" in value for value in values.values()):
        raise DemoError("La configuración no admite saltos de línea")
    ports = []
    for key in ("GALENOS_DEMO_HTTP_PORT", "GALENOS_DEMO_HTTPS_PORT", "GALENOS_DEMO_DB_PORT"):
        if not values[key].isdigit() or not 1024 <= int(values[key]) <= 65535:
            raise DemoError(key + " debe ser un puerto entre 1024 y 65535")
        ports.append(int(values[key]))
    if len(set(ports)) != len(ports):
        raise DemoError("Los puertos de la demo deben ser distintos")
    if not re.fullmatch(r"[a-zA-Z0-9][a-zA-Z0-9_.-]{0,62}", values["GALENOS_DEMO_CONTAINER"]):
        raise DemoError("Nombre de contenedor no válido")
    values["GALENOS_MAVEN_REPO"] = str(Path(values["GALENOS_MAVEN_REPO"]).expanduser().resolve())
    return values


def private_write(path, content):
    path.parent.mkdir(parents=True, exist_ok=True)
    with os.fdopen(os.open(path, os.O_WRONLY | os.O_CREAT | os.O_TRUNC, 0o600), "w") as stream:
        stream.write(content)
    path.chmod(0o600)


def java_home(config):
    candidates = [config["GALENOS_JAVA_HOME"], os.environ.get("JAVA_HOME", ""),
                  str(Path.home() / ".local/share/galenos/jdk-21")]
    executable = shutil.which("java")
    if executable:
        candidates.append(str(Path(executable).resolve().parent.parent))
    for candidate in candidates:
        if not candidate:
            continue
        home = Path(candidate).expanduser().resolve()
        if not (home / "bin/java").is_file() or not (home / "bin/javac").is_file():
            continue
        result = subprocess.run([str(home / "bin/java"), "-XshowSettings:properties", "-version"],
                                capture_output=True, text=True)
        if result.returncode == 0 and re.search(r"java.specification.version\s*=\s*21\s", result.stderr):
            return home
        if candidate == config["GALENOS_JAVA_HOME"]:
            raise DemoError("GALENOS_JAVA_HOME debe señalar un JDK 21, no otra versión")
    raise DemoError("Se requiere JDK 21. Definir GALENOS_JAVA_HOME en demo.env o JAVA_HOME; ver docs/demo-setup.md")


class Demo:
    def __init__(self, config):
        self.config = config
        self.owner = hashlib.sha256(str(ROOT).encode()).hexdigest()[:20]
        self.container = config["GALENOS_DEMO_CONTAINER"]
        self.volume = self.container + "-data"
        self.runtime = STATE / "runtime/wlp"
        self.server = STATE / "usr/servers" / SERVER
        self.base = "http://localhost:" + config["GALENOS_DEMO_HTTP_PORT"] + "/GalenosSV/resources"
        self.env = None
        self.database_started = False

    def initialize(self):
        STATE.mkdir(mode=0o700, exist_ok=True)
        STATE.chmod(0o700)
        (STATE / "logs").mkdir(exist_ok=True)

    @contextmanager
    def setup_lock(self):
        self.initialize()
        with (STATE / "setup.lock").open("w") as lock:
            try:
                fcntl.flock(lock, fcntl.LOCK_EX | fcntl.LOCK_NB)
            except BlockingIOError as error:
                raise DemoError("Otro arranque de la demo está en preparación") from error
            yield

    def toolchain(self):
        for command in ("mvn", "docker"):
            if not shutil.which(command):
                raise DemoError("Falta " + command + "; ver docs/demo-setup.md")
        home = java_home(self.config)
        self.env = dict(os.environ)
        self.env.update(JAVA_HOME=str(home), PATH=str(home / "bin") + os.pathsep + os.environ.get("PATH", ""),
                        WLP_USER_DIR=str(STATE / "usr"), WLP_OUTPUT_DIR=str(STATE / "output"))
        for key in list(self.env):
            if key.startswith("POSTGRESQL_") or key in ("GALENOS_DEMO_MODE", "HTTP_PORT", "HTTPS_PORT"):
                self.env.pop(key)
        print("JDK 21:", home, flush=True)

    def capture(self, args, *, input_text=None, required=True):
        result = subprocess.run(args, input=input_text, capture_output=True, text=True,
                                cwd=ROOT, env=self.env)
        if required and result.returncode:
            raise DemoError("Falló " + Path(args[0]).name + "; comprobar requisitos y estado de la demo")
        return result

    def logged(self, args, phase, *, input_text=None):
        self.initialize()
        path = STATE / "logs" / (phase + ".log")
        print(phase + "… (" + str(path.relative_to(ROOT)) + ")", flush=True)
        with os.fdopen(os.open(path, os.O_WRONLY | os.O_CREAT | os.O_TRUNC, 0o600), "w") as output:
            result = subprocess.run(args, cwd=ROOT, env=self.env, input=input_text, text=True,
                                    stdout=output, stderr=subprocess.STDOUT)
        if result.returncode:
            raise DemoError(phase + " falló; consultar " + str(path.relative_to(ROOT)))

    def maven(self, *goals):
        return ["mvn", "-B", "-Pdemo", "-Dmaven.repo.local=" + self.config["GALENOS_MAVEN_REPO"], *goals]

    def inspect_owner(self, kind, name):
        command = ["docker", "volume", "inspect"] if kind == "volume" else ["docker", "inspect"]
        result = self.capture([*command, name], required=False)
        if result.returncode:
            return None
        info = json.loads(result.stdout)[0]
        labels = info.get("Labels", {}) if kind == "volume" else info["Config"].get("Labels", {})
        if (labels or {}).get(OWNER_LABEL) != self.owner:
            raise DemoError("El recurso " + name + " pertenece a otro entorno; no se modificó")
        return info

    def database(self):
        self.capture(["docker", "info", "--format", "{{.ServerVersion}}"])
        container = self.inspect_owner("container", self.container)
        volume = self.inspect_owner("volume", self.volume)
        path = STATE / "secrets.json"
        if not path.exists():
            if container or volume:
                raise DemoError("Hay recursos persistidos sin sus claves locales; usar otro nombre de contenedor")
            private_write(path, json.dumps({"password": secrets.token_hex(24), "keystore": secrets.token_hex(24)}))
        credentials = json.loads(path.read_text())
        self.credentials = credentials
        private_write(STATE / "postgres.env", "POSTGRES_USER=galenos_demo\nPOSTGRES_DB=galenos_demo_local\n"
                      + "POSTGRES_PASSWORD=" + credentials["password"] + "\n")
        if not volume:
            self.capture(["docker", "volume", "create", "--label", OWNER_LABEL + "=" + self.owner, self.volume])
        if not container:
            self.logged(["docker", "create", "--name", self.container, "--label", OWNER_LABEL + "=" + self.owner,
                         "--env-file", str(STATE / "postgres.env"), "--publish",
                         "127.0.0.1:" + self.config["GALENOS_DEMO_DB_PORT"] + ":5432",
                         "--mount", "type=volume,source=" + self.volume + ",target=/var/lib/postgresql/data",
                         POSTGRES_IMAGE], "create-database")
        else:
            expected = [{"HostIp": "127.0.0.1", "HostPort": self.config["GALENOS_DEMO_DB_PORT"]}]
            if container["HostConfig"]["PortBindings"].get("5432/tcp") != expected:
                raise DemoError("El puerto del contenedor existente difiere de demo.env; no se recreó")
            if container["Config"]["Image"] != POSTGRES_IMAGE:
                raise DemoError("La imagen del contenedor existente no corresponde a la demo fijada")
        self.capture(["docker", "start", self.container])
        self.database_started = True
        deadline = time.monotonic() + 90
        while time.monotonic() < deadline:
            if self.capture(["docker", "exec", self.container, "pg_isready", "-U", "galenos_demo",
                             "-d", "galenos_demo_local"], required=False).returncode == 0:
                self.fixtures()
                return
            time.sleep(1)
        raise DemoError("PostgreSQL de demostración no estuvo listo a tiempo")

    def sql(self, statement):
        return self.capture(["docker", "exec", "-i", self.container, "psql", "-X", "-v", "ON_ERROR_STOP=1",
                             "-U", "galenos_demo", "-d", "galenos_demo_local", "-At"], input_text=statement).stdout.strip()

    def fixtures(self):
        files = [ROOT / "test/fixtures/tipo_documento.sql", ROOT / "test/fixtures/consulta_demo.sql"]
        digest = hashlib.sha256(b"".join(path.read_bytes() for path in files)).hexdigest()
        if self.sql("SELECT to_regclass('public.galenos_demo_fixture');"):
            if self.sql("SELECT revision FROM galenos_demo_fixture;") != digest:
                raise DemoError("Cambió el fixture respecto a esta base; usar un contenedor nuevo, no se borró nada")
            return
        if self.sql("SELECT count(*) FROM pg_tables WHERE schemaname='public';") != "0":
            raise DemoError("La base tiene tablas ajenas al fixture; no se modificaron")
        sql = "\n".join(re.sub(r"(?m)^(?:BEGIN|COMMIT);\s*$", "", path.read_text()) for path in files)
        self.sql("BEGIN;\n" + sql + "\nCREATE TABLE galenos_demo_fixture (revision text NOT NULL);\n"
                 + "INSERT INTO galenos_demo_fixture VALUES ('" + digest + "');\nCOMMIT;")
        print("Fixtures sintéticos importados en una base nueva", flush=True)

    def server_command(self, action):
        return [str(self.runtime / "bin/server"), action, SERVER]

    def is_running(self):
        return (self.runtime / "bin/server").exists() and self.capture(self.server_command("status"), required=False).returncode == 0

    def build(self, verify):
        pom = ET.parse(ROOT / "pom.xml").getroot()
        namespace = {"m": "http://maven.apache.org/POM/4.0.0"}
        self.versions = {child.tag.split("}")[-1]: child.text for child in pom.find("m:properties", namespace)}
        if not (self.runtime / "bin/server").exists():
            self.logged(self.maven("liberty:install-server"), "install-liberty")
        if liberty_version(self.runtime) != self.versions["openliberty.version"]:
            raise DemoError("La versión del runtime local no coincide con pom.xml")
        goals = ["clean", "package"] if verify else ["package", "-DskipTests"]
        self.logged(self.maven(*goals), "verify-java" if verify else "build")

    def configure_server(self):
        for folder in (self.server / "apps", self.server / "jdbc", STATE / "usr/shared/resources/primefaces"):
            folder.mkdir(parents=True, exist_ok=True)
        xml = ET.parse(ROOT / "src/main/liberty/config/server.xml")
        xml.getroot().find("httpEndpoint").set("host", "127.0.0.1")
        xml.write(self.server / "server.xml", encoding="utf-8", xml_declaration=True)
        shutil.copyfile(ROOT / "target/GalenosSV.war", self.server / "apps/GalenosSV.war")
        jdbc = self.versions["postgresql.jdbc.version"]
        repository = Path(self.config["GALENOS_MAVEN_REPO"]).expanduser()
        shutil.copyfile(repository / f"org/postgresql/postgresql/{jdbc}/postgresql-{jdbc}.jar",
                        self.server / f"jdbc/postgresql-{jdbc}.jar")
        for source in (self.runtime / "usr/shared/resources/primefaces").glob("*.jar"):
            shutil.copyfile(source, STATE / "usr/shared/resources/primefaces" / source.name)
        values = {
            "HTTP_PORT": self.config["GALENOS_DEMO_HTTP_PORT"], "HTTPS_PORT": self.config["GALENOS_DEMO_HTTPS_PORT"],
            "POSTGRESQL_DB_HOST": "127.0.0.1", "POSTGRESQL_DB_PORT": self.config["GALENOS_DEMO_DB_PORT"],
            "POSTGRESQL_DB_NAME": "galenos_demo_local", "POSTGRESQL_DB_USER": "galenos_demo",
            "POSTGRESQL_DB_PASSWORD": self.credentials["password"], "keystore_password": self.credentials["keystore"],
            "GALENOS_DEMO_MODE": "true", "GALENOS_CORS_ORIGINS": self.config["GALENOS_CORS_ORIGINS"],
            "GALENOS_CLINIC_TIME_ZONE": self.config["GALENOS_CLINIC_TIME_ZONE"],
            "GALENOS_API_PAGE_SIZE": "20", "GALENOS_API_MAX_PAGE_SIZE": "100",
        }
        private_write(self.server / "server.env", "\n".join(key + "=" + value for key, value in values.items()) + "\n")

    def ready(self, process):
        deadline = time.monotonic() + 180
        while time.monotonic() < deadline:
            if process.poll() is not None:
                raise DemoError("Liberty terminó durante el arranque; ver .galenos-demo/logs/liberty.log")
            try:
                with urlopen(self.base + "/v1/clinica?max=1", timeout=5) as response:
                    if response.status == 200 and response.headers.get("Total-registros") == "2":
                        return
            except (URLError, HTTPError, TimeoutError):
                pass
            time.sleep(1)
        raise DemoError("La API no estuvo lista a tiempo; ver .galenos-demo/logs/liberty.log")

    def check(self, browser=False):
        self.env = self.env or dict(os.environ)
        self.env.update(GALENOS_API_BASE_URL=self.base,
                        GALENOS_TEST_ORIGIN=self.config["GALENOS_CORS_ORIGINS"].split(",")[0].strip())
        for name in ("tipo_documento_http", "consulta_demo_http"):
            self.logged([sys.executable, str(ROOT / "test/integration" / (name + ".py"))], name)
        if browser:
            self.logged([sys.executable, str(ROOT / "test/integration/consulta_demo_browser.py")], "browser")
        print("OK: 12 pruebas HTTP de TipoDocumento + 9 de Consulta" + (" y Chromium" if browser else ""), flush=True)

    def connection(self):
        return {
            "baseUrl": self.base, "credentials": "omit",
            "allowedOrigins": [origin.strip() for origin in self.config["GALENOS_CORS_ORIGINS"].split(",") if origin.strip()],
            "resources": {"tipoDocumento": "v1/tipo_documento", "clinica": "v1/clinica", "consulta": "v1/consulta"},
            "countHeader": "Total-registros", "syntheticDataOnly": True,
        }

    def report(self, browser):
        suites = [ET.parse(path).getroot() for path in (ROOT / "target/surefire-reports").glob("TEST-*.xml")]
        java = self.capture([str(Path(self.env["JAVA_HOME"]) / "bin/java"), "-version"]).stderr.splitlines()[0]
        report = {
            "utc": datetime.now(timezone.utc).isoformat(), "java": java,
            "liberty": self.versions["openliberty.version"], "postgresql": self.sql("SHOW server_version;"),
            "javaTests": {key: sum(int(suite.get(key, 0)) for suite in suites)
                          for key in ("tests", "failures", "errors", "skipped")},
            "http": {"tipoDocumento": "passed", "consulta": "passed"}, "browser": browser,
            "fixtureSha256": self.sql("SELECT revision FROM galenos_demo_fixture;"),
            "warSha256": hashlib.file_digest((ROOT / "target/GalenosSV.war").open("rb"), "sha256").hexdigest(),
        }
        private_write(STATE / "verification.json", json.dumps(report, ensure_ascii=False, indent=2) + "\n")
        print("Evidencia: .galenos-demo/verification.json", flush=True)

    def stop(self):
        if self.is_running():
            self.capture(self.server_command("stop"))
        info = self.inspect_owner("container", self.container)
        if info and info["State"]["Running"]:
            self.capture(["docker", "stop", self.container])
        print("Demo detenida; contenedor, volumen y datos sintéticos conservados", flush=True)

    def run(self, verify=False, browser=False):
        process = None
        try:
            with self.setup_lock():
                self.toolchain()
                if self.is_running():
                    raise DemoError("La demo ya está activa; usar ./demo check o detenerla antes de reconstruir")
                for key in ("GALENOS_DEMO_HTTP_PORT", "GALENOS_DEMO_HTTPS_PORT"):
                    available_port(self.config[key])
                if verify:
                    self.logged([sys.executable, str(ROOT / "test/integration/test_demo_runner.py")], "verify-runner")
                self.database()
                self.build(verify)
                self.configure_server()
                with os.fdopen(os.open(STATE / "logs/liberty.log", os.O_WRONLY | os.O_CREAT | os.O_TRUNC, 0o600), "w") as output:
                    process = subprocess.Popen(self.server_command("run"), cwd=ROOT, env=self.env,
                                               stdout=output, stderr=subprocess.STDOUT, start_new_session=True)
            self.ready(process)
            print("API lista:", self.base, flush=True)
            print("CORS:", self.config["GALENOS_CORS_ORIGINS"], flush=True)
            private_write(STATE / "connection.json", json.dumps(self.connection(), ensure_ascii=False, indent=2) + "\n")
            if verify:
                self.check(browser)
                self.report(browser)
            else:
                print("Detener: Ctrl+C o ./demo stop desde otra terminal", flush=True)
                if process.wait() != 0:
                    raise DemoError("Liberty terminó con error; ver .galenos-demo/logs/liberty.log")
        finally:
            if process is not None:
                if process.poll() is None:
                    self.capture(self.server_command("stop"), required=False)
                    try:
                        process.wait(timeout=30)
                    except subprocess.TimeoutExpired:
                        process.terminate()
                self.stop()
            elif self.database_started:
                self.stop()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("command", choices=("start", "verify", "check", "status", "stop", "info"))
    parser.add_argument("--browser", action="store_true", help="incluye Chromium con Playwright externo ya instalado")
    args = parser.parse_args()
    try:
        demo = Demo(configuration())
        if args.command == "info":
            print(json.dumps(demo.connection(), ensure_ascii=False, indent=2))
        elif args.command in ("start", "verify"):
            demo.run(args.command == "verify", args.browser)
        elif args.command == "check":
            demo.check(args.browser)
        else:
            demo.toolchain()
            if args.command == "stop":
                demo.stop()
            else:
                print("API:", demo.base, "—", "activa" if demo.is_running() else "detenida")
    except (DemoError, OSError, ValueError) as error:
        print("ERROR:", error, file=sys.stderr)
        return 1
    except KeyboardInterrupt:
        return 130
    return 0


if __name__ == "__main__":
    sys.exit(main())
