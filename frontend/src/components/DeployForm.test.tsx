import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'

import { mockFetchResponse, renderWithProviders } from '../test/utils'
import { DeployForm } from './DeployForm'

describe('DeployForm', () => {
  it('sends the version to POST /api/services/{id}/deployments', async () => {
    const fetchMock = mockFetchResponse(202, { id: 1, serviceId: 3, version: '1.5.0', status: 'PENDING' })
    renderWithProviders(<DeployForm serviceId={3} currentVersion="1.4.2" />)

    await userEvent.type(screen.getByLabelText('Version to deploy'), ' 1.5.0 ')
    await userEvent.click(screen.getByRole('button', { name: 'Deploy' }))

    await screen.findByRole('button', { name: 'Deploy' })
    const [url, init] = fetchMock.mock.calls[0]
    expect(url).toBe('/api/services/3/deployments')
    expect(init.method).toBe('POST')
    expect(JSON.parse(init.body)).toEqual({ version: '1.5.0' })
  })

  it('explains a 409 when a deployment is already in progress', async () => {
    mockFetchResponse(409, { status: 409, title: 'Deployment already in progress' })
    renderWithProviders(<DeployForm serviceId={3} currentVersion="1.4.2" />)

    await userEvent.type(screen.getByLabelText('Version to deploy'), '1.5.0')
    await userEvent.click(screen.getByRole('button', { name: 'Deploy' }))

    expect(await screen.findByText(/already in progress/)).toBeTruthy()
  })

  it('shows the broker message when Solace is down (503)', async () => {
    mockFetchResponse(503, {
      status: 503,
      title: 'Message broker unavailable',
      detail: 'Solace PubSub+ could not be reached, so nothing was saved.',
    })
    renderWithProviders(<DeployForm serviceId={3} currentVersion="1.4.2" />)

    await userEvent.type(screen.getByLabelText('Version to deploy'), '1.5.0')
    await userEvent.click(screen.getByRole('button', { name: 'Deploy' }))

    expect(await screen.findByText(/Solace PubSub\+ could not be reached/)).toBeTruthy()
  })
})
