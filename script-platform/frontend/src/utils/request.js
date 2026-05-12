import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
import { useUserStore } from '../store/user'

const service = axios.create({
  baseURL: '/',
  timeout: 30000
})

service.interceptors.request.use((config) => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

service.interceptors.response.use(
  (response) => {
    const { data, config } = response
    // file downloads return blob and do not use the R wrapper
    if (config.responseType === 'blob') return response
    if (data && typeof data === 'object' && 'code' in data) {
      if (data.code === 0) return data.data
      ElMessage({ type: 'error', message: data.message || '请求失败', showClose: true })
      return Promise.reject(new Error(data.message || 'error'))
    }
    return data
  },
  (error) => {
    const status = error.response?.status
    const msg = error.response?.data?.message || error.message || '网络异常'
    if (status === 401) {
      const userStore = useUserStore()
      userStore.clear()
      router.replace('/login')
    }
    ElMessage({ type: 'error', message: msg, showClose: true })
    return Promise.reject(error)
  }
)

export default service
