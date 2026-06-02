# Sistema de Gestão Fiscal — Demo Entrevista Técnica

Aplicação demonstrativa de **evolução tecnológica gradual**: módulos legados em **JSF/PrimeFaces** convivem com novos módulos em **Angular 13**, compartilhando **API Spring Boot**, **PostgreSQL**, **Keycloak (SSO)** e **identidade visual única**.

## Arquitetura

```
┌─────────────────────────────────────────────────────────────┐
│                    Gateway Nginx (:80)                       │
│  /dashboard.xhtml, /aliquotas.xhtml  →  frontend-jsf        │
│  /apuracao-fiscal, /historico      →  frontend-angular      │
│  /api/*                              →  backend               │
└─────────────────────────────────────────────────────────────┘
         │                    │                    │
         ▼                    ▼                    ▼
   frontend-jsf         frontend-angular         backend
   (JoinFaces+PF)        (Angular 13+Material)   (Spring Boot 3)
         │                    │                    │
         └────────────────────┴────────────────────┘
                              │
                    Keycloak (SSO) + PostgreSQL
```

| Camada | Tecnologias |
|--------|-------------|
| Backend | Java 21, Spring Boot 3, Security OAuth2 Resource Server, JPA, Flyway, MapStruct, Lombok, Actuator, OpenAPI |
| Legado | Jakarta Faces, PrimeFaces, JoinFaces, OAuth2 Client |
| Moderno | Angular 13, Angular Material, Chart.js, keycloak-angular |
| Segurança | Keycloak, OpenID Connect, SSO |
| Infra | Docker Compose, Nginx |

## Fluxo de autenticação (SSO)

1. Usuário acessa `http://localhost` (gateway).
2. Módulo JSF redireciona para **Keycloak** (`legacy-jsf`) se não autenticado.
3. Após login, sessão SSO fica no Keycloak (cookie no host `:8180`).
4. Ao navegar para **Angular** (`/apuracao-fiscal`), o `keycloak-angular` usa `check-sso` e reutiliza a sessão — **sem novo login**.
5. Chamadas à API enviam **JWT Bearer** (interceptor Angular / token OAuth2 no JSF).

### Usuários de demonstração

| Usuário | Senha | Roles |
|---------|-------|-------|
| admin | admin123 | ADMIN, FISCAL |
| fiscal | fiscal123 | FISCAL |

## Fluxo JSF ↔ Angular (navegação transparente)

| Menu | Tecnologia | URL |
|------|------------|-----|
| Dashboard | JSF | `/dashboard.xhtml` |
| Cadastro de Alíquotas | JSF | `/aliquotas.xhtml` |
| Apuração Fiscal | Angular | `/apuracao-fiscal` |
| Histórico de Apurações | Angular | `/historico` |

- **Sem iframe**, **sem embed** — cada frontend é independente.
- Links do menu JSF apontam para rotas Angular no mesmo host (`ANGULAR_BASE_URL`).
- Menu Angular aponta para páginas `.xhtml` no mesmo host (`jsfBaseUrl`).
- Tema compartilhado: `shared/theme/corporate.css`.

## Configuração Keycloak

Realm: **entrevista**

Clients:

| Client | Tipo | Uso |
|--------|------|-----|
| legacy-jsf | Confidential | Login JSF |
| angular-app | Public | SPA Angular |
| backend-api | Bearer-only | Documentação API |

Import automático via `keycloak/realm-entrevista.json` no `docker compose up`.

Console admin Keycloak: `http://localhost:8180` (admin / admin).

## Execução com Docker (tudo de uma vez)

```bash
chmod +x scripts/start-all.sh
./scripts/start-all.sh
```

O script aguarda os healthchecks do Compose e valida gateway, Keycloak e Angular antes de concluir.

Diagnóstico opcional (se algo falhar): `bash scripts/diagnose.sh`

Aguarde a primeira subida (build Maven/npm + Keycloak ~3-5 min).

