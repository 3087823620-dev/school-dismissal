<template>
  <div class="login-container">
    <!-- 动态背景层（开屏结束后保留） -->
    <div class="bg-ripple">
      <svg viewBox="0 0 100 100" preserveAspectRatio="xMidYMid slice">
        <circle cx="50" cy="50" r="20" fill="none" stroke="rgba(255,255,255,0.15)" stroke-width="0.2"/>
        <circle cx="50" cy="50" r="30" fill="none" stroke="rgba(255,255,255,0.12)" stroke-width="0.2"/>
        <circle cx="50" cy="50" r="40" fill="none" stroke="rgba(255,255,255,0.1)" stroke-width="0.2"/>
        <circle cx="50" cy="50" r="50" fill="none" stroke="rgba(255,255,255,0.08)" stroke-width="0.2"/>
      </svg>
    </div>
    <div class="bg-glow bg-glow-1"></div>
    <div class="bg-glow bg-glow-2"></div>

    <!-- 开屏 entry 层（复刻青鸽推近动画） -->
    <div class="entry" v-if="showEntry" data-p0-entry>
      <div class="entry-viewport" data-p0-entry-viewport></div>
      <div class="entry-atmosphere" data-p0-entry-atmosphere></div>
      <div class="entry-halo" data-p0-entry-halo>
        <svg viewBox="0 0 100 100" preserveAspectRatio="xMidYMid slice">
          <defs>
            <radialGradient id="haloFade" cx="50%" cy="50%" r="50%">
              <stop offset="0%" stop-color="#e0eeff" stop-opacity="0.6"/>
              <stop offset="70%" stop-color="#e0eeff" stop-opacity="0.3"/>
              <stop offset="100%" stop-color="#e0eeff" stop-opacity="0.05"/>
            </radialGradient>
          </defs>
          <circle cx="50" cy="50" r="15" fill="none" stroke="url(#haloFade)" stroke-width="0.25"/>
          <circle cx="50" cy="50" r="22" fill="none" stroke="url(#haloFade)" stroke-width="0.25"/>
          <circle cx="50" cy="50" r="29" fill="none" stroke="url(#haloFade)" stroke-width="0.25"/>
          <circle cx="50" cy="50" r="36" fill="none" stroke="url(#haloFade)" stroke-width="0.25"/>
          <circle cx="50" cy="50" r="43" fill="none" stroke="url(#haloFade)" stroke-width="0.25"/>
          <circle cx="50" cy="50" r="50" fill="none" stroke="url(#haloFade)" stroke-width="0.25"/>
        </svg>
      </div>
      <div class="entry-camera" data-p0-entry-camera>
        <img src="/hero.jpg" alt="鹰鸽放学" />
      </div>
      <div class="entry-cue" data-p0-entry-scroll-cue>鹰鸽放学</div>
    </div>

    <div class="login-card" :class="{ 'card-enter': cardEntered }">
      <div class="login-header">
        <div class="logo">
          <img src="/hero.jpg" alt="logo" />
        </div>
        <h1 class="title">鹰鸽放学</h1>
        <p class="subtitle">可视化放学协同平台</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            size="large"
            prefix-icon="User"
            autocomplete="username"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            prefix-icon="Lock"
            show-password
            autocomplete="current-password"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-button
          type="primary"
          size="large"
          class="login-btn"
          :loading="userStore.loading"
          @click="handleLogin"
        >
          {{ userStore.loading ? '登录中...' : '登 录' }}
        </el-button>
      </el-form>

      <div class="copyright">
        © 2026 鹰鸽放学 · 演示账号 admin / admin123
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref(null)
const showEntry = ref(true)
const cardEntered = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, message: '用户名至少2个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 3, message: '密码至少3个字符', trigger: 'blur' }
  ]
}

let tl = null

onMounted(() => {
  const run = () => {
    const gsap = window.gsap
    if (!gsap) {
      // GSAP 没加载好直接显示
      showEntry.value = false
      cardEntered.value = true
      return
    }

    const camera = document.querySelector('[data-p0-entry-camera]')
    const halo = document.querySelector('[data-p0-entry-halo]')
    const atmosphere = document.querySelector('[data-p0-entry-atmosphere]')
    const viewport = document.querySelector('[data-p0-entry-viewport]')
    const cue = document.querySelector('[data-p0-entry-scroll-cue]')

    // 青鸽原参数
    gsap.set(camera, { scale: 0.72, rotate: -6, transformOrigin: '50% 50%' })
    gsap.set(halo, { scale: 0.84, rotate: -18 })
    gsap.set(atmosphere, { scale: 0.82, autoAlpha: 0.92 })

    tl = gsap.timeline({
      onComplete: () => {
        showEntry.value = false
        cardEntered.value = true
      }
    })

    tl
      .to(cue, { autoAlpha: 0, y: 10, duration: 0.14, ease: 'none' }, 0)
      .to(camera, { scale: 1.32, rotate: 0, duration: 1.12, ease: 'power2.inOut' }, 0)
      .to(halo, { rotate: 36, scale: 1.06, duration: 1.28, ease: 'power1.out' }, 0)
      .to(atmosphere, { scale: 1.16, autoAlpha: 0.3, duration: 1.28, ease: 'power1.out' }, 0)
      .to(viewport, { backgroundColor: '#0d43a8', duration: 0.36, ease: 'none' }, 0.92)
      .to(camera, { scale: 1.55, autoAlpha: 0, duration: 0.28, ease: 'power1.in' }, 1.12)
  }

  if (window.gsap) run()
  else {
    // 动态加载 GSAP
    const s = document.createElement('script')
    s.src = 'https://cdn.jsdelivr.net/npm/gsap@3.12.5/dist/gsap.min.js'
    s.onload = run
    s.onerror = () => { showEntry.value = false; cardEntered.value = true }
    document.head.appendChild(s)
  }
})

