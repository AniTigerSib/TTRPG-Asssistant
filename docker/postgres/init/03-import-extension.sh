#!/bin/sh
set -eu

psql -v ON_ERROR_STOP=1 \
  --username "$POSTGRES_USER" \
  --dbname "$POSTGRES_DB" <<-SQL
    CREATE EXTENSION IF NOT EXISTS pgcrypto;
    ALTER DATABASE ${POSTGRES_DB} SET timezone TO 'UTC';
SQL