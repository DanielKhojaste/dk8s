// Mirrors the control-api ServiceEventResponse DTO.

export type ServiceEventType =
  | 'SERVICE_REGISTERED'
  | 'DEPLOYMENT_REQUESTED'
  | 'DEPLOYMENT_STARTED'
  | 'DEPLOYMENT_COMPLETED'
  | 'DEPLOYMENT_FAILED'

export interface ServiceEvent {
  id: number
  serviceId: number
  serviceName: string
  deploymentId: number | null
  type: ServiceEventType
  summary: string
  /** The Solace topic the message was published on, e.g. dk8s/deployment/completed/payments-api */
  topic: string
  occurredAt: string
}
