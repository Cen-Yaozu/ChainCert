<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1 class="title">区块链证书存证系统</h1>
        <p class="subtitle">Blockchain Certificate System</p>
      </div>

      <el-form
        ref="formRef"
        :model="loginForm"
        :rules="rules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            size="large"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item prop="captcha">
          <div class="captcha-wrapper">
            <el-input
              v-model="loginForm.captcha"
              placeholder="请输入验证码"
              :prefix-icon="Key"
              size="large"
              clearable
              @keyup.enter="handleLogin"
            />
            <div class="captcha-image" @click="refreshCaptcha">
              <el-image
                v-if="captchaImage"
                :src="captchaImage"
                fit="contain"
                alt="验证码"
              />
              <el-icon v-else class="loading-icon"><Loading /></el-icon>
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <div class="form-options">
            <el-checkbox v-model="rememberMe">记住密码</el-checkbox>
            <el-link type="primary" :underline="false" @click="handleForgotPassword">
              忘记密码？
            </el-link>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-button"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <el-link type="info" :underline="false" @click="goToVerify">
          证书核验
        </el-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock, Key, Loading } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { authApi } from '@/api'
import { requiredRule } from '@/utils/validate'
import { setEncryptedStorage, getEncryptedStorage, removeEncryptedStorage } from '@/utils/crypto'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 表单引用
const formRef = ref<FormInstance>()

// 登录表单
const loginForm = reactive({
  username: '',
  password: '',
  captcha: '',
  captchaKey: '',
})

// 记住密码
const rememberMe = ref(false)

// 加载状态
const loading = ref(false)

// 验证码图片
const captchaImage = ref('')

// 表单验证规则
const rules: FormRules = {
  username: [requiredRule('请输入用户名')],
  password: [requiredRule('请输入密码')],
  captcha: [requiredRule('请输入验证码')],
}

/**
 * 获取验证码
 */
const getCaptcha = async () => {
  try {
    const response = await authApi.getCaptcha()
    console.log('验证码响应:', response)
    
    // 处理验证码图片数据
    if (response.image) {
      // 如果已经是完整的 data URL，直接使用
      if (response.image.startsWith('data:')) {
        captchaImage.value = response.image
      } else {
        // 否则添加 data URL 前缀
        captchaImage.value = `data:image/png;base64,${response.image}`
      }
    }
    
    loginForm.captchaKey = response.key
  } catch (error) {
    console.error('获取验证码失败:', error)
    ElMessage.error('获取验证码失败')
  }
}

/**
 * 刷新验证码
 */
const refreshCaptcha = () => {
  loginForm.captcha = ''
  getCaptcha()
}

/**
 * 处理登录
 */
const handleLogin = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    loading.value = true

    await authStore.login({
      username: loginForm.username,
      password: loginForm.password,
      captcha: loginForm.captcha,
      captchaKey: loginForm.captchaKey,
    })

    // 记住密码
    if (rememberMe.value) {
      setEncryptedStorage('remembered_credentials', {
        username: loginForm.username,
        password: loginForm.password,
      })
    } else {
      removeEncryptedStorage('remembered_credentials')
    }

    ElMessage.success('登录成功')

    // 跳转到重定向页面或首页
    const redirect = (route.query.redirect as string) || '/dashboard'
    router.push(redirect)
  } catch (error: any) {
    ElMessage.error(error.message || '登录失败')
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

/**
 * 忘记密码
 */
const handleForgotPassword = () => {
  ElMessage.info('请联系管理员重置密码')
}

/**
 * 跳转到证书核验页面
 */
const goToVerify = () => {
  router.push('/verify')
}

/**
 * 初始化
 */
onMounted(() => {
  // 获取验证码
  getCaptcha()

  // 恢复记住的密码
  const credentials = getEncryptedStorage<{ username: string; password: string }>(
    'remembered_credentials'
  )
  if (credentials) {
    loginForm.username = credentials.username
    loginForm.password = credentials.password
    rememberMe.value = true
  }
})
</script>

<style scoped lang="scss">
.login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-box {
  width: 100%;
  max-width: 420px;
  padding: 40px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;

  .title {
    font-size: 28px;
    font-weight: 600;
    color: #303133;
    margin: 0 0 8px 0;
  }

  .subtitle {
    font-size: 14px;
    color: #909399;
    margin: 0;
  }
}

.login-form {
  .captcha-wrapper {
    display: flex;
    gap: 12px;

    .el-input {
      flex: 1;
    }

    .captcha-image {
      width: 120px;
      height: 40px;
      border: 1px solid var(--el-border-color);
      border-radius: 4px;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--el-fill-color-light);
      transition: all 0.3s;

      &:hover {
        border-color: var(--el-color-primary);
      }

      .el-image {
        width: 100%;
        height: 100%;
      }

      .loading-icon {
        font-size: 24px;
        color: var(--el-color-primary);
        animation: rotate 1s linear infinite;
      }
    }
  }

  .form-options {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
  }

  .login-button {
    width: 100%;
    margin-top: 8px;
  }
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid var(--el-border-color-lighter);
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 768px) {
  .login-box {
    padding: 30px 20px;
  }

  .login-header {
    .title {
      font-size: 24px;
    }
  }
}
</style>