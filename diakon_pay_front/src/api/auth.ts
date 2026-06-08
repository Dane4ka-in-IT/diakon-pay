import apiClient from './client'
import type { AuthResponse, LoginRequest, RegisterRequest } from '@/types/auth'

export const authApi = {
  login(data: LoginRequest) {
    return apiClient.post<AuthResponse>('/api/auth/login', data)
  },

  refresh(refreshToken: string) {
    return apiClient.post<AuthResponse>('/api/auth/refresh', { refreshToken })
  },

  logout() {
    return apiClient.post<{ message: string }>('/api/auth/logout')
  },

  register(data: RegisterRequest) {
    return apiClient.post<{ message: string }>('/api/users/register', data)
  },

  requestPasswordRecovery(email: string) {
    return apiClient.post<number>('/api/auth/password-recovery/request', { email })
  },

  confirmPasswordRecovery(data: { email: string; newPassword: string; code: number }) {
    return apiClient.put<{ message: string }>('/api/auth/password-recovery/confirm', data)
  },
}
