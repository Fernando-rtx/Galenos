#!/usr/bin/env python3
"""Prepara unicamente la base sintetica CRUD, sin modificar el demo previo o clinica."""
from pathlib import Path
import hashlib
import os
import json
import subprocess

ROOT = Path(__file__).resolve().parents[1]
CONTAINER = os.environ.get("GALENOS_CRUD_CONTAINER", "galenos-demo-db")
OWNER = os.environ.get("GALENOS_CRUD_OWNER", hashlib.sha256(str(ROOT).encode()).hexdigest()[:20])
DATABASE = "galenos_demo_crud"
USER = "galenos_demo"
KNOWN_PRE_HU3_FIXTURE = "1e9b7a17e4cca93768cbb87f7e71a613ad811581cf058e5dda6437d96e4a034b"


def command(args, text=None):
    result = subprocess.run(args, input=text, capture_output=True, text=True)
    if result.returncode:
        raise SystemExit("No se completo la preparacion de la base sintetica; revisar Docker y el fixture")
    return result.stdout.strip()


def sql(database, statement):
    assert database in ("postgres", DATABASE), "Destino fuera del entorno de pruebas"
    return command(["docker", "exec", "-i", CONTAINER, "psql", "-X", "-qAt", "-v", "ON_ERROR_STOP=1",
                    "-U", USER, "-d", database], statement)


def main():
    info = json.loads(command(["docker", "inspect", CONTAINER]))[0]
    if info["Config"].get("Labels", {}).get("org.galenos.demo.owner") != OWNER:
        raise SystemExit("El contenedor no pertenece al demo sintetico verificado")
    if not info["State"]["Running"]:
        raise SystemExit("El contenedor demo debe estar activo; no se inician recursos ajenos")
    schema = (ROOT / "clinica_ppi115_2026_08_20.sql").read_text()
    fixture = (ROOT / "test/fixtures/crud_demo.sql").read_text()
    digest = hashlib.sha256((schema + fixture).encode()).hexdigest()
    if sql("postgres", "SELECT datname FROM pg_database WHERE datname='" + DATABASE + "';") != DATABASE:
        sql("postgres", 'CREATE DATABASE "' + DATABASE + '";')
    marker = sql(DATABASE, "SELECT to_regclass('public.galenos_crud_fixture');")
    if marker:
        existing = sql(DATABASE, "SELECT revision FROM galenos_crud_fixture;")
        if existing == KNOWN_PRE_HU3_FIXTURE:
            migration = (ROOT / "db/migrations/V2026_11_12_01__consulta_paso_atencion.sql").read_text()
            sql(DATABASE, "BEGIN;\n" + migration + "\n"
                + "INSERT INTO documento VALUES "
                + "('00000000-0000-4000-8000-000000009002','00000000-0000-4000-8000-000000000045','00000000-0000-4000-8000-000000000001','DEF',NULL),"
                + "('00000000-0000-4000-8000-000000009003','00000000-0000-4000-8000-000000000046','00000000-0000-4000-8000-000000000001','GHI',NULL) ON CONFLICT (id_documento) DO NOTHING;\n"
                + "UPDATE consulta_procedimiento_paso SET id_procedimiento_paso='00000000-0000-4000-8000-000000006001' "
                + "WHERE id_consulta_procedimiento_paso='00000000-0000-4000-8000-000000019001' AND id_procedimiento_paso IS NULL;\n"
                + "UPDATE orden_examen SET id_examen='00000000-0000-4000-8000-000000003101' "
                + "WHERE id_orden_examen='00000000-0000-4000-8000-000000020001' AND id_examen IS NULL;\n"
                + "UPDATE galenos_crud_fixture SET revision='" + digest + "';\nCOMMIT;")
            print("Base sintética demo actualizada de forma aditiva; no se eliminaron registros")
            return
        if existing != digest:
            raise SystemExit("El fixture cambio; no se reemplazo ni borro la base existente")
        print("Base sintetica reutilizada, revision:", digest)
        return
    if sql(DATABASE, "SELECT count(*) FROM pg_tables WHERE schemaname='public';") != "0":
        raise SystemExit("La base contiene tablas sin marcador; no se modificaron")
    sql(DATABASE, "BEGIN;\n" + schema + "\n" + fixture +
        "\nCREATE TABLE galenos_crud_fixture (revision text NOT NULL);\n" +
        "INSERT INTO galenos_crud_fixture VALUES ('" + digest + "');\nCOMMIT;")
    print("Preparadas 20 tablas del esquema oficial con datos sinteticos en", DATABASE)
    print("Revision:", digest)


if __name__ == "__main__":
    main()
