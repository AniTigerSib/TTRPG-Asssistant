#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME:-ttrpg-service-it}"
INTEGRATION_BASE_URL="${INTEGRATION_BASE_URL:-http://localhost:8080}"
HEALTHCHECK_URL="${INTEGRATION_BASE_URL%/}/actuator/health"
LOG_DIR="${LOG_DIR:-$ROOT_DIR/.ci-logs/integration}"
TEST_LOG_FILE="$LOG_DIR/integration-test.log"
CONTAINER_LOG_DIR="$LOG_DIR/containers"

mkdir -p "$CONTAINER_LOG_DIR"

collect_logs() {
  docker compose -p "$COMPOSE_PROJECT_NAME" logs --no-color > "$CONTAINER_LOG_DIR/docker-compose.log" 2>&1 || true
  docker compose -p "$COMPOSE_PROJECT_NAME" logs --no-color service > "$CONTAINER_LOG_DIR/service.log" 2>&1 || true
  docker compose -p "$COMPOSE_PROJECT_NAME" logs --no-color postgres > "$CONTAINER_LOG_DIR/postgres.log" 2>&1 || true
  docker compose -p "$COMPOSE_PROJECT_NAME" logs --no-color redis > "$CONTAINER_LOG_DIR/redis.log" 2>&1 || true
}

cleanup() {
  local exit_code=$?

  collect_logs

  if [[ $exit_code -ne 0 ]]; then
    cat "$CONTAINER_LOG_DIR/docker-compose.log" || true
  fi

  docker compose -p "$COMPOSE_PROJECT_NAME" down -v --remove-orphans || true
  exit "$exit_code"
}

trap cleanup EXIT

cd "$ROOT_DIR"

./gradlew bootJar 2>&1 | tee "$LOG_DIR/bootJar.log"
docker compose -p "$COMPOSE_PROJECT_NAME" up -d --build postgres redis service

for attempt in $(seq 1 60); do
  if curl --silent --fail "$HEALTHCHECK_URL" >/dev/null; then
    ./gradlew integrationTest 2>&1 | tee "$TEST_LOG_FILE"
    exit 0
  fi

  sleep 2
done

echo "Application did not become ready at $HEALTHCHECK_URL" >&2
exit 1
