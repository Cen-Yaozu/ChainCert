/**
 * 加密工具函数
 */

/**
 * 简单的字符串加密（Base64）
 * @param str 原始字符串
 * @returns 加密后的字符串
 */
export function encryptString(str: string): string {
  try {
    return btoa(encodeURIComponent(str))
  } catch (error) {
    console.error('加密失败:', error)
    return str
  }
}

/**
 * 简单的字符串解密（Base64）
 * @param str 加密后的字符串
 * @returns 解密后的字符串
 */
export function decryptString(str: string): string {
  try {
    return decodeURIComponent(atob(str))
  } catch (error) {
    console.error('解密失败:', error)
    return str
  }
}

/**
 * 加密存储到 localStorage
 * @param key 键名
 * @param value 值
 */
export function setEncryptedStorage(key: string, value: any): void {
  try {
    const jsonStr = JSON.stringify(value)
    const encrypted = encryptString(jsonStr)
    localStorage.setItem(key, encrypted)
  } catch (error) {
    console.error('加密存储失败:', error)
  }
}

/**
 * 从 localStorage 解密读取
 * @param key 键名
 * @returns 解密后的值
 */
export function getEncryptedStorage<T>(key: string): T | null {
  try {
    const encrypted = localStorage.getItem(key)
    if (!encrypted) return null
    const decrypted = decryptString(encrypted)
    return JSON.parse(decrypted) as T
  } catch (error) {
    console.error('解密读取失败:', error)
    return null
  }
}

/**
 * 移除加密存储
 * @param key 键名
 */
export function removeEncryptedStorage(key: string): void {
  localStorage.removeItem(key)
}

/**
 * 生成随机字符串
 * @param length 长度
 * @returns 随机字符串
 */
export function generateRandomString(length: number = 16): string {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  let result = ''
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  return result
}

/**
 * 生成 UUID
 * @returns UUID 字符串
 */
export function generateUUID(): string {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0
    const v = c === 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

/**
 * 计算字符串的简单哈希值
 * @param str 字符串
 * @returns 哈希值
 */
export function simpleHash(str: string): number {
  let hash = 0
  if (str.length === 0) return hash
  for (let i = 0; i < str.length; i++) {
    const char = str.charCodeAt(i)
    hash = (hash << 5) - hash + char
    hash = hash & hash // Convert to 32bit integer
  }
  return Math.abs(hash)
}

/**
 * 掩码处理手机号
 * @param phone 手机号
 * @returns 掩码后的手机号
 */
export function maskPhone(phone: string): string {
  if (!phone || phone.length !== 11) return phone
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

/**
 * 掩码处理邮箱
 * @param email 邮箱
 * @returns 掩码后的邮箱
 */
export function maskEmail(email: string): string {
  if (!email || !email.includes('@')) return email
  const [username, domain] = email.split('@')
  if (!username || username.length <= 2) return email
  const maskedUsername = username.charAt(0) + '***' + username.charAt(username.length - 1)
  return `${maskedUsername}@${domain}`
}

/**
 * 掩码处理身份证号
 * @param idCard 身份证号
 * @returns 掩码后的身份证号
 */
export function maskIdCard(idCard: string): string {
  if (!idCard || idCard.length < 8) return idCard
  return idCard.replace(/(\d{6})\d+(\d{4})/, '$1********$2')
}

/**
 * 掩码处理银行卡号
 * @param cardNumber 银行卡号
 * @returns 掩码后的银行卡号
 */
export function maskBankCard(cardNumber: string): string {
  if (!cardNumber || cardNumber.length < 8) return cardNumber
  return cardNumber.replace(/(\d{4})\d+(\d{4})/, '$1 **** **** $2')
}