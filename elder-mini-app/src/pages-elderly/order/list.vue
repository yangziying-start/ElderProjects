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
          <text class="order-status" :class="getStatusClass(order)">{{ order.statusText }}</text>
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
    completedStatus: 3,
    confirmStatus: 2  // 待确认状态
  },
  meal: {
    0: '待配送', 1: '配送中', 2: '待确认', 3: '已完成', 4: '争议中', 5: '已取消',
    completedStatus: 3,
    confirmStatus: 2  // 待确认状态
  },
  medical: {
    0: '待巡诊', 1: '巡诊中', 2: '待确认', 3: '已完成', 4: '争议中', 5: '已取消',
    completedStatus: 3,
    confirmStatus: 2  // 待确认状态
  }
}

const getStatusLabel = (status) => {
  return statusConfig[currentModule.value][status] || '未知'
}

const getCompletedStatus = () => {
  return statusConfig[currentModule.value].completedStatus
}

// 根据状态文本返回样式类
const getStatusClass = (order) => {
  const statusText = order.statusText
  if (statusText.includes('待') && !statusText.includes('确认')) return 'status-pending'
  if (statusText.includes('中') && !statusText.includes('争议')) return 'status-processing'
  if (statusText.includes('待确认')) return 'status-confirm'
  if (statusText.includes('已完成') || statusText.includes('已确认')) return 'status-completed'
  if (statusText.includes('已取消')) return 'status-cancelled'
  if (statusText.includes('争议')) return 'status-dispute'
  return 'status-default'
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
  // 所有类型订单在待确认状态(status=2)都可以确认
  const config = statusConfig[currentModule.value]
  return order.status === config.confirmStatus
}

const canDispute = (order) => {
  // 所有类型订单在待确认状态(status=2)都可以提交争议
  const config = statusConfig[currentModule.value]
  return order.status === config.confirmStatus
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
    // 处理合并订单的菜品显示：如果remark包含"菜品:"则显示remark中的菜品信息
    let serviceName = order.dish?.dishName || '餐饮配送'
    if (order.remark && order.remark.startsWith('菜品:')) {
      serviceName = order.remark.replace('菜品: ', '')
    }
    return {
      id: order.id,
      orderNo: order.orderNo,
      serviceName: serviceName,
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
  // 根据订单类型显示不同的扣费规则
  let content = ''
  switch (currentModule.value) {
    case 'cleaning':
      content = '扣费规则：\n• 服务开始前30分钟免费取消\n• 30分钟内取消扣除5%积分\n\n确定要取消该订单吗？'
      break
    case 'meal':
      content = '扣费规则：\n• 送餐前>1小时免费取消\n• 1小时~10分钟扣5%\n• <10分钟扣10%\n\n确定要取消该订单吗？'
      break
    case 'medical':
      content = '医疗预约取消免费，确定要取消该预约吗？'
      break
    default:
      content = '确定要取消该订单吗？'
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
          let url = ''
          switch (currentModule.value) {
            case 'cleaning':
              url = `/cleaning/order/${order.id}/confirm`
              break
            case 'meal':
              url = `/meal/order/${order.id}/confirm`
              break
            case 'medical':
              url = `/medical/appointment/${order.id}/confirm`
              break
          }
          await post(url)
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
          let url = ''
          switch (currentModule.value) {
            case 'cleaning':
              url = `/cleaning/order/${order.id}/dispute?reason=${encodeURIComponent(res.content)}`
              break
            case 'meal':
              url = `/meal/order/${order.id}/dispute?reason=${encodeURIComponent(res.content)}`
              break
            case 'medical':
              url = `/medical/appointment/${order.id}/dispute?reason=${encodeURIComponent(res.content)}`
              break
          }
          await post(url)
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
.status-pending { background: #e8f5e9; color: #43a047; }
.status-processing { background: #fff7e6; color: #fa8c16; }
.status-confirm { background: #e6f7ff; color: #1890ff; }
.status-completed { background: #f6ffed; color: #52c41a; }
.status-cancelled { background: #f5f5f5; color: #999999; }
.status-dispute { background: #fff2e8; color: #fa541c; }
.status-default { background: #f5f5f5; color: #666666; }

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
