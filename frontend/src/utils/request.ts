import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'
import type { ApiResponse } from '@/types'

/**
 * 创建 Axios 实例
 */
const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

/**
 * 请求拦截器
 */
service.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    
    // 添加 Token
    if (authStore.accessToken) {
      config.headers.Authorization = `Bearer ${authStore.accessToken}`
    }

    return config
  },
  (error) => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 */
service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data

    // 如果返回的状态码不是 200，则认为是错误
    // 注意：后端返回的code可能是字符串"200"或数字200
    const code = typeof res.code === 'string' ? parseInt(res.code) : res.code
    if (code !== 200 && code !== 0 && res.success !== true) {
      ElMessage.error(res.message || '请求失败')
      
      // 401: Token 过期或无效
      if (code === 401 || res.code === 'UNAUTHORIZED') {
        const authStore = useAuthStore()
        authStore.clearAuthData()
        router.push({ name: 'Login' })
      }

      return Promise.reject(new Error(res.message || '请求失败'))
    }

    return response
  },
  async (error) => {
    console.error('Response error:', error)

    if (error.response) {
      const { status, data } = error.response

      switch (status) {
        case 401:
          // Token 过期，尝试刷新
          const authStore = useAuthStore()
          if (authStore.refreshToken) {
            try {
              await authStore.refresh()
              // 重试原请求
              return service.request(error.config)
            } catch (refreshError) {
              authStore.clearAuthData()
              router.push({ name: 'Login' })
              ElMessage.error('登录已过期，请重新登录')
            }
          } else {
            authStore.clearAuthData()
            router.push({ name: 'Login' })
            ElMessage.error('未授权，请登录')
          }
          break
        case 403:
          ElMessage.error('没有权限访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error(data?.message || '服务器错误')
          break
        default:
          ElMessage.error(data?.message || '请求失败')
      }
    } else if (error.request) {
      ElMessage.error('网络错误，请检查网络连接')
    } else {
      ElMessage.error('请求配置错误')
    }

    return Promise.reject(error)
  }
)

/**
 * 通用请求方法
 */
export const request = {
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.get(url, config).then((res) => res.data.data)
  },

  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.post(url, data, config).then((res) => res.data.data)
  },

  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.put(url, data, config).then((res) => res.data.data)
  },

  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.delete(url, config).then((res) => res.data.data)
  },

  upload<T = any>(url: string, formData: FormData, config?: AxiosRequestConfig): Promise<T> {
    return service.post(url, formData, {
      ...config,
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    }).then((res) => res.data.data)
  },
}

export default service