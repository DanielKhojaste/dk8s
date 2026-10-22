import { Link } from 'react-router'

import type { ServiceEvent, ServiceEventType } from '../types/serviceEvent'
import { DateTime } from './DateTime'

const LABELS: Record<ServiceEventType, string> = {
  SERVICE_REGISTERED: 'Registered',
  DEPLOYMENT_REQUESTED: 'Deploy requested',
  DEPLOYMENT_STARTED: 'Deploy started',
  DEPLOYMENT_COMPLETED: 'Deploy completed',
  DEPLOYMENT_FAILED: 'Deploy failed',
}

/** A vertical list of events, newest first. Each shows the Solace topic it travelled on. */
export function EventTimeline({ events, showService = false }: { events: ServiceEvent[]; showService?: boolean }) {
  return (
    <ol className="timeline">
      {events.map((event) => (
        <li key={event.id} className={`timeline__item timeline__item--${event.type.toLowerCase()}`}>
          <div className="timeline__head">
            <span className="timeline__type">{LABELS[event.type]}</span>
            {showService && (
              <Link to={`/services/${event.serviceId}`} className="code-link">
                {event.serviceName}
              </Link>
            )}
            <span className="timeline__time">
              <DateTime value={event.occurredAt} />
            </span>
          </div>
          <p className="timeline__summary">{event.summary}</p>
          <p className="timeline__topic code">{event.topic}</p>
        </li>
      ))}
    </ol>
  )
}
