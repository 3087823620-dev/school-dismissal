<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">放学管理</h2>
      <p class="page-desc">发布放学信息、记录学生留堂情况</p>
    </div>

    <!-- 放学发布区域 -->
    <el-row :gutter="20">
      <el-col :span="24" :lg="14">
        <div class="card">
          <div class="card-title">今日放学发布</div>
          <el-form :inline="true" :model="publishForm" class="publish-form">
            <el-form-item label="校门">
              <el-select v-model="publishForm.gate" style="width: 120px">
                <el-option label="正门" value="正门" />
                <el-option label="东门" value="东门" />
                <el-option label="西门" value="西门" />
                <el-option label="南门" value="南门" />
                <el-option label="北门" value="北门" />
              </el-select>
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="publishForm.remark" placeholder="可选" style="width: 180px" />
            </el-form-item>
          </el-form>

          <div class="class-pick-list">
            <div class="list-header">
              <el-checkbox v-model="checkAll" @change="handleCheckAllChange">全选</el-checkbox>
              <span class="count-text">已选 {{ selectedIds.length }} / {{ classes.length }} 个班级</span>
            </div>
            <el-scrollbar height="320px">
              <div class="class-list">
                <div 
                  v-for="cls in classes" 
                  :key="cls.id" 
                  class="class-item"
                  :class="{ dismissed: isDismissed(cls.id) }"
                >
                  <el-checkbox 
                    v-model="selectedIds" 
                    :label="cls.id" 
                    @change="handleCheckedClassesChange"
                  />
                  <div class="class-info">
                    <span class="class-name">{{ cls.name }}</span>
                    <span class="class-grade">{{ cls.grade }}</span>
                    <span class="class-time">{{ cls.dismissal_time || '未设置' }}</span>
                    <span class="class-count">{{ cls.student_count }}人</span>
                  </div>
                  <el-tag v-if="isDismissed(cls.id)" type="success" size="small">已发布</el-tag>
                </div>
                <el-empty v-if="classes.length === 0" description="暂无班级数据" />
              </div>
            </el-scrollbar>
          </div>

          <div class="publish-actions">
            <el-button type="primary" size="large" :disabled="selectedIds.length === 0" @click="handleBatchPublish">
              <el-icon><Promotion /></el-icon>
              批量发布放学 ({{ selectedIds.length }})
            </el-button>
          </div>
        </div>
      </el-col>

      <el-col :span="24" :lg="10">
        <div class="card">
          <div class="card-title">今日放学记录</div>
          <el-scrollbar height="300px">
            <div class="record-list">
              <div v-for="record in records" :key="record.id" class="record-item">
                <div class="record-header">
                  <span class="record-class">{{ record.class_name }}</span>
                  <span class="record-gate">{{ record.gate }}</span>
                </div>
                <div class="record-time">
                  <el-icon><Clock /></el-icon>
                  {{ formatTime(record.created_at) }}
                </div>
              </div>
              <el-empty v-if="records.length === 0" description="暂无放学记录" :image-size="60" />
            </div>
          </el-scrollbar>
        </div>

        <div class="card mt-20">
          <div class="card-title">
            <el-icon><Warning /></el-icon>
            留堂记录
          </div>
          <el-button type="warning" size="small" @click="detentionDialogVisible = true" style="margin-bottom: 15px">
            <el-icon><Plus /></el-icon>
            添加留堂记录
          </el-button>
          <el-scrollbar height="200px">
            <div class="detention-list">
              <div v-for="det in detentions" :key="det.id" class="detention-item">
                <div class="detention-student">{{ det.student_name }}</div>
                <div class="detention-reason">{{ det.reason }}</div>
                <div class="detention-date">{{ det.detention_date }}</div>
              </div>
              <el-empty v-if="detentions.length === 0" description="暂无留堂记录" :image-size="60" />
            </div>
          </el-scrollbar>
        </div>
      </el-col>
    </el-row>

    <!-- 留堂对话框 -->
    <el-dialog v-model="detentionDialogVisible" title="添加留堂记录" width="500px">
      <el-form :model="detentionForm" label-width="80px">
        <el-form-item label="学生">
          <el-input v-model="detentionForm.student_id" placeholder="请输入学生ID或姓名" />
        </el-form-item>
        <el-form-item label="原因">
          <el-input 
            v-model="detentionForm.reason" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入留堂原因"
          />
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="detentionForm.detention_date" type="date" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="detentionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitDetention">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getClassList } from '../api/classes'
import { getDismissalRecords, batchPublishDismissal, addDetention, getDetentions } from '../api/dismissal'
import { useUserStore } from '../stores/user'
import {Clock, Plus, Promotion, Warning} from "@element-plus/icons-vue";

