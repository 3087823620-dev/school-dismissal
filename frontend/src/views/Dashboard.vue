<template>
  <div class="dashboard-pro">
    <!-- 顶部横幅 -->
    <div class="hero-banner">
      <div class="hero-left">
        <div class="hero-badge">
          <el-icon><Sunrise /></el-icon>
          <span>{{ periodLabel }}</span>
        </div>
        <h1 class="hero-title">{{ greeting }}，{{ userStore.userName || '老师' }}</h1>
        <p class="hero-subtitle">
          今天是 {{ currentDate }} · {{ currentWeekday }}
          <span class="hero-divider">|</span>
          共有 {{ dismissalStatus.stats?.totalClasses || 0 }} 个班级，{{ dismissalStatus.stats?.totalStudents || 0 }} 名学生在您的管理下
        </p>
      </div>
      <div class="hero-right">
        <div class="hero-action" @click="goToDismissal">
          <el-icon :size="22"><Bell /></el-icon>
          <div>
            <div class="hero-action-title">发布放学信息</div>
            <div class="hero-action-desc">一键通知家长</div>
          </div>
        </div>
      </div>
    </div>

    <!-- KPI 数据卡片 -->
    <el-row :gutter="16" class="kpi-row">
      <el-col :xs="12" :sm="6" v-for="(item, index) in kpiCards" :key="index">
        <div class="kpi-card" :class="item.color" @click="item.action && item.action()">
          <div class="kpi-icon">
            <el-icon :size="22"><component :is="item.icon" /></el-icon>
          </div>
          <div class="kpi-content">
            <div class="kpi-label">{{ item.label }}</div>
            <div class="kpi-value">
              <span class="kpi-number">{{ loading ? '--' : item.value }}</span>
              <span v-if="item.unit" class="kpi-unit">{{ item.unit }}</span>
            </div>
            <div v-if="item.trend !== undefined" class="kpi-trend" :class="item.trend > 0 ? 'up' : 'down'">
              <el-icon><component :is="item.trend > 0 ? 'CaretTop' : 'CaretBottom'" /></el-icon>
              {{ Math.abs(item.trend) }}%
            </div>
          </div>
          <div class="kpi-bg-icon">
            <el-icon :size="80"><component :is="item.icon" /></el-icon>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 主内容区域 -->
    <el-row :gutter="16" class="main-row">
      <!-- 左侧：今日放学状态 -->
      <el-col :span="16" class="main-col">
        <div class="panel">
          <div class="panel-header">
            <div class="panel-title-wrap">
              <div class="panel-title-icon">
                <el-icon><Calendar /></el-icon>
              </div>
              <div>
                <h3 class="panel-title">今日放学状态</h3>
                <p class="panel-subtitle">实时掌握各班放学进度</p>
              </div>
            </div>
            <div class="panel-actions">
              <el-tag size="default" :type="dismissalStats.dismissedRate >= 100 ? 'success' : 'warning'">
                已完成 {{ dismissalStats.dismissedRate }}%
              </el-tag>
              <el-button type="primary" plain size="default" @click="refreshData">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </div>

          <!-- 进度条 -->
          <div class="progress-bar-wrap">
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: dismissalStats.dismissedRate + '%' }"></div>
            </div>
            <div class="progress-labels">
              <span>已放学 {{ dismissalStats.dismissed }} 个班</span>
              <span>待放学 {{ dismissalStats.pending }} 个班</span>
            </div>
          </div>

          <!-- 班级网格 -->
          <div v-if="dismissalStatus.classes && dismissalStatus.classes.length > 0" class="class-grid">
            <div
              v-for="cls in dismissalStatus.classes"
              :key="cls.id"
              class="class-card-pro"
              :class="{ dismissed: cls.dismissed > 0 }"
            >
              <div class="class-status-bar"></div>
              <div class="class-body">
                <div class="class-header">
                  <div class="class-grade-tag">{{ cls.grade }}</div>
                  <el-tag v-if="cls.dismissed > 0" type="success" effect="dark" size="small">
                    <el-icon><Check /></el-icon>已放学
                  </el-tag>
                  <el-tag v-else type="warning" effect="dark" size="small">
                    <el-icon><Clock /></el-icon>待放学
                  </el-tag>
                </div>
                <div class="class-name">{{ cls.name }}</div>
                <div class="class-info-row">
                  <div class="class-info-item">
                    <el-icon><UserFilled /></el-icon>
                    {{ cls.head_teacher_name || '未分配' }}
                  </div>
                  <div class="class-info-item">
                    <el-icon><Watch /></el-icon>
                    {{ cls.dismissal_time || '未设置' }}
                  </div>
                  <div class="class-info-item">
                    <el-icon><LocationInformation /></el-icon>
                    {{ cls.school_gate || '正门' }}
                  </div>
                </div>
                <div class="class-footer">
                  <div class="student-count">
                    <el-icon><UserFilled /></el-icon>
                    {{ cls.student_count }} 名学生
                  </div>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无班级数据" :image-size="100" />
        </div>
      </el-col>

      <!-- 右侧：最新通知 + 快捷操作 -->
      <el-col :span="8" class="main-col">
        <!-- 最新通知 -->
        <div class="panel notice-panel">
          <div class="panel-header">
            <div class="panel-title-wrap">
              <div class="panel-title-icon icon-blue">
                <el-icon><BellFilled /></el-icon>
              </div>
              <div>
                <h3 class="panel-title">最新通知</h3>
                <p class="panel-subtitle">共 {{ notices.length }} 条公告</p>
              </div>
            </div>
            <el-button type="primary" link size="default" @click="$router.push('/notices')">
              查看全部 <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>

          <div v-if="notices.length > 0" class="notice-list-pro">
            <div
              v-for="notice in notices"
              :key="notice.id"
              class="notice-item-pro"
              @click="openNoticeDetail(notice)"
            >
              <div class="notice-type-dot" :class="notice.type === 'important' ? 'important' : 'general'"></div>
              <div class="notice-content-wrap">
                <div class="notice-meta">
                  <el-tag :type="notice.type === 'important' ? 'danger' : 'info'" size="small">
                    {{ notice.type === 'important' ? '重要' : '通知' }}
                  </el-tag>
                  <span class="notice-date">{{ formatDate(notice.created_at) }}</span>
                </div>
                <div class="notice-item-title">{{ notice.title }}</div>
                <div class="notice-item-preview">{{ truncate(notice.content, 50) }}</div>
              </div>
              <div class="notice-arrow">
                <el-icon><ArrowRight /></el-icon>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无通知" :image-size="60" />
        </div>

        <!-- 实时刷脸通过 -->
        <div class="panel notice-panel">
          <div class="panel-header">
            <div class="panel-title-wrap">
              <div class="panel-title-icon" style="background: rgba(82,196,26,0.1); color: #52c41a;">
                <el-icon><VideoCamera /></el-icon>
              </div>
              <div>
                <h3 class="panel-title">实时刷脸通过</h3>
                <p class="panel-subtitle">闸机识别实时推送</p>
              </div>
            </div>
            <el-tag size="small" type="success">{{ liveRecords.length }} 条</el-tag>
          </div>
          <div class="notice-list-pro" style="max-height: 280px;">
            <el-timeline v-if="liveRecords.length > 0">
              <el-timeline-item
                  v-for="(r, i) in liveRecords"
                  :key="i"
                  :timestamp="r.time"
              >
                <b>{{ r.studentName }}</b> 从 {{ r.gate }} 离校
              </el-timeline-item>
            </el-timeline>
            <el-empty v-else description="等待刷脸..." :image-size="60" />
          </div>
        </div>


        <!-- 快捷操作 -->
        <div class="panel quick-panel">
          <div class="panel-header">
            <div class="panel-title-wrap">
              <div class="panel-title-icon icon-green">
                <el-icon><Operation /></el-icon>
              </div>
              <div>
                <h3 class="panel-title">快捷操作</h3>
                <p class="panel-subtitle">常用功能一键直达</p>
              </div>
            </div>
          </div>

          <div class="quick-grid-pro">
            <div class="quick-item-pro" @click="$router.push('/dismissal')">
              <div class="quick-icon quick-blue">
                <el-icon :size="28"><School /></el-icon>
              </div>
              <div class="quick-label">放学发布</div>
            </div>
            <div class="quick-item-pro" @click="$router.push('/students')">
              <div class="quick-icon quick-green">
                <el-icon :size="28"><UserFilled /></el-icon>
              </div>
              <div class="quick-label">学生管理</div>
            </div>
            <div class="quick-item-pro" @click="$router.push('/classes')">
              <div class="quick-icon quick-orange">
                <el-icon :size="28"><Reading /></el-icon>
              </div>
              <div class="quick-label">班级设置</div>
            </div>
            <div class="quick-item-pro" @click="$router.push('/notices')">
              <div class="quick-icon quick-red">
                <el-icon :size="28"><Promotion /></el-icon>
              </div>
              <div class="quick-label">发布通知</div>
            </div>
          </div>
        </div>

        <!-- 温馨提示卡片 -->
        <div class="panel tips-panel">
          <div class="tips-content">
            <div class="tips-icon">
              <el-icon :size="20"><InfoFilled /></el-icon>
            </div>
            <div class="tips-text">
              <div class="tips-title">温馨提示</div>
              <div class="tips-desc">请在各班级放学时间前 10 分钟发布放学信息，确保家长及时接收到通知</div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 通知详情弹窗 -->
    <el-dialog
      v-model="noticeDetailVisible"
      :title="currentNotice?.title || '通知详情'"
      width="680px"
      top="8vh"
      custom-class="notice-detail-dialog"
    >
      <template v-if="currentNotice">
        <div class="notice-detail-header-pro">
          <div class="notice-detail-tags">
            <el-tag :type="getNoticeType(currentNotice.type)" size="default">
              {{ getNoticeTypeName(currentNotice.type) }}通知
            </el-tag>
          </div>
          <div class="detail-meta-row">
            <span class="detail-meta-item">
              <el-icon><UserFilled /></el-icon>
              发布人：{{ currentNotice.author_name || '系统' }}
            </span>
            <span class="detail-meta-item">
              <el-icon><Calendar /></el-icon>
              {{ formatDateFull(currentNotice.created_at) }}
            </span>
            <span class="detail-meta-item">
              <el-icon><View /></el-icon>
              {{ currentNotice.read_count || 0 }} 次阅读
            </span>
          </div>
        </div>
        <div class="notice-detail-content-pro">
          {{ currentNotice.content }}
        </div>
      </template>
      <template #footer>
        <el-button @click="noticeDetailVisible = false" size="default">关闭</el-button>
        <el-button type="primary" @click="noticeDetailVisible = false" size="default">我已阅知</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted,onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { getTodayStatus } from '../api/dismissal'
