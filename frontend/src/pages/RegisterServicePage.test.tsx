import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Route, Routes } from 'react-router'
import { describe, expect, it } from 'vitest'

import { mockFetchResponse, renderWithProviders } from '../test/utils'
import { RegisterServicePage } from './RegisterServicePage'

function renderRegisterPage() {
  renderWithProviders(
    <Routes>
      <Route path="/services/new" element={<RegisterServicePage />} />
      <Route path="/services/:id" element={<p>Service detail page</p>} />
    </Routes>,
    { route: '/services/new' },
  )
}

async function fillAndSubmit(name: string, version: string) {
  const user = userEvent.setup()
  await user.type(screen.getByLabelText('Name'), name)
  await user.type(screen.getByLabelText('Version'), version)
  await user.click(screen.getByRole('button', { name: 'Register service' }))
}

describe('RegisterServicePage', () => {
  it('sends the form to POST /api/services and opens the new service', async () => {
    const fetchMock = mockFetchResponse(201, {
      id: 7,
      name: 'payments-api',
      description: null,
      version: '1.4.2',
      status: 'UNKNOWN',
      createdAt: '2026-10-21T00:00:00Z',
      updatedAt: '2026-10-21T00:00:00Z',
    })
    renderRegisterPage()

    await fillAndSubmit('payments-api', '1.4.2')

    expect(await screen.findByText('Service detail page')).toBeTruthy()
    const [url, init] = fetchMock.mock.calls[0]
    expect(url).toBe('/api/services')
    expect(init.method).toBe('POST')
    expect(JSON.parse(init.body)).toEqual({ name: 'payments-api', version: '1.4.2' })
  })

  it('shows backend validation errors next to the fields', async () => {
    mockFetchResponse(400, {
      status: 400,
      title: 'Validation failed',
      errors: { name: 'must be lowercase letters, digits, and hyphens' },
    })
    renderRegisterPage()

    await fillAndSubmit('Payments API', '1.4.2')

    expect(await screen.findByText('must be lowercase letters, digits, and hyphens')).toBeTruthy()
    expect(screen.getByLabelText('Name').getAttribute('aria-invalid')).toBe('true')
  })

  it('explains a duplicate name conflict', async () => {
    mockFetchResponse(409, { status: 409, title: 'Duplicate managed service name' })
    renderRegisterPage()

    await fillAndSubmit('payments-api', '1.4.2')

    expect(await screen.findByText('A service named “payments-api” is already registered.')).toBeTruthy()
  })
})
