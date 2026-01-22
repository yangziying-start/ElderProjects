<template>
  <view class="container">
    <!-- 老人选择 -->
    <view class="elderly-select">
      <picker mode="selector" :range="elderlyNames" @change="onElderlyChange">
        <view class="picker-value">
          <text>{{ selectedElderly?.name || '选择老人' }}</text>
          <text class="arrow">▼</text>
        </view>
      </picker>
    </view>

    <!-- 日程列表 -->
    <view class="schedule-list" v-if="scheduleList.length > 0">
      <view class="schedule-item" v-for="item in scheduleList" :key="item.id" @click="viewDetail(item)">
        <view class="schedule-date">
          <text class="date-day">{{ item.day }}</text>
          <text class="date-month">{{ item.month }}</text>
        </view>
        <view class="schedule-info">
          <text class="schedule-service">{{ item.serviceName }}</text>
          <text class="schedule-time">{{ item.time }}</text>
          <text class="schedule-address" v-if="item.address">{{ item.address }}</text>
        </view>
        <view class="schedule-right">
          <text class="schedule-status" :class="'status-' + item.status">{{ item.statusText }}</text>
          <text class="proxy-tag" v-if="item.isProxy">代预约</text>
        </view>
      </view>
    </view>
    
    <view class="empty" v-else>
      <text class="empty-icon">📅</text>
      <text class="empty-text">暂无预约日程</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { get } from '@/utils/request'
import { onLoad, onShow } from '@dcloudio/uni-app'

const elderlyList = ref([])
const selectedElderly = ref(null)
const scheduleList = ref([])
const initialElderlyId = ref(null)

const elderlyNames = computed(() => elderlyList.value.map(e => e.name))

onLoad((options) => {
  if (options.elderlyId) {
    initialElderlyId.value = options.elderlyId
  }
})

onShow(async () => {
  try {
    elderlyList.value = await get('/user/bindElderly') || []
  } catch (e) {
    console.error(e)
  }
  
  if (elderlyList.value.length > 0) {
    if (initialElderlyId.value) {
      selectedElderly.value = elderlyList.value.find(e => e.id == initialElderlyId.value) || elderlyList.value[0]
    } else if (!selectedElderly.value) {
      selectedElderly.value = elderlyList.value[0]
    }
    loadSchedule()
  }
})

const onElderlyChange = (e) => {
  selectedElderly.value = elderlyList.value[e.detail.value]
  loadSchedule()
}

const loadSchedule = async () => {
  if (!selectedElderly.value) return
  try {
    scheduleList.value = await get('/order/schedule', { 
      elderlyId: selectedElderly.value.id,
      includeCompleted: true
    }) || []
  } catch (e) {
    console.error(e)
  }
}

const viewDetail = (item) => {
  uni.showModal({
    title: item.serviceName,
    content: `日期：${item.appointmentDate}\n时间：${item.time}\n状态：${item.statusText}\n地址：${item.address || '未填写'}${item.isProxy ? '\n（子女代预约）' : ''}`,
    showCancel: false
  })
}
</script>

<style scoped>
.container {
  padding: 30rpx;
  background: #f5f5f5;
  min-height: 100vh;
}

.elderly-select {
  background: #ffffff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
}

.picker-value {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 34rpx;
  color: #333333;
}

.arrow {
  color: #999999;
  font-size: 24rpx;
}

.schedule-list {
  background: #ffffff;
  border-radius: 20rpx;
}

.schedule-item {
  display: flex;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.schedule-item:last-child {
  border-bottom: none;
}

.schedule-date {
  width: 100rpx;
  text-align: center;
  margin-right: 24rpx;
}

.date-day {
  font-size: 44rpx;
  font-weight: bold;
  color: #43a047;
  display: block;
}

.date-month {
  font-size: 26rpx;
  color: #999999;
  display: block;
}

.schedule-info {
  flex: 1;
}

.schedule-service {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
  display: block;
}

.schedule-time {
  font-size: 28rpx;
  color: #666666;
  display: block;
  margin-top: 8rpx;
}

.schedule-address {
  font-size: 26rpx;
  color: #999999;
  display: block;
  margin-top: 8rpx;
}

.schedule-right {
  text-align: right;
}

.schedule-status {
  font-size: 26rpx;
  padding: 6rpx 16rpx;
  border-radius: 8rpx;
  display: inline-block;
}

.status-0 { background: #e8f5e9; color: #43a047; }
.status-1 { background: #fff7e6; color: #fa8c16; }
.status-2 { background: #e6f7ff; color: #1890ff; }
.status-3 { background: #fff7e6; color: #fa8c16; }
.status-4 { background: #f6ffed; color: #52c41a; }

.proxy-tag {
  font-size: 22rpx;
  color: #1890ff;
  display: block;
  margin-top: 8rpx;
}

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
