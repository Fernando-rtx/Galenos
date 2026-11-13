#!/usr/bin/env python3
"""REST + PostgreSQL real, exclusivamente sobre el fixture galenos_demo_crud."""

from concurrent.futures import ThreadPoolExecutor
import hashlib
import json
import os
from pathlib import Path
import subprocess
from threading import Barrier
from urllib.error import HTTPError
from urllib.request import Request, urlopen


ROOT = Path(__file__).resolve().parents[2]
DATABASE = "galenos_demo_crud"
CLINIC = "00000000-0000-4000-8000-000000000011"
OTHER_CLINIC = "00000000-0000-4000-8000-000000000012"
STEP = "00000000-0000-4000-8000-000000019001"
PARENT = "00000000-0000-4000-8000-000000018001"
RESPONSIBLE = "00000000-0000-4000-8000-000000000201"
BASE = os.environ.get(
    "GALENOS_OPERATIVO_TEST_URL", "http://127.0.0.1:19083/GalenosSV/resources/v2"
).rstrip("/")


def sql(statement):
    result = subprocess.run(
        ["docker", "exec", "-i", "postgres-db", "psql", "-X", "-qAt",
         "-v", "ON_ERROR_STOP=1", "-U", "postgres", "-d", DATABASE],
        input=statement, text=True, capture_output=True, check=True,
    )
    return result.stdout.strip()


def request(method, path, data=None):
    payload = None if data is None else json.dumps(data).encode()
    req = Request(BASE + path, data=payload, method=method)
    if payload is not None:
        req.add_header("Content-Type", "application/json")
    try:
        with urlopen(req, timeout=10) as response:
            return response.status, json.load(response)
    except HTTPError as error:
        return error.code, json.load(error)


def step_body(status):
    return {
        "idConsultaProcedimiento": PARENT,
        "idPersonaRol": RESPONSIBLE,
        "fechaInicio": "2026-10-22T06:00:00Z",
        "fechaFin": None,
        "estado": status,
    }


def assert_step(status, finished):
    http, body = request("GET", f"/consulta_procedimiento_paso/{STEP}?idClinica={CLINIC}")
    assert http == 200 and body["estado"] == status, (http, body)
    assert (body["fechaFin"] is not None) == finished, body
    persisted = sql(
        "SELECT estado,fecha_fin IS NOT NULL FROM consulta_procedimiento_paso "
        f"WHERE id_consulta_procedimiento_paso='{STEP}';"
    )
    assert persisted == f"{status}|{'t' if finished else 'f'}", persisted


def main():
    schema = (ROOT / "clinica_ppi115_2026_08_20.sql").read_text()
    fixture = (ROOT / "test/fixtures/crud_demo.sql").read_text()
    digest = hashlib.sha256((schema + fixture).encode()).hexdigest()
    assert sql("SELECT revision FROM galenos_operativo_fixture;") == digest, \
        "La base no contiene el fixture aislado esperado"
    http, context = request("GET", "/contexto")
    assert http == 200 and context["actor"] == "academico-local" and CLINIC in context["clinicas"]

    # El reinicio afecta solo a esta fila ficticia y permite repetir la prueba.
    sql(
        "UPDATE consulta_procedimiento_paso SET estado='PENDIENTE',fecha_fin=NULL "
        f"WHERE id_consulta_procedimiento_paso='{STEP}';"
    )
    assert_step("PENDIENTE", False)
    http, _ = request("PUT", f"/consulta_procedimiento_paso/{STEP}?idClinica={CLINIC}",
                      step_body("EN_CURSO"))
    assert http == 200, http
    assert_step("EN_CURSO", False)
    http, _ = request("PUT", f"/consulta_procedimiento_paso/{STEP}?idClinica={CLINIC}",
                      step_body("COMPLETADO"))
    assert http == 200, http
    assert_step("COMPLETADO", True)
    http, _ = request("PUT", f"/consulta_procedimiento_paso/{STEP}?idClinica={CLINIC}",
                      step_body("COMPLETADO"))
    assert http == 409, http
    http, _ = request("GET", f"/consulta_procedimiento_paso/{STEP}?idClinica={OTHER_CLINIC}")
    assert http == 404, http

    sql(
        "UPDATE consulta_procedimiento_paso SET estado='EN_CURSO',fecha_fin=NULL "
        f"WHERE id_consulta_procedimiento_paso='{STEP}';"
    )
    barrier = Barrier(2)

    def complete():
        barrier.wait()
        return request("PUT", f"/consulta_procedimiento_paso/{STEP}?idClinica={CLINIC}",
                       step_body("COMPLETADO"))[0]

    with ThreadPoolExecutor(max_workers=2) as pool:
        results = sorted(pool.map(lambda _: complete(), range(2)))
    assert results == [200, 409], results
    assert_step("COMPLETADO", True)
    print("Aprobado: REST/SQL PENDIENTE → EN_CURSO → COMPLETADO, duplicado 409, otra clínica 404 y concurrencia 200/409")


if __name__ == "__main__":
    main()
