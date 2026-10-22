package com.dk8s.controlapi.dto;

import java.time.Instant;
import java.util.List;

import com.dk8s.controlapi.messaging.QueueStatus;

/**
 * Snapshot of the message broker for GET /api/messaging/queues.
 * brokerReachable=false means the broker's management API didn't answer, so queues is empty.
 */
public record MessagingStatusResponse(boolean brokerReachable, List<QueueStatus> queues, Instant checkedAt) {
}
