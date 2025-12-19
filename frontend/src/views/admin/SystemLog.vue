<template>
  <div class="system-log">
    <!-- 搜索表单 -->
    <el-card class="search-card" shadow="never">
      <SearchForm
        :model="searchForm"
        :loading="loading"
        @search="handleSearch"
        @reset="handleReset"
      >
        <el-form-item label="操作用户">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入用户名"
            clearable
          />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select
            v-model="searchForm.action"
            placeholder="请选择操作类型"
            clearable
          >
            <el-option label="登录" value="LOGIN" />
            <el-option label="登出" value="LOGOUT" />
            <el-option label="创建" value="CREATE" />
            <el-option label="更新" value="UPDATE" />
            <el-option label="删除" value="DELETE" />
            <el-option label="审批" value="APPROVE" />
            <el-option label="拒绝" value="REJECT" />
            <el-option label="撤销" value="REVOKE" />
          </el-select>
        </el-form-item>
        <el-form-item label="模块">
          <el-select
            v-model="searchForm.module"
            placeholder="请选择模块"
            clearable
          >
            <el-option label="用户管理" value="USER" />
            <el-option label="申请管理" value="APPLICATION" />
            <el-option label="审批管理" value="APPROVAL" />
            <el-option label="证书管理" value="CERTIFICATE" />
            <el-option label="学院管理" value="COLLEGE" />
            <el-option label="模板管理" value="TEMPLATE" />
            <el-option label="系统配置" value="SYSTEM" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </el-form-item>
      </SearchForm>
    </el-card>

    <!-- 操作按钮 -->
    <el-card class="toolbar-card" shadow="never">
      <el-button
        :icon="Refresh"
        @click="loadLogs"
      >
        刷新
      </el-button>
      <el-button
        :icon="Download"
        @click="handleExport"
      >
        导出日志
      </el-button>
    </el-card>

    <!-- 日志列表 -->
    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="logs"
        stripe
        border
      >
        <el-table-column
          prop="id"
          label="ID"
          width="80"
          align="center"
        />
        <el-table-column
          prop="username"
          label="操作用户"
          width="120"
        />
        <el-table-column
          prop="action"
          label="操作类型"
          width="100"
          align="center"
        >
          <template #default="{ row }">
            <el-tag :type="getActionType(row.action)">
              {{ getActionText(row.action) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="module"
          label="模块"
          width="120"
          align="center"
        >
          <template #default="{ row }">
            {{ getModuleText(row.module) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="description"
          label="操作描述"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column
          prop="ip"
          label="IP地址"
          width="140"
        />
        <el-table-column
          prop="userAgent"
          label="用户代理"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column
          prop="createdAt"
          label="操作时间"
          width="180"
          align="center"
        >
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="100"
          align="center"
          fixed="right"
        >
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              link
              :icon="View"
              @click="handleView(row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <Pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        @change="loadLogs"
      />
    </el-card>

    <!-- 日志详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="日志详情"
      width="700px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">
          {{ detailLog?.id }}
        </el-descriptions-item>
        <el-descriptions-item label="操作用户">
          {{ detailLog?.username }}
        </el-descriptions-item>
        <el-descriptions-item label="操作类型">
          <el-tag :type="getActionType(detailLog?.action || '')">
            {{ getActionText(detailLog?.action || '') }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="模块">
          {{ getModuleText(detailLog?.module || '') }}
        </el-descriptions-item>
        <el-descriptions-item label="操作描述" :span="2">
          {{ detailLog?.description }}
        </el-descriptions-item>
        <el-descriptions-item label="IP地址">
          {{ detailLog?.ip }}
        </el-descriptions-item>
        <el-descriptions-item label="操作时间">
          {{ formatDateTime(detailLog?.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item label="用户代理" :span="2">
          {{ detailLog?.userAgent }}
        </el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <pre style="white-space: pre-wrap; word-wrap: break-word; max-height: 300px; overflow-y: auto;">{{ detailLog?.requestParams }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="响应结果" :span="2">
          <pre style="white-space: pre-wrap; word-wrap: break-word; max-height: 300px; overflow-y: auto;">{{ detailLog?.responseData }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Refresh,
  Download,
  View
} from '@element-plus/icons-vue'
import SearchForm from '@/components/SearchForm.vue'
import Pagination from '@/components/Pagination.vue'
import { logApi } from '@/api'
import { formatDateTime } from '@/utils'
import type { SystemLogVO } from '@/types'

// 搜索表单
const searchForm = reactive({
  username: '',
  action: '',
  module: '',
  startDate: '',
  endDate: ''
})

const dateRange = ref<[string, string]>()

// 日志列表
const logs = ref<SystemLogVO[]>([])
const loading = ref(false)

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 日志详情
const detailVisible = ref(false)
const detailLog = ref<SystemLogVO>()

// 加载日志列表
const loadLogs = async () => {
  loading.value = true
  try {
    const response = await logApi.getLogList({
      ...searchForm,
      page: pagination.page,
      size: pagination.size
    })
    logs.value = response.data.records
    pagination.total = response.data.total
  } catch (error) {
    ElMessage.error('加载日志列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadLogs()
}

// 重置搜索
const handleReset = () => {
  searchForm.username = ''
  searchForm.action = ''
  searchForm.module = ''
  searchForm.startDate = ''
  searchForm.endDate = ''
  dateRange.value = undefined
  handleSearch()
}

// 日期范围变化
const handleDateChange = (value: [string, string] | null) => {
  if (value) {
    searchForm.startDate = value[0]
    searchForm.endDate = value[1]
  } else {
    searchForm.startDate = ''
    searchForm.endDate = ''
  }
}

// 查看详情
const handleView = (row: SystemLogVO) => {
  detailLog.value = row
  detailVisible.value = true
}

// 导出日志
const handleExport = async () => {
  try {
    const response = await logApi.exportLogs(searchForm)
    const blob = new Blob([response.data], { type: 'application/vnd.ms-excel' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `系统日志_${formatDateTime(new Date())}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

// 获取操作类型文本
const getActionText = (action: string) => {
  const actionMap: Record<string, string> = {
    LOGIN: '登录',
    LOGOUT: '登出',
    CREATE: '创建',
    UPDATE: '更新',
    DELETE: '删除',
    APPROVE: '审批',
    REJECT: '拒绝',
    REVOKE: '撤销'
  }
  return actionMap[action] || action
}

// 获取操作类型标签类型
const getActionType = (action: string) => {
  const typeMap: Record<string, any> = {
    LOGIN: 'success',
    LOGOUT: 'info',
    CREATE: 'primary',
    UPDATE: 'warning',
    DELETE: 'danger',
    APPROVE: 'success',
    REJECT: 'danger',
    REVOKE: 'warning'
  }
  return typeMap[action] || ''
}

// 获取模块文本
const getModuleText = (module: string) => {
  const moduleMap: Record<string, string> = {
    USER: '用户管理',
    APPLICATION: '申请管理',
    APPROVAL: '审批管理',
    CERTIFICATE: '证书管理',
    COLLEGE: '学院管理',
    TEMPLATE: '模板管理',
    SYSTEM: '系统配置'
  }
  return moduleMap[module] || module
}

// 初始化
loadLogs()
</script>

<style scoped lang="scss">
.system-log {
  .search-card,
  .toolbar-card {
    margin-bottom: 16px;
  }
}
</style>