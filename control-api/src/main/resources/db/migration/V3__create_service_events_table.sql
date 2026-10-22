-- Operational event timeline: one row per message DK8s sent or received.
CREATE TABLE service_events (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    service_id    BIGINT       NOT NULL REFERENCES managed_services (id),
    deployment_id BIGINT       REFERENCES deployments (id),
    type          VARCHAR(40)  NOT NULL,
    summary       VARCHAR(500) NOT NULL,
    topic         VARCHAR(250) NOT NULL,
    occurred_at   TIMESTAMPTZ  NOT NULL,

    CONSTRAINT ck_service_events_type CHECK (type IN (
        'SERVICE_REGISTERED',
        'DEPLOYMENT_REQUESTED',
        'DEPLOYMENT_STARTED',
        'DEPLOYMENT_COMPLETED',
        'DEPLOYMENT_FAILED'))
);

CREATE INDEX ix_service_events_occurred_at ON service_events (occurred_at DESC);
CREATE INDEX ix_service_events_service_occurred_at ON service_events (service_id, occurred_at DESC);
