<template>
  <div class="profile">
    <el-row :gutter="20">
      <!-- 左侧个人信息卡片 -->
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>个人信息</span>
            </div>
          </template>
          <div class="profile-info">
            <el-avatar
              :size="100"
              :src="userInfo?.avatar"
              class="avatar"
            >
              {{ userInfo?.realName?.charAt(0) }}
            </el-avatar>
            <h2 class="name">{{ userInfo?.realName }}</h2>
            <el-tag :type="getRoleType(userInfo?.role || '')">
              {{ getRoleText(userInfo?.role || '') }}
            </el-tag>
            <el-descriptions :column="1" class="info-list" border>
              <el-descriptions-item label="用户名">
                {{ userInfo?.username }}
              </el-descriptions-item>
              <el-descriptions-item label="邮箱">
                {{ userInfo?.email }}
              </el-descriptions-item>
              <el-descriptions-item label="手机号">
                {{ userInfo?.phone }}
              </el-descriptions-item>
              <el-descriptions-item v-if="userInfo?.collegeName" label="学院">
                {{ userInfo?.collegeName }}
              </el-descriptions-item>
              <el-descriptions-item v-if="userInfo?.majorName" label="专业">
                {{ userInfo?.majorName }}
              </el-descriptions-item>
              <el-descriptions-item v-if="userInfo?.studentId" label="学号">
                {{ userInfo?.studentId }}
              </el-descriptions-item>
              <el-descriptions-item label="注册时间">
                {{ formatDateTime(userInfo?.createdAt) }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧设置区域 -->
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>账号设置</span>
            </div>
          </template>
          <el-tabs v-model="activeTab">
            <!-- 基本信息 -->
            <el-tab-pane label="基本信息" name="basic">
              <el-form
                ref="basicFormRef"
                :model="basicForm"
                :rules="basicRules"
                label-width="100px"
              >
                <el-form-item label="姓名" prop="realName">
                  <el-input
                    v-model="basicForm.realName"
                    placeholder="请输入姓名"
                  />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input
                    v-model="basicForm.email"
                    placeholder="请输入邮箱"
                  />
                </el-form-item>
                <el-form-item label="手机号" prop="phone">
                  <el-input
                    v-model="basicForm.phone"
                    placeholder="请输入手机号"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button
                    type="primary"
                    :loading="submitting"
                    @click="handleUpdateBasicInfo"
                  >
                    保存修改
                  </el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <!-- 修改密码 -->
            <el-tab-pane label="修改密码" name="password">
              <el-form
                ref="passwordFormRef"
                :model="passwordForm"
                :rules="passwordRules"
                label-width="100px"
              >
                <el-form-item label="当前密码" prop="oldPassword">
                  <el-input
                    v-model="passwordForm.oldPassword"
                    type="password"
                    placeholder="请输入当前密码"
                    show-password
                  />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input
                    v-model="passwordForm.newPassword"
                    type="password"
                    placeholder="请输入新密码（6-20位）"
                    show-password
                  />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input
                    v-model="passwordForm.confirmPassword"
                    type="password"
                    placeholder="请再次输入新密码"
                    show-password
                  />
                </el-form-item>
                <el-form-item>
                  <el-button
                    type="primary"
                    :loading="submitting"
                    @click="handleChangePassword"
                  >
                    修改密码
                  </el-button>
                  <el-button @click="handleResetPasswordForm">
                    重置
                  </el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <!-- 账号安全 -->
            <el-tab-pane label="账号安全" name="security">
              <el-descriptions :column="1" border>
                <el-descriptions-item label="账号状态">
                  <el-tag type="success">正常</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="最后登录时间">
                  {{ formatDateTime(userInfo?.lastLoginAt) }}
                </el-descriptions-item>
                <el-descriptions-item label="最后登录IP">
                  {{ userInfo?.lastLoginIp || '未知' }}
                </el-descriptions-item>
                <el-descriptions-item label="密码强度">
                  <el-progress
                    :percentage="passwordStrength"
                    :color="getPasswordStrengthColor()"
                    :format="() => getPasswordStrengthText()"
                  />
                </el-descriptions-item>
              </el-descriptions>

              <el-divider />

              <div class="security-tips">
                <h4>安全建议</h4>
                <ul>
                  <li>定期修改密码，建议每3个月更换一次</li>
                  <li>密码应包含大小写字母、数字和特殊字符</li>
                  <li>不要在多个网站使用相同的密码</li>
                  <li>不要将密码告诉他人或写在容易被发现的地方</li>
                  <li>发现账号异常时，请立即修改密码并联系管理员</li>
                </ul>
              </div>
            </el-tab-pane>

            <!-- 操作日志 -->
            <el-tab-pane label="操作日志" name="logs">
              <el-table
                v-loading="logsLoading"
                :data="logs"
                stripe
                border
              >
                <el-table-column
                  prop="action"
                  label="操作类型"
                  width="120"
                  align="center"
                >
                  <template #default="{ row }">
                    <el-tag :type="getActionType(row.action)">
                      {{ getActionText(row.action) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column
                  prop="description"
                  label="操作描述"
                  min-width="200"
                  show-overflow-tooltip
                />
                <el-table-column
                  prop="ip"
                  label="IP地址"
                  width="140"
                />
                <el-table-column
                  prop="createdAt"
                  label="操作时间"
                  width="180"
                  align="center"
                >
                  <template #default="{ row }">
                    {{ formatDateTime(row.createdAt) }}
                  </template>
                </el-table-column>
              </el-table>

              <!-- 分页 -->
              <Pagination
                v-model:current-page="logsPagination.page"
                v-model:page-size="logsPagination.size"
                :total="logsPagination.total"
                @change="loadLogs"
              />
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import Pagination from '@/components/Pagination.vue'
import { profileApi, logApi } from '@/api'
import { formatDateTime } from '@/utils'
import type { UserResponse, SystemLogVO } from '@/types'

const authStore = useAuthStore()

// 标签页
const activeTab = ref('basic')

// 用户信息
const userInfo = ref<UserResponse>()

// 基本信息表单
const basicFormRef = ref<FormInstance>()
const basicForm = reactive({
  realName: '',
  email: '',
  phone: ''
})

const basicRules: FormRules = {
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 修改密码表单
const passwordFormRef = ref<FormInstance>()
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const submitting = ref(false)

// 操作日志
const logs = ref<SystemLogVO[]>([])
const logsLoading = ref(false)
const logsPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 密码强度（模拟计算）
const passwordStrength = computed(() => {
  // 这里简化处理，实际应该根据用户密码的复杂度计算
  return 75
})

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const response = await profileApi.getProfile()
    userInfo.value = response.data
    // 填充基本信息表单
    Object.assign(basicForm, {
      realName: response.data.realName,
      email: response.data.email,
      phone: response.data.phone
    })
  } catch (error) {
    ElMessage.error('加载用户信息失败')
  }
}

// 加载操作日志
const loadLogs = async () => {
  logsLoading.value = true
  try {
    const response = await logApi.getLogList({
      username: authStore.user?.username,
      page: logsPagination.page,
      size: logsPagination.size
    })
    logs.value = response.data.records
    logsPagination.total = response.data.total
  } catch (error) {
    ElMessage.error('加载操作日志失败')
  } finally {
    logsLoading.value = false
  }
}

// 更新基本信息
const handleUpdateBasicInfo = async () => {
  if (!basicFormRef.value) return
  
  await basicFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      await profileApi.updateProfile(basicForm)
      ElMessage.success('更新个人信息成功')
      loadUserInfo()
    } catch (error) {
      ElMessage.error('更新个人信息失败')
    } finally {
      submitting.value = false
    }
  })
}

