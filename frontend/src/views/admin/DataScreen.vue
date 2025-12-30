<template>
  <div class="data-screen" :class="{ fullscreen: isFullscreen }">
    <!-- 顶部标题栏 -->
    <div class="screen-header">
      <h1 class="title">区块链证书存证系统 - 数据大屏</h1>
      <div class="header-actions">
        <span class="current-time">{{ currentTime }}</span>
        <el-button
          type="primary"
          :icon="isFullscreen ? Close : FullScreen"
          @click="toggleFullscreen"
        >
          {{ isFullscreen ? '退出全屏' : '全屏显示' }}
        </el-button>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="screen-content">
      <!-- 左侧面板 -->
      <div class="left-panel">
        <!-- 证书统计 -->
        <div class="panel-card">
          <div class="card-title">
            <el-icon><Medal /></el-icon>
            <span>证书统计</span>
          </div>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-value">{{ stats.totalCertificates }}</div>
              <div class="stat-label">证书总数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value success">{{ stats.validCertificates }}</div>
              <div class="stat-label">有效证书</div>
            </div>
            <div class="stat-item">
              <div class="stat-value danger">{{ stats.revokedCertificates }}</div>
              <div class="stat-label">已撤销</div>
            </div>
            <div class="stat-item">
              <div class="stat-value warning">{{ stats.expiredCertificates }}</div>
              <div class="stat-label">已过期</div>
            </div>
          </div>
        </div>

        <!-- 审批统计 -->
        <div class="panel-card">
          <div class="card-title">
            <el-icon><CircleCheck /></el-icon>
            <span>审批统计</span>
          </div>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-value">{{ stats.totalApplications }}</div>
              <div class="stat-label">申请总数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value primary">{{ stats.pendingApplications }}</div>
              <div class="stat-label">待审批</div>
            </div>
            <div class="stat-item">
              <div class="stat-value success">{{ stats.approvedApplications }}</div>
              <div class="stat-label">已通过</div>
            </div>
            <div class="stat-item">
              <div class="stat-value danger">{{ stats.rejectedApplications }}</div>
              <div class="stat-label">已驳回</div>
            </div>
          </div>
        </div>

        <!-- 证书类型分布 -->
        <div class="panel-card">
          <div class="card-title">
            <el-icon><PieChart /></el-icon>
            <span>证书类型分布</span>
          </div>
          <div class="chart-container" ref="typeChartRef"></div>
        </div>
      </div>

      <!-- 中间面板 -->
      <div class="center-panel">
        <!-- 核心数据展示 -->
        <div class="core-stats">
          <div class="core-stat-item">
            <div class="core-icon blockchain">
              <el-icon :size="48"><Connection /></el-icon>
            </div>
            <div class="core-info">
              <div class="core-value">{{ stats.blockHeight }}</div>
              <div class="core-label">区块高度</div>
            </div>
          </div>
          <div class="core-stat-item">
            <div class="core-icon transaction">
              <el-icon :size="48"><Document /></el-icon>
            </div>
            <div class="core-info">
              <div class="core-value">{{ stats.totalTransactions }}</div>
              <div class="core-label">链上交易</div>
            </div>
          </div>
          <div class="core-stat-item">
            <div class="core-icon user">
              <el-icon :size="48"><User /></el-icon>
            </div>
            <div class="core-info">
              <div class="core-value">{{ stats.totalUsers }}</div>
              <div class="core-label">系统用户</div>
            </div>
          </div>
          <div class="core-stat-item">
            <div class="core-icon college">
              <el-icon :size="48"><School /></el-icon>
            </div>
            <div class="core-info">
              <div class="core-value">{{ stats.totalColleges }}</div>
              <div class="core-label">学院数量</div>
            </div>
          </div>
        </div>

        <!-- 证书颁发趋势 -->
        <div class="panel-card trend-card">
          <div class="card-title">
            <el-icon><TrendCharts /></el-icon>
            <span>证书颁发趋势（近30天）</span>
          </div>
          <div class="chart-container" ref="trendChartRef"></div>
        </div>

        <!-- 最新动态 -->
        <div class="panel-card activity-card">
          <div class="card-title">
            <el-icon><Bell /></el-icon>
            <span>最新动态</span>
          </div>
          <div class="activity-list">
            <div
              v-for="(activity, index) in recentActivities"
              :key="index"
              class="activity-item"
            >
              <div class="activity-icon" :class="activity.type">
                <el-icon>
                  <Medal v-if="activity.type === 'certificate'" />
                  <CircleCheck v-else-if="activity.type === 'approval'" />
                  <Document v-else />
                </el-icon>
              </div>
              <div class="activity-content">
                <div class="activity-text">{{ activity.content }}</div>
                <div class="activity-time">{{ activity.time }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧面板 -->
      <div class="right-panel">
        <!-- 用户统计 -->
        <div class="panel-card">
          <div class="card-title">
            <el-icon><User /></el-icon>
            <span>用户统计</span>
          </div>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-value">{{ stats.totalUsers }}</div>
              <div class="stat-label">用户总数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value primary">{{ stats.studentCount }}</div>
              <div class="stat-label">学生</div>
            </div>
            <div class="stat-item">
              <div class="stat-value success">{{ stats.teacherCount }}</div>
              <div class="stat-label">教师</div>
            </div>
            <div class="stat-item">
              <div class="stat-value warning">{{ stats.adminCount }}</div>
              <div class="stat-label">管理员</div>
            </div>
          </div>
        </div>

        <!-- 区块链状态 -->
        <div class="panel-card">
          <div class="card-title">
            <el-icon><Connection /></el-icon>
            <span>区块链状态</span>
          </div>
          <div class="blockchain-status">
            <div class="status-row">
              <span class="status-label">网络状态</span>
              <span class="status-value">
                <el-tag type="success" size="small">正常</el-tag>
              </span>
            </div>
            <div class="status-row">
              <span class="status-label">节点数量</span>
              <span class="status-value">{{ stats.nodeCount }} 个</span>
            </div>
            <div class="status-row">
              <span class="status-label">共识算法</span>
              <span class="status-value">PBFT</span>
            </div>
            <div class="status-row">
              <span class="status-label">TPS</span>
              <span class="status-value">{{ stats.tps }} tx/s</span>
            </div>
          </div>
        </div>

        <!-- 学院证书排行 -->
        <div class="panel-card">
          <div class="card-title">
            <el-icon><Rank /></el-icon>
            <span>学院证书排行</span>
          </div>
          <div class="rank-list">
            <div
              v-for="(item, index) in collegeRanking"
              :key="index"
              class="rank-item"
            >
              <div class="rank-index" :class="'rank-' + (index + 1)">
                {{ index + 1 }}
              </div>
              <div class="rank-name">{{ item.name }}</div>
              <div class="rank-value">{{ item.count }}</div>
              <div class="rank-bar">
                <div
                  class="rank-bar-inner"
                  :style="{ width: (item.count / maxCollegeCount * 100) + '%' }"
                ></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import {
  FullScreen,
  Close,
  Medal,
  CircleCheck,
  PieChart,
  Connection,
  Document,
  User,
  School,
  TrendCharts,
  Bell,
  Rank
} from '@element-plus/icons-vue'

// 全屏状态
const isFullscreen = ref(false)

// 当前时间
const currentTime = ref('')

// 图表引用
const typeChartRef = ref<HTMLElement | null>(null)
const trendChartRef = ref<HTMLElement | null>(null)

// 统计数据
const stats = reactive({
  // 证书统计
  totalCertificates: 1256,
  validCertificates: 1180,
  revokedCertificates: 45,
  expiredCertificates: 31,
  // 审批统计
  totalApplications: 1580,
  pendingApplications: 23,
  approvedApplications: 1456,
  rejectedApplications: 101,
  // 用户统计
  totalUsers: 3256,
  studentCount: 3100,
  teacherCount: 150,
  adminCount: 6,
  // 区块链统计
  blockHeight: 125678,
  totalTransactions: 4523,
  nodeCount: 4,
  tps: 1200,
  // 其他
  totalColleges: 12
})

// 最新动态
const recentActivities = ref([
  { type: 'certificate', content: '张三获得"优秀毕业生"证书', time: '2分钟前' },
  { type: 'approval', content: '李四的证书申请已通过学院审批', time: '5分钟前' },
  { type: 'application', content: '王五提交了新的证书申请', time: '10分钟前' },
  { type: 'certificate', content: '赵六获得"奖学金"证书', time: '15分钟前' },
  { type: 'approval', content: '孙七的证书申请已通过学校审批', time: '20分钟前' },
  { type: 'application', content: '周八提交了新的证书申请', time: '25分钟前' }
])

// 学院排行
const collegeRanking = ref([
  { name: '计算机学院', count: 356 },
  { name: '软件学院', count: 298 },
  { name: '信息学院', count: 245 },
  { name: '电子学院', count: 189 },
  { name: '机械学院', count: 168 }
])

// 最大学院证书数
const maxCollegeCount = computed(() => {
  return Math.max(...collegeRanking.value.map(c => c.count))
})

// 定时器
let timeTimer: number | null = null

/**
 * 更新当前时间
 */
const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

/**
 * 切换全屏
 */
const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
    isFullscreen.value = true
  } else {
    document.exitFullscreen()
    isFullscreen.value = false
  }
}

