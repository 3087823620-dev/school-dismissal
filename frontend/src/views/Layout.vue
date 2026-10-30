<template>
  <el-container class="layout-container">
    <!-- 移动端遮罩 -->
    <div v-if="isMobile && mobileMenuOpen" class="sidebar-mask" @click="mobileMenuOpen = false"></div>

    <el-aside :width="isMobile ? '220px' : (isCollapse ? '64px' : '220px')" class="sidebar" :class="{ 'sidebar-mobile-open': isMobile && mobileMenuOpen }">
      <div class="logo-area">
        <el-icon :size="24" color="#fff" class="logo-icon"><School /></el-icon>
        <span v-if="!isCollapse || isMobile" class="logo-text">放学系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isMobile ? false : isCollapse"
        :collapse-transition="false"
        background-color="#1f2937"
        text-color="#cbd5e1"
        active-text-color="#409eff"
        router
        class="sidebar-menu"
        @select="isMobile && (mobileMenuOpen = false)"
      >
        <el-menu-item index="/dashboard">
          <el-icon><House /></el-icon>
          <template #title>首页</template>
        </el-menu-item>
        <el-menu-item index="/dismissal">
          <el-icon><Bell /></el-icon>
          <template #title>放学管理</template>
        </el-menu-item>
        <el-menu-item index="/classes">
          <el-icon><Reading /></el-icon>
          <template #title>班级管理</template>
        </el-menu-item>
        <el-menu-item index="/students">
          <el-icon><User /></el-icon>
          <template #title>学生管理</template>
        </el-menu-item>
        <el-menu-item index="/notices">
          <el-icon><Document /></el-icon>
          <template #title>通知公告</template>
        </el-menu-item>
        <el-menu-item v-if="userStore.isAdmin" index="/users">
          <el-icon><Avatar /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" :size="20" @click="onToggleSidebar">
            <Fold v-if="!isMobile && !isCollapse" />
            <Expand v-else-if="!isMobile" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute !== '/dashboard'">
              {{ currentTitle }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar || null" style="background: #409eff">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <span v-if="!isMobile" class="username">{{ userStore.userName || '用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人信息
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>

    <!-- 个人信息弹窗 -->
    <el-dialog v-model="profileVisible" title="个人信息" :width="isMobile ? '92%' : '480px'">
      <div class="profile-body">
        <div class="profile-avatar-wrap">
          <el-avatar :size="80" :src="profileForm.avatar || null" style="background: #409eff">
            <el-icon :size="40"><UserFilled /></el-icon>
          </el-avatar>
          <el-upload
            :show-file-list="false"
            :http-request="handleAvatarUpload"
            accept="image/jpeg,image/png,image/webp"
          >
            <el-button size="small" type="primary" plain style="margin-top: 10px">更换头像</el-button>
          </el-upload>
        </div>
        <el-form :model="profileForm" label-width="70px" style="margin-top: 20px; width: 100%">
          <el-form-item label="用户名">
            <el-input v-model="profileForm.username" disabled />
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model="profileForm.name" placeholder="请输入姓名" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="角色">
            <el-tag :type="userStore.userRole === 'admin' ? 'danger' : 'info'">
              {{ userStore.userRole === 'admin' ? '管理员' : '教师' }}
            </el-tag>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="profileVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveProfile">保存</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {School, UserFilled} from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { updateProfile, uploadAvatar } from '../api/auth'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)
const activeMenu = computed(() => route.path)

const isMobile = ref(window.innerWidth < 768)
const mobileMenuOpen = ref(false)

const onResize = () => {
  isMobile.value = window.innerWidth < 768
  if (!isMobile.value) mobileMenuOpen.value = false
}

onMounted(() => window.addEventListener('resize', onResize))
onUnmounted(() => window.removeEventListener('resize', onResize))

const onToggleSidebar = () => {
  if (isMobile.value) {
    mobileMenuOpen.value = !mobileMenuOpen.value
  } else {
    isCollapse.value = !isCollapse.value
  }
}

const profileVisible = ref(false)
const saving = ref(false)
const profileForm = reactive({
  username: '',
  name: '',
  phone: '',
  email: '',
  avatar: ''
})

const currentTitle = computed(() => {
  const path = route.path
  if (path.includes('dismissal')) return '放学管理'
  if (path.includes('classes')) return '班级管理'
  if (path.includes('students')) return '学生管理'
  if (path.includes('notices')) return '通知公告'
  if (path.includes('users')) return '用户管理'
  return '首页'
})

const currentRoute = computed(() => route.path)

const openProfile = () => {
  const info = userStore.userInfo || {}
  profileForm.username = info.username || ''
  profileForm.name = info.name || ''
  profileForm.phone = info.phone || ''
  profileForm.email = info.email || ''
  profileForm.avatar = info.avatar || ''
  profileVisible.value = true
}

const saveProfile = async () => {
  saving.value = true
  try {
    const res = await updateProfile({
      name: profileForm.name,
      phone: profileForm.phone,
      email: profileForm.email
    })
    userStore.userInfo = res.data
    localStorage.setItem('userInfo', JSON.stringify(res.data))
    ElMessage.success('保存成功')
    profileVisible.value = false
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleAvatarUpload = async (opt) => {
  const fd = new FormData()
  fd.append('file', opt.file)
  try {
    const res = await uploadAvatar(fd)
    profileForm.avatar = res.data.url
    userStore.userInfo = { ...userStore.userInfo, avatar: res.data.url }
    localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))
    ElMessage.success('头像已更新')
  } catch (e) {
    ElMessage.error('上传失败')
  }
}

const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
    } catch (e) {
      // 用户取消
    }
  } else if (command === 'profile') {
    openProfile()
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background: #1f2937;
  transition: width 0.3s, transform 0.3s;
  overflow: hidden;
}

.sidebar-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 99;
}

.logo-area {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 20px;
  background: #111827;
  color: #fff;
  gap: 10px;
}

.logo-icon {
  flex-shrink: 0;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  white-space: nowrap;
}

.sidebar-menu {
  border-right: none;
  height: calc(100vh - 60px);
}

.sidebar-menu :deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: rgba(64, 158, 255, 0.15);
}

.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
  min-width: 0;
}

.collapse-btn {
  cursor: pointer;
  color: #606266;
  transition: color 0.3s;
  flex-shrink: 0;
}

.collapse-btn:hover {
  color: #409eff;
}

.breadcrumb {
  min-width: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 5px 10px;
  border-radius: 6px;
  transition: background 0.3s;
}

.user-info:hover {
  background: #f5f7fa;
}

.username {
  color: #303133;
  font-size: 14px;
}

.main-content {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.profile-body {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.profile-avatar-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    z-index: 100;
    transform: translateX(-100%);
  }
  .sidebar-mobile-open {
    transform: translateX(0);
  }
  .header {
    padding: 0 12px;
  }
  .header-left {
    gap: 10px;
  }
  .main-content {
    padding: 12px;
  }
  .user-info {
    padding: 5px;
  }
}
</style>
