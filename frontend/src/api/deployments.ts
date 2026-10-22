import { useEffect, useRef } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'

import type { CreateDeploymentRequest, Deployment } from '../types/deployment'
import { apiRequest } from './client'
import { serviceKeys } from './managedServices'

// Nested under the service's key, so invalidating ['services'] also refreshes deployment history.
export const deploymentKeys = {
  forService: (serviceId: number) => [...serviceKeys.detail(serviceId), 'deployments'] as const,
}

export function isActive(deployment: Deployment): boolean {
  return deployment.status === 'PENDING' || deployment.status === 'IN_PROGRESS'
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

/**
 * Deployment history. While a deployment is PENDING or IN_PROGRESS, re-fetches every second
 * so the page shows the worker's progress. When it finishes, refreshes everything else about
 * the service too (version, status, events), because the worker changed those.
 */
export function useDeployments(serviceId: number) {
  const queryClient = useQueryClient()
  const query = useQuery({
    queryKey: deploymentKeys.forService(serviceId),
    queryFn: () => fetchDeployments(serviceId),
    refetchInterval: (q) => (q.state.data?.some(isActive) ? 1000 : false),
  })

  const deploying = query.data?.some(isActive) ?? false
  const wasDeploying = useRef(deploying) // remembers the previous value between renders

  useEffect(() => {
    if (wasDeploying.current && !deploying) {
      void queryClient.invalidateQueries({ queryKey: serviceKeys.all })
      void queryClient.invalidateQueries({ queryKey: ['events'] })
    }
    wasDeploying.current = deploying
  }, [deploying, queryClient])

  return query
}

export function useCreateDeployment(serviceId: number) {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: CreateDeploymentRequest) => createDeployment(serviceId, request),
    // The new PENDING deployment should appear (and start polling) right away.
    onSuccess: () => queryClient.invalidateQueries({ queryKey: serviceKeys.detail(serviceId) }),
  })
}
