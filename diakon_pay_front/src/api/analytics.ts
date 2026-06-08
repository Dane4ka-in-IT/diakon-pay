import apiClient from './client'
import type { AnalyticsTotals, AnalyticsByCategory, AnalyticsTotalBalance } from '@/types/analytics'

export const analyticsApi = {
  getTotals(period: string) {
    return apiClient.get<AnalyticsTotals>('/api/analytics/totals', { params: { period } })
  },

  getByCategory(period: string) {
    return apiClient.get<AnalyticsByCategory[]>('/api/analytics/by-category', { params: { period } })
  },

  getTotalBalance() {
    return apiClient.get<AnalyticsTotalBalance>('/api/analytics/total-balance')
  },
}
