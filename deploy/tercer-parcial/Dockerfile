# PPI115 GT02 - Tercer parcial - backend. Estructura de la clase de Peña.
FROM debian:stable AS liberty-archive
ADD --checksum=sha256:d0682d4a72c13a4955145aa97fae2e432e40fd1b1bc076f0282bfafe02561b81 https://repo.maven.apache.org/maven2/io/openliberty/openliberty-runtime/26.0.0.8/openliberty-runtime-26.0.0.8.zip /openliberty.zip

FROM debian:stable
USER root
RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-21-jdk-headless ca-certificates wget unzip \
    && rm -rf /var/lib/apt/lists/*
RUN useradd --create-home --shell /usr/sbin/nologin usuario
WORKDIR /home/usuario
RUN --mount=type=bind,from=liberty-archive,source=/openliberty.zip,target=/tmp/openliberty.zip \
    echo 'd0682d4a72c13a4955145aa97fae2e432e40fd1b1bc076f0282bfafe02561b81  /tmp/openliberty.zip' | sha256sum -c - \
    && unzip -q /tmp/openliberty.zip -d /home/usuario \
    && chown -R usuario:usuario /home/usuario/wlp
# El ticket PDF del WAR usa Java2D para medir y dibujar texto.
RUN apt-get update && apt-get install -y --no-install-recommends \
    fontconfig fonts-dejavu-core libharfbuzz0b \
    && rm -rf /var/lib/apt/lists/*
USER usuario
RUN /home/usuario/wlp/bin/server create galeno
COPY --chown=usuario:usuario server.xml /home/usuario/wlp/usr/servers/galeno/server.xml
# Los JAR provided NO estan en el WAR: se incluyen en la imagen.
# PrimeFaces como biblioteca GLOBAL: permite usar dropins sin declarar <webApplication>.
RUN mkdir -p /home/usuario/wlp/usr/servers/galeno/lib/global \
             /home/usuario/wlp/usr/servers/galeno/jdbc \
    && wget -q 'https://repo.maven.apache.org/maven2/org/primefaces/primefaces/15.0.17/primefaces-15.0.17-jakarta.jar' \
       -O /home/usuario/wlp/usr/servers/galeno/lib/global/primefaces-15.0.17-jakarta.jar \
    && wget -q 'https://repo.maven.apache.org/maven2/org/postgresql/postgresql/42.7.4/postgresql-42.7.4.jar' \
       -O /home/usuario/wlp/usr/servers/galeno/jdbc/postgresql-42.7.4.jar
RUN /home/usuario/wlp/bin/featureUtility installServerFeatures galeno --acceptLicense
COPY --chown=usuario:usuario entrypoint.sh /home/usuario/entrypoint.sh
# Sin WAR en la imagen: el docente lo monta en dropins durante docker run.
ENV GALENOS_API_MODE=academic \
    GALENOS_DEMO_MODE=false \
    GALENOS_CRUD_DEMO=false \
    GALENOS_CORS_ORIGINS=http://localhost,http://127.0.0.1,http://galeno.frontend
EXPOSE 9080
ENTRYPOINT ["/bin/sh", "/home/usuario/entrypoint.sh"]
CMD ["/home/usuario/wlp/bin/server", "run", "galeno"]
