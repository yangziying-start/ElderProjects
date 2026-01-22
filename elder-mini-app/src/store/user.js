import { defineStore } from 'pinia'
import { ref } from 'vue'
import { get, post } from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(null)
  const token = ref(uni.getStorageSync('token') || '')

  const login = async (code) => {
    const data = await post('/user/login', { code })
    token.value = data
    uni.setStorageSync('token', data)
    await getUserInfo()
    return data
  }

  const getUserInfo = async () => {
    const data = await get('/user/info')
    userInfo.value = data
    return data
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    uni.removeStorageSync('token')
  }

  return { userInfo, token, login, getUserInfo, logout }
})
