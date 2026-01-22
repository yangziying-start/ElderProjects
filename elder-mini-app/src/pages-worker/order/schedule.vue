<template>
  <view class="container">
    <!-- 服务类型筛选 -->
    <view class="type-tabs">
      <view class="type-tab" :class="{ active: currentType === 'all' }" @click="filterByType('all')">全部</view>
      <view class="type-tab" :class="{ active: currentType === 'meal' }" @click="filterByType('meal')">🍚 餐饮</view>
      <view class="type-tab" :class="{ active: currentType === 'cleaning' }" @click="filterByType('cleaning')">🧹 保洁</view>
      <view class="type-tab" :class="{ active: currentType === 'medical' }" @click="filterByType('medical')">🏥 医疗</view>
    </view>

    <scroll-view class="date-scroll" scroll-x>
      <view class="date-list">
        <view class="date-item" :class="{ active: selectedDate === item.date, today: item.isToday }"
          v-for="item in dateList" :key="item.date" @click="selectDate(item.date)">
          <text class="date-week">{{ item.weekDay }}</text>
          <text class="date-day">{{ item.day }}</text>
          <view class="date-dot" v-if="item.hasOrder"></view>
        </view>
      </view>
    </scroll-view>

    <view class="section-title">{{ selectedDateText }}的日程</view>

    <view class="order-list">
      <view class="order-card" v-for="order in filteredOrders" :key="order.id" @click="goDetail(order)">
        <view class="order-time">
          <view class="time-info">
            <text class="time-text">{{ formatTime(order.appointmentTime) }}</text>
            <view class="order-type-tag" :class="'type-' + order.orderType">{{ getTypeText(order.orderType) }}</view>
          </view>
          <view class="status-tag" :class="'status-' + order.status">{{ order.statusText }}</view>
        </view>
        <view class="order-content">
          <text class="service-name">{{ order.serviceName }}</text>
          <view class="order-info">
            <text class="info-item">👴 {{ order.elderlyName }}</text>
            <text class="info-item">📍 {{ order.address }}</text>
          </view>
        </view>
        <view class="order-actions">
          <view class="action-btn primary" v-if="order.status === 0" @click.stop="goExecute(order)">开始服务</view>
          <view class="action-btn secondary" v-if="order.status === 1" @click.stop="goExecute(order)">继续服务</view>
          <view class="action-btn outline" @click.stop="callElderly(order)">联系老人</view>
        </view>
      </view>

      <view class="empty" v-if="filteredOrders.length === 0">
        <text class="empty-icon">📅</text>
        <text class="empty-text">当天暂无日程安排</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { get } from '@/utils/request'
import { onShow } from '@dcloudio/uni-app'

const currentType = ref('all')
const dateList = ref([])
const scheduleData = ref([])
const selectedDate = ref('')

const typeTextMap = {
  meal: '餐饮',
  cleaning: '保洁',
  medical: '医疗'
}

const getTypeText = (type) => typeTextMap[type] || '其他'

const initDateList = () => {
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const today = new Date()
  const list = []
  
  for (let i = 0; i < 7; i++) {
    const date = new Date(today)
    date.setDate(today.getDate() + i)
    const dateStr = date.toISOString().split('T')[0]
    list.push({
      date: dateStr,
      day: date.getDate(),
      weekDay: i === 0 ? '今天' : (i === 1 ? '明天' : weekDays[date.getDay()]),
      isToday: i === 0,
      hasOrder: false
    })
  }
  dateList.value = list
  selectedDate.value = list[0].date
}

const loadSchedule = async () => {
  try {
    const params = currentType.value !== 'all' ? { type: currentType.value } : {}
    const data = await get('/worker/schedule', params) || []
    scheduleData.value = data
    
    const orderDates = new Set(data.map(d => d.date))
    dateList.value.forEach(item => {
      item.hasOrder = orderDates.has(item.date)
    })
  } catch (e) {
    console.error(e)
  }
}

const filterByType = (type) => {
  currentType.value = type
  loadSchedule()
}

