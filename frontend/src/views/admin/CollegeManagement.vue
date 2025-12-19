<template>
  <div class="college-management">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 学院管理 -->
      <el-tab-pane label="学院管理" name="college">
        <!-- 操作按钮 -->
        <div class="toolbar">
          <el-button
            type="primary"
            :icon="Plus"
            @click="handleAddCollege"
          >
            新增学院
          </el-button>
          <el-button
            :icon="Refresh"
            @click="loadColleges"
          >
            刷新
          </el-button>
        </div>

        <!-- 学院列表 -->
        <el-table
          v-loading="collegeLoading"
          :data="colleges"
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
            prop="name"
            label="学院名称"
            min-width="200"
          />
          <el-table-column
            prop="code"
            label="学院代码"
            width="120"
            align="center"
          />
          <el-table-column
            prop="description"
            label="描述"
            min-width="200"
            show-overflow-tooltip
          />
          <el-table-column
            prop="adminName"
            label="管理员"
            width="120"
          />
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
            width="200"
            align="center"
            fixed="right"
          >
            <template #default="{ row }">
              <el-button
                type="primary"
                size="small"
                link
                :icon="Edit"
                @click="handleEditCollege(row)"
              >
                编辑
              </el-button>
              <el-button
                type="warning"
                size="small"
                link
                :icon="User"
                @click="handleAssignAdmin(row)"
              >
                分配管理员
              </el-button>
              <el-button
                type="danger"
                size="small"
                link
                :icon="Delete"
                @click="handleDeleteCollege(row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <Pagination
          v-model:current-page="collegePagination.page"
          v-model:page-size="collegePagination.size"
          :total="collegePagination.total"
          @change="loadColleges"
        />
      </el-tab-pane>

      <!-- 专业管理 -->
      <el-tab-pane label="专业管理" name="major">
        <!-- 搜索表单 -->
        <el-form :inline="true" class="search-form">
          <el-form-item label="所属学院">
            <el-select
              v-model="majorSearchForm.collegeId"
              placeholder="请选择学院"
              clearable
              style="width: 200px"
              @change="loadMajors"
            >
              <el-option
                v-for="college in colleges"
                :key="college.id"
                :label="college.name"
                :value="college.id"
              />
            </el-select>
          </el-form-item>
        </el-form>

        <!-- 操作按钮 -->
        <div class="toolbar">
          <el-button
            type="primary"
            :icon="Plus"
            @click="handleAddMajor"
          >
            新增专业
          </el-button>
          <el-button
            :icon="Refresh"
            @click="loadMajors"
          >
            刷新
          </el-button>
        </div>

        <!-- 专业列表 -->
        <el-table
          v-loading="majorLoading"
          :data="majors"
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
            prop="name"
            label="专业名称"
            min-width="200"
          />
          <el-table-column
            prop="code"
            label="专业代码"
            width="120"
            align="center"
          />
          <el-table-column
            prop="collegeName"
            label="所属学院"
            width="180"
          />
          <el-table-column
            prop="description"
            label="描述"
            min-width="200"
            show-overflow-tooltip
          />
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
            width="150"
            align="center"
            fixed="right"
          >
            <template #default="{ row }">
              <el-button
                type="primary"
                size="small"
                link
                :icon="Edit"
                @click="handleEditMajor(row)"
              >
                编辑
              </el-button>
              <el-button
                type="danger"
                size="small"
                link
                :icon="Delete"
                @click="handleDeleteMajor(row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <Pagination
          v-model:current-page="majorPagination.page"
          v-model:page-size="majorPagination.size"
          :total="majorPagination.total"
          @change="loadMajors"
        />
      </el-tab-pane>
    </el-tabs>

    <!-- 学院对话框 -->
    <el-dialog
      v-model="collegeDialogVisible"
      :title="collegeDialogTitle"
      width="600px"
      @close="handleCollegeDialogClose"
    >
      <el-form
        ref="collegeFormRef"
        :model="collegeForm"
        :rules="collegeRules"
        label-width="100px"
      >
        <el-form-item label="学院名称" prop="name">
          <el-input
            v-model="collegeForm.name"
            placeholder="请输入学院名称"
          />
        </el-form-item>
        <el-form-item label="学院代码" prop="code">
          <el-input
            v-model="collegeForm.code"
            placeholder="请输入学院代码"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="collegeForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入学院描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="collegeDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="submitting"
          @click="handleCollegeSubmit"
        >
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 专业对话框 -->
    <el-dialog
      v-model="majorDialogVisible"
      :title="majorDialogTitle"
      width="600px"
      @close="handleMajorDialogClose"
    >
      <el-form
        ref="majorFormRef"
        :model="majorForm"
        :rules="majorRules"
        label-width="100px"
      >
        <el-form-item label="所属学院" prop="collegeId">
          <el-select
            v-model="majorForm.collegeId"
            placeholder="请选择学院"
            style="width: 100%"
          >
            <el-option
              v-for="college in colleges"
              :key="college.id"
              :label="college.name"
              :value="college.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="专业名称" prop="name">
          <el-input
            v-model="majorForm.name"
            placeholder="请输入专业名称"
          />
        </el-form-item>
        <el-form-item label="专业代码" prop="code">
          <el-input
            v-model="majorForm.code"
            placeholder="请输入专业代码"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="majorForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入专业描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="majorDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="submitting"
          @click="handleMajorSubmit"
        >
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 分配管理员对话框 -->
    <el-dialog
      v-model="assignAdminVisible"
      title="分配学院管理员"
      width="500px"
    >
      <el-form
        ref="assignAdminFormRef"
        :model="assignAdminForm"
        :rules="assignAdminRules"
        label-width="100px"
      >
        <el-form-item label="学院管理员" prop="adminId">
          <el-select
            v-model="assignAdminForm.adminId"
            placeholder="请选择学院管理员"
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="admin in collegeAdmins"
              :key="admin.id"
              :label="`${admin.realName} (${admin.username})`"
              :value="admin.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignAdminVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="submitting"
          @click="handleAssignAdminSubmit"
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
  User
} from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { collegeApi, userApi } from '@/api'
import { formatDateTime } from '@/utils'
import type { CollegeResponse, CollegeRequest, MajorResponse, MajorRequest, UserListVO } from '@/types'

