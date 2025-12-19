<template>
  <div class="dashboard-container">
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
          FISCO BCOS
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
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { UserFilled, Document, Medal, CircleCheck, User, School } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { UserRole } from '@/types'
import { formatDateTime } from '@/utils/format'

const router = useRouter()
const authStore = useAuthStore()

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
const getRoleText = (role?: UserRole) => {
  const roleMap: Record<UserRole, string> = {
    [UserRole.STUDENT]: '学生',
    [UserRole.COLLEGE_ADMIN]: '学院管理员',
    [UserRole.SCHOOL_ADMIN]: '学校管理员',
    [UserRole.SYSTEM_ADMIN]: '系统管理员',
  }
  return role ? roleMap[role] : '未知'
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

  if (
    authStore.user?.role === UserRole.COLLEGE_ADMIN ||
    authStore.user?.role === UserRole.SCHOOL_ADMIN
  ) {
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
      }
    )
  }

  return actions
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

  .quick-actions {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
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

@media (max-width: 768px) {
  .dashboard-container {
    padding: 12px;

    .welcome-card {
      .welcome-content {
        flex-direction: column;
        text-align: center;
      }
    }

    .quick-actions {
      grid-template-columns: 1fr;
    }
  }
}
</style>