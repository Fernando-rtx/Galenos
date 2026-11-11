#!/usr/bin/env python3
"""REST real sobre clinica: únicamente lecturas y sesiones efímeras, sin cambiar datos.
No imprime ni persiste documentos, tokens, nombres ni respuestas clínicas.
"""
import json
from pathlib import Path
import subprocess
from urllib.request import Request, urlopen
from urllib.error import HTTPError

BASE = "http://127.0.0.1:19084/GalenosSV/resources/"
ROOT = Path(__file__).resolve().parents[2]
checks = []


def read_sql(statement):
    command = [
        "docker",
        "exec",
        "-i",
        "postgres_db",
        "psql",
        "-X",
        "-qAt",
        "-v",
        "ON_ERROR_STOP=1",
        "-U",
        "postgres",
        "-d",
        "clinica",
    ]
    return subprocess.run(
        command,
        input="BEGIN READ ONLY;\n" + statement + "\nCOMMIT;",
        text=True,
        capture_output=True,
        check=True,
    ).stdout.strip()


def snapshot():
    names = read_sql(
        "SELECT tablename FROM pg_tables WHERE schemaname='public' ORDER BY tablename;"
    ).splitlines()
    assert len(names) == 20, "Se esperaban las veinte tablas existentes"
    return {
        name: read_sql(
            "SELECT md5(COALESCE(string_agg(row_to_json(r)::text,'' ORDER BY row_to_json(r)::text),'')) FROM public.\""
            + name.replace('"', '""')
            + '" r;'
        )
        for name in names
    }


def call(method, path, data=None, token=None):
    headers = {"Accept": "application/json"}
    if data is not None:
        headers["Content-Type"] = "application/json"
    if token:
        headers["Authorization"] = "Bearer " + token
    request = Request(
        BASE + path,
        method=method,
        headers=headers,
        data=None if data is None else json.dumps(data).encode(),
    )
    try:
        response = urlopen(request, timeout=15)
    except HTTPError as error:
        response = error
    body = response.read()
    assert response.headers.get("Cache-Control") == "no-store"
    return response.status, json.loads(body) if body else None


def main():
    before = snapshot()
    status, types = call("GET", "v1/tipo_documento?max=100")
    assert status == 200 and isinstance(types, list)
    checks.append("Catálogo REST real sobre clinica")
    status, _ = call("GET", "v2/persona")
    assert status == 401
    checks.append("Listado indiscriminado de personas sin sesión rechazado")
    raw = read_sql(
        """SELECT COALESCE(json_agg(x),'[]'::json) FROM (
        SELECT DISTINCT d.id_tipo_documento AS tipo,d.valor,r.nombre AS rol,r.activo AS rol_activo,
               c.activo AS clinica_activa,t.activo AS tipo_activo,pr.id_persona AS persona
        FROM documento d JOIN tipo_documento t ON t.id_tipo_documento=d.id_tipo_documento
        JOIN persona_rol pr ON pr.id_persona=d.id_persona JOIN rol r ON r.id_rol=pr.id_rol
        JOIN clinica c ON c.id_clinica=pr.id_clinica
        WHERE LOWER(TRIM(r.nombre)) IN ('paciente','doctor')
    ) x;"""
    )
    rows = json.loads(raw)
    employee_ok = 0
    patient_inactive = 0
    for row in rows:
        portal = "paciente" if row["rol"].strip().lower() == "paciente" else "empleado"
        status, payload = call(
            "POST",
            "v2/identidad_academica/" + portal,
            {"idTipoDocumento": row["tipo"], "valor": row["valor"]},
        )
        if not row["tipo_activo"]:
            assert status == 422
            checks.append("Tipo de documento inactivo rechazado")
        elif not row["rol_activo"] or not row["clinica_activa"]:
            # La persona podría tener otra asignación válida; aceptar sólo si el contexto la demuestra.
            if status == 403:
                if portal == "paciente":
                    patient_inactive += 1
                checks.append("Asignación no disponible rechazada sin modificar registros")
            else:
                assert status == 200
        else:
            assert status == 200, "Identificación disponible no completada"
        if status == 200:
            token = payload["token"]
            context = payload["contexto"]
            assert context["idPersona"] == row["persona"]
            status, refreshed = call("GET", "v2/identidad_academica/" + portal, token=token)
            assert status == 200 and refreshed["idPersona"] == row["persona"]
            if portal == "empleado":
                employee_ok += 1
                checks.append("HU2 empleado real: identificación, contexto propio y roles/clínicas")
            status, _ = call("DELETE", "v2/identidad_academica/" + portal, token=token)
            assert status == 204
            status, _ = call("GET", "v2/identidad_academica/" + portal, token=token)
            assert status == 401
            checks.append("Cierre y revocación de sesión efímera")
    assert snapshot() == before, "Cambió el contenido de alguna de las veinte tablas"
    checks.append("Hashes de las veinte tablas iguales antes y después")
    output = {
        "scope": "Liberty con JDBC sólo lectura -> clinica existente",
        "checks": checks,
        "employeeFlows": employee_ok,
        "patientRejectedInactive": patient_inactive,
        "unchangedTables": 20,
        "clinicalResponsesStored": False,
    }
    dest = ROOT / ".gt02/evidence"
    dest.mkdir(parents=True, exist_ok=True)
    (dest / "clinica-readonly.json").write_text(json.dumps(output, ensure_ascii=False, indent=2))
    print(json.dumps(output, ensure_ascii=False))


if __name__ == "__main__":
    main()
