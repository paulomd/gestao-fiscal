#!/usr/bin/env bash
# Único ponto de entrada: para stack, rebuild sem cache, sobe e valida.
set -euo pipefail

cd "$(dirname "$0")/.."

if [[ ! -f .env ]]; then
  echo "Criando .env a partir de .env.example..."
  cp .env.example .env
fi

export COMPOSE_PROFILES="${COMPOSE_PROFILES:-docker-db}"
COMPOSE=(docker compose --profile docker-db)

wait_http() {
  local url="$1"
  local label="$2"
  local max_tries="${3:-60}"
  local try=0
  echo -n ">>> Aguardando ${label}..."
  until curl -sf --max-time 5 "$url" >/dev/null 2>&1; do
    try=$((try + 1))
    if [[ "$try" -ge "$max_tries" ]]; then
      echo " falhou."
      return 1
    fi
    echo -n "."
    sleep 3
  done
  echo " ok."
  return 0
}

verificar_api_com_token() {
  local token code
  token=$(curl -s -X POST "http://localhost:8180/realms/entrevista/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "grant_type=password" \
    -d "client_id=angular-app" \
    -d "username=admin" \
    -d "password=admin123" 2>/dev/null \
    | python3 -c "import sys,json; print(json.load(sys.stdin).get('access_token',''))" 2>/dev/null || echo "")
  if [[ -z "$token" ]]; then
    echo ">>> API com token: não foi possível obter token no Keycloak"
    return 1
  fi
  code=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer $token" \
    http://localhost/api/dashboard/stats)
  echo ">>> API /api/dashboard/stats com token → HTTP ${code} (esperado 200)"
  [[ "$code" == "200" ]]
}

verificar_rotas() {
  local failed=0
  wait_http "http://localhost:8180/realms/entrevista" "Keycloak (:8180)" 40 || failed=1
  wait_http "http://localhost/dashboard-angular" "Angular (gateway)" 40 || failed=1
  wait_http "http://localhost:8082/actuator/health" "API (:8082)" 40 || failed=1
  wait_http "http://localhost:8081/actuator/health" "JSF (:8081)" 40 || failed=1

  local loc
  loc=$(curl -sI http://localhost/ 2>/dev/null | grep -i '^location:' | tr -d '\r' || true)
  echo ">>> Redirect / → ${loc:-sem Location}"
  if [[ "$loc" != *"dashboard-angular"* ]]; then
    echo ">>> AVISO: / deveria redirecionar para /dashboard-angular"
    failed=1
  fi

  verificar_api_com_token || failed=1

  local main
  main=$(curl -s http://localhost/dashboard-angular 2>/dev/null | grep -oE 'main\.[a-f0-9]+\.js' | head -1 || true)
  echo ">>> Bundle Angular: ${main:-não encontrado}"
  [[ -n "$main" ]] || failed=1

  local jsf_code
  jsf_code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost/aliquotas.xhtml)
  echo ">>> JSF /aliquotas.xhtml → HTTP ${jsf_code} (esperado 302 para login)"
  [[ "$jsf_code" == "302" || "$jsf_code" == "200" ]] || failed=1

  return "$failed"
}

mostrar_logs_falha() {
  echo ""
  echo ">>> Logs recentes:"
  for c in gestao-fiscal-gateway gestao-fiscal-angular gestao-fiscal-jsf gestao-fiscal-keycloak gestao-fiscal-backend; do
    echo "--- ${c} ---"
    docker logs "$c" --tail 15 2>&1 || true
  done
}

subir_stack() {
  echo ">>> Parando stack e removendo imagens locais antigas..."
  "${COMPOSE[@]}" down --remove-orphans --rmi local 2>/dev/null || true

  echo ">>> Build sem cache (backend, JSF, Angular — 5 a 15 min)..."
  export DOCKER_BUILDKIT="${DOCKER_BUILDKIT:-1}"
  "${COMPOSE[@]}" build --no-cache backend frontend-jsf frontend-angular

  echo ">>> Subindo stack (Keycloak ~2 min na 1ª vez)..."
  "${COMPOSE[@]}" up -d --wait --wait-timeout 900
}

echo ">>> Perfil Compose: ${COMPOSE_PROFILES}"

set +e
subir_stack
rc=$?
set -e

if [[ "$rc" -ne 0 ]]; then
  echo ">>> Subida falhou; limpando cache de build e tentando novamente..."
  docker builder prune -af 2>/dev/null || true
  export DOCKER_BUILDKIT=0
  "${COMPOSE[@]}" down --remove-orphans --rmi local 2>/dev/null || true
  "${COMPOSE[@]}" build --no-cache backend frontend-jsf frontend-angular
  "${COMPOSE[@]}" up -d --wait --wait-timeout 900
fi

echo ""
echo ">>> Verificando serviços..."
failed=0
verificar_rotas || failed=1

echo ""
"${COMPOSE[@]}" ps

if [[ "$failed" -ne 0 ]]; then
  mostrar_logs_falha
  exit 1
fi

echo ""
echo "============================================"
echo "  Aplicação:  http://localhost/dashboard-angular"
echo "  Keycloak:   http://localhost:8180"
echo "  API:        http://localhost:8082/swagger-ui.html"
echo "  JSF:        http://localhost/aliquotas.xhtml"
echo "  Login app:  admin / admin123  |  fiscal / fiscal123"
echo ""
echo "  Use aba anônima após subir (Ctrl+Shift+N)"
echo "============================================"