import { getNoticeList, getNoticeDetail } from '../api/notices'
import {
  ArrowRight,
  Bell, BellFilled,
  Calendar,
  Check,
  Clock, InfoFilled,
  LocationInformation, Operation, Promotion, Reading,
  Refresh, School,
  Sunrise,
  UserFilled, View,
  Watch
} from "@element-plus/icons-vue";



const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const notices = ref([])
const noticeDetailVisible = ref(false)
const currentNotice = ref(null)
const dismissalStatus = reactive({
  classes: [],
  stats: {}
})

const periodLabel = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨时分'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return '欢迎回来'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const currentDate = computed(() => {
  const now = new Date()
  return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日`
})

const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
const currentWeekday = computed(() => weekdays[new Date().getDay()])

// 统计数据
const dismissalStats = computed(() => {
  const total = dismissalStatus.classes?.length || 0
  const dismissed = dismissalStatus.classes?.filter(c => c.dismissed > 0).length || 0
  const pending = total - dismissed
  const rate = total > 0 ? Math.round((dismissed / total) * 100) : 0
  return { total, dismissed, pending, dismissedRate: rate }
})

// KPI 卡片配置
const kpiCards = computed(() => [
  {
    label: '班级总数',
    value: dismissalStatus.stats?.totalClasses || 0,
    unit: '个',
    icon: 'Reading',
    color: 'blue',
    trend: 5
  },
  {
    label: '学生总数',
    value: dismissalStatus.stats?.totalStudents || 0,
    unit: '人',
    icon: 'UserFilled',
    color: 'green',
    trend: 12
  },
  {
    label: '已放班级',
    value: dismissalStats.dismissed || 0,
    unit: '个',
    icon: 'Check',
    color: 'orange',
    trend: 8
  },
  {
    label: '通知公告',
    value: notices.value?.length || 0,
    unit: '条',
    icon: 'BellFilled',
    color: 'purple'
  }
])

// 短日期格式
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 完整日期格式
const formatDateFull = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const truncate = (text, maxLen) => {
  if (!text) return ''
  return text.length > maxLen ? text.substring(0, maxLen) + '...' : text
}

const getNoticeType = (type) => {
  const types = { important: 'danger', general: 'info' }
  return types[type] || 'info'
}

const getNoticeTypeName = (type) => {
  const names = { important: '重要', general: '普通' }
  return names[type] || '通知'
}

const goToDismissal = () => {
  router.push('/dismissal')
}

const openNoticeDetail = async (notice) => {
  try {
    const res = await getNoticeDetail(notice.id)
    currentNotice.value = res.data
    noticeDetailVisible.value = true
    const idx = notices.value.findIndex(n => n.id === notice.id)
    if (idx !== -1) {
      notices.value[idx].read_count = (notices.value[idx].read_count || 0) + 1
    }
  } catch (e) {
    console.error('获取通知详情失败:', e)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const [statusRes, noticesRes] = await Promise.all([
      getTodayStatus(),
      getNoticeList()
    ])
    dismissalStatus.classes = statusRes.data?.classes || []
    dismissalStatus.stats = statusRes.data?.stats || {}
    notices.value = (noticesRes.data || []).slice(0, 5)
  } catch (e) {
    console.error('加载数据失败:', e)
  } finally {
    loading.value = false
  }
}

const refreshData = () => {
  loadData()
}

const liveRecords = ref([])
let ws = null
let wsReconnectTimer = null

// 建立 WebSocket 连接，断线自动 3 秒重连
const connectWs = () => {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const wsHost = window.location.hostname + ':3000'
  ws = new WebSocket(`${protocol}//${wsHost}/ws/dismissal`)

  ws.onmessage = (e) => {
    const data = JSON.parse(e.data)
    if (data.type === 'face_pass') {
      liveRecords.value.unshift(data)
      if (liveRecords.value.length > 20) liveRecords.value.pop()
    }
  }

  // 后端重启/网络断开后自动重连
  ws.onclose = () => {
    console.warn('WebSocket 断开，3 秒后重连...')
    wsReconnectTimer = setTimeout(connectWs, 3000)
  }

  ws.onerror = () => ws.close()
}

