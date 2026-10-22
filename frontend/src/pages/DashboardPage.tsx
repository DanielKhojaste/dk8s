import { Link } from 'react-router'

import { useRecentEvents } from '../api/events'
import { useServices } from '../api/managedServices'
import { EventTimeline } from '../components/EventTimeline'
import { Empty, Loading, LoadError } from '../components/QueryState'
import { ServiceTable } from '../components/ServiceTable'
import { StatusBar } from '../components/StatusBar'

const RECENT_LIMIT = 5

export function DashboardPage() {
  const { data: services, isPending, isError, error } = useServices()
  const { data: events } = useRecentEvents(8)

  return (
    <>
      <h1>Dashboard</h1>
      {isPending && <Loading label="Loading services…" />}
      {isError && <LoadError error={error} what="services" />}
      {services && services.length === 0 && (
        <Empty>
          <p>No services are registered yet.</p>
          <Link to="/services/new" className="button">
            Register a service
          </Link>
        </Empty>
      )}
      {services && services.length > 0 && (
        <>
          <StatusBar services={services} />
          <section className="section" aria-labelledby="recent-heading">
            <div className="section__header">
              <h2 id="recent-heading">Recently registered</h2>
              <Link to="/services">View all services</Link>
            </div>
            <ServiceTable
              services={[...services]
                .sort((a, b) => b.createdAt.localeCompare(a.createdAt))
                .slice(0, RECENT_LIMIT)}
            />
          </section>
          {events && events.length > 0 && (
            <section className="section" aria-labelledby="events-heading">
              <h2 id="events-heading" className="section__title">Recent events</h2>
              <EventTimeline events={events} showService />
            </section>
          )}
        </>
      )}
    </>
  )
}
