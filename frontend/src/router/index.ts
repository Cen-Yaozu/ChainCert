import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { UserRole } from '@/types'

// 导出菜单配置供其他模块使用
export { menuConfig, filterMenuByRole, findActiveMenuPath } from './menu'
export type { MenuItem } from './menu'

/**
 * 路由配置
 */
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false, title: '登录' },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { requiresAuth: false, title: '注册' },
  },
  {
    path: '/verify',
    name: 'Verify',
    component: () => import('@/views/Verify.vue'),
    meta: { requiresAuth: false, title: '证书核验' },
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/dashboard',
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页', icon: 'HomeFilled' },
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心', icon: 'User', hidden: true },
      },
      // 学生端路由
      {
        path: 'applications',
        name: 'Applications',
        component: () => import('@/views/student/Applications.vue'),
        meta: { title: '我的申请', icon: 'Document', roles: [UserRole.STUDENT] },
      },
      {
        path: 'my-certificates',
        name: 'MyCertificates',
        component: () => import('@/views/student/MyCertificates.vue'),
        meta: { title: '我的证书', icon: 'Medal', roles: [UserRole.STUDENT] },
      },
      // 老师端路由
      {
        path: 'approvals',
        name: 'Approvals',
        component: () => import('@/views/teacher/Approvals.vue'),
        meta: {
          title: '待审批',
          icon: 'CircleCheck',
          roles: [UserRole.COLLEGE_ADMIN, UserRole.SCHOOL_ADMIN],
        },
      },
      {
        path: 'my-approvals',
        name: 'MyApprovals',
        component: () => import('@/views/teacher/MyApprovals.vue'),
        meta: {
          title: '审批记录',
          icon: 'List',
          roles: [UserRole.COLLEGE_ADMIN, UserRole.SCHOOL_ADMIN],
        },
      },
      // 管理员端路由
      {
        path: 'admin',
        name: 'Admin',
        meta: { title: '系统管理', icon: 'Setting', roles: [UserRole.SYSTEM_ADMIN] },
        children: [
          {
            path: 'users',
            name: 'UserManagement',
            component: () => import('@/views/admin/UserManagement.vue'),
            meta: { title: '用户管理', icon: 'User' },
          },
          {
            path: 'colleges',
            name: 'CollegeManagement',
            component: () => import('@/views/admin/CollegeManagement.vue'),
            meta: { title: '学院管理', icon: 'School' },
          },
          {
            path: 'templates',
            name: 'TemplateManagement',
            component: () => import('@/views/admin/TemplateManagement.vue'),
            meta: { title: '模板管理', icon: 'Tickets' },
          },
          {
            path: 'approval-flow',
            name: 'ApprovalFlowConfig',
            component: () => import('@/views/admin/ApprovalFlowConfig.vue'),
            meta: { title: '审批流配置', icon: 'SetUp' },
          },
          {
            path: 'blockchain',
            name: 'BlockchainConfig',
            component: () => import('@/views/admin/BlockchainConfig.vue'),
            meta: { title: '区块链配置', icon: 'Connection' },
          },
          {
            path: 'logs',
            name: 'SystemLog',
            component: () => import('@/views/admin/SystemLog.vue'),
            meta: { title: '系统日志', icon: 'Document' },
          },
          {
            path: 'certificates',
            name: 'CertificateManagement',
            component: () => import('@/views/admin/CertificateManagement.vue'),
            meta: { title: '证书管理', icon: 'Medal' },
          },
          {
            path: 'data-screen',
            name: 'DataScreen',
            component: () => import('@/views/admin/DataScreen.vue'),
            meta: { title: '可视化大屏', icon: 'DataAnalysis' },
          },
        ],
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { requiresAuth: false, title: '404' },
  },
]

/**
 * 创建路由实例
 */
const router = createRouter({
  history: createWebHistory(),
  routes,
})

/**
 * 全局前置守卫 - 认证检查
 */
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  const requiresAuth = to.meta.requiresAuth !== false

  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 区块链证书存证系统` : '区块链证书存证系统'

  // 检查是否需要认证
  if (requiresAuth && !authStore.isAuthenticated) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  // 检查角色权限
  if (to.meta.roles && authStore.user) {
    const roles = to.meta.roles as UserRole[]
    const userRole = authStore.user.role as UserRole
    if (!roles.includes(userRole)) {
      next({ name: 'Dashboard' })
      return
    }
  }

  // 已登录用户访问登录页或注册页，重定向到首页
  if ((to.name === 'Login' || to.name === 'Register') && authStore.isAuthenticated) {
    next({ name: 'Dashboard' })
    return
  }

  next()
})

export default router