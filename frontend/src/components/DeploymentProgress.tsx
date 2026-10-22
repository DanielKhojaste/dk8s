import { isActive, useDeployments } from '../api/deployments'
import { useMessagingStatus } from '../api/messaging'
import { WORKER_QUEUE } from '../types/messaging'
import { formatElapsed, useNow } from './useNow'

/**
 * Explains what an unfinished deployment is waiting for, using facts from the broker
 * (is any worker connected to the request queue?) rather than guesses.
 */
export function DeploymentProgress({ serviceId }: { serviceId: number }) {
  const { data: deployments } = useDeployments(serviceId)
  const active = deployments?.find(isActive)
  const { data: messaging } = useMessagingStatus({ enabled: Boolean(active) })
  const now = useNow(Boolean(active))

  if (!active) return null

  const waited = formatElapsed((now - Date.parse(active.requestedAt)) / 1000)

  if (active.status === 'IN_PROGRESS') {
    return (
      <div className="notice notice--progress" role="status">
        <p className="notice__title">The worker is deploying {active.version}… ({waited})</p>
      </div>
    )
  }

  const workerQueue = messaging?.queues.find((queue) => queue.name === WORKER_QUEUE)

  if (messaging && !messaging.brokerReachable) {
    return (
      <div className="notice notice--warning" role="status">
        <p className="notice__title">Waiting for a worker ({waited})</p>
        <p>DK8s couldn't check the message broker, so it can't tell whether a worker is connected.</p>
      </div>
    )
  }

  if (workerQueue && workerQueue.consumers === 0) {
    return (
      <div className="notice notice--warning" role="status">
        <p className="notice__title">Waiting for a worker ({waited})</p>
        <p>
          No worker is connected to the deployment queue ({workerQueue.messagesWaiting}{' '}
          {workerQueue.messagesWaiting === 1 ? 'request' : 'requests'} waiting). The request is stored safely in
          Solace and will run as soon as a worker starts.
        </p>
      </div>
    )
  }

  return (
    <div className="notice notice--progress" role="status">
      <p className="notice__title">Waiting for a worker to pick up {active.version}… ({waited})</p>
    </div>
  )
}
