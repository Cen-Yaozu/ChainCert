<template>
  <div class="pagination-container">
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :page-sizes="pageSizes"
      :total="total"
      :layout="layout"
      :background="background"
      :disabled="disabled"
      :hide-on-single-page="hideOnSinglePage"
      :small="small"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

/**
 * 分页组件属性
 */
interface PaginationProps {
  // 当前页码
  modelValue?: number
  // 每页显示条数
  pageSize?: number
  // 总条数
  total?: number
  // 每页显示条数选择器的选项
  pageSizes?: number[]
  // 组件布局
  layout?: string
  // 是否为分页按钮添加背景色
  background?: boolean
  // 是否禁用
  disabled?: boolean
  // 只有一页时是否隐藏
  hideOnSinglePage?: boolean
  // 是否使用小型分页样式
  small?: boolean
}

const props = withDefaults(defineProps<PaginationProps>(), {
  modelValue: 1,
  pageSize: 10,
  total: 0,
  pageSizes: () => [10, 20, 50, 100],
  layout: 'total, sizes, prev, pager, next, jumper',
  background: true,
  disabled: false,
  hideOnSinglePage: false,
  small: false,
})

/**
 * 组件事件
 */
interface PaginationEmits {
  (e: 'update:modelValue', value: number): void
  (e: 'update:pageSize', value: number): void
  (e: 'change', page: number, pageSize: number): void
}

const emit = defineEmits<PaginationEmits>()

// 当前页码
const currentPage = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})

// 每页显示条数
const pageSize = computed({
  get: () => props.pageSize,
  set: (value) => emit('update:pageSize', value),
})

/**
 * 每页显示条数改变
 */
const handleSizeChange = (size: number) => {
  emit('update:pageSize', size)
  emit('change', currentPage.value, size)
}

/**
 * 当前页码改变
 */
const handleCurrentChange = (page: number) => {
  emit('update:modelValue', page)
  emit('change', page, pageSize.value)
}
</script>

<style scoped lang="scss">
.pagination-container {
  display: flex;
  justify-content: center;
  padding: 20px 0;

  :deep(.el-pagination) {
    .el-pagination__total {
      margin-right: auto;
    }
  }
}
</style>