import { defineStore } from 'pinia'
import { login, logout, getCurrentUser } from '../api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null'),
    loading: false
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    userName: (state) => state.userInfo?.name || '',
    userRole: (state) => state.userInfo?.role || '',
    isAdmin: (state) => state.userInfo?.role === 'admin'
  },

  actions: {
    async login(username, password) {
      this.loading = true
      try {
        const res = await login({ username, password })
        this.token = res.data.token
        this.userInfo = res.data.user
        
        localStorage.setItem('token', res.data.token)
        localStorage.setItem('userInfo', JSON.stringify(res.data.user))
        
        return res
      } finally {
        this.loading = false
      }
    },

    async logout() {
      try {
        await logout()
      } catch (e) {
        // 忽略退出接口错误
      } finally {
        this.token = ''
        this.userInfo = null
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
      }
    },

    async fetchUserInfo() {
      try {
        const res = await getCurrentUser()
        this.userInfo = res.data
        localStorage.setItem('userInfo', JSON.stringify(res.data))
        return res.data
      } catch (e) {
        // 获取用户信息失败时，清除登录状态
        this.token = ''
        this.userInfo = null
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        throw e
      }
    }
  }
})
