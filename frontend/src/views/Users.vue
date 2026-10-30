<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">用户管理</h2>
      <p class="page-desc">管理系统用户账号和权限</p>
    </div>

    <div class="card">
      <div class="toolbar">
        <el-input v-model="searchKey" placeholder="搜索用户名/姓名..." style="width: 280px" clearable>
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>新增用户
        </el-button>
      </div>

      <el-table :data="filteredUsers" style="width: 100%" stripe empty-text="暂无数据">
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="name" label="姓名" width="150" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.role === 'admin' ? 'danger' : 'success'" size="small">
              {{ scope.row.role === 'admin' ? '管理员' : '教师' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" width="140" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="created_at" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.created_at) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="editUser(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteUser(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogMode === 'add' ? '新增用户' : '编辑用户'" width="550px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" :disabled="dialogMode === 'edit'" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item v-if="dialogMode === 'add'" label="密码">
          <el-input v-model="form.password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="角色">
          <el-radio-group v-model="form.role">
            <el-radio value="teacher">教师</el-radio>
            <el-radio value="admin">管理员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
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
import { getUserList, createUser, updateUser, deleteUser as removeUser } from '../api/users'

const users = ref([])
const searchKey = ref('')

const dialogVisible = ref(false)
const dialogMode = ref('add')
const form = reactive({
  id: null,
  username: '',
  password: '',
  name: '',
  role: 'teacher',
  phone: '',
  email: ''
})

const filteredUsers = computed(() => {
  const key = searchKey.value
  return users.value.filter(u => 
    !key || u.username.includes(key) || u.name.includes(key)
  )
})

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const loadData = async () => {
  try {
    const res = await getUserList()
    users.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

const openAddDialog = () => {
  dialogMode.value = 'add'
  form.id = null
  form.username = ''
  form.password = ''
  form.name = ''
  form.role = 'teacher'
  form.phone = ''
  form.email = ''
  dialogVisible.value = true
}

const editUser = (row) => {
  dialogMode.value = 'edit'
  form.id = row.id
  form.username = row.username
  form.name = row.name
  form.role = row.role
  form.phone = row.phone || ''
  form.email = row.email || ''
  dialogVisible.value = true
}


const submitForm = async () => {
  if (dialogMode.value === 'add') {
    if (!form.username || !form.password || !form.name) {
      ElMessage.warning('请填写用户名、密码和姓名')
      return
    }
  } else {
    if (!form.name) {
      ElMessage.warning('请填写姓名')
      return
    }
  }
  try {
    const payload = {
      username: form.username,
      password: form.password,
      name: form.name,
      role: form.role,
      phone: form.phone,
      email: form.email
    }
    if (dialogMode.value === 'add') {
      await createUser(payload)
      ElMessage.success('新增成功')
    } else {
      await updateUser(form.id, payload)
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  }
}

const deleteUser = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除用户 "${row.name}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await removeUser(row.id)
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

</style>
