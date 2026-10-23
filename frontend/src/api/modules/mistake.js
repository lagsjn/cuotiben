/**
 * 功能 2/3/6/7：错题的录入、编辑、删除、检索筛选、掌握状态
 */
import { http } from '@/api/http'

/**
 * 分页检索错题
 * @param {{keyword?:string, subjectId?:number, knowledgePointId?:number,
 *          tagIds?:number[], reasonCode?:string, masteryStatus?:string,
 *          dueOnly?:boolean, page?:number, size?:number}} params
 */
export function fetchMistakes(params) {
  return http.get('/mistakes', params)
}

/** 错题详情（复习页做题时使用） */
export function fetchMistakeById(id) {
  return http.get(`/mistakes/${id}`)
}

/** 新建错题：文字内容字段 + 图片（图片先调 uploadMistakeImage） */
export function createMistake(payload) {
  return http.post('/mistakes', payload)
}

/** 编辑错题 */
export function updateMistake(id, payload) {
  return http.put(`/mistakes/${id}`, payload)
}

/** 删除错题（同时清理标签关联与复习记录） */
export function deleteMistake(id) {
  return http.delete(`/mistakes/${id}`)
}

/** 更新掌握状态与下次复习日期（功能 7/8） */
export function updateMastery(id, payload) {
  return http.put(`/mistakes/${id}/mastery`, payload)
}

/** 功能 5：更新错误原因与补充说明 */
export function updateErrorReason(id, payload) {
  return http.put(`/mistakes/${id}/error-reason`, payload)
}

/**
 * 图片上传（功能 2）：multipart/form-data，返回 { url, path }
 * 后端保存到服务器目录，数据库只存路径
 */
export function uploadMistakeImage(file) {
  const form = new FormData()
  form.append('file', file)
  return http.post('/mistakes/images', form, {
    headers: {} // 交给浏览器自动带 boundary，不要手动设置 Content-Type
  })
}

/** 功能 10：导出错题集，返回文件二进制 Blob */
export function exportMistakes(payload) {
  return http.post('/export', payload, { responseType: 'blob' })
}
