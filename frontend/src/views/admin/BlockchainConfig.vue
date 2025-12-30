<template>
  <div class="blockchain-config">
    <!-- 页面标题 -->
    <el-card class="header-card" shadow="never">
      <div class="page-header">
        <h2>区块链配置</h2>
        <p class="description">查看区块链网络状态、合约信息和节点连接状态</p>
      </div>
    </el-card>

    <!-- 网络状态概览 -->
    <div class="status-cards">
      <el-card shadow="hover" class="status-card">
        <div class="status-content">
          <div class="status-icon" :class="networkStatus.connected ? 'success' : 'danger'">
            <el-icon :size="32">
              <Connection v-if="networkStatus.connected" />
              <CircleClose v-else />
            </el-icon>
          </div>
          <div class="status-info">
            <h3>网络状态</h3>
            <p :class="networkStatus.connected ? 'text-success' : 'text-danger'">
              {{ networkStatus.connected ? '已连接' : '未连接' }}
            </p>
          </div>
        </div>
      </el-card>

      <el-card shadow="hover" class="status-card">
        <div class="status-content">
          <div class="status-icon primary">
            <el-icon :size="32"><Box /></el-icon>
          </div>
          <div class="status-info">
            <h3>当前区块高度</h3>
            <p>{{ networkStatus.blockNumber || '-' }}</p>
          </div>
        </div>
      </el-card>

      <el-card shadow="hover" class="status-card">
        <div class="status-content">
          <div class="status-icon warning">
            <el-icon :size="32"><Cpu /></el-icon>
          </div>
          <div class="status-info">
            <h3>节点数量</h3>
            <p>{{ networkStatus.nodeCount || '-' }}</p>
          </div>
        </div>
      </el-card>

      <el-card shadow="hover" class="status-card">
        <div class="status-content">
          <div class="status-icon info">
            <el-icon :size="32"><Document /></el-icon>
          </div>
          <div class="status-info">
            <h3>存证数量</h3>
            <p>{{ networkStatus.certificateCount || '-' }}</p>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 合约信息 -->
    <el-card shadow="never" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>智能合约信息</span>
          <el-button type="primary" :icon="Refresh" @click="loadContractInfo">
            刷新
          </el-button>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="合约名称">
          CertificateRegistry
        </el-descriptions-item>
        <el-descriptions-item label="合约版本">
          {{ contractInfo.version || 'v1.0.0' }}
        </el-descriptions-item>
        <el-descriptions-item label="合约地址" :span="2">
          <div class="address-wrapper">
            <el-text class="address-text" truncated>
              {{ contractInfo.address || '未部署' }}
            </el-text>
            <el-button
              v-if="contractInfo.address"
              type="primary"
              link
              :icon="CopyDocument"
              @click="copyAddress"
            >
              复制
            </el-button>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="部署时间">
          {{ contractInfo.deployTime ? formatDateTime(contractInfo.deployTime) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="部署区块">
          {{ contractInfo.deployBlock || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="合约状态">
          <el-tag :type="contractInfo.address ? 'success' : 'danger'">
            {{ contractInfo.address ? '已部署' : '未部署' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="Solidity版本">
          0.4.25
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 节点信息 -->
    <el-card shadow="never" class="node-card">
      <template #header>
        <div class="card-header">
          <span>节点连接信息</span>
        </div>
      </template>

      <el-table :data="nodes" stripe border>
        <el-table-column prop="id" label="节点ID" width="80" align="center" />
        <el-table-column prop="name" label="节点名称" min-width="120" />
        <el-table-column prop="endpoint" label="连接地址" min-width="200">
          <template #default="{ row }">
            <el-text class="endpoint-text" truncated>{{ row.endpoint }}</el-text>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'online' ? 'success' : 'danger'">
              {{ row.status === 'online' ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="blockNumber" label="区块高度" width="120" align="center" />
        <el-table-column prop="latency" label="延迟" width="100" align="center">
          <template #default="{ row }">
            <span :class="getLatencyClass(row.latency)">{{ row.latency }}ms</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- FISCO BCOS 配置 -->
    <el-card shadow="never" class="config-card">
      <template #header>
        <div class="card-header">
          <span>FISCO BCOS 配置</span>
        </div>
      </template>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="网络类型">
          FISCO BCOS 2.x
        </el-descriptions-item>
        <el-descriptions-item label="群组ID">
          {{ fiscoConfig.groupId || '1' }}
        </el-descriptions-item>
        <el-descriptions-item label="Channel端口">
          {{ fiscoConfig.channelPort || '20200' }}
        </el-descriptions-item>
        <el-descriptions-item label="JSON-RPC端口">
          {{ fiscoConfig.rpcPort || '8545' }}
        </el-descriptions-item>
        <el-descriptions-item label="SDK版本">
          {{ fiscoConfig.sdkVersion || '2.9.2' }}
        </el-descriptions-item>
        <el-descriptions-item label="共识算法">
          PBFT
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 操作按钮 -->
    <el-card shadow="never" class="action-card">
      <template #header>
        <div class="card-header">
          <span>操作</span>
        </div>
      </template>

      <div class="action-buttons">
        <el-button type="primary" :icon="Refresh" :loading="syncing" @click="handleSync">
          一键同步数据
        </el-button>
        <el-button :icon="Download" @click="handleExportConfig">
          导出配置
        </el-button>
        <el-button :icon="View" @click="handleViewLogs">
          查看区块链日志
        </el-button>
      </div>

      <el-alert
        v-if="syncResult"
        :title="syncResult.success ? '同步成功' : '同步失败'"
        :type="syncResult.success ? 'success' : 'error'"
        :description="syncResult.message"
        show-icon
        closable
        class="sync-alert"
        @close="syncResult = null"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Refresh,
  Connection,
  CircleClose,
  Box,
  Cpu,
  Document,
  CopyDocument,
  Download,
  View
} from '@element-plus/icons-vue'
import { formatDateTime } from '@/utils/format'

// 网络状态
const networkStatus = reactive({
  connected: true,
  blockNumber: 12345,
  nodeCount: 4,
  certificateCount: 156
})

// 合约信息
const contractInfo = reactive({
  address: '0x1234567890abcdef1234567890abcdef12345678',
  version: 'v1.0.0',
  deployTime: new Date().toISOString(),
  deployBlock: 100
})

// 节点列表
const nodes = ref([
  {
    id: 1,
    name: 'Node0',
    endpoint: '127.0.0.1:20200',
    status: 'online',
    blockNumber: 12345,
    latency: 15
  },
  {
    id: 2,
    name: 'Node1',
    endpoint: '127.0.0.1:20201',
    status: 'online',
    blockNumber: 12345,
    latency: 18
  },
  {
    id: 3,
    name: 'Node2',
    endpoint: '127.0.0.1:20202',
    status: 'online',
    blockNumber: 12344,
    latency: 25
  },
  {
    id: 4,
    name: 'Node3',
    endpoint: '127.0.0.1:20203',
    status: 'offline',
    blockNumber: 12340,
    latency: 0
  }
])

// FISCO配置
const fiscoConfig = reactive({
  groupId: '1',
  channelPort: '20200',
  rpcPort: '8545',
  sdkVersion: '2.9.2'
})

// 加载状态
const loading = ref(false)
const syncing = ref(false)

// 同步结果
const syncResult = ref<{ success: boolean; message: string } | null>(null)

/**
 * 加载合约信息
 */
const loadContractInfo = async () => {
  loading.value = true
  try {
    // TODO: 调用后端API获取合约信息
    // const response = await adminApi.getBlockchainInfo()
    
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success('刷新成功')
  } catch (error) {
    ElMessage.error('加载合约信息失败')
  } finally {
    loading.value = false
  }
}

/**
 * 复制合约地址
 */
const copyAddress = async () => {
  try {
    await navigator.clipboard.writeText(contractInfo.address)
    ElMessage.success('已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

/**
 * 获取延迟样式类
 */
const getLatencyClass = (latency: number) => {
  if (latency === 0) return 'text-danger'
  if (latency < 20) return 'text-success'
  if (latency < 50) return 'text-warning'
  return 'text-danger'
}

/**
 * 一键同步数据
 */
const handleSync = async () => {
  syncing.value = true
  syncResult.value = null
  try {
    // TODO: 调用后端API同步数据
    // await adminApi.syncBlockchainData()
    
    await new Promise(resolve => setTimeout(resolve, 2000))
    syncResult.value = {
      success: true,
      message: '成功同步 156 条证书记录，区块高度已更新至 12345'
    }
    ElMessage.success('数据同步成功')
  } catch (error) {
    syncResult.value = {
      success: false,
      message: '同步失败，请检查区块链网络连接'
    }
    ElMessage.error('数据同步失败')
  } finally {
    syncing.value = false
  }
}

/**
 * 导出配置
 */
const handleExportConfig = () => {
  const config = {
    network: 'FISCO BCOS 2.x',
    groupId: fiscoConfig.groupId,
    contractAddress: contractInfo.address,
    nodes: nodes.value.map(n => ({
      name: n.name,
      endpoint: n.endpoint
    }))
  }
  
  const blob = new Blob([JSON.stringify(config, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'blockchain-config.json'
  a.click()
  URL.revokeObjectURL(url)
  
  ElMessage.success('配置已导出')
}

/**
 * 查看区块链日志
 */
const handleViewLogs = () => {
  ElMessage.info('功能开发中...')
}

onMounted(() => {
  loadContractInfo()
})
</script>

<style scoped lang="scss">
.blockchain-config {
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

  .status-cards {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
    margin-bottom: 16px;

    .status-card {
      .status-content {
        display: flex;
        align-items: center;
        gap: 16px;

        .status-icon {
          width: 64px;
          height: 64px;
          border-radius: 12px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #fff;

          &.success {
            background: linear-gradient(135deg, #67C23A, #85ce61);
          }

          &.danger {
            background: linear-gradient(135deg, #F56C6C, #f78989);
          }

          &.primary {
            background: linear-gradient(135deg, #409EFF, #66b1ff);
          }

          &.warning {
            background: linear-gradient(135deg, #E6A23C, #ebb563);
          }

          &.info {
            background: linear-gradient(135deg, #909399, #a6a9ad);
          }
        }

        .status-info {
          h3 {
            margin: 0 0 4px 0;
            font-size: 14px;
            color: var(--el-text-color-secondary);
          }

          p {
            margin: 0;
            font-size: 24px;
            font-weight: 600;
            color: var(--el-text-color-primary);
          }
        }
      }
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .address-wrapper {
    display: flex;
    align-items: center;
    gap: 8px;

    .address-text {
      font-family: 'Courier New', monospace;
      max-width: 400px;
    }
  }

  .endpoint-text {
    font-family: 'Courier New', monospace;
  }

  .node-card,
  .config-card,
  .action-card {
    margin-top: 16px;
  }

  .action-buttons {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
  }

  .sync-alert {
    margin-top: 16px;
  }

  .text-success {
    color: var(--el-color-success);
  }

  .text-warning {
    color: var(--el-color-warning);
  }

  .text-danger {
    color: var(--el-color-danger);
  }
}

@media (max-width: 1200px) {
  .blockchain-config {
    .status-cards {
      grid-template-columns: repeat(2, 1fr);
    }
  }
}

@media (max-width: 768px) {
  .blockchain-config {
    .status-cards {
      grid-template-columns: 1fr;
    }

    .action-buttons {
      flex-direction: column;

      .el-button {
        width: 100%;
      }
    }
  }
}
</style>