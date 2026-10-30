import { request } from '../utils/request'

// 获取放学记录
export const getDismissalRecords = (params) => {
  return request.get('/dismissal/records', params)
}

// 获取今日放学状态
export const getTodayStatus = () => {
  return request.get('/dismissal/today-status')
}

// 发布放学信息
export const publishDismissal = (data) => {
  return request.post('/dismissal/publish', data)
}

// 批量放学
export const batchPublishDismissal = (data) => {
  return request.post('/dismissal/batch-publish', data)
}

// 留堂记录
export const addDetention = (data) => {
  return request.post('/dismissal/detention', data)
}

// 获取留堂记录
export const getDetentions = () => {
  return request.get('/dismissal/detentions')
}
