#!/bin/sh
set -e

if echo "${SPRING_DATASOURCE_URL:-}" | grep -q 'postgres:5432'; then
  echo ">>> Aguardando PostgreSQL..."
  until nc -z postgres 5432 2>/dev/null; do
    sleep 2
  done
fi

echo ">>> Aguardando Keycloak (realm entrevista)..."
TRIES=0
until curl -sf "http://keycloak:8080/realms/entrevista" >/dev/null 2>&1; do
  TRIES=$((TRIES + 1))
  if [ "$TRIES" -gt 60 ]; then
    echo ">>> Keycloak não respondeu a tempo; iniciando mesmo assim..."
    break
  fi
  sleep 3
done

echo ">>> Iniciando API Spring Boot..."
exec java -jar /app/app.jar
