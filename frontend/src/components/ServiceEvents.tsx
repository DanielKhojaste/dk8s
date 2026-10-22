import { useServiceEvents } from '../api/events'
import { EventTimeline } from './EventTimeline'
import { Empty, Loading, LoadError } from './QueryState'

export function ServiceEvents({ serviceId }: { serviceId: number }) {
  const { data: events, isPending, isError, error } = useServiceEvents(serviceId)

  if (isPending) return <Loading label="Loading events…" />
  if (isError) return <LoadError error={error} what="events" />
  if (events.length === 0) return <Empty><p>No events yet.</p></Empty>
  return <EventTimeline events={events} />
}
