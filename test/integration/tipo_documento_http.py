"""Pruebas HTTP reales sobre el dataset test/fixtures/tipo_documento.sql.

Variables obligatorias: GALENOS_API_BASE_URL y GALENOS_TEST_ORIGIN.
No usar contra una base con datos reales ni contra producción.
"""
import json
import os
import unittest
from urllib.error import HTTPError
from urllib.request import Request, urlopen


BASE = os.environ["GALENOS_API_BASE_URL"].rstrip("/")
ORIGIN = os.environ["GALENOS_TEST_ORIGIN"]
RESOURCE = BASE + "/v1/tipo_documento"
FIRST_ID = "00000000-0000-4000-8000-000000000001"
MISSING_ID = "00000000-0000-4000-8000-ffffffffffff"


def request(path="", method="GET", headers=None):
    req = Request(RESOURCE + path, method=method, headers=headers or {})
    try:
        result = urlopen(req, timeout=15)
    except HTTPError as error:
        result = error
    with result:
        raw = result.read()
        body = json.loads(raw) if raw else None
        return result.status, result.headers, body


class TipoDocumentoHttpTest(unittest.TestCase):
    def test_lista_paginada_total_y_dto(self):
        status, headers, body = request("?first=0&max=1")
        self.assertEqual(200, status)
        self.assertIn("application/json", headers["Content-Type"])
        self.assertEqual("2", headers["Total-registros"])
        self.assertEqual(1, len(body))
        self.assertEqual(FIRST_ID, body[0]["idTipoDocumento"])
        self.assertEqual({"idTipoDocumento", "nombre", "indicaciones", "expresionRegular", "activo"}, set(body[0]))

    def test_segunda_pagina_con_nulos_y_estado_inactivo(self):
        status, _, body = request("?first=1&max=1")
        self.assertEqual(200, status)
        self.assertEqual("Documento sintético B", body[0]["nombre"])
        self.assertIsNone(body[0]["indicaciones"])
        self.assertIsNone(body[0]["expresionRegular"])
        self.assertFalse(body[0]["activo"])

    def test_pagina_vacia_conserva_total(self):
        status, headers, body = request("?first=999&max=1")
        self.assertEqual((200, "2", []), (status, headers["Total-registros"], body))

    def test_detalle(self):
        status, _, body = request("/" + FIRST_ID)
        self.assertEqual(200, status)
        self.assertEqual(FIRST_ID, body["idTipoDocumento"])

    def test_detalle_ausente(self):
        status, _, body = request("/" + MISSING_ID)
        self.assertEqual(404, status)
        self.assertEqual({"mensaje"}, set(body))

    def test_parametros_invalidos(self):
        for suffix in ("?first=-1", "?max=0", "?max=101", "?first=abc", "?max=", "/1-1-1-1-1"):
            with self.subTest(suffix=suffix):
                status, _, body = request(suffix)
                self.assertEqual(422, status)
                self.assertEqual({"mensaje"}, set(body))

    def test_get_cors_y_exposicion_del_conteo(self):
        status, headers, _ = request(headers={"Origin": ORIGIN})
        self.assertEqual(200, status)
        self.assertEqual(ORIGIN, headers["Access-Control-Allow-Origin"])
        self.assertIn("Total-registros", headers["Access-Control-Expose-Headers"])
        self.assertIsNone(headers.get("Access-Control-Allow-Credentials"))
        self.assertIn("Origin", ",".join(headers.get_all("Vary", [])))

    def test_cors_tambien_se_aplica_a_errores(self):
        status, headers, _ = request("/" + MISSING_ID, headers={"Origin": ORIGIN})
        self.assertEqual(404, status)
        self.assertEqual(ORIGIN, headers["Access-Control-Allow-Origin"])

    def test_origen_no_permitido(self):
        status, headers, _ = request(headers={"Origin": "https://origen-no-permitido.invalid"})
        self.assertEqual(403, status)
        self.assertIsNone(headers.get("Access-Control-Allow-Origin"))

    def test_preflight(self):
        status, headers, body = request(method="OPTIONS", headers={
            "Origin": ORIGIN, "Access-Control-Request-Method": "GET",
            "Access-Control-Request-Headers": "Content-Type"})
        self.assertEqual(204, status)
        self.assertIsNone(body)
        self.assertEqual(ORIGIN, headers["Access-Control-Allow-Origin"])
        self.assertIn("GET", headers["Access-Control-Allow-Methods"])
        self.assertIn("Content-Type", headers["Access-Control-Allow-Headers"])

    def test_preflight_rechaza_escritura_y_cabeceras_no_acordadas(self):
        for method, header in (("POST", "Content-Type"), ("GET", "Authorization")):
            with self.subTest(method=method, header=header):
                status, _, _ = request(method="OPTIONS", headers={"Origin": ORIGIN,
                    "Access-Control-Request-Method": method, "Access-Control-Request-Headers": header})
                self.assertEqual(403, status)

    def test_escritura_no_disponible(self):
        status, headers, body = request(method="POST")
        self.assertEqual(405, status)
        self.assertIn("GET", headers["Allow"])
        self.assertEqual({"mensaje"}, set(body))


if __name__ == "__main__":
    unittest.main(verbosity=2)
