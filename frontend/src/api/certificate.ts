import { request } from '@/utils/request'
import type {
  Certificate,
  CertificateListItem,
  CertificateQuery,
  VerificationRequest,
  VerificationResult,
  PageResponse,
} from '@/types'

/**
 * 证书相关 API
 */
export const certificateApi = {
  /**
   * 获取证书列表
   */
  getList(params: CertificateQuery): Promise<PageResponse<CertificateListItem>> {
    return request.get('/certificates', { params })
  },

  /**
   * 获取证书详情
   */
  getDetail(id: number): Promise<Certificate> {
    return request.get(`/certificates/${id}`)
  },

  /**
   * 下载证书
   */
  download(id: number): Promise<Blob> {
    return request.get(`/certificates/${id}/download`, {
      responseType: 'blob',
    })
  },

  /**
   * 撤销证书
   */
  revoke(id: number, reason: string): Promise<void> {
    return request.put(`/certificates/${id}/revoke`, { reason })
  },

  /**
   * 获取我的证书列表
   */
  getMyCertificates(params?: CertificateQuery): Promise<PageResponse<CertificateListItem>> {
    return request.get('/certificates/my', { params })
  },

  /**
   * 核验证书
   */
  verify(data: VerificationRequest): Promise<VerificationResult> {
    return request.post('/verification/verify', data)
  },
}