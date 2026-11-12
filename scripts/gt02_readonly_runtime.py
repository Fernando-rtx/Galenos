#!/usr/bin/env python3
"""Verificación HU1/HU2 sobre clinica, con JDBC forzado a sólo lectura.
No crea bases ni tablas. No altera el servidor compartido ni sus credenciales.
"""
from pathlib import Path
import os
import shutil
import socket
import subprocess
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
STATE = ROOT / ".gt02"
SERVER = "GalenosGT02ReadOnly"


def main():
    action = sys.argv[1]
    runtime = Path(os.environ.get("GT02_LIBERTY", str(Path.home() / "openliberty/wlp")))
    server = STATE / "usr/servers" / SERVER
    if action == "stage":
        source = Path(
            os.environ.get("GT02_CONFIG", str(runtime / "usr/servers/GalenosSVAcademico"))
        )
        values = dict(
            line.split("=", 1)
            for line in (source / "server.env").read_text().splitlines()
            if "=" in line and not line.lstrip().startswith("#")
        )
        if values.get("POSTGRESQL_DB_NAME") != "clinica":
            raise SystemExit("La fuente debe apuntar a clinica; no se preparó el runtime")
        employee_roles = os.environ.get("GALENOS_EMPLEADO_ROLES", "")
        if not employee_roles:
            raise SystemExit(
                "Configura GALENOS_EMPLEADO_ROLES con IDs de roles empleados verificados"
            )
        (server / "apps").mkdir(parents=True, exist_ok=True)
        (server / "jdbc").mkdir(exist_ok=True)
        (STATE / "usr/shared/resources/primefaces").mkdir(parents=True, exist_ok=True)
        values.update(
            HTTP_PORT="19084",
            POSTGRESQL_DB_NAME="clinica",
            GALENOS_API_MODE="academic",
            GALENOS_DEMO_MODE="false",
            GALENOS_CRUD_DEMO="false",
            GALENOS_EMPLEADO_ROLES=employee_roles,
            GALENOS_CORS_ORIGINS="http://127.0.0.1:3000,http://localhost:3000",
        )
        path = server / "server.env"
        with os.fdopen(os.open(path, os.O_WRONLY | os.O_CREAT | os.O_TRUNC, 0o600), "w") as stream:
            stream.write("\n".join(k + "=" + v for k, v in values.items()) + "\n")
        tree = ET.parse(ROOT / "src/main/liberty/config/server.xml")
        tree.find(".//properties.postgresql").set("options", "-c default_transaction_read_only=on")
        tree.write(server / "server.xml", encoding="unicode")
        shutil.copyfile(ROOT / "target/GalenosSV.war", server / "apps/GalenosSV.war")
        shutil.copyfile(
            Path.home() / ".m2/repository/org/postgresql/postgresql/42.7.4/postgresql-42.7.4.jar",
            server / "jdbc/postgresql-42.7.4.jar",
        )
        shutil.copyfile(
            runtime / "usr/shared/resources/primefaces/primefaces-15.0.17-jakarta.jar",
            STATE / "usr/shared/resources/primefaces/primefaces-15.0.17-jakarta.jar",
        )
        print("Verificación: clinica, JDBC jdbc/pgdb, transacciones SQL sólo lectura, puerto19084.")
        return
    if action not in ("run", "stop", "status"):
        raise SystemExit("Acción inválida")
    if action == "run":
        with socket.socket() as probe:
            probe.bind(("127.0.0.1", 19084))
    env = dict(os.environ, WLP_USER_DIR=str(STATE / "usr"), WLP_OUTPUT_DIR=str(STATE / "output"))
    raise SystemExit(subprocess.call([str(runtime / "bin/server"), action, SERVER], env=env))


if __name__ == "__main__":
    main()
