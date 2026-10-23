/**
 * 统一出口：页面只从 '@/api' 引入，便于后续统一切换实现
 */
export { http, request, ApiError, getToken, setToken, clearToken, USE_MOCK } from './http'

export * from './modules/home'
export * from './modules/auth'
export * from './modules/mistake'
export * from './modules/catalog'
export * from './modules/review'
export * from './modules/stats'
