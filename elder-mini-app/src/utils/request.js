const BASE_URL = 'http://localhost:8080/api'

export const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')
    
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      success: (res) => {
        console.log(`[${options.method || 'GET'}] ${options.url}`, res.statusCode, res.data)
        
        // 处理403权限问题
        if (res.statusCode === 403) {
          console.warn('权限不足，token可能已过期')
          uni.showModal({
            title: '登录已过期',
            content: '请重新登录',
            showCancel: false,
            success: () => {
              uni.removeStorageSync('token')
              uni.reLaunch({ url: '/pages/login/index' })
            }
          })
          reject(new Error('登录已过期'))
          return
        }
        
        // 处理后端未启动或返回非JSON的情况
        if (!res.data || typeof res.data !== 'object') {
          console.warn('接口返回异常:', res)
          reject(new Error('服务暂不可用'))
          return
        }
        
        if (res.data.code === 200) {
          resolve(res.data.data)
        } else if (res.data.code === 401 || res.statusCode === 401) {
          uni.removeStorageSync('token')
          uni.reLaunch({ url: '/pages/login/index' })
          reject(res.data)
        } else {
          const msg = res.data.message || '请求失败'
          uni.showToast({ title: msg, icon: 'none' })
          reject(res.data)
        }
      },
      fail: (err) => {
        console.error('请求失败:', err)
        uni.showToast({ title: '网络连接失败', icon: 'none' })
        reject(err)
      }
    })
  })
}

export const get = (url, data) => request({ url, method: 'GET', data })
export const post = (url, data) => request({ url, method: 'POST', data })
export const put = (url, data) => request({ url, method: 'PUT', data })
