"""Filtro de atención: SELECT y GET reales sobre clinica; solo crea una sesión en memoria.
No imprime documentos, tokens, nombres ni notas. No registra ni avanza consultas.
"""
import json
from datetime import datetime
from urllib.parse import urlencode
from urllib.request import Request, urlopen
from urllib.error import HTTPError
from zoneinfo import ZoneInfo
import gt02_clinica_readonly as db


def main():
    db.BASE = 'http://127.0.0.1:9080/GalenosSV/resources/'
    before = db.snapshot()
    employees = json.loads(db.read_sql('''SELECT COALESCE(json_agg(x),'[]') FROM (
      SELECT pr.id_persona_rol asignacion,pr.id_clinica clinica,d.id_tipo_documento tipo,d.valor
      FROM persona_rol pr JOIN rol r USING(id_rol) JOIN clinica cl USING(id_clinica)
      JOIN documento d USING(id_persona) JOIN tipo_documento t USING(id_tipo_documento)
      WHERE r.activo AND cl.activo AND t.activo AND lower(trim(r.nombre))<>'paciente'
      AND EXISTS(SELECT 1 FROM consulta c JOIN persona_rol p ON p.id_persona_rol=c.id_persona_rol
        WHERE p.id_clinica=pr.id_clinica) ORDER BY pr.id_persona_rol LIMIT 1
    ) x;'''))
    assert employees, 'Se requiere un empleado con documento válido y citas en su clínica'
    employee = employees[0]
    status, session = db.call('POST', 'v2/identidad_academica/empleado',
                             {'idTipoDocumento': employee['tipo'], 'valor': employee['valor']})
    assert status == 200, f'Identidad HTTP {status}'
    # El UUID viene de PostgreSQL, no de texto de entrada.
    rows = json.loads(db.read_sql(f'''SELECT COALESCE(json_agg(x),'[]') FROM (
      SELECT c.id_consulta id,c.id_persona_rol paciente,c.fecha_inicio inicio,
        (EXISTS(SELECT 1 FROM consulta_procedimiento cp WHERE cp.id_consulta=c.id_consulta)
         AND NOT EXISTS(SELECT 1 FROM consulta_procedimiento cp WHERE cp.id_consulta=c.id_consulta AND cp.fecha_fin IS NULL)
         AND NOT EXISTS(SELECT 1 FROM consulta_procedimiento_paso paso JOIN consulta_procedimiento cp USING(id_consulta_procedimiento)
           WHERE cp.id_consulta=c.id_consulta AND paso.estado IN ('PENDIENTE','EN_CURSO'))) finalizada
      FROM consulta c JOIN persona_rol p USING(id_persona_rol)
      WHERE p.id_clinica='{employee['clinica']}' ORDER BY c.id_consulta
    ) x;'''))

    def get(query, authenticated=True):
        headers = {'Accept': 'application/json'}
        if authenticated:
            headers.update({'Authorization': 'Bearer ' + session['token'],
                            'X-Persona-Rol': employee['asignacion']})
        request = Request(db.BASE + 'v2/consulta?' + urlencode({'idClinica': employee['clinica'], **query}), headers=headers)
        try:
            response = urlopen(request, timeout=20)
        except HTTPError as error:
            response = error
        assert response.headers.get('Cache-Control') == 'no-store'
        return response.status, json.loads(response.read()), response.headers.get('Total-registros')

    def verify(query, expected):
        actual = []
        total = None
        while True:
            status, page, total = get({**query, 'first': len(actual), 'max': 20})
            assert status == 200, f'GET consulta HTTP {status}'
            actual += page
            if len(actual) >= int(total):
                break
            assert page, 'La paginación dejó de avanzar'
        assert [row['idConsulta'] for row in actual] == [row['id'] for row in expected]
        assert int(total) == len(expected)

    pending = [row for row in rows if not row['finalizada']]
    completed = [row for row in rows if row['finalizada']]
    assert pending and completed, 'Se requieren ejemplos pendientes y finalizados para demostrar ambos filtros'
    verify({}, rows)
    verify({'estadoAtencion': 'PENDIENTES'}, pending)
    verify({'estadoAtencion': 'FINALIZADAS'}, completed)
    status, page, total = get({'estadoAtencion': 'FINALIZADAS', 'first': len(completed), 'max': 1})
    assert status == 200 and page == [] and int(total) == len(completed), 'Total incorrecto fuera de la última página'
    candidate = pending[0]
    date = datetime.fromisoformat(candidate['inicio']).astimezone(ZoneInfo('America/El_Salvador')).date().isoformat()
    expected = [row for row in pending if row['paciente'] == candidate['paciente']
                and datetime.fromisoformat(row['inicio']).astimezone(ZoneInfo('America/El_Salvador')).date().isoformat() == date]
    verify({'estadoAtencion': 'PENDIENTES', 'idPersonaRol': candidate['paciente'],
            'fechaDesde': date, 'fechaHasta': date}, expected)
    assert get({'estadoAtencion': 'COMPLETADO'})[0] == 422
    assert get({'estadoAtencion': 'FINALIZADAS'}, authenticated=False)[0] == 401
    assert db.snapshot() == before, 'Hubo cambios concurrentes en las tablas; revisar antes de atribuirlos'
    print(json.dumps({'status': 'PASS', 'todas': len(rows), 'pendientes': len(pending), 'finalizadas': len(completed),
                      'checks': ['Estados contra SELECT', 'Total antes de paginar', 'Página final vacía con total correcto',
                                 'Estado junto a fecha y paciente', 'Estado inválido 422', 'Sin sesión 401'],
                      'tablasSinCambios': 20, 'escriturasClinicas': False}, ensure_ascii=False))


if __name__ == '__main__':
    main()
