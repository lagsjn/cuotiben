/**
 * 内置 mock 服务：按真实接口路径分发，返回与后端约定一致的数据结构。
 * 仅用于后端未就绪阶段；把 .env 中 VITE_USE_MOCK 改为 false 即走真实接口。
 */
import {
  homeOverview,
  subjects,
  knowledgePoints,
  tags,
  mistakes,
  statsSummary
} from './data'

const LATENCY = 240
const clone = (value) => JSON.parse(JSON.stringify(value))

const state = {
  mistakes: clone(mistakes),
  subjects: clone(subjects),
  tags: clone(tags),
  reviews: []
}

let nextMistakeId = 200
let nextTagId = 100
let nextSubjectId = 100

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms))

function fail(message, code = 404, status = 404) {
  const err = new Error(message)
  err.code = code
  err.status = status
  return err
}

const sortByNewest = (list) =>
  [...list].sort(
    (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
  )

const toSummary = (m) => ({
  id: m.id,
  title: m.title,
  subjectName: m.subjectName,
  knowledgePointName: m.knowledgePointName,
  tags: m.tags ?? [],
  masteryStatus: m.masteryStatus,
  reasonCode: m.reasonCode,
  displayDate: m.displayDate ?? ''
})

const findMistake = (id) => {
  const target = state.mistakes.find((m) => String(m.id) === String(id))
  if (!target) throw fail('错题不存在或已删除', 40404, 404)
  return target
}

const routes = [
  /* ---------- 首页 ---------- */
  {
    method: 'GET',
    pattern: /^\/home\/overview$/,
    handler: () => ({
      profile: clone(homeOverview.profile),
      stats: clone(homeOverview.stats),
      reviewQueue: clone(homeOverview.reviewQueue),
      subjectDistribution: clone(homeOverview.subjectDistribution),
      reasonDistribution: clone(homeOverview.reasonDistribution),
      recentMistakes: sortByNewest(state.mistakes).slice(0, 4).map(toSummary)
    })
  },

  /* ---------- 功能 1：认证 ---------- */
  {
    method: 'POST',
    pattern: /^\/auth\/login$/,
    handler: ({ data }) => {
      if (!data?.account || !data?.password) throw fail('请输入账号和密码', 40001, 400)
      return {
        token: 'mock-token',
        user: clone(homeOverview.profile)
      }
    }
  },
  {
    method: 'POST',
    pattern: /^\/auth\/register$/,
    handler: ({ data }) => {
      if (!data?.username || !data?.password) throw fail('请填写用户名和密码', 40001, 400)
      return { id: 1, ...data }
    }
  },
  { method: 'POST', pattern: /^\/auth\/logout$/, handler: () => null },
  {
    method: 'GET',
    pattern: /^\/auth\/me$/,
    handler: () => clone(homeOverview.profile)
  },

  /* ---------- 功能 2/3/6/7：错题 ---------- */
  {
    method: 'GET',
    pattern: /^\/mistakes$/,
    handler: ({ params = {} }) => {
      const {
        keyword,
        subjectId,
        knowledgePointId,
        reasonCode,
        masteryStatus,
        page = 1,
        size = 10
      } = params
      let list = sortByNewest(state.mistakes)
      if (keyword) {
        const kw = String(keyword).trim().toLowerCase()
        list = list.filter((m) =>
          [m.title, m.analysis, m.source, m.reasonNote]
            .filter(Boolean)
            .some((field) => String(field).toLowerCase().includes(kw))
        )
      }
      if (subjectId) list = list.filter((m) => String(m.subjectId) === String(subjectId))
      if (knowledgePointId) {
        list = list.filter(
          (m) => String(m.knowledgePointId) === String(knowledgePointId)
        )
      }
      if (reasonCode) list = list.filter((m) => m.reasonCode === reasonCode)
      if (masteryStatus) list = list.filter((m) => m.masteryStatus === masteryStatus)

      const current = Number(page) || 1
      const pageSize = Number(size) || 10
      const total = list.length
      return {
        list: clone(list.slice((current - 1) * pageSize, current * pageSize)),
        total,
        page: current,
        size: pageSize
      }
    }
  },
  {
    method: 'POST',
    pattern: /^\/mistakes$/,
    handler: ({ data }) => {
      if (!data?.title) throw fail('题目内容不能为空', 40002, 400)
      const subject = state.subjects.find(
        (s) => String(s.id) === String(data.subjectId)
      )
      const created = {
        id: ++nextMistakeId,
        answer: '',
        myAnswer: '',
        analysis: '',
        source: '',
        reasonNote: '',
        imageUrls: [],
        tags: [],
        masteryStatus: 'NOT_REVIEWED',
        createdAt: new Date().toISOString(),
        displayDate: '刚刚',
        ...data,
        subjectName: subject?.name ?? data.subjectName ?? ''
      }
      state.mistakes.push(created)
      return clone(created)
    }
  },
  {
    method: 'GET',
    pattern: /^\/mistakes\/([^/]+)$/,
    handler: ({ match }) => clone(findMistake(match[1]))
  },
  {
    method: 'PUT',
    pattern: /^\/mistakes\/([^/]+)$/,
    handler: ({ match, data }) => {
      const target = findMistake(match[1])
      Object.assign(target, data)
      return clone(target)
    }
  },
  {
    method: 'DELETE',
    pattern: /^\/mistakes\/([^/]+)$/,
    handler: ({ match }) => {
      findMistake(match[1])
      state.mistakes = state.mistakes.filter(
        (m) => String(m.id) !== String(match[1])
      )
      return null
    }
  },
  {
    method: 'PUT',
    pattern: /^\/mistakes\/([^/]+)\/mastery$/,
    handler: ({ match, data }) => {
      const target = findMistake(match[1])
      target.masteryStatus = data?.masteryStatus ?? target.masteryStatus
      target.nextReviewDate = data?.nextReviewDate ?? target.nextReviewDate
      return clone(target)
    }
  },
  {
    method: 'PUT',
    pattern: /^\/mistakes\/([^/]+)\/error-reason$/,
    handler: ({ match, data }) => {
      const target = findMistake(match[1])
      target.reasonCode = data?.reasonCode ?? target.reasonCode
      target.reasonNote = data?.reasonNote ?? target.reasonNote
      return clone(target)
    }
  },
  {
    method: 'PUT',
    pattern: /^\/mistakes\/([^/]+)\/schedule$/,
    handler: ({ match, data }) => {
      const target = findMistake(match[1])
      target.nextReviewDate = data?.nextReviewDate ?? null
      target.inReviewQueue = data?.inReviewQueue ?? true
      return clone(target)
    }
  },
  {
    method: 'GET',
    pattern: /^\/mistakes\/([^/]+)\/reviews$/,
    handler: ({ match }) =>
      clone(state.reviews.filter((r) => String(r.mistakeId) === String(match[1])))
  },

  /* ---------- 功能 8：复习 ---------- */
  {
    method: 'GET',
    pattern: /^\/reviews\/queue$/,
    handler: () => clone(homeOverview.reviewQueue)
  },
  {
    method: 'POST',
    pattern: /^\/reviews$/,
    handler: ({ data }) => {
      const target = findMistake(data?.mistakeId)
      const record = {
        id: state.reviews.length + 1,
        mistakeId: target.id,
        result: data.result,
        masteryStatus: data.masteryStatus,
        reasonCode: data.reasonCode ?? target.reasonCode,
        reasonNote: data.reasonNote ?? '',
        nextReviewDate: data.nextReviewDate ?? null,
        reviewedAt: new Date().toISOString()
      }
      state.reviews.push(record)
      Object.assign(target, {
        masteryStatus: record.masteryStatus,
        nextReviewDate: record.nextReviewDate
      })
      return clone(record)
    }
  },

  /* ---------- 功能 4：分类 ---------- */
  { method: 'GET', pattern: /^\/subjects$/, handler: () => clone(state.subjects) },
  {
    method: 'POST',
    pattern: /^\/subjects$/,
    handler: ({ data }) => {
      const created = { id: ++nextSubjectId, count: 0, ...data }
      state.subjects.push(created)
      return clone(created)
    }
  },
  {
    method: 'GET',
    pattern: /^\/subjects\/([^/]+)\/knowledge-points$/,
    handler: ({ match }) => clone(knowledgePoints[match[1]] ?? [])
  },
  {
    method: 'POST',
    pattern: /^\/knowledge-points$/,
    handler: ({ data }) => ({ id: ++nextSubjectId, count: 0, ...data })
  },
  { method: 'GET', pattern: /^\/tags$/, handler: () => clone(state.tags) },
  {
    method: 'POST',
    pattern: /^\/tags$/,
    handler: ({ data }) => {
      if (!data?.name) throw fail('标签名不能为空', 40003, 400)
      const created = { id: ++nextTagId, count: 0, ...data }
      state.tags.push(created)
      return clone(created)
    }
  },
  {
    method: 'DELETE',
    pattern: /^\/tags\/([^/]+)$/,
    handler: ({ match }) => {
      state.tags = state.tags.filter((t) => String(t.id) !== String(match[1]))
      return null
    }
  },

  /* ---------- 功能 9：统计 ---------- */
  { method: 'GET', pattern: /^\/stats\/summary$/, handler: () => clone(statsSummary) },
  {
    method: 'GET',
    pattern: /^\/stats\/reasons$/,
    handler: () => clone(statsSummary.reasonDistribution)
  },
  {
    method: 'GET',
    pattern: /^\/stats\/trend$/,
    handler: ({ params = {} }) => ({
      range: params.range === 'month' ? 'month' : 'week',
      newCount: params.range === 'month' ? 41 : 12,
      points: [
        { date: '2026-03-16', count: 2 },
        { date: '2026-03-17', count: 3 },
        { date: '2026-03-18', count: 1 },
        { date: '2026-03-19', count: 4 },
        { date: '2026-03-20', count: 2 },
        { date: '2026-03-21', count: 3 },
        { date: '2026-03-22', count: 0 }
      ]
    })
  },

  /* ---------- 功能 10：导出 ---------- */
  {
    method: 'POST',
    pattern: /^\/export$/,
    handler: ({ data }) => ({
      fileName: `错题集-${new Date().toISOString().slice(0, 10)}.${
        data?.format === 'PDF' ? 'pdf' : 'docx'
      }`,
      format: data?.format ?? 'WORD',
      count: data?.ids?.length ?? 0,
      downloadUrl: '#'
    })
  }
]

export async function mockRequest(path, { method = 'GET', data, params } = {}) {
  await sleep(LATENCY)
  const upperMethod = method.toUpperCase()
  for (const route of routes) {
    const match = route.pattern.exec(path)
    if (match && route.method === upperMethod) {
      return route.handler({ match, data, params })
    }
  }
  // 同路径但方法不匹配时给出更明确的提示
  if (routes.some((route) => route.pattern.test(path))) {
    throw fail(`mock 未实现 ${upperMethod} ${path}`, 40500, 405)
  }
  throw fail(`mock 中不存在接口：${upperMethod} ${path}`, 40400, 404)
}
