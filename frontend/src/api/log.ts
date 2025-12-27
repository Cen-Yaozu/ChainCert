import { request } from '@/utils/request'
import type { SystemLogVO, PageResult } from '@/types'

/**
 * 系统日志管理API
 */
export const logApi = {
  /**
   * 分页查询系统日志
   * 后端参数: keyword, module, userId, startTime, endTime
   */
  getLogList(params: {
    page: number
    size: number
    keyword?: string
    module?: string
    userId?: string
    startTime?: string
    endTime?: string
  }) {
    return request.get<PageResult<SystemLogVO>>('/admin/logs', { params })
  },

  /**
   * 清理过期日志
   * 后端路径是 /admin/logs/clean，参数是 retentionDays
   */
  cleanLogs(retentionDays: number) {
    return request.delete<number>('/admin/logs/clean', {
      params: { retentionDays }
    })
  },

  // 注意: 后端没有日志导出接口
  // exportLogs 方法已移除
}