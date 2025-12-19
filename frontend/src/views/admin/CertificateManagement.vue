<template>
  <div class="certificate-management">
    <!-- 搜索表单 -->
    <el-card class="search-card" shadow="never">
      <SearchForm
        :model="searchForm"
        :loading="loading"
        @search="handleSearch"
        @reset="handleReset"
      >
        <el-form-item label="证书编号">
          <el-input
            v-model="searchForm.certificateNumber"
            placeholder="请输入证书编号"
            clearable
          />
        </el-form-item>
        <el-form-item label="学生姓名">
          <el-input
            v-model="searchForm.studentName"
            placeholder="请输入学生姓名"
            clearable
          />
        </el-form-item>
        <el-form-item label="学院">
          <el-select
            v-model="searchForm.collegeId"
            placeholder="请选择学院"
            clearable
            filterable
          >
            <el-option
              v-for="college in colleges"
              :key="college.id"
              :label="college.name"
              :value="college.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="证书类型">
          <el-select
            v-model="searchForm.type"
            placeholder="请选择证书类型"
            clearable
          >
            <el-option label="毕业证书" value="GRADUATION" />
            <el-option label="学位证书" value="DEGREE" />
            <el-option label="荣誉证书" value="HONOR" />
            <el-option label="其他证书" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
          >
            <el-option label="有效" value="VALID" />
            <el-option label="已撤销" value="REVOKED" />
          </el-select>
        </el-form-item>
        <el-form-item label="颁发日期">
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

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="statistics-row">
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="证书总数" :value="statistics.total">
            <template #prefix>
              <el-icon color="#409EFF">
                <Document />
              </el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="有效证书" :value="statistics.valid">
            <template #prefix>
              <el-icon color="#67C23A">
                <CircleCheck />
              </el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="已撤销" :value="statistics.revoked">
            <template #prefix>
              <el-icon color="#F56C6C">
                <CircleClose />
              </el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="本月新增" :value="statistics.thisMonth">
            <template #prefix>
              <el-icon color="#E6A23C">
                <TrendCharts />
              </el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <!-- 操作按钮 -->
    <el-card class="toolbar-card" shadow="never">
      <el-button
        :icon="Refresh"
        @click="loadCertificates"
      >
        刷新
      </el-button>
      <el-button
        :icon="Download"
        @click="handleExport"
      >
        导出数据
      </el-button>
    </el-card>

    <!-- 证书列表 -->
    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="certificates"
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
          prop="certificateNumber"
          label="证书编号"
          width="180"
          align="center"
        />
        <el-table-column
          prop="studentName"
          label="学生姓名"
          width="120"
        />
        <el-table-column
          prop="studentId"
          label="学号"
          width="120"
        />
        <el-table-column
          prop="collegeName"
          label="学院"
          min-width="150"
        />
        <el-table-column
          prop="majorName"
          label="专业"
          min-width="150"
        />
        <el-table-column
          prop="type"
          label="证书类型"
          width="120"
          align="center"
        >
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="status"
          label="状态"
          width="100"
          align="center"
        >
          <template #default="{ row }">
            <StatusTag :status="row.status" type="certificate" />
          </template>
        </el-table-column>
        <el-table-column
          prop="issueDate"
          label="颁发日期"
          width="120"
          align="center"
        >
          <template #default="{ row }">
            {{ formatDate(row.issueDate) }}
          </template>
        </el-table-column>
        <el-table-column
          prop="createdAt"
          label="创建时间"
          width="180"
          align="center"
        >
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="200"
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
              查看
            </el-button>
            <el-button
              type="success"
              size="small"
              link
              :icon="Download"
              @click="handleDownload(row)"
            >
              下载
            </el-button>
            <el-button
              v-if="row.status === 'VALID'"
              type="danger"
              size="small"
              link
              :icon="CircleClose"
              @click="handleRevoke(row)"
            >
              撤销
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <Pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        @change="loadCertificates"
      />
    </el-card>

    <!-- 证书详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="证书详情"
      width="900px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="证书编号">
          {{ detailCertificate?.certificateNumber }}
        </el-descriptions-item>
        <el-descriptions-item label="证书类型">
          <el-tag :type="getTypeTagType(detailCertificate?.type || '')">
            {{ getTypeText(detailCertificate?.type || '') }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="学生姓名">
          {{ detailCertificate?.studentName }}
        </el-descriptions-item>
        <el-descriptions-item label="学号">
          {{ detailCertificate?.studentId }}
        </el-descriptions-item>
        <el-descriptions-item label="学院">
          {{ detailCertificate?.collegeName }}
        </el-descriptions-item>
        <el-descriptions-item label="专业">
          {{ detailCertificate?.majorName }}
        </el-descriptions-item>
        <el-descriptions-item label="颁发日期">
          {{ formatDate(detailCertificate?.issueDate) }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <StatusTag :status="detailCertificate?.status || ''" type="certificate" />
        </el-descriptions-item>
        <el-descriptions-item label="区块链哈希" :span="2">
          <el-text type="primary" style="word-break: break-all;">
            {{ detailCertificate?.blockchainHash }}
          </el-text>
          <el-button
            type="primary"
            size="small"
            link
            :icon="CopyDocument"
            @click="handleCopy(detailCertificate?.blockchainHash)"
          >
            复制
          </el-button>
        </el-descriptions-item>
        <el-descriptions-item label="IPFS哈希" :span="2">
          <el-text type="success" style="word-break: break-all;">
            {{ detailCertificate?.ipfsHash }}
          </el-text>
          <el-button
            type="success"
            size="small"
            link
            :icon="CopyDocument"
            @click="handleCopy(detailCertificate?.ipfsHash)"
          >
            复制
          </el-button>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDateTime(detailCertificate?.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间">
          {{ formatDateTime(detailCertificate?.updatedAt) }}
        </el-descriptions-item>
        <el-descriptions-item v-if="detailCertificate?.revokedAt" label="撤销时间" :span="2">
          {{ formatDateTime(detailCertificate?.revokedAt) }}
        </el-descriptions-item>
        <el-descriptions-item v-if="detailCertificate?.revokeReason" label="撤销原因" :span="2">
          {{ detailCertificate?.revokeReason }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 证书内容预览 -->
      <el-divider>证书内容</el-divider>
      <div class="certificate-preview">
        <pre>{{ detailCertificate?.content }}</pre>
      </div>
    </el-dialog>

    <!-- 撤销证书对话框 -->
    <el-dialog
      v-model="revokeVisible"
      title="撤销证书"
      width="500px"
    >
      <el-form
        ref="revokeFormRef"
        :model="revokeForm"
        :rules="revokeRules"
        label-width="100px"
      >
        <el-alert
          title="警告"
          type="warning"
          description="撤销证书后将无法恢复，请谨慎操作！"
          :closable="false"
          show-icon
          style="margin-bottom: 20px"
        />
        <el-form-item label="撤销原因" prop="reason">
          <el-input
            v-model="revokeForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入撤销原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="revokeVisible = false">取消</el-button>
        <el-button
          type="danger"
          :loading="submitting"
          @click="handleRevokeSubmit"
        >
          确认撤销
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Refresh,
  Download,
  View,
  CircleClose,
  CopyDocument,
  Document,
  CircleCheck,
  TrendCharts
} from '@element-plus/icons-vue'
import SearchForm from '@/components/SearchForm.vue'
import Pagination from '@/components/Pagination.vue'
import StatusTag from '@/components/StatusTag.vue'
import { certificateApi, collegeApi } from '@/api'
import { formatDateTime, formatDate } from '@/utils'
import type { CertificateVO, CollegeResponse } from '@/types'

// 搜索表单
const searchForm = reactive({
  certificateNumber: '',
  studentName: '',
  collegeId: undefined as number | undefined,
  type: '',
  status: '',
  startDate: '',
  endDate: ''
})

const dateRange = ref<[string, string]>()

// 证书列表
const certificates = ref<CertificateVO[]>([])
const loading = ref(false)

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 学院列表
const colleges = ref<CollegeResponse[]>([])

// 统计数据
const statistics = reactive({
  total: 0,
  valid: 0,
  revoked: 0,
  thisMonth: 0
})

// 证书详情
const detailVisible = ref(false)
const detailCertificate = ref<CertificateVO>()

// 撤销证书
const revokeVisible = ref(false)
const revokeFormRef = ref<FormInstance>()
const revokeForm = reactive({
  certificateId: 0,
  reason: ''
})
const submitting = ref(false)

const revokeRules: FormRules = {
  reason: [
    { required: true, message: '请输入撤销原因', trigger: 'blur' },
    { min: 10, message: '撤销原因至少10个字符', trigger: 'blur' }
  ]
}

// 加载证书列表
const loadCertificates = async () => {
  loading.value = true
  try {
    const response = await certificateApi.getCertificateList({
      ...searchForm,
      page: pagination.page,
      size: pagination.size
    })
    certificates.value = response.data.records
    pagination.total = response.data.total
  } catch (error) {
    ElMessage.error('加载证书列表失败')
  } finally {
    loading.value = false
  }
}

// 加载学院列表
const loadColleges = async () => {
  try {
    const response = await collegeApi.getCollegeList({ page: 1, size: 1000 })
    colleges.value = response.data.records
  } catch (error) {
    ElMessage.error('加载学院列表失败')
  }
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    const response = await certificateApi.getStatistics()
    Object.assign(statistics, response.data)
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadCertificates()
}

// 重置搜索
const handleReset = () => {
  searchForm.certificateNumber = ''
  searchForm.studentName = ''
  searchForm.collegeId = undefined
  searchForm.type = ''
  searchForm.status = ''
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
const handleView = async (row: CertificateVO) => {
  try {
    const response = await certificateApi.getCertificateDetail(row.id)
    detailCertificate.value = response.data
    detailVisible.value = true
  } catch (error) {
    ElMessage.error('加载证书详情失败')
  }
}

// 下载证书
const handleDownload = async (row: CertificateVO) => {
  try {
    const response = await certificateApi.downloadCertificate(row.id)
    const blob = new Blob([response.data], { type: 'application/pdf' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${row.certificateNumber}.pdf`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('下载成功')
  } catch (error) {
    ElMessage.error('下载失败')
  }
}

// 撤销证书
const handleRevoke = (row: CertificateVO) => {
  revokeForm.certificateId = row.id
  revokeForm.reason = ''
  revokeVisible.value = true
}

// 提交撤销
const handleRevokeSubmit = async () => {
  if (!revokeFormRef.value) return
  
  await revokeFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      await certificateApi.revokeCertificate(revokeForm.certificateId, {
        reason: revokeForm.reason
      })
      ElMessage.success('撤销证书成功')
      revokeVisible.value = false
      loadCertificates()
      loadStatistics()
    } catch (error) {
      ElMessage.error('撤销证书失败')
    } finally {
      submitting.value = false
    }
  })
}

// 导出数据
const handleExport = async () => {
  try {
    const response = await certificateApi.exportCertificates(searchForm)
    const blob = new Blob([response.data], { type: 'application/vnd.ms-excel' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `证书数据_${formatDateTime(new Date())}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

// 复制文本
const handleCopy = (text?: string) => {
  if (!text) return
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('复制成功')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

// 获取类型文本
const getTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    GRADUATION: '毕业证书',
    DEGREE: '学位证书',
    HONOR: '荣誉证书',
    OTHER: '其他证书'
  }
  return typeMap[type] || type
}

// 获取类型标签类型
const getTypeTagType = (type: string) => {
  const typeMap: Record<string, any> = {
    GRADUATION: 'success',
    DEGREE: 'primary',
    HONOR: 'warning',
    OTHER: 'info'
  }
  return typeMap[type] || ''
}

onMounted(() => {
  loadCertificates()
  loadColleges()
  loadStatistics()
})
</script>

<style scoped lang="scss">
.certificate-management {
  .search-card,
  .toolbar-card {
    margin-bottom: 16px;
  }

  .statistics-row {
    margin-bottom: 16px;
  }

  .certificate-preview {
    padding: 16px;
    background-color: #f5f7fa;
    border-radius: 4px;
    max-height: 400px;
    overflow-y: auto;

    pre {
      margin: 0;
      white-space: pre-wrap;
      word-wrap: break-word;
      font-family: 'Courier New', monospace;
      font-size: 14px;
      line-height: 1.6;
    }
  }
}
</style>