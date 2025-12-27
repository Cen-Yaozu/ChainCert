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
 * 注意: 推荐使用 college.ts 中的 collegeApi
 */
export const adminCollegeApi = {
  /**
   * 创建学院
   */
  create(data: Partial<College>): Promise<College> {
    return request.post('/admin/colleges', data)
  },

  /**
   * 更新学院
   */
  update(id: string, data: Partial<College>): Promise<College> {
    return request.put(`/admin/colleges/${id}`, data)
  },

  /**
   * 删除学院
   */
  delete(id: string): Promise<void> {
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
  getDetail(id: string): Promise<College> {
    return request.get(`/admin/colleges/${id}`)
  },

  /**
   * 分配审批人
   * 后端使用 query param 传递单个 approverId
   */
  assignApprover(id: string, approverId: string): Promise<void> {
    return request.put(`/admin/colleges/${id}/approver`, null, {
      params: { approverId }
    })
  },
}

/**
 * 专业管理相关 API
 * 注意: 推荐使用 college.ts 中的 collegeApi
 */
export const adminMajorApi = {
  /**
   * 创建专业
   */
  create(data: Partial<Major>): Promise<Major> {
    return request.post('/admin/majors', data)
  },

  /**
   * 更新专业
   */
  update(id: string, data: Partial<Major>): Promise<Major> {
    return request.put(`/admin/majors/${id}`, data)
  },

  /**
   * 删除专业
   */
  delete(id: string): Promise<void> {
    return request.delete(`/admin/majors/${id}`)
  },

  /**
   * 获取专业列表
   */
  getList(params?: PageQuery & { collegeId?: string; keyword?: string }): Promise<PageResponse<Major>> {
    return request.get('/admin/majors', { params })
  },

  /**
   * 获取学院下的所有专业
   * 后端路径是 /by-college/
   */
  getByCollege(collegeId: string): Promise<Major[]> {
    return request.get(`/admin/majors/by-college/${collegeId}`)
  },

  /**
   * 获取所有专业（不分页）
   */
  getAll(): Promise<Major[]> {
    return request.get('/admin/majors/all')
  },

  /**
   * 获取专业详情
   */
  getDetail(id: string): Promise<Major> {
    return request.get(`/admin/majors/${id}`)
  },
}

/**
 * 证书模板管理相关 API
 * 注意: 推荐使用 template.ts 中的 templateApi
 */
export const adminTemplateApi = {
  /**
   * 创建模板
   */
  create(data: Partial<CertificateTemplate>): Promise<CertificateTemplate> {
    return request.post('/admin/templates', data)
  },

  /**
   * 更新模板
   */
  update(id: string, data: Partial<CertificateTemplate>): Promise<CertificateTemplate> {
    return request.put(`/admin/templates/${id}`, data)
  },

  /**
   * 删除模板
   */
  delete(id: string): Promise<void> {
    return request.delete(`/admin/templates/${id}`)
  },

  /**
   * 获取模板列表
   */
  getList(params?: PageQuery & { type?: string; keyword?: string; enabled?: boolean }): Promise<PageResponse<CertificateTemplate>> {
    return request.get('/admin/templates', { params })
  },

  /**
   * 获取模板详情
   */
  getDetail(id: string): Promise<CertificateTemplate> {
    return request.get(`/admin/templates/${id}`)
  },

  /**
   * 启用/禁用模板
   * 后端使用 query param 传递 enabled
   */
  toggleStatus(id: string, enabled: boolean): Promise<void> {
    return request.put(`/admin/templates/${id}/status`, null, {
      params: { enabled }
    })
  },

  // 注意: 后端没有以下接口:
  // - enable(id) - 使用 toggleStatus(id, true) 替代
  // - disable(id) - 使用 toggleStatus(id, false) 替代
  // - setDefault(id) - 后端没有此功能
  // - getByType(type) - 后端没有此功能
}

/**
 * 系统日志相关 API
 * 注意: 推荐使用 log.ts 中的 logApi
 */
export const adminLogApi = {
  /**
   * 获取系统日志列表
   * 后端参数: keyword, module, userId, startTime, endTime
   */
  getList(
    params?: PageQuery & {
      keyword?: string
      module?: string
      userId?: string
      startTime?: string
      endTime?: string
    }
  ): Promise<PageResponse<SystemLog>> {
    return request.get('/admin/logs', { params })
  },

  /**
   * 清理过期日志
   * 后端路径是 /admin/logs/clean，参数是 retentionDays
   */
  cleanup(retentionDays: number): Promise<number> {
    return request.delete('/admin/logs/clean', { params: { retentionDays } })
  },

  // 注意: 后端没有日志导出接口
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
   * 后端路径是 /admin/statistics/college/{collegeId}
   */
  getCollegeStats(collegeId: string): Promise<Statistics> {
    return request.get(`/admin/statistics/college/${collegeId}`)
  },

  /**
   * 获取时间范围统计
   * 后端参数是 startTime/endTime，不是 startDate/endDate
   */
  getTimeRangeStats(startTime: string, endTime: string): Promise<Statistics> {
    return request.get('/admin/statistics/date-range', { params: { startTime, endTime } })
  },
}