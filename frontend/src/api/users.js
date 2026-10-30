import { request } from '../utils/request'

// 获取用户列表
export const getUserList = () => {
  return request.get('/users')
}

// 获取用户详情
export const getUserDetail = (id) => {
  return request.get(`/users/${id}`)
}

// 新增用户
export const createUser = (data) => {
  return request.post('/users', data)
}

// 更新用户
export const updateUser = (id, data) => {
  return request.put(`/users/${id}`, data)
}

// 删除用户
export const deleteUser = (id) => {
  return request.delete(`/users/${id}`)
}
