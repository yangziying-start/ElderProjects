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
          <view class="order-info">
            <text class="order-service">{{ order.serviceName }}</text>
            <text class="order-elderly">服务对象：{{ order.elderlyName }}</text>
          </view>
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
            <text class="code-tip">请告知老人在服务人员上门时提供</text>
          </view>
          <view class="code-display">
            <text class="code-char" v-for="(char, idx) in order.serviceCode.split('')" :key="idx">{{ char }}</text>
          </view>
        </view>
        
        <view class="order-footer">
          <text class="order-amount">{{ order.amount }}积分</text>
          <view class="order-actions">
            <view class="action-btn cancel" v-if="canCancel(order)" @click="cancelOrder(order)">取消预约</view>
            <view class="action-btn call" v-if="order.elderlyPhone" @click="callElderly(order)">联系老人</view>
          </view>
        </view>
      </view>

      <view class="empty" v-if="orderList.length === 0">
        <text class="empty-icon">📋</text>
        <text class="empty-text">暂无{{ getModuleName() }}代办订单</text>
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
    0: '待服务', 1: '服务中', 2: '待确认', 3: '已完成', 4: '已取消',
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

const loadOrders = async () => {
  try {
    const params = currentStatus.value !== null ? { status: currentStatus.value } : {}
    params.type = currentModule.value
    
    const data = await get('/order/proxy-by-type', params)
    orderList.value = data || []
  } catch (e) {
    console.error(e)
    orderList.value = []
  }
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

const cancelOrder = async (order) => {
  // 根据订单类型显示不同的扣费规则
  let content = ''
  switch (currentModule.value) {
    case 'cleaning':
      content = '扣费规则：\n• 服务开始前30分钟免费取消\n• 30分钟内取消扣除5%积分\n\n确定要取消该预约吗？'
      break
    case 'meal':
      content = '扣费规则：\n• 送餐前>1小时免费取消\n• 1小时~10分钟扣5%\n• <10分钟扣10%\n\n确定要取消该预约吗？'
      break
    case 'medical':
      content = '医疗预约取消免费，确定要取消该预约吗？'
      break
    default:
      content = '确定要取消该预约吗？'
  }
  
  uni.showModal({
    title: '确认取消',
    content: content,
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
          const result = await post(url)
          // 显示退款信息
          if (result && result.refund !== undefined) {
            uni.showToast({ title: `已取消，退还${result.refund}积分`, icon: 'none', duration: 2000 })
          } else {
            uni.showToast({ title: '取消成功', icon: 'success' })
          }
          await loadOrders()
        } catch (e) {
          uni.showToast({ title: e.message || '取消失败', icon: 'none' })
        }
      }
    }
  })
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

onShow(() => {
  loadOrders()
})
</script>

<style scoped>
.container {
  padding: 30rpx;
  background: #f5f5f5;
  min-height: 100vh;
}

/* 模块切换 */
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

.module-tab.active {
  background: #e8f5e9;
}

.module-icon {
  font-size: 48rpx;
  margin-bottom: 8rpx;
}

.module-name {
  font-size: 28rpx;
  color: #666;
}

.module-tab.active .module-name {
  color: #43a047;
  font-weight: bold;
}

/* 状态筛选 */
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
  font-size: 26rpx;
  color: #666666;
  padding: 16rpx 0;
  border-radius: 12rpx;
}

.status-tab.active {
  color: #ffffff;
  background: #43a047;
  font-weight: bold;
}

/* 订单卡片 */
.order-card {
  background: #ffffff;
  border-radius: 20rpx;
  margin-bottom: 24rpx;
  overflow: hidden;
}

.order-header {
  padding: 24rpx 30rpx;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border-bottom: 1rpx solid #f0f0f0;
}

.order-info {
  flex: 1;
}

.order-service {
  font-size: 34rpx;
  font-weight: bold;
  color: #333333;
  display: block;
  margin-bottom: 8rpx;
}

.order-elderly {
  font-size: 26rpx;
  color: #666;
}

.order-status {
  font-size: 26rpx;
  padding: 6rpx 20rpx;
  border-radius: 8rpx;
  flex-shrink: 0;
}

.status-0 { background: #e8f5e9; color: #43a047; }
.status-1 { background: #fff7e6; color: #fa8c16; }
.status-2 { background: #e6f7ff; color: #1890ff; }
.status-3 { background: #f6ffed; color: #52c41a; }
.status-4 { background: #f0f0f0; color: #999999; }

.order-content {
  padding: 24rpx 30rpx;
}

.order-time, .order-address, .order-worker {
  font-size: 28rpx;
  color: #666666;
  display: block;
  margin-bottom: 10rpx;
}

/* 服务码 */
.service-code-section {
  background: linear-gradient(135deg, #fff7e6, #fffbe6);
  margin: 0 30rpx;
  border-radius: 16rpx;
  padding: 30rpx;
}

.code-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.code-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #fa8c16;
}

.code-tip {
  font-size: 22rpx;
  color: #999;
}

.code-display {
  display: flex;
  justify-content: center;
  gap: 16rpx;
}

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

/* 底部 */
.order-footer {
  padding: 24rpx 30rpx;
  border-top: 1rpx solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-amount {
  font-size: 36rpx;
  font-weight: bold;
  color: #ff4d4f;
}

.order-actions {
  display: flex;
  gap: 16rpx;
}

.action-btn {
  padding: 12rpx 24rpx;
  border-radius: 8rpx;
  font-size: 26rpx;
}

.action-btn.cancel {
  border: 1rpx solid #ff4d4f;
  color: #ff4d4f;
}

.action-btn.call {
  background: #43a047;
  color: #ffffff;
}

/* 空状态 */
.empty {
  text-align: center;
  padding: 100rpx 0;
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
  color: #999999;
}
</style>
