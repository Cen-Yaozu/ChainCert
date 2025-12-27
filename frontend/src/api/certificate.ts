import { request } from '@/utils/request'
import type {
  Certificate,
  CertificateListItem,
  CertificateQuery,
  VerificationResult,
  PageResponse,
} from '@/types'

/**
 * 证书相关 API
 */
export const certificateApi = {
  /**
   * 获取证书列表
   * 学生只能查看自己的证书（后端自动过滤）
   */
  getList(params: CertificateQuery): Promise<PageResponse<CertificateListItem>> {
    return request.get('/certificates', { params })
  },

  /**
   * 获取证书详情
   */
  getDetail(id: string): Promise<Certificate> {
    return request.get(`/certificates/${id}`)
  },

  /**
   * 根据证书编号获取证书
   */
  getByCertificateNo(certificateNo: string): Promise<Certificate> {
    return request.get(`/certificates/by-no/${certificateNo}`)
  },

  /**
   * 下载证书
   */
  download(id: string): Promise<Blob> {
    return request.get(`/certificates/${id}/download`, {
      responseType: 'blob',
    })
  },

  /**
   * 撤销证书（管理员）
   * 后端使用 query param 传递 reason
   */
  revoke(id: string, reason?: string): Promise<void> {
    return request.put(`/certificates/${id}/revoke`, null, {
      params: { reason }
    })
  },

  /**
   * 获取我的证书列表（学生端）
   */
  getMyCertificates(params?: CertificateQuery): Promise<PageResponse<CertificateListItem>> {
    return request.get('/certificates/my', { params })
  },

  /**
   * 统计证书数量
   */
  count(status?: string): Promise<number> {
    return request.get('/certificates/count', { params: { status } })
  },
}

/**
 * 证书核验相关 API（公开接口，无需登录）
 */
export const verificationApi = {
  /**
   * 核验证书
   * 后端使用 query param 传递 certificateNo
   */
  verify(certificateNo: string): Promise<VerificationResult> {
    return request.post('/verification/verify', null, {
      params: { certificateNo }
    })
  },

  /**
   * 通过 GET 方式核验证书
   */
  verifyByGet(certificateNo: string): Promise<VerificationResult> {
    return request.get(`/verification/verify/${certificateNo}`)
  },

  /**
   * 下载证书（公开）
   */
  download(certificateNo: string): Promise<Blob> {
    return request.get(`/verification/download/${certificateNo}`, {
      responseType: 'blob',
    })
  },

  /**
   * 预览证书（公开）
   */
  preview(certificateNo: string): Promise<Blob> {
    return request.get(`/verification/preview/${certificateNo}`, {
      responseType: 'blob',
    })
  },
}