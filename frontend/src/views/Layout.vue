<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '200px'" class="layout-aside">
      <div class="logo">
        <span v-if="!isCollapse" class="logo-text">证书系统</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :unique-opened="true"
        router
        class="layout-menu"
      >
        <template v-for="menu in menus" :key="menu.path">
          <!-- 有子菜单 -->
          <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="menu.path">
            <template #title>
              <el-icon v-if="menu.icon">
                <component :is="menu.icon" />
              </el-icon>
              <span>{{ menu.title }}</span>
            </template>
            <el-menu-item
              v-for="child in menu.children"
              :key="child.path"
              :index="child.path"
            >
              <el-icon v-if="child.icon">
                <component :is="child.icon" />
              </el-icon>
              <span>{{ child.title }}</span>
            </el-menu-item>
          </el-sub-menu>

          <!-- 无子菜单 -->
          <el-menu-item v-else :index="menu.path">
            <el-icon v-if="menu.icon">
              <component :is="menu.icon" />
            </el-icon>
            <span>{{ menu.title }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container class="layout-main">
      <!-- 顶部导航栏 -->
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-icon" @click="toggleCollapse">
            <component :is="isCollapse ? 'Expand' : 'Fold'" />
          </el-icon>

          <el-breadcrumb separator="/">
            <el-breadcrumb-item
              v-for="(item, index) in breadcrumbs"
              :key="index"
              :to="index === breadcrumbs.length - 1 ? undefined : item.path"
            >
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" :icon="UserFilled" />
              <span class="username">{{ authStore.user?.realName || authStore.user?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="layout-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  Fold,
  Expand,
  UserFilled,
  User,
  ArrowDown,
  SwitchButton,
  HomeFilled,
  Document,
  Medal,
  CircleCheck,
  List,
  Setting,
  School,
  Tickets,
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { filterMenuByRole, findActiveMenuPath } from '@/router/menu'
import type { MenuItem } from '@/router/menu'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 侧边栏折叠状态
const isCollapse = ref(false)

// 根据用户角色过滤菜单
const menus = computed(() => {
  return filterMenuByRole(
    [
      {
        path: '/dashboard',
        name: 'Dashboard',
        title: '首页',
        icon: 'HomeFilled',
      },
      {
        path: '/applications',
        name: 'Applications',
        title: '我的申请',
        icon: 'Document',
        roles: [authStore.user?.role!],
      },
      {
        path: '/my-certificates',
        name: 'MyCertificates',
        title: '我的证书',
        icon: 'Medal',
        roles: [authStore.user?.role!],
      },
      {
        path: '/approvals',
        name: 'Approvals',
        title: '待审批',
        icon: 'CircleCheck',
        roles: [authStore.user?.role!],
      },
      {
        path: '/my-approvals',
        name: 'MyApprovals',
        title: '审批记录',
        icon: 'List',
        roles: [authStore.user?.role!],
      },
      {
        path: '/admin',
        name: 'Admin',
        title: '系统管理',
        icon: 'Setting',
        roles: [authStore.user?.role!],
        children: [
          {
            path: '/admin/users',
            name: 'UserManagement',
            title: '用户管理',
            icon: 'User',
          },
          {
            path: '/admin/colleges',
            name: 'CollegeManagement',
            title: '学院管理',
            icon: 'School',
          },
          {
            path: '/admin/templates',
            name: 'TemplateManagement',
            title: '模板管理',
            icon: 'Tickets',
          },
          {
            path: '/admin/certificates',
            name: 'CertificateManagement',
            title: '证书管理',
            icon: 'Medal',
          },
          {
            path: '/admin/logs',
            name: 'SystemLog',
            title: '系统日志',
            icon: 'Document',
          },
        ],
      },
    ],
    authStore.user?.role
  )
})

// 当前激活的菜单
const activeMenu = computed(() => {
  return route.path
})

// 面包屑导航
const breadcrumbs = computed(() => {
  const matched = route.matched.filter((item) => item.meta?.title)
  return matched.map((item) => ({
    path: item.path,
    title: item.meta.title as string,
  }))
})

// 切换侧边栏折叠状态
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

// 处理下拉菜单命令
const handleCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        })
        await authStore.logout()
        ElMessage.success('退出成功')
        router.push('/login')
      } catch (error) {
        // 用户取消
      }
      break
  }
}

// 监听路由变化，更新面包屑
watch(
  () => route.path,
  () => {
    // 可以在这里添加其他逻辑
  }
)
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
}

.layout-aside {
  background: #001529;
  transition: width 0.3s;
  overflow-x: hidden;

  .logo {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 60px;
    padding: 0 16px;
    background: rgba(255, 255, 255, 0.05);

    .logo-image {
      width: 32px;
      height: 32px;
    }

    .logo-text {
      margin-left: 12px;
      font-size: 18px;
      font-weight: 600;
      color: #ffffff;
    }
  }

  .layout-menu {
    border-right: none;
    background: #001529;

    :deep(.el-menu-item),
    :deep(.el-sub-menu__title) {
      color: rgba(255, 255, 255, 0.65);

      &:hover {
        color: #ffffff;
        background: rgba(255, 255, 255, 0.08);
      }

      &.is-active {
        color: #ffffff;
        background: var(--el-color-primary);
      }
    }

    :deep(.el-sub-menu) {
      .el-menu-item {
        background: rgba(0, 0, 0, 0.2);

        &:hover {
          background: rgba(255, 255, 255, 0.08);
        }

        &.is-active {
          background: var(--el-color-primary);
        }
      }
    }
  }
}

.layout-main {
  display: flex;
  flex-direction: column;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: #ffffff;
  border-bottom: 1px solid var(--el-border-color-lighter);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);

  .header-left {
    display: flex;
    align-items: center;
    gap: 20px;

    .collapse-icon {
      font-size: 20px;
      cursor: pointer;
      transition: color 0.3s;

      &:hover {
        color: var(--el-color-primary);
      }
    }
  }

  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      padding: 8px 12px;
      border-radius: 4px;
      transition: background 0.3s;

      &:hover {
        background: var(--el-fill-color-light);
      }

      .username {
        font-size: 14px;
        color: var(--el-text-color-primary);
      }
    }
  }
}

.layout-content {
  background: var(--el-bg-color-page);
  overflow-y: auto;
}

// 路由过渡动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}

.fade-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}
</style>