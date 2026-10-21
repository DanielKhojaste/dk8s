import { Link } from 'react-router'

import { useServices } from '../api/managedServices'
import { Empty, Loading, LoadError } from '../components/QueryState'
import { ServiceTable } from '../components/ServiceTable'

export function ServicesPage() {
  const { data: services, isPending, isError, error } = useServices()

  return (
    <>
      <div className="page-header">
        <h1>Services</h1>
        <Link to="/services/new" className="button">
          Register service
        </Link>
      </div>
      {isPending && <Loading label="Loading services…" />}
      {isError && <LoadError error={error} what="services" />}
      {services && services.length === 0 && (
        <Empty>
          <p>No services are registered yet. Register one to start tracking its version and health.</p>
        </Empty>
      )}
      {services && services.length > 0 && <ServiceTable services={services} />}
    </>
  )
}
