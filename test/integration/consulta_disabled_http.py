"""Comprueba el modo normal con el WAR nuevo y demo desactivada."""
from consulta_demo_http import CLINIC_A, CONSULT_A, request


for path, params in (("/v1/clinica", None), ("/v1/consulta", {"idClinica": CLINIC_A}),
        ("/v1/consulta/" + CONSULT_A, {"idClinica": CLINIC_A})):
    status, headers, body = request(path, params)
    assert status == 404, (path, status)
    assert set(body) == {"mensaje"}
    assert headers.get("Total-registros") is None
status, headers, body = request("/v1/tipo_documento", {"max": 1})
assert (status, headers.get("Total-registros"), len(body)) == (200, "2", 1)
print("OK: recursos demo desactivados y catálogo TipoDocumento disponible")
