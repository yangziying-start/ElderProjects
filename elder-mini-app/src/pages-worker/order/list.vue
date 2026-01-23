<template>
  <view class="container">
    <!-- 服务类型筛选 -->
    <view class="type-tabs">
      <view class="type-tab" :class="{ active: currentType === 'all' }" @click="filterByType('all')">全部</view>
      <view class="type-tab" :class="{ active: currentType === 'meal' }" @click="filterByType('meal')">🍚 餐饮</view>
      <view class="type-tab" :class="{ active: currentType === 'cleaning' }" @click="filterByType('cleaning')">🧹 保洁</view>
      <view class="type-tab" :class="{ active: currentType === 'medical' }" @click="filterByType('medical')">🏥 医疗</view>
    </view>

    <!-- 状态筛选 -->
    <view class="filter-tabs">
      <view class="tab-item" :class="{ active: currentStatus === null }" @click="filterByStatus(null)">全部</view>
      <view class="tab-item" :class="{ active: currentStatus === 0 }" @click="filterByStatus(0)">待服务</view>
      <view class="tab-item" :class="{ active: currentStatus === 1 }" @click="filterByStatus(1)">服务中</view>
      <view class="tab-item" :class="{ active: currentStatus === 2 }" @click="filterByStatus(2)">待确认</view>
      <view class="tab-item" :class="{ active: currentStatus === 3 }" @click="filterByStatus(3)">已完成</view>
    </view>

    <!-- 工单列表 -->
    <view class="order-list">
      <view class="order-card" v-for="order in orderList" :key="order.id" @click="goDetail(order)">
        <view class="order-header">
          <view class="order-type-tag" :class="'type-' + order.orderType">{{ getTypeText(order.orderType) }}</view>
          <view class="status-tag" :class="'status-' + order.status">{{ order.statusText }}</view>
        </view>
        
        <view class="order-content">
          <view class="service-info">
            <text class="service-name">{{ order.serviceName }}</text>
            <text class="service-amount">{{ order.amount }}积分</text>
          </view>
          <view class="order-detail">
            <text class="detail-item">👴 {{ order.elderlyName }}</text>
            <text class="detail-item">📍 {{ order.address }}</text>
            <text class="detail-item">🕐 {{ order.appointmentTime }}</text>
          </view>
        </view>

        <view class="order-actions" v-if="order.status === 0 || order.status === 1">
          <view class="action-btn accept" v-if="order.status === 0 && !order.serviceCode" @click.stop="acceptOrder(order)">接单</view>
          <view class="action-btn primary" v-if="order.status === 0 && order.serviceCode" @click.stop="goExecute(order)">开始服务</view>
          <view class="action-btn primary" v-if="order.status === 1" @click.stop="goExecute(order)">继续服务</view>
          <view class="action-btn outline" @click.stop="callElderly(order)">联系老人</view>
        </view>
      </view>

      <view class="empty" v-if="orderList.length === 0">
        <text class="empty-icon">📋</text>
        <text class="empty-text">暂无工单记录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { get, post } from '@/utils/request'
import { onShow } from '@dcloudio/uni-app'

const currentType = ref('all')
const currentStatus = ref(null)
const orderList = ref([])

const typeTextMap = {
  meal: '餐饮',
  cleaning: '保洁',
  medical: '医疗'
}

const getTypeText = (type) => typeTextMap[type] || '其他'

const loadOrders = async () => {
  try {
    const params = {}
    if (currentStatus.value !== null) params.status = currentStatus.value
    if (currentType.value !== 'all') params.type = currentType.value
    orderList.value = await get('/worker/my-orders', params) || []
  } catch (e) {
    console.error(e)
    orderList.value = []
  }
}

const filterByType = (type) => {
  currentType.value = type
  loadOrders()
}

const filterByStatus = (status) => {
  currentStatus.value = status
  loadOrders()
}

