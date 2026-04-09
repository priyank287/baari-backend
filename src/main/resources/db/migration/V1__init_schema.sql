-- ─────────────────────────────────────────────
-- HOSPITALS
-- ─────────────────────────────────────────────
CREATE TABLE hospitals (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                        VARCHAR(255) NOT NULL,
    address                     VARCHAR(255),
    phone                       VARCHAR(255),
    whatsapp_sender_id          VARCHAR(255),
    plan_type                   VARCHAR(50)  NOT NULL DEFAULT 'BASIC',
    display_token               VARCHAR(255) UNIQUE,
    display_token_active        BOOLEAN      NOT NULL DEFAULT TRUE,
    display_token_generated_at  TIMESTAMP,
    is_active                   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at                  TIMESTAMP    NOT NULL DEFAULT now()
);

-- ─────────────────────────────────────────────
-- SUBSCRIPTIONS
-- ─────────────────────────────────────────────
CREATE TABLE subscriptions (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hospital_id      UUID        NOT NULL REFERENCES hospitals(id),
    plan_type        VARCHAR(50) NOT NULL,
    status           VARCHAR(50) NOT NULL DEFAULT 'TRIAL',
    start_date       DATE,
    end_date         DATE,
    max_departments  INT         NOT NULL DEFAULT 1,
    max_doctors      INT         NOT NULL DEFAULT 3,
    max_staff_users  INT         NOT NULL DEFAULT 2,
    created_at       TIMESTAMP   NOT NULL DEFAULT now()
);

-- ─────────────────────────────────────────────
-- DEPARTMENTS
-- ─────────────────────────────────────────────
CREATE TABLE departments (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hospital_id  UUID         NOT NULL REFERENCES hospitals(id),
    name         VARCHAR(255) NOT NULL,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE
);

-- ─────────────────────────────────────────────
-- DOCTORS
-- ─────────────────────────────────────────────
CREATE TABLE doctors (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hospital_id      UUID         NOT NULL REFERENCES hospitals(id),
    department_id    UUID         NOT NULL REFERENCES departments(id),
    name             VARCHAR(255) NOT NULL,
    specialization   VARCHAR(255),
    is_available     BOOLEAN      NOT NULL DEFAULT TRUE,
    can_manage_queue BOOLEAN      NOT NULL DEFAULT FALSE
);

-- ─────────────────────────────────────────────
-- STAFF USERS
-- ─────────────────────────────────────────────
CREATE TABLE staff_users (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hospital_id   UUID         REFERENCES hospitals(id),
    doctor_id     UUID         REFERENCES doctors(id),
    name          VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(50)  NOT NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    last_login_at TIMESTAMP,
    created_at    TIMESTAMP    NOT NULL DEFAULT now()
);

-- ─────────────────────────────────────────────
-- SESSIONS
-- ─────────────────────────────────────────────
CREATE TABLE sessions (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hospital_id    UUID        NOT NULL REFERENCES hospitals(id),
    doctor_id      UUID        NOT NULL REFERENCES doctors(id),
    department_id  UUID        NOT NULL REFERENCES departments(id),
    created_by     UUID        NOT NULL REFERENCES staff_users(id),
    label          VARCHAR(50) NOT NULL,
    session_date   DATE        NOT NULL,
    status         VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    started_at     TIMESTAMP,
    closed_at      TIMESTAMP
);

-- ─────────────────────────────────────────────
-- QUEUE ENTRIES
-- ─────────────────────────────────────────────
CREATE TABLE queue_entries (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hospital_id       UUID         NOT NULL REFERENCES hospitals(id),
    session_id        UUID         NOT NULL REFERENCES sessions(id),
    doctor_id         UUID         NOT NULL REFERENCES doctors(id),
    department_id     UUID         NOT NULL REFERENCES departments(id),
    patient_name      VARCHAR(255) NOT NULL,
    mobile_number     VARCHAR(255) NOT NULL,
    token_number      INT          NOT NULL,
    status            VARCHAR(50)  NOT NULL DEFAULT 'WAITING',
    wait_time_minutes INT,
    registered_at     TIMESTAMP    NOT NULL DEFAULT now(),
    called_at         TIMESTAMP,
    completed_at      TIMESTAMP,
    queue_date        DATE         NOT NULL
);

CREATE INDEX idx_queue_hospital_date  ON queue_entries (hospital_id, queue_date);
CREATE INDEX idx_queue_session_status ON queue_entries (session_id, status);
CREATE INDEX idx_queue_doctor_date    ON queue_entries (doctor_id, queue_date);

-- ─────────────────────────────────────────────
-- NOTIFICATION LOGS
-- ─────────────────────────────────────────────
CREATE TABLE notification_logs (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    queue_entry_id    UUID        NOT NULL REFERENCES queue_entries(id),
    message_type      VARCHAR(50) NOT NULL,
    whatsapp_message  TEXT,
    delivery_status   VARCHAR(50) NOT NULL DEFAULT 'SENT',
    sent_at           TIMESTAMP
);

-- ─────────────────────────────────────────────
-- DAILY STATS
-- ─────────────────────────────────────────────
CREATE TABLE daily_stats (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hospital_id      UUID NOT NULL REFERENCES hospitals(id),
    doctor_id        UUID NOT NULL REFERENCES doctors(id),
    department_id    UUID NOT NULL REFERENCES departments(id),
    stat_date        DATE NOT NULL,
    total_patients   INT  NOT NULL DEFAULT 0,
    completed        INT  NOT NULL DEFAULT 0,
    skipped          INT  NOT NULL DEFAULT 0,
    no_shows         INT  NOT NULL DEFAULT 0,
    avg_wait_minutes INT  NOT NULL DEFAULT 0,
    peak_hour        INT  NOT NULL DEFAULT 0,
    UNIQUE (hospital_id, doctor_id, stat_date)
);
