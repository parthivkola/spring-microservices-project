# Architecture & Design Decisions

Learning-oriented Spring Boot microservices platform. This document records **what** was built, **which technologies** were chosen, and **why**.

---

## 1. System overview

```
Client
  │
  ▼
API Gateway (:8080)
  │  JWT validation · Redis rate limiting · routing (lb://)
  ▼
┌─────────────┬──────────────┬───────────────┐
│ Auth/User   │ Order        │ Product       │
│ :8081       │ :8082        │ :8083         │
│ H2/Postgres │ Feign+R4j    │ Redis cache   │
└─────────────┴──────┬───────┴───────────────┘
                     │ OrderCreatedEvent
                     ▼
                   Kafka (order-events)
                     │
       ┌─────────────┼─────────────┐
       ▼             ▼             ▼
  Inventory     Notification   Analytics
  :8085 Redis   :8084          :8086
```

**Platform services:** Eureka (:8761), Config Server (:8888).

---

## 2. Technology choices

| Concern | Choice | Version / notes | Reasoning |
|---------|--------|-----------------|-----------|
| Runtime | Java 17 | LTS | Matches Spring Boot 3.x baseline; widely available |
| Framework | Spring Boot | 3.3.4 | Current stable 3.3 line used by the parent POM |
| Cloud | Spring Cloud | 2023.0.3 | Compatible BOM for Boot 3.3 |
| Discovery | Netflix Eureka | via Spring Cloud | Simple dashboard; interview-standard vs Consul/K8s DNS |
| Gateway | Spring Cloud Gateway | WebFlux/Netty | Non-blocking; replaces Zuul 1; native Redis rate limiter |
| Config | Spring Cloud Config (native) | local `config-repo/` | Demonstrates centralized config + `@RefreshScope` without needing Git |
| Sync RPC | OpenFeign + LoadBalancer | | Declarative clients; Eureka-aware client-side LB |
| Resilience | Resilience4j | CB, Retry, Bulkhead | Annotation-driven; health indicators for demos |
| Messaging | Apache Kafka | Confluent 7.6 images | Industry default for event fan-out; UI on :8090 |
| Cache / rate limit / idempotency / inventory | Redis 7 | | One infra component, four teaching use-cases |
| Auth | JWT (jjwt 0.12) HS256 | | Shared secret keeps gateway + auth service simple; RS256 is the production upgrade path |
| Authorization | Spring Security + `@PreAuthorize` | | Gateway authenticates; services authorize via roles |
| API docs | springdoc-openapi 2.6 | | OpenAPI 3 + Swagger UI without hand-written specs |
| User DB | H2 (local) / PostgreSQL (Docker) | | Database-per-service demo without forcing Docker for every `mvn` run |
| Build | Maven multi-module | | One reactor build; clear module boundaries |
| Containers | Docker + Compose | multi-stage JRE 17 | Reproducible full-stack local run |
| CI | GitHub Actions | Temurin 17 | Compile gate on push/PR |

---

## 3. Decision records (ADRs)

### ADR-001 — Gateway authenticates; services authorize

**Decision:** Validate JWT only in the API Gateway. Propagate `X-User-Id` / `X-User-Roles` to downstream services.

**Why:** Avoids duplicating crypto validation in every service; matches a common edge-auth pattern.

**Trade-off:** Downstream ports must not be publicly exposed — headers are spoofable. For production: private network, mTLS, or re-validate JWT in each service.

---

### ADR-002 — HS256 shared secret (not RS256)

**Decision:** Use a shared HMAC secret (`JWT_SECRET`) for sign/verify.

**Why:** Fewer moving parts for a learning project (no key distribution).

**Trade-off:** Any service with the secret can mint tokens. Production preference: RS256/ES256 with private key only in auth service.

---

### ADR-003 — Auth lives in `user-service` (not a separate auth module)

**Decision:** Login/register + user lookup share one deployable (`user-service`).

**Why:** Diagram shows “Auth Service”; Feign clients already call `user-service`. Keeping one module avoids breaking discovery names and Feign contracts.

**Trade-off:** Auth and user profile concerns are coupled. A future split would extract `/api/auth/**` into `auth-service`.

---

### ADR-004 — Dual stock paths (Product sync + Inventory async)

**Decision:**
- Order creation **synchronously** reduces stock via Feign → `product-service`.
- Kafka event **asynchronously** updates Redis inventory in `inventory-service`.

**Why:** Teaches both sync orchestration and event-driven side effects in one flow.

