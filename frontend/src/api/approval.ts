import { request } from '@/utils/request'
import type {
  ApprovalRequest,
  ApprovalResponse,
  ApprovalHistoryItem,
  PendingApproval,
  PageResponse,
  PageQuery,
} from '@/types'

/**
 * 审批相关 API
 * 注意: 后端角色是 COLLEGE_TEACHER/SCHOOL_TEACHER，不是 COLLEGE_ADMIN/SCHOOL_ADMIN
 */
export const approvalApi = {
  /**
   * 审批申请
   */
  approve(applicationId: string, data: ApprovalRequest): Promise<ApprovalResponse> {
    return request.post(`/approvals/${applicationId}`, data)
  },

  /**
   * 获取待审批列表
   * 支持 certificateType 和 keyword 参数
   */
  getPendingList(params?: PageQuery & { certificateType?: string; keyword?: string }): Promise<PageResponse<PendingApproval>> {
    return request.get('/approvals/pending', { params })
  },

  /**
   * 获取我的审批记录
   * 支持 applicationId, startDate, endDate 参数
   */
  getMyApprovals(params?: PageQuery & { applicationId?: string; startDate?: string; endDate?: string }): Promise<PageResponse<ApprovalHistoryItem>> {
    return request.get('/approvals/my', { params })
  },

  /**
   * 获取申请的审批历史
   * 后端返回分页结果，不是数组
   */
  getApprovalHistory(applicationId: string, params?: PageQuery): Promise<PageResponse<ApprovalHistoryItem>> {
    return request.get(`/approvals/history/${applicationId}`, { params })
  },

  /**
   * 获取所有审批记录（仅管理员）
   */
  getAllApprovals(params?: PageQuery & { approverId?: string; applicationId?: string; startDate?: string; endDate?: string }): Promise<PageResponse<ApprovalHistoryItem>> {
    return request.get('/approvals/all', { params })
  },
}