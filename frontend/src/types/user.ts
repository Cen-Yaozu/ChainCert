/**
 * 用户角色枚举
 * 与后端数据库 ENUM 保持一致：STUDENT, COLLEGE_ADMIN, SCHOOL_ADMIN, SYSTEM_ADMIN
 */
export enum UserRole {
  STUDENT = 'STUDENT',
  COLLEGE_ADMIN = 'COLLEGE_ADMIN',
  SCHOOL_ADMIN = 'SCHOOL_ADMIN',
  SYSTEM_ADMIN = 'SYSTEM_ADMIN',
}

/**
 * 兼容旧代码的别名
 * @deprecated 请使用 COLLEGE_ADMIN 和 SCHOOL_ADMIN
 */
export const COLLEGE_TEACHER = UserRole.COLLEGE_ADMIN
export const SCHOOL_TEACHER = UserRole.SCHOOL_ADMIN

/**
 * 用户信息接口
 */
export interface User {
  id: string
  username: string
  realName: string
  email: string
  phone?: string
  role: UserRole | string
  studentId?: string
  collegeId?: string
  collegeName?: string
  majorId?: string
  majorName?: string
  enabled: boolean
  createdAt: string
  updatedAt: string
}

/**
 * 登录请求
 */
export interface LoginRequest {
  username: string
  password: string
  captcha: string
  captchaKey: string
  rememberMe?: boolean
}

/**
 * 登录响应
 */
export interface LoginResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  user: User
}

/**
 * Token 刷新请求
 */
export interface RefreshTokenRequest {
  refreshToken: string
}

/**
 * 用户创建/更新请求
 */
export interface UserRequest {
  username: string
  password?: string
  realName: string
  email: string
  phone?: string
  role: UserRole | string
  studentId?: string
  collegeId?: string
  majorId?: string
  enabled?: boolean
}

/**
 * 密码修改请求
 */
export interface PasswordChangeRequest {
  oldPassword: string
  newPassword: string
}

/**
 * 用户列表项
 */
export interface UserListItem {
  id: string
  username: string
  realName: string
  email: string
  phone?: string
  role: UserRole | string
  roleName: string
  studentId?: string
  collegeName?: string
  majorName?: string
  enabled: boolean
  createdAt: string
}

/**
 * 用户列表VO（后端返回格式）
 */
export interface UserListVO {
  id: string
  username: string
  realName: string
  email: string
  phone?: string
  role: string
  studentId?: string
  collegeId?: string
  collegeName?: string
  majorId?: string
  majorName?: string
  enabled: boolean
  createdAt: string
}

/**
 * 用户响应
 */
export interface UserResponse {
  id: string
  username: string
  realName: string
  email: string
  phone?: string
  role: string
  studentId?: string
  collegeId?: string
  collegeName?: string
  majorId?: string
  majorName?: string
  enabled: boolean
  createdAt: string
  updatedAt?: string
}

/**
 * 密码重置请求
 */
export interface PasswordResetRequest {
  newPassword: string
}