import { request } from '@/utils/request'
import type { TemplateRequest, TemplateResponse, PageResult } from '@/types'

/**
 * 证书模板管理API
 */
export const templateApi = {
  /**
   * 创建模板
   */
  createTemplate(data: TemplateRequest) {
    return request.post<TemplateResponse>('/admin/templates', data)
  },

  /**
   * 更新模板
   */
  updateTemplate(id: string, data: TemplateRequest) {
    return request.put<TemplateResponse>(`/admin/templates/${id}`, data)
  },

  /**
   * 删除模板
   */
  deleteTemplate(id: string) {
    return request.delete<void>(`/admin/templates/${id}`)
  },

  /**
   * 获取模板详情
   */
  getTemplateDetail(id: string) {
    return request.get<TemplateResponse>(`/admin/templates/${id}`)
  },

  /**
   * 分页查询模板列表
   */
  getTemplateList(params: { page: number; size: number; type?: string; keyword?: string; enabled?: boolean }) {
    return request.get<PageResult<TemplateResponse>>('/admin/templates', { params })
  },

  /**
   * 启用/禁用模板
   * 后端使用 query param 传递 enabled
   */
  toggleTemplateStatus(id: string, enabled: boolean) {
    return request.put<void>(`/admin/templates/${id}/status`, null, {
      params: { enabled }
    })
  },

  // 注意: 后端没有以下接口:
  // - enable(id) - 使用 toggleTemplateStatus(id, true) 替代
  // - disable(id) - 使用 toggleTemplateStatus(id, false) 替代
  // - setDefault(id) - 后端没有此功能
  // - getByType(type) - 后端没有此功能
}