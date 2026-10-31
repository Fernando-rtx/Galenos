"""Ejecutar solo con la tabla del catálogo temporalmente inaccesible en la BD sintética."""
from tipo_documento_http import ORIGIN, request


status, headers, body = request(headers={"Origin": ORIGIN})
assert status == 500, ("Se esperaba HTTP 500", status)
assert body == {"mensaje": "Error interno del servidor"}, "El error debe ser genérico"
assert headers.get("Total-registros") is None
assert headers["Access-Control-Allow-Origin"] == ORIGIN
print("OK: HTTP 500 seguro y cabeceras CORS conservadas")
