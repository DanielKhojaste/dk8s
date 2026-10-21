// These types mirror the control-api DTOs (ManagedServiceResponse, CreateManagedServiceRequest).

export type ServiceStatus = 'HEALTHY' | 'DEGRADED' | 'FAILED' | 'UNKNOWN'

export const SERVICE_STATUSES: ServiceStatus[] = ['HEALTHY', 'DEGRADED', 'FAILED', 'UNKNOWN']

export interface ManagedService {
  id: number
  name: string
  description: string | null
  version: string
  status: ServiceStatus
  /** ISO-8601 UTC timestamp, e.g. "2026-10-20T23:47:28.224502Z" */
  createdAt: string
  updatedAt: string
}

export interface CreateManagedServiceRequest {
  name: string
  description?: string
  version: string
}
