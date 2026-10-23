/**
 * 功能 1：用户注册与登录
 */
import { http, setToken, clearToken } from '@/api/http'

/** @param {{username:string, email:string, password:string}} payload */
export function register(payload) {
  return http.post('/auth/register', payload)
}

/** @param {{account:string, password:string}} payload account 可为用户名或邮箱 */
export async function login(payload) {
  const data = await http.post('/auth/login', payload)
  if (data?.token) setToken(data.token)
  return data
}

export async function logout() {
  try {
    await http.post('/auth/logout')
  } finally {
    clearToken()
  }
}

/** 获取当前登录用户信息（未登录时后端返回 401） */
export function fetchCurrentUser() {
  return http.get('/auth/me')
}
