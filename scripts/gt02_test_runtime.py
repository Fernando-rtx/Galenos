#!/usr/bin/env python3
"""Únicamente ensayos automatizados autorizados. Nunca configura la aplicación normal.
Requiere GT02_DATABASE explícita NUEVA; rechaza reutilizarla o tocar clinica.
No elimina automáticamente bases, contenedores ni datos.
"""
from pathlib import Path
import json
import os
import re
import shutil
import socket
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
DATABASE = os.environ.get("GT02_DATABASE", "")
STATE = ROOT / ".gt02/test"
SERVER = "GalenosGT02Test"
CONTAINER = os.environ.get("GT02_CONTAINER", "postgres_db")


def sql(database, statement):
    assert database in ("postgres", DATABASE)
    return subprocess.run(
        [
            "docker",
            "exec",
            "-i",
            CONTAINER,
            "psql",
            "-X",
            "-qAt",
            "-v",
            "ON_ERROR_STOP=1",
            "-U",
            "postgres",
            "-d",
            database,
        ],
        input=statement,
        text=True,
        capture_output=True,
        check=True,
    ).stdout.strip()


def main():
    if not re.fullmatch(r"galenos_rest_test_gt02_[a-z0-9_]+", DATABASE):
        raise SystemExit("GT02_DATABASE debe identificar explícitamente una base de pruebas nueva")
    action = sys.argv[1]
    runtime = Path(os.environ.get("GT02_LIBERTY", str(Path.home() / "openliberty/wlp")))
    if action == "prepare":
        if sql("postgres", "SELECT 1 FROM pg_database WHERE datname='" + DATABASE + "'"):
            raise SystemExit("La base ya existe; no se modificó. Usa otro nombre nuevo.")
        sql("postgres", 'CREATE DATABASE "' + DATABASE + '";')
        if sql(DATABASE, "SELECT count(*) FROM pg_tables WHERE schemaname='public'") != "0":
            raise SystemExit("La base debe estar vacía")
        text = "\n".join(
            (ROOT / p).read_text()
            for p in [
                "clinica_ppi115_2026_08_20.sql",
                "test/fixtures/crud_demo.sql",
                "test/fixtures/gt02.sql",
            ]
        )
        sql(DATABASE, "BEGIN;\n" + text + "\nCOMMIT;")
        STATE.mkdir(parents=True, exist_ok=True)
        (STATE / "database").write_text(DATABASE)
        print("Base nueva exclusivamente ficticia:", DATABASE)
        return
    if not (STATE / "database").exists() or (STATE / "database").read_text() != DATABASE:
        raise SystemExit("No corresponde a la base preparada por este runner")
    if action == "stage":
        server = STATE / "usr/servers" / SERVER
        (server / "apps").mkdir(parents=True, exist_ok=True)
        (server / "jdbc").mkdir(exist_ok=True)
        (STATE / "usr/shared/resources/primefaces").mkdir(parents=True, exist_ok=True)
        info = json.loads(subprocess.check_output(["docker", "inspect", CONTAINER]))[0]
        values = dict(item.split("=", 1) for item in info["Config"]["Env"] if "=" in item)
        java_home = os.environ.get("JAVA_HOME")
        if not java_home:
            java_command = shutil.which("java")
            if java_command:
                java_home = str(Path(java_command).resolve().parent.parent)
        if not java_home:
            raise SystemExit("Define JAVA_HOME para usar un JDK 21 instalado en este equipo")
        env = {
            "HTTP_PORT": "19085",
            "POSTGRESQL_DB_HOST": "127.0.0.1",
            "POSTGRESQL_DB_PORT": "5432",
            "POSTGRESQL_DB_NAME": DATABASE,
            "POSTGRESQL_DB_USER": "postgres",
            "POSTGRESQL_DB_PASSWORD": values["POSTGRES_PASSWORD"],
            "GALENOS_API_MODE": "test",
            "GALENOS_DEMO_MODE": "false",
            "GALENOS_CRUD_DEMO": "false",
            "GALENOS_CORS_ORIGINS": "http://127.0.0.1:3000,http://localhost:3000",
            "GALENOS_EMPLEADO_ROLES": os.environ.get("GT02_EMPLEADO_ROLES", "00000000-0000-4000-8000-000000000032,00000000-0000-4000-8000-000000000033"),
            "JAVA_HOME": java_home,
        }
        with os.fdopen(
            os.open(server / "server.env", os.O_WRONLY | os.O_CREAT | os.O_TRUNC, 0o600), "w"
        ) as f:
            f.write("\n".join(k + "=" + v for k, v in env.items()) + "\n")
        shutil.copyfile(ROOT / "target/GalenosSV.war", server / "apps/GalenosSV.war")
        shutil.copyfile(ROOT / "src/main/liberty/config/server.xml", server / "server.xml")
        shutil.copyfile(
            Path.home() / ".m2/repository/org/postgresql/postgresql/42.7.4/postgresql-42.7.4.jar",
            server / "jdbc/postgresql-42.7.4.jar",
        )
        shutil.copyfile(
            runtime / "usr/shared/resources/primefaces/primefaces-15.0.17-jakarta.jar",
            STATE / "usr/shared/resources/primefaces/primefaces-15.0.17-jakarta.jar",
        )
        print("Runtime de ensayo: puerto19085. Configuración compartida de clinica intacta.")
        return
    if action not in ("run", "stop", "status"):
        raise SystemExit("Acción inválida")
    if action == "run":
        with socket.socket() as probe:
            probe.bind(("127.0.0.1", 19085))
    env = dict(os.environ, WLP_USER_DIR=str(STATE / "usr"), WLP_OUTPUT_DIR=str(STATE / "output"))
    raise SystemExit(subprocess.call([str(runtime / "bin/server"), action, SERVER], env=env))


if __name__ == "__main__":
    main()
