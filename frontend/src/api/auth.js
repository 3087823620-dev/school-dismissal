import { request } from '../utils/request'

// 登录
export const login = (data) => {
  return request.post('/auth/login', data)
}

// 退出登录
export const logout = () => {
  return request.post('/auth/logout')
}

// 获取当前用户信息
export const getCurrentUser = () => {
  return request.get('/auth/me')
}

// 修改密码
export const changePassword = (data) => {
  return request.post('/auth/change-password', data)
}

// 更新个人信息
export const updateProfile = (data) => {
  return request.put('/auth/profile', data)
}

// 上传头像
export const uploadAvatar = (formData) => {
  return request.post('/auth/avatar', formData)
}

// 获取统计数据
export const getStats = () => {
  return request.get('/users/stats/summary')
}
