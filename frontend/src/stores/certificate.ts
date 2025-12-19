import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Certificate, CertificateListItem, CertificateQuery } from '@/types'
import { certificateApi } from '@/api/certificate'

/**
 * 证书状态管理
 */
export const useCertificateStore = defineStore('certificate', () => {
  // 状态
  const certificates = ref<CertificateListItem[]>([])
  const currentCertificate = ref<Certificate | null>(null)
  const total = ref(0)
  const loading = ref(false)

  /**
   * 获取证书列表
   */
  const fetchCertificates = async (params: CertificateQuery) => {
    loading.value = true
    try {
      const response = await certificateApi.getList(params)
      certificates.value = response.content
      total.value = response.totalElements
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取我的证书列表
   */
  const fetchMyCertificates = async (params?: CertificateQuery) => {
    loading.value = true
    try {
      const response = await certificateApi.getMyCertificates(params)
      certificates.value = response.content
      total.value = response.totalElements
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取证书详情
   */
  const fetchCertificateDetail = async (id: number) => {
    loading.value = true
    try {
      currentCertificate.value = await certificateApi.getDetail(id)
    } finally {
      loading.value = false
    }
  }

  /**
   * 下载证书
   */
  const downloadCertificate = async (id: number, fileName?: string) => {
    loading.value = true
    try {
      const blob = await certificateApi.download(id)
      // 创建下载链接
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = fileName || `certificate_${id}.pdf`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
    } finally {
      loading.value = false
    }
  }

  /**
   * 撤销证书
   */
  const revokeCertificate = async (id: number, reason: string) => {
    loading.value = true
    try {
      await certificateApi.revoke(id, reason)
      // 更新列表中的状态
      const index = certificates.value.findIndex(cert => cert.id === id)
      if (index !== -1 && certificates.value[index]) {
        certificates.value[index].status = 'REVOKED' as any
        certificates.value[index].statusName = '已撤销'
      }
      // 更新当前证书状态
      if (currentCertificate.value?.id === id) {
        currentCertificate.value.status = 'REVOKED' as any
        currentCertificate.value.statusName = '已撤销'
      }
    } finally {
      loading.value = false
    }
  }

  /**
   * 清空当前证书
   */
  const clearCurrentCertificate = () => {
    currentCertificate.value = null
  }

  /**
   * 重置状态
   */
  const reset = () => {
    certificates.value = []
    currentCertificate.value = null
    total.value = 0
    loading.value = false
  }

  return {
    // 状态
    certificates,
    currentCertificate,
    total,
    loading,
    // 方法
    fetchCertificates,
    fetchMyCertificates,
    fetchCertificateDetail,
    downloadCertificate,
    revokeCertificate,
    clearCurrentCertificate,
    reset,
  }
})