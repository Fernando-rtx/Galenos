"""Valida el cliente de Itzep servido por BrowserSync sin modificar sus módulos.

Requiere el runner y environment.local.js del frontend ya configurados contra
la demo sintética. Playwright/Chromium son herramientas externas opcionales.
"""
import json
import os
from pathlib import Path
from urllib.parse import urlsplit
from playwright.sync_api import sync_playwright


origin = os.environ["GALENOS_FRONTEND_ORIGIN"].rstrip("/")
if urlsplit(origin).hostname not in ("localhost", "127.0.0.1"):
    raise SystemExit("Esta prueba solo admite un frontend local con datos sintéticos")
artifacts = Path(os.environ.get("GALENOS_INTEGRATION_ARTIFACTS",
                 str(Path(__file__).resolve().parents[2] / ".galenos-demo/frontend-integration")))
artifacts.mkdir(parents=True, exist_ok=True)
with sync_playwright() as playwright:
    options = {"headless": True}
    if os.environ.get("GALENOS_TEST_CHROMIUM"):
        options["executable_path"] = os.environ["GALENOS_TEST_CHROMIUM"]
    browser = playwright.chromium.launch(**options)
    try:
        page = browser.new_page()
        cdp = page.context.new_cdp_session(page)
        cdp.send("Network.enable")
        preflights = []
        cdp.on("Network.responseReceived", lambda event: preflights.append(event["response"]["status"])
               if event.get("type") == "Preflight" else None)
        page.goto(origin + "/test/integration/runner.html")
        page.wait_for_function("""() => {
            const text = document.querySelector('#status')?.textContent || '';
            return /completadas|Fallaron|INTEGRACIÓN NO EJECUTADA/.test(text);
        }""", timeout=90000)
        suite = page.evaluate("""() => ({
            status: document.querySelector('#status').textContent,
            passes: Number(document.querySelector('#mocha-stats .passes em')?.textContent || 0),
            failures: Number(document.querySelector('#mocha-stats .failures em')?.textContent || 0)
        })""")
        assert suite["status"] == "Pruebas configuradas completadas.", suite
        assert suite["passes"] >= 7 and suite["failures"] == 0, suite
        page.screenshot(path=str(artifacts / "runner.png"), full_page=True)

        checks = page.evaluate("""async () => {
            const api = await import('/src/js/index.js');
            const { config } = await import('/test/integration/environment.local.js');
            const clinics = await new api.ClinicaDAO({config}).findRange(0, 2);
            const query = { idClinica: clinics.datos[0].idClinica };
            const dao = new api.ConsultaDAO({config});
            const rows = await dao.findRange(0, 1, {query});
            const empty = await dao.findRange(999, 1, {query});
            const dates = await dao.findRange(0, 10, {query: {...query,
                fechaDesde: '2026-10-22', fechaHasta: '2026-10-22'}});
            let wrongClinic, invalidMax;
            try {
                await dao.findById(rows.datos[0].idConsulta, {query: {idClinica: clinics.datos[1].idClinica}});
            } catch (error) { wrongClinic = {type: error.type, status: error.status}; }
            try { await dao.findRange(0, 101, {query}); }
            catch (error) { invalidMax = {type: error.type, status: error.status}; }
            const preflightConfig = api.createApiConfig({...config, http: {...config.http,
                headers: {'Content-Type': 'application/json'}}});
            const withPreflight = await new api.ConsultaDAO({config: preflightConfig}).findRange(0, 1, {query});
            return {baseUrl: config.baseUrl, responseClass: rows instanceof api.DefaultResponse,
                clinicsTotal: clinics.totalDatos, total: rows.totalDatos, count: rows.datos.length,
                exposedTotal: rows.getHeader('Total-registros'), emptyCount: empty.datos.length,
                emptyTotal: empty.totalDatos, filteredTotal: dates.totalDatos, wrongClinic, invalidMax,
                preflightTotal: withPreflight.totalDatos};
        }""")
        assert checks["responseClass"] is True
        assert (checks["clinicsTotal"], checks["total"], checks["count"], checks["exposedTotal"]) == (2, 5, 1, "5")
        assert (checks["emptyCount"], checks["emptyTotal"], checks["filteredTotal"]) == (0, 5, 2)
        assert checks["wrongClinic"] == {"type": "http", "status": 404}
        assert checks["invalidMax"] == {"type": "http", "status": 422}
        assert checks["preflightTotal"] == 5
        page.wait_for_timeout(100)
        assert 204 in preflights, "No se observó un preflight 204 real"

        denied = None
        denied_origin = os.environ.get("GALENOS_TEST_DENIED_ORIGIN")
        if denied_origin:
            if urlsplit(denied_origin).hostname not in ("localhost", "127.0.0.1"):
                raise SystemExit("El origen de rechazo debe ser local")
            other = browser.new_page()
            other.goto(denied_origin.rstrip("/") + "/")
            denied = other.evaluate("""async baseUrl => {
                const api = await import('/src/js/index.js');
                try {
                    await new api.TipoDocumentoDAO({config: api.createApiConfig({baseUrl})}).findRange(0, 1);
                    return {unexpectedSuccess: true};
                } catch (error) { return {type: error.type, code: error.code, status: error.status}; }
            }""", checks["baseUrl"])
            assert denied == {"type": "connection", "code": "NETWORK_ERROR", "status": None}, denied
            other.close()

        result = {"frontendOrigin": origin, "mocha": suite, "daoChecks": checks,
                  "preflightStatuses": preflights, "deniedOriginError": denied,
                  "syntheticDataOnly": True}
        (artifacts / "result.json").write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n")
        print(json.dumps(result, ensure_ascii=False, indent=2))
    finally:
        browser.close()
