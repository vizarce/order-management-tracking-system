#!/usr/bin/env bash
# =============================================================================
# GitHub Project Setup: Order Management Tracking System
# =============================================================================
# Creates a Kanban project with sprint iterations, all labels, milestones,
# and issues for PR #1 (dual-microservice CQRS architecture).
#
# Prerequisites:
#   gh CLI authenticated:  gh auth login
#   Repo variable:         REPO=vizarce/order-management-tracking-system
#
# Usage:
#   chmod +x .github/project-setup.sh
#   REPO=vizarce/order-management-tracking-system bash .github/project-setup.sh
# =============================================================================

set -euo pipefail

REPO="${REPO:-vizarce/order-management-tracking-system}"
OWNER="${REPO%%/*}"
REPO_NAME="${REPO##*/}"

echo "=== Setting up GitHub Project for ${REPO} ==="

# ─────────────────────────────────────────────────────────────────────────────
# 1. LABELS
# ─────────────────────────────────────────────────────────────────────────────
echo ""
echo "--- Creating / updating labels ---"

create_label() {
  local name="$1" color="$2" description="$3"
  gh label create "$name" --color "$color" --description "$description" \
    --repo "$REPO" --force
}

# Type labels
create_label "feature"         "0075ca" "New feature or request"
create_label "bug"             "d73a4a" "Something isn't working"
create_label "security"        "e4e669" "Security vulnerability or hardening"
create_label "documentation"   "0075ca" "Improvements or additions to documentation"
create_label "testing"         "bfd4f2" "Unit, integration or e2e tests"
create_label "ci-cd"           "f9d0c4" "CI/CD pipeline and automation"
create_label "review"          "cfd3d7" "Code review and quality gate"
create_label "chore"           "e4e669" "Maintenance, refactoring, tooling"

# Module labels
create_label "module: common"           "c5def5" "Shared common module"
create_label "module: order-service"    "fbca04" "Order Service (write side)"
create_label "module: tracking-service" "0e8a16" "Tracking Service (read side)"
create_label "module: infrastructure"   "e99695" "Cross-cutting infrastructure"

# Observability
create_label "observability" "1d76db" "Logging, tracing, metrics"

# Priority labels
create_label "priority: critical" "b60205" "Must be fixed immediately"
create_label "priority: high"     "e4534a" "High priority"
create_label "priority: medium"   "fbca04" "Medium priority"
create_label "priority: low"      "c2e0c6" "Low priority / nice to have"

# Size labels (T-shirt sizing)
create_label "size: XS" "ededed" "< 1 hour"
create_label "size: S"  "c5def5" "1–4 hours"
create_label "size: M"  "bfd4f2" "4–8 hours (1 day)"
create_label "size: L"  "0075ca" "1–3 days"
create_label "size: XL" "d93f0b" "> 3 days"

# Sprint labels (for filtering without a project)
create_label "sprint: 1" "f9e4b7" "Sprint 1 — Foundation"
create_label "sprint: 2" "ffddc1" "Sprint 2 — Order Service"
create_label "sprint: 3" "d4f9d4" "Sprint 3 — Tracking Service"
create_label "sprint: 4" "d4e8f9" "Sprint 4 — Integration & Testing"
create_label "sprint: 5" "f9d4f9" "Sprint 5 — Production Readiness"

echo "✓ Labels created"

# ─────────────────────────────────────────────────────────────────────────────
# 2. MILESTONES
# ─────────────────────────────────────────────────────────────────────────────
echo ""
echo "--- Creating milestones (sprints) ---"

TODAY=$(date -u +%Y-%m-%d)

create_milestone() {
  local title="$1" description="$2" due_date="$3"
  # gh doesn't support --force for milestones; create if not exists
  gh api "repos/${REPO}/milestones" \
    --method POST \
    --field title="$title" \
    --field description="$description" \
    --field due_on="${due_date}T23:59:59Z" \
    --silent 2>/dev/null || echo "  (milestone '$title' may already exist — skipping)"
}

