import { screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'

import { mockFetchResponse, renderWithProviders } from '../test/utils'
import type { ManagedService } from '../types/managedService'
import { ServicesPage } from './ServicesPage'

const paymentsApi: ManagedService = {
  id: 1,
  name: 'payments-api',
  description: 'Handles card payments',
  version: '1.4.2',
  status: 'HEALTHY',
  createdAt: '2026-10-20T23:47:28.224502Z',
  updatedAt: '2026-10-20T23:47:28.224502Z',
}

describe('ServicesPage', () => {
  it('lists services returned by GET /api/services', async () => {
    const fetchMock = mockFetchResponse(200, [paymentsApi])

    renderWithProviders(<ServicesPage />)

    const link = await screen.findByRole('link', { name: 'payments-api' })
    expect(link.getAttribute('href')).toBe('/services/1')
    expect(screen.getByText('Healthy')).toBeTruthy()
    expect(screen.getByText('1.4.2')).toBeTruthy()
    expect(screen.getByText('Handles card payments')).toBeTruthy()
    expect(fetchMock).toHaveBeenCalledWith('/api/services', expect.anything())
  })

  it('shows an empty state when no services are registered', async () => {
    mockFetchResponse(200, [])

    renderWithProviders(<ServicesPage />)

    expect(await screen.findByText(/No services are registered yet/)).toBeTruthy()
  })

  it('explains how to recover when the API fails', async () => {
    mockFetchResponse(500, { status: 500, title: 'Internal Server Error' })

    renderWithProviders(<ServicesPage />)

    const alert = await screen.findByRole('alert')
    expect(alert.textContent).toContain("Couldn't load services.")
    expect(alert.textContent).toContain('control-api is running')
  })
})
