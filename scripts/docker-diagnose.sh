#!/usr/bin/env bash
set -e
cd "$(dirname "$0")/.."

echo "=== Status dos containers ==="
docker compose ps -a

echo ""
echo "=== Health (últimas linhas) ==="
for s in gestao-fiscal-backend gestao-fiscal-jsf gestao-fiscal-angular gestao-fiscal-gateway gestao-fiscal-keycloak; do
  echo "--- $s ---"
  docker inspect --format='{{.State.Status}} health={{if .State.Health}}{{.State.Health.Status}}{{else}}n/a{{end}}' "$s" 2>/dev/null || echo "container não existe"
done

echo ""
echo "=== Logs backend (erros comuns: Postgres) ==="
docker compose logs backend --tail 30 2>/dev/null || true

echo ""
echo "=== Logs frontend-jsf ==="
docker compose logs frontend-jsf --tail 30 2>/dev/null || true

echo ""
echo "=== Logs gateway ==="
docker compose logs gateway --tail 15 2>/dev/null || true
