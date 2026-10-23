/**
 * 功能 8：错题复习与待复习管理
 */
import { http } from '@/api/http'

/**
 * 待复习队列：按日期排序，同一天优先未掌握
 * @param {{date?:string, subjectId?:number, reasonCode?:string}} params
 */
export function fetchReviewQueue(params) {
  return http.get('/reviews/queue', params)
}

/**
 * 提交一次复习结果
 * @param {{mistakeId:number, result:'CORRECT'|'WRONG'|'UNSURE',
 *          masteryStatus:string, reasonCode?:string, reasonNote?:string,
 *          nextReviewDate?:string|null}} payload
 */
export function submitReview(payload) {
  return http.post('/reviews', payload)
}

/** 单题加入 / 移出待复习，并设置下次复习日期 */
export function scheduleReview(id, payload) {
  return http.put(`/mistakes/${id}/schedule`, payload)
}

/** 某道错题的复习历史 */
export function fetchReviewHistory(id) {
  return http.get(`/mistakes/${id}/reviews`)
}
