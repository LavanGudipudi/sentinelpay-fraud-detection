# SentinelPay

A small, runnable payment-risk investigation demo built with Java 21 and Spring Boot. It uses transparent rules to flag unusual synthetic transactions, explains each score, and gives an investigator a simple review workflow.

This is a portfolio demonstration, not a production fraud system. It does not process real payments, connect to banks, or make automated blocking decisions.

## Run it

**Requirements:** Java 21 and Maven 3.9+.

```bash
mvn spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080). The dashboard loads a set of synthetic cases. No external service or API key is needed.

Run the tests with:

```bash
mvn test
```

## What to try

1. Open a high-risk case and read the individual signals behind its score.
2. Compare it with a normal transaction from the same demo dataset.
3. Approve or reject a case that is waiting for review. Add your name as the reviewer.
4. Refresh the page and see that the review state remains available while the application is running.

## How the score works

The current demo uses a deliberately small, explainable ruleset:

| Signal | Example condition | Points |
|---|---|---:|
| Amount deviation | Amount is over three times the customer's reference median and above NOK 10,000 | 35 |
| Transaction velocity | Five or more recent transactions | 25 |
| Country change | Transaction country differs from the customer's usual country | 25 |
| Unusual hour | Transaction occurs between 00:00 and 05:00 UTC | 10 |

The score is capped at 100. Bands are Low (0–34), Medium (35–59), High (60–79), and Critical (80–100). High and Critical cases are marked for human review. These thresholds and weights are illustrative and are not calibrated against real fraud outcomes.

## Design

- `FraudAssessmentService` owns the rules, case state, and review audit events.
- `TransactionController` exposes a small REST API.
- `src/main/resources/static` contains the browser dashboard; it uses the same API as any other client.
- The demo data is synthetic and stored in memory. Restarting the application resets cases and review decisions.

## API

- `GET /api/cases` – list assessed cases
- `GET /api/cases/{id}` – retrieve one case with its reasons
- `POST /api/cases/{id}/review` – record an `APPROVE` or `REJECT` decision
- `GET /api/audit` – list review events

Example review request:

```bash
curl -X POST http://localhost:8080/api/cases/SP-1001/review \
  -H 'Content-Type: application/json' \
  -d '{"decision":"REJECT","reviewer":"Demo Investigator","note":"Several independent signals need follow-up."}'
```

## Next engineering steps

For a production-oriented evolution, replace the in-memory repository with PostgreSQL, add schema migrations, authenticate investigators, enforce role-based access, publish immutable audit events, add idempotency and concurrency controls, and evaluate the detection policy against labelled and representative data. A statistical or machine-learning model should only be introduced with documented data quality, explainability, drift monitoring, and human-oversight controls.

## Tech

Java 21 · Spring Boot · REST · JUnit 5 · Maven · HTML/CSS/JavaScript

## Author

Lavan Kumar Gudipudi
