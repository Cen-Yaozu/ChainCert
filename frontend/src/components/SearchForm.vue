<template>
  <div class="search-form">
    <el-form
      ref="formRef"
      :model="formData"
      :inline="inline"
      :label-width="labelWidth"
      :size="size"
      @submit.prevent="handleSearch"
    >
      <slot :form-data="formData" />
      
      <el-form-item>
        <el-button type="primary" :icon="Search" @click="handleSearch">
          搜索
        </el-button>
        <el-button :icon="Refresh" @click="handleReset">
          重置
        </el-button>
        <slot name="extra-buttons" />
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'

/**
 * 搜索表单组件属性
 */
interface SearchFormProps {
  // 表单数据
  modelValue: Record<string, any>
  // 是否行内表单
  inline?: boolean
  // 标签宽度
  labelWidth?: string | number
  // 表单尺寸
  size?: 'large' | 'default' | 'small'
}

const props = withDefaults(defineProps<SearchFormProps>(), {
  inline: true,
  labelWidth: 'auto',
  size: 'default',
})

/**
 * 组件事件
 */
interface SearchFormEmits {
  (e: 'update:modelValue', value: Record<string, any>): void
  (e: 'search', value: Record<string, any>): void
  (e: 'reset'): void
}

const emit = defineEmits<SearchFormEmits>()

// 表单引用
const formRef = ref<FormInstance>()

// 表单数据
const formData = ref({ ...props.modelValue })

/**
 * 搜索
 */
const handleSearch = () => {
  emit('update:modelValue', formData.value)
  emit('search', formData.value)
}

/**
 * 重置
 */
const handleReset = () => {
  formRef.value?.resetFields()
  emit('update:modelValue', formData.value)
  emit('reset')
}

/**
 * 验证表单
 */
const validate = async () => {
  return await formRef.value?.validate()
}

/**
 * 清空验证
 */
const clearValidate = () => {
  formRef.value?.clearValidate()
}

// 暴露方法
defineExpose({
  validate,
  clearValidate,
  handleSearch,
  handleReset,
})
</script>

<style scoped lang="scss">
.search-form {
  padding: 16px;
  background-color: var(--el-bg-color);
  border-radius: 4px;
  margin-bottom: 16px;

  :deep(.el-form-item) {
    margin-bottom: 16px;
  }

  :deep(.el-form-item:last-child) {
    margin-right: 0;
  }
}
</style>