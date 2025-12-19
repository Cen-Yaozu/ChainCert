<template>
  <div class="file-upload">
    <el-upload
      ref="uploadRef"
      :action="action"
      :headers="headers"
      :data="data"
      :name="name"
      :multiple="multiple"
      :limit="limit"
      :accept="accept"
      :file-list="fileList"
      :auto-upload="autoUpload"
      :disabled="disabled"
      :drag="drag"
      :show-file-list="showFileList"
      :before-upload="handleBeforeUpload"
      :on-success="handleSuccess"
      :on-error="handleError"
      :on-progress="handleProgress"
      :on-change="handleChange"
      :on-exceed="handleExceed"
      :on-remove="handleRemove"
      :list-type="listType"
    >
      <template v-if="drag">
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template v-if="tip">
          <div class="el-upload__tip">{{ tip }}</div>
        </template>
      </template>
      <template v-else>
        <el-button :type="buttonType" :icon="Upload">{{ buttonText }}</el-button>
        <template v-if="tip">
          <div class="el-upload__tip">{{ tip }}</div>
        </template>
      </template>
    </el-upload>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload, UploadFilled } from '@element-plus/icons-vue'
import type { UploadInstance, UploadProps, UploadUserFile, UploadFile } from 'element-plus'
import { validateFileType, validateFileSize } from '@/utils/file'

/**
 * 文件上传组件属性
 */
interface FileUploadProps {
  // 上传地址
  action?: string
  // 请求头
  headers?: Record<string, string>
  // 额外数据
  data?: Record<string, any>
  // 文件字段名
  name?: string
  // 是否支持多选
  multiple?: boolean
  // 最大上传数量
  limit?: number
  // 接受的文件类型
  accept?: string
  // 允许的文件类型数组
  allowedTypes?: string[]
  // 最大文件大小（MB）
  maxSize?: number
  // 是否自动上传
  autoUpload?: boolean
  // 是否禁用
  disabled?: boolean
  // 是否启用拖拽上传
  drag?: boolean
  // 是否显示文件列表
  showFileList?: boolean
  // 文件列表类型
  listType?: 'text' | 'picture' | 'picture-card'
  // 按钮文字
  buttonText?: string
  // 按钮类型
  buttonType?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
  // 提示文字
  tip?: string
  // 文件列表
  modelValue?: UploadUserFile[]
}

const props = withDefaults(defineProps<FileUploadProps>(), {
  action: '#',
  name: 'file',
  multiple: false,
  limit: 1,
  accept: '*',
  autoUpload: true,
  disabled: false,
  drag: false,
  showFileList: true,
  listType: 'text',
  buttonText: '选择文件',
  buttonType: 'primary',
  modelValue: () => [],
})

/**
 * 组件事件
 */
interface FileUploadEmits {
  (e: 'update:modelValue', value: UploadUserFile[]): void
  (e: 'success', response: any, file: UploadFile, fileList: UploadUserFile[]): void
  (e: 'error', error: Error, file: UploadFile, fileList: UploadUserFile[]): void
  (e: 'progress', event: any, file: UploadFile, fileList: UploadUserFile[]): void
  (e: 'change', file: UploadFile, fileList: UploadUserFile[]): void
  (e: 'exceed', files: File[], fileList: UploadUserFile[]): void
  (e: 'remove', file: UploadFile, fileList: UploadUserFile[]): void
}

const emit = defineEmits<FileUploadEmits>()

// 上传组件引用
const uploadRef = ref<UploadInstance>()

// 文件列表
const fileList = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})

/**
 * 上传前的钩子
 */
const handleBeforeUpload: UploadProps['beforeUpload'] = (rawFile) => {
  // 验证文件类型
  if (props.allowedTypes && props.allowedTypes.length > 0) {
    if (!validateFileType(rawFile, props.allowedTypes)) {
      ElMessage.error('文件类型不符合要求')
      return false
    }
  }

  // 验证文件大小
  if (props.maxSize) {
    if (!validateFileSize(rawFile, props.maxSize)) {
      ElMessage.error(`文件大小不能超过 ${props.maxSize}MB`)
      return false
    }
  }

  return true
}

/**
 * 上传成功的钩子
 */
const handleSuccess: UploadProps['onSuccess'] = (response, file, fileList) => {
  ElMessage.success('上传成功')
  emit('success', response, file, fileList)
  emit('update:modelValue', fileList)
}

/**
 * 上传失败的钩子
 */
const handleError: UploadProps['onError'] = (error, file, fileList) => {
  ElMessage.error('上传失败')
  emit('error', error, file, fileList)
}

/**
 * 上传进度的钩子
 */
const handleProgress: UploadProps['onProgress'] = (event, file, fileList) => {
  emit('progress', event, file, fileList)
}

/**
 * 文件状态改变的钩子
 */
const handleChange: UploadProps['onChange'] = (file, fileList) => {
  emit('change', file, fileList)
  emit('update:modelValue', fileList)
}

/**
 * 文件超出个数限制的钩子
 */
const handleExceed: UploadProps['onExceed'] = (files, fileList) => {
  ElMessage.warning(`最多只能上传 ${props.limit} 个文件`)
  emit('exceed', files, fileList)
}

/**
 * 文件移除的钩子
 */
const handleRemove: UploadProps['onRemove'] = (file, fileList) => {
  emit('remove', file, fileList)
  emit('update:modelValue', fileList)
}

/**
 * 手动上传
 */
const submit = () => {
  uploadRef.value?.submit()
}

/**
 * 清空文件列表
 */
const clearFiles = () => {
  uploadRef.value?.clearFiles()
}

/**
 * 取消上传
 */
const abort = () => {
  uploadRef.value?.abort()
}

// 暴露方法
defineExpose({
  submit,
  clearFiles,
  abort,
})
</script>

<style scoped lang="scss">
.file-upload {
  :deep(.el-upload__tip) {
    margin-top: 8px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  :deep(.el-upload-dragger) {
    padding: 40px;
  }

  :deep(.el-icon--upload) {
    font-size: 67px;
    color: var(--el-text-color-placeholder);
    margin-bottom: 16px;
  }

  :deep(.el-upload__text) {
    font-size: 14px;
    color: var(--el-text-color-regular);

    em {
      color: var(--el-color-primary);
      font-style: normal;
    }
  }
}
</style>