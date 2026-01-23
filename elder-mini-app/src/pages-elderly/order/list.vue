<template>
  <view class="container">
    <!-- 模块切换 -->
    <view class="module-tabs">
      <view class="module-tab" :class="{ active: currentModule === 'cleaning' }" @click="switchModule('cleaning')">
        <text class="module-icon">🧹</text>
        <text class="module-name">保洁</text>
      </view>
      <view class="module-tab" :class="{ active: currentModule === 'meal' }" @click="switchModule('meal')">
        <text class="module-icon">🍱</text>
        <text class="module-name">餐饮</text>
      </view>
      <view class="module-tab" :class="{ active: currentModule === 'medical' }" @click="switchModule('medical')">
        <text class="module-icon">🏥</text>
        <text class="module-name">医疗</text>
      </view>
    </view>

    <!-- 状态筛选 -->
    <view class="status-tabs">
      <view class="status-tab" :class="{ active: currentStatus === null }" @click="filterByStatus(null)">全部</view>
      <view class="status-tab" :class="{ active: currentStatus === 0 }" @click="filterByStatus(0)">{{ getStatusLabel(0) }}</view>
      <view class="status-tab" :class="{ active: currentStatus === 1 }" @click="filterByStatus(1)">{{ getStatusLabel(1) }}</view>
      <view class="status-tab" :class="{ active: currentStatus === 2 }" @click="filterByStatus(2)">{{ getStatusLabel(2) }}</view>
    </view>

    <!-- 订单列表 -->
    <view class="order-list">
      <view class="order-card" v-for="order in orderList" :key="order.id">
        <view class="order-header">
          <text class="order-service">{{ order.serviceName }}</text>
          <text class="order-status" :class="'status-' + order.status">{{ order.statusText }}</text>
        </view>
        <view class="order-content">
          <text class="order-time">预约时间：{{ order.appointmentTime }}</text>
          <text class="order-address">服务地址：{{ order.address || '未填写' }}</text>
          <text class="order-worker" v-if="order.workerName">服务人员：{{ order.workerName }}</text>
        </view>
        
        <!-- 服务码显示区域 -->
        <view class="service-code-section" v-if="order.serviceCode && order.status < getCompletedStatus()">
          <view class="code-header">
            <text class="code-title">服务验证码</text>
            <text class="code-tip">请在服务人员上门时提供</text>
          </view>
          <view class="code-display">
            <text class="code-char" v-for="(char, idx) in order.serviceCode.split('')" :key="idx">{{ char }}</text>
          </view>
        </view>

        <view class="order-footer">
          <text class="order-amount">{{ order.amount }}积分</text>
          <view class="order-actions">
            <view class="action-btn cancel" v-if="canCancel(order)" @click="cancelOrder(order)">取消订单</view>
            <view class="action-btn dispute" v-if="canDispute(order)" @click="showDisputeDialog(order)">有争议</view>
            <view class="action-btn confirm" v-if="canConfirm(order)" @click="confirmOrder(order)">确认完成</view>
          </view>
        </view>
      </view>
      
      <view class="empty" v-if="orderList.length === 0">
        <text class="empty-icon">📋</text>
        <text class="empty-text">暂无{{ getModuleName() }}订单</text>
      </view>
    </view>

    <!-- 底部导航 -->
    <view class="tab-bar">
      <view class="tab-item" @click="goPage('/pages-elderly/index/index')">
        <text class="tab-icon">🏠</text>
        <text class="tab-text">首页</text>
      </view>
      <view class="tab-item active">
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
import { ref } from 'vue'
import { get, post } from '@/utils/request'
import { onShow } from '@dcloudio/uni-app'

const currentModule = ref('cleaning')
const currentStatus = ref(null)
const orderList = ref([])

// 不同模块的状态文本
const statusConfig = {
  cleaning: {
    0: '待服务', 1: '服务中', 2: '待确认', 3: '已完成', 4: '已取消', 5: '争议中',
    completedStatus: 3
  },
  meal: {
    0: '待配送', 1: '配送中', 2: '已送达', 3: '已取消',
    completedStatus: 2
  },
  medical: {
    0: '待巡诊', 1: '巡诊中', 2: '已完成', 3: '已取消',
    completedStatus: 2
  }
}

