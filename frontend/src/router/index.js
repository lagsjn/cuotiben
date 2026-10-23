import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'

/**
 * 路由已按《基本功能文档》的 10 项功能预留：
 *  - /library  错题库（功能 2/3/4/5/6/7）
 *  - /review   复习（功能 8）
 *  - /stats    统计（功能 9）
 *  - /export   导出（功能 10）
 *  - /login /register（功能 1）
 * 当前仅首页为正式页面，其余跳转到占位页，后续逐个替换为真实页面即可。
 */
const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView,
    meta: { title: '错题本 · 错过的题，只错一次' }
  },
  {
    path: '/library',
    name: 'library',
    component: () => import('@/views/PlaceholderView.vue'),
    meta: { title: '错题库', desc: '检索、筛选、编辑与维护你的错题' }
  },
  {
    path: '/library/new',
    name: 'mistake-new',
    component: () => import('@/views/PlaceholderView.vue'),
    meta: { title: '录入错题', desc: '支持文字录入与题目图片上传' }
  },
  {
    path: '/review',
    name: 'review',
    component: () => import('@/views/PlaceholderView.vue'),
    meta: { title: '复习', desc: '按到期日期练习错题，先做题再展开答案' }
  },
  {
    path: '/stats',
    name: 'stats',
    component: () => import('@/views/PlaceholderView.vue'),
    meta: { title: '统计', desc: '学科、知识点、错误原因与掌握状态分布' }
  },
  {
    path: '/export',
    name: 'export',
    component: () => import('@/views/PlaceholderView.vue'),
    meta: { title: '导出错题集', desc: '勾选题目，导出为 Word 或 PDF' }
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/PlaceholderView.vue'),
    meta: { title: '登录', desc: '用户名或邮箱 + 密码登录' }
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/PlaceholderView.vue'),
    meta: { title: '注册', desc: '创建账号，开始积累自己的错题库' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/PlaceholderView.vue'),
    meta: { title: '页面不存在', desc: '这个地址暂时没有对应页面' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    return savedPosition ?? { top: 0 }
  }
})

router.afterEach((to) => {
  document.title = to.meta?.title ? to.meta.title : '错题本'
})

export default router
