import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import type { AuthResponse, LoginRequest, RegisterRequest } from '@/types/auth'

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref<string | null>(localStorage.getItem('accessToken'))
  const refreshToken = ref<string | null>(localStorage.getItem('refreshToken'))
  const userId = ref<number | null>(
    localStorage.getItem('userId') ? Number(localStorage.getItem('userId')) : null
  )
  const userName = ref<string | null>(localStorage.getItem('userName'))
  const userEmail = ref<string | null>(localStorage.getItem('userEmail'))
  const avatarUrl = ref<string | null>(localStorage.getItem('avatarUrl'))

  const isAuthenticated = computed(() => !!accessToken.value)

  function persist(data: AuthResponse) {
    accessToken.value = data.accessToken
    refreshToken.value = data.refreshToken
    userId.value = data.userId
    userName.value = data.name
    userEmail.value = data.email
    avatarUrl.value = data.avatarUrl

    localStorage.setItem('accessToken', data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)
    localStorage.setItem('userId', String(data.userId))
    localStorage.setItem('userName', data.name)
    localStorage.setItem('userEmail', data.email)
    if (data.avatarUrl) localStorage.setItem('avatarUrl', data.avatarUrl)
    else localStorage.removeItem('avatarUrl')
  }

  function clear() {
    accessToken.value = null
    refreshToken.value = null
    userId.value = null
    userName.value = null
    userEmail.value = null
    avatarUrl.value = null

    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userId')
    localStorage.removeItem('userName')
    localStorage.removeItem('userEmail')
    localStorage.removeItem('avatarUrl')
  }

  function patchProfile(name: string, avatar: string | null) {
    userName.value = name
    avatarUrl.value = avatar
    localStorage.setItem('userName', name)
    if (avatar) localStorage.setItem('avatarUrl', avatar)
    else localStorage.removeItem('avatarUrl')
  }

  function patchEmail(email: string) {
    userEmail.value = email
    localStorage.setItem('userEmail', email)
  }

  async function login(request: LoginRequest) {
    const { data } = await authApi.login(request)
    persist(data)
  }

  async function register(request: RegisterRequest) {
    await authApi.register(request)
  }

  async function logout() {
    try {
      await authApi.logout()
    } finally {
      clear()
    }
  }

  return {
    accessToken,
    refreshToken,
    userId,
    userName,
    userEmail,
    avatarUrl,
    isAuthenticated,
    login,
    register,
    logout,
    patchProfile,
    patchEmail,
    clear,
  }
})
