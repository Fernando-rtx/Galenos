# Backend del tercer parcial PPI115

El Dockerfile parte de `debian:stable` y prepara un servidor Open Liberty llamado `galeno` con Java 21. PrimeFaces se carga desde `lib/global` y PostgreSQL JDBC desde la biblioteca explícita del datasource `jdbc/pgdb`. El servidor corre en primer plano como `usuario` y autodespliega el WAR que se monta en solo lectura.

## Build local

Desde las raíces de los repositorios, cambiar `arm64` por `amd64` para la otra plataforma.

```sh
mvn -B clean package -Dliberty.install.dir="$PWD/target/liberty-staging"
docker buildx build --platform linux/arm64 --load -t galenos-backend:ppi115-arm64 deploy/tercer-parcial
# Desde el repositorio frontend
docker buildx build --platform linux/arm64 --load -t galenos-frontend:ppi115-arm64 deploy/tercer-parcial
```

Para reutilizar una descarga ya verificada, colocar el ZIP original de Open Liberty 26.0.0.8 como `openliberty.zip` en un directorio y añadir `--build-context liberty-archive=/ruta/directorio` a Buildx. El Dockerfile valida el mismo SHA-256 para ambas fuentes.

## Ejecución oficial

Crear una vez la red `docker network create ppi115net`. Exportar los cinco valores suministrados por el docente. El host PostgreSQL debe ser alcanzable desde Docker. Para un FQDN con IP indicada por el docente se puede añadir `--add-host FQDN:IP` al backend. El datasource usa el esquema existente.

```sh
docker run --rm --name galeno.backend -h galeno.backend --network ppi115net -p 9080:9080 \
  -e PG_USER -e PG_PASS -e PG_DB -e PG_HOST -e PG_PORT \
  -v "/ruta/galeno.war:/home/usuario/wlp/usr/servers/galeno/dropins/galeno.war:ro" \
  galenos-backend:ppi115-arm64

docker run --rm --name galeno.frontend -h galeno.frontend --network ppi115net -p 80:80 \
  -v "/ruta/src:/var/www/html:ro" galenos-frontend:ppi115-arm64
```

El WAR puede conservar el nombre `target/GalenosSV.war` en el host durante el ensayo. En el lado del contenedor debe llamarse `galeno.war`. Probar ambos contenedores en terminales distintas.

Dentro de `ppi115net`, la API se alcanza en `http://galeno.backend:9080/galeno/resources/`. Desde el navegador, abrir `http://localhost/` para el ensayo local o el nombre público del frontend para el examen. Por defecto la SPA usa ese mismo nombre y el puerto 9080 para REST. CORS permite `http://localhost`, `http://127.0.0.1` y `http://galeno.frontend`. Para un nombre público distinto, configurar `GALENOS_CORS_ORIGINS` con el origen exacto. El frontend también admite `PUBLIC_BACKEND_BASE_URL` si el navegador puede resolver un nombre distinto para la API. Los nombres de Docker se resuelven dentro de la red configurada.

```sh
docker exec galeno.frontend node -e 'fetch("http://galeno.backend:9080/galeno/resources/v1/tipo_documento").then(async r => { if (!r.ok) throw Error(r.status); console.log("API interna", r.status, (await r.json()).length) }).catch(e => { console.error(e); process.exitCode=1 })'
curl -fi http://localhost:9080/galeno/paginas/CambiarRol.xhtml
curl -fi -H 'Origin: http://localhost' http://localhost:9080/galeno/resources/v1/tipo_documento
```

## Prueba local con datos ficticios

`bash deploy/tercer-parcial/verificar-ejecucion.sh arm64 DIRECTORIO_EVIDENCIA` verifica los contenedores locales y los dos registros del fixture GT02. Desde el frontend, ejecutar `node --test deploy/tercer-parcial/server.test.mjs` y, con Playwright y Chromium instalados, `python deploy/tercer-parcial/browser-check.py DIRECTORIO_EVIDENCIA`. La opción `--write-fixture` guarda una atención y debe usarse exclusivamente en un PostgreSQL aislado con datos ficticios.

Si Peña configura la resolución de `galeno.backend` también fuera de Docker, el comando del frontend puede incluir `-e PUBLIC_BACKEND_BASE_URL=http://galeno.backend:9080/galeno/resources/` para usar el FQDN literal desde el navegador. La red interna no proporciona DNS a los equipos externos.

## Publicación y ZIP

En la fase de publicación, ejecutar desde cada repositorio `docker buildx build --platform linux/arm64,linux/amd64 --push -t USUARIO/IMAGEN:ppi115-gt02 deploy/tercer-parcial`. Inspeccionar las dos plataformas con `docker buildx imagetools inspect USUARIO/IMAGEN:ppi115-gt02` y repetir el ensayo con las etiquetas descargadas.

El script `preparar-zip.sh` compila desde una copia temporal del commit del backend y exporta solo el `src/` del commit del frontend. Produce exactamente WAR, ZIP interno y texto de despliegue. Requiere una ruta de salida nueva.

```sh
bash deploy/tercer-parcial/preparar-zip.sh /ruta/frontend \
  USUARIO/galenos-backend:ppi115-gt02 USUARIO/galenos-frontend:ppi115-gt02 \
  /ruta/entrega-ppi115.zip
```