// 标签页
const activeTab = ref('college')

// 学院列表
const colleges = ref<CollegeResponse[]>([])
const collegeLoading = ref(false)
const collegePagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 专业列表
const majors = ref<MajorResponse[]>([])
const majorLoading = ref(false)
const majorPagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 专业搜索表单
const majorSearchForm = reactive({
  collegeId: undefined as number | undefined
})

// 学院对话框
const collegeDialogVisible = ref(false)
const collegeDialogTitle = computed(() => isEditCollege.value ? '编辑学院' : '新增学院')
const isEditCollege = ref(false)
const collegeFormRef = ref<FormInstance>()
const collegeForm = reactive<CollegeRequest>({
  name: '',
  code: '',
  description: ''
})

const collegeRules: FormRules = {
  name: [
    { required: true, message: '请输入学院名称', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入学院代码', trigger: 'blur' }
  ]
}

// 专业对话框
const majorDialogVisible = ref(false)
const majorDialogTitle = computed(() => isEditMajor.value ? '编辑专业' : '新增专业')
const isEditMajor = ref(false)
const majorFormRef = ref<FormInstance>()
const majorForm = reactive<MajorRequest>({
  collegeId: undefined,
  name: '',
  code: '',
  description: ''
})

const majorRules: FormRules = {
  collegeId: [
    { required: true, message: '请选择所属学院', trigger: 'change' }
  ],
  name: [
    { required: true, message: '请输入专业名称', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入专业代码', trigger: 'blur' }
  ]
}

// 分配管理员对话框
const assignAdminVisible = ref(false)
const assignAdminFormRef = ref<FormInstance>()
const assignAdminForm = reactive({
  collegeId: 0,
  adminId: undefined as number | undefined
})
const collegeAdmins = ref<UserListVO[]>([])

const assignAdminRules: FormRules = {
  adminId: [
    { required: true, message: '请选择学院管理员', trigger: 'change' }
  ]
}

const submitting = ref(false)

// 加载学院列表
const loadColleges = async () => {
  collegeLoading.value = true
  try {
    const response = await collegeApi.getCollegeList({
      page: collegePagination.page,
      size: collegePagination.size
    })
    colleges.value = response.data.records
    collegePagination.total = response.data.total
  } catch (error) {
    ElMessage.error('加载学院列表失败')
  } finally {
    collegeLoading.value = false
  }
}

// 加载专业列表
const loadMajors = async () => {
  majorLoading.value = true
  try {
    const params: any = {
      page: majorPagination.page,
      size: majorPagination.size
    }
    if (majorSearchForm.collegeId) {
      params.collegeId = majorSearchForm.collegeId
    }
    const response = await collegeApi.getMajorList(params)
    majors.value = response.data.records
    majorPagination.total = response.data.total
  } catch (error) {
    ElMessage.error('加载专业列表失败')
  } finally {
    majorLoading.value = false
  }
}

// 加载学院管理员列表
const loadCollegeAdmins = async () => {
  try {
    const response = await userApi.getUserList({
      role: 'COLLEGE_ADMIN',
      page: 1,
      size: 1000
    })
    collegeAdmins.value = response.data.records
  } catch (error) {
    ElMessage.error('加载学院管理员列表失败')
  }
}

// 新增学院
const handleAddCollege = () => {
  isEditCollege.value = false
  collegeDialogVisible.value = true
}

// 编辑学院
const handleEditCollege = (row: CollegeResponse) => {
  isEditCollege.value = true
  Object.assign(collegeForm, {
    id: row.id,
    name: row.name,
    code: row.code,
    description: row.description
  })
  collegeDialogVisible.value = true
}

// 提交学院表单
const handleCollegeSubmit = async () => {
  if (!collegeFormRef.value) return
  
  await collegeFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (isEditCollege.value) {
        await collegeApi.updateCollege(collegeForm.id!, collegeForm)
        ElMessage.success('更新学院成功')
      } else {
        await collegeApi.createCollege(collegeForm)
        ElMessage.success('创建学院成功')
      }
      collegeDialogVisible.value = false
      loadColleges()
    } catch (error) {
      ElMessage.error(isEditCollege.value ? '更新学院失败' : '创建学院失败')
    } finally {
      submitting.value = false
    }
  })
}

