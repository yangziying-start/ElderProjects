<template>
  <view class="login-container">
    <view class="logo">
      <text class="title">适老化社区服务</text>
    </view>
    
    <!-- 身份选择 -->
    <view class="role-select" v-if="!selectedRole">
      <text class="tip">请选择您的身份</text>
      
      <view class="role-card" @click="selectRole(1)">
        <text class="role-icon">👴</text>
        <view class="role-info">
          <text class="role-name">老人端</text>
          <text class="role-desc">一键呼叫、服务预约</text>
        </view>
      </view>
      
      <view class="role-card" @click="selectRole(2)">
        <text class="role-icon">👨‍👩‍👧</text>
        <view class="role-info">
          <text class="role-name">子女端</text>
          <text class="role-desc">应急响应、远程代办</text>
        </view>
      </view>
      
      <view class="role-card" @click="selectRole(3)">
        <text class="role-icon">👷</text>
        <view class="role-info">
          <text class="role-name">服务人员端</text>
          <text class="role-desc">任务接单、服务执行</text>
        </view>
      </view>
    </view>
    
    <!-- 登录表单 -->
    <!-- <view class="login-form" v-else>
      <view class="form-header">
        <text class="back-btn" @click="selectedRole = null">← 返回</text>
        <text class="form-title">{{ roleNames[selectedRole] }}登录</text>
      </view>
      
      <view class="form-item">
        <text class="label">手机号</text>
        <input 
          class="input" 
          type="number" 
          v-model="phone" 
          placeholder="请输入手机号"
          maxlength="11"
        />
      </view>
      
      <view class="form-item">
        <text class="label">密码</text>
        <input 
          class="input" 
          type="password" 
          v-model="password" 
          placeholder="请输入密码"
          maxlength="20"
        />
      </view>
      
      <view class="login-btn" @click="doLogin">登录</view>
      
      <view class="password-tip">
        <text>初始密码为身份证后6位</text>
      </view>
    </view>-->
    <view class="login-form" v-else>
      <view class="form-header">
        <text class="back-btn" @click="selectedRole = null">← 返回</text>
        <text class="form-title">{{ roleNames[selectedRole] }}登录</text>
      </view>
      
      <view class="form-item">
        <text class="label">手机号</text>
        <input 
          class="input" 
          type="number" 
          v-model="phone" 
          placeholder="请输入11位手机号"
          maxlength="11"
          @blur="validatePhone"
          @input="phoneError = ''"
        />
        <text v-if="phoneError" class="error-text">{{ phoneError }}</text>
      </view>
      
      <view class="form-item">
        <text class="label">密码</text>
        <view class="password-box">
          <input 
            class="input password-input" 
            type="text"
            :password="!showPassword"
            v-model="password" 
            placeholder="请输入密码"
            maxlength="20"
          />
          <view class="eye-icon" @click.stop="togglePassword">
            <text style="font-size: 36rpx;">{{ showPassword ? '👀' : '🔒' }}</text>
          </view>
        </view>
        <text class="tip-text" v-if="selectedRole === 1">老人模式默认显示密码，防止输错</text>
      </view>
      
      <view class="login-btn" @click="doLogin">登录</view>
      
      <view class="password-tip">
        <text>初始密码为身份证后6位</text>
      </view>
    </view>
  </view>
  
</template>

<script setup>
import { ref, watch } from 'vue'
import { post } from '@/utils/request'

const selectedRole = ref(null)
const phone = ref('')
const password = ref('')
const showPassword = ref(false) // 控制密码显示状态
const phoneError = ref('') // 手机号错误提示

const roleNames = {
  1: '老人端',
  2: '子女端',
  3: '服务人员端'
}

// 监听角色选择，自动调整密码显示策略
watch(selectedRole, (newRole) => {
  if (newRole === 1) {
    showPassword.value = true; // 老人端：默认显示
  } else {
    showPassword.value = false; // 其他端：默认隐藏
  }
  phone.value = ''
  password.value = ''
  phoneError.value = ''
})

const selectRole = (roleType) => {
  selectedRole.value = roleType
  // phone.value = ''
  // password.value = ''
}

const togglePassword = () => {
  showPassword.value = !showPassword.value
}

// 手机号格式验证
const validatePhone = () => {
  if (!phone.value) {
    phoneError.value = ''
    return false
  }
  if (!/^1[3-9]\d{9}$/.test(phone.value)) {
    phoneError.value = '手机号格式不正确，请检查'
    return false
  }
  phoneError.value = ''
  return true
}

