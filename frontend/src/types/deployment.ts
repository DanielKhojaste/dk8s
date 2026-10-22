// Mirrors the control-api DeploymentResponse and CreateDeploymentRequest DTOs.

export type DeploymentStatus = 'PENDING' | 'IN_PROGRESS' | 'SUCCEEDED' | 'FAILED'

export interface Deployment {
  id: number
  serviceId: number
  version: string
  previousVersion: string
  status: DeploymentStatus
  failureReason: string | null
  requestedAt: string
  completedAt: string | null
}

export interface CreateDeploymentRequest {
  version: string
}
