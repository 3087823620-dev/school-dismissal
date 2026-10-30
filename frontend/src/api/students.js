import service, { request } from '../utils/request'

// 获取学生列表
export const getStudentList = (params) => {
  return request.get('/students', params)
}

// 获取学生详情
export const getStudentDetail = (id) => {
  return request.get(`/students/${id}`)
}

// 新增学生
export const createStudent = (data) => {
  return request.post('/students', data)
}

// 更新学生
export const updateStudent = (id, data) => {
  return request.put(`/students/${id}`, data)
}

// 删除学生
export const deleteStudent = (id) => {
  return request.delete(`/students/${id}`)
}

//上传学生人脸照片
export const uploadStudentFace = (id, file)=>{
  const fd = new FormData();
  fd.append('file',file);
  return service.post(`/students/${id}/face`, fd,{
    headers: { 'Content-type': 'multipart/form-data'}
  })
}