import { Link, useParams } from 'react-router'

import { ApiError } from '../api/client'
import { useService } from '../api/managedServices'
import { DateTime } from '../components/DateTime'
import { Loading, LoadError } from '../components/QueryState'
import { StatusBadge } from '../components/StatusBadge'

export function ServiceDetailPage() {
  const { id: idParam } = useParams()
  // Only whole numbers are valid IDs; anything else can't exist, so don't call the API.
  const id = idParam && /^\d+$/.test(idParam) ? Number(idParam) : null
  const { data: service, isPending, isError, error } = useService(id)

  const notFound = id === null || (error instanceof ApiError && error.status === 404)

  if (notFound) {
    return (
      <>
        <h1>Service not found</h1>
        <div className="notice">
          <p>There is no registered service with ID “{idParam}”.</p>
          <Link to="/services">Back to services</Link>
        </div>
      </>
    )
  }

  if (isPending) return <Loading label="Loading service…" />
  if (isError) return <LoadError error={error} what="this service" />

  return (
    <>
      <p className="breadcrumb">
        <Link to="/services">Services</Link>
      </p>
      <div className="page-header">
        <h1 className="code">{service.name}</h1>
        <StatusBadge status={service.status} />
      </div>
      <p className="lead">{service.description ?? <span className="muted">No description</span>}</p>
      <dl className="details">
        <div>
          <dt>Version</dt>
          <dd className="code">{service.version}</dd>
        </div>
        <div>
          <dt>Service ID</dt>
          <dd className="code">{service.id}</dd>
        </div>
        <div>
          <dt>Registered</dt>
          <dd>
            <DateTime value={service.createdAt} />
          </dd>
        </div>
        <div>
          <dt>Last updated</dt>
          <dd>
            <DateTime value={service.updatedAt} />
          </dd>
        </div>
      </dl>
    </>
  )
}