const getStatusLabel = (status) => {
  return statusConfig[currentModule.value][status] || '未知'
}

const getCompletedStatus = () => {
  return statusConfig[currentModule.value].completedStatus
}

const getModuleName = () => {
  const names = { cleaning: '保洁', meal: '餐饮', medical: '医疗' }
  return names[currentModule.value]
}

const canCancel = (order) => {
  if (currentModule.value === 'cleaning') return order.status < 2
  if (currentModule.value === 'meal') return order.status < 2
  if (currentModule.value === 'medical') return order.status === 0
  return false
}

const canConfirm = (order) => {
  if (currentModule.value === 'cleaning') return order.status === 2
  return false
}

const canDispute = (order) => {
  // 只有保洁订单在待确认状态才能提交争议
  if (currentModule.value === 'cleaning') return order.status === 2
  return false
}

const loadOrders = async () => {
  try {
    let url = ''
    const params = currentStatus.value !== null ? { status: currentStatus.value } : {}
    
    switch (currentModule.value) {
      case 'cleaning':
        url = '/cleaning/orders'
        break
      case 'meal':
        url = '/meal/orders'
        break
      case 'medical':
        url = '/medical/appointments'
        break
    }
    
    const data = await get(url, params)
    orderList.value = (data || []).map(order => formatOrder(order))
  } catch (e) {
    console.error(e)
    orderList.value = []
  }
}

const formatOrder = (order) => {
  const config = statusConfig[currentModule.value]
  
  if (currentModule.value === 'cleaning') {
    return {
      id: order.id,
      orderNo: order.orderNo,
      serviceName: order.service?.name || '保洁服务',
      appointmentTime: `${order.serviceDate} ${order.startTime}-${order.endTime}`,
      address: order.address,
      workerName: order.worker?.name,
      amount: order.amount,
      status: order.status,
      statusText: config[order.status] || '未知',
      serviceCode: order.serviceCode
    }
  }
  
  if (currentModule.value === 'meal') {
    const mealTypes = { 1: '早餐', 2: '午餐', 3: '晚餐' }
    return {
      id: order.id,
      orderNo: order.orderNo,
      serviceName: order.dish?.dishName || '餐饮配送',
      appointmentTime: `${order.dishDate} ${mealTypes[order.mealType] || ''}`,
      address: order.address,
      workerName: order.workerName,
      amount: order.amount,
      status: order.status,
      statusText: config[order.status] || '未知',
      serviceCode: order.serviceCode
    }
  }
  
  if (currentModule.value === 'medical') {
    return {
      id: order.id,
      orderNo: order.orderNo,
      serviceName: order.doctor?.name ? `${order.doctor.name}医生` : '医疗巡诊',
      appointmentTime: `${order.appointmentDate} 排队号:${order.queueNumber}`,
      address: order.address,
      workerName: order.doctor?.name,
      amount: 0,
      status: order.status,
      statusText: config[order.status] || '未知',
      serviceCode: order.serviceCode
    }
  }
  
  return order
}

const switchModule = (module) => {
  currentModule.value = module
  currentStatus.value = null
  loadOrders()
}

const filterByStatus = (status) => {
  currentStatus.value = status
  loadOrders()
}

const cancelOrder = (order) => {
  uni.showModal({
    title: '确认取消',
    content: '确定要取消该订单吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          let url = ''
          switch (currentModule.value) {
            case 'cleaning':
              url = `/cleaning/order/${order.id}/cancel`
              break
            case 'meal':
              url = `/meal/order/${order.id}/cancel`
              break
            case 'medical':
              url = `/medical/appointment/${order.id}/cancel`
              break
          }
          await post(url)
          uni.showToast({ title: '取消成功', icon: 'success' })
          loadOrders()
        } catch (e) {
          console.error(e)
        }
      }
    }
  })
}

const confirmOrder = (order) => {
  uni.showModal({
    title: '确认服务完成',
    content: '确认服务人员已完成服务？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await post(`/cleaning/order/${order.id}/confirm`)
          uni.showToast({ title: '确认成功', icon: 'success' })
          loadOrders()
        } catch (e) {
          console.error(e)
        }
      }
    }
  })
}

