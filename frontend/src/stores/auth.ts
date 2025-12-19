import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, LoginRequest, LoginResponse } from '@/types'
import { authApi } from '@/api/auth'

/**
 * 认证状态管理
 */
export const useAuthStore = defineStore('auth', () => {
  // 状态
  const user = ref<User | null>(null)
  const accessToken = ref<string>('')
  const refreshToken = ref<string>('')

  // 计算属性
  const isAuthenticated = computed(() => !!accessToken.value && !!user.value)
  const userRole = computed(() => user.value?.role)
  const userName = computed(() => user.value?.realName || user.value?.username)

  /**
   * 登录
   */
  const login = async (loginData: LoginRequest): Promise<void> => {
    const response = await authApi.login(loginData)
    setAuthData(response)
  }

  /**
   * 登出
   */
  const logout = async (): Promise<void> => {
    try {
      if (refreshToken.value) {
        await authApi.logout(refreshToken.value)
      }
    } finally {
      clearAuthData()
    }
  }

  /**
   * 刷新 Token
   */
  const refresh = async (): Promise<void> => {
    if (!refreshToken.value) {
      throw new Error('No refresh token available')
    }
    const response = await authApi.refreshToken({ refreshToken: refreshToken.value })
    setAuthData(response)
  }

  /**
   * 设置认证数据
   */
  const setAuthData = (data: LoginResponse): void => {
    user.value = data.user
    accessToken.value = data.accessToken
    refreshToken.value = data.refreshToken

    // 保存到 localStorage
    localStorage.setItem('user', JSON.stringify(data.user))
    localStorage.setItem('accessToken', data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)
  }

  /**
   * 清除认证数据
   */
  const clearAuthData = (): void => {
    user.value = null
    accessToken.value = ''
    refreshToken.value = ''

    // 清除 localStorage
    localStorage.removeItem('user')
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  }

  /**
   * 从 localStorage 恢复认证状态
   */
  const restoreAuth = (): void => {
    const storedUser = localStorage.getItem('user')
    const storedAccessToken = localStorage.getItem('accessToken')
    const storedRefreshToken = localStorage.getItem('refreshToken')

    if (storedUser && storedAccessToken && storedRefreshToken) {
      try {
        user.value = JSON.parse(storedUser)
        accessToken.value = storedAccessToken
        refreshToken.value = storedRefreshToken
      } catch (error) {
        console.error('Failed to restore auth state:', error)
        clearAuthData()
      }
    }
  }

  /**
   * 更新用户信息
   */
  const updateUser = (userData: Partial<User>): void => {
    if (user.value) {
      user.value = { ...user.value, ...userData }
      localStorage.setItem('user', JSON.stringify(user.value))
    }
  }

  // 初始化时恢复认证状态
  restoreAuth()

  return {
    // 状态
    user,
    accessToken,
    refreshToken,
    // 计算属性
    isAuthenticated,
    userRole,
    userName,
    // 方法
    login,
    logout,
    refresh,
    setAuthData,
    clearAuthData,
    updateUser,
  }
})