// 删除学院
const handleDeleteCollege = async (row: CollegeResponse) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除学院"${row.name}"吗？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    await collegeApi.deleteCollege(row.id)
    ElMessage.success('删除学院成功')
    loadColleges()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除学院失败')
    }
  }
}

// 分配管理员
const handleAssignAdmin = (row: CollegeResponse) => {
  assignAdminForm.collegeId = row.id
  assignAdminForm.adminId = row.adminId
  assignAdminVisible.value = true
}

// 提交分配管理员
const handleAssignAdminSubmit = async () => {
  if (!assignAdminFormRef.value) return
  
  await assignAdminFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      await collegeApi.assignAdmin(assignAdminForm.collegeId, assignAdminForm.adminId!)
      ElMessage.success('分配管理员成功')
      assignAdminVisible.value = false
      loadColleges()
    } catch (error) {
      ElMessage.error('分配管理员失败')
    } finally {
      submitting.value = false
    }
  })
}

// 新增专业
const handleAddMajor = () => {
  isEditMajor.value = false
  majorDialogVisible.value = true
}

// 编辑专业
const handleEditMajor = (row: MajorResponse) => {
  isEditMajor.value = true
  Object.assign(majorForm, {
    id: row.id,
    collegeId: row.collegeId,
    name: row.name,
    code: row.code,
    description: row.description
  })
  majorDialogVisible.value = true
}

// 提交专业表单
const handleMajorSubmit = async () => {
  if (!majorFormRef.value) return
  
  await majorFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (isEditMajor.value) {
        await collegeApi.updateMajor(majorForm.id!, majorForm)
        ElMessage.success('更新专业成功')
      } else {
        await collegeApi.createMajor(majorForm)
        ElMessage.success('创建专业成功')
      }
      majorDialogVisible.value = false
      loadMajors()
    } catch (error) {
      ElMessage.error(isEditMajor.value ? '更新专业失败' : '创建专业失败')
    } finally {
      submitting.value = false
    }
  })
}

// 删除专业
const handleDeleteMajor = async (row: MajorResponse) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除专业"${row.name}"吗？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    await collegeApi.deleteMajor(row.id)
    ElMessage.success('删除专业成功')
    loadMajors()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除专业失败')
    }
  }
}

// 学院对话框关闭
const handleCollegeDialogClose = () => {
  collegeFormRef.value?.resetFields()
  Object.assign(collegeForm, {
    name: '',
    code: '',
    description: ''
  })
}

// 专业对话框关闭
const handleMajorDialogClose = () => {
  majorFormRef.value?.resetFields()
  Object.assign(majorForm, {
    collegeId: undefined,
    name: '',
    code: '',
    description: ''
  })
}

onMounted(() => {
  loadColleges()
  loadMajors()
  loadCollegeAdmins()
})
</script>

<style scoped lang="scss">
.college-management {
  .toolbar {
    margin-bottom: 16px;
  }

  .search-form {
    margin-bottom: 16px;
  }

  :deep(.el-tabs__content) {
    padding: 20px;
  }
}
</style>