**Trade-off:** Two sources of truth can diverge if Kafka lags or fails. Production: single inventory owner, or saga/outbox with compensating transactions.

---

### ADR-005 — No shared library for `OrderCreatedEvent`

**Decision:** Duplicate the event POJO in producer and each consumer.

**Why:** Keeps services independently buildable without a `common` JAR (easier for learners).

**Trade-off:** Field drift risk. Production: shared Avro/Protobuf schema registry or a versioned `events` module.

---

### ADR-006 — Kafka JSON without type headers

**Decision:** Producer sets `ADD_TYPE_INFO_HEADERS=false`; consumers set `spring.json.value.default.type`.

**Why:** Avoids coupling consumers to producer’s Java package name in headers.

**Trade-off:** Each consumer must declare the target class explicitly.

---

### ADR-007 — In-memory stores for Order & Product (demo)

**Decision:** Orders and products use `ConcurrentHashMap`, not Postgres (except users).

**Why:** Focus on patterns (Feign, CB, Kafka, cache) without Flyway/JPA ceremony everywhere.

**Trade-off:** Data lost on restart; diagram’s “PostgreSQL per service” is only fully applied to auth/user.

---

### ADR-008 — Optional Config Server

**Decision:** `spring.config.import=optional:configserver:...` plus pinned `server.port` on user-service.

**Why:** Services still start if Config Server is down; port pin prevents accidental bind to :8080 (gateway collision).

---

### ADR-009 — Idempotency via Redis + optional header

**Decision:** `Idempotency-Key` on `POST /api/orders` caches the response JSON in Redis (24h TTL).

**Why:** Demonstrates safe client retries without duplicate orders.

**Trade-off:** Header is optional (missing key = always create). Production APIs often require the key on mutating endpoints.

---

### ADR-010 — Rate limiting at the gateway only

**Decision:** Redis token-bucket `RequestRateLimiter` per IP on gateway routes.

**Why:** Protects the fleet at the edge; one place to tune limits.

**Trade-off:** Bypass if callers hit service ports directly.

---

## 4. Cross-cutting patterns map

| Pattern | Location |
|---------|----------|
| Service discovery | Eureka + `lb://` routes |
| Centralized config | Config Server + `config-repo/` |
| API gateway | `api-gateway` |
| JWT + RBAC | user-service issue; gateway validate; order `@PreAuthorize` |
| Circuit breaker / retry / bulkhead | `order-service` Resilience4j gateways |
| Sync inter-service | OpenFeign (+ header propagation) |
| Async fan-out | Kafka `order-events` |
| Cache-aside | `product-service` `@Cacheable` / `@CacheEvict` |
| Idempotency | `order-service` + Redis |
| Rate limiting | Gateway + Redis |
| Exception handling | `@RestControllerAdvice` per service |
| OpenAPI | springdoc on domain services |
| Containerization | Dockerfiles + `docker-compose.yml` |
| CI | `.github/workflows/ci.yml` |

---

## 5. Security model (honest scope)

**In scope for this demo**
- Login/register → JWT
- Gateway rejects missing/invalid Bearer tokens (except `/api/auth/**`)
- Role checks on order endpoints

**Known demo limitations (documented, not production-ready)**
- Default JWT secret in YAML/compose
- Trust of `X-User-*` on internal calls
- Several Feign targets `permitAll` so inter-service calls work without tokens
- Consumer services (inventory/notification/analytics) have no Spring Security; rely on gateway + network boundary
- CSRF disabled (stateless JWT APIs)

---

## 6. Runtime ports

| Port | Component |
|------|-----------|
| 8761 | Eureka |
| 8888 | Config Server |
| 8080 | API Gateway |
| 8081 | User / Auth |
| 8082 | Order |
| 8083 | Product |
| 8084 | Notification |
| 8085 | Inventory |
| 8086 | Analytics |
| 6379 | Redis |
| 9092 | Kafka (host) |
| 8090 | Kafka UI |
| 5433 | Postgres (userdb, host-mapped) |

---

## 7. Suggested production upgrades

1. RS256 JWT + JWKS; revoke/blocklist if needed  
2. Database-per-service for order & product + Flyway  
3. Outbox pattern for Kafka publish  
4. Single inventory authority + saga compensations  
5. Network policies: only gateway publicly routable  
6. Distributed tracing (Micrometer + Zipkin/OTel)  
7. Real automated tests + contract tests (Pact)  
8. Secrets via vault/K8s secrets — never defaults in compose  
