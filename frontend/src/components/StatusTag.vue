<template>
  <el-tag :type="tagType" :effect="effect" :size="size">
    {{ statusText }}
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ApplicationStatus, ApprovalDecision, CertificateStatus } from '@/types'

/**
 * 状态标签组件属性
 */
interface StatusTagProps {
  // 状态值
  status: ApplicationStatus | ApprovalDecision | CertificateStatus | string
  // 状态类型
  statusType?: 'application' | 'approval' | 'certificate' | 'custom'
  // 标签效果
  effect?: 'dark' | 'light' | 'plain'
  // 标签尺寸
  size?: 'large' | 'default' | 'small'
  // 自定义状态映射
  customMap?: Record<string, { text: string; type: string }>
}

const props = withDefaults(defineProps<StatusTagProps>(), {
  statusType: 'application',
  effect: 'light',
  size: 'default',
})

/**
 * 申请状态映射
 */
const applicationStatusMap: Record<ApplicationStatus, { text: string; type: string }> = {
  [ApplicationStatus.DRAFT]: { text: '草稿', type: 'info' },
  [ApplicationStatus.PENDING_FIRST_REVIEW]: { text: '待初审', type: 'warning' },
  [ApplicationStatus.PENDING_FINAL_REVIEW]: { text: '待终审', type: 'warning' },
  [ApplicationStatus.APPROVED]: { text: '已通过', type: 'success' },
  [ApplicationStatus.REJECTED]: { text: '已驳回', type: 'danger' },
  [ApplicationStatus.CANCELLED]: { text: '已取消', type: 'info' },
}

/**
 * 审批决策映射
 */
const approvalDecisionMap: Record<ApprovalDecision, { text: string; type: string }> = {
  [ApprovalDecision.APPROVED]: { text: '通过', type: 'success' },
  [ApprovalDecision.REJECTED]: { text: '驳回', type: 'danger' },
  [ApprovalDecision.PENDING]: { text: '待审批', type: 'warning' },
}

/**
 * 证书状态映射
 */
const certificateStatusMap: Record<CertificateStatus, { text: string; type: string }> = {
  [CertificateStatus.VALID]: { text: '有效', type: 'success' },
  [CertificateStatus.REVOKED]: { text: '已撤销', type: 'danger' },
  [CertificateStatus.EXPIRED]: { text: '已过期', type: 'info' },
}

/**
 * 获取状态映射
 */
const getStatusMap = () => {
  switch (props.statusType) {
    case 'application':
      return applicationStatusMap
    case 'approval':
      return approvalDecisionMap
    case 'certificate':
      return certificateStatusMap
    case 'custom':
      return props.customMap || {}
    default:
      return {}
  }
}

/**
 * 状态文本
 */
const statusText = computed(() => {
  const map = getStatusMap()
  return map[props.status]?.text || props.status
})

/**
 * 标签类型
 */
const tagType = computed(() => {
  const map = getStatusMap()
  const type = map[props.status]?.type || 'info'
  return type as 'success' | 'warning' | 'danger' | 'info'
})
</script>

<style scoped lang="scss">
// 标签样式由 Element Plus 提供
</style>