const userStore = useUserStore()

const classes = ref([])
const records = ref([])
const detentions = ref([])
const checkAll = ref(false)
const isIndeterminate = ref(false)
const selectedIds = ref([])

const detentionDialogVisible = ref(false)
const detentionForm = reactive({
  student_id: '',
  reason: '',
  detention_date: new Date().toISOString().split('T')[0]
})

const publishForm = reactive({
  gate: '正门',
  remark: ''
})

const isDismissed = (classId) => {
  return records.value.some(r => r.class_id === classId)
}

const handleCheckAllChange = (val) => {
  selectedIds.value = val ? classes.value.map(c => c.id) : []
  isIndeterminate.value = false
}

const handleCheckedClassesChange = (value) => {
  const checkedCount = value.length
  checkAll.value = checkedCount === classes.value.length
  isIndeterminate.value = checkedCount > 0 && checkedCount < classes.value.length
}

const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`
}

const loadClasses = async () => {
  try {
    const res = await getClassList()
    classes.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

const loadRecords = async () => {
  try {
    const today = new Date().toISOString().split('T')[0]
    const res = await getDismissalRecords({ date: today })
    records.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

const loadDetentions = async () => {
  try {
    const res = await getDetentions()
    detentions.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

const handleBatchPublish = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择要发布的班级')
    return
  }
  try {
    await batchPublishDismissal({
      class_ids: selectedIds.value,
      gate: publishForm.gate,
      remark: publishForm.remark,
      operator_id: userStore.userInfo?.id
    })
    ElMessage.success(`成功发布 ${selectedIds.value.length} 个班级的放学信息`)
    selectedIds.value = []
    checkAll.value = false
    loadRecords()
  } catch (e) {
    console.error(e)
  }
}

const submitDetention = async () => {
  if (!detentionForm.student_id || !detentionForm.reason) {
    ElMessage.warning('请填写完整信息')
    return
  }
  try {
    await addDetention({
      student_id: detentionForm.student_id,
      reason: detentionForm.reason,
      detention_date: detentionForm.detention_date,
      operator_id: userStore.userInfo?.id
    })
    ElMessage.success('留堂记录已添加')
    detentionDialogVisible.value = false
    detentionForm.student_id = ''
    detentionForm.reason = ''
    loadDetentions()
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadClasses()
  loadRecords()
  loadDetentions()
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

.mt-20 {
  margin-top: 20px;
}

.publish-form {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 15px;
}

.count-text {
  font-size: 13px;
  color: #606266;
}

.class-list {
  padding: 0 10px;
}

.class-item {
  display: flex;
  align-items: center;
  padding: 12px 15px;
  margin-bottom: 8px;
  background: #fafafa;
  border-radius: 8px;
  transition: all 0.2s;
  gap: 12px;
}

.class-item:hover {
  background: #f0f9ff;
}

.class-item.dismissed {
  background: #f0f9eb;
  opacity: 0.8;
}

.class-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 14px;
}

.class-name {
  font-weight: 600;
  color: #303133;
}

.class-grade {
  color: #909399;
}

.class-time {
  color: #409eff;
  font-size: 13px;
}

.class-count {
  color: #67c23a;
  font-size: 13px;
}

.publish-actions {
  margin-top: 20px;
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.record-list {
  padding: 5px 10px;
}

.record-item {
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.record-item:last-child {
  border-bottom: none;
}

.record-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}

.record-class {
  font-weight: 600;
  color: #303133;
  font-size: 14px;
}

.record-gate {
  color: #409eff;
  font-size: 12px;
}

.record-time {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #909399;
  font-size: 13px;
}

.detention-list {
  padding: 5px 10px;
}

.detention-item {
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.detention-item:last-child {
  border-bottom: none;
}

.detention-student {
  font-weight: 600;
  color: #e6a23c;
  font-size: 14px;
  margin-bottom: 4px;
}

.detention-reason {
  color: #606266;
  font-size: 13px;
  margin-bottom: 4px;
}

.detention-date {
  color: #909399;
  font-size: 12px;
}
</style>
