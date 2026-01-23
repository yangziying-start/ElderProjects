<template>
  <view class="container">
    <!-- 选择老人 -->
    <view class="section-title">选择老人</view>
    <view class="elderly-list">
      <view 
        class="elderly-card" 
        :class="{ active: selectedElderly?.id === elderly.id }"
        v-for="elderly in elderlyList" 
        :key="elderly.id"
        @click="selectElderly(elderly)"
      >
        <view class="elderly-avatar">👴</view>
        <view class="elderly-info">
          <text class="elderly-name">{{ elderly.name }}</text>
          <text class="elderly-address">{{ elderly.address }}</text>
        </view>
        <view class="check-icon" v-if="selectedElderly?.id === elderly.id">✓</view>
      </view>
      <view class="add-elderly" @click="goBindElderly" v-if="elderlyList.length === 0">
        <text class="add-icon">+</text>
        <text class="add-text">绑定老人</text>
      </view>
    </view>

    <!-- 选择服务 -->
    <view class="section-title">选择服务</view>
    <view class="service-grid">
      <view class="service-item" @click="goBookService(1)">
        <text class="service-icon">🧹</text>
        <text class="service-name">生活保洁</text>
        <text class="service-desc">日常清洁服务</text>
      </view>
      <view class="service-item" @click="goBookService(2)">
        <text class="service-icon">🍱</text>
        <text class="service-name">餐饮配送</text>
        <text class="service-desc">营养餐配送</text>
      </view>
      <view class="service-item" @click="goBookService(3)">
        <text class="service-icon">🏥</text>
        <text class="service-name">社区医疗</text>
        <text class="service-desc">医疗巡诊服务</text>
      </view>
      <view class="service-item" @click="goHealthRecord">
        <text class="service-icon">💊</text>
        <text class="service-name">健康档案</text>
        <text class="service-desc">查看健康记录</text>
      </view>
    </view>

    <!-- 老人日程 -->
    <view class="section-title">
      <text>老人日程</text>
      <text class="view-all" @click="goSchedule" v-if="scheduleList.length > 0">查看全部</text>
    </view>
    <view class="schedule-list" v-if="scheduleList.length > 0">
      <view class="schedule-item" v-for="item in scheduleList" :key="item.id">
        <view class="schedule-date">
          <text class="date-day">{{ item.day }}</text>
          <text class="date-month">{{ item.month }}</text>
        </view>
        <view class="schedule-info">
          <text class="schedule-service">{{ item.serviceName }}</text>
          <text class="schedule-time">{{ item.time }}</text>
        </view>
        <text class="schedule-status" :class="'status-' + item.status">{{ item.statusText }}</text>
      </view>
    </view>
    <view class="empty-schedule" v-else>
      <text>暂无预约日程</text>
    </view>

    <!-- 代办订单 -->
    <view class="section-title">
      <text>代办订单</text>
      <text class="view-all" @click="goOrderList">查看全部</text>
    </view>
    <view class="order-list">
      <view class="order-card" v-for="order in orderList" :key="order.id" @click="viewOrderDetail(order)">
        <view class="order-header">
          <text class="order-elderly">{{ order.elderlyName }}</text>
          <text class="order-status" :class="'status-' + order.status">{{ order.statusText }}</text>
        </view>
        <text class="order-service">{{ order.serviceName }}</text>
        <view class="order-footer">
          <text class="order-time">{{ order.appointmentTime }}</text>
          <view class="order-actions" v-if="order.status === 0">
            <text class="cancel-btn" @click.stop="cancelOrder(order)">取消</text>
          </view>
        </view>
      </view>
      <view class="empty" v-if="orderList.length === 0">
        <text class="empty-icon">📝</text>
        <text class="empty-text">暂无代办订单</text>
        <text class="empty-tip">选择老人和服务开始预约</text>
      </view>
    </view>

    <!-- 底部导航 -->
    <view class="tab-bar">
      <view class="tab-item active">
        <text class="tab-icon">🏠</text>
        <text class="tab-text">首页</text>
      </view>
      <view class="tab-item" @click="goTabPage('/pages-child/user/index')">
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

const elderlyList = ref([])
const selectedElderly = ref(null)
const orderList = ref([])
const scheduleList = ref([])

let isNavigating = false

