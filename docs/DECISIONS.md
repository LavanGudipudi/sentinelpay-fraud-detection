# Design notes

## Why a rules-first baseline?

The first version uses a small, explicit ruleset rather than claiming to train a fraud model. In a payment environment, a reviewer needs to understand what caused a case to surface. Each signal therefore has a stable code, a plain-language explanation and a points contribution.

## Current rules

| Signal | Trigger | Points |
|---|---|---:|
| Amount spike | Amount is greater than 3x the customer's usual amount | 35 |
| Rapid activity | Previous payment was 5 minutes ago or less | 25 |
| New device | Device is marked as first-seen | 20 |
| Cross-border | Country differs from the demo home market (NO) | 15 |

The score is capped at 100. Bands are LOW (0–24), MEDIUM (25–49), HIGH (50–74) and CRITICAL (75–100). These values are illustrative and are not calibrated probabilities.

## Review workflow

A pending case can be approved, rejected or escalated. The demo records the reviewer, note, decision and timestamp. A second decision on the same case is rejected with HTTP 409.

## Deliberate limitations

- All transactions and review history live in memory; a restart resets them.
- The seeded records are fictional and must not be replaced with real customer or card data.
- There is no authentication, authorization, rate limiting or production-grade audit storage.
- Rules do not establish fraud. They surface patterns for investigation.
- No machine-learning model or real-time payment integration is included.

These constraints are intentional for a small, runnable portfolio demonstration. A production design would need governed data, identity and access controls, durable event storage, model validation, monitoring, privacy controls and human oversight.
