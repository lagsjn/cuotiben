/**
 * 统一请求封装
 * 约定：后端 SpringBoot 统一返回 { code: 0, message: "ok", data: {...} }
 * 当 VITE_USE_MOCK=true 时走内置 mock，无需后端即可开发页面。
 * mock 用动态 import：生产构建（VITE_USE_MOCK=false）不会把 mock 数据打进主包。
 */

const API_BASE = import.meta.env.VITE_API_BASE || '/api'

export const USE_MOCK =
  String(import.meta.env.VITE_USE_MOCK ?? 'true').toLowerCase() !== 'false'

const TOKEN_KEY = 'ctb_token'

export class ApiError extends Error {
  constructor(message, code = -1, status = 0) {
    super(message)
    this.name = 'ApiError'
    this.code = code
    this.status = status
  }
}

export function getToken() {
  try {
    return localStorage.getItem(TOKEN_KEY) || ''
  } catch {
    return ''
  }
}

export function setToken(token) {
  try {
    if (token) localStorage.setItem(TOKEN_KEY, token)
    else localStorage.removeItem(TOKEN_KEY)
  } catch {
    /* 隐私模式下忽略 */
  }
}

export function clearToken() {
  setToken('')
}

function withQuery(path, params) {
  if (!params) return path
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') return
    if (Array.isArray(value)) value.forEach((v) => search.append(key, v))
    else search.append(key, value)
  })
  const qs = search.toString()
  return qs ? `${path}${path.includes('?') ? '&' : '?'}${qs}` : path
}

export async function request(path, options = {}) {
  const { method = 'GET', data, params, headers = {}, signal, responseType } = options

  if (USE_MOCK) {
    const { mockRequest } = await import('@/mock')
    return mockRequest(path, { method, data, params })
  }

  const isFormData = typeof FormData !== 'undefined' && data instanceof FormData
  const token = getToken()
  let res
  try {
    res = await fetch(withQuery(`${API_BASE}${path}`, params), {
      method,
      headers: {
        // FormData 必须由浏览器自行生成带 boundary 的 Content-Type
        ...(data !== undefined && !isFormData ? { 'Content-Type': 'application/json' } : {}),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...headers
      },
      body: data === undefined ? undefined : (isFormData ? data : JSON.stringify(data)),
      signal
    })
  } catch (err) {
    if (err?.name === 'AbortError') throw err
    throw new ApiError('网络异常，请确认后端服务已启动', -1, 0)
  }

  if (!res.ok) {
    throw new ApiError(`请求失败（${res.status}）`, res.status, res.status)
  }

  // 导出等接口直接返回文件流，不做 JSON 解析
  if (responseType === 'blob') {
    return res.blob()
  }

  const payload = await res.json()
  if (payload && typeof payload === 'object' && 'code' in payload) {
    if (payload.code !== 0) {
      throw new ApiError(payload.message || '业务处理失败', payload.code, res.status)
    }
    return payload.data
  }
  return payload
}

export const http = {
  get: (path, params, options) => request(path, { ...options, params }),
  post: (path, data, options) => request(path, { ...options, method: 'POST', data }),
  put: (path, data, options) => request(path, { ...options, method: 'PUT', data }),
  delete: (path, params, options) => request(path, { ...options, method: 'DELETE', params })
}
