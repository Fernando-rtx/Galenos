"""Lectura JSF real sobre el demo oficial; Playwright es herramienta externa.

Requiere GALENOS_JSF_BASE_URL (origen local seguido de /GalenosSV).
No prueba escrituras ni rollback. El fixture oficial contiene cinco consultas
para la clínica A y dos en el día clínico 22/10/2026.
"""
import os
from pathlib import Path
import unittest
from urllib.parse import urlsplit
from playwright.sync_api import sync_playwright, expect

BASE = os.environ["GALENOS_JSF_BASE_URL"].rstrip("/")
if urlsplit(BASE).hostname not in ("localhost", "127.0.0.1"):
    raise SystemExit("Usar exclusivamente el demo sintético local")


class ConsultaJsfBrowserTest(unittest.TestCase):
    def test_clinica_y_limites_diarios_compartidos_con_rest(self):
        with sync_playwright() as playwright:
            options = {"headless": True}
            if os.environ.get("GALENOS_TEST_CHROMIUM"):
                options["executable_path"] = os.environ["GALENOS_TEST_CHROMIUM"]
            browser = playwright.chromium.launch(**options)
            try:
                page = browser.new_page()
                errors = []
                page.on("pageerror", lambda error: errors.append(str(error)))
                response = page.goto(BASE + "/paginas/Consulta.xhtml")
                self.assertEqual(200, response.status)
                page.locator('[id="layoutForm:clinicaActual"]').click()
                page.get_by_role("option", name="Clínica ficticia A", exact=True).click()
                rows = page.locator('[id="layoutForm:consultasTable_data"] tr[data-ri]')
                expect(rows).to_have_count(5, timeout=15000)
                page.locator('[id="layoutForm:fechaDesde_input"]').fill("22/10/2026")
                page.locator('[id="layoutForm:fechaHasta_input"]').fill("22/10/2026")
                page.locator("button").filter(has_text="Buscar por fechas").click()
                expect(rows).to_have_count(2, timeout=15000)
                self.assertEqual(errors, [])
                if os.environ.get("GALENOS_INTEGRATION_ARTIFACTS"):
                    folder = Path(os.environ["GALENOS_INTEGRATION_ARTIFACTS"])
                    folder.mkdir(parents=True, exist_ok=True)
                    page.screenshot(path=str(folder / "consulta-jsf.png"), full_page=True)
            finally:
                browser.close()


if __name__ == "__main__":
    unittest.main(verbosity=2)
