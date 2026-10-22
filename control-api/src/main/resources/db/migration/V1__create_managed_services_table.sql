CREATE TABLE managed_services (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(63)  NOT NULL,
    description VARCHAR(500),
    version     VARCHAR(50)  NOT NULL,
    status      VARCHAR(20)  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,

    CONSTRAINT uq_managed_services_name UNIQUE (name),
    CONSTRAINT ck_managed_services_status
        CHECK (status IN ('HEALTHY', 'DEGRADED', 'FAILED', 'UNKNOWN'))
);
