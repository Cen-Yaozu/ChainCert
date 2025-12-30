<template>
  <div class="register-container">
    <div class="register-card">
      <div class="register-header">
        <img src="@/assets/logo.svg" alt="Logo" class="logo" />
        <h1>区块链证书存证系统</h1>
        <p>用户注册</p>
      </div>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="register-form"
        label-position="top"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item label="姓名" prop="name">
          <el-input
            v-model="registerForm.name"
            placeholder="请输入真实姓名"
            prefix-icon="UserFilled"
          />
        </el-form-item>

        <el-form-item label="角色" prop="role">
          <el-select v-model="registerForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="学生" value="STUDENT" />
            <el-option label="学院管理员" value="COLLEGE_ADMIN" />
            <el-option label="学校管理员" value="SCHOOL_ADMIN" />
            <el-option label="系统管理员" value="SYSTEM_ADMIN" />
          </el-select>
        </el-form-item>

        <!-- 学院选择（学生和学院管理员必填） -->
        <el-form-item
          v-if="registerForm.role === 'STUDENT' || registerForm.role === 'COLLEGE_ADMIN'"
          label="学院"
          prop="collegeId"
        >
          <el-select
            v-model="registerForm.collegeId"
            placeholder="请选择学院"
            style="width: 100%"
            @change="handleCollegeChange"
          >
            <el-option
              v-for="college in colleges"
              :key="college.id"
              :label="college.name"
              :value="String(college.id)"
            />
          </el-select>
        </el-form-item>

        <!-- 专业选择（学生必填） -->
        <el-form-item
          v-if="registerForm.role === 'STUDENT'"
          label="专业"
          prop="majorId"
        >
          <el-select
            v-model="registerForm.majorId"
            placeholder="请选择专业"
            style="width: 100%"
            :disabled="!registerForm.collegeId"
          >
            <el-option
              v-for="major in majors"
              :key="major.id"
              :label="major.name"
              :value="String(major.id)"
            />
          </el-select>
        </el-form-item>

        <!-- 学号（学生必填） -->
        <el-form-item
          v-if="registerForm.role === 'STUDENT'"
          label="学号"
          prop="studentNo"
        >
          <el-input
            v-model="registerForm.studentNo"
            placeholder="请输入学号"
            prefix-icon="Tickets"
          />
        </el-form-item>

        <!-- 工号（管理员可选） -->
        <el-form-item
          v-if="registerForm.role !== 'STUDENT'"
          label="工号"
          prop="employeeNo"
        >
          <el-input
            v-model="registerForm.employeeNo"
            placeholder="请输入工号（可选）"
            prefix-icon="Tickets"
          />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱（可选）"
            prefix-icon="Message"
          />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入手机号（可选）"
            prefix-icon="Phone"
          />
        </el-form-item>

        <el-form-item label="验证码" prop="captcha">
          <div class="captcha-row">
            <el-input
              v-model="registerForm.captcha"
              placeholder="请输入验证码"
              prefix-icon="Key"
              style="flex: 1"
            />
            <img
              :src="captchaImage"
              alt="验证码"
              class="captcha-image"
              @click="refreshCaptcha"
              title="点击刷新验证码"
            />
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            class="register-button"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>

        <div class="login-link">
          已有账号？<router-link to="/login">立即登录</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const registerFormRef = ref<FormInstance>()
const loading = ref(false)
const captchaImage = ref('')
const captchaKey = ref('')

// 学院和专业数据
const colleges = ref<Array<{ id: number; name: string; code: string }>>([])
const majors = ref<Array<{ id: number; name: string; code: string }>>([])

// 表单数据
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  name: '',
  role: '',
  collegeId: '',
  majorId: '',
  studentNo: '',
  employeeNo: '',
  email: '',
  phone: '',
  captcha: '',
})

