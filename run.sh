#!/usr/bin/env bash
set -euo pipefail
umask 077

cd "$(dirname "${BASH_SOURCE[0]}")"
if [[ ! -f src/main/liberty/config/server.env ]]; then
    echo "Configura src/main/liberty/config/server.env a partir de server.env.example." >&2
    exit 1
fi
exec mvn package liberty:run
