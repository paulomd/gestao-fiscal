-- Execute no Postgres local (ajuste usuário/senha se necessário):
--   psql -U postgres -f scripts/init-local-db.sql

CREATE USER gestao WITH PASSWORD 'gestao123';
CREATE DATABASE gestao_fiscal OWNER gestao;
GRANT ALL PRIVILEGES ON DATABASE gestao_fiscal TO gestao;
