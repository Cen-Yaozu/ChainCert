<template>
  <div class="dashboard-container">
    <!-- 欢迎卡片 -->
    <el-card class="welcome-card">
      <template #header>
        <div class="card-header">
          <span>欢迎使用区块链证书存证系统</span>
        </div>
      </template>
      <div class="welcome-content">
        <el-avatar :size="80" :icon="UserFilled" />
        <div class="user-info">
          <h2>{{ greeting }}，{{ authStore.user?.realName || authStore.user?.username }}！</h2>
          <p class="role-text">{{ getRoleText(authStore.user?.role) }}</p>
        </div>
      </div>
    </el-card>

    <!-- 统计卡片 - 学生端 -->
    <div v-if="authStore.user?.role === UserRole.STUDENT" class="stats-cards">
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-icon primary">
            <el-icon :size="32"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ studentStats.totalApplications }}</div>
            <div class="stat-label">申请总数</div>
          </div>
        </div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-icon warning">
            <el-icon :size="32"><Clock /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ studentStats.pendingApplications }}</div>
            <div class="stat-label">待审批</div>
          </div>
        </div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-icon success">
            <el-icon :size="32"><Medal /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ studentStats.totalCertificates }}</div>
            <div class="stat-label">已获证书</div>
          </div>
        </div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-icon danger">
            <el-icon :size="32"><CircleClose /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ studentStats.rejectedApplications }}</div>
            <div class="stat-label">已驳回</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 统计卡片 - 教师端 -->
    <div v-if="isTeacher" class="stats-cards">
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-icon warning">
            <el-icon :size="32"><Bell /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ teacherStats.pendingApprovals }}</div>
            <div class="stat-label">待我审批</div>
          </div>
        </div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-icon success">
            <el-icon :size="32"><CircleCheck /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ teacherStats.approvedCount }}</div>
            <div class="stat-label">我已通过</div>
          </div>
        </div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-icon danger">
            <el-icon :size="32"><CircleClose /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ teacherStats.rejectedCount }}</div>
            <div class="stat-label">我已驳回</div>
          </div>
        </div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-icon primary">
            <el-icon :size="32"><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ teacherStats.totalApprovals }}</div>
            <div class="stat-label">总审批量</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 统计卡片 - 管理员端 -->
    <div v-if="authStore.user?.role === UserRole.SYSTEM_ADMIN" class="stats-cards">
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-icon primary">
            <el-icon :size="32"><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ adminStats.totalUsers }}</div>
            <div class="stat-label">用户总数</div>
          </div>
        </div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-icon success">
            <el-icon :size="32"><Medal /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ adminStats.totalCertificates }}</div>
            <div class="stat-label">证书总数</div>
          </div>
        </div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-icon warning">
            <el-icon :size="32"><School /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ adminStats.totalColleges }}</div>
            <div class="stat-label">学院数量</div>
          </div>
        </div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-icon info">
            <el-icon :size="32"><Connection /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ adminStats.blockHeight }}</div>
            <div class="stat-label">区块高度</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 快捷入口 -->
    <div class="quick-actions">
      <el-card
        v-for="action in quickActions"
        :key="action.path"
        class="action-card"
        shadow="hover"
        @click="router.push(action.path)"
      >
        <div class="action-content">
          <el-icon :size="40" :color="action.color">
            <component :is="action.icon" />
          </el-icon>
          <div class="action-info">
            <h3>{{ action.title }}</h3>
            <p>{{ action.description }}</p>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 系统信息 -->
    <el-card class="system-info">
      <template #header>
        <div class="card-header">
          <span>系统信息</span>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="系统名称">
          区块链证书存证系统
        </el-descriptions-item>
        <el-descriptions-item label="版本号">
          v1.0.0
        </el-descriptions-item>
        <el-descriptions-item label="区块链网络">
          FISCO BCOS 2.x
        </el-descriptions-item>
        <el-descriptions-item label="存储方式">
          IPFS 分布式存储
        </el-descriptions-item>
        <el-descriptions-item label="登录时间">
          {{ formatDateTime(new Date()) }}
        </el-descriptions-item>
        <el-descriptions-item label="用户角色">
          {{ getRoleText(authStore.user?.role) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  UserFilled,
  Document,
  Medal,
  CircleCheck,
  CircleClose,
  User,
  School,
  Clock,
  Bell,
  Connection
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { UserRole } from '@/types'
import { formatDateTime } from '@/utils/format'

const router = useRouter()
const authStore = useAuthStore()

// 判断是否是教师
const isTeacher = computed(() => {
  return authStore.user?.role === UserRole.COLLEGE_ADMIN ||
         authStore.user?.role === UserRole.SCHOOL_ADMIN
})

// 学生统计数据
const studentStats = reactive({
  totalApplications: 0,
  pendingApplications: 0,
  totalCertificates: 0,
  rejectedApplications: 0
})

// 教师统计数据
const teacherStats = reactive({
  pendingApprovals: 0,
  approvedCount: 0,
  rejectedCount: 0,
  totalApprovals: 0
})

// 管理员统计数据
const adminStats = reactive({
  totalUsers: 0,
  totalCertificates: 0,
  totalColleges: 0,
  blockHeight: 0
})

// 问候语
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 17) return '下午好'
  if (hour < 19) return '傍晚好'
  if (hour < 22) return '晚上好'
  return '夜深了'
})

