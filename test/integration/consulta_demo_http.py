"""HTTP real, exclusivamente con test/fixtures/consulta_demo.sql y modo demo activo."""
import json
import os
import unittest
from urllib.error import HTTPError
from urllib.parse import urlencode
from urllib.request import Request, urlopen

BASE = os.environ["GALENOS_API_BASE_URL"].rstrip("/")
ORIGIN = os.environ["GALENOS_TEST_ORIGIN"]
CLINIC_A = "00000000-0000-4000-8000-000000000011"
CLINIC_B = "00000000-0000-4000-8000-000000000012"
INACTIVE = "00000000-0000-4000-8000-000000000013"
CONSULT_A = "00000000-0000-4000-8000-000000001002"
CONSULT_B = "00000000-0000-4000-8000-000000002001"
ASSIGNMENT = "00000000-0000-4000-8000-000000000104"


def request(path, params=None, origin=None):
    url = BASE + path + ("?" + urlencode(params) if params else "")
    req = Request(url, headers={"Origin": origin} if origin else {})
    try:
        result = urlopen(req, timeout=20)
    except HTTPError as error:
        result = error
    with result:
        return result.status, result.headers, json.loads(result.read())


class ConsultaDemoHttpTest(unittest.TestCase):
    def test_selector_solo_clinicas_activas_y_conteo(self):
        status, headers, body = request("/v1/clinica", {"max": 1})
        self.assertEqual((200, "2", 1), (status, headers["Total-registros"], len(body)))
        self.assertEqual({"idClinica", "nombre"}, set(body[0]))
        self.assertEqual(CLINIC_A, body[0]["idClinica"])

    def test_lista_paginada_no_mezcla_clinicas(self):
        status, headers, body = request("/v1/consulta", {"idClinica": CLINIC_A, "max": 2})
        self.assertEqual((200, "5", 2), (status, headers["Total-registros"], len(body)))
        self.assertTrue(all(row["idClinica"] == CLINIC_A for row in body))
        status, headers, body = request("/v1/consulta", {"idClinica": CLINIC_B})
        self.assertEqual((200, "1", CONSULT_B), (status, headers["Total-registros"], body[0]["idConsulta"]))

    def test_dia_inclusivo_en_zona_clinica_con_limites_exclusivos(self):
        status, headers, body = request("/v1/consulta", {"idClinica": CLINIC_A,
            "fechaDesde": "2026-10-22", "fechaHasta": "2026-10-22"})
        self.assertEqual((200, "2"), (status, headers["Total-registros"]))
        self.assertEqual([CONSULT_A, "00000000-0000-4000-8000-000000001003"], [r["idConsulta"] for r in body])

    def test_persona_rol_filtra_y_cuenta_solo_en_clinica(self):
        status, headers, body = request("/v1/consulta", {"idClinica": CLINIC_A, "idPersonaRol": ASSIGNMENT})
        self.assertEqual((200, "2", 2), (status, headers["Total-registros"], len(body)))
        self.assertTrue(all(r["idPersonaRol"] == ASSIGNMENT for r in body))
        self.assertIsNone(body[0]["personaNombre"])
        status, headers, body = request("/v1/consulta", {"idClinica": CLINIC_B, "idPersonaRol": ASSIGNMENT})
        self.assertEqual((200, "0", []), (status, headers["Total-registros"], body))

    def test_detalle_dto_campos_y_fechas_utc(self):
        status, headers, body = request("/v1/consulta/" + CONSULT_A, {"idClinica": CLINIC_A})
        self.assertEqual(200, status)
        self.assertEqual({"idConsulta", "fechaInicio", "fechaFin", "idPersonaRol", "personaNombre",
            "rolNombre", "idClinica", "clinicaNombre"}, set(body))
        self.assertEqual("Persona ficticia Uno", body["personaNombre"])
        self.assertEqual("2026-10-22T06:00:00Z", body["fechaInicio"])
        self.assertEqual("2026-10-22T07:00:00Z", body["fechaFin"])
        self.assertIsNone(headers.get("Total-registros"))

    def test_detalle_de_otra_clinica_y_clinica_inactiva_son404(self):
        for path, clinic in (("/v1/consulta/" + CONSULT_B, CLINIC_A), ("/v1/consulta", INACTIVE)):
            with self.subTest(path=path, clinic=clinic):
                status, _, body = request(path, {"idClinica": clinic})
                self.assertEqual(404, status)
                self.assertEqual({"mensaje"}, set(body))

    def test_parametros_invalidos_son422(self):
        for params in ({}, {"idClinica": "abc"}, {"idClinica": CLINIC_A, "max": 0},
                {"idClinica": CLINIC_A, "fechaDesde": "2026-02-30"},
                {"idClinica": CLINIC_A, "fechaDesde": "2026-10-23", "fechaHasta": "2026-10-22"}):
            with self.subTest(params=params):
                self.assertEqual(422, request("/v1/consulta", params)[0])

    def test_pagina_vacia_y_nulos(self):
        status, headers, body = request("/v1/consulta", {"idClinica": CLINIC_A, "first": 999})
        self.assertEqual((200, "5", []), (status, headers["Total-registros"], body))
        _, _, body = request("/v1/consulta/00000000-0000-4000-8000-000000001005", {"idClinica": CLINIC_A})
        self.assertIsNone(body["fechaInicio"])
        self.assertIsNone(body["fechaFin"])

    def test_cors_y_errores_legibles(self):
        status, headers, _ = request("/v1/consulta", {"idClinica": CLINIC_A}, ORIGIN)
        self.assertEqual(200, status)
        self.assertEqual(ORIGIN, headers["Access-Control-Allow-Origin"])
        self.assertIn("Total-registros", headers["Access-Control-Expose-Headers"])
        status, headers, _ = request("/v1/consulta/" + CONSULT_B, {"idClinica": CLINIC_A}, ORIGIN)
        self.assertEqual(404, status)
        self.assertEqual(ORIGIN, headers["Access-Control-Allow-Origin"])


if __name__ == "__main__":
    unittest.main(verbosity=2)
