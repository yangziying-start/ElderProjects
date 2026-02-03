<template>
  <view class="container">
    <view class="tabs">
      <view 
        class="tab-item" 
        :class="{ active: currentTab === 0 }"
        @click="switchTab(0)"
      >待处理</view>
      <view 
        class="tab-item" 
        :class="{ active: currentTab === 1 }"
        @click="switchTab(1)"
      >已处理</view>
    </view>

    <view class="call-list">
      <view class="call-card" v-for="call in callList" :key="call.id" @click="goDetail(call)">
        <view class="call-header">
          <text class="call-type" :class="'type-' + call.eventType">{{ getEventTypeText(call.eventType) }}</text>
          <text class="call-status" :class="'status-' + call.status">{{ getStatusText(call.status) }}</text>
        </view>
        <view class="call-info">
          <text class="call-elderly">{{ call.elderlyName }}</text>
          <text class="call-address">{{ call.address }}</text>
          <text class="call-time">{{ call.triggerTime }}</text>
        </view>
        <view class="call-actions" v-if="call.status === 0">
          <view class="action-btn primary" @click.stop="handleRespond(call)">已处理</view>
          <view class="action-btn" @click.stop="callElderly(call)">📞 拨打电话</view>
        </view>
      </view>

      <view class="empty" v-if="callList.length === 0">
        <text>暂无呼叫记录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { get, post } from '@/utils/request'
import { onShow } from '@dcloudio/uni-app'

const currentTab = ref(0)
const callList = ref([])

const eventTypeMap = { 1: '紧急医疗', 2: '摔倒求助', 3: '一般求助' }
const statusMap = { 0: '待处理', 1: '已响应', 2: '已完成' }

const getEventTypeText = (type) => eventTypeMap[type] || '未知'
const getStatusText = (status) => statusMap[status] || '未知'

const loadData = async () => {
  try {
    const status = currentTab.value === 0 ? 0 : null
    const params = status !== null ? { status } : {}
    callList.value = await get('/emergency/list', params) || []
  } catch (e) {
    console.error(e)
    callList.value = []
  }
}

const switchTab = (index) => {
  currentTab.value = index
  loadData()
}

const goDetail = (call) => {
  uni.navigateTo({
    url: `/pages-child/emergency/detail?id=${call.id}`
  })
}

const handleRespond = async (call) => {
  uni.showModal({
    title: '确认处理',
    content: '确认已处理该呼叫？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await post(`/emergency/${call.id}/respond`)
          uni.showToast({ title: '处理成功', icon: 'success' })
          loadData()
        } catch (e) {
          console.error(e)
        }
      }
    }
  })
}

const callElderly = (call) => {
  if (call.phone) {
    uni.makePhoneCall({ 
      phoneNumber: call.phone, 
      fail: (err) => {
        // 用户取消拨打不提示错误
        if (err.errMsg && err.errMsg.includes('cancel')) return
        console.error('拨打电话失败:', err)
      }
    })
  }
}

onShow(() => {
  loadData()
})
</script>

<style scoped>
.container {
  padding: 30rpx;
  background: #f5f5f5;
  min-height: 100vh;
}

.tabs {
  display: flex;
  background: #ffffff;
  border-radius: 20rpx;
  padding: 16rpx;
  margin-bottom: 30rpx;
}

.tab-item {
  flex: 1;
  text-align: center;
  font-size: 32rpx;
  color: #666666;
  padding: 24rpx 0;
  border-radius: 12rpx;
}

.tab-item.active {
  color: #ffffff;
  background: #ff4d4f;
  font-weight: bold;
}

.call-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 40rpx;
  margin-bottom: 30rpx;
}

.call-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.call-type {
  font-size: 30rpx;
  padding: 8rpx 20rpx;
  border-radius: 8rpx;
}

.type-1 { background: #fff1f0; color: #ff4d4f; }
.type-2 { background: #fff7e6; color: #fa8c16; }
.type-3 { background: #e8f5e9; color: #43a047; }

.call-status {
  font-size: 28rpx;
}

.status-0 { color: #ff4d4f; }
.status-1 { color: #fa8c16; }
.status-2 { color: #52c41a; }

.call-elderly {
  font-size: 38rpx;
  font-weight: bold;
  color: #333333;
  display: block;
}

.call-address, .call-time {
  font-size: 30rpx;
  color: #666666;
  display: block;
  margin-top: 12rpx;
}

.call-actions {
  display: flex;
  gap: 20rpx;
  margin-top: 30rpx;
  padding-top: 30rpx;
  border-top: 1rpx solid #f0f0f0;
}

.action-btn {
  flex: 1;
  text-align: center;
  padding: 24rpx;
  border-radius: 12rpx;
  font-size: 32rpx;
  border: 2rpx solid #d9d9d9;
  color: #666666;
}

.action-btn.primary {
  background: #ff4d4f;
  color: #ffffff;
  border-color: #ff4d4f;
}

.empty {
  text-align: center;
  padding: 100rpx 0;
  color: #999999;
  font-size: 32rpx;
}
</style>
