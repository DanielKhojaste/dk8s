import { useQuery } from '@tanstack/react-query'

import type { ServiceEvent } from '../types/serviceEvent'
import { apiRequest } from './client'
import { serviceKeys } from './managedServices'

const LIVE_REFRESH_MS = 3000

export const eventKeys = {
  recent: ['events'] as const,
  forService: (serviceId: number) => [...serviceKeys.detail(serviceId), 'events'] as const,
}

export function useRecentEvents(limit = 10) {
  return useQuery({
    queryKey: [...eventKeys.recent, limit],
    queryFn: () => apiRequest<ServiceEvent[]>(`/api/events?limit=${limit}`),
    // The feed changes whenever the worker reports progress, so keep it fresh.
    refetchInterval: LIVE_REFRESH_MS,
  })
}

export function useServiceEvents(serviceId: number) {
  return useQuery({
    queryKey: eventKeys.forService(serviceId),
    queryFn: () => apiRequest<ServiceEvent[]>(`/api/services/${serviceId}/events`),
    refetchInterval: LIVE_REFRESH_MS,
  })
}
