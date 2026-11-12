#!/usr/bin/env python3
"""HTTP real contra Java y PostgreSQL con fixture GT02 sintético; no usa clinica."""
import json
import os
from pathlib import Path
from urllib.request import Request, urlopen
from urllib.error import HTTPError

BASE = os.environ.get("GT02_API", "http://127.0.0.1:19085/GalenosSV/resources/")
ROOT = Path(__file__).resolve().parents[2]
checks = []


def uid(n):
    return f"00000000-0000-4000-8000-{n:012d}"


def call(method, path, status=200, data=None, token=None, headers=None, pdf=False):
    request_headers = {
        "Accept": "application/pdf, application/json" if pdf else "application/json",
        **(headers or {}),
    }
    if data is not None:
        request_headers["Content-Type"] = "application/json"
    if token:
        request_headers["Authorization"] = "Bearer " + token
    request = Request(
        BASE + path,
        method=method,
        headers=request_headers,
        data=None if data is None else json.dumps(data).encode(),
    )
    try:
        response = urlopen(request, timeout=15)
    except HTTPError as error:
        response = error
    body = response.read()
    assert response.status == status, (method, path, response.status, body[:300])
    assert response.headers.get("Cache-Control") == "no-store"
    checks.append(f'{method} {path.split("?")[0]} -> {status}')
    if status >= 400:
        payload = json.loads(body)
        assert set(payload) == {"mensaje"}
        assert all(
            value not in payload["mensaje"] for value in ["SQLException", "java.", "SELECT "]
        )
    return (body if pdf or status == 204 else json.loads(body)), response.headers


def identify(portal, value, status=200, extra=None):
    return call(
        "POST",
        "v2/identidad_academica/" + portal,
        status,
        {"idTipoDocumento": uid(1), "valor": value, **(extra or {})},
    )[0]


