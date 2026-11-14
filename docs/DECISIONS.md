# Design notes

These notes describe the current implementation on `feature/postgres-persistence`. They are implementation notes, not a claim that the service is production-ready.

## Why a rules-first baseline?

The project uses explicit rules rather than claiming to train or deploy a fraud model. Analysts should be able to see why a synthetic transaction was flagged. Each signal has a stable code, a human-readable explanation and a points contribution.

## Current rules

| Signal | Trigger | Points |
|---|---|---:|
| Amount spike | Amount is greater than 3x the customer's usual amount | 35 |
| Rapid activity | Previous payment was 5 minutes ago or less | 25 |
| New device | Device is marked as first-seen | 20 |
| Cross-border | Country differs from the demo home market (NO) | 15 |

The score is capped at 100. Bands are LOW (0–24), MEDIUM (25–49), HIGH (50–74) and CRITICAL (75–100). These values are illustrative weights, not calibrated fraud probabilities or model confidence. A high score is a review signal, not a finding of fraud.

## Persistence and schema

- PostgreSQL stores synthetic payment transactions, fraud cases, risk signals and review audit entries.
- Flyway owns schema changes; Hibernate validates the schema at startup.
- A fraud case is keyed by transaction ID and references its transaction.
- Risk signals have a unique constraint per transaction and signal code.
- Review updates run in a database transaction. The case uses a JPA version field for optimistic locking.

## Review workflow

A pending case can be approved, rejected or escalated. The application stores the decision, note, reviewer label and timestamp, and writes a corresponding audit entry in the same transaction. A sequential second decision is rejected with HTTP 409. Concurrent review attempts rely on optimistic locking. Optimistic-lock failures are translated into HTTP 409 with a message asking the analyst to refresh the case.

The reviewer identity is derived from the authenticated Spring Security principal; the API does not accept a reviewer name from the request body. The demo has one in-memory analyst account, so multi-user identity lifecycle and fine-grained permissions remain out of scope.

## Security boundary

The demo protects API routes with stateless HTTP Basic authentication and BCrypt password hashing. One in-memory analyst account is configured through environment variables. The default credentials are for local use only. This is not persistent identity management, fine-grained authorization, secrets management or a production deployment security design.

## Deliberate limitations

- The dataset is fictional and must never be replaced with real customer or card data.
- The scoring rules are simple heuristics and have not been calibrated against labelled data.
- No machine-learning model, streaming ingestion, payment-network integration or real payment processing is included.
- Audit records are persisted, but are not tamper-evident or independently protected from privileged database changes.
- The current API loads the full demo case set for list/summary operations; pagination and database-side aggregation are future scalability work.
- Integration, concurrency and security tests need to complement the current risk-scoring unit tests.

A production system would require governed data, authenticated identity and access controls, idempotent ingestion, durable and protected audit controls, evaluation and monitoring, privacy controls, operational resilience and human oversight.