// 获取角色文本
const getRoleText = (role?: UserRole | string) => {
  const roleMap: Record<string, string> = {
    [UserRole.STUDENT]: '学生',
    [UserRole.COLLEGE_ADMIN]: '学院教师',
    [UserRole.SCHOOL_ADMIN]: '学校教师',
    [UserRole.SYSTEM_ADMIN]: '系统管理员',
  }
  return role ? (roleMap[role] || role) : '未知'
}

// 快捷操作
const quickActions = computed(() => {
  const actions = []

  if (authStore.user?.role === UserRole.STUDENT) {
    actions.push(
      {
        title: '我的申请',
        description: '查看和管理证书申请',
        icon: 'Document',
        color: '#409EFF',
        path: '/applications',
      },
      {
        title: '我的证书',
        description: '查看已获得的证书',
        icon: 'Medal',
        color: '#67C23A',
        path: '/my-certificates',
      }
    )
  }

  if (isTeacher.value) {
    actions.push(
      {
        title: '待审批',
        description: '处理待审批的申请',
        icon: 'CircleCheck',
        color: '#E6A23C',
        path: '/approvals',
      },
      {
        title: '审批记录',
        description: '查看审批历史记录',
        icon: 'Document',
        color: '#409EFF',
        path: '/my-approvals',
      }
    )
  }

  if (authStore.user?.role === UserRole.SYSTEM_ADMIN) {
    actions.push(
      {
        title: '用户管理',
        description: '管理系统用户',
        icon: 'User',
        color: '#409EFF',
        path: '/admin/users',
      },
      {
        title: '学院管理',
        description: '管理学院和专业',
        icon: 'School',
        color: '#67C23A',
        path: '/admin/colleges',
      },
      {
        title: '可视化大屏',
        description: '查看数据统计大屏',
        icon: 'DataAnalysis',
        color: '#E6A23C',
        path: '/admin/data-screen',
      },
      {
        title: '区块链配置',
        description: '查看区块链网络状态',
        icon: 'Connection',
        color: '#909399',
        path: '/admin/blockchain',
      }
    )
  }

  return actions
})

// 加载统计数据
const loadStats = async () => {
  try {
    // TODO: 调用后端API获取统计数据
    // 模拟数据
    if (authStore.user?.role === UserRole.STUDENT) {
      studentStats.totalApplications = 5
      studentStats.pendingApplications = 2
      studentStats.totalCertificates = 3
      studentStats.rejectedApplications = 0
    } else if (isTeacher.value) {
      teacherStats.pendingApprovals = 8
      teacherStats.approvedCount = 45
      teacherStats.rejectedCount = 5
      teacherStats.totalApprovals = 58
    } else if (authStore.user?.role === UserRole.SYSTEM_ADMIN) {
      adminStats.totalUsers = 256
      adminStats.totalCertificates = 1256
      adminStats.totalColleges = 12
      adminStats.blockHeight = 125678
    }
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped lang="scss">
.dashboard-container {
  padding: 20px;

  .welcome-card {
    margin-bottom: 20px;

    .card-header {
      font-size: 18px;
      font-weight: 600;
    }

    .welcome-content {
      display: flex;
      align-items: center;
      gap: 24px;
      padding: 20px 0;

      .user-info {
        h2 {
          margin: 0 0 8px 0;
          font-size: 24px;
          color: var(--el-text-color-primary);
        }

        .role-text {
          margin: 0;
          font-size: 14px;
          color: var(--el-text-color-secondary);
        }
      }
    }
  }

  .stats-cards {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    margin-bottom: 20px;

    .stat-card {
      .stat-content {
        display: flex;
        align-items: center;
        gap: 16px;

        .stat-icon {
          width: 64px;
          height: 64px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #fff;

          &.primary {
            background: linear-gradient(135deg, #409EFF, #66b1ff);
          }

          &.success {
            background: linear-gradient(135deg, #67C23A, #85ce61);
          }

          &.warning {
            background: linear-gradient(135deg, #E6A23C, #ebb563);
          }

          &.danger {
            background: linear-gradient(135deg, #F56C6C, #f78989);
          }

          &.info {
            background: linear-gradient(135deg, #909399, #a6a9ad);
          }
        }

        .stat-info {
          .stat-value {
            font-size: 32px;
            font-weight: 700;
            color: var(--el-text-color-primary);
          }

          .stat-label {
            font-size: 14px;
            color: var(--el-text-color-secondary);
          }
        }
      }
    }
  }

  .quick-actions {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
    margin-bottom: 20px;

    .action-card {
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        transform: translateY(-4px);
      }

      .action-content {
        display: flex;
        align-items: center;
        gap: 20px;

        .action-info {
          flex: 1;

          h3 {
            margin: 0 0 8px 0;
            font-size: 18px;
            color: var(--el-text-color-primary);
          }

          p {
            margin: 0;
            font-size: 14px;
            color: var(--el-text-color-secondary);
          }
        }
      }
    }
  }

  .system-info {
    .card-header {
      font-size: 18px;
      font-weight: 600;
    }
  }
}

@media (max-width: 1200px) {
  .dashboard-container {
    .stats-cards {
      grid-template-columns: repeat(2, 1fr);
    }
  }
}

@media (max-width: 768px) {
  .dashboard-container {
    padding: 12px;

    .welcome-card {
      .welcome-content {
        flex-direction: column;
        text-align: center;
      }
    }

    .stats-cards {
      grid-template-columns: 1fr;
    }

    .quick-actions {
      grid-template-columns: 1fr;
    }
  }
}
</style>