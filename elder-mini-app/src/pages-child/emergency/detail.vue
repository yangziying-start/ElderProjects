<template>
  <view class="container">
    <view class="status-banner" :class="'status-' + callInfo.status">
      <text class="status-text">{{ getStatusText(callInfo.status) }}</text>
    </view>

    <view class="info-section">
      <view class="info-item">
        <text class="label">呼叫老人</text>
        <text class="value">{{ callInfo.elderlyName }}</text>
      </view>
      <view class="info-item">
        <text class="label">事件类型</text>
        <text class="value type" :class="'type-' + callInfo.eventType">{{ getEventTypeText(callInfo.eventType) }}</text>
      </view>
      <view class="info-item">
        <text class="label">老人地址</text>
        <text class="value">{{ callInfo.address }}</text>
      </view>
      <view class="info-item">
        <text class="label">呼叫时间</text>
        <text class="value">{{ callInfo.triggerTime }}</text>
      </view>
      <view class="info-item" v-if="callInfo.responseTime">
        <text class="label">响应时间</text>
        <text class="value">{{ callInfo.responseTime }}</text>
      </view>
    </view>

    <view class="action-section" v-if="callInfo.status === 0">
      <view class="action-btn call" @click="callElderly">
        <text>📞 拨打老人电话</text>
      </view>
      <view class="action-btn respond" @click="handleRespond">
        <text>✓ 确认已处理</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { get, post } from '@/utils/request'
import { onLoad } from '@dcloudio/uni-app'

const callInfo = ref({})

const eventTypeMap = { 1: '紧急医疗', 2: '摔倒求助', 3: '一般求助' }
const statusMap = { 0: '待处理', 1: '已响应', 2: '已完成' }

const getEventTypeText = (type) => eventTypeMap[type] || '未知'
const getStatusText = (status) => statusMap[status] || '未知'

onLoad(async (options) => {
  const id = options.id
  try {
    callInfo.value = await get(`/emergency/${id}`) || {}
  } catch (e) {
    console.error(e)
  }
})

const callElderly = () => {
  if (callInfo.value.phone) {
    uni.makePhoneCall({ 
      phoneNumber: callInfo.value.phone, 
      fail: (err) => {
        // 用户取消拨打不提示错误
        if (err.errMsg && err.errMsg.includes('cancel')) return
        console.error('拨打电话失败:', err)
      }
    })
  }
}

const handleRespond = async () => {
  uni.showModal({
    title: '确认处理',
    content: '确认已处理该呼叫？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await post(`/emergency/${callInfo.value.id}/respond`)
          uni.showToast({ title: '处理成功', icon: 'success' })
          callInfo.value.status = 1
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
  background: #f5f5f5;
  min-height: 100vh;
}

.status-banner {
  padding: 60rpx;
  text-align: center;
}

.status-0 { background: linear-gradient(135deg, #ff4d4f, #ff7875); }
.status-1 { background: linear-gradient(135deg, #fa8c16, #ffc53d); }
.status-2 { background: linear-gradient(135deg, #52c41a, #95de64); }

.status-text {
  font-size: 48rpx;
  font-weight: bold;
  color: #ffffff;
}

.info-section {
  background: #ffffff;
  margin: 30rpx;
  border-radius: 24rpx;
  padding: 20rpx 40rpx;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 30rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.label {
  font-size: 32rpx;
  color: #999999;
}

.value {
  font-size: 32rpx;
  color: #333333;
}

.value.type {
  padding: 8rpx 20rpx;
  border-radius: 8rpx;
}

.type-1 { background: #fff1f0; color: #ff4d4f; }
.type-2 { background: #fff7e6; color: #fa8c16; }
.type-3 { background: #e8f5e9; color: #43a047; }

.action-section {
  padding: 30rpx;
}

.action-btn {
  padding: 40rpx;
  border-radius: 20rpx;
  text-align: center;
  font-size: 38rpx;
  font-weight: bold;
  margin-bottom: 30rpx;
}

.action-btn.call {
  background: #52c41a;
  color: #ffffff;
}

.action-btn.respond {
  background: #43a047;
  color: #ffffff;
}
</style>