def main():
    types, _ = call("GET", "v1/tipo_documento?first=0&max=100")
    assert len(types) == 2
    for data in [
        {"valor": "ABC"},
        {"idTipoDocumento": uid(1), "valor": ""},
        {"idTipoDocumento": uid(1), "valor": "abc"},
        {"idTipoDocumento": uid(2), "valor": "ABC"},
    ]:
        call("POST", "v2/identidad_academica/paciente", 422, data)
    identify("paciente", "ZZZ", 404)
    identify("paciente", "ABC", 422, {"idPersona": uid(41)})
    identify("paciente", "INA", 403)
    identify("paciente", "EMP", 403)
    identify("empleado", "ABC", 403)
    identify("empleado", "ZZZ", 404)
    identify("empleado", "abc", 422)
    patient = identify("paciente", "ABC")
    other = identify("paciente", "DEF")
    assert len(patient["contexto"]["asignaciones"]) == 2
    assert len(other["contexto"]["asignaciones"]) == 1
    assert set(patient["contexto"]) == {
        "tipo",
        "idPersona",
        "personaNombre",
        "asignaciones",
        "zonaHoraria",
    }
    token = patient["token"]
    call("GET", "v2/identidad_academica/paciente", token=token)
    call("GET", "v2/identidad_academica/empleado", 403, token=token)
    call("GET", "v2/consulta?idClinica=" + uid(11), 403, token=token)
    call("GET", "v2/persona", 401)
    call(
        "GET",
        "v2/citas_paciente?idClinica=" + uid(11) + "&fechaDesde=2026-10-22&fechaHasta=2026-10-22",
        401,
    )
    query = f"v2/citas_paciente?idClinica={uid(11)}&fechaDesde=2026-10-22&fechaHasta=2026-10-22"
    rows, headers = call("GET", query, token=token)
    assert len(rows) == 2 and headers["Total-registros"] == "2"
    assert rows[0]["idConsulta"] == uid(1002)
    assert rows[0]["procedimientos"][0]["procedimientoNombre"] == "Procedimiento sintético"
    assert set(rows[0]) == {
        "idConsulta",
        "idClinica",
        "clinicaNombre",
        "fechaInicio",
        "fechaFin",
        "procedimientos",
    }
    page, headers = call("GET", query + "&max=1&first=1", token=token)
    assert len(page) == 1 and headers["Total-registros"] == "2"
    empty, _ = call("GET", query.replace("2026-10-22", "2026-10-24"), token=token)
    assert empty == []
    rows_b, _ = call("GET", query.replace(uid(11), uid(12)), token=token)
    assert len(rows_b) == 1 and rows_b[0]["idConsulta"] == uid(2002)
    call("GET", query, 403, token=other["token"])
    call("GET", query.replace("fechaDesde=2026-10-22", "fechaDesde=2026-10-23"), 422, token=token)
    call("GET", query.replace("fechaDesde=2026-10-22", "fechaDesde=2026-02-30"), 422, token=token)
    call("GET", query + "&idPersona=" + uid(42), 422, token=token)
    pdf, headers = call("GET", f"v2/citas_paciente/{uid(1002)}/ticket", token=token, pdf=True)
    assert pdf.startswith(b"%PDF-1.4") and pdf.endswith(b"%%EOF\n")
    assert headers["Content-Type"] == "application/pdf"
    assert headers["Content-Disposition"] == f'attachment; filename="cita-{uid(1002)}.pdf"'
    out = ROOT / ".gt02/evidence"
    out.mkdir(parents=True, exist_ok=True)
    (out / "ticket.pdf").write_bytes(pdf)
    for id_ in [uid(2001), uid(999999)]:
        call(
            "GET",
            f"v2/citas_paciente/{id_}/ticket",
            404,
            token=token,
            headers={"Accept": "application/pdf, application/json"},
        )
    employee = identify("empleado", "EMP")
    med = identify("empleado", "MED")
    assert len(employee["contexto"]["asignaciones"]) == 2
    assert len(med["contexto"]["asignaciones"]) == 1
    assert {a["idClinica"] for a in employee["contexto"]["asignaciones"]} == {uid(11), uid(12)}
    et = employee["token"]
    context, _ = call("GET", "v2/identidad_academica/empleado", token=et)
    assert context["idPersona"] == uid(45)
    clinics, _ = call("GET", "v2/clinica_disponible", token=et)
    assert {c["idClinica"] for c in clinics} == {uid(11), uid(12)}
    call("GET", query, 403, token=et)
    call("GET", f"v2/consulta?idClinica={uid(11)}", token=et, headers={"X-Persona-Rol": uid(201)})
    call(
        "GET",
        f"v2/consulta?idClinica={uid(12)}",
        403,
        token=et,
        headers={"X-Persona-Rol": uid(201)},
    )
    call(
        "GET",
        f"v2/consulta?idClinica={uid(11)}",
        403,
        token=et,
        headers={"X-Persona-Rol": uid(202)},
    )
    call("POST", "v2/consulta?idClinica=" + uid(11), 403, data={}, token=et)
    call("GET", "v2/persona/" + uid(42), 403, token=et)
    _, cors = call(
        "OPTIONS",
        "v2/identidad_academica/empleado",
        204,
        headers={
            "Origin": "http://127.0.0.1:3000",
            "Access-Control-Request-Method": "GET",
            "Access-Control-Request-Headers": "authorization,x-persona-rol",
        },
    )
    assert cors["Access-Control-Allow-Origin"] == "http://127.0.0.1:3000"
    call("DELETE", "v2/identidad_academica/empleado", 403, token=token)
    call("DELETE", "v2/identidad_academica/empleado", 204, token=et)
    call("GET", "v2/identidad_academica/empleado", 401, token=et)
    call("DELETE", "v2/identidad_academica/paciente", 204, token=token)
    call("GET", query, 401, token=token)
    call("DELETE", "v2/identidad_academica/paciente", 204, token=token)
    report = {
        "scope": "HTTP real -> Liberty Java -> PostgreSQL temporal con datos ficticios; clinica no modificada",
        "checks": checks,
        "pdfBytes": len(pdf),
    }
    (out / "http.json").write_text(json.dumps(report, ensure_ascii=False, indent=2))
    print(
        json.dumps(
            {
                "passed": len(checks),
                "pdfBytes": len(pdf),
                "database": "temporal ficticia; no clinica",
            }
        )
    )


if __name__ == "__main__":
    main()