// 修改密码
const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      await profileApi.changePassword({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      })
      ElMessage.success('修改密码成功，请重新登录')
      // 清空表单
      handleResetPasswordForm()
      // 延迟后退出登录
      setTimeout(() => {
        authStore.logout()
      }, 1500)
    } catch (error) {
      ElMessage.error('修改密码失败')
    } finally {
      submitting.value = false
    }
  })
}

// 重置密码表单
const handleResetPasswordForm = () => {
  passwordFormRef.value?.resetFields()
  Object.assign(passwordForm, {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  })
}

// 获取角色文本
const getRoleText = (role: string) => {
  const roleMap: Record<string, string> = {
    STUDENT: '学生',
    COLLEGE_ADMIN: '学院管理员',
    SCHOOL_ADMIN: '学校管理员',
    SYSTEM_ADMIN: '系统管理员'
  }
  return roleMap[role] || role
}

// 获取角色标签类型
const getRoleType = (role: string) => {
  const typeMap: Record<string, any> = {
    STUDENT: '',
    COLLEGE_ADMIN: 'success',
    SCHOOL_ADMIN: 'warning',
    SYSTEM_ADMIN: 'danger'
  }
  return typeMap[role] || ''
}

// 获取密码强度颜色
const getPasswordStrengthColor = () => {
  if (passwordStrength.value < 40) return '#F56C6C'
  if (passwordStrength.value < 70) return '#E6A23C'
  return '#67C23A'
}

// 获取密码强度文本
const getPasswordStrengthText = () => {
  if (passwordStrength.value < 40) return '弱'
  if (passwordStrength.value < 70) return '中'
  return '强'
}

// 获取操作类型文本
const getActionText = (action: string) => {
  const actionMap: Record<string, string> = {
    LOGIN: '登录',
    LOGOUT: '登出',
    CREATE: '创建',
    UPDATE: '更新',
    DELETE: '删除',
    APPROVE: '审批',
    REJECT: '拒绝',
    REVOKE: '撤销'
  }
  return actionMap[action] || action
}

// 获取操作类型标签类型
const getActionType = (action: string) => {
  const typeMap: Record<string, any> = {
    LOGIN: 'success',
    LOGOUT: 'info',
    CREATE: 'primary',
    UPDATE: 'warning',
    DELETE: 'danger',
    APPROVE: 'success',
    REJECT: 'danger',
    REVOKE: 'warning'
  }
  return typeMap[action] || ''
}

onMounted(() => {
  loadUserInfo()
  loadLogs()
})
</script>

<style scoped lang="scss">
.profile {
  .profile-info {
    text-align: center;

    .avatar {
      margin-bottom: 16px;
      font-size: 40px;
      font-weight: bold;
    }

    .name {
      margin: 16px 0 8px;
      font-size: 24px;
      font-weight: 500;
    }

    .info-list {
      margin-top: 24px;
      text-align: left;
    }
  }

  .security-tips {
    padding: 16px;
    background-color: #f5f7fa;
    border-radius: 4px;

    h4 {
      margin: 0 0 12px;
      font-size: 16px;
      font-weight: 500;
    }

    ul {
      margin: 0;
      padding-left: 20px;

      li {
        margin-bottom: 8px;
        color: #606266;
        line-height: 1.6;

        &:last-child {
          margin-bottom: 0;
        }
      }
    }
  }
}
</style>