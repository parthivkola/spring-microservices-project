# Audit Report

Date of audit: project completion pass (post JWT/Kafka/Redis/Docker additions).

---

## Verdict

The stack matches the target architecture and feature list at **demo quality**. It compiles cleanly (`mvn clean package -DskipTests`). A local smoke run requires Redis + Kafka (+ Eureka/Config) before domain services and the gateway.

---

## Feature matrix

| Feature | Status | Notes |
|---------|--------|-------|
| JWT | Done | Issue in user-service; validate in gateway |
| RBAC | Partial | Enforced on orders; many reads/`reduce-stock` open for Feign |
| Kafka | Done | `order-events` → inventory, notification, analytics |
| Redis | Done | Rate limit, product cache, inventory, idempotency |
| Circuit breaker / retry / bulkhead | Done | order → product/user |
| Rate limiting | Done | Gateway token bucket |
| Idempotency | Done | Optional `Idempotency-Key` |
| Centralized exceptions | Done | Per-service `@RestControllerAdvice` |
| OpenAPI/Swagger | Done | Domain services |
| Docker Compose | Done | Infra + 9 apps |
| GitHub Actions | Done | Build/test; **no unit tests yet** |
| Postgres per Auth/Order/Product | Partial | User only (H2 local / Postgres Docker) |

---

## Fixes applied during audit

1. Pinned `server.port: 8081` on user-service (avoid gateway port collision if Config Server is down).
2. Eureka URLs parameterized via `EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE` on gateway, config-server, inventory, notification, analytics.
3. ZooKeeper healthcheck switched to TCP port check (Confluent images often block `ruok`).
4. Clarified Kafka producer comment (`ADD_TYPE_INFO_HEADERS=false`).
5. Added root `.gitignore` (excludes `target/`, IDE files, `.env`).

---

## Remaining known limitations

| Item | Risk | Mitigation if hardening |
|------|------|-------------------------|
| Dual stock (product map + inventory Redis) | Divergence | Single writer + saga |
| Spoofable `X-User-*` on open ports | Privilege escalation | Private network / re-validate JWT |
| Hardcoded default JWT secret | Token forgery | Env/secret manager only |
| No automated tests | Regressions | Add `@SpringBootTest` / Testcontainers |
| Consumer HTTP APIs unsecured | Direct-port bypass | Security or internal-only bind |
| Order/product not persisted | Data loss on restart | JPA + Flyway |

---

## Ownership / identity scrub

| Source | Finding | Action |
|--------|---------|--------|
| Git history / remote | Prior commits and personal remote URL | Removed (`.git` wiped); fresh `main` root commit as `Contributor <contributor@example.com>`; **no remotes** |
| Source code | No personal names/emails in Java/YAML | Demo emails only (`@example.com`) |
| Build artifacts | Absolute workstation paths under `target/` | Ignored via `.gitignore`; `target/` cleaned before commit |

---

## Smoke-test results (executed)

Infra: Redis + ZooKeeper + Kafka + Kafka UI via Compose (`cp-kafka`/`cp-zookeeper` **7.4.0** images).  
Apps: all 9 jars started locally. Gateway bound to **:18080** because host **:8080** was already occupied by another container.

| Check | Result |
|-------|--------|
| Unauthenticated `POST /api/orders` | **401** |
| Login `alice@example.com` / `password` via gateway | **200** JWT issued |
| Create order (`p1`, qty 1) + `Idempotency-Key` | **201** `CONFIRMED` |
| Replay same idempotency key | **same order id** |
| Inventory `p1` after event | stock **49** (seed 50 − 1) |
| Notifications | 1 × `ORDER_CREATED` |
| Analytics | `totalOrders=1`, `totalRevenue=999.99` |

**Note:** First gateway calls immediately after a cold start can return **503** until Eureka client caches refresh (~30s). Kafka must be healthy before order publish succeeds.
