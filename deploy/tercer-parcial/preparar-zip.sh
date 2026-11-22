#!/usr/bin/env bash
set -euo pipefail

if [ "$#" -ne 4 ]; then
    echo 'Uso: preparar-zip.sh RUTA_FRONTEND IMAGEN_BACKEND IMAGEN_FRONTEND ARCHIVO_SALIDA.zip' >&2
    exit 2
fi

backend=$(git -C "$(dirname "$0")" rev-parse --show-toplevel)
frontend=$(git -C "$1" rev-parse --show-toplevel)
backend_image=$2
frontend_image=$3
output_dir=$(cd "$(dirname "$4")" && pwd)
output="$output_dir/$(basename "$4")"

if [[ "$output" != *.zip || -e "$output" || -L "$output" ]]; then
    echo 'Indica un archivo .zip de salida nuevo en un directorio existente.' >&2
    exit 2
fi

backend_sha=$(git -C "$backend" rev-parse HEAD)
frontend_sha=$(git -C "$frontend" rev-parse HEAD)
stage=$(mktemp -d "${TMPDIR:-/tmp}/ppi115-zip.XXXXXXXX")
trap 'rm -rf -- "$stage"' EXIT
mkdir "$stage/backend" "$stage/entrega"

git -C "$backend" archive "$backend_sha" | tar -xf - -C "$stage/backend"
(
    cd "$stage/backend"
    mvn -B clean package -Dliberty.install.dir="$PWD/target/liberty-staging"
)
cp "$stage/backend/target/GalenosSV.war" "$stage/entrega/galeno.war"
git -C "$frontend" archive --format=zip --prefix=src/ -o "$stage/entrega/frontend-src.zip" "$frontend_sha:src"
cat > "$stage/entrega/integrantes-y-despliegue.txt" <<INFO
Tercer examen parcial PPI115 2026 · GT02

Alberto Alexander Itzep Sarceño
José Rodrigo Cortez Flores
Fernando José Guevara Palma

Backend https://github.com/Fernando-rtx/Galenos
Commit $backend_sha
Imagen $backend_image

Frontend https://github.com/Fernando-rtx/GalenoSVFE
Commit $frontend_sha
Imagen $frontend_image

Crear la red con docker network create ppi115net.
Exportar PG_USER, PG_PASS, PG_DB, PG_HOST y PG_PORT con los valores del docente.
Resolver por DNS el nombre público del frontend. Por defecto, el navegador usa ese mismo host en 9080 para la API.
El FQDN interno del backend es http://galeno.backend:9080/galeno.
Si el entorno ofrece otro origen público, configurar GALENOS_CORS_ORIGINS con el origen exacto y,
si la API tiene otro host, PUBLIC_BACKEND_BASE_URL con la URL completa /galeno/resources/.
Si el DNS de PostgreSQL requiere un alias facilitado por el docente, añadir --add-host FQDN:IP al backend.

docker run --rm --name galeno.backend -h galeno.backend --network ppi115net -p 9080:9080 \\
  -e PG_USER -e PG_PASS -e PG_DB -e PG_HOST -e PG_PORT \\
  -v "/ruta/galeno.war:/home/usuario/wlp/usr/servers/galeno/dropins/galeno.war:ro" $backend_image

docker run --rm --name galeno.frontend -h galeno.frontend --network ppi115net -p 80:80 \\
  -v "/ruta/src:/var/www/html:ro" $frontend_image
INFO
(
    cd "$stage/entrega"
    zip -q "$stage/entrega.zip" galeno.war frontend-src.zip integrantes-y-despliegue.txt
)
(set -o noclobber; cat "$stage/entrega.zip" > "$output")
unzip -Z1 "$output"
