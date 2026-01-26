<template>
  <view class="container">
    <view class="form-card">
      <view class="form-item">
        <text class="label">原密码</text>
        <input 
          class="input" 
          type="password" 
          v-model="oldPassword" 
          placeholder="请输入原密码"
          maxlength="20"
        />
      </view>
      
      <view class="form-item">
        <text class="label">新密码</text>
        <input 
          class="input" 
          type="password" 
          v-model="newPassword" 
          placeholder="请输入新密码（至少6位）"
          maxlength="20"
        />
      </view>
      
      <view class="form-item">
        <text class="label">确认新密码</text>
        <input 
          class="input" 
          type="password" 
          v-model="confirmPassword" 
          placeholder="请再次输入新密码"
          maxlength="20"
        />
      </view>
      
      <view class="submit-btn" @click="handleSubmit">确认修改</view>
      
      <view class="tips">
        <text class="tip-title">温馨提示：</text>
        <text class="tip-text">• 密码长度至少6位</text>
        <text class="tip-text">• 如忘记密码，请联系管理员重置</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { get, post } from '@/utils/request'

const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const userPhone = ref('')

onMounted(async () => {
  try {
    const userInfo = await get('/user/info')
    userPhone.value = userInfo.phone
  } catch (e) {
    console.error(e)
  }
})

const handleSubmit = async () => {
  if (!oldPassword.value) {
    uni.showToast({ title: '请输入原密码', icon: 'none' })
    return
  }
  
  if (!newPassword.value || newPassword.value.length < 6) {
    uni.showToast({ title: '新密码至少6位', icon: 'none' })
    return
  }
  
  if (newPassword.value !== confirmPassword.value) {
    uni.showToast({ title: '两次密码不一致', icon: 'none' })
    return
  }
  
  if (oldPassword.value === newPassword.value) {
    uni.showToast({ title: '新密码不能与原密码相同', icon: 'none' })
    return
  }
  
  try {
    uni.showLoading({ title: '提交中...' })
    await post('/user/password', {
      oldPassword: oldPassword.value,
      newPassword: newPassword.value
    })
    uni.hideLoading()
    
    // 标记该用户已修改过密码
    if (userPhone.value) {
      uni.setStorageSync('passwordChanged_' + userPhone.value, true)
    }
    
    uni.showToast({ title: '修改成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch (e) {
    uni.hideLoading()
    console.error(e)
  }
}
</script>

<style scoped>
.container {
  padding: 30rpx;
  background: #f5f5f5;
  min-height: 100vh;
}

.form-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 40rpx;
}

.form-item {
  margin-bottom: 30rpx;
}

.label {
  font-size: 32rpx;
  color: #333333;
  margin-bottom: 16rpx;
  display: block;
}

.input {
  width: 100%;
  height: 100rpx;
  background: #f5f5f5;
  border-radius: 16rpx;
  padding: 0 30rpx;
  font-size: 34rpx;
  box-sizing: border-box;
}

.submit-btn {
  width: 100%;
  height: 100rpx;
  background: #43a047;
  color: #ffffff;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  font-weight: bold;
  margin-top: 40rpx;
}

.tips {
  margin-top: 40rpx;
  padding: 30rpx;
  background: #fff8e1;
  border-radius: 16rpx;
}

.tip-title {
  font-size: 28rpx;
  color: #f57c00;
  font-weight: bold;
  display: block;
  margin-bottom: 16rpx;
}

.tip-text {
  font-size: 26rpx;
  color: #666666;
  display: block;
  line-height: 1.8;
}
</style>
