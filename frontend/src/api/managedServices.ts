import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import type { CreateManagedServiceRequest, ManagedService } from '../types/managedService'
import { apiRequest } from './client'

// Query keys identify cached data. Everything about managed services lives under ['services'],
// so invalidating ['services'] refreshes both the list and any cached detail pages.
export const serviceKeys = {
  all: ['services'] as const,
  detail: (id: number) => ['services', id] as const,
}

export function fetchServices(): Promise<ManagedService[]> {
  return apiRequest<ManagedService[]>('/api/services')
}

export function fetchService(id: number): Promise<ManagedService> {
  return apiRequest<ManagedService>(`/api/services/${id}`)
}

export function createService(request: CreateManagedServiceRequest): Promise<ManagedService> {
  return apiRequest<ManagedService>('/api/services', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  })
}

export function useServices() {
  // Statuses change when the worker finishes a deployment, so keep the list fresh.
  return useQuery({ queryKey: serviceKeys.all, queryFn: fetchServices, refetchInterval: 5000 })
}

export function useService(id: number | null) {
  return useQuery({
    queryKey: serviceKeys.detail(id ?? -1),
    queryFn: () => fetchService(id as number),
    enabled: id !== null,
  })
}

export function useCreateService() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: createService,
    onSuccess: (created) => {
      // Seed the detail cache so the new service's page renders instantly,
      // and mark the list as stale so it refetches with the new entry.
      queryClient.setQueryData(serviceKeys.detail(created.id), created)
      return queryClient.invalidateQueries({ queryKey: serviceKeys.all, exact: true })
    },
  })
}
