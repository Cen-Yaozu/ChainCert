/**
 * API 响应结构
 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
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
  id: number
  name: string
  code: string
  description?: string
  approverIds: number[]
  enabled: boolean
  createdAt: string
}

/**
 * 学院请求
 */
export interface CollegeRequest {
  id?: number
  name: string
  code: string
  description?: string
}

/**
 * 学院响应
 */
export interface CollegeResponse {
  id: number
  name: string
  code: string
  description?: string
  adminId?: number
  adminName?: string
  createdAt: string
  updatedAt?: string
}

/**
 * 专业信息
 */
export interface Major {
  id: number
  collegeId: number
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
  id?: number
  collegeId?: number
  name: string
  code: string
  description?: string
}

/**
 * 专业响应
 */
export interface MajorResponse {
  id: number
  collegeId: number
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
  id: number
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
  id?: number
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
  id: number
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
  id: number
  userId: number
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
  id: number
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