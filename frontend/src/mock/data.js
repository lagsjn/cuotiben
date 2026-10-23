/**
 * 演示数据（mock）
 * 数据结构与 docs/API接口文档.md 中的响应体保持一致；
 * 后端接通后删除 mock 即可，页面代码无需改动。
 */

export const subjects = [
  { id: 1, name: '高等数学', color: '#2c4bd6', count: 42 },
  { id: 2, name: '数据结构', color: '#4b5b8f', count: 26 },
  { id: 3, name: '大学英语', color: '#2a7250', count: 24 },
  { id: 4, name: '线性代数', color: '#8a6a2f', count: 21 },
  { id: 5, name: '概率论', color: '#7a4b6b', count: 15 }
]

export const knowledgePoints = {
  1: [
    { id: 11, name: '极限', parentId: null, count: 18 },
    { id: 12, name: '导数与微分', parentId: null, count: 9 },
    { id: 13, name: '定积分', parentId: null, count: 10 },
    { id: 14, name: '洛必达法则', parentId: 11, count: 6 }
  ],
  2: [
    { id: 21, name: '树与二叉树', parentId: null, count: 11 },
    { id: 22, name: '图与最短路径', parentId: null, count: 8 },
    { id: 23, name: '排序算法', parentId: null, count: 7 }
  ],
  3: [
    { id: 31, name: '虚拟语气', parentId: null, count: 8 },
    { id: 32, name: '主谓一致', parentId: null, count: 6 },
    { id: 33, name: '阅读理解', parentId: null, count: 10 }
  ],
  4: [
    { id: 41, name: '线性方程组', parentId: null, count: 9 },
    { id: 42, name: '特征值与特征向量', parentId: null, count: 12 }
  ],
  5: [
    { id: 51, name: '随机变量分布', parentId: null, count: 8 },
    { id: 52, name: '参数估计', parentId: null, count: 7 }
  ]
}

export const tags = [
  { id: 1, name: '重点题', count: 23 },
  { id: 2, name: '易错题', count: 31 },
  { id: 3, name: '典型题', count: 18 },
  { id: 4, name: '计算题', count: 27 },
  { id: 5, name: '期末复习', count: 39 }
]

const makeMistake = (m) => ({
  answer: '',
  myAnswer: '',
  analysis: '',
  source: '',
  reasonNote: '',
  imageUrls: [],
  nextReviewDate: null,
  createdAt: '',
  ...m
})

