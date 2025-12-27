import type { ApplicationStatus } from './application'

/**
 * 审批决定枚举
 */
export enum ApprovalDecision {
  APPROVE = 'APPROVE',
  REJECT = 'REJECT',
}

/**
 * 审批请求
 */
export interface ApprovalRequest {
  decision: ApprovalDecision
  comment?: string
}

/**
 * 审批记录
 */
export interface Approval {
  id: string
  applicationId: string
  approverId: string
  approverName: string
  approverRole: string
  decision: ApprovalDecision
  comment?: string
  digitalSignature: string
  createdAt: string
}

/**
 * 审批响应（后端返回格式）
 */
export interface ApprovalResponse {
  id: string
  applicationId: string
  approverId: string
  approverName: string
  approverRole: string
  decision: string
  comment?: string
  digitalSignature?: string
  createdAt: string
  updatedAt?: string
}

/**
 * 审批历史项
 */
export interface ApprovalHistoryItem {
  id: string
  approverName: string
  approverRole: string
  decision: ApprovalDecision | string
  decisionName: string
  comment?: string
  createdAt: string
}

/**
 * 待审批申请
 */
export interface PendingApproval {
  id: string
  studentId: string
  studentName: string
  collegeName: string
  majorName: string
  title?: string
  certificateType?: string
  reason?: string
  description?: string
  status: ApplicationStatus
  statusName: string
  createdAt: string
}