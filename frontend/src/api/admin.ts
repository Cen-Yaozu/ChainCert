import { request } from '@/utils/request'
import type {
  College,
  Major,
  CertificateTemplate,
  SystemLog,
  Statistics,
  PageResponse,
  PageQuery,
} from '@/types'

/**
 * 学院管理相关 API
 */
export const collegeApi = {
  /**
   * 创建学院
   */
  create(data: Partial<College>): Promise<College> {
    return request.post('/admin/colleges', data)
  },

  /**
   * 更新学院
   */
  update(id: number, data: Partial<College>): Promise<College> {
    return request.put(`/admin/colleges/${id}`, data)
  },

  /**
   * 删除学院
   */
  delete(id: number): Promise<void> {
    return request.delete(`/admin/colleges/${id}`)
  },

  /**
   * 获取学院列表
   */
  getList(params?: PageQuery & { keyword?: string }): Promise<PageResponse<College>> {
    return request.get('/admin/colleges', { params })
  },

  /**
   * 获取所有学院（不分页）
   */
  getAll(): Promise<College[]> {
    return request.get('/admin/colleges/all')
  },

  /**
   * 获取学院详情
   */
  getDetail(id: number): Promise<College> {
    return request.get(`/admin/colleges/${id}`)
  },

  /**
   * 分配审批人
   */
  assignApprovers(id: number, approverIds: number[]): Promise<void> {
    return request.put(`/admin/colleges/${id}/approvers`, { approverIds })
  },
}

/**
 * 专业管理相关 API
 */
export const majorApi = {
  /**
   * 创建专业
   */
  create(data: Partial<Major>): Promise<Major> {
    return request.post('/admin/majors', data)
  },

  /**
   * 更新专业
   */
  update(id: number, data: Partial<Major>): Promise<Major> {
    return request.put(`/admin/majors/${id}`, data)
  },

  /**
   * 删除专业
   */
  delete(id: number): Promise<void> {
    return request.delete(`/admin/majors/${id}`)
  },

  /**
   * 获取专业列表
   */
  getList(params?: PageQuery & { collegeId?: number; keyword?: string }): Promise<PageResponse<Major>> {
    return request.get('/admin/majors', { params })
  },

  /**
   * 获取学院下的所有专业
   */
  getByCollege(collegeId: number): Promise<Major[]> {
    return request.get(`/admin/majors/college/${collegeId}`)
  },

  /**
   * 获取专业详情
   */
  getDetail(id: number): Promise<Major> {
    return request.get(`/admin/majors/${id}`)
  },
}

/**
 * 证书模板管理相关 API
 */
export const templateApi = {
  /**
   * 创建模板
   */
  create(data: Partial<CertificateTemplate>): Promise<CertificateTemplate> {
    return request.post('/admin/templates', data)
  },

  /**
   * 更新模板
   */
  update(id: number, data: Partial<CertificateTemplate>): Promise<CertificateTemplate> {
    return request.put(`/admin/templates/${id}`, data)
  },

  /**
   * 删除模板
   */
  delete(id: number): Promise<void> {
    return request.delete(`/admin/templates/${id}`)
  },

  /**
   * 获取模板列表
   */
  getList(params?: PageQuery & { type?: string; keyword?: string }): Promise<PageResponse<CertificateTemplate>> {
    return request.get('/admin/templates', { params })
  },

  /**
   * 获取模板详情
   */
  getDetail(id: number): Promise<CertificateTemplate> {
    return request.get(`/admin/templates/${id}`)
  },

  /**
   * 启用模板
   */
  enable(id: number): Promise<void> {
    return request.put(`/admin/templates/${id}/enable`)
  },

  /**
   * 禁用模板
   */
  disable(id: number): Promise<void> {
    return request.put(`/admin/templates/${id}/disable`)
  },

  /**
   * 设置默认模板
   */
  setDefault(id: number): Promise<void> {
    return request.put(`/admin/templates/${id}/set-default`)
  },

  /**
   * 按类型获取模板
   */
  getByType(type: string): Promise<CertificateTemplate[]> {
    return request.get(`/admin/templates/type/${type}`)
  },
}

/**
 * 系统日志相关 API
 */
export const logApi = {
  /**
   * 获取系统日志列表
   */
  getList(
    params?: PageQuery & {
      module?: string
      operation?: string
      username?: string
      startDate?: string
      endDate?: string
    }
  ): Promise<PageResponse<SystemLog>> {
    return request.get('/admin/logs', { params })
  },

  /**
   * 清理过期日志
   */
  cleanup(days: number): Promise<{ deletedCount: number }> {
    return request.delete('/admin/logs/cleanup', { params: { days } })
  },
}

/**
 * 统计数据相关 API
 */
export const statisticsApi = {
  /**
   * 获取系统统计数据
   */
  getSystemStats(): Promise<Statistics> {
    return request.get('/admin/statistics')
  },

  /**
   * 获取学院统计数据
   */
  getCollegeStats(collegeId?: number): Promise<Statistics> {
    return request.get('/admin/statistics/college', { params: { collegeId } })
  },

  /**
   * 获取时间范围统计
   */
  getTimeRangeStats(startDate: string, endDate: string): Promise<Statistics> {
    return request.get('/admin/statistics/time-range', { params: { startDate, endDate } })
  },
}