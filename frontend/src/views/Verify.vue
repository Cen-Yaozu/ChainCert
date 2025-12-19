<template>
  <div class="verify-container">
    <div class="verify-box">
      <div class="verify-header">
        <h1 class="title">证书核验</h1>
        <p class="subtitle">Certificate Verification</p>
      </div>

      <el-form
        ref="formRef"
        :model="verifyForm"
        :rules="rules"
        class="verify-form"
        @submit.prevent="handleVerify"
      >
        <el-form-item prop="certificateNumber">
          <el-input
            v-model="verifyForm.certificateNumber"
            placeholder="请输入证书编号（格式：CERT-YYYYMMDD-XXXXXX）"
            :prefix-icon="Document"
            size="large"
            clearable
            @keyup.enter="handleVerify"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="verify-button"
            @click="handleVerify"
          >
            {{ loading ? '核验中...' : '开始核验' }}
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 核验结果 -->
      <div v-if="verificationResult" class="verification-result">
        <el-result
          :icon="verificationResult.valid ? 'success' : 'error'"
          :title="verificationResult.valid ? '证书有效' : '证书无效'"
          :sub-title="verificationResult.message"
        >
          <template #extra>
            <div v-if="verificationResult.valid && verificationResult.certificate" class="certificate-info">
              <el-descriptions :column="1" border>
                <el-descriptions-item label="证书编号">
                  {{ verificationResult.certificate.certificateNumber }}
                </el-descriptions-item>
                <el-descriptions-item label="学生姓名">
                  {{ verificationResult.certificate.studentName }}
                </el-descriptions-item>
                <el-descriptions-item label="学号">
                  {{ verificationResult.certificate.studentId }}
                </el-descriptions-item>
                <el-descriptions-item label="学院">
                  {{ verificationResult.certificate.collegeName }}
                </el-descriptions-item>
                <el-descriptions-item label="专业">
                  {{ verificationResult.certificate.majorName }}
                </el-descriptions-item>
                <el-descriptions-item label="证书类型">
                  {{ verificationResult.certificate.certificateType }}
                </el-descriptions-item>
                <el-descriptions-item label="颁发日期">
                  {{ formatDate(verificationResult.certificate.issueDate) }}
                </el-descriptions-item>
                <el-descriptions-item label="证书状态">
                  <StatusTag
                    :status="verificationResult.certificate.status"
                    status-type="certificate"
                  />
                </el-descriptions-item>
              </el-descriptions>

              <!-- 区块链信息 -->
              <el-divider content-position="left">区块链信息</el-divider>
              <el-descriptions :column="1" border>
                <el-descriptions-item label="区块链哈希">
                  <el-text class="hash-text" truncated>
                    {{ verificationResult.certificate.blockchainHash }}
                  </el-text>
                </el-descriptions-item>
                <el-descriptions-item label="交易哈希">
                  <el-text class="hash-text" truncated>
                    {{ verificationResult.certificate.transactionHash }}
                  </el-text>
                </el-descriptions-item>
                <el-descriptions-item label="区块高度">
                  {{ verificationResult.certificate.blockNumber }}
                </el-descriptions-item>
                <el-descriptions-item label="上链时间">
                  {{ formatDateTime(verificationResult.certificate.blockchainTimestamp) }}
                </el-descriptions-item>
              </el-descriptions>

              <!-- IPFS 信息 -->
              <el-divider content-position="left">IPFS 信息</el-divider>
              <el-descriptions :column="1" border>
                <el-descriptions-item label="IPFS 哈希">
                  <el-text class="hash-text" truncated>
                    {{ verificationResult.certificate.ipfsHash }}
                  </el-text>
                </el-descriptions-item>
              </el-descriptions>

              <!-- 操作按钮 -->
              <div class="action-buttons">
                <el-button
                  type="primary"
                  :icon="Download"
                  @click="handleDownload"
                >
                  下载证书
                </el-button>
                <el-button @click="handleReset">
                  重新核验
                </el-button>
              </div>
            </div>
          </template>
        </el-result>
      </div>

      <div class="verify-footer">
        <el-link type="info" :underline="false" @click="goToLogin">
          返回登录
        </el-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Document, Download } from '@element-plus/icons-vue'
import { certificateApi } from '@/api'
import { requiredRule, validateCertNumber } from '@/utils/validate'
import { formatDate, formatDateTime } from '@/utils/format'
import { StatusTag } from '@/components'
import type { VerificationResult } from '@/types'

const router = useRouter()

// 表单引用
const formRef = ref<FormInstance>()

// 核验表单
const verifyForm = reactive({
  certificateNumber: '',
})

// 加载状态
const loading = ref(false)

// 核验结果
const verificationResult = ref<VerificationResult | null>(null)

// 表单验证规则
const rules: FormRules = {
  certificateNumber: [
    requiredRule('请输入证书编号'),
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (!value) {
          callback()
        } else if (!validateCertNumber(value)) {
          callback(new Error('证书编号格式不正确'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

/**
 * 处理核验
 */
const handleVerify = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    loading.value = true
    verificationResult.value = null

    const result = await certificateApi.verify({ certificateNumber: verifyForm.certificateNumber })
    verificationResult.value = result

    if (result.valid) {
      ElMessage.success('证书核验成功')
    } else {
      ElMessage.warning('证书核验失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '核验失败')
  } finally {
    loading.value = false
  }
}

/**
 * 下载证书
 */
const handleDownload = async () => {
  if (!verificationResult.value?.certificate) return

  try {
    await certificateApi.download(verificationResult.value.certificate.id)
    ElMessage.success('证书下载成功')
  } catch (error: any) {
    ElMessage.error(error.message || '下载失败')
  }
}

/**
 * 重置
 */
const handleReset = () => {
  verifyForm.certificateNumber = ''
  verificationResult.value = null
  formRef.value?.clearValidate()
}

/**
 * 返回登录
 */
const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped lang="scss">
.verify-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.verify-box {
  width: 100%;
  max-width: 800px;
  padding: 40px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.verify-header {
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

.verify-form {
  .verify-button {
    width: 100%;
  }
}

.verification-result {
  margin-top: 32px;

  .certificate-info {
    width: 100%;
    text-align: left;

    .hash-text {
      max-width: 400px;
      font-family: 'Courier New', monospace;
      font-size: 12px;
    }

    .el-divider {
      margin: 24px 0 16px 0;
    }

    .action-buttons {
      display: flex;
      gap: 12px;
      justify-content: center;
      margin-top: 24px;
    }
  }
}

.verify-footer {
  text-align: center;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid var(--el-border-color-lighter);
}

@media (max-width: 768px) {
  .verify-box {
    padding: 30px 20px;
  }

  .verify-header {
    .title {
      font-size: 24px;
    }
  }

  .verification-result {
    .certificate-info {
      .hash-text {
        max-width: 200px;
      }

      .action-buttons {
        flex-direction: column;

        .el-button {
          width: 100%;
        }
      }
    }
  }
}
</style>