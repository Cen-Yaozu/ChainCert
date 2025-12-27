import { request } from '@/utils/request'
import type {
  User,
  UserRequest,
  UserListVO,
  UserResponse,
  PasswordChangeRequest,
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
  updateUser(id: string, data: UserRequest) {
    return request.put<UserResponse>(`/users/${id}`, data)
  },

  /**
   * 删除用户
   */
  deleteUser(id: string) {
    return request.delete<void>(`/users/${id}`)
  },

  /**
   * 获取用户列表
   * 后端使用 keyword 参数进行搜索，而不是 username/realName
   */
  getUserList(params: {
    page: number
    size: number
    keyword?: string
    role?: string
    collegeId?: string
  }) {
    return request.get<PageResult<UserListVO>>('/users', { params })
  },

  /**
   * 获取用户详情
   */
  getUserDetail(id: string) {
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
   * 后端使用 query param 传递 newPassword
   */
  resetPassword(id: string, newPassword: string) {
    return request.put<void>(`/users/${id}/reset-password`, null, {
      params: { newPassword }
    })
  },

  /**
   * 切换用户状态
   * 后端需要 enabled 参数
   */
  toggleUserStatus(id: string, enabled: boolean) {
    return request.put<void>(`/users/${id}/status`, null, {
      params: { enabled }
    })
  },

  /**
   * 批量导入用户
   * 后端接收 JSON 数组，不是 FormData
   */
  batchImport(users: UserRequest[]) {
    return request.post<void>('/users/batch-import', users)
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
   * 更新头像
   * 后端使用 PUT + query param，不是 POST + FormData
   */
  updateAvatar(avatarUrl: string): Promise<void> {
    return request.put('/profile/avatar', null, {
      params: { avatarUrl }
    })
  },
}