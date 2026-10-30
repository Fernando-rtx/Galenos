"""HTTP real contra Liberty aislado con datasource inaccesible del demo sintético.

No modificar tablas ni detener contenedores compartidos para provocar el fallo.
"""
from tipo_documento_http import ORIGIN, request
from consulta_demo_http import request as consulta_request, CLINIC_A


responses = [
    ("TipoDocumento", lambda: request(headers={"Origin": ORIGIN})),
    ("Clinica", lambda: consulta_request("/v1/clinica", origin=ORIGIN)),
    ("Consulta", lambda: consulta_request("/v1/consulta", {"idClinica": CLINIC_A}, origin=ORIGIN)),
]
for name, execute in responses:
    status, headers, body = execute()
    assert status == 500, (name, "Se esperaba HTTP 500", status)
    assert body == {"mensaje": "Error interno del servidor"}, (name, "El error debe ser genérico")
    assert headers.get("Total-registros") is None, name
    assert headers["Access-Control-Allow-Origin"] == ORIGIN, name
    print("OK:", name, "HTTP 500 seguro y cabeceras CORS conservadas")
