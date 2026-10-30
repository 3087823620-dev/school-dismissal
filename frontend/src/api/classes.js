import { request } from '../utils/request'

// 获取班级列表
export const getClassList = () => {
  return request.get('/classes')
}

// 获取年级列表
export const getGradeList = () => {
  return request.get('/classes/grades/list')
}

// 获取班级详情
export const getClassDetail = (id) => {
  return request.get(`/classes/${id}`)
}

// 新增班级
export const createClass = (data) => {
  return request.post('/classes', data)
}

// 更新班级
export const updateClass = (id, data) => {
  return request.put(`/classes/${id}`, data)
}

// 删除班级
export const deleteClass = (id) => {
  return request.delete(`/classes/${id}`)
}
