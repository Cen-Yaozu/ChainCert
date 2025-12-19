/**
 * 表单验证工具函数
 */

/**
 * 验证手机号
 * @param phone 手机号
 * @returns 是否有效
 */
export function validatePhone(phone: string): boolean {
  const phoneRegex = /^1[3-9]\d{9}$/
  return phoneRegex.test(phone)
}

/**
 * 验证邮箱
 * @param email 邮箱
 * @returns 是否有效
 */
export function validateEmail(email: string): boolean {
  const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
  return emailRegex.test(email)
}

/**
 * 验证学号（8-20位数字或字母）
 * @param studentId 学号
 * @returns 是否有效
 */
export function validateStudentId(studentId: string): boolean {
  const studentIdRegex = /^[a-zA-Z0-9]{8,20}$/
  return studentIdRegex.test(studentId)
}

/**
 * 验证身份证号
 * @param idCard 身份证号
 * @returns 是否有效
 */
export function validateIdCard(idCard: string): boolean {
  const idCardRegex = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/
  return idCardRegex.test(idCard)
}

/**
 * 验证密码强度（至少8位，包含大小写字母和数字）
 * @param password 密码
 * @returns 是否有效
 */
export function validatePassword(password: string): boolean {
  const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d@$!%*?&]{8,}$/
  return passwordRegex.test(password)
}

/**
 * 验证用户名（4-20位字母、数字、下划线）
 * @param username 用户名
 * @returns 是否有效
 */
export function validateUsername(username: string): boolean {
  const usernameRegex = /^[a-zA-Z0-9_]{4,20}$/
  return usernameRegex.test(username)
}

/**
 * 验证URL
 * @param url URL地址
 * @returns 是否有效
 */
export function validateUrl(url: string): boolean {
  const urlRegex = /^(https?:\/\/)?([\da-z.-]+)\.([a-z.]{2,6})([/\w .-]*)*\/?$/
  return urlRegex.test(url)
}

/**
 * 验证证书编号（格式：CERT-YYYYMMDD-XXXXXX）
 * @param certNumber 证书编号
 * @returns 是否有效
 */
export function validateCertNumber(certNumber: string): boolean {
  const certNumberRegex = /^CERT-\d{8}-\d{6}$/
  return certNumberRegex.test(certNumber)
}

/**
 * Element Plus 表单验证规则生成器
 */

/**
 * 必填验证规则
 * @param message 错误提示信息
 * @param trigger 触发方式
 * @returns 验证规则
 */
export function requiredRule(message: string, trigger: string | string[] = 'blur') {
  return { required: true, message, trigger }
}

/**
 * 手机号验证规则
 * @param trigger 触发方式
 * @returns 验证规则
 */
export function phoneRule(trigger: string | string[] = 'blur') {
  return {
    validator: (_rule: any, value: string, callback: any) => {
      if (!value) {
        callback()
      } else if (!validatePhone(value)) {
        callback(new Error('请输入正确的手机号'))
      } else {
        callback()
      }
    },
    trigger,
  }
}

/**
 * 邮箱验证规则
 * @param trigger 触发方式
 * @returns 验证规则
 */
export function emailRule(trigger: string | string[] = 'blur') {
  return {
    validator: (_rule: any, value: string, callback: any) => {
      if (!value) {
        callback()
      } else if (!validateEmail(value)) {
        callback(new Error('请输入正确的邮箱地址'))
      } else {
        callback()
      }
    },
    trigger,
  }
}

/**
 * 学号验证规则
 * @param trigger 触发方式
 * @returns 验证规则
 */
export function studentIdRule(trigger: string | string[] = 'blur') {
  return {
    validator: (_rule: any, value: string, callback: any) => {
      if (!value) {
        callback()
      } else if (!validateStudentId(value)) {
        callback(new Error('学号应为8-20位数字或字母'))
      } else {
        callback()
      }
    },
    trigger,
  }
}

/**
 * 身份证号验证规则
 * @param trigger 触发方式
 * @returns 验证规则
 */
export function idCardRule(trigger: string | string[] = 'blur') {
  return {
    validator: (_rule: any, value: string, callback: any) => {
      if (!value) {
        callback()
      } else if (!validateIdCard(value)) {
        callback(new Error('请输入正确的身份证号'))
      } else {
        callback()
      }
    },
    trigger,
  }
}

/**
 * 密码强度验证规则
 * @param trigger 触发方式
 * @returns 验证规则
 */
export function passwordRule(trigger: string | string[] = 'blur') {
  return {
    validator: (_rule: any, value: string, callback: any) => {
      if (!value) {
        callback()
      } else if (!validatePassword(value)) {
        callback(new Error('密码至少8位，需包含大小写字母和数字'))
      } else {
        callback()
      }
    },
    trigger,
  }
}

/**
 * 确认密码验证规则
 * @param password 原密码
 * @param trigger 触发方式
 * @returns 验证规则
 */
export function confirmPasswordRule(password: string, trigger: string | string[] = 'blur') {
  return {
    validator: (_rule: any, value: string, callback: any) => {
      if (!value) {
        callback(new Error('请再次输入密码'))
      } else if (value !== password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    },
    trigger,
  }
}

/**
 * 长度验证规则
 * @param min 最小长度
 * @param max 最大长度
 * @param trigger 触发方式
 * @returns 验证规则
 */
export function lengthRule(min: number, max: number, trigger: string | string[] = 'blur') {
  return {
    min,
    max,
    message: `长度应在 ${min} 到 ${max} 个字符之间`,
    trigger,
  }
}

/**
 * 数字范围验证规则
 * @param min 最小值
 * @param max 最大值
 * @param trigger 触发方式
 * @returns 验证规则
 */
export function rangeRule(min: number, max: number, trigger: string | string[] = 'blur') {
  return {
    type: 'number',
    min,
    max,
    message: `数值应在 ${min} 到 ${max} 之间`,
    trigger,
  }
}