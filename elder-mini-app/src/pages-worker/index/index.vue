<template>
  <view class="container">
    <!-- 服务人员信息 -->
    <view class="worker-info" v-if="workerInfo">
      <view class="worker-avatar">
        <image :src="workerInfo.avatar || '/static/default-avatar.png'" mode="aspectFill" />
      </view>
      <view class="worker-detail">
        <text class="worker-name">{{ workerInfo.name }}</text>
        <view class="worker-type-tag" :class="'type-' + workerInfo.workerType">
          {{ workerInfo.workerTypeName }}
        </view>
      </view>
    </view>

    <!-- 今日统计 -->
    <view class="stat-section">
      <view class="stat-item">
        <text class="stat-value">{{ stats.pending }}</text>
        <text class="stat-label">待接单</text>
      </view>
      <view class="stat-item">
        <text class="stat-value">{{ stats.today }}</text>
        <text class="stat-label">今日任务</text>
      </view>
      <view class="stat-item">
        <text class="stat-value">{{ stats.completed }}</text>
        <text class="stat-label">已完成</text>
      </view>
    </view>

    <!-- 快捷入口 -->
    <view class="quick-entry">
      <view class="entry-item" @click="goPage('/pages-worker/order/schedule')">
        <text class="entry-icon">📅</text>
        <text class="entry-text">我的日程</text>
      </view>
      <view class="entry-item" @click="goPage('/pages-worker/order/list')">
        <text class="entry-icon">📝</text>
        <text class="entry-text">我的工单</text>
      </view>
    </view>

    <!-- 待处理任务 -->
    <view class="section-title">待处理任务</view>
    <view class="task-list">
      <view class="task-card" v-for="task in pendingTasks" :key="task.id" @click="goTaskDetail(task)">
        <view class="task-header">
          <view class="task-title">
            <view class="order-type-tag" :class="'type-' + task.orderType">{{ getTypeText(task.orderType) }}</view>
            <text class="task-service">{{ task.serviceName }}</text>
          </view>
          <text class="task-status" :class="'status-' + task.status">{{ task.statusText }}</text>
        </view>
        <view class="task-info">
          <text class="task-item">👴 {{ task.elderlyName }}</text>
          <text class="task-item">📍 {{ task.address }}</text>
          <text class="task-item">🕐 {{ task.appointmentTime }}</text>
        </view>
        <view class="task-actions">
          <view class="action-btn" @click.stop="acceptTask(task)">接单</view>
        </view>
      </view>

      <view class="empty" v-if="pendingTasks.length === 0">
        <text>暂无待处理任务</text>
      </view>
    </view>

    <!-- 底部导航 -->
    <view class="tab-bar">
      <view class="tab-item active">
        <text class="tab-icon">🏠</text>
        <text class="tab-text">首页</text>
      </view>
      <view class="tab-item" @click="switchTab('user')">
        <text class="tab-icon">👤</text>
        <text class="tab-text">我的</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { get, post } from '@/utils/request'
import { onShow } from '@dcloudio/uni-app'

const stats = ref({ pending: 0, today: 0, completed: 0 })
const pendingTasks = ref([])
const workerInfo = ref(null)

let isNavigating = false

const typeTextMap = {
  meal: '餐饮配送',
  cleaning: '保洁服务',
  medical: '医疗巡诊'
}

const getTypeText = (type) => typeTextMap[type] || '其他'

onShow(async () => {
  try {
    // 获取服务人员信息
    workerInfo.value = await get('/worker/info') || null
    
    stats.value = await get('/worker/stats') || { pending: 0, today: 0, completed: 0 }
    // 获取可接单的任务（待接单大厅）
    const availableOrders = await get('/worker/available') || []
    pendingTasks.value = availableOrders.slice(0, 5)
  } catch (e) {
    console.error(e)
  }
})

const switchTab = (tab) => {
  if (tab === 'user') {
    uni.reLaunch({ url: '/pages-worker/user/index' })
  }
}

const goPage = (url) => {
  if (isNavigating) return
  isNavigating = true
  
  uni.showLoading({ title: '加载中...' })
  uni.navigateTo({ 
    url,
    complete: () => {
      uni.hideLoading()
      setTimeout(() => { isNavigating = false }, 300)
    },
    fail: (err) => {
      console.error('跳转失败:', err)
      uni.showToast({ title: '页面加载失败，请重试', icon: 'none' })
    }
  })
}

