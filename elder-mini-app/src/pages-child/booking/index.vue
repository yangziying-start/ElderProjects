<template>
  <view class="container">
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

    <view class="section-title">选择服务</view>
    <view class="service-grid">
      <view class="service-item" @click="goBookService(1, '生活保洁')">
        <text class="service-icon">🧹</text>
        <text class="service-name">生活保洁</text>
        <text class="service-desc">日常清洁服务</text>
      </view>
      <view class="service-item" @click="goBookService(2, '餐饮配送')">
        <text class="service-icon">🍱</text>
        <text class="service-name">餐饮配送</text>
        <text class="service-desc">营养餐配送</text>
      </view>
      <view class="service-item" @click="goBookService(3, '社区医疗')">
        <text class="service-icon">🏥</text>
        <text class="service-name">社区医疗</text>
        <text class="service-desc">医疗巡诊服务</text>
      </view>
    </view>

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
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { get, post } from '@/utils/request'
import { onShow } from '@dcloudio/uni-app'

const elderlyList = ref([])
const selectedElderly = ref(null)
const orderList = ref([])

onShow(async () => {
  try {
    elderlyList.value = await get('/user/bindElderly') || []
    const allOrders = await get('/order/proxy') || []
    // 首页只显示最近5条订单
    orderList.value = allOrders.slice(0, 5)
  } catch (e) {
    console.error(e)
  }
  
  if (elderlyList.value.length > 0 && !selectedElderly.value) {
    selectedElderly.value = elderlyList.value[0]
  }
})

const selectElderly = (elderly) => {
  selectedElderly.value = elderly
  uni.setStorageSync('selectedElderly', elderly)
}

const goBookService = (categoryId, categoryName) => {
  if (!selectedElderly.value) {
    uni.showToast({ title: '请先选择老人', icon: 'none' })
    return
  }
  
  // 根据服务类型跳转到对应的预约页面
  const elderlyId = selectedElderly.value.id
  let url = ''
  
  switch (categoryId) {
    case 1: // 生活保洁
      url = `/pages-elderly/service/cleaning?elderlyId=${elderlyId}&proxy=1`
      break
    case 2: // 餐饮配送
      url = `/pages-elderly/service/meal?elderlyId=${elderlyId}&proxy=1`
      break
    case 3: // 社区医疗
      url = `/pages-elderly/service/medical?elderlyId=${elderlyId}&proxy=1`
      break
  }
  
  uni.navigateTo({ url })
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
          // 刷新老人列表
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
  uni.navigateTo({ url: '/pages-child/booking/orders' })
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
          await post(`/order/${order.id}/cancel`)
          uni.showToast({ title: '取消成功', icon: 'success' })
          orderList.value = await get('/order/proxy') || []
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
  padding: 30rpx;
  background: #f5f5f5;
  min-height: 100vh;
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
</style>
