#!/bin/sh
set -eu

psql -v ON_ERROR_STOP=1 \
  --username "$POSTGRES_USER" \
  --dbname "$POSTGRES_DB" <<-SQL
    DO
    \$\$
    BEGIN
      IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = '${DB_FLYWAY_USER}') THEN
        EXECUTE format('CREATE ROLE %I LOGIN PASSWORD %L', '${DB_FLYWAY_USER}', '${DB_FLYWAY_PASSWORD}');
      END IF;
      IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = '${DB_USER}') THEN
        EXECUTE format('CREATE ROLE %I LOGIN PASSWORD %L', '${DB_USER}', '${DB_PASSWORD}');
      END IF;
    END
    \$\$;

    GRANT CONNECT ON DATABASE ${POSTGRES_DB} TO ${DB_USER};
    GRANT CONNECT ON DATABASE ${POSTGRES_DB} TO ${DB_FLYWAY_USER};
SQL
