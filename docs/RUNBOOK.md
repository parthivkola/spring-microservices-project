# Runbook — local smoke test

## Prerequisites

- JDK **17** (project `java.version=17`; newer JDKs may work but are untested)
- Maven 3.9+
- Docker + Docker Compose
- Free ports: 8761, 8888, 8080–8086, 6379, 9092, 8090

## 1. Infrastructure

```bash
docker compose up -d redis zookeeper kafka kafka-ui
docker compose ps
```

Wait until `kafka` is healthy. Kafka UI: http://localhost:8090

Optional (user-service Docker profile): `docker compose up -d postgres-user`

## 2. Build

```bash
mvn -B clean package -DskipTests
```

## 3. Start order (separate terminals or background)

```text
1. eureka-server          :8761
2. config-server          :8888
3. user-service           :8081
4. product-service        :8083
5. order-service          :8082
6. inventory-service      :8085
7. notification-service   :8084
8. analytics-service      :8086
9. api-gateway            :8080   (needs Redis)
```

Example:

```bash
cd eureka-server && mvn spring-boot:run
# ...
```

Or run packaged jars:

```bash
java -jar eureka-server/target/*.jar
```

## 4. Verify Eureka

Open http://localhost:8761 — expect all app names registered.

## 5. API smoke

```bash
# Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"alice@example.com","password":"password"}' | jq -r .token)

echo "token=$TOKEN"

# Create order
curl -s -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: smoke-1' \
  -d '{"userId":"u1","productId":"p1","quantity":1}' | jq .

# Consumers (via gateway)
curl -s http://localhost:8080/api/inventory/p1 -H "Authorization: Bearer $TOKEN" | jq .
curl -s http://localhost:8080/api/notifications -H "Authorization: Bearer $TOKEN" | jq .
curl -s http://localhost:8080/api/analytics/summary -H "Authorization: Bearer $TOKEN" | jq .

# Idempotency: same key must return the same order id
curl -s -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: smoke-1' \
  -d '{"userId":"u1","productId":"p1","quantity":1}' | jq .id

# Unauthenticated → 401
curl -s -o /dev/null -w '%{http_code}\n' http://localhost:8080/api/orders
```

## Demo users

| Email | Password | Role | User id |
|-------|----------|------|---------|
| alice@example.com | password | USER | u1 |
| bob@example.com | password | USER | u2 |
| admin@example.com | admin123 | ADMIN | admin |

## Full Docker stack

```bash
docker compose up --build
```

First build is slow (per-service Maven multi-stage images).

## Troubleshooting

| Symptom | Likely cause |
|---------|----------------|
| Gateway 401 on all routes | Missing `Authorization: Bearer …` |
| Gateway 500 / rate limit errors | Redis not running |
| Order create 503 | product-service down or circuit open |
| Consumers silent | Kafka not healthy / wrong `KAFKA_BOOTSTRAP` |
| user-service on 8080 | Old binary without pinned port; rebuild |
| Eureka empty | Services started before Eureka; wait/restart |
