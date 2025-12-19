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
  id: number
  studentId: string
  studentName: string
  collegeName: string
  majorName: string
  certificateType: string
  reason: string
  proofDocuments: ProofDocument[]
  status: ApplicationStatus
  statusName: string
  createdAt: string
  updatedAt: string
}

/**
 * 申请创建请求
 */
export interface ApplicationRequest {
  certificateType: string
  reason: string
  proofDocuments: File[]
}

/**
 * 申请列表项
 */
export interface ApplicationListItem {
  id: number
  studentId: string
  studentName: string
  collegeName: string
  majorName: string
  certificateType: string
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