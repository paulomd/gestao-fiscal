# Sistema de Gestão Fiscal — Resumo das Stacks

**Projeto:** demo de evolução tecnológica gradual (JSF legado + Angular moderno + API compartilhada)  
**Repositório:** gestao-fiscal (monorepo)

---

## Visão geral

Monorepo com **migração gradual**: módulo legado (JSF) + módulo moderno (Angular) + **API única**, com **SSO Keycloak** e deploy **Docker**.

```
┌─────────────────────────────────────────────────────────────┐
│                    Gateway Nginx (:80)                      │
│  /dashboard.xhtml, /aliquotas.xhtml  →  frontend-jsf        │
│  /apuracao-fiscal, /historico      →  frontend-angular      │
│  /api/*                              →  backend             │
└─────────────────────────────────────────────────────────────┘
                              │
                    Keycloak (SSO) + PostgreSQL
```

---

## Backend (`backend/`)

| Área | Stack |
|------|--------|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.3.5 |
| API | REST (`/api/*`), Spring Web |
| Segurança | OAuth2 Resource Server (JWT Keycloak) |
| Persistência | Spring Data JPA, PostgreSQL, Flyway |
| Mapeamento | MapStruct, Lombok |
| Documentação / ops | SpringDoc OpenAPI (Swagger), Spring Actuator |
| Testes | JUnit 5, MockMvc, JaCoCo (~87% cobertura) |

---

## Frontend legado (`frontend-jsf/`)

| Área | Stack |
|------|--------|
| UI | Jakarta Faces (JSF), PrimeFaces |
| Integração Spring | JoinFaces 5.3.7 |
| Runtime | Spring Boot 3.3.5 |
| Login | Spring Security OAuth2 Client → Keycloak (client `legacy-jsf`) |
| Chamadas API | Java HttpClient + Jackson (`BackendHttpClient` / `ApiClient`) |
| Testes | JUnit 5, Spring Boot Test |

---

## Frontend moderno (`frontend-angular/`)

| Área | Stack |
|------|--------|
| Framework | Angular 13 |
| UI | Angular Material 13, Chart.js / ng2-charts |
| Auth | keycloak-angular + keycloak-js 18 (PKCE, login-required) |
| HTTP | HttpClient + Bearer (interceptor Keycloak) |
| Build | Angular CLI 13, TypeScript, RxJS 7 |

---

## Segurança e identidade

| Componente | Uso |
|------------|-----|
| Keycloak 24 | SSO, realm `entrevista` |
| Clients | `legacy-jsf` (confidential), `angular-app` (public), `backend-api` (bearer) |
| Fluxo | OpenID Connect; JWT nas chamadas à API |
| Tema | CSS corporativo em `shared/theme/` |

---

## Dados

| Item | Stack |
|------|--------|
| Banco | PostgreSQL 16 |
| Migrações | Flyway (schema + seed) |
| Domínio | Gestão fiscal (alíquotas, apuração fiscal, dashboard) |

---

## Infraestrutura

| Item | Stack |
|------|--------|
| Orquestração | Docker Compose |
| Gateway | Nginx 1.27 |
| Registry | GitHub Container Registry (GHCR) |
| Deploy VPS | GitHub Actions (SSH + pasta `deploy/`) |
| Demo | http://82.25.69.207/ |

---

## CI/CD

| Item | Stack |
|------|--------|
| CI | GitHub Actions — Maven (backend, JSF), npm (Angular), build Docker |
| Publish | Imagens `ghcr.io/.../{backend,jsf,angular}` |
| Deploy | Workflow `deploy-vps.yml` após publish |
| Otimização | paths-filter / paths-ignore (CI sob demanda) |

---

## Linha única

**Java 21 + Spring Boot 3 + PostgreSQL + Keycloak** no núcleo; **JSF/PrimeFaces** e **Angular 13** nos fronts; **Nginx + Docker + GitHub Actions** na entrega.

---

*Documento gerado a partir do repositório spring-jsf / gestao-fiscal.*
