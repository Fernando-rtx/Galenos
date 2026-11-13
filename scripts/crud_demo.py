#!/usr/bin/env python3
"""Liberty CRUD aislado: reutiliza solo un PostgreSQL demo de propiedad verificada."""
from pathlib import Path
import argparse
import hashlib
import json
import os
import shutil
import secrets
import tempfile
import zipfile
import socket
import subprocess
import sys
import xml.etree.ElementTree as E

ROOT = Path(__file__).resolve().parents[1]
STATE = ROOT / ".galenos-demo/crud"
SERVER = "GalenosCrudDemo"


def private(path, text):
    path.parent.mkdir(parents=True, exist_ok=True)
    with os.fdopen(os.open(path, os.O_WRONLY | os.O_CREAT | os.O_TRUNC, 0o600), "w") as f:
        f.write(text)


def demo_web_xml():
    tree = E.parse(ROOT / "src/main/webapp/WEB-INF/web.xml")
    root = tree.getroot()
    ns = root.tag.split("}")[0] + "}"
    constraint = E.SubElement(root, ns + "security-constraint")
    collection = E.SubElement(constraint, ns + "web-resource-collection")
    E.SubElement(collection, ns + "web-resource-name").text = "Gestion sintetica autenticada"
    E.SubElement(collection, ns + "url-pattern").text = "/resources/v2/*"
    for method in ("GET", "HEAD", "POST", "PUT", "DELETE"):
        E.SubElement(collection, ns + "http-method").text = method
    auth = E.SubElement(constraint, ns + "auth-constraint")
    for role in ("galenos-demo-read", "galenos-demo-write"):
        E.SubElement(auth, ns + "role-name").text = role
    login = E.SubElement(root, ns + "login-config")
    E.SubElement(login, ns + "auth-method").text = "BASIC"
    E.SubElement(login, ns + "realm-name").text = "GalenosDemo"
    for role in ("galenos-demo-read", "galenos-demo-write"):
        E.SubElement(E.SubElement(root, ns + "security-role"), ns + "role-name").text = role
    return E.tostring(root, encoding="utf-8", xml_declaration=True)


