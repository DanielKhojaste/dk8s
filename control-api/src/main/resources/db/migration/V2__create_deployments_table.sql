CREATE TABLE deployments (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    service_id       BIGINT       NOT NULL REFERENCES managed_services (id),
    version          VARCHAR(50)  NOT NULL,
    previous_version VARCHAR(50)  NOT NULL,
    status           VARCHAR(20)  NOT NULL,
    failure_reason   VARCHAR(500),
    requested_at     TIMESTAMPTZ  NOT NULL,
    completed_at     TIMESTAMPTZ,

    CONSTRAINT ck_deployments_status
        CHECK (status IN ('PENDING', 'IN_PROGRESS', 'SUCCEEDED', 'FAILED'))
);

-- Deployment history is always read per service, newest first.
CREATE INDEX ix_deployments_service_requested_at ON deployments (service_id, requested_at DESC);
