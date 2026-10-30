import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const service = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 添加 token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data
    
    // 如果是二进制数据（下载），直接返回
    if (response.config.responseType === 'blob') {
      return response
    }
    
    // 业务错误
    if (res.code && res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || 'Error'))
    }
    
    return res
  },
  (error) => {
    console.error('响应错误:', error)
    
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        ElMessage.error('登录已过期，请重新登录')
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        setTimeout(() => {
          window.location.href = '/login'
        }, 1000)
      } else if (status === 429) {
        // 限流提示 — 从响应体中提取消息
        const msg = error.response.data?.message || '请求过于频繁，请稍后再试'
        ElMessage.warning(msg)
      } else if (status === 404) {
        ElMessage.error('请求的资源不存在')
      } else if (status === 500) {
        ElMessage.error('服务器内部错误')
      } else {
        ElMessage.error(error.message || '网络错误')
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请检查网络连接')
    } else {
      ElMessage.error('网络连接失败，请检查后端服务是否启动')
    }
    
    return Promise.reject(error)
  }
)

// 封装常用请求方法
const wrap = (fn) => (url, data, config) => {
  // FormData 上传：删掉默认的 application/json，让浏览器自动带 multipart boundary
  if (data instanceof FormData) {
    config = { ...(config || {}), headers: { ...(config?.headers || {}), 'Content-Type': undefined } }
  }
  return fn(url, data, config)
}

export const request = {
  get: (url, params) => service.get(url, { params }),
  post: wrap((url, data, config) => service.post(url, data, config)),
  put: (url, data) => service.put(url, data),
  delete: (url) => service.delete(url)
}


export default service
