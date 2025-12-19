<template>
  <div class="certificates-container">
    <el-card class="header-card">
      <div class="header-content">
        <h2>我的证书</h2>
        <el-tag type="success" size="large">
          共 {{ certificateStore.total }} 张证书
        </el-tag>
      </div>
    </el-card>

    <!-- 搜索表单 -->
    <SearchForm v-model="searchForm" @search="handleSearch" @reset="handleReset">
      <el-form-item label="证书类型">
        <el-input v-model="searchForm.certificateType" placeholder="请输入证书类型" clearable />
      </el-form-item>
      <el-form-item label="证书状态">
        <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
          <el-option label="全部" value="" />
          <el-option
            v-for="(label, value) in statusOptions"
            :key="value"
            :label="label"
            :value="value"
          />
        </el-select>
      </el-form-item>
    </SearchForm>

    <!-- 证书列表 -->
    <el-card>
      <el-table v-loading="loading" :data="certificateStore.certificates" stripe>
        <el-table-column prop="certificateNumber" label="证书编号" width="200" />
        <el-table-column prop="certificateType" label="证书类型" width="150" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <StatusTag :status="row.status" status-type="certificate" />
          </template>
        </el-table-column>
        <el-table-column prop="issueDate" label="颁发日期" width="120">
          <template #default="{ row }">
            {{ formatDate(row.issueDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="blockchainHash" label="区块链哈希" show-overflow-tooltip>
          <template #default="{ row }">
            <el-text class="hash-text" truncated>
              {{ row.blockchainHash }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">
              查看
            </el-button>
            <el-button link type="success" @click="handleDownload(row)">
              下载
            </el-button>
            <el-button
              v-if="row.status === CertificateStatus.VALID"
              link
              type="danger"
              @click="handleRevoke(row)"
            >
              撤销
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <Pagination
        v-model="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="certificateStore.total"
        @change="fetchCertificates"
      />
    </el-card>

    <!-- 证书详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="证书详情"
      width="900px"
    >
      <div v-if="currentCertificate" class="certificate-detail">
        <!-- 基本信息 -->
        <el-descriptions title="基本信息" :column="2" border>
          <el-descriptions-item label="证书编号" :span="2">
            {{ currentCertificate.certificateNumber }}
          </el-descriptions-item>
          <el-descriptions-item label="证书类型">
            {{ currentCertificate.certificateType }}
          </el-descriptions-item>
          <el-descriptions-item label="证书状态">
            <StatusTag :status="currentCertificate.status" status-type="certificate" />
          </el-descriptions-item>
          <el-descriptions-item label="学生姓名">
            {{ currentCertificate.studentName }}
          </el-descriptions-item>
          <el-descriptions-item label="学号">
            {{ currentCertificate.studentId }}
          </el-descriptions-item>
          <el-descriptions-item label="学院">
            {{ currentCertificate.collegeName }}
          </el-descriptions-item>
          <el-descriptions-item label="专业">
            {{ currentCertificate.majorName }}
          </el-descriptions-item>
          <el-descriptions-item label="颁发日期">
            {{ formatDate(currentCertificate.issueDate) }}
          </el-descriptions-item>
          <el-descriptions-item label="有效期至">
            {{ currentCertificate.expiryDate ? formatDate(currentCertificate.expiryDate) : '永久有效' }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 区块链信息 -->
        <el-divider content-position="left">区块链信息</el-divider>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="区块链哈希">
            <el-text class="hash-text" truncated>
              {{ currentCertificate.blockchainHash }}
            </el-text>
            <el-button
              link
              type="primary"
              :icon="CopyDocument"
              @click="copyToClipboard(currentCertificate.blockchainHash)"
            >
              复制
            </el-button>
          </el-descriptions-item>
          <el-descriptions-item label="交易哈希">
            <el-text class="hash-text" truncated>
              {{ currentCertificate.transactionHash }}
            </el-text>
            <el-button
              link
              type="primary"
              :icon="CopyDocument"
              @click="copyToClipboard(currentCertificate.transactionHash)"
            >
              复制
            </el-button>
          </el-descriptions-item>
          <el-descriptions-item label="区块高度">
            {{ currentCertificate.blockNumber }}
          </el-descriptions-item>
          <el-descriptions-item label="上链时间">
            {{ formatDateTime(currentCertificate.blockchainTimestamp) }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- IPFS 信息 -->
        <el-divider content-position="left">IPFS 信息</el-divider>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="IPFS 哈希">
            <el-text class="hash-text" truncated>
              {{ currentCertificate.ipfsHash }}
            </el-text>
            <el-button
              link
              type="primary"
              :icon="CopyDocument"
              @click="copyToClipboard(currentCertificate.ipfsHash)"
            >
              复制
            </el-button>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 证书预览 -->
        <el-divider content-position="left">证书预览</el-divider>
        <div class="certificate-preview">
          <el-image
            v-if="currentCertificate.previewUrl"
            :src="currentCertificate.previewUrl"
            fit="contain"
            :preview-src-list="[currentCertificate.previewUrl]"
          />
          <el-empty v-else description="暂无预览" />
        </div>
      </div>

      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
        <el-button
          type="primary"
          :icon="Download"
          @click="handleDownload(currentCertificate!)"
        >
          下载证书
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, CopyDocument } from '@element-plus/icons-vue'
import { useCertificateStore } from '@/stores/certificate'
import { SearchForm, Pagination, StatusTag } from '@/components'
import { CertificateStatus } from '@/types'
import type { Certificate } from '@/types'
import { formatDate, formatDateTime } from '@/utils/format'

const certificateStore = useCertificateStore()

// 搜索表单
const searchForm = reactive({
  certificateType: '',
  status: '',
})

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
})

// 加载状态
const loading = ref(false)

// 对话框显示状态
const showDetailDialog = ref(false)

// 当前证书
const currentCertificate = ref<Certificate | null>(null)

// 状态选项
const statusOptions = {
  [CertificateStatus.VALID]: '有效',
  [CertificateStatus.REVOKED]: '已撤销',
  [CertificateStatus.EXPIRED]: '已过期',
}

/**
 * 获取证书列表
 */
const fetchCertificates = async () => {
  loading.value = true
  try {
    await certificateStore.fetchMyCertificates({
      page: pagination.page,
      pageSize: pagination.pageSize,
      ...searchForm,
    })
  } catch (error: any) {
    ElMessage.error(error.message || '获取证书列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  pagination.page = 1
  fetchCertificates()
}

/**
 * 重置
 */
const handleReset = () => {
  searchForm.certificateType = ''
  searchForm.status = ''
  pagination.page = 1
  fetchCertificates()
}

/**
 * 查看详情
 */
const handleView = async (certificate: Certificate) => {
  try {
    await certificateStore.fetchCertificateDetail(certificate.id)
    currentCertificate.value = certificateStore.currentCertificate
    showDetailDialog.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '获取证书详情失败')
  }
}

/**
 * 下载证书
 */
const handleDownload = async (certificate: Certificate) => {
  try {
    await certificateStore.downloadCertificate(certificate.id)
    ElMessage.success('证书下载成功')
  } catch (error: any) {
    ElMessage.error(error.message || '下载失败')
  }
}

/**
 * 撤销证书
 */
const handleRevoke = async (certificate: Certificate) => {
  try {
    await ElMessageBox.prompt('请输入撤销原因', '撤销证书', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '请输入撤销原因',
    })

    await certificateStore.revokeCertificate(certificate.id, '用户主动撤销')
    ElMessage.success('证书已撤销')
    fetchCertificates()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '撤销失败')
    }
  }
}

/**
 * 复制到剪贴板
 */
const copyToClipboard = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

// 初始化
onMounted(() => {
  fetchCertificates()
})
</script>

<style scoped lang="scss">
.certificates-container {
  padding: 20px;

  .header-card {
    margin-bottom: 20px;

    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;

      h2 {
        margin: 0;
        font-size: 20px;
        font-weight: 600;
      }
    }
  }

  .hash-text {
    max-width: 300px;
    font-family: 'Courier New', monospace;
    font-size: 12px;
  }
}

.certificate-detail {
  .hash-text {
    max-width: 500px;
    font-family: 'Courier New', monospace;
    font-size: 12px;
  }

  .el-divider {
    margin: 24px 0 16px 0;
  }

  .certificate-preview {
    display: flex;
    justify-content: center;
    padding: 20px;
    background: var(--el-fill-color-light);
    border-radius: 4px;

    .el-image {
      max-width: 100%;
      max-height: 500px;
    }
  }
}
</style>