const goTaskDetail = (task) => {
  if (isNavigating) return
  isNavigating = true
  
  uni.showLoading({ title: '加载中...' })
  uni.navigateTo({
    url: `/pages-worker/task/detail?id=${task.id}&type=${task.orderType || 'cleaning'}`,
    complete: () => {
      uni.hideLoading()
      setTimeout(() => { isNavigating = false }, 300)
    },
    fail: (err) => {
      console.error('跳转失败:', err)
      uni.showToast({ title: '页面加载失败，请重试', icon: 'none' })
    }
  })
}

const goExecute = (task) => {
  if (isNavigating) return
  isNavigating = true
  
  uni.showLoading({ title: '加载中...' })
  uni.navigateTo({
    url: `/pages-worker/order/execute?id=${task.id}&type=${task.orderType || 'cleaning'}`,
    complete: () => {
      uni.hideLoading()
      setTimeout(() => { isNavigating = false }, 300)
    },
    fail: (err) => {
      console.error('跳转失败:', err)
      uni.showToast({ title: '页面加载失败，请重试', icon: 'none' })
    }
  })
}

const acceptTask = async (task) => {
  uni.showModal({
    title: '确认接单',
    content: '确定接受该任务？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await post('/worker/accept', {
            orderId: task.id,
            type: task.orderType || 'cleaning'
          })
          uni.showToast({ title: '接单成功', icon: 'success' })
          // 刷新数据
          stats.value = await get('/worker/stats') || { pending: 0, today: 0, completed: 0 }
          const availableOrders = await get('/worker/available') || []
          pendingTasks.value = availableOrders.slice(0, 5)
        } catch (e) {
          console.error(e)
        }
      }
    }
  })
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 30rpx;
  padding-bottom: 180rpx;
}

.worker-info {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx;
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
}

.worker-avatar {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 24rpx;
}

.worker-avatar image {
  width: 100%;
  height: 100%;
}

.worker-detail {
  flex: 1;
}

.worker-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 12rpx;
}

.worker-type-tag {
  display: inline-block;
  font-size: 24rpx;
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
}

.worker-type-tag.type-1 { background: #fff7e6; color: #fa8c16; }
.worker-type-tag.type-2 { background: #e6f7ff; color: #1890ff; }
.worker-type-tag.type-3 { background: #f6ffed; color: #52c41a; }

.stat-section {
  background: linear-gradient(135deg, #43a047 0%, #66bb6a 100%);
  border-radius: 24rpx;
  padding: 50rpx;
  display: flex;
  justify-content: space-around;
  margin-bottom: 30rpx;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 56rpx;
  font-weight: bold;
  color: #ffffff;
  display: block;
}

.stat-label {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 8rpx;
  display: block;
}

.quick-entry {
  display: flex;
  background: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
}

.entry-item {
  flex: 1;
  text-align: center;
}

.entry-icon {
  font-size: 56rpx;
  display: block;
  margin-bottom: 12rpx;
}

.entry-text {
  font-size: 28rpx;
  color: #666666;
}

.section-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333333;
  margin: 30rpx 0 20rpx;
}

.task-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 40rpx;
  margin-bottom: 30rpx;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.task-title { display: flex; align-items: center; gap: 16rpx; }

.order-type-tag { font-size: 24rpx; padding: 8rpx 16rpx; border-radius: 8rpx; }
.type-meal { background: #fff7e6; color: #fa8c16; }
.type-cleaning { background: #e6f7ff; color: #1890ff; }
.type-medical { background: #f6ffed; color: #52c41a; }

.task-service {
  font-size: 38rpx;
  font-weight: bold;
  color: #333333;
}

.task-status {
  font-size: 28rpx;
  padding: 8rpx 20rpx;
  border-radius: 8rpx;
}

.task-status.status-0 {
  background: #fff7e6;
  color: #fa8c16;
}

.task-status.status-1 {
  background: #e6f7ff;
  color: #1890ff;
}

.task-item {
  font-size: 30rpx;
  color: #666666;
  display: block;
  margin-bottom: 12rpx;
}

.task-actions {
  margin-top: 30rpx;
  padding-top: 30rpx;
  border-top: 1rpx solid #f0f0f0;
}

.action-btn {
  background: #43a047;
  color: #ffffff;
  text-align: center;
  padding: 24rpx;
  border-radius: 12rpx;
  font-size: 34rpx;
  font-weight: bold;
}

.action-btn.green {
  background: #52c41a;
}

.empty {
  text-align: center;
  padding: 100rpx 0;
  color: #999999;
  font-size: 32rpx;
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