| Serviço | URL |
|---------|-----|
| Aplicação (gateway) | http://localhost |
| API / Swagger | http://localhost:8082/swagger-ui.html |
| Keycloak | http://localhost:8180 |
| JSF direto | http://localhost:8081 |
| PostgreSQL | localhost:5432 |

## Execução local (desenvolvimento)

### Pré-requisitos

- Java 21, Maven 3.9+
- Node 18+ e npm
- PostgreSQL 16
- Keycloak 24+ (ou use apenas Postgres/Keycloak via Docker)

### 1. Infraestrutura

**Postgres local (recomendado em dev)** — o container `postgres` **não sobe por padrão**:

```bash
# Criar banco no Postgres da máquina (uma vez)
psql -U postgres -f scripts/init-local-db.sql

# Só Keycloak no Docker
docker compose up -d keycloak
```

**Postgres no Docker** (quem não tiver Postgres local):

```bash
docker compose --profile docker-db up -d postgres keycloak
# No .env: SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/gestao_fiscal
```

Variáveis: copie `.env.example` → `.env` e ajuste usuário/senha do seu Postgres.

### 2. Backend

```bash
cd backend
mvn spring-boot:run
```

### 3. JSF

```bash
cd frontend-jsf
mvn spring-boot:run
```

### 4. Angular

```bash
cd frontend-angular
npm install --legacy-peer-deps
npm start
```

Acesse JSF em `http://localhost:8081` e Angular em `http://localhost:4200`, ou configure o gateway.

## API REST

| Método | Endpoint |
|--------|----------|
| GET | `/api/aliquotas` |
| GET | `/api/aliquotas/{id}` |
| GET | `/api/aliquotas/regime/{regime}` |
| POST | `/api/aliquotas` |
| PUT | `/api/aliquotas/{id}` |
| DELETE | `/api/aliquotas/{id}` |
| GET | `/api/apuracoes` |
| POST | `/api/apuracoes` |
| POST | `/api/apuracoes/calcular` |
| GET | `/api/dashboard/stats` |

## Observabilidade

- **Actuator**: `/actuator/health`, `/actuator/metrics`
- **Logs estruturados** (Logstash encoder)
- **Tratamento global de exceções** (`GlobalExceptionHandler`)

## CI/CD

Pipeline GitHub Actions (`.github/workflows/ci.yml`):

1. Build e testes do backend
2. Build JSF
3. Build Angular
4. Análise estática (SpotBugs)
5. Build das imagens Docker

## Uso de IA no desenvolvimento

Este projeto foi estruturado com assistência de IA (Cursor/Composer) para acelerar:

- Geração do esqueleto multi-módulo e Docker Compose
- Boilerplate Spring (entidades, DTOs, MapStruct, segurança OAuth2)
- Layout corporativo compartilhado e rotas de integração JSF/Angular
- Documentação e pipeline CI

A arquitetura, regras de negócio fiscal e decisões de integração SSO foram definidas conforme o briefing da entrevista técnica.

## Estrutura do repositório

```
spring-jsf/
├── backend/              # API Spring Boot
├── frontend-jsf/         # Módulo legado JSF
├── frontend-angular/     # Módulo moderno Angular
├── shared/theme/         # CSS corporativo unificado
├── keycloak/             # Realm import
├── docker/               # Nginx gateway
├── docker-compose.yml
└── .github/workflows/
```

## Resultado para o entrevistador

1. Sistema corporativo legado em JSF com CRUD de alíquotas (PrimeFaces).
2. Novos módulos em Angular (apuração, histórico, dashboard com gráficos).
3. Navegação transparente entre tecnologias no mesmo host.
4. Login único Keycloak (SSO).
5. API Spring Boot compartilhada.
6. Ambiente dockerizado completo.
7. Domínio fiscal (PIS, COFINS, IRPJ, CSLL, regimes, carga tributária).
8. Arquitetura preparada para migração gradual JSF → Angular.
