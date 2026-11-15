#!/usr/bin/env python3
"""Respaldo de lectura: no crea bases, no restaura ni ejecuta DDL/DML.
Verifica el archivo completo mediante pg_restore SIN conexión de destino.
"""
import argparse
import hashlib
import json
import os
import re
import subprocess
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CONTAINER = "postgres_db"


def sql(statement):
    return subprocess.check_output(
        ["docker", "exec", "-i", CONTAINER, "psql", "-X", "-qAt", "-v", "ON_ERROR_STOP=1", "-U", "postgres", "-d", "clinica"],
        input="BEGIN READ ONLY;\n" + statement + "\nCOMMIT;", text=True,
    ).strip()


def snapshot(after_a=False):
    tables = sql("SELECT tablename FROM pg_tables WHERE schemaname='public' ORDER BY tablename;").splitlines()
    if len(tables) != 20:
        raise RuntimeError("La revisión requiere exactamente las20tablas existentes")
    rows = {}
    for name in tables:
        quoted = '"' + name.replace('"', '""') + '"'
        source = "public." + quoted
        if after_a and name == "orden_examen":
            source = "(SELECT id_orden_examen,id_consulta_procedimiento_paso,fecha_creacion,indicaciones FROM public.orden_examen)"
        rows[name] = json.loads(sql(
            "SELECT json_build_object('filas',count(*),'hash',md5(coalesce(string_agg(row_to_json(r)::text,'' ORDER BY row_to_json(r)::text),''))) FROM "
            + source + " r;"))
    exclude_column = " AND NOT(table_name='orden_examen' AND column_name='id_examen')" if after_a else ""
    exclude_constraint = " AND NOT(conrelid='public.orden_examen'::regclass AND conname='fk_orden_examen_examen')" if after_a else ""
    rows["__columnas"] = sql("SELECT md5(string_agg(row_to_json(c)::text,'' ORDER BY table_name,ordinal_position)) FROM (SELECT table_name,column_name,data_type,is_nullable,column_default,ordinal_position FROM information_schema.columns WHERE table_schema='public'" + exclude_column + ") c;")
    rows["__restricciones"] = sql("SELECT md5(string_agg(conrelid::text||conname||pg_get_constraintdef(oid)||convalidated::text,'' ORDER BY conrelid,conname)) FROM pg_constraint WHERE connamespace='public'::regnamespace" + exclude_constraint + ";")
    return rows


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--verificar-manifest", type=Path)
    parser.add_argument("--despues-a", action="store_true", help="Solo verificar: permite exclusivamente la columna/FK aprobadas de A")
    args = parser.parse_args()
    if args.verificar_manifest:
        manifest = json.loads(args.verificar_manifest.read_text())
        archive = args.verificar_manifest.parent / manifest["archive"]
        with archive.open("rb") as source:
            digest = hashlib.file_digest(source, "sha256").hexdigest()
        if manifest.get("database") != "clinica" or not manifest.get("fullArchiveRead") or not manifest.get("unchangedDuringBackup") or digest != manifest["sha256"]:
            raise RuntimeError("El respaldo no conserva sus verificaciones")
        if args.despues_a:
            compatible = sql("SELECT EXISTS(SELECT FROM information_schema.columns WHERE table_schema='public' AND table_name='orden_examen' AND column_name='id_examen' AND data_type='uuid' AND is_nullable='YES' AND column_default IS NULL) AND EXISTS(SELECT FROM pg_constraint WHERE conrelid='public.orden_examen'::regclass AND confrelid='public.examen'::regclass AND conname='fk_orden_examen_examen' AND contype='f' AND convalidated AND confupdtype='c' AND confdeltype='r' AND conkey=ARRAY[(SELECT attnum FROM pg_attribute WHERE attrelid='public.orden_examen'::regclass AND attname='id_examen')]);")
            if compatible != "t":
                raise RuntimeError("La operación A no coincide con la propuesta")
        if snapshot(args.despues_a) != manifest["snapshot"]:
            raise RuntimeError("Cambió el contenido o esquema fuera del alcance permitido; revisar y renovar respaldo/plan")
        print("Respaldo SHA-256 válido; contenido/esquema coinciden" + (" salvo la columna/FK de A" if args.despues_a else "") + ". Ninguna escritura PostgreSQL.")
        return
    if args.despues_a:
        parser.error("--despues-a solo acompaña a --verificar-manifest")
    os.umask(0o077)
    started = datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ")
    directory = ROOT / ".gt02/backups" / ("clinica_pre_hu34_" + started)
    directory.mkdir(parents=True, exist_ok=False, mode=0o700)
    before = snapshot()
    archive = directory / "clinica.dump"
    with archive.open("xb") as output:
        subprocess.run(["docker", "exec", CONTAINER, "pg_dump", "-U", "postgres", "--dbname=clinica", "--format=custom", "--no-password"],
                       stdout=output, check=True)
    # --list y --file no se conectan a PostgreSQL; nunca se pasa --dbname a pg_restore.
    with archive.open("rb") as source:
        toc = subprocess.check_output(["docker", "exec", "-i", CONTAINER, "pg_restore", "--list"], stdin=source, text=True)
    tables = re.findall(r"^\d+; \d+ \d+ TABLE public (\S+) ", toc, re.MULTILINE)
    data = re.findall(r"^\d+; \d+ \d+ TABLE DATA public (\S+) ", toc, re.MULTILINE)
    expected = sorted(k for k in before if not k.startswith("__"))
    if sorted(tables) != expected or sorted(data) != expected:
        raise RuntimeError("El archivo no contiene las20tablas y sus20entradas de datos")
    with archive.open("rb") as source:
        subprocess.run(["docker", "exec", "-i", CONTAINER, "pg_restore", "--file=/dev/null"], stdin=source, check=True)
    with archive.open("rb") as source:
        digest = hashlib.file_digest(source, "sha256").hexdigest()
    after = snapshot()
    manifest = {
        "database": "clinica", "createdUtc": started,
        "archive": archive.name, "bytes": archive.stat().st_size, "sha256": digest,
        "tablesInArchive": len(tables), "tableDataEntries": len(data),
        "fullArchiveRead": True, "restoredIntoDatabase": False,
        "unchangedDuringBackup": before == after,
        "snapshot": before,
        "postgresqlVersion": sql("SELECT version();"),
    }
    (directory / "manifest.json").write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n")
    (directory / "toc.txt").write_text(toc)
    (directory / "SHA256SUMS").write_text(digest + "  clinica.dump\n")
    print(json.dumps({k: v for k, v in manifest.items() if k not in ("snapshot", "postgresqlVersion")}, ensure_ascii=False))
    print("Directorio privado:", directory)
    if before != after:
        raise RuntimeError("Cambió la base durante el respaldo: revisar y repetir antes de aplicar A/B")


if __name__ == "__main__":
    main()
