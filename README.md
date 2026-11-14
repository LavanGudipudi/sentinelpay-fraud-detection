# SentinelPay

A portfolio project exploring how a payment operations team can investigate unusual transactions, understand why they were flagged, and record a review decision. The application is built with Java 21, Spring Boot and PostgreSQL.

It is intentionally a transparent, rules-based prototype. It does not connect to a payment network, process real payments, or determine whether a transaction is actually fraudulent.

## Run the complete demo

Requirements: Docker and Docker Compose.

```bash
docker compose up --build
```

Open [http://localhost:8080](http://localhost:8080). The first start creates the PostgreSQL schema through Flyway and loads fictional demo cases. Review decisions are stored in PostgreSQL and survive application restarts. When the dashboard calls the API, sign in with username `demo-analyst` and password `local-demo-password-change-me` (local demo credentials only). Set `DEMO_ANALYST_USERNAME` and `DEMO_ANALYST_PASSWORD` to override them.

To stop the services:

```bash
docker compose down
```

To remove the local demo database as well:

```bash
docker compose down -v
```

For a local Maven run, start PostgreSQL with the settings in `compose.yaml`, then run `mvn spring-boot:run`.

## What to explore

1. Select a case and inspect the individual risk signals.
2. Compare a high-scoring case with a normal payment.
3. Record an approval, rejection or escalation with a reviewer and reason.
4. Refresh the page or restart the application; the review state remains in PostgreSQL.
5. Inspect the audit endpoint to see the recorded review actions.

## API

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/summary` | Queue and flagged-value summary |
| GET | `/api/transactions` | List transactions and assessments |
| GET | `/api/transactions/{id}` | Read one case |
| POST | `/api/transactions/{id}/review` | Record a review decision |
| GET | `/api/audit` | Review history |
| GET | `/actuator/health` | Application health |

Example review body:

```json
{
  "decision": "ESCALATED",
  "note": "Please verify the device and contact the customer through the approved channel."
}
```

## Risk assessment

The current baseline adds points for four observable demo signals:

| Signal | Condition | Points |
|---|---|---:|
| Amount spike | Amount is more than 3x the customer's usual amount | 35 |
| Rapid activity | Previous payment was within 5 minutes | 25 |
| First-seen device | New device indicator is true | 20 |
| Cross-border | Country differs from the demo home market (NO) | 15 |

The score is capped at 100. Bands are LOW (0–24), MEDIUM (25–49), HIGH (50–74) and CRITICAL (75–100). These points are illustrative weights, not calibrated fraud probabilities. A flagged case is a reason to investigate, not a finding of fraud.

## Engineering choices

- Layered packages for API, domain, service, persistence and configuration.
- PostgreSQL for transaction, case, risk-signal and review-audit records.
- Flyway-managed schema migrations; Hibernate validates the schema rather than creating it.
- Transactional review updates, optimistic locking and HTTP 409 handling for concurrent case decisions.
- Reviewer identity is taken from the authenticated Spring Security principal, not trusted from the request body.
- HTTP Basic analyst authentication for API endpoints, BCrypt password hashing and stateless sessions. The demo uses one environment-configured in-memory analyst account.
- Request validation and consistent Problem Details error responses.
- JUnit 5 unit tests and GitHub Actions verification.
- Docker image runs as a non-root user.

## Project structure

```
src/main/java/no/lavankumar/sentinelpay/
  api/          REST endpoints, request validation and error handling
  config/       synthetic demo data initialisation
  domain/       transaction models and risk scoring
  persistence/  JPA entities and repositories
  service/      review workflow and summary queries
src/main/resources/db/migration/
  V1__create_fraud_case_tables.sql
src/main/resources/static/
  index.html    analyst case desk
src/test/
  risk scoring unit tests
```

## Scope and next steps

This is a local portfolio demonstration, not a production-ready fraud service. The dataset is synthetic. The current version uses a single in-memory analyst account configured through environment variables; it does not yet provide persistent identity management, fine-grained role administration, secrets management, durable tamper-evident audit controls, rate limiting, message streaming, model training, model monitoring, or a payment-network integration. The demo credentials in Compose are for local use only and must be replaced before any shared deployment.

A realistic next iteration would add role-based analyst access, externalised secrets, idempotent event ingestion, stronger audit integrity, integration tests with PostgreSQL, observability, and a carefully evaluated anomaly-detection model with human review.
