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
   * 后端字段: title, certificateType, collegeId, majorId, description, proofFiles
   */
  create(data: ApplicationRequest): Promise<Application> {
    const formData = new FormData()
    formData.append('title', data.title)
    formData.append('certificateType', data.certificateType)
    formData.append('collegeId', String(data.collegeId))
    if (data.majorId) {
      formData.append('majorId', String(data.majorId))
    }
    if (data.description) {
      formData.append('description', data.description)
    }
    if (data.proofFiles) {
      data.proofFiles.forEach(file => {
        formData.append('proofFiles', file)
      })
    }
    return request.upload('/applications', formData)
  },

  /**
   * 获取申请列表
   * 后端会根据用户角色自动过滤（学生只能看自己的申请）
   */
  getList(params: ApplicationQuery): Promise<PageResponse<ApplicationListItem>> {
    return request.get('/applications', { params })
  },

  /**
   * 获取申请详情
   */
  getDetail(id: string): Promise<Application> {
    return request.get(`/applications/${id}`)
  },

  /**
   * 取消申请
   */
  cancel(id: string): Promise<void> {
    return request.delete(`/applications/${id}`)
  },

  // 注意: 后端没有 /applications/my 接口
  // 学生调用 getList 时，后端会自动过滤只返回自己的申请
}