#!/usr/bin/env python3
"""Liberty CRUD aislado: reutiliza solo un PostgreSQL demo de propiedad verificada."""
from pathlib import Path
import argparse
import hashlib
import os
import shutil
import socket
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
STATE = ROOT / ".galenos-demo/crud"
SERVER = "GalenosCrudDemo"


def private(path, text):
    path.parent.mkdir(parents=True, exist_ok=True)
    with os.fdopen(os.open(path, os.O_WRONLY | os.O_CREAT | os.O_TRUNC, 0o600), "w") as f:
        f.write(text)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("action", choices=["start", "stop", "status"])
    args = parser.parse_args()
    source = Path(os.environ.get("GALENOS_CRUD_SOURCE", str(ROOT / ".galenos-demo"))).resolve()
    runtime = Path(os.environ.get("GALENOS_CRUD_LIBERTY", str(source / "runtime/wlp")))
    env = dict(os.environ, JAVA_HOME=os.environ.get("JAVA_HOME", "/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home"),
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
    server = STATE / "usr/servers" / SERVER
    (server / "apps").mkdir(parents=True, exist_ok=True)
    (server / "jdbc").mkdir(exist_ok=True)
    (STATE / "usr/shared/resources/primefaces").mkdir(parents=True, exist_ok=True)
    shutil.copyfile(ROOT / "target/GalenosSV.war", server / "apps/GalenosSV.war")
    for p in (source / "usr/servers/GalenosSVDemo/jdbc").glob("*.jar"):
        shutil.copyfile(p, server / "jdbc" / p.name)
    for p in (source / "usr/shared/resources/primefaces").glob("*.jar"):
        shutil.copyfile(p, STATE / "usr/shared/resources/primefaces" / p.name)
    shutil.copyfile(ROOT / "src/main/liberty/config/server.xml", server / "server.xml")
    values.update(HTTP_PORT=str(http), POSTGRESQL_DB_NAME="galenos_demo_crud",
                  GALENOS_CRUD_DEMO="true", GALENOS_API_MODE="disabled",
                  GALENOS_CORS_ORIGINS="http://127.0.0.1:3000,http://localhost:3000")
    private(server / "server.env", "\n".join(k + "=" + v for k, v in values.items()) + "\n")
    print("CRUD sintetico:", "http://localhost:" + str(http) + "/GalenosSV/resources/v2", flush=True)
    raise SystemExit(subprocess.call(command, env=env))


if __name__ == "__main__":
    main()
