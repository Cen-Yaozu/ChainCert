<template>
  <div class="user-management">
    <!-- 搜索表单 -->
    <el-card class="search-card" shadow="never">
      <SearchForm
        :model="searchForm"
        :loading="loading"
        @search="handleSearch"
        @reset="handleReset"
      >
        <el-form-item label="用户名">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入用户名"
            clearable
          />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input
            v-model="searchForm.realName"
            placeholder="请输入姓名"
            clearable
          />
        </el-form-item>
        <el-form-item label="角色">
          <el-select
            v-model="searchForm.role"
            placeholder="请选择角色"
            clearable
          >
            <el-option label="学生" value="STUDENT" />
            <el-option label="学院教师" value="COLLEGE_TEACHER" />
            <el-option label="学校教师" value="SCHOOL_TEACHER" />
            <el-option label="系统管理员" value="SYSTEM_ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.enabled"
            placeholder="请选择状态"
            clearable
          >
            <el-option label="启用" :value="true" />
            <el-option label="禁用" :value="false" />
          </el-select>
        </el-form-item>
      </SearchForm>
    </el-card>

    <!-- 操作按钮 -->
    <el-card class="toolbar-card" shadow="never">
      <el-button
        type="primary"
        :icon="Plus"
        @click="handleAdd"
      >
        新增用户
      </el-button>
      <el-button
        :icon="Refresh"
        @click="loadUsers"
      >
        刷新
      </el-button>
    </el-card>

    <!-- 用户列表 -->
    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="users"
        stripe
        border
      >
        <el-table-column
          prop="id"
          label="ID"
          width="80"
          align="center"
        />
        <el-table-column
          prop="username"
          label="用户名"
          min-width="120"
        />
        <el-table-column
          prop="realName"
          label="姓名"
          min-width="100"
        />
        <el-table-column
          prop="role"
          label="角色"
          width="120"
          align="center"
        >
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)">
              {{ getRoleText(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="email"
          label="邮箱"
          min-width="180"
        />
        <el-table-column
          prop="phone"
          label="手机号"
          width="120"
        />
        <el-table-column
          prop="collegeName"
          label="所属学院"
          min-width="120"
        />
        <el-table-column
          prop="enabled"
          label="状态"
          width="80"
          align="center"
        >
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="createdAt"
          label="创建时间"
          width="180"
          align="center"
        >
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="280"
          align="center"
          fixed="right"
        >
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              link
              :icon="Edit"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              type="warning"
              size="small"
              link
              :icon="Key"
              @click="handleResetPassword(row)"
            >
              重置密码
            </el-button>
            <el-button
              :type="row.enabled ? 'danger' : 'success'"
              size="small"
              link
              :icon="row.enabled ? Lock : Unlock"
              @click="handleToggleStatus(row)"
            >
              {{ row.enabled ? '禁用' : '启用' }}
            </el-button>
            <el-button
              type="danger"
              size="small"
              link
              :icon="Delete"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <Pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        @change="loadUsers"
      />
    </el-card>

    <!-- 新增/编辑用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input
            v-model="form.realName"
            placeholder="请输入姓名"
          />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select
            v-model="form.role"
            placeholder="请选择角色"
            style="width: 100%"
          >
            <el-option label="学生" value="STUDENT" />
            <el-option label="学院教师" value="COLLEGE_TEACHER" />
            <el-option label="学校教师" value="SCHOOL_TEACHER" />
            <el-option label="系统管理员" value="SYSTEM_ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="form.email"
            placeholder="请输入邮箱"
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入手机号"
          />
        </el-form-item>
        <el-form-item
          v-if="form.role === 'STUDENT' || form.role === 'COLLEGE_TEACHER'"
          label="所属学院"
          prop="collegeId"
        >
          <el-select
            v-model="form.collegeId"
            placeholder="请选择学院"
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="college in colleges"
              :key="college.id"
              :label="college.name"
              :value="college.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          v-if="form.role === 'STUDENT'"
          label="专业"
          prop="majorId"
        >
          <el-select
            v-model="form.majorId"
            placeholder="请选择专业"
            style="width: 100%"
            filterable
            :disabled="!form.collegeId"
          >
            <el-option
              v-for="major in filteredMajors"
              :key="major.id"
              :label="major.name"
              :value="major.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          v-if="form.role === 'STUDENT'"
          label="学号"
          prop="studentId"
        >
          <el-input
            v-model="form.studentId"
            placeholder="请输入学号"
          />
        </el-form-item>
        <el-form-item
          v-if="!isEdit"
          label="初始密码"
          prop="password"
        >
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入初始密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="状态" prop="enabled">
          <el-switch
            v-model="form.enabled"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="submitting"
          @click="handleSubmit"
        >
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog
      v-model="resetPasswordVisible"
      title="重置密码"
      width="400px"
    >
      <el-form
        ref="resetPasswordFormRef"
        :model="resetPasswordForm"
        :rules="resetPasswordRules"
        label-width="100px"
      >
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="resetPasswordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="resetPasswordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPasswordVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="submitting"
          @click="handleResetPasswordSubmit"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  Plus,
  Refresh,
  Edit,
  Delete,
  Key,
  Lock,
  Unlock
} from '@element-plus/icons-vue'
import SearchForm from '@/components/SearchForm.vue'
import Pagination from '@/components/Pagination.vue'
import { userApi, collegeApi } from '@/api'
import { formatDateTime } from '@/utils'
import type { UserListVO, UserRequest, CollegeResponse, MajorResponse } from '@/types'

// 搜索表单
const searchForm = reactive({
  username: '',
  realName: '',
  role: '',
  enabled: undefined as boolean | undefined
})

// 用户列表
const users = ref<UserListVO[]>([])
const loading = ref(false)

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = computed(() => isEdit.value ? '编辑用户' : '新增用户')
const isEdit = ref(false)
const submitting = ref(false)

// 表单
const formRef = ref<FormInstance>()
const form = reactive<UserRequest>({
  username: '',
  realName: '',
  role: 'STUDENT',
  email: '',
  phone: '',
  collegeId: undefined,
  majorId: undefined,
  studentId: '',
  password: '',
  enabled: true
})

// 学院和专业数据
const colleges = ref<CollegeResponse[]>([])
const majors = ref<MajorResponse[]>([])

// 根据选择的学院过滤专业
const filteredMajors = computed(() => {
  if (!form.collegeId) return []
  return majors.value.filter(major => major.collegeId === form.collegeId)
})

// 表单验证规则
const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  collegeId: [
    { required: true, message: '请选择学院', trigger: 'change' }
  ],
  majorId: [
    { required: true, message: '请选择专业', trigger: 'change' }
  ],
  studentId: [
    { required: true, message: '请输入学号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入初始密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ]
}

// 重置密码对话框
const resetPasswordVisible = ref(false)
const resetPasswordFormRef = ref<FormInstance>()
const resetPasswordForm = reactive({
  userId: 0,
  newPassword: '',
  confirmPassword: ''
})

const resetPasswordRules: FormRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== resetPasswordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 加载用户列表
const loadUsers = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      page: pagination.page,
      size: pagination.size
    }
    const response = await userApi.getUserList(params)
    users.value = response.data.records
    pagination.total = response.data.total
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

// 加载学院列表
const loadColleges = async () => {
  try {
    const response = await collegeApi.getCollegeList({ page: 1, size: 1000 })
    colleges.value = response.data.records
  } catch (error) {
    ElMessage.error('加载学院列表失败')
  }
}

// 加载专业列表
const loadMajors = async () => {
  try {
    const response = await collegeApi.getMajorList({ page: 1, size: 1000 })
    majors.value = response.data.records
  } catch (error) {
    ElMessage.error('加载专业列表失败')
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadUsers()
}

// 重置搜索
const handleReset = () => {
  searchForm.username = ''
  searchForm.realName = ''
  searchForm.role = ''
  searchForm.enabled = undefined
  handleSearch()
}

// 新增用户
const handleAdd = () => {
  isEdit.value = false
  dialogVisible.value = true
}

// 编辑用户
const handleEdit = (row: UserListVO) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    username: row.username,
    realName: row.realName,
    role: row.role,
    email: row.email,
    phone: row.phone,
    collegeId: row.collegeId,
    majorId: row.majorId,
    studentId: row.studentId,
    enabled: row.enabled
  })
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (isEdit.value) {
        await userApi.updateUser(form.id!, form)
        ElMessage.success('更新用户成功')
      } else {
        await userApi.createUser(form)
        ElMessage.success('创建用户成功')
      }
      dialogVisible.value = false
      loadUsers()
    } catch (error) {
      ElMessage.error(isEdit.value ? '更新用户失败' : '创建用户失败')
    } finally {
      submitting.value = false
    }
  })
}