onMounted(() => {
  // 页面加载时先拉一次数据
  loadData()
  // 建立实时推送连接
  connectWs()
})

onUnmounted(() => {
  if (wsReconnectTimer) clearTimeout(wsReconnectTimer)
  if (ws) ws.close()
})
</script>

<style scoped>
.dashboard-pro {
  padding: 24px 0;
  max-width: 1600px;
  margin: 0 auto;
}

/* ===== 顶部横幅 ===== */
.hero-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #5b7bea 100%);
  border-radius: 16px;
  padding: 32px 36px;
  color: #fff;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.25);
}

.hero-banner::before {
  content: '';
  position: absolute;
  right: -50px;
  top: -50px;
  width: 200px;
  height: 200px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 50%;
}

.hero-banner::after {
  content: '';
  position: absolute;
  right: 150px;
  bottom: -80px;
  width: 160px;
  height: 160px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 50%;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.2);
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  margin-bottom: 14px;
  backdrop-filter: blur(10px);
}

.hero-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 10px 0;
  letter-spacing: -0.5px;
}

.hero-subtitle {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
  line-height: 1.6;
}

.hero-divider {
  margin: 0 10px;
  opacity: 0.5;
}

.hero-right {
  z-index: 1;
}

.hero-action {
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.3);
  padding: 16px 24px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
  transition: all 0.25s;
  backdrop-filter: blur(10px);
}

