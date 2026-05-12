import request from '../utils/request'

export const pageAuditLogs = (params) => request.get('/api/audit-logs', { params })

export const exportAuditLogs = (params) =>
  request.get('/api/audit-logs/export', { params, responseType: 'blob' })

export const getDashboardStats = () => request.get('/api/dashboard/stats')
