"""Comprobación CORS real en Chromium; requiere Playwright como herramienta externa.

Usa únicamente el dataset sintético y un puerto local libre para GALENOS_TEST_ORIGIN.
No instala dependencias en el frontend ni implementa sus componentes.
"""
import json
import os
import threading
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import urlsplit
from playwright.sync_api import sync_playwright


base = os.environ["GALENOS_API_BASE_URL"].rstrip("/")
origin = os.environ["GALENOS_TEST_ORIGIN"]
parsed = urlsplit(origin)
if parsed.scheme != "http" or parsed.hostname not in ("localhost", "127.0.0.1"):
    raise SystemExit("La prueba requiere un origen HTTP local")


class TestPage(BaseHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200)
        self.send_header("Content-Type", "text/html; charset=utf-8")
        self.end_headers()
        self.wfile.write(b"<!doctype html><html lang='es'><title>Prueba CORS</title><p>Prueba de integracion</p></html>")

    def log_message(self, *_args):
        pass


server = ThreadingHTTPServer((parsed.hostname, parsed.port or 80), TestPage)
threading.Thread(target=server.serve_forever, daemon=True).start()
try:
    with sync_playwright() as playwright:
        options = {"headless": True}
        if os.environ.get("GALENOS_TEST_CHROMIUM"):
            options["executable_path"] = os.environ["GALENOS_TEST_CHROMIUM"]
        browser = playwright.chromium.launch(**options)
        try:
            page = browser.new_page()
            page.goto(origin)
            result = page.evaluate("""async (base) => {
                const response = await fetch(base + '/v1/tipo_documento?max=1', {
                    credentials: 'omit', headers: {'Content-Type': 'application/json'}
                });
                const body = await response.json();
                const missing = await fetch(base + '/v1/tipo_documento/00000000-0000-4000-8000-ffffffffffff', {
                    credentials: 'omit'
                });
                return {status: response.status, total: response.headers.get('Total-registros'),
                    count: body.length, missingStatus: missing.status,
                    missingBody: await missing.json()};
            }""", base)
            assert result["status"] == 200
            assert result["total"] == "2", "El navegador debe poder leer Total-registros"
            assert result["count"] == 1
            assert result["missingStatus"] == 404
            assert set(result["missingBody"]) == {"mensaje"}
            print("OK navegador:", json.dumps(result, ensure_ascii=False))
        finally:
            browser.close()
finally:
    server.shutdown()
    server.server_close()