onBeforeUnmount(() => {
  tl?.kill()
})

const handleLogin = async () => {
  if (!loginFormRef.value) return
  try {
    await loginFormRef.value.validate()
  } catch (e) {
    return
  }
  try {
    await userStore.login(loginForm.username, loginForm.password)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (e) {
    console.error('登录失败:', e)
  }
}
</script>

<style scoped>
/* ===== 开屏 entry 层 ===== */
.entry {
  position: fixed;
  inset: 0;
  z-index: 9999;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.entry-viewport {
  position: absolute;
  inset: 0;
  background: #1656c9;
}

.entry-atmosphere {
  position: absolute;
  inset: 0;
  background: #1656c9;
}

.entry-halo {
  position: absolute;
  inset: 0;
}

.entry-halo svg {
  width: 100%;
  height: 100%;
}

.entry-camera {
  position: relative;
  width: clamp(110px, 16vw, 170px);
  aspect-ratio: 1;
  border-radius: 50%;
  overflow: hidden;
  box-shadow:
    0 0 0 1px rgba(255,255,255,0.3),
    0 0 0 40px rgba(120,180,255,0.08),
    0 0 0 80px rgba(120,180,255,0.05),
    0 0 0 120px rgba(120,180,255,0.03),
    0 20px 50px rgba(0,0,0,0.35);
}

.entry-camera img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.entry-cue {
  position: absolute;
  bottom: 12%;
  left: 0;
  right: 0;
  text-align: center;
  color: rgba(255,255,255,0.85);
  font-size: 18px;
  letter-spacing: 6px;
  font-weight: 500;
}

/* ===== 登录主体 ===== */
.login-container {
  position: relative;
  width: 100%;
  height: 100vh;
  min-height: 600px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: linear-gradient(135deg, #0a2a7a 0%, #1656c9 50%, #1a63d8 100%);
}

.bg-ripple {
  position: absolute;
  inset: -20%;
  animation: rippleSpin 40s linear infinite;
}

.bg-ripple svg {
  width: 100%;
  height: 100%;
}

@keyframes rippleSpin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.bg-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
}

.bg-glow-1 {
  width: 400px;
  height: 400px;
  background: #4a9eff;
  top: -100px;
  left: -100px;
  animation: glowFloat1 12s ease-in-out infinite;
}

.bg-glow-2 {
  width: 500px;
  height: 500px;
  background: #7ab8ff;
  bottom: -150px;
  right: -150px;
  animation: glowFloat2 15s ease-in-out infinite;
}

@keyframes glowFloat1 {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(80px, 60px); }
}

@keyframes glowFloat2 {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(-60px, -80px); }
}

.login-card {
  position: relative;
  z-index: 1;
  width: 420px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 44px 45px;
  box-shadow: 0 25px 60px rgba(0, 20, 60, 0.35);
  opacity: 0;
  transform: translateY(30px);
  transition: opacity 0.6s ease, transform 0.6s cubic-bezier(0.22, 1, 0.36, 1);
  backdrop-filter: blur(10px);
}

.login-card.card-enter {
  opacity: 1;
  transform: translateY(0);
}

.login-header {
  text-align: center;
  margin-bottom: 35px;
}

.logo {
  width: 80px;
  height: 80px;
  margin: 0 auto 15px;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 10px 25px rgba(22, 86, 201, 0.35);
  border: 3px solid #fff;
}

.logo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.title {
  font-size: 26px;
  font-weight: 700;
  color: #0f2547;
  margin: 0 0 8px 0;
  letter-spacing: 1px;
}

.subtitle {
  font-size: 14px;
  color: #5a6b8a;
  margin: 0;
}

.login-form {
  margin-bottom: 20px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #0d43a8, #1656c9);
  border: none;
  margin-top: 10px;
}

.login-btn:hover {
  opacity: 0.92;
  transform: translateY(-1px);
  box-shadow: 0 10px 24px rgba(22, 86, 201, 0.35);
  background: linear-gradient(135deg, #0d43a8, #1656c9);
}

.copyright {
  text-align: center;
  font-size: 12px;
  color: #8aa0c2;
  margin-top: 10px;
}
</style>
