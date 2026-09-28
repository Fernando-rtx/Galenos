#!/usr/bin/env bash
#
# run.sh — Compila, despliega y arranca GalenosSV en Open Liberty.
#
# Flujo:
#   1. mvn clean package       → compila, prueba y genera el WAR
#   2. mvn liberty:create      → crea el servidor y copia PostgreSQL JDBC
#   3. Copia PrimeFaces jakarta → shared library en apps/
#   4. liberty:run             → arranca el servidor
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

SERVER_DIR="target/liberty/wlp/usr/servers/GalenosSV"
APPS_DIR="$SERVER_DIR/apps"
PRIMEFACES_JAR="$HOME/.m2/repository/org/primefaces/primefaces/15.0.17/primefaces-15.0.17-jakarta.jar"
PRIMEFACES_DEST="$APPS_DIR/primefaces-15.0.17-jakarta.jar"

# ── 1. Compilar y empaquetar ─────────────────────────────────────────────────
echo "==> mvn clean package"
mvn clean package -q

# ── 2. Crear servidor Liberty ────────────────────────────────────────────────
echo "==> mvn liberty:create"
mvn liberty:create -q

# ── 3. Copiar PrimeFaces jakarta como shared library ─────────────────────────
echo "==> Copiando PrimeFaces jakarta a $APPS_DIR"
mkdir -p "$APPS_DIR"
cp "$PRIMEFACES_JAR" "$PRIMEFACES_DEST"

# ── 4. Arrancar el servidor ──────────────────────────────────────────────────
echo "==> Arrancando Liberty..."
mvn liberty:run
