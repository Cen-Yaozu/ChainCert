import type { RouteRecordRaw } from 'vue-router'
import { UserRole } from '@/types'

/**
 * 菜单项接口
 */
export interface MenuItem {
  path: string
  name: string
  title: string
  icon?: string
  roles?: UserRole[]
  children?: MenuItem[]
  hidden?: boolean
}

/**
 * 菜单配置
 */
export const menuConfig: MenuItem[] = [
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
    roles: [UserRole.STUDENT],
  },
  {
    path: '/my-certificates',
    name: 'MyCertificates',
    title: '我的证书',
    icon: 'Medal',
    roles: [UserRole.STUDENT],
  },
  {
    path: '/approvals',
    name: 'Approvals',
    title: '待审批',
    icon: 'CircleCheck',
    roles: [UserRole.COLLEGE_ADMIN, UserRole.SCHOOL_ADMIN],
  },
  {
    path: '/my-approvals',
    name: 'MyApprovals',
    title: '审批记录',
    icon: 'List',
    roles: [UserRole.COLLEGE_ADMIN, UserRole.SCHOOL_ADMIN],
  },
  {
    path: '/admin',
    name: 'Admin',
    title: '系统管理',
    icon: 'Setting',
    roles: [UserRole.SYSTEM_ADMIN],
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
        path: '/admin/approval-flow',
        name: 'ApprovalFlowConfig',
        title: '审批流配置',
        icon: 'SetUp',
      },
      {
        path: '/admin/blockchain',
        name: 'BlockchainConfig',
        title: '区块链配置',
        icon: 'Connection',
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
      {
        path: '/admin/data-screen',
        name: 'DataScreen',
        title: '可视化大屏',
        icon: 'DataAnalysis',
      },
    ],
  },
]

/**
 * 根据用户角色过滤菜单
 * @param menus 菜单配置
 * @param userRole 用户角色
 * @returns 过滤后的菜单
 */
export function filterMenuByRole(
  menus: MenuItem[],
  userRole?: UserRole
): MenuItem[] {
  if (!userRole) return []

  return menus
    .filter((menu) => {
      // 如果菜单没有角色限制，或者用户角色在允许的角色列表中
      if (!menu.roles || menu.roles.includes(userRole)) {
        return true
      }
      return false
    })
    .map((menu) => {
      // 如果有子菜单，递归过滤
      if (menu.children) {
        return {
          ...menu,
          children: filterMenuByRole(menu.children, userRole),
        }
      }
      return menu
    })
    .filter((menu) => {
      // 过滤掉没有子菜单的父菜单
      if (menu.children) {
        return menu.children.length > 0
      }
      return true
    })
}

/**
 * 从路由配置生成菜单
 * @param routes 路由配置
 * @returns 菜单配置
 */
export function generateMenuFromRoutes(routes: RouteRecordRaw[]): MenuItem[] {
  const menus: MenuItem[] = []

  routes.forEach((route) => {
    if (route.meta?.hidden) return

    const menu: MenuItem = {
      path: route.path,
      name: route.name as string,
      title: (route.meta?.title as string) || '',
      icon: route.meta?.icon as string,
      roles: route.meta?.roles as UserRole[],
    }

    if (route.children && route.children.length > 0) {
      menu.children = generateMenuFromRoutes(route.children)
    }

    if (menu.title) {
      menus.push(menu)
    }
  })

  return menus
}

/**
 * 查找当前激活的菜单路径
 * @param menus 菜单配置
 * @param currentPath 当前路径
 * @returns 激活的菜单路径数组
 */
export function findActiveMenuPath(
  menus: MenuItem[],
  currentPath: string
): string[] {
  for (const menu of menus) {
    if (menu.path === currentPath) {
      return [menu.path]
    }

    if (menu.children) {
      const childPath = findActiveMenuPath(menu.children, currentPath)
      if (childPath.length > 0) {
        return [menu.path, ...childPath]
      }
    }
  }

  return []
}