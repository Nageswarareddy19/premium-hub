CREATE TABLE policies (
  policy_number  VARCHAR(30) PRIMARY KEY,
  holder_name    VARCHAR(100) NOT NULL,
  holder_email   VARCHAR(120),
  holder_phone   VARCHAR(20),
  status         VARCHAR(20) NOT NULL,
  expiry_date    DATE NOT NULL,
  premium_amount NUMERIC(12,2) NOT NULL,
  updated_at     TIMESTAMP NOT NULL
);

CREATE TABLE payments (
  payment_ref     VARCHAR(40) PRIMARY KEY,
  policy_number   VARCHAR(30) NOT NULL REFERENCES policies(policy_number),
  amount          NUMERIC(12,2) NOT NULL,
  currency        CHAR(3) NOT NULL,
  method          VARCHAR(20) NOT NULL,
  status          VARCHAR(25) NOT NULL,
  gateway_ref     VARCHAR(60),
  correlation_id  VARCHAR(60) NOT NULL,
  failure_reason  VARCHAR(200),
  review_flag     BOOLEAN DEFAULT FALSE,
  created_at      TIMESTAMP NOT NULL,
  updated_at      TIMESTAMP NOT NULL
);

CREATE TABLE payment_events (
  id           BIGSERIAL PRIMARY KEY,
  payment_ref  VARCHAR(40) NOT NULL,
  event_type   VARCHAR(40) NOT NULL,
  detail       VARCHAR(500),
  created_at   TIMESTAMP NOT NULL
);

CREATE TABLE notifications (
  id             BIGSERIAL PRIMARY KEY,
  policy_number  VARCHAR(30) NOT NULL,
  reminder_type  VARCHAR(20) NOT NULL,
  expiry_date    DATE NOT NULL,
  status         VARCHAR(20) NOT NULL,
  attempts       INT DEFAULT 0,
  created_at     TIMESTAMP NOT NULL,
  UNIQUE (policy_number, reminder_type, expiry_date)
);

CREATE TABLE dead_letters (
  id           BIGSERIAL PRIMARY KEY,
  payment_ref  VARCHAR(40),
  payload      TEXT,
  reason       VARCHAR(300),
  created_at   TIMESTAMP NOT NULL
);
