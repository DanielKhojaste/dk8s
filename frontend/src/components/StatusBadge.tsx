import type { ServiceStatus } from '../types/managedService'

const LABELS: Record<ServiceStatus, string> = {
  HEALTHY: 'Healthy',
  DEGRADED: 'Degraded',
  FAILED: 'Failed',
  UNKNOWN: 'Unknown',
}

/** Status shown with color *and* a distinct shape, so it never relies on color alone. */
export function StatusBadge({ status }: { status: ServiceStatus }) {
  return (
    <span className={`status status--${status.toLowerCase()}`}>
      <span className="status__mark" aria-hidden="true" />
      {LABELS[status]}
    </span>
  )
}
