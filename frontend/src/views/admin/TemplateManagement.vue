<template>
  <div class="template-management">
    <!-- 操作按钮 -->
    <el-card class="toolbar-card" shadow="never">
      <el-button
        type="primary"
        :icon="Plus"
        @click="handleAdd"
      >
        新增模板
      </el-button>
      <el-button
        :icon="Refresh"
        @click="loadTemplates"
      >
        刷新
      </el-button>
    </el-card>

    <!-- 模板列表 -->
    <el-card shadow="never">
      <el-table
        v-loading="loading"
        :data="templates"
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
          label="模板名称"
          min-width="150"
        />
        <el-table-column
          prop="type"
          label="证书类型"
          width="120"
          align="center"
        >
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="description"
          label="描述"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column
          prop="isDefault"
          label="默认模板"
          width="100"
          align="center"
        >
          <template #default="{ row }">
            <el-tag :type="row.isDefault ? 'success' : 'info'">
              {{ row.isDefault ? '是' : '否' }}
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
              :icon="View"
              @click="handleView(row)"
            >
              查看
            </el-button>
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
              v-if="!row.isDefault"
              type="success"
              size="small"
              link
              :icon="Check"
              @click="handleSetDefault(row)"
            >
              设为默认
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
        @change="loadTemplates"
      />
    </el-card>

    <!-- 新增/编辑模板对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-form-item label="模板名称" prop="name">
          <el-input
            v-model="form.name"
            placeholder="请输入模板名称"
          />
        </el-form-item>
        <el-form-item label="证书类型" prop="type">
          <el-select
            v-model="form.type"
            placeholder="请选择证书类型"
            style="width: 100%"
          >
            <el-option label="毕业证书" value="GRADUATION" />
            <el-option label="学位证书" value="DEGREE" />
            <el-option label="荣誉证书" value="HONOR" />
            <el-option label="其他证书" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入模板描述"
          />
        </el-form-item>
        <el-form-item label="模板内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="10"
            placeholder="请输入模板内容（支持变量：{studentName}, {major}, {graduationDate}等）"
          />
        </el-form-item>
        <el-form-item label="字段配置" prop="fields">
          <div class="fields-config">
            <el-button
              type="primary"
              size="small"
              :icon="Plus"
              @click="handleAddField"
            >
              添加字段
            </el-button>
            <el-table
              :data="form.fields"
              border
              style="margin-top: 10px"
            >
              <el-table-column
                prop="name"
                label="字段名称"
                width="150"
              >
                <template #default="{ row, $index }">
                  <el-input
                    v-model="row.name"
                    placeholder="字段名称"
                    size="small"
                  />
                </template>
              </el-table-column>
              <el-table-column
                prop="label"
                label="字段标签"
                width="150"
              >
                <template #default="{ row, $index }">
                  <el-input
                    v-model="row.label"
                    placeholder="字段标签"
                    size="small"
                  />
                </template>
              </el-table-column>
              <el-table-column
                prop="type"
                label="字段类型"
                width="120"
              >
                <template #default="{ row, $index }">
                  <el-select
                    v-model="row.type"
                    placeholder="类型"
                    size="small"
                  >
                    <el-option label="文本" value="text" />
                    <el-option label="日期" value="date" />
                    <el-option label="数字" value="number" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column
                prop="required"
                label="必填"
                width="80"
                align="center"
              >
                <template #default="{ row, $index }">
                  <el-checkbox v-model="row.required" />
                </template>
              </el-table-column>
              <el-table-column
                prop="defaultValue"
                label="默认值"
                min-width="150"
              >
                <template #default="{ row, $index }">
                  <el-input
                    v-model="row.defaultValue"
                    placeholder="默认值"
                    size="small"
                  />
                </template>
              </el-table-column>
              <el-table-column
                label="操作"
                width="80"
                align="center"
              >
                <template #default="{ row, $index }">
                  <el-button
                    type="danger"
                    size="small"
                    link
                    :icon="Delete"
                    @click="handleRemoveField($index)"
                  >
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-form-item>
        <el-form-item label="设为默认" prop="isDefault">
          <el-switch
            v-model="form.isDefault"
            active-text="是"
            inactive-text="否"
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

    <!-- 查看模板对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      title="查看模板"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="模板名称">
          {{ viewTemplate?.name }}
        </el-descriptions-item>
        <el-descriptions-item label="证书类型">
          <el-tag :type="getTypeTagType(viewTemplate?.type || '')">
            {{ getTypeText(viewTemplate?.type || '') }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">
          {{ viewTemplate?.description }}
        </el-descriptions-item>
        <el-descriptions-item label="默认模板">
          <el-tag :type="viewTemplate?.isDefault ? 'success' : 'info'">
            {{ viewTemplate?.isDefault ? '是' : '否' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDateTime(viewTemplate?.createdAt) }}
        </el-descriptions-item>
        <el-descriptions-item label="模板内容" :span="2">
          <pre style="white-space: pre-wrap; word-wrap: break-word;">{{ viewTemplate?.content }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="字段配置" :span="2">
          <el-table
            :data="viewTemplate?.fields"
            border
            size="small"
          >
            <el-table-column prop="name" label="字段名称" width="120" />
            <el-table-column prop="label" label="字段标签" width="120" />
            <el-table-column prop="type" label="字段类型" width="100" />
            <el-table-column prop="required" label="必填" width="80" align="center">
              <template #default="{ row }">
                {{ row.required ? '是' : '否' }}
              </template>
            </el-table-column>
            <el-table-column prop="defaultValue" label="默认值" min-width="150" />
          </el-table>
        </el-descriptions-item>
      </el-descriptions>
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
  View,
  Check
} from '@element-plus/icons-vue'
import Pagination from '@/components/Pagination.vue'
import { templateApi } from '@/api'
import { formatDateTime } from '@/utils'
import type { TemplateResponse, TemplateRequest } from '@/types'

interface TemplateField {
  name: string
  label: string
  type: string
  required: boolean
  defaultValue: string
}

// 模板列表
const templates = ref<TemplateResponse[]>([])
const loading = ref(false)

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = computed(() => isEdit.value ? '编辑模板' : '新增模板')
const isEdit = ref(false)
const submitting = ref(false)

// 表单
const formRef = ref<FormInstance>()
const form = reactive<TemplateRequest & { fields: TemplateField[] }>({
  name: '',
  type: 'GRADUATION',
  description: '',
  content: '',
  fields: [],
  isDefault: false
})

// 表单验证规则
const rules: FormRules = {
  name: [
    { required: true, message: '请输入模板名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择证书类型', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入模板内容', trigger: 'blur' }
  ]
}

// 查看模板对话框
const viewDialogVisible = ref(false)
const viewTemplate = ref<TemplateResponse>()

// 加载模板列表
const loadTemplates = async () => {
  loading.value = true
  try {
    const response = await templateApi.getTemplateList({
      page: pagination.page,
      size: pagination.size
    })
    templates.value = response.data.records
    pagination.total = response.data.total
  } catch (error) {
    ElMessage.error('加载模板列表失败')
  } finally {
    loading.value = false
  }
}

// 新增模板
const handleAdd = () => {
  isEdit.value = false
  dialogVisible.value = true
}

// 编辑模板
const handleEdit = async (row: TemplateResponse) => {
  isEdit.value = true
  try {
    const response = await templateApi.getTemplateDetail(row.id)
    const template = response.data
    Object.assign(form, {
      id: template.id,
      name: template.name,
      type: template.type,
      description: template.description,
      content: template.content,
      fields: template.fields ? JSON.parse(template.fields) : [],
      isDefault: template.isDefault
    })
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('加载模板详情失败')
  }
}

// 查看模板
const handleView = async (row: TemplateResponse) => {
  try {
    const response = await templateApi.getTemplateDetail(row.id)
    viewTemplate.value = {
      ...response.data,
      fields: response.data.fields ? JSON.parse(response.data.fields) : []
    }
    viewDialogVisible.value = true
  } catch (error) {
    ElMessage.error('加载模板详情失败')
  }
}

// 添加字段
const handleAddField = () => {
  form.fields.push({
    name: '',
    label: '',
    type: 'text',
    required: false,
    defaultValue: ''
  })
}

// 删除字段
const handleRemoveField = (index: number) => {
  form.fields.splice(index, 1)
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      const data = {
        ...form,
        fields: JSON.stringify(form.fields)
      }
      
      if (isEdit.value) {
        await templateApi.updateTemplate(form.id!, data)
        ElMessage.success('更新模板成功')
      } else {
        await templateApi.createTemplate(data)
        ElMessage.success('创建模板成功')
      }
      dialogVisible.value = false
      loadTemplates()
    } catch (error) {
      ElMessage.error(isEdit.value ? '更新模板失败' : '创建模板失败')
    } finally {
      submitting.value = false
    }
  })
}