.hero-action:hover {
  background: rgba(255, 255, 255, 0.28);
  transform: translateY(-2px);
}

.hero-action-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 3px;
}

.hero-action-desc {
  font-size: 12px;
  opacity: 0.85;
}

/* ===== KPI 卡片 ===== */
.kpi-row {
  margin-bottom: 20px;
}

.kpi-card {
  background: #fff;
  border-radius: 14px;
  padding: 22px 24px;
  position: relative;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.25s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid #f0f2f5;
  display: flex;
  align-items: flex-start;
}

.kpi-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.08);
}

.kpi-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  z-index: 1;
}

.kpi-card.blue .kpi-icon {
  background: rgba(64, 158, 255, 0.1);
  color: #1890ff;
}
.kpi-card.green .kpi-icon {
  background: rgba(82, 196, 26, 0.1);
  color: #52c41a;
}
.kpi-card.orange .kpi-icon {
  background: rgba(250, 173, 20, 0.1);
  color: #faad14;
}
.kpi-card.purple .kpi-icon {
  background: rgba(114, 46, 209, 0.1);
  color: #722ed1;
}

.kpi-content {
  flex: 1;
  margin-left: 14px;
  z-index: 1;
}

.kpi-label {
  font-size: 13px;
  color: #8c8c8c;
  margin-bottom: 8px;
}

