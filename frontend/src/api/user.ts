import { request } from '@/utils/request'
import type {
  User,
  UserRequest,
  UserListVO,
  UserResponse,
  PasswordChangeRequest,
  PasswordResetRequest,
  PageResult
} from '@/types'

/**
 * 用户管理相关 API
 */
export const userApi = {
  /**
   * 创建用户
   */
  createUser(data: UserRequest) {
    return request.post<UserResponse>('/users', data)
  },

  /**
   * 更新用户
   */
  updateUser(id: number, data: UserRequest) {
    return request.put<UserResponse>(`/users/${id}`, data)
  },

  /**
   * 删除用户
   */
  deleteUser(id: number) {
    return request.delete<void>(`/users/${id}`)
  },

  /**
   * 获取用户列表
   */
  getUserList(params: {
    page: number
    size: number
    username?: string
    realName?: string
    role?: string
    enabled?: boolean
  }) {
    return request.get<PageResult<UserListVO>>('/users', { params })
  },

  /**
   * 获取用户详情
   */
  getUserDetail(id: number) {
    return request.get<UserResponse>(`/users/${id}`)
  },

  /**
   * 获取当前用户信息
   */
  getCurrentUser() {
    return request.get<UserResponse>('/users/current')
  },

  /**
   * 重置密码
   */
  resetPassword(id: number, data: PasswordResetRequest) {
    return request.put<void>(`/users/${id}/reset-password`, data)
  },

  /**
   * 切换用户状态
   */
  toggleUserStatus(id: number) {
    return request.put<void>(`/users/${id}/status`)
  },

  /**
   * 批量导入用户
   */
  batchImport(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post<{ successCount: number; failCount: number; errors: string[] }>(
      '/users/batch-import',
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      }
    )
  },
}

/**
 * 个人中心相关 API
 */
export const profileApi = {
  /**
   * 获取个人信息
   */
  getProfile(): Promise<User> {
    return request.get('/profile')
  },

  /**
   * 更新个人信息
   */
  updateProfile(data: Partial<User>): Promise<User> {
    return request.put('/profile', data)
  },

  /**
   * 修改密码
   */
  changePassword(data: PasswordChangeRequest): Promise<void> {
    return request.put('/profile/password', data)
  },

  /**
   * 上传头像
   */
  uploadAvatar(file: File): Promise<{ avatarUrl: string }> {
    const formData = new FormData()
    formData.append('avatar', file)
    return request.upload('/profile/avatar', formData)
  },
}