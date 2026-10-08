# Spring Microservices Project

Hands-on Spring Boot 3.3 / Spring Cloud microservices covering interview-ready patterns.

**Docs:** [Architecture & decisions](docs/ARCHITECTURE.md) · [Audit report](docs/AUDIT.md) · [Runbook](docs/RUNBOOK.md)

```
                   ┌───────────────┐
                   │    Client     │
                   └───────┬───────┘
                           ↓
                   ┌───────────────┐
                   │ API Gateway   │  JWT auth · rate limit (Redis)
                   └───────┬───────┘
                           ↓
        ┌──────────────────┼──────────────────┐
        ↓                  ↓                  ↓
   Auth / User         Order Service      Product Service
   (JWT + RBAC)        (CB · Retry ·      (Redis cache)
        │               Idempotency)
        ↓                  ↓                  ↓
   PostgreSQL/H2      In-memory           In-memory
                           │
                           ↓
                         Kafka  (order-events)
                           │
             ┌─────────────┼─────────────┐
             ↓             ↓             ↓
       Inventory      Notification    Analytics
        Service         Service        Service
             │
             ↓
           Redis
```

---

## Services

| Service | Port | Role |
|---------|------|------|
| eureka-server | 8761 | Service discovery |
| config-server | 8888 | Centralized config |
| api-gateway | 8080 | JWT validation, routing, rate limiting |
| user-service (Auth) | 8081 | Login/register, JWT issue, users |
| order-service | 8082 | Orders, Feign, Resilience4j, Kafka producer, idempotency |
| product-service | 8083 | Catalog + Redis `@Cacheable` |
| notification-service | 8084 | Kafka consumer → notifications |
| inventory-service | 8085 | Kafka consumer → Redis inventory |
| analytics-service | 8086 | Kafka consumer → aggregates |

---

## Features implemented

| Feature | Where |
|---------|--------|
| **JWT** | `user-service` issues; `api-gateway` validates |
| **RBAC** | Roles `USER` / `ADMIN`; `@PreAuthorize` on orders |
| **Kafka** | `order-service` → `order-events` → inventory / notification / analytics |
| **Redis** | Gateway rate limit · product cache · inventory stock · order idempotency |
| **Circuit breaker / Retry / Bulkhead** | `order-service` → product/user (Resilience4j) |
| **Rate limiting** | Gateway `RequestRateLimiter` (token bucket) |
| **Idempotency** | `Idempotency-Key` header on `POST /api/orders` |
| **Centralized exceptions** | `@RestControllerAdvice` per service |
| **OpenAPI / Swagger** | `/swagger-ui.html` on domain services |
| **Docker** | `docker-compose.yml` + per-service Dockerfiles |
| **GitHub Actions** | `.github/workflows/ci.yml` |

---

## Quick start

### Infra only (local `mvn spring-boot:run`)

```bash
docker compose up -d redis zookeeper kafka kafka-ui postgres-user
```

Start order: **Eureka → Config Server → domain services → Gateway**

```bash
# terminals
cd eureka-server && mvn spring-boot:run
cd config-server && mvn spring-boot:run
cd user-service && mvn spring-boot:run
cd product-service && mvn spring-boot:run
cd order-service && mvn spring-boot:run
cd inventory-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
cd analytics-service && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
```

### Full stack

```bash
docker compose up --build
```

---

## Demo credentials

| Email | Password | Role |
|-------|----------|------|
| `alice@example.com` | `password` | USER |
| `bob@example.com` | `password` | USER |
| `admin@example.com` | `admin123` | ADMIN |

User IDs for orders: `u1` (Alice), `u2` (Bob). Products: `p1`, `p2`, `p3`.

---

## Example flow

```bash
# 1. Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"alice@example.com","password":"password"}' | jq -r .token)

# 2. Create order (with idempotency key)
curl -s -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: order-demo-1' \
  -d '{"userId":"u1","productId":"p1","quantity":1}'

# 3. Check consumers
curl -s http://localhost:8080/api/inventory/p1 -H "Authorization: Bearer $TOKEN"
curl -s http://localhost:8080/api/notifications -H "Authorization: Bearer $TOKEN"
curl -s http://localhost:8080/api/analytics/summary -H "Authorization: Bearer $TOKEN"
```

Without a token → **401** from the gateway. Wrong role → **403** from the service.

---

## Useful URLs

| UI / API | URL |
|----------|-----|
| Eureka | http://localhost:8761 |
| Gateway | http://localhost:8080 |
| Kafka UI | http://localhost:8090 |
| User Swagger | http://localhost:8081/swagger-ui.html |
| Order Swagger | http://localhost:8082/swagger-ui.html |

---

## Tech stack

Spring Boot **3.3.4** · Spring Cloud **2023.0.3** · Java **17** · Eureka · Gateway · Config Server · OpenFeign · Resilience4j · Kafka · Redis · JWT (jjwt) · Spring Security · springdoc OpenAPI · Docker Compose · GitHub Actions
