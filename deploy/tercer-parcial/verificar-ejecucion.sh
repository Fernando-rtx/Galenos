#!/usr/bin/env bash
set -euo pipefail
if [ "$#" -ne 2 ]; then
    echo 'Uso: verificar-ejecucion.sh arm64|amd64 DIRECTORIO_EVIDENCIA' >&2
    exit 2
fi
arch=$1
evidence=$2
mkdir -p "$evidence"

docker image inspect --format '{{.Os}}/{{.Architecture}} {{.Config.User}} {{.Id}}' \
  "galenos-backend:ppi115-$arch" "galenos-frontend:ppi115-$arch"
docker inspect --format '{{.Name}} {{.Config.User}} {{json .HostConfig.PortBindings}} {{range .Mounts}}{{.Destination}} (RW={{.RW}}){{end}} {{range $k,$v := .NetworkSettings.Networks}}{{$k}}{{end}}' \
  galeno.backend galeno.frontend
docker exec galeno.backend sh -ec 'id; uname -m; cat /etc/debian_version; java -version; /home/usuario/wlp/bin/productInfo version; ls /home/usuario/wlp/usr/servers/galeno/lib/global /home/usuario/wlp/usr/servers/galeno/jdbc'
docker exec galeno.frontend sh -ec 'id; uname -m; cat /etc/debian_version; node -p "process.version + \" \" + process.arch"; getent hosts galeno.backend'
ready=0
for ((i=0; i<150; i++)); do
    if curl -fsS -m 3 'http://127.0.0.1:9080/galeno/resources/v1/tipo_documento?first=0&max=10' -o "$evidence/catalogo.json" 2>/dev/null; then
        ready=1
        break
    fi
    sleep 1
done
docker logs galeno.backend > "$evidence/liberty.log" 2>&1
docker logs galeno.frontend > "$evidence/node.log" 2>&1
test "$ready" = 1
test "$(grep -c 'CWWKZ0001I: Application galeno started' "$evidence/liberty.log")" = 1
python3 -c 'import json,sys; rows=json.load(open(sys.argv[1])); assert len(rows)==2; print("Catalogo JDBC/JPA", [(r["idTipoDocumento"],r["activo"]) for r in rows])' "$evidence/catalogo.json"
docker exec galeno.frontend node -e 'fetch("http://galeno.backend:9080/galeno/resources/v1/tipo_documento").then(async r=>{if(!r.ok)throw Error(r.status);console.log("API desde frontend en ppi115net",r.status,(await r.json()).length)}).catch(e=>{console.error(e);process.exitCode=1})'
curl -fsS -o "$evidence/jsf.html" http://127.0.0.1:9080/galeno/paginas/CambiarRol.xhtml
grep -q 'primefaces' "$evidence/jsf.html"
resource=$(python3 -c 'import re,sys; text=open(sys.argv[1]).read(); match=re.search(r"""src="([^"]*\.js[^"]*ln=primefaces[^"]*)""",text); assert match; print(match[1].replace("&amp;","&"))' "$evidence/jsf.html")
curl -fsS "http://127.0.0.1:9080$resource" -o "$evidence/primefaces.js"
test -s "$evidence/primefaces.js"
echo "JSF y recurso JavaScript suministrado por PrimeFaces: 200"
curl -fsSI http://127.0.0.1/ > "$evidence/frontend-headers.txt"
curl -fsS -D "$evidence/preflight.txt" -o /dev/null -X OPTIONS \
  -H 'Origin: http://galeno.frontend' -H 'Access-Control-Request-Method: POST' \
  -H 'Access-Control-Request-Headers: authorization,content-type,x-persona-rol' \
  http://127.0.0.1:9080/galeno/resources/v2/atencion/registrar
grep -qi '^Access-Control-Allow-Origin: http://galeno.frontend' "$evidence/preflight.txt"
grep -q ' 204 ' "$evidence/preflight.txt"
test "$(curl -sS -o /dev/null -w '%{http_code}' -H 'Origin: http://otro.invalid' http://127.0.0.1:9080/galeno/resources/v1/tipo_documento)" = 403
echo "CORS: origen de evaluación 204, origen ajeno 403"
