<template>
  <div class="approval-flow-config">
    <!-- 页面标题 -->
    <el-card class="header-card" shadow="never">
      <div class="page-header">
        <h2>审批流配置</h2>
        <p class="description">配置证书申请的审批流程，可以选择是否需要学校级别审批</p>
      </div>
    </el-card>

    <!-- 审批流程配置 -->
    <el-card shadow="never" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>当前审批流程</span>
          <el-button type="primary" :icon="Refresh" @click="loadConfig">
            刷新
          </el-button>
        </div>
      </template>

      <!-- 流程图示 -->
      <div class="flow-diagram">
        <div class="flow-step">
          <div class="step-icon student">
            <el-icon :size="32"><User /></el-icon>
          </div>
          <div class="step-info">
            <h4>学生提交申请</h4>
            <p>填写申请表单并上传证明材料</p>
          </div>
        </div>

        <div class="flow-arrow">
          <el-icon :size="24"><ArrowRight /></el-icon>
        </div>

        <div class="flow-step">
          <div class="step-icon college">
            <el-icon :size="32"><School /></el-icon>
          </div>
          <div class="step-info">
            <h4>学院审批</h4>
            <p>学院教师进行初审</p>
          </div>
        </div>

        <div class="flow-arrow" :class="{ disabled: !config.requireSchoolApproval }">
          <el-icon :size="24"><ArrowRight /></el-icon>
        </div>

        <div class="flow-step" :class="{ disabled: !config.requireSchoolApproval }">
          <div class="step-icon school">
            <el-icon :size="32"><OfficeBuilding /></el-icon>
          </div>
          <div class="step-info">
            <h4>学校审批</h4>
            <p>学校教师进行终审</p>
            <el-tag v-if="!config.requireSchoolApproval" type="info" size="small">
              已跳过
            </el-tag>
          </div>
        </div>

        <div class="flow-arrow">
          <el-icon :size="24"><ArrowRight /></el-icon>
        </div>

        <div class="flow-step">
          <div class="step-icon certificate">
            <el-icon :size="32"><Medal /></el-icon>
          </div>
          <div class="step-info">
            <h4>生成证书</h4>
            <p>自动生成证书并上链存证</p>
          </div>
        </div>
      </div>

      <!-- 配置选项 -->
      <el-divider content-position="left">配置选项</el-divider>

      <el-form :model="config" label-width="180px" class="config-form">
        <el-form-item label="是否需要学校审批">
          <el-switch
            v-model="config.requireSchoolApproval"
            active-text="需要"
            inactive-text="不需要"
            :loading="saving"
          />
          <div class="form-tip">
            <el-icon><InfoFilled /></el-icon>
            <span>开启后，学院审批通过的申请需要学校教师进行终审；关闭后，学院审批通过即可生成证书</span>
          </div>
        </el-form-item>

        <el-form-item label="审批超时提醒（天）">
          <el-input-number
            v-model="config.approvalTimeoutDays"
            :min="1"
            :max="30"
            :disabled="saving"
          />
          <div class="form-tip">
            <el-icon><InfoFilled /></el-icon>
            <span>超过设定天数未审批的申请将发送提醒通知</span>
          </div>
        </el-form-item>

        <el-form-item label="自动撤销超时申请">
          <el-switch
            v-model="config.autoRevokeTimeout"
            active-text="开启"
            inactive-text="关闭"
            :loading="saving"
          />
          <div class="form-tip">
            <el-icon><InfoFilled /></el-icon>
            <span>开启后，超过30天未处理的申请将自动撤销</span>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">
            保存配置
          </el-button>
          <el-button @click="handleReset">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 审批流程说明 -->
    <el-card shadow="never" class="info-card">
      <template #header>
        <div class="card-header">
          <span>审批流程说明</span>
        </div>
      </template>

      <el-timeline>
        <el-timeline-item timestamp="第一步" placement="top" type="primary">
          <el-card shadow="hover">
            <h4>学生提交申请</h4>
            <p>学生登录系统后，填写证书申请表单，上传相关证明材料（PDF/图片格式，1-3个文件）。</p>
          </el-card>
        </el-timeline-item>
        <el-timeline-item timestamp="第二步" placement="top" type="success">
          <el-card shadow="hover">
            <h4>学院教师审批</h4>
            <p>学院教师查看申请详情和证明材料，进行初步审核。审批时需要使用数字签名确认。</p>
          </el-card>
        </el-timeline-item>
        <el-timeline-item timestamp="第三步" placement="top" type="warning" v-if="config.requireSchoolApproval">
          <el-card shadow="hover">
            <h4>学校教师审批</h4>
            <p>学校教师对学院审批通过的申请进行终审。审批时同样需要数字签名确认。</p>
          </el-card>
        </el-timeline-item>
        <el-timeline-item timestamp="最后一步" placement="top" type="danger">
          <el-card shadow="hover">
            <h4>证书生成与上链</h4>
            <p>审批通过后，系统自动生成证书PDF，上传至IPFS存储，并将证书哈希上链存证。</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Refresh,
  User,
  School,
  OfficeBuilding,
  Medal,
  ArrowRight,
  InfoFilled
} from '@element-plus/icons-vue'