const selectDate = (date) => {
  selectedDate.value = date
}

const selectedDateText = computed(() => {
  const item = dateList.value.find(d => d.date === selectedDate.value)
  return item ? item.weekDay : ''
})

const filteredOrders = computed(() => {
  const dayData = scheduleData.value.find(d => d.date === selectedDate.value)
  return dayData ? dayData.orders : []
})

const formatTime = (timeStr) => {
  if (!timeStr) return '待定'
  return timeStr.substring(11, 16)
}

const goDetail = (order) => {
  uni.navigateTo({ url: `/pages-worker/task/detail?id=${order.id}&type=${order.orderType || 'cleaning'}` })
}

const goExecute = (order) => {
  uni.navigateTo({ url: `/pages-worker/order/execute?id=${order.id}&type=${order.orderType || 'cleaning'}` })
}

const callElderly = (order) => {
  if (order.elderlyPhone) {
    uni.makePhoneCall({ phoneNumber: order.elderlyPhone })
  }
}

onShow(() => {
  initDateList()
  loadSchedule()
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
.date-scroll { background: #fff; padding: 20rpx 0; }
.date-list { display: flex; padding: 0 20rpx; }
.date-item { 
  min-width: 100rpx; text-align: center; padding: 20rpx 16rpx; margin: 0 8rpx;
  border-radius: 16rpx; position: relative; background: #f5f5f5;
}
.date-item.active { background: #43a047; }
.date-item.active .date-week, .date-item.active .date-day { color: #fff; }
.date-week { font-size: 24rpx; color: #999; display: block; }
.date-day { font-size: 36rpx; font-weight: bold; color: #333; display: block; margin-top: 8rpx; }
.date-dot { 
  width: 12rpx; height: 12rpx; background: #ff4d4f; border-radius: 50%;
  position: absolute; top: 12rpx; right: 12rpx;
}
.date-item.active .date-dot { background: #fff; }
.section-title { font-size: 32rpx; font-weight: bold; color: #333; padding: 30rpx; }
.order-list { padding: 0 30rpx 30rpx; }
.order-card { background: #fff; border-radius: 20rpx; padding: 30rpx; margin-bottom: 20rpx; }
.order-time { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx; }
.time-info { display: flex; align-items: center; gap: 16rpx; }
.time-text { font-size: 40rpx; font-weight: bold; color: #333; }
.order-type-tag { font-size: 22rpx; padding: 6rpx 12rpx; border-radius: 6rpx; }
.type-meal { background: #fff7e6; color: #fa8c16; }
.type-cleaning { background: #e6f7ff; color: #1890ff; }
.type-medical { background: #f6ffed; color: #52c41a; }
.status-tag { font-size: 24rpx; padding: 8rpx 16rpx; border-radius: 8rpx; }
.status-0 { background: #e6f7ff; color: #1890ff; }
.status-1 { background: #fff7e6; color: #fa8c16; }
.status-2 { background: #e6f7ff; color: #1890ff; }
.status-3 { background: #f6ffed; color: #52c41a; }
.service-name { font-size: 34rpx; font-weight: bold; color: #333; display: block; margin-bottom: 16rpx; }
.info-item { font-size: 28rpx; color: #666; display: block; margin-bottom: 8rpx; }
.order-actions { display: flex; gap: 20rpx; margin-top: 24rpx; padding-top: 24rpx; border-top: 1rpx solid #f0f0f0; }
.action-btn { flex: 1; text-align: center; padding: 20rpx; border-radius: 12rpx; font-size: 28rpx; }
.action-btn.primary { background: #43a047; color: #fff; }
.action-btn.secondary { background: #1890ff; color: #fff; }
.action-btn.outline { background: #fff; color: #666; border: 1rpx solid #d9d9d9; }
.empty { text-align: center; padding: 100rpx 0; }
.empty-icon { font-size: 80rpx; display: block; margin-bottom: 20rpx; }
.empty-text { font-size: 30rpx; color: #999; }
</style>
