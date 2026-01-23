<template>
  <view class="container">
    <view class="service-header">
      <text class="service-name">{{ service.name }}</text>
      <text class="service-price">¥{{ service.price }}</text>
    </view>
    
    <view class="service-desc">
      <text class="label">服务说明</text>
      <text class="content">{{ service.description }}</text>
    </view>
    
    <view class="service-info">
      <view class="info-item">
        <text class="label">服务时长</text>
        <text class="value">{{ service.duration }}分钟</text>
      </view>
      <view class="info-item">
        <text class="label">今日剩余名额</text>
        <text class="value" :class="{ 'full': totalCapacity <= 0 }">
          {{ totalCapacity > 0 ? totalCapacity + '个' : '已约满' }}
        </text>
      </view>
    </view>
    
    <view class="book-section">
      <view class="book-btn" :class="{ 'disabled': totalCapacity <= 0 }" @click="goBook">
        {{ totalCapacity > 0 ? '立即预约' : '今日已约满' }}
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { get } from '@/utils/request'
import { onLoad } from '@dcloudio/uni-app'

const service = ref({})
const timeSlots = ref([])
const serviceId = ref('')

const totalCapacity = computed(() => {
  return timeSlots.value.reduce((sum, slot) => sum + (slot.remainingCapacity || 0), 0)
})

onLoad(async (options) => {
  serviceId.value = options.id
  await loadData()
})

const loadData = async () => {
  try {
    const today = new Date().toISOString().split('T')[0]
    const [serviceData, slotsData] = await Promise.all([
      get(`/service/items/${serviceId.value}`),
      get(`/service/items/${serviceId.value}/time-slots`, { date: today })
    ])
    service.value = serviceData || {}
    timeSlots.value = slotsData || []
  } catch (e) {
    console.error(e)
  }
}

const goBook = () => {
  if (totalCapacity.value <= 0) {
    uni.showToast({ title: '今日已约满', icon: 'none' })
    return
  }
  uni.navigateTo({
    url: `/pages-elderly/service/book?id=${serviceId.value}&categoryId=${service.value.categoryId}`
  })
}
</script>

<style scoped>
.container {
  padding: 30rpx;
  background: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 200rpx;
}

.service-header {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 50rpx;
  margin-bottom: 30rpx;
}

.service-name {
  font-size: 48rpx;
  font-weight: bold;
  color: #333333;
  display: block;
}

.service-price {
  font-size: 56rpx;
  color: #ff4d4f;
  font-weight: bold;
  margin-top: 20rpx;
  display: block;
}

.service-desc, .service-info {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 40rpx;
  margin-bottom: 30rpx;
}

.label {
  font-size: 32rpx;
  color: #999999;
  display: block;
}

.content {
  font-size: 36rpx;
  color: #333333;
  margin-top: 20rpx;
  display: block;
  line-height: 1.6;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.value {
  font-size: 36rpx;
  color: #333333;
  font-weight: 500;
}

.value.full {
  color: #ff4d4f;
}

.book-section {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 30rpx;
  background: #ffffff;
  padding-bottom: calc(30rpx + env(safe-area-inset-bottom));
}

.book-btn {
  background: #43a047;
  color: #ffffff;
  text-align: center;
  padding: 36rpx;
  border-radius: 20rpx;
  font-size: 40rpx;
  font-weight: bold;
}

.book-btn.disabled {
  background: #cccccc;
}
</style>
