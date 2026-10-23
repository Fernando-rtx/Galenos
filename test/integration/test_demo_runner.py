"""Regresiones de seguridad del orquestador; no modifica Docker ni bases reales."""
import importlib.util
import json
from pathlib import Path
import subprocess
import socket
import tempfile
import unittest
from unittest.mock import patch

spec = importlib.util.spec_from_file_location("demo_runner", Path(__file__).resolve().parents[2] / "scripts/demo.py")
runner = importlib.util.module_from_spec(spec)
spec.loader.exec_module(runner)


class DemoRunnerTest(unittest.TestCase):
    def test_version_liberty_no_depende_de_etiquetas_del_idioma(self):
        with tempfile.TemporaryDirectory() as folder:
            runtime = Path(folder)
            metadata = runtime / "lib/versions/openliberty.properties"
            metadata.parent.mkdir(parents=True)
            metadata.write_text("# Metadatos en español\ncom.ibm.websphere.productName=Open Liberty\n"
                                "com.ibm.websphere.productVersion=26.0.0.8\n", encoding="utf-8")
            with patch.object(runner.subprocess, "run", side_effect=AssertionError("No ejecutar productInfo")):
                self.assertEqual("26.0.0.8", runner.liberty_version(runtime))

    def test_runtime_con_otra_version_se_rechaza_antes_de_compilar(self):
        with tempfile.TemporaryDirectory() as folder, patch.object(runner, "STATE", Path(folder)):
            demo = runner.Demo(dict(runner.DEFAULTS))
            server = demo.runtime / "bin/server"
            server.parent.mkdir(parents=True)
            server.touch()
            metadata = demo.runtime / "lib/versions/openliberty.properties"
            metadata.parent.mkdir(parents=True)
            metadata.write_text("com.ibm.websphere.productVersion=26.0.0.9\n")
            with patch.object(demo, "logged") as logged:
                with self.assertRaises(runner.DemoError):
                    demo.build(False)
                logged.assert_not_called()

    def test_metadatos_liberty_ausentes_o_incompletos_no_se_aceptan(self):
        with tempfile.TemporaryDirectory() as folder:
            runtime = Path(folder)
            with self.assertRaises(runner.DemoError):
                runner.liberty_version(runtime)
            metadata = runtime / "lib/versions/openliberty.properties"
            metadata.parent.mkdir(parents=True)
            metadata.write_text("com.ibm.websphere.productName=Open Liberty\n")
            with self.assertRaises(runner.DemoError):
                runner.liberty_version(runtime)

    def test_listener_activo_no_se_confunde_con_puerto_disponible(self):
        with socket.socket() as server:
            server.bind(("127.0.0.1", 0))
            server.listen()
            with self.assertRaises(runner.DemoError):
                runner.available_port(server.getsockname()[1])

    def test_reinicio_rapido_no_falla_por_time_wait(self):
        with socket.socket() as server:
            server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            server.bind(("127.0.0.1", 0))
            server.listen()
            port = server.getsockname()[1]
            with socket.create_connection(("127.0.0.1", port)) as client:
                accepted, _ = server.accept()
                accepted.close()
                self.assertEqual(b"", client.recv(1))
        runner.available_port(port)

    def test_recurso_docker_ajeno_no_se_detiene(self):
        demo = runner.Demo(dict(runner.DEFAULTS))
        response = subprocess.CompletedProcess([], 0, json.dumps([{
            "Config": {"Labels": {runner.OWNER_LABEL: "otro-proyecto"}}, "State": {"Running": True}}]), "")
        with patch.object(demo, "is_running", return_value=False), patch.object(demo, "capture", return_value=response) as call:
            with self.assertRaises(runner.DemoError):
                demo.stop()
            self.assertEqual(1, call.call_count)
            self.assertNotIn("stop", call.call_args.args[0])

    def test_base_con_tablas_ajenas_no_recibe_fixture(self):
        demo = runner.Demo(dict(runner.DEFAULTS))
        with patch.object(demo, "sql", side_effect=["", "1"]) as sql:
            with self.assertRaises(runner.DemoError):
                demo.fixtures()
            self.assertEqual(2, sql.call_count)
            self.assertTrue(all("SELECT" in call.args[0] for call in sql.call_args_list))

    def test_fixture_cambiado_no_borra_ni_reemplaza_la_base(self):
        demo = runner.Demo(dict(runner.DEFAULTS))
        with patch.object(demo, "sql", side_effect=["galenos_demo_fixture", "revision-anterior"]) as sql:
            with self.assertRaises(runner.DemoError):
                demo.fixtures()
            self.assertTrue(all("SELECT" in call.args[0] for call in sql.call_args_list))

    def test_jdk25_explicito_se_rechaza(self):
        with tempfile.TemporaryDirectory() as folder:
            home = Path(folder)
            (home / "bin").mkdir()
            (home / "bin/java").touch()
            (home / "bin/javac").touch()
            result = subprocess.CompletedProcess([], 0, "", "java.specification.version = 25\n")
            config = dict(runner.DEFAULTS, GALENOS_JAVA_HOME=str(home))
            with patch.object(runner.subprocess, "run", return_value=result):
                with self.assertRaises(runner.DemoError):
                    runner.java_home(config)

    def test_configuracion_rechaza_puertos_repetidos_e_inyeccion_de_lineas(self):
        with tempfile.TemporaryDirectory() as folder, patch.object(runner, "ROOT", Path(folder)):
            for values in ({"GALENOS_DEMO_HTTP_PORT": "15432"},
                           {"GALENOS_CORS_ORIGINS": "http://localhost:3000\nGALENOS_DEMO_MODE=false"}):
                with self.subTest(values=values), patch.dict(runner.os.environ, values, clear=True):
                    with self.assertRaises(runner.DemoError):
                        runner.configuration()


if __name__ == "__main__":
    unittest.main(verbosity=2)
