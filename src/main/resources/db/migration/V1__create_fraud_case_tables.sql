CREATE TABLE payment_transaction (
    id VARCHAR(64) PRIMARY KEY,
    customer_name VARCHAR(160) NOT NULL,
    amount_nok NUMERIC(19,2) NOT NULL CHECK (amount_nok > 0),
    merchant VARCHAR(160) NOT NULL,
    country CHAR(2) NOT NULL,
    minutes_since_previous INTEGER NOT NULL CHECK (minutes_since_previous >= 0),
    usual_amount_nok NUMERIC(19,2) NOT NULL CHECK (usual_amount_nok > 0),
    new_device BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE fraud_case (
    transaction_id VARCHAR(64) PRIMARY KEY REFERENCES payment_transaction(id),
    risk_score INTEGER NOT NULL CHECK (risk_score BETWEEN 0 AND 100),
    risk_level VARCHAR(16) NOT NULL,
    review_status VARCHAR(16) NOT NULL,
    reviewer_note VARCHAR(500),
    reviewer VARCHAR(80),
    reviewed_at TIMESTAMPTZ,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE risk_signal (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(64) NOT NULL REFERENCES payment_transaction(id) ON DELETE CASCADE,
    signal_code VARCHAR(40) NOT NULL,
    title VARCHAR(160) NOT NULL,
    detail VARCHAR(500) NOT NULL,
    points INTEGER NOT NULL CHECK (points >= 0),
    UNIQUE(transaction_id, signal_code)
);

CREATE TABLE review_audit (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(64) NOT NULL REFERENCES payment_transaction(id),
    decision VARCHAR(16) NOT NULL,
    note VARCHAR(500) NOT NULL,
    reviewer VARCHAR(80) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_fraud_case_status_created ON fraud_case(review_status);
CREATE INDEX idx_fraud_case_score ON fraud_case(risk_score);
CREATE INDEX idx_review_audit_transaction ON review_audit(transaction_id, created_at);
