<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">通知公告</h2>
      <p class="page-desc">发布和管理学校通知公告信息，点击通知卡片查看详情</p>
    </div>

    <el-row :gutter="20">
      <el-col :span="24" :lg="8">
        <div class="card">
          <div class="card-title">
            <el-icon><Plus /></el-icon>
            发布新通知
          </div>
          <el-form :model="noticeForm" label-width="80px">
            <el-form-item label="标题">
              <el-input v-model="noticeForm.title" placeholder="请输入通知标题" maxlength="100" show-word-limit />
            </el-form-item>
            <el-form-item label="类型">
              <el-radio-group v-model="noticeForm.type">
                <el-radio value="general">普通</el-radio>
                <el-radio value="important">重要</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="内容">
              <el-input
                v-model="noticeForm.content"
                type="textarea"
                :rows="8"
                placeholder="请输入通知内容（支持换行）"
                maxlength="2000"
                show-word-limit
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="publishNotice">
                <el-icon><Promotion /></el-icon>
                发布通知
              </el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-col>

      <el-col :span="24" :lg="16">
        <div class="card">
          <div class="card-title">
            <el-icon><Document /></el-icon>
            已发布通知 ({{ notices.length }})
            <span class="card-hint">点击卡片查看详情</span>
          </div>

          <div class="notice-list">
            <div
              v-for="notice in notices"
              :key="notice.id"
              class="notice-card clickable"
              @click="openDetail(notice)"
            >
              <div class="notice-header">
                <div class="notice-title-row">
                  <el-tag :type="notice.type === 'important' ? 'danger' : 'info'" size="small">
                    {{ notice.type === 'important' ? '重要' : '普通' }}
                  </el-tag>
                  <span class="notice-title-text">{{ notice.title }}</span>
                </div>
                <div class="notice-actions" @click.stop>
                  <el-button size="small" type="primary" text @click="openDetail(notice)">
                    查看
                  </el-button>
                  <el-button size="small" type="danger" text @click="deleteNotice(notice)">
                    删除
                  </el-button>
                </div>
              </div>
              <div class="notice-content">{{ truncate(notice.content, 100) }}</div>
              <div class="notice-footer">
                <span><el-icon><User /></el-icon> {{ notice.author_name || '系统' }}</span>
                <span><el-icon><Clock /></el-icon> {{ formatDate(notice.created_at) }}</span>
                <span><el-icon><View /></el-icon> {{ notice.read_count || 0 }} 次阅读</span>
              </div>
            </div>
            <el-empty v-if="notices.length === 0" description="暂无通知" />
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 通知详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      :title="currentNotice?.title || '通知详情'"
      width="650px"
      top="10vh"
    >
      <template v-if="currentNotice">
        <div class="notice-detail-header">
          <el-tag :type="currentNotice.type === 'important' ? 'danger' : 'info'" size="default">
            {{ currentNotice.type === 'important' ? '重要通知' : '普通通知' }}
          </el-tag>
          <div class="detail-meta">
            <span><el-icon><User /></el-icon> 发布人：{{ currentNotice.author_name || '系统' }}</span>
            <span><el-icon><Clock /></el-icon> 发布时间：{{ formatDate(currentNotice.created_at) }}</span>
            <span><el-icon><View /></el-icon> 阅读：{{ currentNotice.read_count || 0 }} 次</span>
          </div>
        </div>
        <div class="notice-detail-content">
          {{ currentNotice.content }}
        </div>
      </template>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">我已阅知</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getNoticeList, getNoticeDetail, createNotice, deleteNotice as removeNotice } from '../api/notices'
import { useUserStore } from '../stores/user'
import {Document, Plus} from "@element-plus/icons-vue";

const userStore = useUserStore()
const notices = ref([])
const detailVisible = ref(false)
const currentNotice = ref(null)

