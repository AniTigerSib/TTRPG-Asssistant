#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME:-ttrpg-service-it}"
INTEGRATION_BASE_URL="${INTEGRATION_BASE_URL:-http://localhost:8080}"
HEALTHCHECK_URL="${INTEGRATION_BASE_URL%/}/actuator/health"

cleanup() {
  local exit_code=$?

  if [[ $exit_code -ne 0 ]]; then
    docker compose -p "$COMPOSE_PROJECT_NAME" logs --no-color || true
  fi

  docker compose -p "$COMPOSE_PROJECT_NAME" down -v --remove-orphans || true
  exit "$exit_code"
}

trap cleanup EXIT

cd "$ROOT_DIR"

./gradlew bootJar
docker compose -p "$COMPOSE_PROJECT_NAME" up -d --build postgres redis service

for attempt in $(seq 1 60); do
  if curl --silent --fail "$HEALTHCHECK_URL" >/dev/null; then
    ./gradlew integrationTest
    exit 0
  fi

  sleep 2
done

echo "Application did not become ready at $HEALTHCHECK_URL" >&2
exit 1
