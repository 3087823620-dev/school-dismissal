import { request } from '../utils/request'

// 获取通知列表
export const getNoticeList = (params) => {
  return request.get('/notices', params)
}

// 获取通知详情
export const getNoticeDetail = (id) => {
  return request.get(`/notices/${id}`)
}

// 发布通知
export const createNotice = (data) => {
  return request.post('/notices', data)
}

// 更新通知
export const updateNotice = (id, data) => {
  return request.put(`/notices/${id}`, data)
}

// 删除通知
export const deleteNotice = (id) => {
  return request.delete(`/notices/${id}`)
}
