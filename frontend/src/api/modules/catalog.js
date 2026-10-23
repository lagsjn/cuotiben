/**
 * 功能 4：学科、知识点与标签分类
 */
import { http } from '@/api/http'

/** 学科列表（含题量，用于筛选下拉与分类页） */
export function fetchSubjects() {
  return http.get('/subjects')
}

/** 某学科下的知识点树，如 高等数学 → 极限 → 洛必达法则 */
export function fetchKnowledgePoints(subjectId) {
  return http.get(`/subjects/${subjectId}/knowledge-points`)
}

export function createSubject(payload) {
  return http.post('/subjects', payload)
}

export function createKnowledgePoint(payload) {
  return http.post('/knowledge-points', payload)
}

/** 自定义标签：重点题 / 易错题 / 典型题 / 计算题 / 期末复习 … */
export function fetchTags() {
  return http.get('/tags')
}

export function createTag(payload) {
  return http.post('/tags', payload)
}

export function deleteTag(id) {
  return http.delete(`/tags/${id}`)
}
