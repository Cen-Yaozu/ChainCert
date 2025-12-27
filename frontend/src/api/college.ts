import { request } from '@/utils/request'
import type { CollegeRequest, CollegeResponse, MajorRequest, MajorResponse, PageResult } from '@/types'

/**
 * 学院管理API
 */
export const collegeApi = {
  /**
   * 创建学院
   */
  createCollege(data: CollegeRequest) {
    return request.post<CollegeResponse>('/admin/colleges', data)
  },

  /**
   * 更新学院
   */
  updateCollege(id: string, data: CollegeRequest) {
    return request.put<CollegeResponse>(`/admin/colleges/${id}`, data)
  },

  /**
   * 删除学院
   */
  deleteCollege(id: string) {
    return request.delete<void>(`/admin/colleges/${id}`)
  },

  /**
   * 获取学院详情
   */
  getCollegeDetail(id: string) {
    return request.get<CollegeResponse>(`/admin/colleges/${id}`)
  },

  /**
   * 分页查询学院列表
   */
  getCollegeList(params: { page: number; size: number; keyword?: string }) {
    return request.get<PageResult<CollegeResponse>>('/admin/colleges', { params })
  },

  /**
   * 获取所有学院（不分页）
   */
  getAllColleges() {
    return request.get<CollegeResponse[]>('/admin/colleges/all')
  },

  /**
   * 分配审批人
   * 后端使用 query param 传递 approverId
   */
  assignApprover(collegeId: string, approverId: string) {
    return request.put<void>(`/admin/colleges/${collegeId}/approver`, null, {
      params: { approverId }
    })
  },

  /**
   * 创建专业
   */
  createMajor(data: MajorRequest) {
    return request.post<MajorResponse>('/admin/majors', data)
  },

  /**
   * 更新专业
   */
  updateMajor(id: string, data: MajorRequest) {
    return request.put<MajorResponse>(`/admin/majors/${id}`, data)
  },

  /**
   * 删除专业
   */
  deleteMajor(id: string) {
    return request.delete<void>(`/admin/majors/${id}`)
  },

  /**
   * 获取专业详情
   */
  getMajorDetail(id: string) {
    return request.get<MajorResponse>(`/admin/majors/${id}`)
  },

  /**
   * 分页查询专业列表
   */
  getMajorList(params: { page: number; size: number; collegeId?: string; keyword?: string }) {
    return request.get<PageResult<MajorResponse>>('/admin/majors', { params })
  },

  /**
   * 获取学院的所有专业
   * 后端路径是 /by-college/
   */
  getMajorsByCollege(collegeId: string) {
    return request.get<MajorResponse[]>(`/admin/majors/by-college/${collegeId}`)
  },

  /**
   * 获取所有专业（不分页）
   */
  getAllMajors() {
    return request.get<MajorResponse[]>('/admin/majors/all')
  }
}