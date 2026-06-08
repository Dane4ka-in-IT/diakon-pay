import apiClient from './client'
import type { UserProfile, UserUpdateRequest } from '@/types/user'

export const usersApi = {
  getMe() {
    return apiClient.get<UserProfile>('/api/users/me')
  },

  updateProfile(data: UserUpdateRequest) {
    return apiClient.put<{ message: string; user: { id: number; name: string; email: string; avatarUrl: string | null } }>(
      '/api/users/me',
      data
    )
  },

  requestEmailChange(newEmail: string) {
    return apiClient.post<number>('/api/users/email-change/request', { newEmail })
  },

  confirmEmailChange(newEmail: string, code: number) {
    return apiClient.put<{ message: string; newEmail: string }>(
      '/api/users/email-change/confirm',
      { newEmail, code }
    )
  },
}
