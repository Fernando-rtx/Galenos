#!/usr/bin/env python3
"""PROPUESTA de fase REST de B. No ejecutar sin autorización B independiente.
No crea catálogos ni abre permisos administrativos. No reintenta POST.
"""
import argparse
import hashlib
import json
import os
import re
from pathlib import Path
from urllib.error import HTTPError
from urllib.parse import urlencode
from urllib.request import Request, urlopen

ROOT = Path(__file__).resolve().parents[1]
PLAN = ROOT / "db/proposals/gt02-hu34-20261115/escenario.psql"
BASE = "http://127.0.0.1:9080/GalenosSV/resources/"


def variables():
    result = {}
    for line in PLAN.read_text().splitlines():
        if not line or line.startswith("--"):
            continue
        match = re.fullmatch(r"\\set ([a-z_]+) '([^']*)'", line)
        if not match:
            raise ValueError("El manifiesto contiene una instrucción no prevista")
        result[match[1]] = match[2]
    if not result.get("examen_id") or result.get("paso_examen_id") != result["plantilla_enfermeria"]:
        raise ValueError("Falta confirmar la relación examen/paso del plan B; no se enviaron peticiones")
    return result


def call(method, route, data=None, token=None, assignment=None):
    headers = {"Accept": "application/json", "Cache-Control": "no-store"}
    if token:
        headers["Authorization"] = "Bearer " + token
    if assignment:
        headers["X-Persona-Rol"] = assignment
    if data is not None:
        headers["Content-Type"] = "application/json"
    request = Request(BASE + route, method=method, headers=headers,
                      data=None if data is None else json.dumps(data).encode())
    try:
        with urlopen(request, timeout=20) as response:
            raw = response.read()
            return None if not raw else json.loads(raw)
    except HTTPError as error:
        raise RuntimeError(f"REST respondió HTTP{error.code}; revisar antes de reenviar") from None


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--autorizacion", required=True)
    parser.add_argument("--respaldo-manifest", required=True, type=Path)
    args = parser.parse_args()
    if args.autorizacion != "AUTORIZAR_B_GT02_HU34_20261115":
        parser.error("Falta autorización específica de B")
    v = variables()  # Falla antes de cualquier red si la decisión de examen está pendiente.
    backup = json.loads(args.respaldo_manifest.read_text())
    archive = args.respaldo_manifest.parent / backup["archive"]
    with archive.open("rb") as source:
        sha = hashlib.file_digest(source, "sha256").hexdigest()
    if backup.get("database") != "clinica" or not backup.get("fullArchiveRead") or not backup.get("unchangedDuringBackup") or backup.get("tablesInArchive") != 20 or sha != backup["sha256"]:
        raise ValueError("El respaldo no cumple las verificaciones del plan")
    os.umask(0o077)
    output = ROOT / ".gt02/preparacion-hu34"
    output.mkdir(parents=True, exist_ok=True, mode=0o700)
    log = {"status": "iniciado", "consulta": v["consulta"], "asociacion": v["asociacion"], "confirmado": []}
    file = output / "fase-rest.json"
    sessions = []

    def identify(actor):
        session = call("POST", "v2/identidad_academica/empleado", {"idTipoDocumento": v["tipo_documento"], "valor": v["pasaporte_" + actor]})
        sessions.append(session["token"])
        context = session["contexto"]
        assignment = next((a for a in context["asignaciones"] if a["idPersonaRol"] == v["asignacion_" + actor]), None)
        if context["tipo"] != "EMPLEADO" or context["idPersona"] != v[actor] or not assignment or assignment["idClinica"] != v["clinica"] or assignment["idRol"] != v["rol_" + actor]:
            raise ValueError("El contexto recibido no coincide con la asignación académica")
        return session["token"], assignment, {"idPersona": context["idPersona"], "idClinica": assignment["idClinica"], "idRol": assignment["idRol"]}

    try:
        token, assignment, context = identify("recepcion")
        route = "v2/atencion/consulta/" + v["consulta"] + "?" + urlencode(context)
        detail = call("GET", route, token=token, assignment=assignment["idPersonaRol"])
        if detail["idConsultaProcedimiento"] != v["asociacion"] or detail["idConsultaProcedimientoPaso"] != v["ejecucion_inicial"] or detail["estadoPasoActual"] != "PENDIENTE" or detail["valorActual"] or detail["historial"] or detail["ordenesActuales"]:
            raise ValueError("El escenario ya tiene atención o cambió; no sobrescribir")
        if not any(s["idPaso"] == v["plantilla_enfermeria"] for s in detail["siguientes"]):
            raise ValueError("La transición real a Enfermería no está disponible")
        body = {**context, "idConsulta": detail["idConsulta"], "idConsultaProcedimientoPaso": detail["idConsultaProcedimientoPaso"],
                "accion": "GUARDAR_PARCIAL", "valor": "Recepción académica: documento verificado y paciente recibido.", "examenes": []}
        file.write_text(json.dumps(log, indent=2))
        saved = call("POST", "v2/atencion/registrar", body, token, assignment["idPersonaRol"])
        if saved["accion"] != "GUARDAR_PARCIAL" or saved["idConsultaProcedimientoPaso"] != v["ejecucion_inicial"] or saved["estado"] != "EN_CURSO":
            raise ValueError("El servidor no confirmó el guardado esperado; revisar mediante GET")
        log["confirmado"].append("GUARDAR_PARCIAL recepción")
        file.write_text(json.dumps(log, indent=2))
        advanced = call("POST", "v2/atencion/registrar", {**body, "accion": "AVANZAR", "idPasoSiguiente": v["plantilla_enfermeria"]}, token, assignment["idPersonaRol"])
        if advanced["accion"] != "AVANZAR" or advanced["idPasoActual"] != v["plantilla_enfermeria"] or advanced["finalizada"]:
            raise ValueError("El servidor no confirmó la transición esperada; revisar mediante GET")
        log["confirmado"].append("AVANZAR recepción→enfermería")
        log["ejecucion_enfermeria"] = advanced["idConsultaProcedimientoPaso"]
        file.write_text(json.dumps(log, indent=2))
        nurse, nurse_assignment, nurse_context = identify("enfermeria")
        detail = call("GET", "v2/atencion/consulta/" + v["consulta"] + "?" + urlencode(nurse_context), token=nurse, assignment=nurse_assignment["idPersonaRol"])
        if detail["idPasoActual"] != v["plantilla_enfermeria"] or not any(h["idConsultaProcedimientoPaso"] == v["ejecucion_inicial"] and h["estado"] == "COMPLETADO" and h["valor"] == body["valor"] and h["fechaFin"] and h["responsable"] for h in detail["historial"]):
            raise ValueError("No se confirmó el historial persistido")
        if not any(e["idExamen"] == v["examen_id"] for e in detail["examenes"]):
            raise ValueError("El examen confirmado no aparece como permitido")
        log["status"] = "preparado_para_HU3_HU4_enfermeria"
        print("Primera atención y transición confirmadas por REST; verificar ahora HU3/HU4 en la SPA.")
    except Exception:
        log["status"] = "interrumpido_revisar_GET_antes_de_reenviar"
        raise
    finally:
        log["sesiones_no_revocadas"] = 0
        for token in sessions:
            try:
                call("DELETE", "v2/identidad_academica/empleado", token=token)
            except Exception:
                log["sesiones_no_revocadas"] += 1
        file.write_text(json.dumps(log, indent=2))
        if log["sesiones_no_revocadas"]:
            print("Advertencia: no se confirmó la revocación de todas las sesiones académicas.")


if __name__ == "__main__":
    main()