// 验证确认密码
const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 表单验证规则
const registerRules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在 3 到 50 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度在 6 到 100 个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { max: 50, message: '姓名长度不能超过 50 个字符', trigger: 'blur' },
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' },
  ],
  collegeId: [
    { required: true, message: '请选择学院', trigger: 'change' },
  ],
  majorId: [
    { required: true, message: '请选择专业', trigger: 'change' },
  ],
  studentNo: [
    { required: true, message: '请输入学号', trigger: 'blur' },
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' },
  ],
})

// 获取验证码
const refreshCaptcha = async () => {
  try {
    const res = await request.get('/auth/captcha')
    if (res.data) {
      captchaKey.value = res.data.key
      captchaImage.value = res.data.image
    }
  } catch (error) {
    console.error('获取验证码失败:', error)
  }
}

// 获取学院列表
const fetchColleges = async () => {
  try {
    // 使用公开接口或模拟数据
    colleges.value = [
      { id: 1, name: '计算机学院', code: 'CS' },
      { id: 2, name: '软件学院', code: 'SE' },
      { id: 3, name: '信息学院', code: 'IS' },
    ]
  } catch (error) {
    console.error('获取学院列表失败:', error)
  }
}

// 获取专业列表
const fetchMajors = async (collegeId: string) => {
  try {
    // 根据学院ID获取专业列表
    const majorData: Record<string, Array<{ id: number; name: string; code: string }>> = {
      '1': [
        { id: 1, name: '计算机科学与技术', code: 'CS01' },
        { id: 2, name: '人工智能', code: 'CS02' },
      ],
      '2': [
        { id: 3, name: '软件工程', code: 'SE01' },
        { id: 4, name: '数据科学与大数据技术', code: 'SE02' },
      ],
      '3': [
        { id: 5, name: '信息安全', code: 'IS01' },
      ],
    }
    majors.value = majorData[collegeId] || []
  } catch (error) {
    console.error('获取专业列表失败:', error)
  }
}

// 学院变化时获取专业
const handleCollegeChange = (collegeId: string) => {
  registerForm.majorId = ''
  if (collegeId) {
    fetchMajors(collegeId)
  } else {
    majors.value = []
  }
}

// 监听角色变化，清空相关字段
watch(() => registerForm.role, () => {
  registerForm.collegeId = ''
  registerForm.majorId = ''
  registerForm.studentNo = ''
  registerForm.employeeNo = ''
  majors.value = []
})

// 注册
const handleRegister = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const data = {
        ...registerForm,
        captchaKey: captchaKey.value,
      }

      const res: any = await request.post('/auth/register', data)
      
      if (res.success) {
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      } else {
        ElMessage.error(res.message || '注册失败')
        refreshCaptcha()
      }
    } catch (error: any) {
      ElMessage.error(error.message || '注册失败，请稍后重试')
      refreshCaptcha()
    } finally {
      loading.value = false
    }
  })
}

onMounted(() => {
  refreshCaptcha()
  fetchColleges()
})
</script>

<style lang="scss" scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-card {
  width: 100%;
  max-width: 500px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  padding: 40px;
  max-height: 90vh;
  overflow-y: auto;
}

.register-header {
  text-align: center;
  margin-bottom: 30px;

  .logo {
    width: 60px;
    height: 60px;
    margin-bottom: 16px;
  }

  h1 {
    font-size: 24px;
    color: #303133;
    margin-bottom: 8px;
  }

  p {
    font-size: 14px;
    color: #909399;
  }
}

.register-form {
  .captcha-row {
    display: flex;
    gap: 12px;
  }

  .captcha-image {
    height: 40px;
    cursor: pointer;
    border-radius: 4px;
    border: 1px solid #dcdfe6;
  }

  .register-button {
    width: 100%;
    height: 44px;
    font-size: 16px;
  }
}

.login-link {
  text-align: center;
  margin-top: 16px;
  color: #909399;

  a {
    color: #409eff;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}
</style>