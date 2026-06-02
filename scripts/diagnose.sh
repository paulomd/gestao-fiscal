#!/usr/bin/env bash
set -uo pipefail

cd "$(dirname "$0")/.."

echo "=== Containers ==="
docker compose --profile docker-db ps -a 2>/dev/null || docker ps -a --filter name=gestao-fiscal

echo ""
echo "=== Portas HTTP (esperado: 80, 8082, 8081, 8180) ==="
for url in \
  "http://localhost:8180/realms/entrevista|Keycloak (:8180)" \
  "http://localhost:8082/actuator/health|Backend" \
  "http://localhost:8081/oauth2/authorization/keycloak|JSF" \
  "http://localhost/dashboard-angular|Gateway→Angular" \
  "http://localhost/dashboard.xhtml|Gateway→JSF"; do
  IFS='|' read -r u label <<< "$url"
  code=$(curl -sS -o /dev/null -w "%{http_code}" --max-time 5 "$u" 2>/dev/null || echo "ERR")
  printf "  %-22s %s → %s\n" "$label" "$u" "$code"
done

echo ""
echo "=== Últimas linhas de log (se existirem) ==="
for c in gestao-fiscal-keycloak gestao-fiscal-jsf gestao-fiscal-backend gestao-fiscal-gateway gestao-fiscal-angular; do
  if docker logs "$c" --tail 3 2>/dev/null | grep -q .; then
    echo "--- $c ---"
    docker logs "$c" --tail 8 2>&1
  fi
done
