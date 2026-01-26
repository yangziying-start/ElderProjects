<template>
  <view class="container">
    <!-- 服务类型筛选（根据服务人员类型显示） -->
    <view class="type-tabs" v-if="showTypeTabs">
      <view class="type-tab" :class="{ active: currentType === 'all' }" @click="filterByType('all')">全部</view>
      <view class="type-tab" :class="{ active: currentType === 'meal' }" @click="filterByType('meal')" v-if="canShowType('meal')">🍚 餐饮</view>
      <view class="type-tab" :class="{ active: currentType === 'cleaning' }" @click="filterByType('cleaning')" v-if="canShowType('cleaning')">🧹 保洁</view>
      <view class="type-tab" :class="{ active: currentType === 'medical' }" @click="filterByType('medical')" v-if="canShowType('medical')">🏥 医疗</view>
    </view>
    
    <!-- 服务人员类型提示 -->
    <view class="worker-type-hint" v-if="workerTypeName">
      <text>当前身份：{{ workerTypeName }}</text>
    </view>

    <view class="tabs">
      <view class="tab-item" :class="{ active: currentTab === 0 }" @click="switchTab(0)">可接单</view>
      <view class="tab-item" :class="{ active: currentTab === 1 }" @click="switchTab(1)">已接单</view>
    </view>

    <view class="task-list">
      <view class="task-card" v-for="task in taskList" :key="task.id" @click="goDetail(task)">
        <view class="task-header">
          <view class="task-title">
            <view class="order-type-tag" :class="'type-' + task.orderType">{{ getTypeText(task.orderType) }}</view>
            <text class="task-service">{{ task.serviceName }}</text>
          </view>
          <text class="task-amount">{{ task.amount }}积分</text>
        </view>
        <view class="task-info">
          <text class="task-item">👴 {{ task.elderlyName }}</text>
          <text class="task-item">📍 {{ task.address }}</text>
          <text class="task-item">🕐 {{ task.appointmentTime }}</text>
        </view>
        <view class="task-actions">
          <view class="action-btn" v-if="currentTab === 0" @click.stop="acceptTask(task)">接单</view>
          <view class="action-btn secondary" v-if="currentTab === 1" @click.stop="goExecute(task)">开始服务</view>
        </view>
      </view>

      <view class="empty" v-if="taskList.length === 0">
        <text class="empty-icon">📋</text>
        <text class="empty-text">暂无任务</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { get, post } from '@/utils/request'
import { onShow } from '@dcloudio/uni-app'

const currentType = ref('all')
const currentTab = ref(0)
const taskList = ref([])
const workerType = ref(null)

const typeTextMap = {
  meal: '餐饮配送',
  cleaning: '保洁服务',
  medical: '医疗巡诊'
}

const workerTypeNameMap = {
  1: '配送员',
  2: '保洁员',
  3: '医疗人员'
}

const workerTypeName = computed(() => workerTypeNameMap[workerType.value] || '')

// 是否显示类型筛选tabs（只有未设置workerType时才显示全部）
const showTypeTabs = computed(() => !workerType.value)

// 判断是否可以显示某个类型
const canShowType = (type) => {
  if (!workerType.value) return true
  switch (workerType.value) {
    case 1: return type === 'meal'
    case 2: return type === 'cleaning'
    case 3: return type === 'medical'
    default: return true
  }
}

const getTypeText = (type) => typeTextMap[type] || '其他'

const loadWorkerInfo = async () => {
  try {
    const info = await get('/worker/info')
    workerType.value = info?.workerType || null
  } catch (e) {
    console.error(e)
  }
}

const loadData = async () => {
  try {
    const params = {}
    if (currentType.value !== 'all') params.type = currentType.value
    
    if (currentTab.value === 0) {
      taskList.value = await get('/worker/available', params) || []
    } else {
      params.status = 0
      taskList.value = await get('/worker/my-orders', params) || []
    }
  } catch (e) {
    console.error(e)
    taskList.value = []
  }
}

const filterByType = (type) => {
  currentType.value = type
  loadData()
}

const switchTab = (index) => {
  currentTab.value = index
  loadData()
}

const goDetail = (task) => {
  uni.navigateTo({
    url: `/pages-worker/task/detail?id=${task.id}&type=${task.orderType || 'cleaning'}`
  })
}

const acceptTask = async (task) => {
  uni.showModal({
    title: '确认接单',
    content: '确定接受该任务？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await post('/worker/accept', {
            orderId: task.id,
            type: task.orderType || 'cleaning'
          })
          uni.showToast({ title: '接单成功', icon: 'success' })
          loadData()
        } catch (e) {
          console.error(e)
        }
      }
    }
  })
}

const goExecute = (task) => {
  uni.navigateTo({
    url: `/pages-worker/order/execute?id=${task.id}&type=${task.orderType || 'cleaning'}`
  })
}

onShow(async () => {
  await loadWorkerInfo()
  loadData()
})
</script>

<style scoped>
.container {
  background: #f5f5f5;
  min-height: 100vh;
}

.type-tabs { display: flex; background: #fff; padding: 20rpx 30rpx; }
.type-tab { 
  flex: 1; text-align: center; padding: 20rpx 0; font-size: 28rpx; color: #666;
  border-radius: 12rpx; margin: 0 8rpx;
}
.type-tab.active { background: #43a047; color: #fff; }

.worker-type-hint {
  background: #e6f7ff;
  padding: 20rpx 30rpx;
  text-align: center;
  font-size: 28rpx;
  color: #1890ff;
}

.tabs {
  display: flex;
  background: #ffffff;
  padding: 16rpx 30rpx;
  border-top: 1rpx solid #f0f0f0;
}

.tab-item {
  flex: 1;
  text-align: center;
  font-size: 32rpx;
  color: #666666;
  padding: 24rpx 0;
  border-radius: 12rpx;
  margin: 0 8rpx;
  background: #f5f5f5;
}

.tab-item.active {
  color: #ffffff;
  background: #43a047;
  font-weight: bold;
}

.task-list { padding: 30rpx; }

.task-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 40rpx;
  margin-bottom: 30rpx;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.task-title { display: flex; align-items: center; gap: 16rpx; }

.order-type-tag { font-size: 24rpx; padding: 8rpx 16rpx; border-radius: 8rpx; }
.type-meal { background: #fff7e6; color: #fa8c16; }
.type-cleaning { background: #e6f7ff; color: #1890ff; }
.type-medical { background: #f6ffed; color: #52c41a; }

.task-service {
  font-size: 38rpx;
  font-weight: bold;
  color: #333333;
}

.task-amount {
  font-size: 36rpx;
  color: #ff4d4f;
  font-weight: bold;
}

.task-item {
  font-size: 30rpx;
  color: #666666;
  display: block;
  margin-bottom: 12rpx;
}

.task-actions {
  margin-top: 30rpx;
  padding-top: 30rpx;
  border-top: 1rpx solid #f0f0f0;
}

.action-btn {
  background: #43a047;
  color: #ffffff;
  text-align: center;
  padding: 24rpx;
  border-radius: 12rpx;
  font-size: 34rpx;
  font-weight: bold;
}

.action-btn.secondary {
  background: #52c41a;
}

.empty {
  text-align: center;
  padding: 100rpx 0;
}

.empty-icon { font-size: 80rpx; display: block; margin-bottom: 20rpx; }
.empty-text { font-size: 32rpx; color: #999999; }
</style>