// 配置数据
const config = reactive({
  requireSchoolApproval: true,
  approvalTimeoutDays: 7,
  autoRevokeTimeout: false
})

// 原始配置（用于重置）
const originalConfig = ref({
  requireSchoolApproval: true,
  approvalTimeoutDays: 7,
  autoRevokeTimeout: false
})

// 加载状态
const loading = ref(false)
const saving = ref(false)

/**
 * 加载配置
 */
const loadConfig = async () => {
  loading.value = true
  try {
    // TODO: 调用后端API获取配置
    // const response = await adminApi.getApprovalFlowConfig()
    // Object.assign(config, response)
    
    // 模拟数据
    await new Promise(resolve => setTimeout(resolve, 500))
    config.requireSchoolApproval = true
    config.approvalTimeoutDays = 7
    config.autoRevokeTimeout = false
    
    // 保存原始配置
    originalConfig.value = { ...config }
  } catch (error) {
    ElMessage.error('加载配置失败')
  } finally {
    loading.value = false
  }
}

/**
 * 保存配置
 */
const handleSave = async () => {
  saving.value = true
  try {
    // TODO: 调用后端API保存配置
    // await adminApi.saveApprovalFlowConfig(config)
    
    // 模拟保存
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 更新原始配置
    originalConfig.value = { ...config }
    
    ElMessage.success('配置保存成功')
  } catch (error) {
    ElMessage.error('保存配置失败')
  } finally {
    saving.value = false
  }
}

/**
 * 重置配置
 */
const handleReset = () => {
  Object.assign(config, originalConfig.value)
  ElMessage.info('已重置为上次保存的配置')
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped lang="scss">
.approval-flow-config {
  .header-card {
    margin-bottom: 16px;

    .page-header {
      h2 {
        margin: 0 0 8px 0;
        font-size: 20px;
        color: var(--el-text-color-primary);
      }

      .description {
        margin: 0;
        font-size: 14px;
        color: var(--el-text-color-secondary);
      }
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .flow-diagram {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 40px 20px;
    background: var(--el-fill-color-light);
    border-radius: 8px;
    margin-bottom: 20px;
    overflow-x: auto;

    .flow-step {
      display: flex;
      flex-direction: column;
      align-items: center;
      min-width: 120px;
      transition: all 0.3s;

      &.disabled {
        opacity: 0.4;
      }

      .step-icon {
        width: 64px;
        height: 64px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 12px;
        color: #fff;

        &.student {
          background: linear-gradient(135deg, #409EFF, #66b1ff);
        }

        &.college {
          background: linear-gradient(135deg, #67C23A, #85ce61);
        }

        &.school {
          background: linear-gradient(135deg, #E6A23C, #ebb563);
        }

        &.certificate {
          background: linear-gradient(135deg, #F56C6C, #f78989);
        }
      }

      .step-info {
        text-align: center;

        h4 {
          margin: 0 0 4px 0;
          font-size: 14px;
          color: var(--el-text-color-primary);
        }

        p {
          margin: 0;
          font-size: 12px;
          color: var(--el-text-color-secondary);
        }

        .el-tag {
          margin-top: 8px;
        }
      }
    }

    .flow-arrow {
      margin: 0 20px;
      color: var(--el-color-primary);
      margin-bottom: 40px;

      &.disabled {
        opacity: 0.3;
      }
    }
  }

  .config-form {
    max-width: 600px;

    .form-tip {
      display: flex;
      align-items: flex-start;
      gap: 4px;
      margin-top: 8px;
      font-size: 12px;
      color: var(--el-text-color-secondary);

      .el-icon {
        margin-top: 2px;
        flex-shrink: 0;
      }
    }
  }

  .info-card {
    margin-top: 16px;

    h4 {
      margin: 0 0 8px 0;
      font-size: 16px;
    }

    p {
      margin: 0;
      font-size: 14px;
      color: var(--el-text-color-secondary);
    }
  }
}

@media (max-width: 768px) {
  .approval-flow-config {
    .flow-diagram {
      flex-direction: column;
      padding: 20px;

      .flow-arrow {
        transform: rotate(90deg);
        margin: 16px 0;
      }
    }
  }
}
</style>