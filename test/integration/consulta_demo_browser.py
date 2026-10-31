"""Prueba de lectura cross-origin con el dataset sintético de Consulta y Playwright externo."""
import json
import os
import tempfile
import threading
from functools import partial
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import urlsplit
from playwright.sync_api import sync_playwright


base = os.environ["GALENOS_API_BASE_URL"].rstrip("/")
origin = os.environ["GALENOS_TEST_ORIGIN"]
parsed = urlsplit(origin)
if parsed.scheme != "http" or parsed.hostname not in ("localhost", "127.0.0.1"):
    raise SystemExit("La prueba requiere un origen HTTP local")

with tempfile.TemporaryDirectory(prefix="galenos-consulta-browser-") as folder:
    Path(folder, "index.html").write_text("<!doctype html><html lang='es'><title>Prueba Consulta</title><p>Datos ficticios</p></html>")
    server = ThreadingHTTPServer((parsed.hostname, parsed.port or 80), partial(SimpleHTTPRequestHandler, directory=folder))
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
                result = page.evaluate("""async base => {
                    const get = url => fetch(url, {credentials: 'omit', headers: {'Content-Type': 'application/json'}});
                    const clinicsResponse = await get(base + '/v1/clinica');
                    const clinics = await clinicsResponse.json();
                    const query = new URLSearchParams({idClinica: clinics[0].idClinica, max: '1'});
                    const response = await get(base + '/v1/consulta?' + query);
                    const rows = await response.json();
                    const detail = await get(base + '/v1/consulta/' + rows[0].idConsulta + '?idClinica=' + clinics[0].idClinica);
                    const otherClinic = await get(base + '/v1/consulta/' + rows[0].idConsulta + '?idClinica=' + clinics[1].idClinica);
                    return {clinicsStatus: clinicsResponse.status, clinicsTotal: clinicsResponse.headers.get('Total-registros'),
                        status: response.status, total: response.headers.get('Total-registros'), count: rows.length,
                        personaNombre: rows[0].personaNombre, detailStatus: detail.status,
                        otherClinicStatus: otherClinic.status, otherClinicError: await otherClinic.json()};
                }""", base)
                assert (result["clinicsStatus"], result["clinicsTotal"]) == (200, "2")
                assert (result["status"], result["total"], result["count"]) == (200, "5", 1)
                assert result["personaNombre"] == "Persona ficticia Uno"
                assert result["detailStatus"] == 200
                assert result["otherClinicStatus"] == 404
                assert set(result["otherClinicError"]) == {"mensaje"}
                print("OK navegador Consulta:", json.dumps(result, ensure_ascii=False))
            finally:
                browser.close()
    finally:
        server.shutdown()
        server.server_close()
