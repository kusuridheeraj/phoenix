# 🔥 PHOENIX — Distributed Job Orchestrator

> **A high-reliability "Guaranteed Webhook Delivery" engine for B2B platforms.**

---

## 🚀 The Mission

When your app sends a webhook to a customer, and their server is down, what happens?
- **Most apps:** Drop the event. (Bad)
- **Better apps:** Retry immediately and flood them. (Bad)
- **Phoenix:** Persists, backs off exponentially, and **guarantees delivery** even if *Phoenix itself* crashes.

---

## 🏗 Architecture

- **Core:** Java (Spring Boot)
- **Persistence:** PostgreSQL (The Source of Truth)
- **Queue:** Redis (For speed)
- **Concurrency:** Competitive Workers (Leaderless)
- **Reliability:** At-Least-Once Delivery + Idempotency Keys

---

## 📚 Documentation

- [Architecture & Design Decisions](docs/ARCHITECTURE.md)
- [Job Lifecycle State Machine](docs/JOB_LIFECYCLE.md)

---

## 🛠 Roadmap

- [ ] **Architecture:** Architecture & Job Semantics (✅ Done)
- [ ] **Engine:** Core Engine (API + Worker)
- [ ] **Recovery:** Failure Recovery (Crash Handling)
- [ ] **Concurrency:** Concurrency (Redis Locks)
- [ ] **Observability:** Observability (Lag Metrics)
- [ ] **Chaos:** Chaos Testing (Kill Workers)
- [ ] **Polish:** Final Polish & Blog

---

## License
MIT
