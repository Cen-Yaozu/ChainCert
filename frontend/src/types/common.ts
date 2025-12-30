/**
 * API 响应结构
 * 后端返回格式: { success: boolean, code: string, message: string, data: T, timestamp: string }
 */
export interface ApiResponse<T = any> {
  success: boolean
  code: string | number
  message: string
  data: T
  timestamp?: string
}

/**
 * 分页响应结构
 */
export interface PageResponse<T = any> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

/**
 * 分页结果（后端实际返回格式）
 */
export interface PageResult<T = any> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/**
 * 分页查询参数
 */
export interface PageQuery {
  page?: number
  size?: number
  sort?: string
}

/**
 * 选项接口
 */
export interface Option {
  label: string
  value: string | number
}

/**
 * 学院信息
 */
export interface College {
  id: string
  name: string
  code: string
  description?: string
  approverIds?: string[]
  enabled: boolean
  createdAt: string
}

/**
 * 学院请求
 */
export interface CollegeRequest {
  id?: string
  name: string
  code: string
  description?: string
}

/**
 * 学院响应
 */
export interface CollegeResponse {
  id: string
  name: string
  code: string
  description?: string
  adminId?: string
  adminName?: string
  createdAt: string
  updatedAt?: string
}

/**
 * 专业信息
 */
export interface Major {
  id: string
  collegeId: string
  collegeName: string
  name: string
  code: string
  description?: string
  enabled: boolean
  createdAt: string
}

/**
 * 专业请求
 */
export interface MajorRequest {
  id?: string
  collegeId?: string
  name: string
  code: string
  description?: string
}

/**
 * 专业响应
 */
export interface MajorResponse {
  id: string
  collegeId: string
  collegeName?: string
  name: string
  code: string
  description?: string
  createdAt: string
  updatedAt?: string
}

/**
 * 证书模板
 */
export interface CertificateTemplate {
  id: string
  name: string
  type: string
  description?: string
  fieldDefinitions: Record<string, any>
  isDefault: boolean
  enabled: boolean
  createdAt: string
}

/**
 * 模板请求
 */
export interface TemplateRequest {
  id?: string
  name: string
  type: string
  description?: string
  content: string
  fields?: string
  isDefault?: boolean
}

/**
 * 模板响应
 */
export interface TemplateResponse {
  id: string
  name: string
  type: string
  description?: string
  content: string
  fields?: any
  isDefault: boolean
  createdAt: string
  updatedAt?: string
}

/**
 * 系统日志
 */
export interface SystemLog {
  id: string
  userId: string
  username: string
  operation: string
  module: string
  method: string
  params?: string
  result?: string
  ip: string
  duration: number
  createdAt: string
}

/**
 * 系统日志VO
 */
export interface SystemLogVO {
  id: string
  username: string
  action: string
  module: string
  description: string
  ip: string
  userAgent?: string
  requestParams?: string
  responseData?: string
  createdAt: string
}

/**
 * 统计数据
 */
export interface Statistics {
  totalApplications: number
  pendingApplications: number
  approvedApplications: number
  rejectedApplications: number
  totalCertificates: number
  activeCertificates: number
  revokedCertificates: number
  totalUsers: number
  activeUsers: number
  collegeStats?: CollegeStatistics[]
}

/**
 * 学院统计数据
 */
export interface CollegeStatistics {
  collegeId: number
  collegeName: string
  applicationCount: number
  certificateCount: number
}