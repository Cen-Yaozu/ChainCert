import { request } from '@/utils/request'
import type { SystemLogVO, PageResult } from '@/types'

/**
 * 系统日志管理API
 */
export const logApi = {
  /**
   * 分页查询系统日志
   */
  getLogList(params: {
    page: number
    size: number
    username?: string
    action?: string
    module?: string
    startDate?: string
    endDate?: string
  }) {
    return request.get<PageResult<SystemLogVO>>('/admin/logs', { params })
  },

  /**
   * 清理过期日志
   */
  cleanLogs(days: number) {
    return request.delete<{ deletedCount: number }>('/admin/logs/clean', { params: { days } })
  },

  /**
   * 导出日志
   */
  exportLogs(params: {
    username?: string
    action?: string
    module?: string
    startDate?: string
    endDate?: string
  }) {
    return request.get('/admin/logs/export', {
      params,
      responseType: 'blob'
    })
  }
}