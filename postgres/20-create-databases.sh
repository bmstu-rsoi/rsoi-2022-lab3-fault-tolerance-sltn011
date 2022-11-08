#!/usr/bin/env bash
set -e

# TODO для создания баз прописать свой вариант
export VARIANT="<variant>"
export SCRIPT_PATH=/docker-entrypoint-initdb.d/
export PGPASSWORD=postgres
psql -f "$SCRIPT_PATH/scripts/db-$VARIANT.sql"
