import { useQuery } from '@tanstack/react-query'

import type { MessagingStatus } from '../types/messaging'
import { apiRequest } from './client'

export function useMessagingStatus({ enabled = true } = {}) {
  return useQuery({
    queryKey: ['messaging', 'queues'],
    queryFn: () => apiRequest<MessagingStatus>('/api/messaging/queues'),
    enabled,
    refetchInterval: 3000,
  })
}