const goDetail = (order) => {
  uni.navigateTo({ url: `/pages-worker/task/detail?id=${order.id}&type=${order.orderType || 'cleaning'}` })
}

const goExecute = (order) => {
  uni.navigateTo({ url: `/pages-worker/order/execute?id=${order.id}&type=${order.orderType || 'cleaning'}` })
}

const callElderly = (order) => {
  if (order.elderlyPhone) {
    uni.makePhoneCall({ 
      phoneNumber: order.elderlyPhone, 
      fail: (err) => {
        // 用户取消拨打不提示错误
        if (err.errMsg && err.errMsg.includes('cancel')) return
        console.error('拨打电话失败:', err)
      }
    })
  }
}

const acceptOrder = async (order) => {
  try {
    const result = await post('/worker/accept', {
      orderId: order.id,
      type: order.orderType || 'cleaning'
    })
    uni.showToast({ title: '接单成功', icon: 'success' })
    order.serviceCode = result.serviceCode
  } catch (e) {
    console.error('接单失败:', e)
  }
}

onShow(() => {
  loadOrders()
})
</script>

<style scoped>
.container { background: #f5f5f5; min-height: 100vh; }
.type-tabs { display: flex; background: #fff; padding: 20rpx 30rpx; border-bottom: 1rpx solid #f0f0f0; }
.type-tab { 
  flex: 1; text-align: center; padding: 20rpx 0; font-size: 28rpx; color: #666;
  border-radius: 12rpx; margin: 0 8rpx;
}
.type-tab.active { background: #43a047; color: #fff; }
.filter-tabs { display: flex; background: #fff; padding: 20rpx; overflow-x: auto; }
.tab-item { 
  flex-shrink: 0; padding: 16rpx 28rpx; margin-right: 16rpx; font-size: 28rpx; 
  color: #666; background: #f5f5f5; border-radius: 30rpx; 
}
.tab-item.active { background: #43a047; color: #fff; }
.order-list { padding: 30rpx; }
.order-card { background: #fff; border-radius: 20rpx; padding: 30rpx; margin-bottom: 24rpx; }
.order-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx; padding-bottom: 20rpx; border-bottom: 1rpx solid #f0f0f0; }
.order-type-tag { font-size: 24rpx; padding: 8rpx 16rpx; border-radius: 8rpx; }
.type-meal { background: #fff7e6; color: #fa8c16; }
.type-cleaning { background: #e6f7ff; color: #1890ff; }
.type-medical { background: #f6ffed; color: #52c41a; }
.status-tag { font-size: 24rpx; padding: 8rpx 16rpx; border-radius: 8rpx; }
.status-0 { background: #e6f7ff; color: #1890ff; }
.status-1 { background: #fff7e6; color: #fa8c16; }
.status-2 { background: #e6f7ff; color: #1890ff; }
.status-3 { background: #f6ffed; color: #52c41a; }
.status-4 { background: #f5f5f5; color: #999; }
.service-info { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.service-name { font-size: 34rpx; font-weight: bold; color: #333; }
.service-amount { font-size: 36rpx; font-weight: bold; color: #ff4d4f; }
.detail-item { font-size: 28rpx; color: #666; display: block; margin-bottom: 8rpx; }
.order-actions { display: flex; gap: 20rpx; margin-top: 24rpx; padding-top: 24rpx; border-top: 1rpx solid #f0f0f0; }
.action-btn { flex: 1; text-align: center; padding: 20rpx; border-radius: 12rpx; font-size: 28rpx; }
.action-btn.primary { background: #43a047; color: #fff; }
.action-btn.accept { background: #fa8c16; color: #fff; }
.action-btn.outline { background: #fff; color: #666; border: 1rpx solid #d9d9d9; }
.empty { text-align: center; padding: 100rpx 0; }
.empty-icon { font-size: 80rpx; display: block; margin-bottom: 20rpx; }
.empty-text { font-size: 30rpx; color: #999; }
</style>
