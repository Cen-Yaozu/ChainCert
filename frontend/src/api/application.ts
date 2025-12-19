import { request } from '@/utils/request'
import type {
  Application,
  ApplicationRequest,
  ApplicationListItem,
  ApplicationQuery,
  PageResponse,
} from '@/types'

/**
 * 申请相关 API
 */
export const applicationApi = {
  /**
   * 创建申请
   */
  create(data: ApplicationRequest): Promise<Application> {
    const formData = new FormData()
    formData.append('certificateType', data.certificateType)
    formData.append('reason', data.reason)
    data.proofDocuments.forEach(file => {
      formData.append('proofDocuments', file)
    })
    return request.upload('/applications', formData)
  },

  /**
   * 获取申请列表
   */
  getList(params: ApplicationQuery): Promise<PageResponse<ApplicationListItem>> {
    return request.get('/applications', { params })
  },

  /**
   * 获取申请详情
   */
  getDetail(id: number): Promise<Application> {
    return request.get(`/applications/${id}`)
  },

  /**
   * 取消申请
   */
  cancel(id: number): Promise<void> {
    return request.delete(`/applications/${id}`)
  },

  /**
   * 获取我的申请列表
   */
  getMyApplications(params: ApplicationQuery): Promise<PageResponse<ApplicationListItem>> {
    return request.get('/applications/my', { params })
  },
}