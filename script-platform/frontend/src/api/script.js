import request from '../utils/request'

export const pageScripts = (params) => request.get('/api/scripts', { params })
export const getScript = (id) => request.get(`/api/scripts/${id}`)
export const createScript = (data) => request.post('/api/scripts', data)
export const updateScript = (id, data) => request.put(`/api/scripts/${id}`, data)
export const deleteScript = (id) => request.delete(`/api/scripts/${id}`)
export const updateScriptStatus = (id, status) =>
  request.patch(`/api/scripts/${id}/status`, { status })
export const listModules = () => request.get('/api/scripts/modules')
