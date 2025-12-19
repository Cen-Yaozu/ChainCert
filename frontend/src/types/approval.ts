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
  id: number
  applicationId: number
  approverId: number
  approverName: string
  approverRole: string
  decision: ApprovalDecision
  comment?: string
  digitalSignature: string
  createdAt: string
}

/**
 * 审批历史项
 */
export interface ApprovalHistoryItem {
  id: number
  approverName: string
  approverRole: string
  decision: ApprovalDecision
  decisionName: string
  comment?: string
  createdAt: string
}

/**
 * 待审批申请
 */
export interface PendingApproval {
  id: number
  studentId: string
  studentName: string
  collegeName: string
  majorName: string
  certificateType: string
  reason: string
  status: ApplicationStatus
  statusName: string
  createdAt: string
}