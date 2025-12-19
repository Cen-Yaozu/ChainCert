<template>
  <div class="applications-container">
    <el-card class="header-card">
      <div class="header-content">
        <h2>我的申请</h2>
        <el-button type="primary" :icon="Plus" @click="showCreateDialog = true">
          新建申请
        </el-button>
      </div>
    </el-card>

    <!-- 搜索表单 -->
    <SearchForm v-model="searchForm" @search="handleSearch" @reset="handleReset">
      <el-form-item label="申请状态">
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
      <el-form-item label="证书类型">
        <el-input v-model="searchForm.certificateType" placeholder="请输入证书类型" clearable />
      </el-form-item>
    </SearchForm>

    <!-- 申请列表 -->
    <el-card>
      <el-table v-loading="loading" :data="applicationStore.applications" stripe>
        <el-table-column prop="id" label="申请ID" width="80" />
        <el-table-column prop="certificateType" label="证书类型" width="150" />
        <el-table-column prop="reason" label="申请理由" show-overflow-tooltip />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <StatusTag :status="row.status" status-type="application" />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">
              查看
            </el-button>
            <el-button
              v-if="row.status === ApplicationStatus.PENDING"
              link
              type="warning"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="canCancel(row.status)"
              link
              type="danger"
              @click="handleCancel(row)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <Pagination
        v-model="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="applicationStore.total"
        @change="fetchApplications"
      />
    </el-card>

    <!-- 新建/编辑申请对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      :title="editingApplication ? '编辑申请' : '新建申请'"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="applicationForm"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="证书类型" prop="certificateType">
          <el-input
            v-model="applicationForm.certificateType"
            placeholder="请输入证书类型，如：毕业证书、学位证书"
          />
        </el-form-item>

        <el-form-item label="申请理由" prop="reason">
          <el-input
            v-model="applicationForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入申请理由"
          />
        </el-form-item>

        <el-form-item label="证明文件" prop="proofDocuments">
          <FileUpload
            v-model="applicationForm.proofDocuments"
            :multiple="true"
            :limit="3"
            :allowed-types="['application/pdf', 'image/jpeg', 'image/png']"
            :max-size="10"
            tip="支持 PDF、JPG、PNG 格式，单个文件不超过10MB，最多3个文件"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          提交申请
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看申请详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="申请详情"
      width="800px"
    >
      <el-descriptions v-if="currentApplication" :column="2" border>
        <el-descriptions-item label="申请ID">
          {{ currentApplication.id }}
        </el-descriptions-item>
        <el-descriptions-item label="证书类型">
          {{ currentApplication.certificateType }}
        </el-descriptions-item>
        <el-descriptions-item label="申请状态" :span="2">
          <StatusTag :status="currentApplication.status" status-type="application" />
        </el-descriptions-item>
        <el-descriptions-item label="申请理由" :span="2">
          {{ currentApplication.reason }}
        </el-descriptions-item>
        <el-descriptions-item label="申请时间" :span="2">
          {{ formatDateTime(currentApplication.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item
          v-if="currentApplication.updatedAt"
          label="更新时间"
          :span="2"
        >
          {{ formatDateTime(currentApplication.updatedAt) }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 审批历史 -->
      <el-divider content-position="left">审批历史</el-divider>
      <el-timeline v-if="currentApplication?.approvalHistory?.length">
        <el-timeline-item
          v-for="(item, index) in currentApplication.approvalHistory"
          :key="index"
          :timestamp="formatDateTime(item.createdAt)"
          placement="top"
        >
          <el-card>
            <p><strong>审批人：</strong>{{ item.approverName }}</p>
            <p><strong>审批结果：</strong>
              <StatusTag :status="item.decision" status-type="approval" />
            </p>
            <p v-if="item.comments"><strong>审批意见：</strong>{{ item.comments }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无审批记录" />

      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useApplicationStore } from '@/stores/application'
import { SearchForm, Pagination, StatusTag, FileUpload } from '@/components'
import { ApplicationStatus } from '@/types'
import type { Application } from '@/types'
import { formatDateTime } from '@/utils/format'
import { requiredRule } from '@/utils/validate'

const applicationStore = useApplicationStore()

// 搜索表单
const searchForm = reactive({
  status: '',
  certificateType: '',
})

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
})

// 加载状态
const loading = ref(false)
const submitting = ref(false)

// 对话框显示状态
const showCreateDialog = ref(false)
const showDetailDialog = ref(false)

// 当前操作的申请
const currentApplication = ref<Application | null>(null)
const editingApplication = ref<Application | null>(null)

// 表单引用
const formRef = ref<FormInstance>()

// 申请表单
const applicationForm = reactive({
  certificateType: '',
  reason: '',
  proofDocuments: [] as File[],
})

// 状态选项
const statusOptions = {
  [ApplicationStatus.PENDING]: '待审批',
  [ApplicationStatus.COLLEGE_APPROVED]: '学院已通过',
  [ApplicationStatus.COLLEGE_REJECTED]: '学院已驳回',
  [ApplicationStatus.SCHOOL_APPROVED]: '学校已通过',
  [ApplicationStatus.SCHOOL_REJECTED]: '学校已驳回',
  [ApplicationStatus.COMPLETED]: '已完成',
  [ApplicationStatus.CANCELLED]: '已取消',
}

// 表单验证规则
const formRules: FormRules = {
  certificateType: [requiredRule('请输入证书类型')],
  reason: [requiredRule('请输入申请理由')],
}

/**
 * 获取申请列表
 */
const fetchApplications = async () => {
  loading.value = true
  try {
    await applicationStore.fetchMyApplications({
      page: pagination.page,
      pageSize: pagination.pageSize,
      ...searchForm,
    })
  } catch (error: any) {
    ElMessage.error(error.message || '获取申请列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  pagination.page = 1
  fetchApplications()
}

/**
 * 重置
 */
const handleReset = () => {
  searchForm.status = ''
  searchForm.certificateType = ''
  pagination.page = 1
  fetchApplications()
}

/**
 * 查看详情
 */
const handleView = async (application: Application) => {
  try {
    await applicationStore.fetchApplicationDetail(application.id)
    currentApplication.value = applicationStore.currentApplication
    showDetailDialog.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '获取申请详情失败')
  }
}

/**
 * 编辑申请
 */
const handleEdit = (application: Application) => {
  editingApplication.value = application
  applicationForm.certificateType = application.certificateType
  applicationForm.reason = application.reason
  applicationForm.proofDocuments = []
  showCreateDialog.value = true
}

/**
 * 提交申请
 */
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    submitting.value = true

    if (editingApplication.value) {
      // 编辑
      await applicationStore.createApplication({
        ...applicationForm,
        id: editingApplication.value.id,
      })
      ElMessage.success('申请更新成功')
    } else {
      // 新建
      await applicationStore.createApplication(applicationForm)
      ElMessage.success('申请提交成功')
    }

    showCreateDialog.value = false
    resetForm()
    fetchApplications()
  } catch (error: any) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

/**
 * 取消申请
 */
const handleCancel = async (application: Application) => {
  try {
    await ElMessageBox.confirm('确定要取消该申请吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    await applicationStore.cancelApplication(application.id)
    ElMessage.success('申请已取消')
    fetchApplications()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '取消失败')
    }
  }
}

/**
 * 判断是否可以取消
 */
const canCancel = (status: ApplicationStatus) => {
  return [
    ApplicationStatus.PENDING,
    ApplicationStatus.COLLEGE_APPROVED,
  ].includes(status)
}

/**
 * 重置表单
 */
const resetForm = () => {
  applicationForm.certificateType = ''
  applicationForm.reason = ''
  applicationForm.proofDocuments = []
  editingApplication.value = null
  formRef.value?.resetFields()
}

// 初始化
onMounted(() => {
  fetchApplications()
})
</script>

<style scoped lang="scss">
.applications-container {
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
}
</style>