#!/bin/sh
set -e

echo ">>> Aguardando API backend..."
TRIES=0
until curl -sf "http://backend:8082/actuator/health" >/dev/null 2>&1; do
  TRIES=$((TRIES + 1))
  if [ "$TRIES" -gt 40 ]; then
    echo ">>> Backend não respondeu; iniciando JSF mesmo assim..."
    break
  fi
  sleep 3
done

echo ">>> Aguardando Keycloak (rede Docker)..."
TRIES=0
until curl -sf -H "Host: localhost" "http://keycloak:8080/realms/entrevista" >/dev/null 2>&1; do
  TRIES=$((TRIES + 1))
  if [ "$TRIES" -gt 60 ]; then
    echo ">>> Keycloak não respondeu; iniciando JSF mesmo assim (OAuth pode falhar até KC subir)..."
    break
  fi
  sleep 3
done

echo ">>> Iniciando módulo JSF..."
exec java -jar /app/app.war