const showDisputeDialog = (order) => {
  uni.showModal({
    title: '提交争议',
    editable: true,
    placeholderText: '请输入争议原因',
    success: async (res) => {
      if (res.confirm && res.content) {
        try {
          await post(`/cleaning/order/${order.id}/dispute?reason=${encodeURIComponent(res.content)}`)
          uni.showToast({ title: '争议已提交', icon: 'success' })
          loadOrders()
        } catch (e) {
          console.error(e)
        }
      } else if (res.confirm && !res.content) {
        uni.showToast({ title: '请输入争议原因', icon: 'none' })
      }
    }
  })
}

const goPage = (url) => {
  uni.reLaunch({ url })
}

onShow(() => {
  loadOrders()
})
</script>

<style scoped>
.container { padding: 30rpx; background: #f5f5f5; min-height: 100vh; padding-bottom: 180rpx; }

.module-tabs {
  display: flex;
  background: #ffffff;
  border-radius: 20rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
}

.module-tab {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx;
  border-radius: 16rpx;
}

.module-tab.active { background: #e8f5e9; }
.module-icon { font-size: 48rpx; margin-bottom: 8rpx; }
.module-name { font-size: 28rpx; color: #666; }
.module-tab.active .module-name { color: #43a047; font-weight: bold; }

.status-tabs {
  display: flex;
  background: #ffffff;
  border-radius: 16rpx;
  padding: 12rpx;
  margin-bottom: 30rpx;
}

.status-tab {
  flex: 1;
  text-align: center;
  font-size: 28rpx;
  color: #666666;
  padding: 16rpx 0;
  border-radius: 12rpx;
}

.status-tab.active { color: #ffffff; background: #43a047; font-weight: bold; }

.order-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 40rpx;
  margin-bottom: 30rpx;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.order-service { font-size: 36rpx; font-weight: bold; color: #333333; }
.order-status { font-size: 28rpx; padding: 8rpx 20rpx; border-radius: 8rpx; }
.status-0 { background: #e8f5e9; color: #43a047; }
.status-1 { background: #fff7e6; color: #fa8c16; }
.status-2 { background: #e6f7ff; color: #1890ff; }
.status-3 { background: #f6ffed; color: #52c41a; }
.status-4 { background: #f5f5f5; color: #999999; }
.status-5 { background: #fff2e8; color: #fa541c; }

.order-content { border-top: 1rpx solid #f0f0f0; padding: 24rpx 0; }
.order-time, .order-address, .order-worker { font-size: 30rpx; color: #666666; display: block; margin-bottom: 12rpx; }

.service-code-section {
  background: linear-gradient(135deg, #fff7e6, #fffbe6);
  border-radius: 16rpx;
  padding: 30rpx;
  margin: 20rpx 0;
}

.code-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx; }
.code-title { font-size: 32rpx; font-weight: bold; color: #fa8c16; }
.code-tip { font-size: 24rpx; color: #999; }
.code-display { display: flex; justify-content: center; gap: 16rpx; }
.code-char {
  width: 70rpx;
  height: 90rpx;
  background: #fff;
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
  font-weight: bold;
  color: #fa8c16;
  box-shadow: 0 4rpx 12rpx rgba(250, 140, 22, 0.2);
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 24rpx;
  border-top: 1rpx solid #f0f0f0;
}

.order-amount { font-size: 40rpx; color: #ff4d4f; font-weight: bold; }
.order-actions { display: flex; gap: 16rpx; }
.action-btn { padding: 20rpx 40rpx; border-radius: 12rpx; font-size: 28rpx; }
.action-btn.cancel { border: 2rpx solid #999999; color: #666666; }
.action-btn.dispute { border: 2rpx solid #fa8c16; color: #fa8c16; }
.action-btn.confirm { background: #43a047; color: #fff; }

.empty { text-align: center; padding: 100rpx 0; }
.empty-icon { font-size: 80rpx; display: block; margin-bottom: 20rpx; }
.empty-text { font-size: 32rpx; color: #999999; }

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
