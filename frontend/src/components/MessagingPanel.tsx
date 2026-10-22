import { useMessagingStatus } from '../api/messaging'

/** Dashboard panel: each DK8s queue on the Solace broker with its consumers and waiting messages. */
export function MessagingPanel() {
  const { data, isError } = useMessagingStatus()

  if (isError || (data && !data.brokerReachable)) {
    return (
      <div className="notice notice--warning" role="status">
        <p className="notice__title">Solace broker not reachable</p>
        <p>DK8s couldn't read queue statistics. Check that the broker is running (docker compose ps).</p>
      </div>
    )
  }
  if (!data) return null

  return (
    <table className="table">
      <thead>
        <tr>
          <th scope="col">Queue</th>
          <th scope="col">Consumers</th>
          <th scope="col">Waiting messages</th>
        </tr>
      </thead>
      <tbody>
        {data.queues.map((queue) => (
          <tr key={queue.name}>
            <td data-label="Queue" className="code">
              {queue.name}
            </td>
            <td data-label="Consumers">
              {queue.consumers === 0 ? (
                <span className="status status--degraded">
                  <span className="status__mark" aria-hidden="true" />0 (nothing is listening)
                </span>
              ) : (
                queue.consumers
              )}
            </td>
            <td data-label="Waiting messages">{queue.messagesWaiting}</td>
          </tr>
        ))}
      </tbody>
    </table>
  )
}
