#!/bin/sh
set -eu

: "${PG_USER:?Falta PG_USER}"
: "${PG_PASS:?Falta PG_PASS}"
: "${PG_DB:?Falta PG_DB}"
: "${PG_HOST:?Falta PG_HOST}"
: "${PG_PORT:?Falta PG_PORT}"
case "$PG_PORT" in
    *[!0-9]*|'') echo 'PG_PORT debe ser un entero entre 1 y 65535' >&2; exit 1 ;;
esac
if ! [ "$PG_PORT" -ge 1 ] || ! [ "$PG_PORT" -le 65535 ]; then
    echo 'PG_PORT debe ser un entero entre 1 y 65535' >&2
    exit 1
fi

export POSTGRESQL_DB_NAME="$PG_DB"
exec "$@"
