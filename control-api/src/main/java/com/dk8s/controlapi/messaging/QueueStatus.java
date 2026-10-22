package com.dk8s.controlapi.messaging;

/**
 * What the broker reports about one queue.
 *
 * @param name             queue name, e.g. dk8s.worker.deployment-requests
 * @param consumers        programs currently listening on the queue (0 = nobody will process its messages)
 * @param messagesWaiting  messages stored in the queue, not yet processed
 */
public record QueueStatus(String name, int consumers, long messagesWaiting) {
}