const noticeForm = reactive({
  title: '',
  content: '',
  type: 'general'
})

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 截断文本
const truncate = (text, maxLen) => {
  if (!text) return ''
  return text.length > maxLen ? text.substring(0, maxLen) + '...' : text
}

// 加载通知列表
const loadData = async () => {
  try {
    const res = await getNoticeList()
    notices.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

// 重置表单
const resetForm = () => {
  noticeForm.title = ''
  noticeForm.content = ''
  noticeForm.type = 'general'
}

// 发布通知
const publishNotice = async () => {
  if (!noticeForm.title || !noticeForm.content) {
    ElMessage.warning('请填写标题和内容')
    return
  }
  try {
    await createNotice({
      title: noticeForm.title,
      content: noticeForm.content,
      type: noticeForm.type,
      target_audience: 'all',
      author_id: userStore.userInfo?.id,
      is_published: true
    })
    ElMessage.success('发布成功')
    resetForm()
    loadData()
  } catch (e) {
    console.error(e)
  }
}

// 打开通知详情
const openDetail = async (notice) => {
  try {
    const res = await getNoticeDetail(notice.id)
    currentNotice.value = res.data
    detailVisible.value = true
    // 更新本地列表中该条通知的阅读数
    const idx = notices.value.findIndex(n => n.id === notice.id)
    if (idx !== -1) {
      notices.value[idx].read_count = (notices.value[idx].read_count || 0) + 1
    }
  } catch (e) {
    console.error(e)
  }
}

// 删除通知
const deleteNotice = async (notice) => {
  try {
    await ElMessageBox.confirm(`确定删除通知 "${notice.title}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await removeNotice(notice.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel' && e?.message !== 'cancel') {
      console.error(e)
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  margin-bottom: 10px;
}

.page-title {
  margin: 0;
  font-size: 22px;
  color: #303133;
}

.page-desc {
  margin: 8px 0 0;
  color: #909399;
  font-size: 14px;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  height: 100%;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  border-left: 4px solid #409eff;
  padding-left: 12px;
}

.card-hint {
  font-size: 12px;
  font-weight: normal;
  color: #909399;
  margin-left: auto;
}

.notice-list {
  max-height: 600px;
  overflow-y: auto;
  padding-right: 10px;
}

.notice-card {
  background: #fafbfc;
  border-radius: 10px;
  padding: 18px;
  margin-bottom: 15px;
  border: 1px solid #e4e7ed;
  transition: all 0.2s;
}

.notice-card.clickable {
  cursor: pointer;
}

.notice-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  background: #fff;
  transform: translateY(-1px);
  border-color: #409eff;
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.notice-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.notice-title-text {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.notice-actions {
  display: flex;
  gap: 5px;
}

.notice-content {
  color: #606266;
  line-height: 1.7;
  font-size: 14px;
  padding: 10px 0;
  white-space: pre-wrap;
}

.notice-footer {
  display: flex;
  gap: 20px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  color: #909399;
  font-size: 13px;
}

.notice-footer span {
  display: flex;
  align-items: center;
  gap: 5px;
}

/* 详情对话框样式 */
.notice-detail-header {
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 15px;
  margin-bottom: 20px;
}

.detail-meta {
  margin-top: 12px;
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  color: #909399;
  font-size: 13px;
}

.detail-meta span {
  display: flex;
  align-items: center;
  gap: 5px;
}

.notice-detail-content {
  background: #fafbfc;
  border-radius: 8px;
  padding: 20px;
  font-size: 15px;
  line-height: 1.9;
  color: #303133;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 400px;
  overflow-y: auto;
}

/* 滚动条美化 */
.notice-list::-webkit-scrollbar,
.notice-detail-content::-webkit-scrollbar {
  width: 6px;
}

.notice-list::-webkit-scrollbar-thumb,
.notice-detail-content::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 3px;
}

.notice-list::-webkit-scrollbar-track,
.notice-detail-content::-webkit-scrollbar-track {
  background: #f5f7fa;
}
</style>
