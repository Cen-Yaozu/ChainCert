/**
 * 文件处理工具函数
 */

/**
 * 允许的图片格式
 */
export const IMAGE_TYPES = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']

/**
 * 允许的文档格式
 */
export const DOCUMENT_TYPES = [
  'application/pdf',
  'application/msword',
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  'application/vnd.ms-excel',
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
]

/**
 * 允许的压缩包格式
 */
export const ARCHIVE_TYPES = ['application/zip', 'application/x-rar-compressed', 'application/x-7z-compressed']

/**
 * 文件大小限制（MB）
 */
export const FILE_SIZE_LIMITS = {
  image: 5, // 图片最大5MB
  document: 10, // 文档最大10MB
  archive: 50, // 压缩包最大50MB
  default: 20, // 默认最大20MB
}

/**
 * 验证文件类型
 * @param file 文件对象
 * @param allowedTypes 允许的文件类型数组
 * @returns 是否有效
 */
export function validateFileType(file: File, allowedTypes: string[]): boolean {
  return allowedTypes.includes(file.type)
}

/**
 * 验证文件大小
 * @param file 文件对象
 * @param maxSize 最大大小（MB）
 * @returns 是否有效
 */
export function validateFileSize(file: File, maxSize: number): boolean {
  const fileSizeMB = file.size / 1024 / 1024
  return fileSizeMB <= maxSize
}

/**
 * 验证图片文件
 * @param file 文件对象
 * @returns 验证结果
 */
export function validateImageFile(file: File): { valid: boolean; message?: string } {
  if (!validateFileType(file, IMAGE_TYPES)) {
    return { valid: false, message: '只支持 JPG、PNG、GIF、WebP 格式的图片' }
  }
  if (!validateFileSize(file, FILE_SIZE_LIMITS.image)) {
    return { valid: false, message: `图片大小不能超过 ${FILE_SIZE_LIMITS.image}MB` }
  }
  return { valid: true }
}

/**
 * 验证文档文件
 * @param file 文件对象
 * @returns 验证结果
 */
export function validateDocumentFile(file: File): { valid: boolean; message?: string } {
  if (!validateFileType(file, DOCUMENT_TYPES)) {
    return { valid: false, message: '只支持 PDF、Word、Excel 格式的文档' }
  }
  if (!validateFileSize(file, FILE_SIZE_LIMITS.document)) {
    return { valid: false, message: `文档大小不能超过 ${FILE_SIZE_LIMITS.document}MB` }
  }
  return { valid: true }
}

/**
 * 格式化文件大小
 * @param bytes 字节数
 * @returns 格式化后的文件大小
 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i]
}

/**
 * 获取文件扩展名
 * @param filename 文件名
 * @returns 扩展名
 */
export function getFileExtension(filename: string): string {
  return filename.slice(((filename.lastIndexOf('.') - 1) >>> 0) + 2)
}

/**
 * 获取文件名（不含扩展名）
 * @param filename 文件名
 * @returns 文件名
 */
export function getFileNameWithoutExtension(filename: string): string {
  return filename.replace(/\.[^/.]+$/, '')
}

/**
 * 读取文件为 Base64
 * @param file 文件对象
 * @returns Base64 字符串
 */
export function readFileAsBase64(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result as string)
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
}

/**
 * 读取文件为文本
 * @param file 文件对象
 * @returns 文本内容
 */
export function readFileAsText(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result as string)
    reader.onerror = reject
    reader.readAsText(file)
  })
}

/**
 * 下载文件
 * @param blob Blob 对象
 * @param filename 文件名
 */
export function downloadFile(blob: Blob, filename: string): void {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

/**
 * 下载 Base64 文件
 * @param base64 Base64 字符串
 * @param filename 文件名
 * @param mimeType MIME 类型
 */
export function downloadBase64File(base64: string, filename: string, mimeType: string): void {
  const byteCharacters = atob(base64.split(',')[1] || base64)
  const byteNumbers = new Array(byteCharacters.length)
  for (let i = 0; i < byteCharacters.length; i++) {
    byteNumbers[i] = byteCharacters.charCodeAt(i)
  }
  const byteArray = new Uint8Array(byteNumbers)
  const blob = new Blob([byteArray], { type: mimeType })
  downloadFile(blob, filename)
}

/**
 * 压缩图片
 * @param file 图片文件
 * @param maxWidth 最大宽度
 * @param maxHeight 最大高度
 * @param quality 质量（0-1）
 * @returns 压缩后的 Blob
 */
export function compressImage(
  file: File,
  maxWidth: number = 1920,
  maxHeight: number = 1080,
  quality: number = 0.8
): Promise<Blob> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      const img = new Image()
      img.onload = () => {
        const canvas = document.createElement('canvas')
        let width = img.width
        let height = img.height

        // 计算缩放比例
        if (width > maxWidth || height > maxHeight) {
          const ratio = Math.min(maxWidth / width, maxHeight / height)
          width = width * ratio
          height = height * ratio
        }

        canvas.width = width
        canvas.height = height

        const ctx = canvas.getContext('2d')
        if (!ctx) {
          reject(new Error('无法获取 canvas context'))
          return
        }

        ctx.drawImage(img, 0, 0, width, height)

        canvas.toBlob(
          (blob) => {
            if (blob) {
              resolve(blob)
            } else {
              reject(new Error('图片压缩失败'))
            }
          },
          file.type,
          quality
        )
      }
      img.onerror = reject
      img.src = e.target?.result as string
    }
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
}

/**
 * 创建文件预览 URL
 * @param file 文件对象
 * @returns 预览 URL
 */
export function createFilePreviewUrl(file: File): string {
  return URL.createObjectURL(file)
}

/**
 * 释放文件预览 URL
 * @param url 预览 URL
 */
export function revokeFilePreviewUrl(url: string): void {
  URL.revokeObjectURL(url)
}

/**
 * 判断是否为图片文件
 * @param file 文件对象
 * @returns 是否为图片
 */
export function isImageFile(file: File): boolean {
  return IMAGE_TYPES.includes(file.type)
}

/**
 * 判断是否为文档文件
 * @param file 文件对象
 * @returns 是否为文档
 */
export function isDocumentFile(file: File): boolean {
  return DOCUMENT_TYPES.includes(file.type)
}

/**
 * 判断是否为压缩包文件
 * @param file 文件对象
 * @returns 是否为压缩包
 */
export function isArchiveFile(file: File): boolean {
  return ARCHIVE_TYPES.includes(file.type)
}