create_milestone \
  "Sprint 1 — Foundation" \
  "Project scaffolding, root POM, Common module (shared events, DTOs, MDC constants)." \
  "$(date -u -d "+7 days" +%Y-%m-%d 2>/dev/null || date -u -v+7d +%Y-%m-%d)"

create_milestone \
  "Sprint 2 — Order Service" \
  "Full Order Service implementation: Domain, Application, Infrastructure (JPA, Kafka, Feign), Web layer, configuration." \
  "$(date -u -d "+14 days" +%Y-%m-%d 2>/dev/null || date -u -v+14d +%Y-%m-%d)"

create_milestone \
  "Sprint 3 — Tracking Service" \
  "Full Tracking Service implementation: Domain, Application, Infrastructure (MongoDB, Redis Cache-Aside, Kafka Consumer), Web layer." \
  "$(date -u -d "+21 days" +%Y-%m-%d 2>/dev/null || date -u -v+21d +%Y-%m-%d)"

create_milestone \
  "Sprint 4 — Integration & Testing" \
  "End-to-end distributed tracing wiring, unit tests (domain + web layer), integration tests, H2/test config." \
  "$(date -u -d "+28 days" +%Y-%m-%d 2>/dev/null || date -u -v+28d +%Y-%m-%d)"

create_milestone \
  "Sprint 5 — Production Readiness" \
  "Security hardening (CVE fix), CI/CD pipeline, README documentation, final code review and merge." \
  "$(date -u -d "+35 days" +%Y-%m-%d 2>/dev/null || date -u -v+35d +%Y-%m-%d)"

echo "✓ Milestones created"

# ─────────────────────────────────────────────────────────────────────────────
# Helper: create issue and return its number
# ─────────────────────────────────────────────────────────────────────────────
ISSUE_NUMBERS=()

create_issue() {
  local title="$1" body="$2" labels="$3" milestone="$4"
  local number
  number=$(gh issue create \
    --repo "$REPO" \
    --title "$title" \
    --body "$body" \
    --label "$labels" \
    --milestone "$milestone" \
    --assignee "$OWNER" \
    | grep -oE '[0-9]+$')
  echo "  Created issue #${number}: ${title}"
  ISSUE_NUMBERS+=("$number")
}

# ─────────────────────────────────────────────────────────────────────────────
# 3. ISSUES — Sprint 1: Foundation
# ─────────────────────────────────────────────────────────────────────────────
echo ""
echo "--- Creating Sprint 1 issues ---"

create_issue \
  "Set up multi-module Maven project structure" \
  "## Goal
