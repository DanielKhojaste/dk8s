import { screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'

import { renderWithProviders } from '../test/utils'
import type { ServiceEvent } from '../types/serviceEvent'
import { EventTimeline } from './EventTimeline'

const completed: ServiceEvent = {
  id: 3,
  serviceId: 1,
  serviceName: 'payments-api',
  deploymentId: 1,
  type: 'DEPLOYMENT_COMPLETED',
  summary: 'payments-api is now running 1.5.0',
  topic: 'dk8s/deployment/completed/payments-api',
  occurredAt: '2026-10-22T21:00:03Z',
}

describe('EventTimeline', () => {
  it('shows each event with its label, summary, Solace topic, and service link', () => {
    renderWithProviders(<EventTimeline events={[completed]} showService />)

    expect(screen.getByText('Deploy completed')).toBeTruthy()
    expect(screen.getByText('payments-api is now running 1.5.0')).toBeTruthy()
    expect(screen.getByText('dk8s/deployment/completed/payments-api')).toBeTruthy()
    expect(screen.getByRole('link', { name: 'payments-api' }).getAttribute('href')).toBe('/services/1')
  })
})
