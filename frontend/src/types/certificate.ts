/**
 * 证书状态枚举
 */
export enum CertificateStatus {
  VALID = 'VALID',
  ACTIVE = 'ACTIVE',
  REVOKED = 'REVOKED',
  EXPIRED = 'EXPIRED',
}

/**
 * 证书信息
 */
export interface Certificate {
  id: string
  certificateNumber: string
  studentId: string
  studentName: string
  collegeId?: string
  collegeName: string
  majorId?: string
  majorName: string
  certificateType: string
  issueDate: string
  pdfUrl?: string
  ipfsCid?: string
  blockchainTxHash?: string
  status: CertificateStatus
  statusName: string
  createdAt: string
  updatedAt?: string
}

/**
 * 证书列表项
 */
export interface CertificateListItem {
  id: string
  certificateNumber: string
  studentId: string
  studentName: string
  collegeName: string
  certificateType: string
  issueDate: string
  status: CertificateStatus
  statusName: string
}

/**
 * 证书查询参数
 */
export interface CertificateQuery {
  status?: CertificateStatus
  certificateType?: string
  keyword?: string
  page?: number
  size?: number
}

/**
 * 证书核验请求
 */
export interface VerificationRequest {
  certificateNumber: string
}

/**
 * 证书核验结果
 */
export interface VerificationResult {
  valid: boolean
  certificate?: Certificate
  message: string
  verificationDetails: {
    databaseValid: boolean
    blockchainValid: boolean
    ipfsValid: boolean
  }
}