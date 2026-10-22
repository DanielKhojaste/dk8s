import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import type { CreateDeploymentRequest, Deployment } from '../types/deployment'
import { apiRequest } from './client'
import { serviceKeys } from './managedServices'

// Nested under the service's key, so invalidating ['services'] also refreshes deployment history.
export const deploymentKeys = {
  forService: (serviceId: number) => [...serviceKeys.detail(serviceId), 'deployments'] as const,
}

export function fetchDeployments(serviceId: number): Promise<Deployment[]> {
  return apiRequest<Deployment[]>(`/api/services/${serviceId}/deployments`)
}

export function createDeployment(serviceId: number, request: CreateDeploymentRequest): Promise<Deployment> {
  return apiRequest<Deployment>(`/api/services/${serviceId}/deployments`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  })
}

export function useDeployments(serviceId: number) {
  return useQuery({
    queryKey: deploymentKeys.forService(serviceId),
    queryFn: () => fetchDeployments(serviceId),
  })
}

export function useCreateDeployment(serviceId: number) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: CreateDeploymentRequest) => createDeployment(serviceId, request),
    // A deployment changes the service (version, status) and its history, and the
    // dashboard/list show versions too, so refresh everything under ['services'].
    onSuccess: () => queryClient.invalidateQueries({ queryKey: serviceKeys.all }),
  })
}