onShow(async () => {
  try {
    elderlyList.value = await get('/user/bindElderly') || []
    const allOrders = await get('/order/proxy') || []
    orderList.value = allOrders.slice(0, 5)
  } catch (e) {
    console.error(e)
  }
  
  if (elderlyList.value.length > 0 && !selectedElderly.value) {
    selectedElderly.value = elderlyList.value[0]
  }
  
  // 加载老人日程
  if (selectedElderly.value) {
    loadSchedule()
  }
})

const loadSchedule = async () => {
  if (!selectedElderly.value) return
  try {
    const data = await get('/order/schedule', { elderlyId: selectedElderly.value.id })
    scheduleList.value = (data || []).slice(0, 3)
  } catch (e) {
    console.error(e)
  }
}

const selectElderly = (elderly) => {
  selectedElderly.value = elderly
  uni.setStorageSync('selectedElderly', elderly)
  loadSchedule()
}

const goBookService = (categoryId) => {
  if (!selectedElderly.value) {
    uni.showToast({ title: '请先选择老人', icon: 'none' })
    return
  }
  if (isNavigating) return
  isNavigating = true
  
  uni.showLoading({ title: '加载中...' })
  
  const elderlyId = selectedElderly.value.id
  let url = ''
  
  switch (categoryId) {
    case 1:
      url = `/pages-elderly/service/cleaning?elderlyId=${elderlyId}&proxy=1`
      break
    case 2:
      url = `/pages-elderly/service/meal?elderlyId=${elderlyId}&proxy=1`
      break
    case 3:
      url = `/pages-elderly/service/medical?elderlyId=${elderlyId}&proxy=1`
      break
  }
  
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

const goHealthRecord = () => {
  if (!selectedElderly.value) {
    uni.showToast({ title: '请先选择老人', icon: 'none' })
    return
  }
  if (isNavigating) return
  isNavigating = true
  
  uni.showLoading({ title: '加载中...' })
  uni.navigateTo({ 
    url: `/pages-child/care/health?elderlyId=${selectedElderly.value.id}`,
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

const goSchedule = () => {
  if (!selectedElderly.value) {
    uni.showToast({ title: '请先选择老人', icon: 'none' })
    return
  }
  if (isNavigating) return
  isNavigating = true
  
  uni.showLoading({ title: '加载中...' })
  uni.navigateTo({ 
    url: `/pages-child/care/schedule?elderlyId=${selectedElderly.value.id}`,
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

const goBindElderly = () => {
  uni.showModal({
    title: '绑定老人',
    editable: true,
    placeholderText: '请输入老人手机号',
    success: async (res) => {
      if (res.confirm && res.content) {
        try {
          await post('/user/bindElderly', { phone: res.content })
          uni.showToast({ title: '绑定成功', icon: 'success' })
          elderlyList.value = await get('/user/bindElderly') || []
          if (elderlyList.value.length > 0) {
            selectedElderly.value = elderlyList.value[0]
          }
        } catch (e) {
          console.error(e)
        }
      }
    }
  })
}

const goOrderList = () => {
  if (isNavigating) return
  isNavigating = true
  
  uni.showLoading({ title: '加载中...' })
  uni.navigateTo({ 
    url: '/pages-child/booking/orders',
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

const goTabPage = (url) => {
  uni.reLaunch({ url })
}

const viewOrderDetail = (order) => {
  uni.showModal({
    title: order.serviceName,
    content: `老人：${order.elderlyName}\n预约时间：${order.appointmentTime}\n状态：${order.statusText}\n地址：${order.address || '未填写'}`,
    showCancel: false
  })
}

const cancelOrder = async (order) => {
  uni.showModal({
    title: '确认取消',
    content: '确定要取消这个预约吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          // 根据订单类型调用不同的取消接口
          const orderType = order.orderType || 'order'
          let cancelUrl = ''
          switch (orderType) {
            case 'cleaning':
              cancelUrl = `/cleaning/order/${order.id}/cancel`
              break
            case 'meal':
              cancelUrl = `/meal/order/${order.id}/cancel`
              break
            case 'medical':
              cancelUrl = `/medical/appointment/${order.id}/cancel`
              break
            default:
              cancelUrl = `/order/${order.id}/cancel`
          }
          const result = await post(cancelUrl)
          // 检查取消是否成功
          if (result === false) {
            uni.showToast({ title: '取消失败，订单可能已超时', icon: 'none' })
            return
          }
          uni.showToast({ title: '取消成功', icon: 'success' })
          // 刷新订单列表
          const allOrders = await get('/order/proxy') || []
          orderList.value = allOrders.slice(0, 5)
          // 同时刷新日程
          if (selectedElderly.value) {
            await loadSchedule()
          }
        } catch (e) {
          uni.showToast({ title: e.message || '取消失败', icon: 'none' })
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

.section-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333333;
  margin: 30rpx 0 20rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title:first-child {
  margin-top: 0;
}

.view-all {
  font-size: 28rpx;
  font-weight: normal;
  color: #43a047;
}

.elderly-card {
  background: #ffffff;
  border-radius: 20rpx;
  padding: 30rpx;
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
  border: 4rpx solid transparent;
}

.elderly-card.active {
  border-color: #43a047;
  background: #e8f5e9;
}

.elderly-avatar {
  width: 80rpx;
  height: 80rpx;
  background: #f0f0f0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
  margin-right: 24rpx;
}

.elderly-info {
  flex: 1;
}

.elderly-name {
  font-size: 34rpx;
  font-weight: bold;
  color: #333333;
  display: block;
}

.elderly-address {
  font-size: 28rpx;
  color: #999999;
  margin-top: 8rpx;
  display: block;
}

.check-icon {
  width: 50rpx;
  height: 50rpx;
  background: #43a047;
  color: #ffffff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
}

.add-elderly {
  background: #ffffff;
  border-radius: 20rpx;
  padding: 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  border: 2rpx dashed #cccccc;
}

.add-icon {
  font-size: 60rpx;
  color: #43a047;
}

.add-text {
  font-size: 30rpx;
  color: #666666;
  margin-top: 12rpx;
}

.service-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

.service-item {
  background: #ffffff;
  border-radius: 20rpx;
  padding: 30rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.service-icon {
  font-size: 60rpx;
  margin-bottom: 16rpx;
}

.service-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
}

.service-desc {
  font-size: 24rpx;
  color: #999999;
  margin-top: 8rpx;
}

.order-card {
  background: #ffffff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.order-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.order-elderly {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
}

.order-status {
  font-size: 26rpx;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
}

.status-0 { background: #e8f5e9; color: #43a047; }
.status-1 { background: #fff7e6; color: #fa8c16; }
.status-2 { background: #e6f7ff; color: #1890ff; }
.status-3 { background: #fff7e6; color: #fa8c16; }
.status-4 { background: #f6ffed; color: #52c41a; }
.status-5 { background: #f0f0f0; color: #999999; }

.order-service {
  font-size: 30rpx;
  color: #333333;
  display: block;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16rpx;
}

.order-time {
  font-size: 28rpx;
  color: #999999;
}

.cancel-btn {
  font-size: 26rpx;
  color: #ff4d4f;
  padding: 8rpx 20rpx;
  border: 1rpx solid #ff4d4f;
  border-radius: 8rpx;
}

.empty {
  text-align: center;
  padding: 80rpx 0;
  background: #ffffff;
  border-radius: 20rpx;
}

.empty-icon {
  font-size: 80rpx;
  display: block;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 32rpx;
  color: #333333;
  display: block;
}

.empty-tip {
  font-size: 26rpx;
  color: #999999;
  display: block;
  margin-top: 12rpx;
}

.schedule-list {
  background: #ffffff;
  border-radius: 20rpx;
  padding: 20rpx;
}

.schedule-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.schedule-item:last-child {
  border-bottom: none;
}

.schedule-date {
  width: 100rpx;
  text-align: center;
  margin-right: 20rpx;
}

.date-day {
  font-size: 40rpx;
  font-weight: bold;
  color: #43a047;
  display: block;
}

.date-month {
  font-size: 24rpx;
  color: #999999;
  display: block;
}

.schedule-info {
  flex: 1;
}

.schedule-service {
  font-size: 30rpx;
  font-weight: bold;
  color: #333333;
  display: block;
}

.schedule-time {
  font-size: 26rpx;
  color: #999999;
  display: block;
  margin-top: 8rpx;
}

.schedule-status {
  font-size: 24rpx;
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
}

.empty-schedule {
  background: #ffffff;
  border-radius: 20rpx;
  padding: 60rpx;
  text-align: center;
  color: #999999;
  font-size: 28rpx;
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
