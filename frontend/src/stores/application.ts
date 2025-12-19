import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Application, ApplicationListItem, ApplicationQuery } from '@/types'
import { applicationApi } from '@/api/application'

/**
 * 申请状态管理
 */
export const useApplicationStore = defineStore('application', () => {
  // 状态
  const applications = ref<ApplicationListItem[]>([])
  const currentApplication = ref<Application | null>(null)
  const total = ref(0)
  const loading = ref(false)

  /**
   * 获取申请列表
   */
  const fetchApplications = async (params: ApplicationQuery) => {
    loading.value = true
    try {
      const response = await applicationApi.getList(params)
      applications.value = response.content
      total.value = response.totalElements
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取我的申请列表
   */
  const fetchMyApplications = async (params: ApplicationQuery) => {
    loading.value = true
    try {
      const response = await applicationApi.getMyApplications(params)
      applications.value = response.content
      total.value = response.totalElements
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取申请详情
   */
  const fetchApplicationDetail = async (id: number) => {
    loading.value = true
    try {
      currentApplication.value = await applicationApi.getDetail(id)
    } finally {
      loading.value = false
    }
  }

  /**
   * 创建申请
   */
  const createApplication = async (data: any) => {
    loading.value = true
    try {
      const application = await applicationApi.create(data)
      return application
    } finally {
      loading.value = false
    }
  }

  /**
   * 取消申请
   */
  const cancelApplication = async (id: number) => {
    loading.value = true
    try {
      await applicationApi.cancel(id)
      // 从列表中移除
      applications.value = applications.value.filter(app => app.id !== id)
      total.value--
    } finally {
      loading.value = false
    }
  }

  /**
   * 清空当前申请
   */
  const clearCurrentApplication = () => {
    currentApplication.value = null
  }

  /**
   * 重置状态
   */
  const reset = () => {
    applications.value = []
    currentApplication.value = null
    total.value = 0
    loading.value = false
  }

  return {
    // 状态
    applications,
    currentApplication,
    total,
    loading,
    // 方法
    fetchApplications,
    fetchMyApplications,
    fetchApplicationDetail,
    createApplication,
    cancelApplication,
    clearCurrentApplication,
    reset,
  }
})