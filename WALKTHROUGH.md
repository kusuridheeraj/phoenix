# 🔥 Phoenix — Verification Walkthrough

> **Goal:** Verify Guaranteed Delivery and Crash Recovery.

---

## 1. Setup & Run

```bash
cd phoenix
# Build
mvn clean install

# Run (Requires Postgres & Redis running)
# Ensure Docker is up: docker run -p 5432:5432 postgres ...
mvn spring-boot:run
```

**Expected Output:**
```
Started PhoenixApplication in 2.45 seconds (JVM running for 3.1)
```

---

## 2. Verify Job Submission

**Action:**
Send a POST request to create a job.

```bash
curl -X POST http://localhost:8082/api/v1/jobs 
  -H "Content-Type: application/json" 
  -d '{"targetUrl": "https://webhook.site/...", "payload": "{}", "idempotencyKey": "txn_123"}'
```

**Expected Response (201 Created):**
```json
{
  "id": "uuid-...",
  "status": "CREATED",
  "idempotencyKey": "txn_123"
}
```

**Test Idempotency:**
Run the exact same command again.
**Expected:** `200 OK` (Not 201) and the SAME Job ID. *No duplicate job created.*

---

## 3. Verify Crash Recovery

**Action:**
Watch the logs. Every 60 seconds, you will see the "Zombie Hunter":

```
INFO  com.phoenix.service.RecoveryService : 🧟 Found 0 stuck jobs (Zombies). Recovering...
```

**Simulation:**
1. Manually update a job in DB to `RUNNING` and set `updated_at` to 10 minutes ago.
2. Wait 60 seconds.
3. Log should show: `Recovering job ID: ... Resetting to PENDING.`

---

## 4. Verify Competitive Workers

**Action:**
Check `src/worker/JobWorker.java`.

**Key Code:**
```java
Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, workerId, ...);
```
*This single line prevents race conditions in a distributed system.*