/**
 * 监听全屏变化
 */
const handleFullscreenChange = () => {
  isFullscreen.value = !!document.fullscreenElement
}

/**
 * 初始化图表
 */
const initCharts = () => {
  // 这里可以使用 ECharts 或其他图表库
  // 由于没有安装 ECharts，这里使用简单的 CSS 模拟
  console.log('Charts initialized')
}

onMounted(() => {
  updateTime()
  timeTimer = window.setInterval(updateTime, 1000)
  document.addEventListener('fullscreenchange', handleFullscreenChange)
  initCharts()
})

onUnmounted(() => {
  if (timeTimer) {
    clearInterval(timeTimer)
  }
  document.removeEventListener('fullscreenchange', handleFullscreenChange)
})
</script>

<style scoped lang="scss">
.data-screen {
  min-height: 100vh;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  color: #fff;
  padding: 16px;

  &.fullscreen {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 9999;
  }

  .screen-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 24px;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 8px;
    margin-bottom: 16px;

    .title {
      margin: 0;
      font-size: 24px;
      font-weight: 600;
      background: linear-gradient(90deg, #00d4ff, #7b2cbf);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }

    .header-actions {
      display: flex;
      align-items: center;
      gap: 16px;

      .current-time {
        font-size: 16px;
        color: rgba(255, 255, 255, 0.8);
      }
    }
  }

  .screen-content {
    display: grid;
    grid-template-columns: 1fr 2fr 1fr;
    gap: 16px;
    height: calc(100vh - 120px);
  }

  .left-panel,
  .right-panel {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .center-panel {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .panel-card {
    background: rgba(255, 255, 255, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 8px;
    padding: 16px;
    flex: 1;

    .card-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 600;
      margin-bottom: 16px;
      color: #00d4ff;

      .el-icon {
        font-size: 20px;
      }
    }
  }

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;

    .stat-item {
      text-align: center;
      padding: 12px;
      background: rgba(255, 255, 255, 0.03);
      border-radius: 8px;

      .stat-value {
        font-size: 28px;
        font-weight: 700;
        color: #fff;

        &.success { color: #67C23A; }
        &.danger { color: #F56C6C; }
        &.warning { color: #E6A23C; }
        &.primary { color: #409EFF; }
      }

      .stat-label {
        font-size: 12px;
        color: rgba(255, 255, 255, 0.6);
        margin-top: 4px;
      }
    }
  }

  .core-stats {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;

    .core-stat-item {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 24px;
      background: rgba(255, 255, 255, 0.05);
      border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 8px;

      .core-icon {
        width: 80px;
        height: 80px;
        border-radius: 16px;
        display: flex;
        align-items: center;
        justify-content: center;

        &.blockchain {
          background: linear-gradient(135deg, #667eea, #764ba2);
        }

        &.transaction {
          background: linear-gradient(135deg, #f093fb, #f5576c);
        }

        &.user {
          background: linear-gradient(135deg, #4facfe, #00f2fe);
        }

        &.college {
          background: linear-gradient(135deg, #43e97b, #38f9d7);
        }
      }

      .core-info {
        .core-value {
          font-size: 36px;
          font-weight: 700;
        }

        .core-label {
          font-size: 14px;
          color: rgba(255, 255, 255, 0.6);
        }
      }
    }
  }

  .trend-card {
    flex: 2;

    .chart-container {
      height: 200px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: rgba(255, 255, 255, 0.4);
      font-size: 14px;

      &::after {
        content: '图表区域（需要安装 ECharts）';
      }
    }
  }

  .activity-card {
    flex: 1;

    .activity-list {
      max-height: 200px;
      overflow-y: auto;

      .activity-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 8px 0;
        border-bottom: 1px solid rgba(255, 255, 255, 0.05);

        &:last-child {
          border-bottom: none;
        }

        .activity-icon {
          width: 32px;
          height: 32px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 14px;

          &.certificate {
            background: rgba(103, 194, 58, 0.2);
            color: #67C23A;
          }

          &.approval {
            background: rgba(64, 158, 255, 0.2);
            color: #409EFF;
          }

          &.application {
            background: rgba(230, 162, 60, 0.2);
            color: #E6A23C;
          }
        }

        .activity-content {
          flex: 1;

          .activity-text {
            font-size: 13px;
            color: rgba(255, 255, 255, 0.9);
          }

          .activity-time {
            font-size: 11px;
            color: rgba(255, 255, 255, 0.4);
            margin-top: 2px;
          }
        }
      }
    }
  }

  .blockchain-status {
    .status-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid rgba(255, 255, 255, 0.05);

      &:last-child {
        border-bottom: none;
      }

      .status-label {
        font-size: 14px;
        color: rgba(255, 255, 255, 0.6);
      }

      .status-value {
        font-size: 14px;
        font-weight: 500;
      }
    }
  }

  .rank-list {
    .rank-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 10px 0;

      .rank-index {
        width: 24px;
        height: 24px;
        border-radius: 4px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 12px;
        font-weight: 600;
        background: rgba(255, 255, 255, 0.1);

        &.rank-1 {
          background: linear-gradient(135deg, #ffd700, #ffb700);
          color: #000;
        }

        &.rank-2 {
          background: linear-gradient(135deg, #c0c0c0, #a0a0a0);
          color: #000;
        }

        &.rank-3 {
          background: linear-gradient(135deg, #cd7f32, #b87333);
          color: #fff;
        }
      }

      .rank-name {
        flex: 1;
        font-size: 13px;
      }

      .rank-value {
        font-size: 14px;
        font-weight: 600;
        color: #00d4ff;
        min-width: 40px;
        text-align: right;
      }

      .rank-bar {
        width: 60px;
        height: 6px;
        background: rgba(255, 255, 255, 0.1);
        border-radius: 3px;
        overflow: hidden;

        .rank-bar-inner {
          height: 100%;
          background: linear-gradient(90deg, #00d4ff, #7b2cbf);
          border-radius: 3px;
          transition: width 0.3s;
        }
      }
    }
  }

  .chart-container {
    height: 150px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: rgba(255, 255, 255, 0.4);
    font-size: 14px;

    &::after {
      content: '图表区域';
    }
  }
}

@media (max-width: 1400px) {
  .data-screen {
    .screen-content {
      grid-template-columns: 1fr 1fr;
    }

    .right-panel {
      display: none;
    }
  }
}

@media (max-width: 768px) {
  .data-screen {
    .screen-content {
      grid-template-columns: 1fr;
    }

    .left-panel {
      display: none;
    }

    .core-stats {
      grid-template-columns: repeat(2, 1fr);
    }
  }
}
</style>