export const mistakes = [
  makeMistake({
    id: 128,
    title: '求 lim(x→0) (eˣ − 1 − x) / x²',
    subjectId: 1,
    subjectName: '高等数学',
    knowledgePointId: 14,
    knowledgePointName: '极限 · 洛必达法则',
    tags: ['期末复习', '计算题'],
    masteryStatus: 'REVIEWING',
    reasonCode: 'CALCULATION',
    reasonNote: '等价无穷小替换后没有展开到二阶，直接用了 x²/2 近似却漏了剩余项',
    source: '《高等数学》同济七版 上册 P78 习题 3-4',
    createdAt: '2026-03-21T09:24:00',
    displayDate: '今天',
    dueLabel: '逾期 2 天',
    overdue: true
  }),
  makeMistake({
    id: 127,
    title: '给定中序线索二叉树，写出中序遍历的非递归算法，并说明时间复杂度。',
    subjectId: 2,
    subjectName: '数据结构',
    knowledgePointId: 21,
    knowledgePointName: '树与二叉树',
    tags: ['易错题'],
    masteryStatus: 'NOT_REVIEWED',
    reasonCode: 'THINKING',
    reasonNote: '没有理解“线索”指向后继节点这一前提，写成了普通栈式遍历',
    source: '《数据结构》严蔚敏 第 6 章 课后 6.14',
    createdAt: '2026-03-20T21:10:00',
    displayDate: '昨天'
  }),
  makeMistake({
    id: 126,
    title: '设 X ~ N(μ, σ²)，求 P(|X − μ| < 2σ) 的近似值。',
    subjectId: 5,
    subjectName: '概率论',
    knowledgePointId: 51,
    knowledgePointName: '随机变量分布',
    tags: ['重点题'],
    masteryStatus: 'MASTERED',
    reasonCode: 'FORMULA',
    reasonNote: '标准正态分布表查错行，把 2.0 查成了 1.96',
    source: '《概率论与数理统计》浙大四版 P56 例 2.18',
    createdAt: '2026-03-19T16:02:00',
    displayDate: '2 天前'
  }),
  makeMistake({
    id: 125,
    title: 'The committee ___ divided in their opinions on the new policy.',
    subjectId: 3,
    subjectName: '大学英语',
    knowledgePointId: 32,
    knowledgePointName: '主谓一致',
    tags: ['典型题'],
    masteryStatus: 'REVIEWING',
    reasonCode: 'READING',
    reasonNote: '看到 committee 就当单数选 was，忽略了 in their opinions 的复数指向',
    source: '《新视野大学英语》读写教程 3 Unit 2',
    createdAt: '2026-03-18T10:35:00',
    displayDate: '3 天前'
  }),
  makeMistake({
    id: 124,
    title: '设 A 为 3 阶矩阵，r(A) = 2，求 Ax = 0 的基础解系。',
    subjectId: 4,
    subjectName: '线性代数',
    knowledgePointId: 41,
    knowledgePointName: '线性方程组',
    tags: ['重点题', '计算题'],
    masteryStatus: 'NOT_REVIEWED',
    reasonCode: 'THINKING',
    reasonNote: '没先判断自由变量个数 n − r(A) = 1，导致解向量取错',
    source: '《线性代数》同济六版 P92 习题 4-2',
    createdAt: '2026-03-18T08:10:00',
    displayDate: '3 天前',
    dueLabel: '今天到期',
    overdue: false
  }),
  makeMistake({
    id: 121,
    title:
      'It is essential that every student ___ the safety rules before the experiment.',
    subjectId: 3,
    subjectName: '大学英语',
    knowledgePointId: 31,
    knowledgePointName: '虚拟语气',
    tags: ['期末复习'],
    masteryStatus: 'REVIEWING',
    reasonCode: 'KNOWLEDGE',
    reasonNote: 'it is essential that 后用 (should) + 动词原形，背诵时记串了',
    source: '大学英语四级真题 2024.12 选词填空',
    createdAt: '2026-03-17T19:40:00',
    displayDate: '4 天前',
    dueLabel: '今天到期',
    overdue: false
  })
]

export const statsSummary = {
  totalCount: 128,
  dueTodayCount: 3,
  overdueCount: 1,
  masteredCount: 59,
  masteredRate: 46,
  reviewingCount: 32,
  notReviewedCount: 37,
  newThisWeekCount: 12,
  newGrowth: 4,
  streakDays: 6,
  subjectDistribution: subjects.map(({ name, count, color }) => ({
    name,
    count,
    color
  })),
  reasonDistribution: [
    { code: 'CALCULATION', name: '计算错误', count: 44, percent: 34 },
    { code: 'THINKING', name: '思路错误', count: 34, percent: 27 },
    { code: 'KNOWLEDGE', name: '知识点不会', count: 23, percent: 18 },
    { code: 'FORMULA', name: '公式记错', count: 15, percent: 12 },
    { code: 'READING', name: '审题错误', count: 12, percent: 9 }
  ]
}

export const homeOverview = {
  profile: {
    nickname: '林同学',
    identity: '大三 · 计算机科学与技术',
    goal: '考研 + 期末复习'
  },
  stats: {
    totalCount: 128,
    dueTodayCount: 3,
    overdueCount: 1,
    masteredCount: 59,
    masteredRate: 46,
    newThisWeekCount: 12,
    newGrowth: 4,
    streakDays: 6
  },
  reviewQueue: [
    {
      id: 128,
      title: '求 lim(x→0) (eˣ − 1 − x) / x²',
      subjectName: '高等数学',
      knowledgePointName: '极限 · 洛必达法则',
      masteryStatus: 'REVIEWING',
      dueLabel: '逾期 2 天',
      overdue: true
    },
    {
      id: 124,
      title: '设 A 为 3 阶矩阵，r(A) = 2，求 Ax = 0 的基础解系。',
      subjectName: '线性代数',
      knowledgePointName: '线性方程组',
      masteryStatus: 'NOT_REVIEWED',
      dueLabel: '今天到期',
      overdue: false
    },
    {
      id: 121,
      title:
        'It is essential that every student ___ the safety rules before the experiment.',
      subjectName: '大学英语',
      knowledgePointName: '虚拟语气',
      masteryStatus: 'REVIEWING',
      dueLabel: '今天到期',
      overdue: false
    }
  ],
  subjectDistribution: statsSummary.subjectDistribution,
  reasonDistribution: statsSummary.reasonDistribution
}
