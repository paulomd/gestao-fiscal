#!/usr/bin/env bash
# Recria o Keycloak para reimportar keycloak/realm-entrevista.json (redirect URIs, logout, etc.)
set -euo pipefail
cd "$(dirname "$0")/.."

echo "Parando Keycloak..."
docker compose stop keycloak 2>/dev/null || true

echo "Removendo container e dados dev (H2 em memória/arquivo do start-dev)..."
docker compose rm -f keycloak 2>/dev/null || true

echo "Subindo Keycloak com realm atualizado..."
docker compose up -d keycloak

echo "Aguardando realm entrevista..."
for i in $(seq 1 60); do
  if curl -sf -H "Host: localhost" "http://localhost:8180/realms/entrevista" >/dev/null 2>&1; then
    echo "Keycloak OK."
    exit 0
  fi
  sleep 2
done

echo "Keycloak ainda não respondeu — verifique: docker logs gestao-fiscal-keycloak --tail 40"
exit 1
