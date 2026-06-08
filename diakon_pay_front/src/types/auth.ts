export interface AuthResponse {
  userId: number
  name: string
  email: string
  avatarUrl: string | null
  accessToken: string
  refreshToken: string
}

export interface LoginRequest {
  email: string
  password: string
}

/** Registration uses raw User entity fields — userEmail/userPassword (Java field names) */
export interface RegisterRequest {
  name: string
  userEmail: string
  userPassword: string
}

export interface RefreshRequest {
  refreshToken: string
}
