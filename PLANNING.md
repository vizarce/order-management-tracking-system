# Order Management Tracking System — Project Planning

## Overview

This document describes the **GitHub Project board**, sprint structure, and issue breakdown for [PR #1](https://github.com/vizarce/order-management-tracking-system/pull/1) — a dual-microservice CQRS system built with Spring Boot 3.

> **Quick setup:** Run `.github/project-setup.sh` with `gh` CLI authenticated to create the project, all labels, milestones, and issues automatically.

---

## Architecture Summary

```
Browser → X-Request-Id → Order Service (write side)
              │  JPA / H2            │ Kafka producer
              │  Clean Architecture  ↓
              │              orders.events topic
              │                      │
              └── Feign ────────→  Tracking Service (read side)
                                     │  Kafka consumer
                                     │  MongoDB + Redis (Cache-Aside)
                                     │  Spring WebFlux (reactive)
                                     ↓
                              GET /tracking/{orderId}
```

**Modules:**
| Module | Role | Stack |
|---|---|---|
| `common` | Shared events, DTOs, MDC constants | Plain Java records |
| `order-service` | Write side (CQRS Command) | Spring Boot 3, JPA, H2, Kafka Producer, Feign |
| `tracking-service` | Read side (CQRS Query) | Spring WebFlux, MongoDB, Reactive Redis, Kafka Consumer |

---

## GitHub Project Setup

**Project name:** `Order Management Tracking System`  
**Type:** Kanban board with Sprint iterations  
**URL:** https://github.com/users/vizarce/projects (after creation)

### Kanban Columns (Status field)
| Column | Meaning |
|---|---|
| 📋 Backlog | Not yet started |
| 🔍 In Analysis | Being investigated / designed |
| 🚧 In Progress | Actively being implemented |
| 👀 In Review | PR open, awaiting review |
| ✅ Done | Merged / closed |

### Custom Fields
| Field | Type | Values |
|---|---|---|
| Sprint | Iteration | Sprint 1–5 (1 week each) |
| Priority | Single select | 🔴 Critical, 🟠 High, 🟡 Medium, 🟢 Low |
| Size | Single select | XS, S, M, L, XL |
| Module | Single select | Common, Order Service, Tracking Service, Infrastructure |

### Views
1. **Board** — Kanban grouped by Status
2. **Sprint** — Table/board grouped by Sprint iteration
3. **Backlog** — Table sorted by Priority, filtered to Backlog status

---

## Labels

### Type
| Label | Color | Purpose |
|---|---|---|
| `feature` | `#0075ca` | New feature |
| `bug` | `#d73a4a` | Something broken |
| `security` | `#e4e669` | Security issue |
| `documentation` | `#0075ca` | Docs update |
| `testing` | `#bfd4f2` | Tests |
| `ci-cd` | `#f9d0c4` | CI/CD pipeline |
| `review` | `#cfd3d7` | Code review gate |
| `chore` | `#e4e669` | Maintenance |

### Module
| Label | Color |
|---|---|
| `module: common` | `#c5def5` |
| `module: order-service` | `#fbca04` |
| `module: tracking-service` | `#0e8a16` |
| `module: infrastructure` | `#e99695` |
| `observability` | `#1d76db` |

### Priority
`priority: critical` 🔴 · `priority: high` 🟠 · `priority: medium` 🟡 · `priority: low` 🟢

### Size
`size: XS` · `size: S` · `size: M` · `size: L` · `size: XL`

### Sprint
`sprint: 1` · `sprint: 2` · `sprint: 3` · `sprint: 4` · `sprint: 5`

---

## Milestones (Sprints)

| Milestone | Duration | Goal |
|---|---|---|
| **Sprint 1 — Foundation** | Week 1 | Root POM, Common module |
| **Sprint 2 — Order Service** | Week 2 | Full write-side implementation |
| **Sprint 3 — Tracking Service** | Week 3 | Full read-side reactive implementation |
| **Sprint 4 — Integration & Testing** | Week 4 | End-to-end tracing, unit & integration tests |
| **Sprint 5 — Production Readiness** | Week 5 | Security, CI/CD, docs, final review & merge |

---

## Issues

### Sprint 1 — Foundation

| # | Title | Labels | Size | Priority |
|---|---|---|---|---|
| 1 | Set up multi-module Maven project structure | `feature` `module: common` `sprint: 1` | S | Critical |
| 2 | Implement Common module — shared Kafka events, DTOs, and MDC constants | `feature` `module: common` `sprint: 1` | M | High |

---

### Sprint 2 — Order Service (Write Side)

| # | Title | Labels | Size | Priority |
|---|---|---|---|---|
| 3 | [order-service] Domain Layer — aggregates, value objects, exceptions | `feature` `module: order-service` `sprint: 2` | L | High |
| 4 | [order-service] Application Layer — use cases and application services | `feature` `module: order-service` `sprint: 2` | L | High |
| 5 | [order-service] Infrastructure — JPA persistence layer | `feature` `module: order-service` `sprint: 2` | L | High |
| 6 | [order-service] Infrastructure — Kafka producer and Feign client | `feature` `module: order-service` `observability` `sprint: 2` | M | High |
| 7 | [order-service] Web Layer — REST controllers and global exception handler | `feature` `module: order-service` `sprint: 2` | M | High |
| 8 | [order-service] Configure application.yml, logback-spring.xml, MdcRequestFilter | `feature` `module: order-service` `observability` `sprint: 2` | M | High |

---

### Sprint 3 — Tracking Service (Read Side)

| # | Title | Labels | Size | Priority |
|---|---|---|---|---|
| 9 | [tracking-service] Domain Layer — OrderTracking model and repository | `feature` `module: tracking-service` `sprint: 3` | M | High |
| 10 | [tracking-service] Application Layer — OrderTrackingService and DTOs | `feature` `module: tracking-service` `sprint: 3` | M | High |
| 11 | [tracking-service] Infrastructure — MongoDB persistence and Redis Cache-Aside | `feature` `module: tracking-service` `sprint: 3` | L | High |
| 12 | [tracking-service] Kafka Consumer — OrderEventConsumer with MDC restoration | `feature` `module: tracking-service` `observability` `sprint: 3` | M | High |
| 13 | [tracking-service] Web Layer — reactive TrackingController and MdcWebFilter | `feature` `module: tracking-service` `sprint: 3` | M | High |
| 14 | [tracking-service] Configure application.yml and logback-spring.xml | `feature` `module: tracking-service` `observability` `sprint: 3` | S | Medium |

---

### Sprint 4 — Integration & Testing

| # | Title | Labels | Size | Priority |
|---|---|---|---|---|
| 15 | End-to-end distributed tracing — MDC propagation across HTTP, Feign, Kafka | `feature` `observability` `sprint: 4` | L | Critical |
| 16 | [order-service] Unit tests — Domain Layer (Customer, Order, Product) | `testing` `module: order-service` `sprint: 4` | M | High |
| 17 | [order-service] Unit tests — Web Layer (CustomerController, OrderController) | `testing` `module: order-service` `sprint: 4` | M | High |
| 18 | [order-service] Integration test — Spring Boot context smoke test | `testing` `module: order-service` `sprint: 4` | S | Medium |

---

### Sprint 5 — Production Readiness

| # | Title | Labels | Size | Priority |
|---|---|---|---|---|
| 19 | ~~Security: upgrade logback-classic 1.4.11 → 1.4.12 (CVE fix)~~ ✅ | `security` `bug` `sprint: 5` | XS | Critical |
| 20 | Set up GitHub Actions CI pipeline — build, test, security scan | `ci-cd` `feature` `sprint: 5` | M | High |
| 21 | Update README — architecture, setup instructions, API reference | `documentation` `sprint: 5` | M | Medium |
| 22 | Add docker-compose.yml (Kafka, MongoDB, Redis, Zookeeper) | `feature` `module: infrastructure` `sprint: 5` | M | High |
| 23 | Code review and final merge of PR #1 | `review` `sprint: 5` | M | Critical |

---

## Sprint Capacity Overview

| Sprint | Issues | Total Size | Focus |
|---|---|---|---|
| Sprint 1 | 2 | S + M | Foundation: Maven structure, shared code |
| Sprint 2 | 6 | 3×L + 3×M | Order Service: all 4 layers + config |
| Sprint 3 | 6 | L + 4×M + S | Tracking Service: all layers + config |
| Sprint 4 | 4 | L + 2×M + S | Tracing wiring + unit/integration tests |
| Sprint 5 | 5 | 3×M + XS + M | Security, CI/CD, docs, final review |

---

## Running the Setup Script

```bash
# Prerequisites: gh CLI authenticated with 'project' and 'repo' scopes
gh auth login
gh auth refresh -s project

# Run setup (creates labels, milestones, issues, and project board)
REPO=vizarce/order-management-tracking-system bash .github/project-setup.sh
```

The script will:
1. Create/update all 25 labels
2. Create 5 milestones (one per sprint)
3. Create all 23 issues with full descriptions, labels, and milestone assignments
4. Create the GitHub Project and link all issues to it

> **Note:** After running the script, manually configure the Project fields (Sprint iterations, Priority, Size, Module) and board views via the GitHub Projects UI, as these require interactive setup.
