// Mirrors the control-api MessagingStatusResponse DTO.

export interface QueueStatus {
  name: string
  /** Programs currently listening on the queue. 0 means nothing will process its messages. */
  consumers: number
  messagesWaiting: number
}

export interface MessagingStatus {
  brokerReachable: boolean
  queues: QueueStatus[]
  checkedAt: string
}

/** The queue the worker reads deployment requests from (created by infra/solace/provision.sh). */
export const WORKER_QUEUE = 'dk8s.worker.deployment-requests'
