<template>
  <view class="container">
    <view class="user-header" @click="goPage('/pages-child/user/profile')">
      <view class="avatar">👨‍👩‍👧</view>
      <view class="user-info">
        <text class="user-name">{{ userInfo.name || '未设置姓名' }}</text>
        <text class="user-phone">{{ userInfo.phone || '未绑定手机' }}</text>
      </view>
      <text class="edit-arrow">›</text>
    </view>

    <view class="menu-list">
      <view class="menu-item" @click="goPage('/pages-child/emergency/index')">
        <text class="menu-icon">🚨</text>
        <text class="menu-text">应急记录</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goPage('/pages-child/booking/orders')">
        <text class="menu-icon">📝</text>
        <text class="menu-text">代办记录</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goPage('/pages-child/user/bindList')">
        <text class="menu-icon">👴</text>
        <text class="menu-text">绑定老人</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goPage('/pages/password/index')">
        <text class="menu-icon">🔐</text>
        <text class="menu-text">修改密码</text>
        <text class="menu-arrow">›</text>
      </view>
    </view>

    <view class="logout-btn" @click="logout">退出登录</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { get } from '@/utils/request'
import { onShow } from '@dcloudio/uni-app'

const userInfo = ref({})

onShow(async () => {
  try {
    userInfo.value = await get('/user/info') || {}
  } catch (e) {
    console.error(e)
  }
})

const goPage = (url) => {
  uni.navigateTo({ url })
}

const logout = () => {
  uni.showModal({
    title: '确认退出',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        uni.clearStorageSync()
        uni.reLaunch({ url: '/pages/login/index' })
      }
    }
  })
}
</script>

<style scoped>
.container {
  padding: 30rpx;
  background: #f5f5f5;
  min-height: 100vh;
}

.user-header {
  background: linear-gradient(135deg, #43a047 0%, #66bb6a 100%);
  border-radius: 24rpx;
  padding: 60rpx 40rpx;
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
}

.avatar {
  width: 140rpx;
  height: 140rpx;
  background: #ffffff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 80rpx;
  margin-right: 40rpx;
}

.user-name {
  font-size: 44rpx;
  font-weight: bold;
  color: #ffffff;
  display: block;
}

.user-phone {
  font-size: 32rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 12rpx;
  display: block;
}

.edit-arrow {
  font-size: 48rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-left: auto;
}

.menu-list {
  background: #ffffff;
  border-radius: 24rpx;
  margin-bottom: 30rpx;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 50rpx 40rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-icon {
  font-size: 48rpx;
  margin-right: 30rpx;
}

.menu-text {
  flex: 1;
  font-size: 36rpx;
  color: #333333;
}

.menu-arrow {
  font-size: 36rpx;
  color: #cccccc;
}

.logout-btn {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 40rpx;
  text-align: center;
  font-size: 36rpx;
  color: #ff4d4f;
}
</style>