Create the root \`pom.xml\` that wires together the three child modules:
- \`common\` — shared events, DTOs, MDC constants
- \`order-service\` — write side
- \`tracking-service\` — read side

## Acceptance Criteria
- [ ] Root \`pom.xml\` declares all three modules
- [ ] Dependency-management (BOM) section covers Spring Boot 3, Kafka, Jackson, Logback
- [ ] \`mvn clean package -DskipTests\` passes from root
- [ ] Java 17 compiler settings applied globally

## Files
- \`pom.xml\`
- \`common/pom.xml\`
- \`order-service/pom.xml\`
- \`tracking-service/pom.xml\`" \
  "feature,module: common,size: S,sprint: 1,priority: critical" \
  "Sprint 1 — Foundation"

create_issue \
  "Implement Common module — shared Kafka events, DTOs, and MDC constants" \
  "## Goal
Build the \`common\` library shared by both microservices.

## Acceptance Criteria
- [ ] \`OrderCreatedEvent\` record with all order fields
- [ ] \`OrderStatusUpdatedEvent\` record
- [ ] DTOs: \`CustomerDto\`, \`OrderDto\`, \`OrderItemDto\`, \`ProductDto\`
- [ ] \`MdcConstants\` with all MDC key constants (traceId, requestId, userId, etc.)
- [ ] Module compiles cleanly as a JAR dependency

## Files
\`common/src/main/java/com/ordertracking/common/\`
- \`event/OrderCreatedEvent.java\`
- \`event/OrderStatusUpdatedEvent.java\`
- \`dto/CustomerDto.java\`, \`OrderDto.java\`, \`OrderItemDto.java\`, \`ProductDto.java\`
- \`mdc/MdcConstants.java\`" \
  "feature,module: common,size: M,sprint: 1,priority: high" \
  "Sprint 1 — Foundation"

# ─────────────────────────────────────────────────────────────────────────────
# 4. ISSUES — Sprint 2: Order Service
# ─────────────────────────────────────────────────────────────────────────────
echo ""
echo "--- Creating Sprint 2 issues ---"

create_issue \
  "[order-service] Implement Domain Layer — aggregates, value objects, exceptions" \
  "## Goal
Implement the core domain model for Order Service following Clean Architecture.

## Acceptance Criteria
- [ ] Aggregate roots: \`Order\`, \`Customer\`, \`Product\`
- [ ] \`OrderItem\` as part of the Order aggregate
- [ ] \`OrderStatus\` enum (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)
- [ ] Value objects (immutable): \`Money\`, \`Email\`, \`CustomerId\`, \`OrderId\`
- [ ] Domain exceptions: \`DomainException\`, \`CustomerNotFoundException\`, \`ProductNotFoundException\`, \`InsufficientStockException\`
- [ ] Domain event: \`OrderCreatedEvent\`
- [ ] Business rules enforced: stock validation, email format, non-negative money

## Files
\`order-service/src/main/java/com/ordertracking/orderservice/domain/\`" \
  "feature,module: order-service,size: L,sprint: 2,priority: high" \
  "Sprint 2 — Order Service"

create_issue \
  "[order-service] Implement Application Layer — use cases and application services" \
  "## Goal
Build the application layer that orchestrates domain objects and use cases.

## Acceptance Criteria
- [ ] Use case interfaces: \`CreateCustomerUseCase\`, \`CreateOrderUseCase\`, \`CreateProductUseCase\`, \`GetOrderUseCase\`
- [ ] Application services implementing use cases: \`CustomerApplicationService\`, \`OrderApplicationService\`, \`ProductApplicationService\`
- [ ] Request/Response DTOs for all operations
- [ ] Async order confirmation via \`CompletableFuture\` + \`ScheduledExecutorService\` (no blocking sleep)
- [ ] MDC context captured across async boundary
- [ ] 202 ACCEPTED returned immediately; confirmation runs in background

## Files
\`order-service/src/main/java/com/ordertracking/orderservice/application/\`" \
  "feature,module: order-service,size: L,sprint: 2,priority: high" \
  "Sprint 2 — Order Service"

create_issue \
  "[order-service] Implement Infrastructure — JPA persistence layer (entities, repositories, adapters, mappers)" \
  "## Goal
Wire the domain repository interfaces to a real H2/PostgreSQL persistence layer via JPA.

## Acceptance Criteria
- [ ] JPA entities: \`CustomerEntity\`, \`OrderEntity\` (with \`@Version\` optimistic locking), \`OrderItemEntity\`, \`ProductEntity\`
- [ ] Spring Data JPA repositories: \`CustomerJpaRepository\`, \`OrderJpaRepository\`, \`ProductJpaRepository\`
- [ ] Adapter classes bridging domain ↔ JPA: \`CustomerRepositoryAdapter\`, \`OrderRepositoryAdapter\`, \`ProductRepositoryAdapter\`
- [ ] Mappers: \`CustomerMapper\`, \`OrderMapper\`, \`ProductMapper\`
- [ ] H2 in-memory database for dev/test; schema auto-created
- [ ] Transactional boundaries on service methods

## Files
\`order-service/src/main/java/com/ordertracking/orderservice/infrastructure/persistence/\`" \
  "feature,module: order-service,size: L,sprint: 2,priority: high" \
  "Sprint 2 — Order Service"

create_issue \
  "[order-service] Implement Infrastructure — Kafka producer and Feign client for inter-service communication" \
  "## Goal
Connect Order Service to Kafka and the Tracking Service via Feign with MDC propagation.

## Acceptance Criteria
- [ ] \`OrderEventProducer\`: publishes \`OrderCreatedEvent\` and \`OrderStatusUpdatedEvent\` to \`orders.events\` topic with MDC headers
- [ ] \`TrackingServiceClient\` (Feign): declares REST methods to call Tracking Service
- [ ] \`MdcFeignInterceptor\`: injects \`X-Request-Id\`, \`X-Trace-Id\`, \`X-User-Id\` headers from MDC into every Feign request
- [ ] Kafka producer serializer: Jackson JSON

## Files
\`order-service/src/main/java/com/ordertracking/orderservice/infrastructure/\`
- \`kafka/producer/OrderEventProducer.java\`
- \`feign/TrackingServiceClient.java\`
- \`feign/MdcFeignInterceptor.java\`" \
  "feature,module: order-service,observability,size: M,sprint: 2,priority: high" \
  "Sprint 2 — Order Service"

create_issue \
  "[order-service] Implement Web Layer — REST controllers and global exception handler" \
  "## Goal
Expose RESTful HTTP endpoints for customers, products, and orders.

## Acceptance Criteria
- [ ] \`CustomerController\`: POST /customers, GET /customers/{id}
- [ ] \`ProductController\`: POST /products, GET /products/{id}
- [ ] \`OrderController\`: POST /orders (returns 202 ACCEPTED), GET /orders/{id}
- [ ] \`GlobalExceptionHandler\` (\`@RestControllerAdvice\`): maps domain exceptions to HTTP 404/400/409
- [ ] Request validation with \`@Valid\`

## Files
\`order-service/src/main/java/com/ordertracking/orderservice/web/\`" \
  "feature,module: order-service,size: M,sprint: 2,priority: high" \
  "Sprint 2 — Order Service"

create_issue \
  "[order-service] Configure application.yml, structured JSON logging (logback-spring.xml) and MdcRequestFilter" \
  "## Goal
Configure Order Service for production-ready structured logging and distributed tracing.

## Acceptance Criteria
- [ ] \`application.yml\`: Kafka broker, H2 datasource, Spring app name, Feign timeout
- [ ] \`logback-spring.xml\`: JSON pattern with \`traceId\`, \`requestId\`, \`userId\`, \`method\`, \`uri\`, \`service\`, \`clientIp\` fields
- [ ] \`MdcRequestFilter extends OncePerRequestFilter\` at \`HIGHEST_PRECEDENCE\`:
  - Reads \`X-Request-Id\`, \`X-Trace-Id\`, \`X-User-Id\` headers (or generates new UUIDs)
  - Populates MDC; calls \`MDC.clear()\` in finally block
  - Propagates IDs back in response headers
- [ ] logback-classic pinned to 1.4.12

## Files
- \`order-service/src/main/resources/application.yml\`
- \`order-service/src/main/resources/logback-spring.xml\`
- \`order-service/src/main/java/.../infrastructure/filter/MdcRequestFilter.java\`" \
  "feature,module: order-service,observability,size: M,sprint: 2,priority: high" \
  "Sprint 2 — Order Service"

# ─────────────────────────────────────────────────────────────────────────────
# 5. ISSUES — Sprint 3: Tracking Service
# ─────────────────────────────────────────────────────────────────────────────
echo ""
echo "--- Creating Sprint 3 issues ---"

create_issue \
  "[tracking-service] Implement Domain Layer — OrderTracking model and repository interface" \
  "## Goal
Create the domain model for tracking order lifecycle on the read side.

## Acceptance Criteria
- [ ] \`OrderTracking\` domain object (orderId, status, timestamps, event log)
- [ ] \`TrackingStatus\` enum (RECEIVED, PROCESSING, SHIPPED, DELIVERED, FAILED)
- [ ] \`OrderTrackingRepository\` interface (domain-level abstraction)

## Files
\`tracking-service/src/main/java/com/ordertracking/trackingservice/domain/\`" \
  "feature,module: tracking-service,size: M,sprint: 3,priority: high" \
  "Sprint 3 — Tracking Service"

create_issue \
  "[tracking-service] Implement Application Layer — OrderTrackingService and DTOs" \
  "## Goal
Implement reactive application service that handles order tracking queries.

## Acceptance Criteria
- [ ] \`OrderTrackingDto\` and \`TrackingUpdateDto\`
- [ ] \`OrderTrackingService\`: \`Mono<OrderTrackingDto> getTracking(String orderId)\` with Cache-Aside via Redis
- [ ] Returns \`Mono.error(NotFoundException)\` when not found

## Files
\`tracking-service/src/main/java/com/ordertracking/trackingservice/application/\`" \
  "feature,module: tracking-service,size: M,sprint: 3,priority: high" \
  "Sprint 3 — Tracking Service"

create_issue \
  "[tracking-service] Implement Infrastructure — MongoDB persistence and reactive Redis Cache-Aside" \
  "## Goal
Build the reactive persistence and caching layer for the Tracking Service.

## Acceptance Criteria
- [ ] \`OrderTrackingDocument\` (\`@Document\` MongoDB)
- [ ] \`ReactiveOrderTrackingRepository\` (extends \`ReactiveMongoRepository\`)
- [ ] \`OrderTrackingRepositoryAdapter\` implementing domain repository via MongoDB
- [ ] \`OrderTrackingMapper\` (domain ↔ document ↔ DTO)
- [ ] \`ReactiveRedisConfig\`: typed \`ReactiveRedisTemplate<String, OrderTrackingDto>\` — avoids \`DefaultTyping\` entirely
- [ ] Cache-Aside pattern: Redis lookup → MongoDB fallback → cache write
- [ ] TTL configurable via \`tracking.cache.ttl-seconds\`

## Files
\`tracking-service/src/main/java/com/ordertracking/trackingservice/infrastructure/\`" \
  "feature,module: tracking-service,size: L,sprint: 3,priority: high" \
  "Sprint 3 — Tracking Service"

create_issue \
  "[tracking-service] Implement Kafka Consumer — OrderEventConsumer with MDC context restoration" \
  "## Goal
Consume Kafka events published by Order Service and update the MongoDB read model.

## Acceptance Criteria
- [ ] \`OrderEventConsumer\`: \`@KafkaListener\` on \`orders.events\` topic
- [ ] Handles \`OrderCreatedEvent\`: creates new \`OrderTracking\` document
- [ ] Handles \`OrderStatusUpdatedEvent\`: updates tracking status
- [ ] Restores MDC context from Kafka record headers (\`X-Trace-Id\`, \`X-Request-Id\`)
- [ ] Idempotent processing (safe to re-process duplicate events)
- [ ] Invalidates Redis cache on status update

## Files
\`tracking-service/src/main/java/com/ordertracking/trackingservice/infrastructure/kafka/consumer/OrderEventConsumer.java\`" \
  "feature,module: tracking-service,observability,size: M,sprint: 3,priority: high" \
  "Sprint 3 — Tracking Service"

create_issue \
  "[tracking-service] Implement Web Layer — reactive TrackingController and MdcWebFilter" \
  "## Goal
Expose reactive REST endpoints and ensure MDC propagation in WebFlux context.

## Acceptance Criteria
- [ ] \`TrackingController\`: \`GET /tracking/{orderId}\` returns \`Mono<OrderTrackingDto>\`
- [ ] \`MdcWebFilter implements WebFilter\`: propagates MDC fields from request headers through reactive context
- [ ] Returns 404 when order not found
- [ ] Content-Type: application/json

## Files
\`tracking-service/src/main/java/com/ordertracking/trackingservice/\`
- \`web/controller/TrackingController.java\`
- \`infrastructure/filter/MdcWebFilter.java\`" \
  "feature,module: tracking-service,size: M,sprint: 3,priority: high" \
  "Sprint 3 — Tracking Service"

create_issue \
  "[tracking-service] Configure application.yml and logback-spring.xml" \
  "## Goal
Configure Tracking Service runtime settings and structured JSON logging.

## Acceptance Criteria
- [ ] \`application.yml\`: MongoDB URI, Redis host/port, Kafka consumer group, cache TTL
- [ ] \`logback-spring.xml\`: same JSON pattern as Order Service for consistent log correlation
- [ ] logback-classic pinned to 1.4.12

## Files
\`tracking-service/src/main/resources/\`" \
  "feature,module: tracking-service,observability,size: S,sprint: 3,priority: medium" \
  "Sprint 3 — Tracking Service"

# ─────────────────────────────────────────────────────────────────────────────
# 6. ISSUES — Sprint 4: Integration & Testing
# ─────────────────────────────────────────────────────────────────────────────
echo ""
echo "--- Creating Sprint 4 issues ---"

create_issue \
  "Implement end-to-end distributed tracing — MDC propagation across HTTP, Feign, and Kafka" \
  "## Goal
Wire MDC context through every hop of the system so a single \`traceId\` appears in all log lines.

## Tracing Flow
\`\`\`
Browser → X-Request-Id → MdcRequestFilter (MDC populated)
       → OrderApplicationService → MdcFeignInterceptor (headers: X-Trace-Id, X-Request-Id, X-User-Id)
       → OrderEventProducer (Kafka record headers: same IDs)
       → OrderEventConsumer (MDC restored from Kafka headers)
       → MdcWebFilter (WebFlux reactive context)
\`\`\`

## Acceptance Criteria
- [ ] Single \`traceId\` visible in all log lines across both services for one request
- [ ] Works in async \`CompletableFuture\` context (MDC copied to child thread)
- [ ] Works in reactive \`WebFlux\` context (MDC propagated via reactor context)
- [ ] Kibana/Loki: filter by one \`traceId\` shows complete request lifecycle

## Verification
Run both services locally, place an order, verify \`grep traceId\` shows consistent ID in both service logs." \
  "feature,observability,size: L,sprint: 4,priority: critical" \
  "Sprint 4 — Integration & Testing"

create_issue \
  "[order-service] Write unit tests — Domain Layer (Customer, Order, Product)" \
  "## Goal
Ensure domain business rules are enforced via fast, isolated unit tests.

## Test Coverage
- [ ] \`CustomerTest\`: valid customer creation, email validation, duplicate email rejection
- [ ] \`OrderTest\`: order creation, status transitions, insufficient stock scenario
- [ ] \`ProductTest\`: product creation, stock decrease/increase, zero-stock guard

## Acceptance Criteria
- [ ] All tests pass with \`mvn test -pl order-service\`
- [ ] No Spring context loaded (pure unit tests, no \`@SpringBootTest\`)
- [ ] Coverage ≥ 80% on domain model classes

## Files
\`order-service/src/test/java/.../domain/model/\`" \
  "testing,module: order-service,size: M,sprint: 4,priority: high" \
  "Sprint 4 — Integration & Testing"

create_issue \
  "[order-service] Write unit tests — Web Layer (CustomerController, OrderController)" \
  "## Goal
Verify controller behavior with mocked application services using MockMvc.

## Test Coverage
- [ ] \`CustomerControllerTest\`: POST /customers (happy path + validation errors)
- [ ] \`OrderControllerTest\`: POST /orders (returns 202), GET /orders/{id} (found + not found)
- [ ] Verify JSON serialization of response bodies
- [ ] Verify exception mapping (404, 400, 409)

## Acceptance Criteria
- [ ] Uses \`@WebMvcTest\` with \`@MockBean\` (no full Spring context)
- [ ] All tests pass with \`mvn test -pl order-service\`

## Files
\`order-service/src/test/java/.../web/controller/\`" \
  "testing,module: order-service,size: M,sprint: 4,priority: high" \
  "Sprint 4 — Integration & Testing"

create_issue \
  "[order-service] Write integration test — Spring Boot context smoke test" \
  "## Goal
Verify the Spring application context loads without errors.

## Acceptance Criteria
- [ ] \`OrderServiceApplicationTests\`: context loads with H2 in-memory DB
- [ ] Uses \`src/test/resources/application.yml\` (H2, disabled Kafka auto-config)
- [ ] Test passes with \`mvn verify -pl order-service\`

## Files
- \`order-service/src/test/java/.../OrderServiceApplicationTests.java\`
- \`order-service/src/test/resources/application.yml\`" \
  "testing,module: order-service,size: S,sprint: 4,priority: medium" \
  "Sprint 4 — Integration & Testing"

# ─────────────────────────────────────────────────────────────────────────────
# 7. ISSUES — Sprint 5: Production Readiness
# ─────────────────────────────────────────────────────────────────────────────
echo ""
echo "--- Creating Sprint 5 issues ---"

create_issue \
  "Security: upgrade logback-classic from 1.4.11 to 1.4.12 (CVE serialization fix)" \
  "## Problem
\`logback-classic 1.4.11\` is vulnerable to a serialization CVE affecting versions \`>= 1.4.0, < 1.4.12\`.

## Fix
Pin \`logback-classic\` to \`1.4.12\` in the root \`pom.xml\` dependency management section.

## Acceptance Criteria
- [ ] Both \`order-service\` and \`tracking-service\` resolve \`logback-classic:1.4.12\`
- [ ] \`mvn dependency:tree | grep logback-classic\` shows only 1.4.12
- [ ] No test regressions

## References
- CVE: logback serialization vulnerability >= 1.4.0, < 1.4.12
- Root pom: \`<logback.version>1.4.12</logback.version>\`" \
  "security,bug,size: XS,sprint: 5,priority: critical" \
  "Sprint 5 — Production Readiness"

create_issue \
  "Set up GitHub Actions CI pipeline — build, test, and security scan" \
  "## Goal
Add an automated CI workflow that runs on every push and PR.

## Acceptance Criteria
- [ ] \`.github/workflows/ci.yml\` triggers on \`push\` and \`pull_request\` to \`main\`
- [ ] Steps: checkout → set up JDK 17 → \`mvn --batch-mode clean verify\`
- [ ] Caches \`~/.m2\` for faster builds
- [ ] Test results published as job summary
- [ ] Build badge added to README
- [ ] Optional: OWASP dependency-check step for known CVEs

## Files
\`.github/workflows/ci.yml\`" \
  "ci-cd,feature,size: M,sprint: 5,priority: high" \
  "Sprint 5 — Production Readiness"

create_issue \
  "Update README with architecture overview, setup instructions, and API reference" \
  "## Goal
Write a comprehensive README that helps new contributors run and understand the system.

## Sections
- [ ] Architecture diagram (ASCII or Mermaid): Browser → Order Service → Kafka → Tracking Service
- [ ] Module breakdown: common, order-service, tracking-service
- [ ] Prerequisites: JDK 17, Docker (Kafka, MongoDB, Redis), Maven
- [ ] Quick start: \`docker-compose up\`, \`mvn spring-boot:run\`
- [ ] API reference table: all endpoints with request/response examples
- [ ] Distributed tracing section: how to filter logs by traceId in Kibana/Loki
- [ ] Running tests: \`mvn verify\`

## Files
- \`README.md\`" \
  "documentation,size: M,sprint: 5,priority: medium" \
  "Sprint 5 — Production Readiness"

create_issue \
  "Add docker-compose.yml for local development (Kafka, MongoDB, Redis, Zookeeper)" \
  "## Goal
Provide a single \`docker-compose.yml\` that spins up all external dependencies for local development.

## Acceptance Criteria
- [ ] Services: Zookeeper, Kafka (Bitnami or Confluent), MongoDB, Redis
- [ ] Kafka topic \`orders.events\` auto-created on startup
- [ ] Health checks for all services
- [ ] Port mappings match \`application.yml\` defaults
- [ ] \`docker-compose up -d\` starts everything; both microservices connect successfully
- [ ] README quick-start updated to reference compose file

## Files
- \`docker-compose.yml\`" \
  "feature,module: infrastructure,size: M,sprint: 5,priority: high" \
  "Sprint 5 — Production Readiness"

create_issue \
  "Code review and final merge of PR #1 — dual-microservice CQRS architecture" \
  "## Goal
Review, finalize, and merge PR #1 into \`main\`.

## Checklist
- [ ] All Sprint 1–4 issues closed
- [ ] CI pipeline green
- [ ] Security: logback-classic 1.4.12 confirmed
- [ ] Clean Architecture compliance verified (no layer violations)
- [ ] MDC context thread-safety verified (CompletableFuture, WebFlux reactor context)
- [ ] Cache-Aside Redis deserialization safety confirmed (no DefaultTyping)
- [ ] \`@Version\` optimistic locking tested for concurrent order updates
- [ ] No hardcoded credentials or secrets
- [ ] Code review approved by at least one reviewer
- [ ] Draft PR #1 converted to ready-for-review

## Related PR
https://github.com/vizarce/order-management-tracking-system/pull/1" \
  "review,size: M,sprint: 5,priority: critical" \
  "Sprint 5 — Production Readiness"

# ─────────────────────────────────────────────────────────────────────────────
# 8. GITHUB PROJECT (v2)
# ─────────────────────────────────────────────────────────────────────────────
echo ""
echo "--- Creating GitHub Project (Kanban with sprints) ---"

# Create project
PROJECT_URL=$(gh project create \
  --owner "$OWNER" \
  --title "Order Management Tracking System" \
  --format json 2>/dev/null | python3 -c "import json,sys; print(json.load(sys.stdin)['url'])" 2>/dev/null || echo "")

if [ -z "$PROJECT_URL" ]; then
  echo "  ⚠ Could not create project automatically (may need 'project' scope on token)"
  echo "  → Create manually at: https://github.com/users/${OWNER}/projects/new"
  echo "  → Title: Order Management Tracking System"
else
  echo "  ✓ Project created: $PROJECT_URL"

  # Extract project number
  PROJECT_NUM=$(echo "$PROJECT_URL" | grep -oE '[0-9]+$')

  # Add all created issues to project
  echo "  Adding issues to project..."
  for num in "${ISSUE_NUMBERS[@]}"; do
    gh project item-add "$PROJECT_NUM" \
      --owner "$OWNER" \
      --url "https://github.com/${REPO}/issues/${num}" 2>/dev/null || true
  done
  echo "  ✓ Issues linked to project"
fi

echo ""
echo "=== ✅ Setup complete ==="
echo ""
echo "Next steps:"
echo "  1. Open the project at https://github.com/users/${OWNER}/projects"
echo "  2. Add an 'Iteration' field named 'Sprint' with 5 x 1-week iterations"
echo "  3. Rename the default 'Status' field options to:"
echo "     📋 Backlog | 🔍 In Analysis | 🚧 In Progress | 👀 In Review | ✅ Done"
echo "  4. Add a 'Priority' single-select field: 🔴 Critical | 🟠 High | 🟡 Medium | 🟢 Low"
echo "  5. Add a 'Size' single-select field: XS | S | M | L | XL"
echo "  6. Add a 'Module' single-select field: Common | Order Service | Tracking Service | Infrastructure"
echo "  7. Create a 'Board' (Kanban) view grouped by Status"
echo "  8. Create a 'Sprint' view grouped by Iteration field"
echo "  9. Assign each issue to its Sprint iteration and set Status → 📋 Backlog"
echo " 10. Move PR #1 items to 🚧 In Progress"
