import { useDeployments } from '../api/deployments'
import { DateTime } from './DateTime'
import { DeploymentStatusBadge } from './DeploymentStatusBadge'
import { Empty, Loading, LoadError } from './QueryState'

export function DeploymentHistory({ serviceId }: { serviceId: number }) {
  const { data: deployments, isPending, isError, error } = useDeployments(serviceId)

  if (isPending) return <Loading label="Loading deployment history…" />
  if (isError) return <LoadError error={error} what="deployment history" />
  if (deployments.length === 0) {
    return (
      <Empty>
        <p>This service hasn't been deployed through DK8s yet.</p>
      </Empty>
    )
  }

  return (
    <table className="table">
      <thead>
        <tr>
          <th scope="col">Version</th>
          <th scope="col">Status</th>
          <th scope="col">Requested</th>
          <th scope="col">Completed</th>
        </tr>
      </thead>
      <tbody>
        {deployments.map((deployment) => (
          <tr key={deployment.id}>
            <td data-label="Version" className="code">
              <span>
                <span className="muted">{deployment.previousVersion} → </span>
                {deployment.version}
              </span>
            </td>
            <td data-label="Status">
              <DeploymentStatusBadge status={deployment.status} />
              {deployment.failureReason && <p className="table__note">{deployment.failureReason}</p>}
            </td>
            <td data-label="Requested">
              <DateTime value={deployment.requestedAt} />
            </td>
            <td data-label="Completed">
              {deployment.completedAt ? <DateTime value={deployment.completedAt} /> : <span className="muted">Not yet</span>}
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  )
}
