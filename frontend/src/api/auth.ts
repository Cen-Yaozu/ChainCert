import { request } from '@/utils/request'
import type { LoginRequest, LoginResponse, RefreshTokenRequest } from '@/types'

/**
 * 认证相关 API
 */
export const authApi = {
  /**
   * 登录
   */
  login(data: LoginRequest): Promise<LoginResponse> {
    return request.post('/auth/login', data)
  },

  /**
   * 获取验证码
   */
  getCaptcha(): Promise<{ key: string; image: string }> {
    return request.get('/auth/captcha')
  },

  /**
   * 刷新 Token
   */
  refreshToken(data: RefreshTokenRequest): Promise<LoginResponse> {
    return request.post('/auth/refresh', data)
  },

  /**
   * 登出
   */
  logout(refreshToken: string): Promise<void> {
    return request.post('/auth/logout', { refreshToken })
  },
}