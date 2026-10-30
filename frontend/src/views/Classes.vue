<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">班级管理</h2>
      <p class="page-desc">管理学校各班级信息、放学时间和校门</p>
    </div>

    <div class="card">
      <div class="toolbar">
        <el-input v-model="searchKey" placeholder="搜索班级名称..." style="width: 250px" clearable>
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="gradeFilter" placeholder="按年级筛选" style="width: 150px" clearable>
          <el-option v-for="g in grades" :key="g" :label="g" :value="g" />
        </el-select>
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>新增班级
        </el-button>
      </div>

      <el-table :data="filteredClasses" style="width: 100%" stripe empty-text="暂无数据">
        <el-table-column prop="name" label="班级名称" min-width="150" />
        <el-table-column prop="grade" label="年级" width="120" />
        <el-table-column prop="head_teacher_name" label="班主任" width="120">
          <template #default="scope">
            <span v-if="scope.row.head_teacher_name">{{ scope.row.head_teacher_name }}</span>
            <el-tag v-else type="info" size="small">未分配</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dismissal_time" label="放学时间" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.dismissal_time" type="success" size="small">
              {{ scope.row.dismissal_time }}
            </el-tag>
            <span v-else style="color: #c0c4cc">未设置</span>
          </template>
        </el-table-column>
        <el-table-column prop="school_gate" label="校门" width="100" />
        <el-table-column prop="student_count" label="学生数" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'active' ? 'success' : 'info'" size="small">
              {{ scope.row.status === 'active' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="editClass(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteClass(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 编辑/新增对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogMode === 'add' ? '新增班级' : '编辑班级'" width="550px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="班级名称">
          <el-input v-model="form.name" placeholder="如：一年级(1)班" />
        </el-form-item>
        <el-form-item label="年级">
          <el-select v-model="form.grade" style="width: 100%" placeholder="请选择年级">
            <el-option v-for="g in gradeOptions" :key="g" :label="g" :value="g" />
          </el-select>
        </el-form-item>
        <el-form-item label="班主任">
          <el-select v-model="form.head_teacher_id" style="width: 100%" placeholder="请选择班主任" clearable filterable>
            <el-option
              v-for="t in teacherList"
              :key="t.id"
              :label="t.name + ' (' + t.username + ')'"
              :value="t.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="放学时间">
          <el-time-picker v-model="form.dismissal_time" format="HH:mm" value-format="HH:mm" style="width: 100%" />
        </el-form-item>
        <el-form-item label="校门">
          <el-select v-model="form.school_gate" style="width: 100%">
            <el-option label="正门" value="正门" />
            <el-option label="东门" value="东门" />
            <el-option label="西门" value="西门" />
            <el-option label="南门" value="南门" />
            <el-option label="北门" value="北门" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="active">启用</el-radio>
            <el-radio value="inactive">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getClassList, createClass, updateClass, deleteClass as removeClass, getGradeList } from '../api/classes'
import { getUserList } from '../api/users'
import {Plus} from "@element-plus/icons-vue";

const classes = ref([])
const grades = ref([])
const teacherList = ref([])
const searchKey = ref('')
const gradeFilter = ref('')

const gradeOptions = [
  '一年级', '二年级', '三年级', '四年级', '五年级', '六年级',
  '七年级', '八年级', '九年级'
]

const dialogVisible = ref(false)
const dialogMode = ref('add')
const form = reactive({
  id: null,
  name: '',
  grade: '',
  head_teacher_id: null,
  dismissal_time: '',
  school_gate: '正门',
  status: 'active'
})

const filteredClasses = computed(() => {
  return classes.value.filter(c => {
    const matchKey = !searchKey.value || c.name.includes(searchKey.value)
    const matchGrade = !gradeFilter.value || c.grade === gradeFilter.value
    return matchKey && matchGrade
  })
})

const loadData = async () => {
  try {
    const [classRes, gradeRes, userRes] = await Promise.all([
      getClassList(),
      getGradeList(),
      getUserList()
    ])
    classes.value = classRes.data || []
    grades.value = gradeRes.data || []
    teacherList.value = (userRes.data || []).filter(u => u.role === 'teacher' || u.role === 'admin')
  } catch (e) {
    console.error(e)
  }
}

const openAddDialog = () => {
  dialogMode.value = 'add'
  form.id = null
  form.name = ''
  form.grade = ''
  form.head_teacher_id = null
  form.dismissal_time = ''
  form.school_gate = '正门'
  form.status = 'active'
  dialogVisible.value = true
}

const editClass = (row) => {
  dialogMode.value = 'edit'
  form.id = row.id
  form.name = row.name
  form.grade = row.grade
  form.head_teacher_id = row.head_teacher_id || null
  form.dismissal_time = row.dismissal_time || ''
  form.school_gate = row.school_gate || '正门'
  form.status = row.status || 'active'
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.name || !form.grade) {
    ElMessage.warning('请填写班级名称和年级')
    return
  }
  try {
    const payload = {
      name: form.name,
      grade: form.grade,
      head_teacher_id: form.head_teacher_id || null,
      dismissal_time: form.dismissal_time,
      school_gate: form.school_gate
    }

    if (dialogMode.value === 'add') {
      await createClass(payload)
      ElMessage.success('新增成功')
    } else {
      await updateClass(form.id, {
        ...payload,
        status: form.status
      })
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  }
}

const deleteClass = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除班级 "${row.name}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await removeClass(row.id)
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
}

.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.toolbar > :last-child {
  margin-left: auto;
}

.card{
  width: 100%;
}







.toolbar > * {
  flex:1;
  min-width: 90px;
  max-lines: 1;
}

.table-footer{
  display: grid;
  grid-area: initial;
  grid-template-areas: revert;
  grid: 880px /1fr;
  column-fill: balance;
  cursor: pointer;
  border-bottom: pink;
  border-bottom: darkgray;
  border-bottom: deepskyblue;
  border-image-outset: clamp( 1px, 0.5em, 0.25em);
}

circle{
  fill: red;
  block-ellipsis: revert;
  stroke: navajowhite;
  unicode-bidi: revert;
  text-align: justify;
  animation: ease-in-out cubic-bezier(0.1, 0.7, 0.1, 1) 2s;
  animation-timing-function: ease-in-out;
  line-grid: unset;
  animation-delay: calc( 0.1 * var());
}
@keyframes dash {
  from{
    stroke: lightgreen;
    line-grid: match-parent;
    stop-color: #409eff;
    opacity: revert;
    stroke-dasharray: clamp(1px, 0.5em, 0.25em);
    caption-side: revert;
  }
}




</style>