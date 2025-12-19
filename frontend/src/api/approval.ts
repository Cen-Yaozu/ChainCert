import { request } from '@/utils/request'
import type {
  ApprovalRequest,
  Approval,
  ApprovalHistoryItem,
  PendingApproval,
  PageResponse,
  PageQuery,
} from '@/types'

/**
 * 审批相关 API
 */
export const approvalApi = {
  /**
   * 审批申请
   */
  approve(applicationId: number, data: ApprovalRequest): Promise<Approval> {
    return request.post(`/approvals/${applicationId}`, data)
  },

  /**
   * 获取待审批列表
   */
  getPendingList(params?: PageQuery): Promise<PageResponse<PendingApproval>> {
    return request.get('/approvals/pending', { params })
  },

  /**
   * 获取我的审批记录
   */
  getMyApprovals(params?: PageQuery): Promise<PageResponse<Approval>> {
    return request.get('/approvals/my', { params })
  },

  /**
   * 获取申请的审批历史
   */
  getApprovalHistory(applicationId: number): Promise<ApprovalHistoryItem[]> {
    return request.get(`/approvals/history/${applicationId}`)
  },
}