.kpi-value {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.kpi-number {
  font-size: 28px;
  font-weight: 700;
  color: #262626;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.kpi-unit {
  font-size: 13px;
  color: #8c8c8c;
}

.kpi-trend {
  margin-top: 8px;
  font-size: 12px;
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

.kpi-trend.up { color: #52c41a; }
.kpi-trend.down { color: #f5222d; }

.kpi-bg-icon {
  position: absolute;
  right: -10px;
  bottom: -10px;
  opacity: 0.04;
  pointer-events: none;
}

.kpi-card.blue .kpi-bg-icon { color: #1890ff; }
.kpi-card.green .kpi-bg-icon { color: #52c41a; }
.kpi-card.orange .kpi-bg-icon { color: #faad14; }
.kpi-card.purple .kpi-bg-icon { color: #722ed1; }

/* ===== 主内容区 ===== */
.main-row {
  margin-bottom: 0;
}

.main-col {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.panel {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid #f0f2f5;
  overflow: hidden;
}

.panel-header {
  padding: 20px 24px;
  border-bottom: 1px solid #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.panel-title-wrap {
  display: flex;
  align-items: center;
  gap: 14px;
}

.panel-title-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: rgba(250, 173, 20, 0.1);
  color: #faad14;
  display: flex;
  align-items: center;
  justify-content: center;
}

.panel-title-icon.icon-blue {
  background: rgba(24, 144, 255, 0.1);
  color: #1890ff;
}

.panel-title-icon.icon-green {
  background: rgba(82, 196, 26, 0.1);
  color: #52c41a;
}

.panel-title {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
  margin: 0;
}

.panel-subtitle {
  font-size: 12px;
  color: #8c8c8c;
  margin: 4px 0 0 0;
}

.panel-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* ===== 进度条 ===== */
.progress-bar-wrap {
  padding: 20px 24px 0;
}

.progress-bar {
  height: 8px;
  background: #f0f2f5;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #52c41a 0%, #73d13d 100%);
  border-radius: 4px;
  transition: width 0.5s ease;
}

.progress-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  font-size: 12px;
  color: #8c8c8c;
}

/* ===== 班级网格 ===== */
.class-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 14px;
  padding: 20px 24px 24px;
}

.class-card-pro {
  background: #fafafa;
  border-radius: 12px;
  position: relative;
  overflow: hidden;
  transition: all 0.25s;
  border: 1px solid #f0f2f5;
}

.class-card-pro:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  border-color: #d9d9d9;
}

.class-status-bar {
  height: 3px;
  background: #faad14;
  transition: background 0.3s;
}

.class-card-pro.dismissed .class-status-bar {
  background: #52c41a;
}

.class-body {
  padding: 16px;
}

.class-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.class-grade-tag {
  font-size: 11px;
  color: #8c8c8c;
  background: #f5f5f5;
  padding: 2px 8px;
  border-radius: 4px;
}

.class-name {
  font-size: 15px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 10px;
}

.class-info-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 12px;
}

.class-info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #595959;
}

.class-footer {
  padding-top: 10px;
  border-top: 1px dashed #e8e8e8;
}

.student-count {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #1890ff;
  font-weight: 500;
}

/* ===== 通知面板 ===== */
.notice-panel {
  flex-shrink: 0;
}

.notice-list-pro {
  padding: 8px 12px 12px;
  max-height: 360px;
  overflow-y: auto;
}

.notice-item-pro {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 4px;
}

.notice-item-pro:hover {
  background: #f5f7fa;
}

.notice-type-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.notice-type-dot.important {
  background: #f5222d;
  box-shadow: 0 0 0 3px rgba(245, 34, 45, 0.15);
}

.notice-type-dot.general {
  background: #1890ff;
  box-shadow: 0 0 0 3px rgba(24, 144, 255, 0.15);
}

.notice-content-wrap {
  flex: 1;
  min-width: 0;
}

.notice-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.notice-date {
  font-size: 11px;
  color: #bfbfbf;
}

.notice-item-title {
  font-size: 14px;
  font-weight: 500;
  color: #262626;
  margin-bottom: 4px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.notice-item-preview {
  font-size: 12px;
  color: #8c8c8c;
  line-height: 1.5;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.notice-arrow {
  color: #bfbfbf;
  font-size: 14px;
  flex-shrink: 0;
  transition: transform 0.2s;
}

.notice-item-pro:hover .notice-arrow {
  color: #1890ff;
  transform: translateX(3px);
}

/* ===== 快捷操作 ===== */
.quick-panel {
  flex-shrink: 0;
}

.quick-grid-pro {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  padding: 16px 20px 20px;
}

.quick-item-pro {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 18px 10px;
  background: #fafafa;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.quick-item-pro:hover {
  background: #fff;
  border-color: #e6f4ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.1);
}

.quick-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.quick-blue { background: rgba(24, 144, 255, 0.1); color: #1890ff; }
.quick-green { background: rgba(82, 196, 26, 0.1); color: #52c41a; }
.quick-orange { background: rgba(250, 173, 20, 0.1); color: #faad14; }
.quick-red { background: rgba(245, 34, 45, 0.1); color: #f5222d; }

.quick-item-pro:hover .quick-blue { background: rgba(24, 144, 255, 0.18); }
.quick-item-pro:hover .quick-green { background: rgba(82, 196, 26, 0.18); }
.quick-item-pro:hover .quick-orange { background: rgba(250, 173, 20, 0.18); }
.quick-item-pro:hover .quick-red { background: rgba(245, 34, 45, 0.18); }

.quick-label {
  font-size: 13px;
  color: #595959;
  font-weight: 500;
}

/* ===== 温馨提示 ===== */
.tips-panel {
  background: linear-gradient(135deg, #fffbe6 0%, #fff1b8 100%);
  border: 1px solid #ffe58f;
  flex-shrink: 0;
}

.tips-content {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 18px 20px;
}

.tips-icon {
  width: 36px;
  height: 36px;
  border-radius: 9px;
  background: rgba(250, 173, 20, 0.15);
  color: #d48806;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.tips-title {
  font-size: 14px;
  font-weight: 600;
  color: #874d00;
  margin-bottom: 4px;
}

.tips-desc {
  font-size: 12px;
  color: #ad6800;
  line-height: 1.6;
}

/* ===== 通知详情弹窗 ===== */
.notice-detail-header-pro {
  border-bottom: 1px solid #f0f2f5;
  padding-bottom: 18px;
  margin-bottom: 20px;
}

.notice-detail-tags {
  margin-bottom: 14px;
}

.detail-meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.detail-meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #8c8c8c;
}

.notice-detail-content-pro {
  background: #fafafa;
  border-radius: 10px;
  padding: 24px;
  font-size: 15px;
  line-height: 2;
  color: #262626;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 400px;
  overflow-y: auto;
}

/* ===== 响应式 ===== */
@media (max-width: 1200px) {
  .class-grid {
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  }
}

@media (max-width: 768px) {
  .dashboard-pro {
    padding: 16px 0;
  }
  .hero-banner {
    padding: 24px;
    flex-direction: column;
    gap: 20px;
    align-items: flex-start;
  }
  .hero-title {
    font-size: 22px;
  }
}

/* 滚动条美化 */
.notice-list-pro::-webkit-scrollbar,
.notice-detail-content-pro::-webkit-scrollbar {
  width: 6px;
}

.notice-list-pro::-webkit-scrollbar-thumb,
.notice-detail-content-pro::-webkit-scrollbar-thumb {
  background: #d9d9d9;
  border-radius: 3px;
}

.notice-list-pro::-webkit-scrollbar-track,
.notice-detail-content-pro::-webkit-scrollbar-track {
  background: transparent;
}
</style>
