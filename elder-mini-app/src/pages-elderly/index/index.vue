<template>
  <view class="container">
    <!-- 一键呼叫 - 适老化大按钮 -->
    <view class="emergency-section">
      <view 
        class="emergency-btn" 
        @longpress="handleEmergencyCall"
        @touchstart="startPress"
        @touchend="endPress"
      >
        <text class="emergency-text">一键呼叫</text>
        <text class="emergency-tip">长按3秒呼叫</text>
      </view>
      <text class="emergency-hint">紧急情况请长按上方按钮</text>
    </view>

    <!-- 快捷服务 - 大图标大文字 -->
    <view class="section-title">常用服务</view>
    <view class="service-grid">
      <view class="service-item" @click="goService('meal')">
        <text class="service-icon">🍱</text>
        <text class="service-name">社区餐饮</text>
      </view>
      <view class="service-item" @click="goService('cleaning')">
        <text class="service-icon">🧹</text>
        <text class="service-name">居家保洁</text>
      </view>
      <view class="service-item large" @click="goService('medical')">
        <text class="service-icon">🏥</text>
        <text class="service-name">医疗巡诊</text>
      </view>
    </view>

    <!-- 底部导航 -->
    <view class="tab-bar">
      <view class="tab-item active">
        <text class="tab-icon">🏠</text>
        <text class="tab-text">首页</text>
      </view>
      <view class="tab-item" @click="goPage('/pages-elderly/order/list')">
        <text class="tab-icon">📋</text>
        <text class="tab-text">我的订单</text>
      </view>
      <view class="tab-item" @click="goPage('/pages-elderly/user/index')">
        <text class="tab-icon">👤</text>
        <text class="tab-text">个人中心</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { post } from '@/utils/request'

let pressTimer = null

const startPress = () => {
  pressTimer = setTimeout(() => {
    uni.vibrateShort()
  }, 2000)
}

const endPress = () => {
  if (pressTimer) {
    clearTimeout(pressTimer)
    pressTimer = null
  }
}

const handleEmergencyCall = () => {
  uni.showActionSheet({
    itemList: ['紧急医疗', '摔倒求助', '一般求助'],
    itemColor: '#ff4d4f',
    success: async (res) => {
      const eventType = res.tapIndex + 1
      try {
        await post('/emergency/call', { eventType })
        uni.showModal({
          title: '呼叫已发送',
          content: '管理人员已收到您的呼叫，请保持电话畅通，稍后会有人联系您',
          showCancel: false
        })
      } catch (e) {
        console.error(e)
        uni.showToast({ title: '呼叫失败，请重试', icon: 'none' })
      }
    }
  })
}

const goService = (type) => {
  const pageMap = {
    meal: '/pages-elderly/service/meal',        // 社区餐饮
    cleaning: '/pages-elderly/service/cleaning', // 居家保洁
    medical: '/pages-elderly/service/medical'    // 医疗巡诊
  }
  uni.navigateTo({
    url: pageMap[type]
  })
}

const goPage = (url) => {
  uni.navigateTo({ url })
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 30rpx;
  padding-bottom: 180rpx;
}

.emergency-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 0;
}

.emergency-btn {
  width: 360rpx;
  height: 360rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff4d4f, #ff7875);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 16rpx 48rpx rgba(255, 77, 79, 0.4);
}

.emergency-text {
  font-size: 56rpx;
  font-weight: bold;
  color: #ffffff;
}

.emergency-tip {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 16rpx;
}

.emergency-hint {
  font-size: 28rpx;
  color: #999999;
  margin-top: 30rpx;
}

.section-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333333;
  margin: 40rpx 0 30rpx;
}

.service-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 30rpx;
}

.service-item {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 50rpx 30rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
}

.service-item.large {
  grid-column: span 2;
}

.service-icon {
  font-size: 80rpx;
  margin-bottom: 20rpx;
}

.service-name {
  font-size: 36rpx;
  font-weight: 500;
  color: #333333;
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
