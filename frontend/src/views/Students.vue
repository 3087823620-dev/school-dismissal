<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">学生管理</h2>
      <p class="page-desc">管理学生信息和家长联系方式</p>
    </div>

    <div class="card">
      <div class="toolbar">
        <el-input v-model="searchKey" placeholder="搜索学生姓名/学号..." style="width: 280px" clearable>
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="classFilter" placeholder="按班级筛选" style="width: 180px" clearable>
          <el-option v-for="c in classes" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>新增学生
        </el-button>
      </div>

      <el-table :data="filteredStudents" style="width: 100%" stripe empty-text="暂无数据">
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="student_no" label="学号" width="130" />
        <el-table-column prop="class_name" label="班级" width="140" />
        <el-table-column prop="grade" label="年级" width="100" />
        <el-table-column prop="gender" label="性别" width="80" align="center">
          <template #default="scope">
            <el-icon v-if="scope.row.gender === '男'" color="#409eff"><Male /></el-icon>
            <el-icon v-else-if="scope.row.gender === '女'" color="#f56c6c"><Female /></el-icon>
            <span v-else style="color: #c0c4cc">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="parent_name" label="家长姓名" width="120" />
        <el-table-column prop="parent_phone" label="联系电话" width="140" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="editStudent(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteStudent(scope.row)">删除</el-button>
          </template>
        </el-table-column>
        <el-table-column label="人脸" width="80">
          <template #default="{ row }">
            <el-avatar :src="row.face_image_url" :size="40" v-if="row.face_image_url" />
            <el-upload
                :show-file-list="false"
                :http-request="(opt) => handleUploadFace(opt, row)"
                style="display:inline-block"
            >
              <el-button size="small" link>上传</el-button>
            </el-upload>
          </template>
        </el-table-column>

      </el-table>

      <div class="table-footer">
        <span>共 {{ filteredStudents.length }} 条记录</span>
      </div>
    </div>

    <!-- 编辑/新增对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogMode === 'add' ? '新增学生' : '编辑学生'" width="550px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="姓名">
          <el-input v-model="form.name" placeholder="请输入学生姓名" />
        </el-form-item>
        <el-form-item label="学号">
          <el-input v-model="form.student_no" placeholder="请输入学号" />
        </el-form-item>
        <el-form-item label="班级">
          <el-select v-model="form.class_id" style="width: 100%" placeholder="请选择班级">
            <el-option v-for="c in classes" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio value="男">男</el-radio>
            <el-radio value="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="家长姓名">
          <el-input v-model="form.parent_name" placeholder="请输入家长姓名" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.parent_phone" placeholder="请输入联系电话" />
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
import {
  getStudentList,
  createStudent,
  updateStudent,
  deleteStudent as removeStudent,
  uploadStudentFace
} from '../api/students'
import { getClassList } from '../api/classes'
import {Female, Male, Paperclip, Plus, Search} from "@element-plus/icons-vue";

const students = ref([])
const classes = ref([])
const searchKey = ref('')
const classFilter = ref('')

const dialogVisible = ref(false)
const dialogMode = ref('add')
const form = reactive({
  id: null,
  name: '',
  student_no: '',
  class_id: '',
  gender: '男',
  parent_name: '',
  parent_phone: ''
})

const filteredStudents = computed(() => {
  const key = searchKey.value.toLowerCase()
  return students.value.filter(s => {
    const matchKey = !key || 
      s.name.includes(searchKey.value) || 
      (s.student_no && s.student_no.includes(searchKey.value)) ||
      (s.parent_name && s.parent_name.includes(searchKey.value))
    const matchClass = !classFilter.value || s.class_id === classFilter.value
    return matchKey && matchClass
  })
})

const loadData = async () => {
  try {
    const [studentRes, classRes] = await Promise.all([
      getStudentList(),
      getClassList()
    ])
    students.value = studentRes.data || []
    classes.value = classRes.data || []
  } catch (e) {
    console.error(e)
  }
}

const openAddDialog = () => {
  dialogMode.value = 'add'
  form.id = null
  form.name = ''
  form.student_no = ''
  form.class_id = classes.value.length > 0 ? classes.value[0].id : ''
  form.gender = '男'
  form.parent_name = ''
  form.parent_phone = ''
  dialogVisible.value = true
}

const editStudent = (row) => {
  dialogMode.value = 'edit'
  form.id = row.id
  form.name = row.name
  form.student_no = row.student_no || ''
  form.class_id = row.class_id
  form.gender = row.gender || '男'
  form.parent_name = row.parent_name || ''
  form.parent_phone = row.parent_phone || ''
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.name || !form.class_id) {
    ElMessage.warning('请填写姓名并选择班级')
    return
  }
  try {
    const payload = {
      name: form.name,
      student_no: form.student_no,
      class_id: form.class_id,
      gender: form.gender,
      parent_name: form.parent_name,
      parent_phone: form.parent_phone
    }
    if (dialogMode.value === 'add') {
      await createStudent(payload)
      ElMessage.success('新增成功')
    } else {
      await updateStudent(form.id, { ...payload, status: 'active' })
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  }
}

const deleteStudent = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除学生 "${row.name}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await removeStudent(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel' && e?.message !== 'cancel') {
      console.error(e)
    }
  }
}

const handleUploadFace = async (option,row)=>{
  try {
    const res  = uploadStudentFace(row.id, option.file);
    row.face_image_url = res.data.url;
    ElMessage.success('人脸照片已上传')
  }catch (e){
    ElMessage.error('上传失败')
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

.table-footer {
  padding: 15px 10px 0;
  text-align: right;
  color: #909399;
  font-size: 13px;
}
</style>