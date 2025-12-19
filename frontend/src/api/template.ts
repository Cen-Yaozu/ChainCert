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
  updateTemplate(id: number, data: TemplateRequest) {
    return request.put<TemplateResponse>(`/admin/templates/${id}`, data)
  },

  /**
   * 删除模板
   */
  deleteTemplate(id: number) {
    return request.delete<void>(`/admin/templates/${id}`)
  },

  /**
   * 获取模板详情
   */
  getTemplateDetail(id: number) {
    return request.get<TemplateResponse>(`/admin/templates/${id}`)
  },

  /**
   * 分页查询模板列表
   */
  getTemplateList(params: { page: number; size: number; type?: string; keyword?: string }) {
    return request.get<PageResult<TemplateResponse>>('/admin/templates', { params })
  },

  /**
   * 启用/禁用模板
   */
  toggleTemplateStatus(id: number, enabled: boolean) {
    return request.put<void>(`/admin/templates/${id}/status`, { enabled })
  },

  /**
   * 设置默认模板
   */
  setDefaultTemplate(id: number) {
    return request.put<void>(`/admin/templates/${id}/status`, { isDefault: true })
  }
}