"""HTTP y PostgreSQL reales, exclusivamente contra galenos_demo_crud sintética."""
import base64
import json
import os
from pathlib import Path
import subprocess
import unittest
import urllib.request
import urllib.error
import uuid

ROOT = Path(__file__).resolve().parents[2]
BASE = 'http://localhost:' + os.environ.get('GALENOS_CRUD_HTTP_PORT', '19083') + '/GalenosSV/resources/v2/'
CONTAINER = os.environ.get('GALENOS_CRUD_CONTAINER', 'galenos-demo-db')
CREDENTIALS = json.loads((ROOT / '.galenos-demo/crud/credentials.json').read_text())

def http(path, method='GET', data=None, role='editor', origin='http://127.0.0.1:3000'):
    headers = {'Origin': origin}
    if role:
        c = CREDENTIALS[role]
        headers['Authorization'] = 'Basic ' + base64.b64encode((c['username'] + ':' + c['password']).encode()).decode()
    if data is not None: headers['Content-Type'] = 'application/json'
    request = urllib.request.Request(BASE + path, data=None if data is None else json.dumps(data).encode(), headers=headers, method=method)
    try: response = urllib.request.urlopen(request, timeout=15)
    except urllib.error.HTTPError as error: response = error
    raw = response.read()
    return response.status, response.headers, json.loads(raw) if raw else None

class CatalogosHTTP(unittest.TestCase):
    def test_authentication_and_read_only_authorization(self):
        status, headers, body = http('tipo_documento', role=None)
        self.assertEqual(status, 401); self.assertEqual(headers['Access-Control-Allow-Origin'], 'http://127.0.0.1:3000')
        self.assertIn('mensaje', body)
        self.assertEqual(http('tipo_documento')[0], 200)
        self.assertEqual(http('tipo_documento', 'POST', {'nombre':'No autorizado'}, role='lector')[0], 403)
        self.assertEqual(http('contexto', role='lector')[2]['puedeEscribir'], False)

    def test_pagination_validation_missing_and_method(self):
        status, headers, rows = http('tipo_documento?first=0&max=1')
        self.assertEqual(status, 200); self.assertEqual(len(rows), 1); self.assertGreaterEqual(int(headers['Total-registros']), 2)
        for query in ['first=-1', 'max=0', 'max=101']:
            with self.subTest(query=query): self.assertEqual(http('tipo_documento?' + query)[0], 422)
        self.assertEqual(http('tipo_documento/not-uuid')[0], 422)
        self.assertEqual(http('tipo_documento/' + str(uuid.uuid4()))[0], 404)
        self.assertEqual(http('tipo_documento/' + rows[0]['idTipoDocumento'], 'DELETE')[0], 405)

    def test_create_read_update_persist_and_deactivate_catalogs(self):
        for route, id_field, extra in [
            ('tipo_documento', 'idTipoDocumento', {'indicaciones':'Prueba aislada','expresionRegular':None}),
            ('tipo_medio_contacto', 'idTipoMedioContacto', {'indicaciones':'Prueba aislada','expresionRegular':None}),
            ('tipo_examen', 'idTipoExamen', {'observaciones':'Prueba aislada'}),
            ('clinica', 'idClinica', {'tipo':'Académica','comentarios':'Prueba aislada'}),
            ('rol', 'idRol', {'observaciones':'Prueba aislada'})]:
            with self.subTest(route=route):
                name = 'Prueba CRUD ' + uuid.uuid4().hex[:12]
                data = dict(nombre=name, activo=True, **extra)
                status, headers, created = http(route, 'POST', data)
                self.assertEqual(status, 201); ident = created[id_field]
                self.assertTrue(headers['Location'].endswith('/' + ident))
                self.assertEqual(http(route + '/' + ident)[2], created)
                if route != 'tipo_medio_contacto':
                    self.assertEqual(http(route, 'POST', data)[0], 409)
                changed = dict(data, nombre=name + ' editado', activo=False)
                self.assertEqual(http(route + '/' + ident, 'PUT', changed)[0], 200)
                self.assertEqual(http(route + '/' + ident)[2]['nombre'], changed['nombre'])
                # UUID generado por el servidor: no se interpola entrada del usuario en SQL.
                uuid.UUID(ident)
                count = subprocess.check_output(['docker','exec', CONTAINER,'psql','-U','galenos_demo','-d','galenos_demo_crud','-At','-v','ON_ERROR_STOP=1','-c',
                    'SELECT count(*) FROM public.' + route + ' WHERE ' + 'id_' + route + " = '" + ident + "' AND activo IS FALSE"], text=True).strip()
                self.assertEqual(count, '1')
                if route in ['clinica','rol']:
                    self.assertEqual(http(route + '/' + ident, 'DELETE')[0], 204)
                    self.assertEqual(http(route + '/' + ident)[0], 404)

    def test_invalid_input_and_referenced_delete_preserve_records(self):
        for data in [{'nombre':'   '}, {'nombre':'Válido','extra':'No publicado'}, {'nombre':5}, {'nombre':'Válido','expresionRegular':'['}]:
            with self.subTest(data=data): self.assertEqual(http('tipo_documento','POST',data)[0], 422)
        ident = '00000000-0000-4000-8000-000000000011'
        self.assertEqual(http('clinica/' + ident, 'DELETE')[0], 409)
        self.assertEqual(http('clinica/' + ident)[0], 200)

if __name__ == '__main__': unittest.main(verbosity=2)