// 设为默认模板
const handleSetDefault = async (row: TemplateResponse) => {
  try {
    await ElMessageBox.confirm(
      `确定要将"${row.name}"设为默认模板吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await templateApi.setDefaultTemplate(row.id)
    ElMessage.success('设置默认模板成功')
    loadTemplates()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('设置默认模板失败')
    }
  }
}

// 删除模板
const handleDelete = async (row: TemplateResponse) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除模板"${row.name}"吗？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      }
    )
    
    await templateApi.deleteTemplate(row.id)
    ElMessage.success('删除模板成功')
    loadTemplates()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除模板失败')
    }
  }
}

// 对话框关闭
const handleDialogClose = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    name: '',
    type: 'GRADUATION',
    description: '',
    content: '',
    fields: [],
    isDefault: false
  })
}

// 获取类型文本
const getTypeText = (type: string) => {
  const typeMap: Record<string, string> = {
    GRADUATION: '毕业证书',
    DEGREE: '学位证书',
    HONOR: '荣誉证书',
    OTHER: '其他证书'
  }
  return typeMap[type] || type
}

// 获取类型标签类型
const getTypeTagType = (type: string) => {
  const typeMap: Record<string, any> = {
    GRADUATION: 'success',
    DEGREE: 'primary',
    HONOR: 'warning',
    OTHER: 'info'
  }
  return typeMap[type] || ''
}

onMounted(() => {
  loadTemplates()
})
</script>

<style scoped lang="scss">
.template-management {
  .toolbar-card {
    margin-bottom: 16px;
  }

  .fields-config {
    width: 100%;
  }
}
</style>