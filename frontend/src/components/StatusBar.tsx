import { SERVICE_STATUSES, type ManagedService } from '../types/managedService'
import { StatusBadge } from './StatusBadge'

/** One segmented bar showing how many registered services are in each status. */
export function StatusBar({ services }: { services: ManagedService[] }) {
  const total = services.length
  const counts = SERVICE_STATUSES.map((status) => ({
    status,
    count: services.filter((service) => service.status === status).length,
  }))

  return (
    <section className="status-bar" aria-labelledby="status-bar-heading">
      <h2 id="status-bar-heading" className="status-bar__total">
        {total} registered {total === 1 ? 'service' : 'services'}
      </h2>
      <div className="status-bar__track" aria-hidden="true">
        {counts
          .filter(({ count }) => count > 0)
          .map(({ status, count }) => (
            <span
              key={status}
              className={`status-bar__segment status-bar__segment--${status.toLowerCase()}`}
              style={{ flexGrow: count }}
            />
          ))}
      </div>
      <ul className="status-bar__legend">
        {counts.map(({ status, count }) => (
          <li key={status}>
            <StatusBadge status={status} />
            <span className="status-bar__count">{count}</span>
          </li>
        ))}
      </ul>
    </section>
  )
}
