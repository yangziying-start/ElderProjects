<template>
  <view class="container">
    <view class="user-header" @click="goPage('/pages-worker/user/profile')">
      <view class="avatar">👷</view>
      <view class="user-info">
        <text class="user-name">{{ userInfo.name || '未设置姓名' }}</text>
        <text class="user-phone">{{ userInfo.phone || '未绑定手机' }}</text>
        <text class="user-id">ID: {{ userInfo.id }}</text>
      </view>
      <text class="edit-arrow">›</text>
    </view>

    <view class="stat-section">
      <view class="stat-item">
        <text class="stat-value">{{ stats.completed || 0 }}</text>
        <text class="stat-label">已完成</text>
      </view>
      <view class="stat-item">
        <text class="stat-value">{{ stats.today || 0 }}</text>
        <text class="stat-label">今日任务</text>
      </view>
      <view class="stat-item">
        <text class="stat-value">{{ stats.pending || 0 }}</text>
        <text class="stat-label">待接单</text>
      </view>
    </view>

    <view class="menu-list">
      <view class="menu-item" @click="goPage('/pages-worker/order/schedule')">
        <text class="menu-icon">📅</text>
        <text class="menu-text">我的日程</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goPage('/pages-worker/order/list')">
        <text class="menu-icon">📋</text>
        <text class="menu-text">我的工单</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goPage('/pages/password/index')">
        <text class="menu-icon">🔐</text>
        <text class="menu-text">修改密码</text>
        <text class="menu-arrow">›</text>
      </view>
    </view>

    <view class="logout-btn" @click="logout">退出登录</view>

    <!-- 底部导航 -->
    <view class="tab-bar">
      <view class="tab-item" @click="switchTab('home')">
        <text class="tab-icon">🏠</text>
        <text class="tab-text">首页</text>
      </view>
      <view class="tab-item active">
        <text class="tab-icon">👤</text>
        <text class="tab-text">我的</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { get } from '@/utils/request'
import { onShow } from '@dcloudio/uni-app'

const userInfo = ref({})
const stats = ref({ completed: 0, today: 0, pending: 0 })

onShow(async () => {
  try {
    userInfo.value = await get('/user/info') || {}
    stats.value = await get('/worker/stats') || { completed: 0, today: 0, pending: 0 }
  } catch (e) {
    console.error(e)
  }
})

const goPage = (url) => { uni.navigateTo({ url }) }

const switchTab = (tab) => {
  if (tab === 'home') {
    uni.reLaunch({ url: '/pages-worker/index/index' })
  }
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
.container { padding: 30rpx; background: #f5f5f5; min-height: 100vh; padding-bottom: 180rpx; }
.user-header { background: linear-gradient(135deg, #43a047 0%, #66bb6a 100%); border-radius: 24rpx; padding: 60rpx 40rpx; display: flex; align-items: center; margin-bottom: 30rpx; }
.avatar { width: 140rpx; height: 140rpx; background: #fff; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 80rpx; margin-right: 40rpx; }
.user-info { flex: 1; }
.user-name { font-size: 44rpx; font-weight: bold; color: #fff; display: block; }
.user-phone { font-size: 32rpx; color: rgba(255,255,255,0.8); margin-top: 12rpx; display: block; }
.user-id { font-size: 24rpx; color: rgba(255,255,255,0.6); margin-top: 8rpx; display: block; }
.edit-arrow { font-size: 48rpx; color: rgba(255,255,255,0.8); }
.stat-section { background: #fff; border-radius: 24rpx; padding: 40rpx; display: flex; justify-content: space-around; margin-bottom: 30rpx; }
.stat-item { text-align: center; }
.stat-value { font-size: 48rpx; font-weight: bold; color: #43a047; display: block; }
.stat-label { font-size: 28rpx; color: #999; margin-top: 8rpx; display: block; }
.menu-list { background: #fff; border-radius: 24rpx; margin-bottom: 30rpx; }
.menu-item { display: flex; align-items: center; padding: 50rpx 40rpx; border-bottom: 1rpx solid #f0f0f0; }
.menu-item:last-child { border-bottom: none; }
.menu-icon { font-size: 48rpx; margin-right: 30rpx; }
.menu-text { flex: 1; font-size: 36rpx; color: #333; }
.menu-arrow { font-size: 36rpx; color: #ccc; }
.logout-btn { background: #fff; border-radius: 24rpx; padding: 40rpx; text-align: center; font-size: 36rpx; color: #ff4d4f; }

.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 140rpx;
  background: #ffffff;
  display: flex;
  box-shadow: 0 -4rpx 16rpx rgba(0, 0, 0, 0.05);
  padding-bottom: env(safe-area-inset-bottom);
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.tab-icon {
  font-size: 48rpx;
}

.tab-text {
  font-size: 26rpx;
  color: #999999;
  margin-top: 8rpx;
}

.tab-item.active .tab-text {
  color: #43a047;
}
</style>