def demo_war(source, target):
    with tempfile.NamedTemporaryFile(dir=target.parent, suffix=".war", delete=False) as temporary:
        temp_path = Path(temporary.name)
    try:
        with zipfile.ZipFile(source, "r") as original, zipfile.ZipFile(temp_path, "w") as output:
            for info in original.infolist():
                data = demo_web_xml() if info.filename == "WEB-INF/web.xml" else original.read(info.filename)
                output.writestr(info, data)
        shutil.copyfile(temp_path, target)
    finally:
        temp_path.unlink(missing_ok=True)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("action", choices=["start", "stop", "status"])
    args = parser.parse_args()
    source = Path(os.environ.get("GALENOS_CRUD_SOURCE", str(ROOT / ".galenos-demo"))).resolve()
    runtime = Path(os.environ.get("GALENOS_CRUD_LIBERTY", str(source / "runtime/wlp")))
    java_command = shutil.which("java")
    java_home = os.environ.get("JAVA_HOME") or (str(Path(java_command).resolve().parent.parent) if java_command else "")
    if not java_home:
        raise SystemExit("No se encontró Java. Define JAVA_HOME para iniciar Liberty.")
    env = dict(os.environ, JAVA_HOME=java_home,
               WLP_USER_DIR=str(STATE / "usr"), WLP_OUTPUT_DIR=str(STATE / "output"))
    command = [str(runtime / "bin/server"), args.action if args.action != "start" else "run", SERVER]
    if args.action != "start":
        raise SystemExit(subprocess.call(command, env=env))
    if not (source / "usr/servers/GalenosSVDemo/server.env").is_file():
        raise SystemExit("Indica GALENOS_CRUD_SOURCE al estado privado de una demo oficial ya preparada")
    values = dict(line.split("=", 1) for line in (source / "usr/servers/GalenosSVDemo/server.env").read_text().splitlines()
                  if "=" in line and not line.lstrip().startswith("#"))
    if values.get("GALENOS_DEMO_MODE") != "true" or not values.get("POSTGRESQL_DB_NAME", "").startswith("galenos_demo_"):
        raise SystemExit("La fuente no corresponde a la demostracion sintetica")
    STATE.mkdir(parents=True, exist_ok=True); STATE.chmod(0o700)
    http = int(os.environ.get("GALENOS_CRUD_HTTP_PORT", "19083"))
    with socket.socket() as probe:
        probe.bind(("127.0.0.1", http))
    fixture_env = dict(env, GALENOS_CRUD_CONTAINER=os.environ.get("GALENOS_CRUD_CONTAINER", "galenos-demo-db"),
                       GALENOS_CRUD_OWNER=hashlib.sha256(str(source.parent).encode()).hexdigest()[:20])
    subprocess.run([sys.executable, str(ROOT / "scripts/prepare_crud_demo.py")], env=fixture_env, check=True)
    subprocess.run(["mvn", "-q", "package", "-DskipTests"], cwd=ROOT, env=env, check=True)
    credential_path = STATE / "credentials.json"
    accounts = json.loads(credential_path.read_text()) if credential_path.exists() else {}
    credentials_changed = False
    for name in ["editor", "lector", "otro", "medico"]:
        if name not in accounts:
            accounts[name] = {"username": name + "-demo", "password": secrets.token_hex(24)}
            credentials_changed = True
    if credentials_changed:
        private(credential_path, json.dumps(accounts))
    server = STATE / "usr/servers" / SERVER
    (server / "apps").mkdir(parents=True, exist_ok=True)
    (server / "jdbc").mkdir(exist_ok=True)
    (STATE / "usr/shared/resources/primefaces").mkdir(parents=True, exist_ok=True)
    demo_war(ROOT / "target/GalenosSV.war", STATE / "GalenosSV.war")
    shutil.copyfile(STATE / "GalenosSV.war", server / "apps/GalenosSV.war")
    for p in (source / "usr/servers/GalenosSVDemo/jdbc").glob("*.jar"):
        shutil.copyfile(p, server / "jdbc" / p.name)
    for p in (source / "usr/shared/resources/primefaces").glob("*.jar"):
        shutil.copyfile(p, STATE / "usr/shared/resources/primefaces" / p.name)
    xml = E.parse(ROOT / "src/main/liberty/config/server.xml")
    root = xml.getroot()
    root.find("httpEndpoint").set("host", "127.0.0.1")
    features = root.find("featureManager")
    if not any(feature.text == "appSecurity-6.0" for feature in features.findall("feature")):
        E.SubElement(features, "feature").text = "appSecurity-6.0"
    registry = E.SubElement(root, "basicRegistry", {"id": "crud-demo", "realm": "GalenosDemo"})
    app = root.find("webApplication")
    binding = app.find("application-bnd")
    if binding is None:
        binding = E.SubElement(app, "application-bnd")
    for role, users in [("galenos-demo-read", ["editor", "lector", "otro", "medico"]),
                        ("galenos-demo-write", ["editor", "otro", "medico"])]:
        mapping = E.SubElement(binding, "security-role", {"name": role})
        for name in users:
            E.SubElement(mapping, "user", {"name": accounts[name]["username"]})
    for name, account in accounts.items():
        E.SubElement(registry, "user", {"name": account["username"], "password": "${env.GALENOS_CRUD_" + name.upper() + "_PASSWORD}"})
        values["GALENOS_CRUD_" + name.upper() + "_PASSWORD"] = account["password"]
    xml.write(server / "server.xml", encoding="utf-8", xml_declaration=True)
    values.update(HTTP_PORT=str(http), POSTGRESQL_DB_NAME="galenos_demo_crud",
                  GALENOS_DEMO_MODE="true", GALENOS_CRUD_DEMO="true", GALENOS_API_MODE="disabled",
                  GALENOS_CORS_ORIGINS="http://127.0.0.1:3000,http://localhost:3000",
                  GALENOS_CRUD_ACTOR_CLINICS="editor-demo=00000000-0000-4000-8000-000000000011;lector-demo=00000000-0000-4000-8000-000000000011;otro-demo=00000000-0000-4000-8000-000000000012;medico-demo=00000000-0000-4000-8000-000000000011",
                  GALENOS_CRUD_ACTOR_PERSONS="editor-demo=00000000-0000-4000-8000-000000000045;lector-demo=00000000-0000-4000-8000-000000000046;otro-demo=00000000-0000-4000-8000-000000000046;medico-demo=00000000-0000-4000-8000-000000000046")
    private(server / "server.env", "\n".join(k + "=" + v for k, v in values.items()) + "\n")
    print("CRUD sintetico:", "http://localhost:" + str(http) + "/GalenosSV/resources/v2", flush=True)
    raise SystemExit(subprocess.call(command, env=env))


if __name__ == "__main__":
    main()
