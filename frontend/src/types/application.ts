/**
 * 申请状态枚举
 */
export enum ApplicationStatus {
  PENDING = 'PENDING',
  COLLEGE_APPROVED = 'COLLEGE_APPROVED',
  COLLEGE_REJECTED = 'COLLEGE_REJECTED',
  SCHOOL_APPROVED = 'SCHOOL_APPROVED',
  SCHOOL_REJECTED = 'SCHOOL_REJECTED',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
}

/**
 * 证明文件
 */
export interface ProofDocument {
  fileName: string
  fileUrl: string
  ipfsCid: string
  uploadTime: string
}

/**
 * 申请信息
 */
export interface Application {
  id: string
  studentId: string
  studentName: string
  collegeId?: string
  collegeName: string
  majorId?: string
  majorName: string
  title: string
  description?: string
  certificateType?: string
  reason?: string
  proofFiles?: string[]
  proofDocuments?: ProofDocument[]
  status: ApplicationStatus
  statusName: string
  createdAt: string
  updatedAt: string
}

/**
 * 申请创建请求
 * 后端 ApplicationController.create 接收的字段
 */
export interface ApplicationRequest {
  title: string
  certificateType: string
  collegeId: string
  majorId?: string
  description?: string
  proofFiles?: string[]
}

/**
 * 申请列表项
 */
export interface ApplicationListItem {
  id: string
  studentId: string
  studentName: string
  collegeName: string
  majorName: string
  title: string
  certificateType?: string
  status: ApplicationStatus
  statusName: string
  createdAt: string
}

/**
 * 申请查询参数
 */
export interface ApplicationQuery {
  status?: ApplicationStatus
  certificateType?: string
  keyword?: string
  page?: number
  size?: number
}