const doLogin = async () => {
  // if (!phone.value || phone.value.length !== 11) {
  //   uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
  //   return
  // }
  
  // if (!password.value || password.value.length < 6) {
  //   uni.showToast({ title: '密码至少6位', icon: 'none' })
  //   return
  // }

  // 1. 登录前的强制校验
  if (!phone.value) {
    uni.showToast({ title: '请输入手机号', icon: 'none' })
    return
  }
 
  // 再次触发格式校验
  if (!/^1[3-9]\d{9}$/.test(phone.value)) {
    uni.showToast({ title: '手机号格式错误', icon: 'none' })
    phoneError.value = '手机号格式不正确'
    return
  }
  
  if (!password.value || password.value.length < 6) {
    uni.showToast({ title: '密码至少6位', icon: 'none' })
    return
  }
  
  try {
    uni.showLoading({ title: '登录中...' })
    
    const token = await post('/user/login', { 
      phone: phone.value, 
      password: password.value, 
      userType: selectedRole.value 
    })
    
    uni.setStorageSync('token', token)
    uni.setStorageSync('userType', selectedRole.value)
    
    const pages = {
      1: '/pages-elderly/index/index',
      2: '/pages-child/index/index',
      3: '/pages-worker/index/index'
    }
    
    uni.hideLoading()

    // 检查是否首次登录（使用默认密码 - 身份证后6位通常是6位数字）
    const hasChangedPassword = uni.getStorageSync('passwordChanged_' + phone.value)
    if (!hasChangedPassword && password.value.length === 6 && /^\d{6}$/.test(password.value)) {
      uni.showModal({
        title: '安全提示',
        content: '您可能正在使用初始密码（身份证后6位），建议修改密码以保障账号安全',
        confirmText: '去修改',
        cancelText: '稍后再说',
        success: (res) => {
          if (res.confirm) {
            uni.reLaunch({ url: '/pages/password/index' })
          } else {
            uni.reLaunch({ url: pages[selectedRole.value] })
          }
        }
      })
    } else {
      uni.reLaunch({ url: pages[selectedRole.value] })
    }
  } catch (e) {
    uni.hideLoading()
    // console.error('登录失败:', e)
    uni.showToast({ title: e.message || '登录失败', icon: 'none' })
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #e8f5e9 0%, #fff8e1 100%);
  padding: 60rpx 40rpx;
}

.logo {
  text-align: center;
  padding: 80rpx 0;
}

.title {
  font-size: 56rpx;
  font-weight: bold;
  color: #2e7d32;
}

.role-select, .login-form {
  background: #ffffff;
  border-radius: 30rpx;
  padding: 50rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.08);
}

.tip {
  display: block;
  text-align: center;
  font-size: 32rpx;
  color: #666666;
  margin-bottom: 40rpx;
}

.role-card {
  background: #fafafa;
  border-radius: 20rpx;
  padding: 40rpx;
  margin-bottom: 30rpx;
  display: flex;
  align-items: center;
  border: 2rpx solid #e8f5e9;
}

.role-card:active {
  background: #e8f5e9;
  border-color: #4caf50;
}

.role-icon {
  font-size: 60rpx;
  margin-right: 30rpx;
}

.role-info {
  flex: 1;
}

.role-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #333333;
  display: block;
}

.role-desc {
  font-size: 26rpx;
  color: #999999;
  margin-top: 8rpx;
  display: block;
}

.form-header {
  display: flex;
  align-items: center;
  margin-bottom: 50rpx;
}

.back-btn {
  font-size: 32rpx;
  color: #43a047;
}

.form-title {
  flex: 1;
  text-align: center;
  font-size: 40rpx;
  font-weight: bold;
  color: #333333;
  margin-right: 60rpx;
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
  font-size: 36rpx;
  box-sizing: border-box;
}

.login-btn {
  width: 100%;
  height: 100rpx;
  background: #43a047;
  color: #ffffff;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  font-weight: bold;
  margin-top: 40rpx;
}

.password-tip {
  text-align: center;
  margin-top: 30rpx;
  padding: 20rpx;
  background: #fff3e0;
  border-radius: 12rpx;
}

.password-tip text {
  font-size: 26rpx;
  color: #e65100;
}

.password-box {
  position: relative;
  width: 100%;
}
.password-input {
  padding-right: 80rpx; /* 给眼睛图标留位置 */
}
.eye-icon {
  position: absolute;
  right: 20rpx;
  top: 50%;
  transform: translateY(-50%);
  padding: 10rpx;
  z-index: 10;
}
.error-text {
  font-size: 24rpx;
  color: #ff4d4f;
  margin-top: 10rpx;
  display: block;
}
.tip-text {
  font-size: 24rpx;
  color: #999;
  margin-top: 10rpx;
  display: block;
}
</style>
