import request from '../utils/request'

export const login = (data) => request.post('/api/auth/login', data)
export const refresh = () => request.post('/api/auth/refresh')
export const logout = () => request.post('/api/auth/logout')
export const me = () => request.get('/api/users/me')
