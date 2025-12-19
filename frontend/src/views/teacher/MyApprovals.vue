<template>
  <div class="my-approvals-container">
    <el-card class="header-card">
      <div class="header-content">
        <h2>审批记录</h2>
        <el-tag type="info" size="large">
          共 {{ total }} 条记录
        </el-tag>
      </div>
    </el-card>

    <!-- 搜索表单 -->
    <SearchForm v-model="searchForm" @search="handleSearch" @reset="handleReset">
      <el-form-item label="学生姓名">
        <el-input v-model="searchForm.studentName" placeholder="请输入学生姓名" clearable />
      </el-form-item>
      <el-form-item label="审批结果">
        <el-select v-model="searchForm.decision" placeholder="请选择审批结果" clearable>
          <el-option label="全部" value="" />
          <el-option
            v-for="(label, value) in decisionOptions"
            :key="value"
            :label="label"
            :value="value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="审批时间">
        <el-date-picker
          v-model="searchForm.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
        />
      </el-form-item>
    </SearchForm>

    <!-- 审批记录列表 -->
    <el-card>
      <el-table v-loading="loading" :data="approvalList" stripe>
        <el-table-column prop="applicationId" label="申请ID" width="80" />
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="studentId" label="学号" width="150" />
        <el-table-column prop="certificateType" label="证书类型" width="150" />
        <el-table-column label="审批结果" width="100">
          <template #default="{ row }">
            <StatusTag :status="row.decision" status-type="approval" />
          </template>
        </el-table-column>
        <el-table-column prop="comments" label="审批意见" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="审批时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">
              查看详情
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

    <!-- 审批详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="审批详情"
      width="900px"
    >
      <div v-if="currentApproval" class="approval-detail">
        <!-- 审批信息 -->
        <el-descriptions title="审批信息" :column="2" border>
          <el-descriptions-item label="审批ID">
            {{ currentApproval.id }}
          </el-descriptions-item>
          <el-descriptions-item label="审批结果">
            <StatusTag :status="currentApproval.decision" status-type="approval" />
          </el-descriptions-item>
          <el-descriptions-item label="审批人">
            {{ currentApproval.approverName }}
          </el-descriptions-item>
          <el-descriptions-item label="审批时间">
            {{ formatDateTime(currentApproval.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="审批意见" :span="2">
            {{ currentApproval.comments || '无' }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentApproval.signature" label="数字签名" :span="2">
            <el-text class="signature-text" truncated>
              {{ currentApproval.signature }}
            </el-text>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 学生信息 -->
        <el-divider content-position="left">学生信息</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="学生姓名">
            {{ currentApproval.studentName }}
          </el-descriptions-item>
          <el-descriptions-item label="学号">
            {{ currentApproval.studentId }}
          </el-descriptions-item>
          <el-descriptions-item label="学院">
            {{ currentApproval.collegeName }}
          </el-descriptions-item>
          <el-descriptions-item label="专业">
            {{ currentApproval.majorName }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 申请信息 -->
        <el-divider content-position="left">申请信息</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申请ID">
            {{ currentApproval.applicationId }}
          </el-descriptions-item>
          <el-descriptions-item label="证书类型">
            {{ currentApproval.certificateType }}
          </el-descriptions-item>
          <el-descriptions-item label="申请理由" :span="2">
            {{ currentApproval.reason }}
          </el-descriptions-item>
          <el-descriptions-item label="申请时间" :span="2">
            {{ formatDateTime(currentApproval.applicationCreatedAt) }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 完整审批流程 -->
        <el-divider content-position="left">审批流程</el-divider>
        <el-timeline v-if="currentApproval.approvalHistory?.length">
          <el-timeline-item
            v-for="(item, index) in currentApproval.approvalHistory"
            :key="index"
            :timestamp="formatDateTime(item.createdAt)"
            placement="top"
            :type="getTimelineType(item.decision)"
          >
            <el-card>
              <p><strong>审批环节：</strong>{{ getApprovalStage(index) }}</p>
              <p><strong>审批人：</strong>{{ item.approverName }}</p>
              <p><strong>审批结果：</strong>
                <StatusTag :status="item.decision" status-type="approval" />
              </p>
              <p v-if="item.comments"><strong>审批意见：</strong>{{ item.comments }}</p>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无审批流程" />
      </div>

      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { approvalApi } from '@/api'
import { SearchForm, Pagination, StatusTag } from '@/components'
import { ApprovalDecision } from '@/types'
import type { Approval } from '@/types'
import { formatDateTime } from '@/utils/format'

// 搜索表单
const searchForm = reactive({
  studentName: '',
  decision: '',
  dateRange: [] as string[],
})

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
})

// 数据
const approvalList = ref<Approval[]>([])
const total = ref(0)

// 加载状态
const loading = ref(false)

// 对话框显示状态
const showDetailDialog = ref(false)

// 当前审批记录
const currentApproval = ref<Approval | null>(null)

// 审批结果选项
const decisionOptions = {
  [ApprovalDecision.APPROVED]: '通过',
  [ApprovalDecision.REJECTED]: '驳回',
  [ApprovalDecision.PENDING]: '待审批',
}

/**
 * 获取审批记录列表
 */
const fetchApprovals = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      studentName: searchForm.studentName,
      decision: searchForm.decision,
    }

    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }

    const response = await approvalApi.getMyApprovals(params)
    approvalList.value = response.records
    total.value = response.total
  } catch (error: any) {
    ElMessage.error(error.message || '获取审批记录失败')
  } finally {
    loading.value = false
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
  searchForm.decision = ''
  searchForm.dateRange = []
  pagination.page = 1
  fetchApprovals()
}

/**
 * 查看详情
 */
const handleView = async (approval: Approval) => {
  try {
    // 获取完整的审批详情和流程
    const history = await approvalApi.getApprovalHistory(approval.applicationId)
    currentApproval.value = { ...approval, approvalHistory: history }
    showDetailDialog.value = true
  } catch (error: any) {
    ElMessage.error(error.message || '获取审批详情失败')
  }
}

/**
 * 获取时间线类型
 */
const getTimelineType = (decision: ApprovalDecision) => {
  switch (decision) {
    case ApprovalDecision.APPROVED:
      return 'success'
    case ApprovalDecision.REJECTED:
      return 'danger'
    case ApprovalDecision.PENDING:
      return 'warning'
    default:
      return 'info'
  }
}

/**
 * 获取审批环节名称
 */
const getApprovalStage = (index: number) => {
  const stages = ['初审', '终审', '复审']
  return stages[index] || `第${index + 1}次审批`
}

// 初始化
onMounted(() => {
  fetchApprovals()
})
</script>

<style scoped lang="scss">
.my-approvals-container {
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

.approval-detail {
  .el-divider {
    margin: 24px 0 16px 0;
  }

  .signature-text {
    max-width: 600px;
    font-family: 'Courier New', monospace;
    font-size: 12px;
  }
}

@media (max-width: 768px) {
  .my-approvals-container {
    padding: 12px;
  }
}
</style>