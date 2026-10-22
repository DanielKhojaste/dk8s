import { Link } from 'react-router'

import type { ManagedService } from '../types/managedService'
import { DateTime } from './DateTime'
import { StatusBadge } from './StatusBadge'

export function ServiceTable({ services }: { services: ManagedService[] }) {
  return (
    <table className="table">
      <thead>
        <tr>
          <th scope="col">Name</th>
          <th scope="col">Status</th>
          <th scope="col">Version</th>
          <th scope="col">Description</th>
          <th scope="col">Registered</th>
        </tr>
      </thead>
      <tbody>
        {services.map((service) => (
          <tr key={service.id}>
            <td data-label="Name">
              <Link to={`/services/${service.id}`} className="code-link">
                {service.name}
              </Link>
            </td>
            <td data-label="Status">
              <StatusBadge status={service.status} />
            </td>
            <td data-label="Version" className="code">
              {service.version}
            </td>
            <td data-label="Description" className="table__description">
              {service.description ?? <span className="muted">No description</span>}
            </td>
            <td data-label="Registered">
              <DateTime value={service.createdAt} />
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  )
}
