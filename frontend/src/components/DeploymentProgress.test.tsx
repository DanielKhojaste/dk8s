import { screen, waitFor } from '@testing-library/react'
import { describe, expect, it } from 'vitest'

import { mockFetchRoutes, renderWithProviders } from '../test/utils'
import { DeploymentProgress } from './DeploymentProgress'

const pending = {
  id: 4,
  serviceId: 1,
  version: '1.7.0',
  previousVersion: '1.5.0',
  status: 'PENDING',
  failureReason: null,
  requestedAt: new Date().toISOString(),
  completedAt: null,
}

describe('DeploymentProgress', () => {
  it('says no worker is connected when the worker queue has 0 consumers', async () => {
    mockFetchRoutes({
      '/api/services/1/deployments': [pending],
      '/api/messaging/queues': {
        brokerReachable: true,
        checkedAt: new Date().toISOString(),
        queues: [{ name: 'dk8s.worker.deployment-requests', consumers: 0, messagesWaiting: 1 }],
      },
    })

    renderWithProviders(<DeploymentProgress serviceId={1} />)

    expect(await screen.findByText(/No worker is connected to the deployment queue \(1 request waiting\)/)).toBeTruthy()
  })

  it('shows nothing when no deployment is running', async () => {
    const fetchMock = mockFetchRoutes({ '/api/services/1/deployments': [{ ...pending, status: 'SUCCEEDED' }] })

    const { container } = renderWithProviders(<DeploymentProgress serviceId={1} />)

    await waitFor(() => expect(fetchMock).toHaveBeenCalledWith('/api/services/1/deployments', expect.anything()))
    await new Promise((resolve) => setTimeout(resolve, 50)) // let React render the fetched data
    expect(container.textContent).toBe('')
    expect(fetchMock.mock.calls.map(([url]) => url)).not.toContain('/api/messaging/queues')
  })
})
