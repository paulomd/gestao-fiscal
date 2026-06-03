#!/usr/bin/env bash
# VPS: só Docker — pull das imagens publicadas pelo GitHub Actions (GHCR).
# Uso: ./deploy.sh http://203.0.113.10
set -euo pipefail

cd "$(dirname "$0")"

PUBLIC_URL="${1:-${APP_PUBLIC_URL:-}}"
if [[ -z "$PUBLIC_URL" ]]; then
  echo "Uso: $0 <URL pública sem barra final>"
  echo "  ou export APP_PUBLIC_URL=http://203.0.113.10"
  exit 1
fi
PUBLIC_URL="${PUBLIC_URL%/}"

if [[ ! -f .env ]]; then
  echo "Copie .env.example para .env e defina GHCR_REGISTRY (ex. ghcr.io/usuario/gestao-fiscal)."
  exit 1
fi

# shellcheck disable=SC1091
set -a
source .env
set +a

if [[ -z "${GHCR_REGISTRY:-}" ]]; then
  echo "Defina GHCR_REGISTRY no .env (ex. ghcr.io/seu-usuario/gestao-fiscal)."
  exit 1
fi

scheme="${PUBLIC_URL%%://*}"
rest="${PUBLIC_URL#*://}"
host="${rest%%/*}"
KEYCLOAK_PUBLIC_URL="${KEYCLOAK_PUBLIC_URL:-${scheme}://${host}:8180}"

python3 patch-realm.py "$PUBLIC_URL"

# Garante URLs no .env para o compose
touch .env
grep -q '^APP_PUBLIC_URL=' .env && sed -i "s|^APP_PUBLIC_URL=.*|APP_PUBLIC_URL=${PUBLIC_URL}|" .env || echo "APP_PUBLIC_URL=${PUBLIC_URL}" >> .env
grep -q '^KEYCLOAK_PUBLIC_URL=' .env && sed -i "s|^KEYCLOAK_PUBLIC_URL=.*|KEYCLOAK_PUBLIC_URL=${KEYCLOAK_PUBLIC_URL}|" .env || echo "KEYCLOAK_PUBLIC_URL=${KEYCLOAK_PUBLIC_URL}" >> .env

# shellcheck disable=SC1091
set -a
source .env
set +a

export APP_PUBLIC_URL="${PUBLIC_URL}"
export KEYCLOAK_PUBLIC_URL="${KEYCLOAK_PUBLIC_URL}"
export IMAGE_TAG="${IMAGE_TAG:-latest}"

COMPOSE=(docker compose)

if ! docker pull "${GHCR_REGISTRY}/backend:${IMAGE_TAG}" 2>/dev/null; then
  echo ""
  echo ">>> Não foi possível baixar imagens do GHCR."
  echo "    Repositório privado? Faça login:"
  echo "      echo SEU_PAT | docker login ghcr.io -u SEU_USUARIO --password-stdin"
  echo "    (PAT com permissão read:packages)"
  echo "    Ou torne os pacotes públicos em GitHub → Packages."
  exit 1
fi

echo ">>> Atualizando imagens..."
"${COMPOSE[@]}" pull

echo ">>> Subindo stack..."
"${COMPOSE[@]}" up -d

echo ""
echo ">>> Deploy concluído."
echo "    App:       ${PUBLIC_URL}"
echo "    Keycloak:  ${KEYCLOAK_PUBLIC_URL}  (container Docker, porta 8180)"
echo "    Postgres:  postgres:5432 na rede interna (volume postgres_data)"
echo "    Imagens:   ${GHCR_REGISTRY}/*:${IMAGE_TAG}"
