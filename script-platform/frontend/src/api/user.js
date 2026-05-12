import request from '../utils/request'

export const pageUsers = (params) => request.get('/api/users', { params })
export const getUser = (id) => request.get(`/api/users/${id}`)
export const createUser = (data) => request.post('/api/users', data)
export const updateUser = (id, data) => request.put(`/api/users/${id}`, data)
export const deleteUser = (id) => request.delete(`/api/users/${id}`)
export const updateUserStatus = (id, status) =>
  request.patch(`/api/users/${id}/status`, { status })
