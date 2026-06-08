import type { AccountResponse } from './account'

export interface UserProfile {
  id: number
  name: string
  email: string
  avatarUrl: string | null
  accounts: AccountResponse[]
}

export interface UserUpdateRequest {
  name: string
  avatarUrl?: string
}

export interface PasswordRecoveryRequest {
  email: string
}

export interface PasswordUpdateRequest {
  email: string
  newPassword: string
  code: number
}
