import type { DeploymentStatus } from '../types/deployment'

const LOOK: Record<DeploymentStatus, { label: string; className: string }> = {
  PENDING: { label: 'Pending', className: 'status--pending' },
  IN_PROGRESS: { label: 'In progress', className: 'status--pending' },
  SUCCEEDED: { label: 'Succeeded', className: 'status--healthy' },
  FAILED: { label: 'Failed', className: 'status--failed' },
}

export function DeploymentStatusBadge({ status }: { status: DeploymentStatus }) {
  const { label, className } = LOOK[status]
  return (
    <span className={`status ${className}`}>
      <span className="status__mark" aria-hidden="true" />
      {label}
    </span>
  )
}