// 重置密码
const handleResetPassword = (row: UserListVO) => {
  resetPasswordForm.userId = row.id
  resetPasswordForm.newPassword = ''
  resetPasswordForm.confirmPassword = ''
  resetPasswordVisible.value = true
}

// 提交重置密码
const handleResetPasswordSubmit = async () => {
  if (!resetPasswordFormRef.value) return
  
  await resetPasswordFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      await userApi.resetPassword(resetPasswordForm.userId, {
        newPassword: resetPasswordForm.newPassword
      })
      ElMessage.success('重置密码成功')
      resetPasswordVisible.value = false
    } catch (error) {
      ElMessage.error('重置密码失败')
    } finally {
      submitting.value = false
    }
  })
}

// 切换用户状态
const handleToggleStatus = async (row: UserListVO) => {
  try {
    await ElMessageBox.confirm(
      `确定要${row.enabled ? '禁用' : '启用'}用户"${row.realName}"吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await userApi.toggleUserStatus(row.id)
    ElMessage.success(`${row.enabled ? '禁用' : '启用'}用户成功`)
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${row.enabled ? '禁用' : '启用'}用户失败`)
    }
  }
}

// 删除用户
const handleDelete = async (row: UserListVO) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户"${row.realName}"吗？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    await userApi.deleteUser(row.id)
    ElMessage.success('删除用户成功')
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除用户失败')
    }
  }
}

// 对话框关闭
const handleDialogClose = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    username: '',
    realName: '',
    role: 'STUDENT',
    email: '',
    phone: '',
    collegeId: undefined,
    majorId: undefined,
    studentId: '',
    password: '',
    enabled: true
  })
}

// 获取角色文本
// 注意：后端使用 COLLEGE_TEACHER/SCHOOL_TEACHER，不是 COLLEGE_ADMIN/SCHOOL_ADMIN
const getRoleText = (role: string) => {
  const roleMap: Record<string, string> = {
    STUDENT: '学生',
    COLLEGE_TEACHER: '学院教师',
    SCHOOL_TEACHER: '学校教师',
    SYSTEM_ADMIN: '系统管理员'
  }
  return roleMap[role] || role
}

// 获取角色标签类型
const getRoleType = (role: string) => {
  const typeMap: Record<string, any> = {
    STUDENT: '',
    COLLEGE_TEACHER: 'success',
    SCHOOL_TEACHER: 'warning',
    SYSTEM_ADMIN: 'danger'
  }
  return typeMap[role] || ''
}

onMounted(() => {
  loadUsers()
  loadColleges()
  loadMajors()
})
</script>

<style scoped lang="scss">
.user-management {
  .search-card,
  .toolbar-card {
    margin-bottom: 16px;
  }
}
</style>