/**
 * 格式化工具函数
 */

/**
 * 格式化日期时间
 * @param date 日期对象或时间戳
 * @param format 格式字符串
 * @returns 格式化后的日期时间字符串
 */
export function formatDateTime(date: Date | string | number, format: string = 'YYYY-MM-DD HH:mm:ss'): string {
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''

  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')

  return format
    .replace('YYYY', String(year))
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 格式化日期
 * @param date 日期对象或时间戳
 * @returns 格式化后的日期字符串
 */
export function formatDate(date: Date | string | number): string {
  return formatDateTime(date, 'YYYY-MM-DD')
}

/**
 * 格式化时间
 * @param date 日期对象或时间戳
 * @returns 格式化后的时间字符串
 */
export function formatTime(date: Date | string | number): string {
  return formatDateTime(date, 'HH:mm:ss')
}

/**
 * 格式化相对时间（多久之前）
 * @param date 日期对象或时间戳
 * @returns 相对时间字符串
 */
export function formatRelativeTime(date: Date | string | number): string {
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''

  const now = new Date()
  const diff = now.getTime() - d.getTime()
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  const months = Math.floor(days / 30)
  const years = Math.floor(days / 365)

  if (seconds < 60) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 30) return `${days}天前`
  if (months < 12) return `${months}个月前`
  return `${years}年前`
}

/**
 * 格式化数字（千分位）
 * @param num 数字
 * @param decimals 小数位数
 * @returns 格式化后的数字字符串
 */
export function formatNumber(num: number, decimals: number = 0): string {
  const parts = num.toFixed(decimals).split('.')
  if (parts[0]) {
    parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',')
  }
  return parts.join('.')
}

/**
 * 格式化货币
 * @param amount 金额
 * @param currency 货币符号
 * @returns 格式化后的货币字符串
 */
export function formatCurrency(amount: number, currency: string = '¥'): string {
  return `${currency}${formatNumber(amount, 2)}`
}

/**
 * 格式化百分比
 * @param value 数值（0-1）
 * @param decimals 小数位数
 * @returns 格式化后的百分比字符串
 */
export function formatPercentage(value: number, decimals: number = 2): string {
  return `${(value * 100).toFixed(decimals)}%`
}

/**
 * 格式化手机号（添加空格）
 * @param phone 手机号
 * @returns 格式化后的手机号
 */
export function formatPhone(phone: string): string {
  if (!phone || phone.length !== 11) return phone
  return phone.replace(/(\d{3})(\d{4})(\d{4})/, '$1 $2 $3')
}

/**
 * 格式化银行卡号（添加空格）
 * @param cardNumber 银行卡号
 * @returns 格式化后的银行卡号
 */
export function formatBankCard(cardNumber: string): string {
  if (!cardNumber) return cardNumber
  return cardNumber.replace(/(\d{4})(?=\d)/g, '$1 ')
}

/**
 * 截断文本
 * @param text 文本
 * @param maxLength 最大长度
 * @param suffix 后缀
 * @returns 截断后的文本
 */
export function truncateText(text: string, maxLength: number, suffix: string = '...'): string {
  if (!text || text.length <= maxLength) return text
  return text.substring(0, maxLength) + suffix
}

/**
 * 首字母大写
 * @param str 字符串
 * @returns 首字母大写的字符串
 */
export function capitalize(str: string): string {
  if (!str) return str
  return str.charAt(0).toUpperCase() + str.slice(1)
}

/**
 * 驼峰转下划线
 * @param str 驼峰字符串
 * @returns 下划线字符串
 */
export function camelToSnake(str: string): string {
  return str.replace(/[A-Z]/g, (letter) => `_${letter.toLowerCase()}`)
}

/**
 * 下划线转驼峰
 * @param str 下划线字符串
 * @returns 驼峰字符串
 */
export function snakeToCamel(str: string): string {
  return str.replace(/_([a-z])/g, (_, letter) => letter.toUpperCase())
}

/**
 * 格式化持续时间（秒转为时分秒）
 * @param seconds 秒数
 * @returns 格式化后的持续时间
 */
export function formatDuration(seconds: number): string {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60

  const parts: string[] = []
  if (hours > 0) parts.push(`${hours}小时`)
  if (minutes > 0) parts.push(`${minutes}分钟`)
  if (secs > 0 || parts.length === 0) parts.push(`${secs}秒`)

  return parts.join('')
}

/**
 * 格式化枚举值为中文
 * @param value 枚举值
 * @param enumMap 枚举映射
 * @returns 中文描述
 */
export function formatEnum(value: string | number, enumMap: Record<string | number, string>): string {
  return enumMap[value] || String(value)
}

/**
 * 高亮搜索关键词
 * @param text 文本
 * @param keyword 关键词
 * @returns HTML 字符串
 */
export function highlightKeyword(text: string, keyword: string): string {
  if (!keyword) return text
  const regex = new RegExp(`(${keyword})`, 'gi')
  return text.replace(regex, '<mark>$1</mark>')
}

/**
 * 移除 HTML 标签
 * @param html HTML 字符串
 * @returns 纯文本
 */
export function stripHtmlTags(html: string): string {
  return html.replace(/<[^>]*>/g, '')
}

/**
 * 转义 HTML 特殊字符
 * @param str 字符串
 * @returns 转义后的字符串
 */
export function escapeHtml(str: string): string {
  const map: Record<string, string> = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;',
  }
  return str.replace(/[&<>"']/g, (char) => map[char] || char)
}

/**
 * 反转义 HTML 特殊字符
 * @param str 字符串
 * @returns 反转义后的字符串
 */
export function unescapeHtml(str: string): string {
  const map: Record<string, string> = {
    '&amp;': '&',
    '&lt;': '<',
    '&gt;': '>',
    '&quot;': '"',
    '&#39;': "'",
  }
  return str.replace(/&(amp|lt|gt|quot|#39);/g, (match) => map[match] || match)
}