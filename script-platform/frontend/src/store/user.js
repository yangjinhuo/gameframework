import { defineStore } from 'pinia'

const STORAGE_KEY = 'script-platform-user'

const load = () => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : { token: '', userId: null, username: '', role: '' }
  } catch (e) {
    return { token: '', userId: null, username: '', role: '' }
  }
}

export const useUserStore = defineStore('user', {
  state: () => load(),
  getters: {
    isLogin: (s) => !!s.token,
    isSuperAdmin: (s) => s.role === 'super_admin',
    isAdmin: (s) => s.role === 'admin' || s.role === 'super_admin',
    isReadonly: (s) => s.role === 'readonly'
  },
  actions: {
    setLogin(payload) {
      this.token = payload.token
      this.userId = payload.userId
      this.username = payload.username
      this.role = payload.role
      localStorage.setItem(STORAGE_KEY, JSON.stringify(this.$state))
    },
    clear() {
      this.token = ''
      this.userId = null
      this.username = ''
      this.role = ''
      localStorage.removeItem(STORAGE_KEY)
    }
  }
})
