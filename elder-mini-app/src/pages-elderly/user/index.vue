<template>
  <view class="container">
    <view class="user-header" @click="goPage('/pages-elderly/user/profile')">
      <view class="avatar">
        <image v-if="userInfo.avatar" class="avatar-img" :src="userInfo.avatar" mode="aspectFill" />
        <text v-else class="avatar-emoji">👴</text>
      </view>
      <view class="user-info">
        <text class="user-name">{{ userInfo.name || '未设置姓名' }}</text>
        <text class="user-phone">{{ userInfo.phone || '未绑定手机' }}</text>
      </view>
    </view>

    <!-- 积分余额（仅显示，不可充值） -->
    <view class="points-card">
      <view class="points-info">
        <text class="points-label">积分余额</text>
        <text class="points-value">{{ userPoints }}</text>
      </view>
    </view>

    <view class="menu-list">
      <view class="menu-item" @click="goPage('/pages-elderly/user/health')">
        <text class="menu-icon">📋</text>
        <text class="menu-text">健康档案</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goPage('/pages-elderly/user/contacts')">
        <text class="menu-icon">👨‍👩‍👧</text>
        <text class="menu-text">紧急联系人</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goTabPage('/pages-elderly/order/list')">
        <text class="menu-icon">📝</text>
        <text class="menu-text">我的订单</text>
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
      <view class="tab-item" @click="goTabPage('/pages-elderly/index/index')">
        <text class="tab-icon">🏠</text>
        <text class="tab-text">首页</text>
      </view>
      <view class="tab-item" @click="goTabPage('/pages-elderly/order/list')">
        <text class="tab-icon">📋</text>
        <text class="tab-text">我的订单</text>
      </view>
      <view class="tab-item active">
        <text class="tab-icon">👤</text>
        <text class="tab-text">个人中心</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { get } from '@/utils/request'
import { onShow } from '@dcloudio/uni-app'

const userInfo = ref({})
const userPoints = ref(0)

onShow(async () => {
  try {
    userInfo.value = await get('/user/info')
    const pointsData = await get('/meal/points')
    userPoints.value = pointsData?.points || 0
  } catch (e) {
    console.error(e)
  }
})

const goPage = (url) => {
  uni.navigateTo({ url })
}

const goTabPage = (url) => {
  uni.reLaunch({ url })
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
  padding-bottom: 180rpx;
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
  margin-right: 40rpx;
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
}

.avatar-emoji {
  font-size: 80rpx;
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
  font-size: 38rpx;
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
  font-size: 38rpx;
  color: #ff4d4f;
}

.points-card {
  background: linear-gradient(135deg, #ff9800 0%, #ffb74d 100%);
  border-radius: 24rpx;
  padding: 40rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 30rpx;
}

.points-info {
  display: flex;
  flex-direction: column;
}

.points-label {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
}

.points-value {
  font-size: 56rpx;
  font-weight: bold;
  color: #ffffff;
  margin-top: 8rpx;
}

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

.tab-icon { font-size: 48rpx; }
.tab-text { font-size: 26rpx; color: #999999; margin-top: 8rpx; }
.tab-item.active .tab-text { color: #43a047; }
</style>
