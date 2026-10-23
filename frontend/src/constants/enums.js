/**
 * 业务枚举常量
 * 与后端 MasteryStatus / ErrorReasonCode 枚举一一对应，改动需同步接口文档。
 */

/** 掌握状态（功能 7：掌握状态管理） */
export const MASTERY_STATUS = {
  NOT_REVIEWED: { code: 'NOT_REVIEWED', label: '未复习', tone: 'gray' },
  REVIEWING: { code: 'REVIEWING', label: '复习中', tone: 'amber' },
  MASTERED: { code: 'MASTERED', label: '已掌握', tone: 'green' }
}

export const MASTERY_STATUS_LIST = Object.values(MASTERY_STATUS)

/** 错误原因（功能 5：错误原因记录） */
export const ERROR_REASONS = [
  { code: 'KNOWLEDGE', label: '知识点不会' },
  { code: 'THINKING', label: '思路错误' },
  { code: 'FORMULA', label: '公式记错' },
  { code: 'CALCULATION', label: '计算错误' },
  { code: 'READING', label: '审题错误' },
  { code: 'CARELESS', label: '粗心' },
  { code: 'TIME_SHORTAGE', label: '时间不足' },
  { code: 'OTHER', label: '其他' }
]

export const ERROR_REASON_MAP = ERROR_REASONS.reduce((acc, item) => {
  acc[item.code] = item.label
  return acc
}, {})

/** 复习结果（功能 8：错题复习） */
export const REVIEW_RESULTS = [
  { code: 'CORRECT', label: '做对了' },
  { code: 'WRONG', label: '又做错' },
  { code: 'UNSURE', label: '没把握' }
]

/** 导出格式（功能 10：错题集导出） */
export const EXPORT_FORMATS = [
  { code: 'WORD', label: 'Word' },
  { code: 'PDF', label: 'PDF' }
]

export function masteryLabel(code) {
  return MASTERY_STATUS[code]?.label ?? '未知'
}
