<template>
  <div class="approvals-container">
    <el-card class="header-card">
      <div class="header-content">
        <h2>待审批</h2>
        <el-tag type="warning" size="large">
          待处理 {{ pendingCount }} 项
        </el-tag>
      </div>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <el-statistic title="待审批" :value="stats.pending">
            <template #prefix>
              <el-icon color="#E6A23C"><Clock /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <el-statistic title="今日已审" :value="stats.todayApproved">
            <template #prefix>
              <el-icon color="#67C23A"><CircleCheck /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <el-statistic title="本周已审" :value="stats.weekApproved">
            <template #prefix>
              <el-icon color="#409EFF"><Document /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <el-statistic title="累计审批" :value="stats.totalApproved">
            <template #prefix>
              <el-icon color="#909399"><DataAnalysis /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索表单 -->
    <SearchForm v-model="searchForm" @search="handleSearch" @reset="handleReset">
      <el-form-item label="学生姓名">
        <el-input v-model="searchForm.studentName" placeholder="请输入学生姓名" clearable />
      </el-form-item>
      <el-form-item label="证书类型">
        <el-input v-model="searchForm.certificateType" placeholder="请输入证书类型" clearable />
      </el-form-item>
    </SearchForm>

    <!-- 待审批列表 -->
    <el-card>
      <el-table v-loading="loading" :data="approvalList" stripe>
        <el-table-column prop="applicationId" label="申请ID" width="80" />
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="studentId" label="学号" width="150" />
        <el-table-column prop="certificateType" label="证书类型" width="150" />
        <el-table-column prop="reason" label="申请理由" show-overflow-tooltip />
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
            <el-button link type="success" @click="handleApprove(row, ApprovalDecision.APPROVE)">
              通过
            </el-button>
            <el-button link type="danger" @click="handleApprove(row, ApprovalDecision.REJECT)">
              驳回
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <Pagination
        v-model="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="total"
        @change="fetchApprovals"
      />
    </el-card>

    <!-- 申请详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="申请详情"
      width="900px"
    >
      <div v-if="currentApplication" class="application-detail">
        <!-- 学生信息 -->
        <el-descriptions title="学生信息" :column="2" border>
          <el-descriptions-item label="学生姓名">
            {{ currentApplication.studentName }}
          </el-descriptions-item>
          <el-descriptions-item label="学号">
            {{ currentApplication.studentId }}
          </el-descriptions-item>
          <el-descriptions-item label="学院">
            {{ currentApplication.collegeName }}
          </el-descriptions-item>
          <el-descriptions-item label="专业">
            {{ currentApplication.majorName }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 申请信息 -->
        <el-divider content-position="left">申请信息</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申请ID">
            {{ currentApplication.id }}
          </el-descriptions-item>
          <el-descriptions-item label="证书类型">
            {{ currentApplication.certificateType }}
          </el-descriptions-item>
          <el-descriptions-item label="申请理由" :span="2">
            {{ currentApplication.reason }}
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">
            {{ currentApplication.remarks || '无' }}
          </el-descriptions-item>
          <el-descriptions-item label="申请时间" :span="2">
            {{ formatDateTime(currentApplication.createdAt) }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 支撑材料 -->
        <el-divider content-position="left">支撑材料</el-divider>
        <div v-if="currentApplication.attachments?.length" class="attachments">
          <el-link
            v-for="(file, index) in currentApplication.attachments"
            :key="index"
            type="primary"
            :icon="Document"
            :href="file.url"
            target="_blank"
          >
            {{ file.name }}
          </el-link>
        </div>
        <el-empty v-else description="无支撑材料" />

        <!-- 审批历史 -->
        <el-divider content-position="left">审批历史</el-divider>
        <el-timeline v-if="currentApplication.approvalHistory?.length">
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
      </div>

      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
        <el-button
          type="success"
          :icon="CircleCheck"
          @click="handleApprove(currentApplication!, ApprovalDecision.APPROVE)"
        >
          通过
        </el-button>
        <el-button
          type="danger"
          :icon="CircleClose"
          @click="handleApprove(currentApplication!, ApprovalDecision.REJECT)"
        >
          驳回
        </el-button>
      </template>
    </el-dialog>

    <!-- 审批对话框 -->
    <el-dialog
      v-model="showApprovalDialog"
      :title="approvalDecision === ApprovalDecision.APPROVE ? '审批通过' : '审批驳回'"
      width="600px"
    >
      <el-form
        ref="approvalFormRef"
        :model="approvalForm"
        :rules="approvalRules"
        label-width="100px"
      >
        <el-form-item label="审批意见" prop="comment">
          <el-input
            v-model="approvalForm.comment"
            type="textarea"
            :rows="5"
            :placeholder="
              approvalDecision === ApprovalDecision.APPROVE
                ? '请输入审批意见（选填）'
                : '请输入驳回理由（必填）'
            "
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showApprovalDialog = false">取消</el-button>
        <el-button
          :type="approvalDecision === ApprovalDecision.APPROVE ? 'success' : 'danger'"
          :loading="submitting"
          @click="handleSubmitApproval"
        >
          确认{{ approvalDecision === ApprovalDecision.APPROVE ? '通过' : '驳回' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
  Clock,
  CircleCheck,
  CircleClose,
  Document,
  DataAnalysis,
} from '@element-plus/icons-vue'
import { approvalApi } from '@/api'
import { SearchForm, Pagination, StatusTag } from '@/components'
import { ApprovalDecision } from '@/types'
import type { Application } from '@/types'
import { formatDateTime } from '@/utils/format'
import { requiredRule } from '@/utils/validate'

// 搜索表单
const searchForm = reactive({
  studentName: '',
  certificateType: '',
})

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
})

// 数据
const approvalList = ref<Application[]>([])
const total = ref(0)
const pendingCount = computed(() => total.value)

// 统计数据
const stats = reactive({
  pending: 0,
  todayApproved: 0,
  weekApproved: 0,
  totalApproved: 0,
})

// 加载状态
const loading = ref(false)
const submitting = ref(false)

// 对话框显示状态
const showDetailDialog = ref(false)
const showApprovalDialog = ref(false)

// 当前操作的申请
const currentApplication = ref<Application | null>(null)
const approvalDecision = ref<ApprovalDecision>(ApprovalDecision.APPROVE)

// 表单引用
const approvalFormRef = ref<FormInstance>()

// 审批表单
const approvalForm = reactive({
  comment: '',
})

// 审批表单验证规则
const approvalRules = computed<FormRules>(() => ({
  comment:
    approvalDecision.value === ApprovalDecision.REJECT
      ? [requiredRule('请输入驳回理由')]
      : [],
}))

/**
 * 获取待审批列表
 */
const fetchApprovals = async () => {
  loading.value = true
  try {
    const response = await approvalApi.getPendingList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      ...searchForm,
    })
    approvalList.value = response.content
    total.value = response.totalElements
    stats.pending = response.total
  } catch (error: any) {
    ElMessage.error(error.message || '获取待审批列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 获取统计数据
 */
const fetchStats = async () => {
  try {
    // 这里可以调用统计API获取数据
    // const response = await approvalApi.getStats()
    // stats.todayApproved = response.todayApproved
    // stats.weekApproved = response.weekApproved
    // stats.totalApproved = response.totalApproved
  } catch (error) {
    // 忽略错误
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  pagination.page = 1
  fetchApprovals()
}

/**
 * 重置
 */
const handleReset = () => {
  searchForm.studentName = ''
  searchForm.certificateType = ''
  pagination.page = 1
  fetchApprovals()
}

/**
 * 查看详情
 */
const handleView = async (application: Application) => {
  try {
    // 获取完整的申请详情
    const response = await approvalApi.getApprovalHistory(application.id)
    currentApplication.value = { ...application, approvalHistory: response }
    showDetailDialog.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '获取申请详情失败')
  }
}

/**
 * 审批
 */
const handleApprove = (application: Application, decision: ApprovalDecision) => {
  currentApplication.value = application
  approvalDecision.value = decision
  approvalForm.comment = ''
  showDetailDialog.value = false
  showApprovalDialog.value = true
}

/**
 * 提交审批
 */
const handleSubmitApproval = async () => {
  if (!approvalFormRef.value || !currentApplication.value) return

  try {
    await approvalFormRef.value.validate()
    submitting.value = true

    await approvalApi.approve(currentApplication.value.id, {
      decision: approvalDecision.value,
      comment: approvalForm.comment,
    })

    ElMessage.success(
      approvalDecision.value === ApprovalDecision.APPROVE ? '审批通过' : '已驳回申请'
    )

    showApprovalDialog.value = false
    fetchApprovals()
    fetchStats()
  } catch (error: any) {
    ElMessage.error(error.message || '审批失败')
  } finally {
    submitting.value = false
  }
}

// 初始化
onMounted(() => {
  fetchApprovals()
  fetchStats()
})
</script>

<style scoped lang="scss">
.approvals-container {
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

  .stats-row {
    margin-bottom: 20px;

    .el-col {
      margin-bottom: 20px;
    }
  }
}

.application-detail {
  .el-divider {
    margin: 24px 0 16px 0;
  }

  .attachments {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
}

@media (max-width: 768px) {
  .approvals-container {
    padding: 12px;

    .stats-row {
      .el-col {
        margin-bottom: 12px;
      }
    }
  }
}
</style>