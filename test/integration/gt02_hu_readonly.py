"""HU1/HU3/HU4: GET reales y sesiones efímeras, sin escrituras clínicas ni DELETE.
Requiere el runtime JDBC de solo lectura en localhost:19084 y clinica existente.
Solo publica estados, conteos y UUID del fixture; nunca documentos, notas o tokens.
"""
import argparse
import json
from datetime import datetime, timedelta
from uuid import UUID
from pathlib import Path
from zoneinfo import ZoneInfo
from urllib.request import Request, urlopen
from urllib.error import HTTPError
from gt02_clinica_readonly import read_sql, call, snapshot, BASE


def employee_get(path, token, assignment):
    request = Request(BASE + path, headers={
        'Authorization': 'Bearer ' + token, 'X-Persona-Rol': assignment,
    })
    try:
        response = urlopen(request, timeout=15)
    except HTTPError as error:
        response = error
    assert response.headers.get('Cache-Control') == 'no-store'
    return response.status, json.loads(response.read())


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--consulta', type=UUID,
                        default=UUID('e5bd6458-4469-5543-98b0-1a5b595559a3'))
    args = parser.parse_args()
    consultation = str(args.consulta)
    runtime = Path(__file__).resolve().parents[2] / '.gt02/usr/servers/GalenosGT02ReadOnly'
    assert 'default_transaction_read_only=on' in (runtime / 'server.xml').read_text(), 'Se requiere JDBC de solo lectura'
    values = dict(line.split('=', 1) for line in (runtime / 'server.env').read_text().splitlines()
                  if '=' in line and not line.startswith('#'))
    assert values.get('POSTGRESQL_DB_NAME') == 'clinica'
    assert values.get('POSTGRESQL_DB_HOST', 'localhost') in ('localhost', '127.0.0.1')
    assert values.get('POSTGRESQL_DB_PORT', '5432') == '5432'
    # La interpolación usa únicamente un UUID validado; nunca texto libre de usuario.
    academic = read_sql(f"""SELECT EXISTS(SELECT 1 FROM consulta c
      JOIN persona_rol pr USING(id_persona_rol) JOIN documento d USING(id_persona)
      WHERE c.id_consulta='{consultation}' AND d.valor LIKE 'ACAD%');""")
    assert academic == 't', 'La consulta debe pertenecer al fixture de documento ACAD'
    before = snapshot()
    rows = json.loads(read_sql(f"""SELECT COALESCE(json_agg(x),'[]') FROM (
      SELECT d.id_tipo_documento tipo,d.valor,pr.id_persona_rol asignacion,
             pr.id_clinica clinica,pr.id_rol rol,r.nombre
      FROM documento d JOIN persona_rol pr USING(id_persona) JOIN rol r USING(id_rol)
      JOIN tipo_documento t USING(id_tipo_documento) JOIN clinica c USING(id_clinica)
      WHERE t.activo AND r.activo AND c.activo AND (d.valor LIKE 'ACAD%' OR
        pr.id_persona IN (SELECT responsible.id_persona FROM consulta_procedimiento cp
          JOIN consulta_procedimiento_paso step USING(id_consulta_procedimiento)
          JOIN persona_rol responsible ON responsible.id_persona_rol=step.id_persona_rol
          WHERE cp.id_consulta='{consultation}'))
    ) x;"""))
    report = {'consulta': consultation, 'scope': 'REST real + SELECT, sin escrituras clínicas', 'checks': []}
    status, _ = call('GET', 'v2/empleado/consultas')
    assert status == 401, 'La bandeja sin sesión debe rechazarse'
    patient_only = json.loads(read_sql('''SELECT COALESCE(json_agg(x),'[]') FROM (
      SELECT d.id_tipo_documento tipo,d.valor FROM documento d JOIN tipo_documento t USING(id_tipo_documento)
      JOIN persona_rol pr USING(id_persona) JOIN rol r USING(id_rol) JOIN clinica c USING(id_clinica)
      WHERE d.valor LIKE 'ACAD%' AND t.activo AND c.activo AND r.activo AND lower(trim(r.nombre))='paciente'
      AND NOT EXISTS(SELECT 1 FROM persona_rol other JOIN rol staff ON staff.id_rol=other.id_rol
        JOIN clinica clinic ON clinic.id_clinica=other.id_clinica WHERE other.id_persona=pr.id_persona
        AND staff.activo AND clinic.activo AND lower(trim(staff.nombre))<>'paciente') LIMIT 1) x;'''))
    assert patient_only, 'Falta paciente sintético sin rol de empleado para comprobar HU2'
    status, _ = call('POST', 'v2/identidad_academica/empleado',
                     {'idTipoDocumento': patient_only[0]['tipo'], 'valor': patient_only[0]['valor']})
    assert status == 403, 'El paciente sin asignación de empleado no debe ingresar como empleado'
    report['checks'].append({'HU2': 'Bandeja sin sesión 401 y paciente como empleado 403'})
    patient_verified = False
    for row in rows:
        if row['nombre'].strip().lower() != 'paciente':
            continue
        status, session = call('POST', 'v2/identidad_academica/paciente',
                               {'idTipoDocumento': row['tipo'], 'valor': row['valor']})
        if status != 200:
            continue
        token = session['token']
        status, appointments = call('GET', f"v2/citas_paciente?idClinica={row['clinica']}"
                                    '&fechaDesde=2026-11-15&fechaHasta=2026-11-17&max=100', token=token)
        if status != 200 or not appointments:
            continue
        request = Request(BASE + f"v2/citas_paciente/{appointments[0]['idConsulta']}/ticket",
                          headers={'Authorization': 'Bearer ' + token})
        with urlopen(request, timeout=15) as response:
            pdf = response.read()
            assert pdf.startswith(b'%PDF-')
            assert response.headers.get('Cache-Control') == 'no-store'
        report['checks'].append({'HU1': 'GET citas y PDF Java', 'citas': len(appointments), 'pdfBytes': len(pdf)})
        patient_verified = True
        break
    assert patient_verified, 'Falta un paciente académico con cita para probar HU1'
    employee_verified = False
    denied = 0
    today = datetime.now(ZoneInfo('America/El_Salvador')).date()
    for row in rows:
        if row['nombre'].strip().lower() == 'paciente':
            continue
        status, session = call('POST', 'v2/identidad_academica/empleado',
                               {'idTipoDocumento': row['tipo'], 'valor': row['valor']})
        if status != 200:
            continue
        token = session['token']
        query = f"idClinica={row['clinica']}&idRol={row['rol']}"
        status, detail = employee_get(f'v2/atencion/consulta/{consultation}?{query}', token, row['asignacion'])
        if status == 403:
            denied += 1
            assert set(detail) == {'mensaje'}, 'El rechazo no debe exponer información clínica'
            continue
        if status != 200:
            continue
        status, context = call('GET', 'v2/identidad_academica/empleado', token=token)
        assert status == 200
        assert any(item['idPersonaRol'] == row['asignacion'] for item in context['asignaciones'])
        report['checks'].append({'HU2': 'Identidad y asignación propia recuperadas desde backend'})
        previous = detail.get('historial', [])
        historical_orders = [order for step in previous if step['estado'] == 'RETROCEDIDO'
                             for order in step.get('ordenes', [])]
        assert any(order['examen'] == 'Hemograma completo' for order in historical_orders)
        current_orders = detail.get('ordenesActuales', [])
        assert not ({order['idOrdenExamen'] for order in historical_orders}
                    & {order['idOrdenExamen'] for order in current_orders})
        report['checks'].append({'HU4': 'GET autorizado, orden histórica separada de actual',
                                'pasosHistoricos': len(previous), 'ordenesRetrocedidas': len(historical_orders),
                                'ordenesActuales': len(current_orders)})
        for start, end in [('2026-11-15', '2026-11-16'), (str(today), str(today)),
                           (str(today + timedelta(days=1)), str(today + timedelta(days=30)))]:
            status, appointments = employee_get(
                f'v2/empleado/consultas?{query}&fechaDesde={start}&fechaHasta={end}&max=100',
                token, row['asignacion'])
            assert status == 200 and isinstance(appointments, list)
            report['checks'].append({'HU3': f'{start}..{end}', 'status': status, 'registros': len(appointments)})
        employee_verified = True
        break
    assert employee_verified, 'No hay responsable académico que pueda leer esta consulta activa'
    assert denied > 0, 'No se pudo demostrar un rechazo 403 con el fixture disponible'
    assert snapshot() == before, 'Cambió alguna tabla; revisar concurrencia antes de atribuir el cambio'
    report.update({'status': 'PASS', 'unchangedTables': 20, 'rejectedForeignAssignments': denied,
                   'clinicalWrites': False, 'sessions': 'Solo memoria, expiran o se descartan al parar el runtime'})
    print(json.dumps(report, ensure_ascii=False, indent=2))


if __name__ == '__main__':
    main()
