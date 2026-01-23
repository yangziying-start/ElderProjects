<template>
  <view class="container">
    <view class="elderly-select">
      <picker mode="selector" :range="elderlyNames" @change="onElderlyChange">
        <view class="picker-value">
          <text>{{ selectedElderly?.name || '选择老人' }}</text>
          <text class="arrow">▼</text>
        </view>
      </picker>
    </view>

    <view class="section">
      <view class="section-header">
        <text class="section-title">基本信息</text>
      </view>
      <view class="info-grid">
        <view class="info-item">
          <text class="label">姓名</text>
          <text class="value">{{ healthInfo.name || '未填写' }}</text>
        </view>
        <view class="info-item">
          <text class="label">性别</text>
          <text class="value">{{ healthInfo.gender || '未填写' }}</text>
        </view>
        <view class="info-item">
          <text class="label">年龄</text>
          <text class="value">{{ healthInfo.age ? healthInfo.age + '岁' : '未填写' }}</text>
        </view>
        <view class="info-item">
          <text class="label">生日</text>
          <text class="value">{{ healthInfo.birthday || '未填写' }}</text>
        </view>
        <view class="info-item full">
          <text class="label">联系电话</text>
          <text class="value phone" @click="callElderly">{{ healthInfo.phone || '未填写' }}</text>
        </view>
        <view class="info-item full">
          <text class="label">家庭住址</text>
          <text class="value">{{ healthInfo.address || '未填写' }}</text>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="section-header">
        <text class="section-title">健康概况</text>
      </view>
      <view class="health-stats">
        <view class="stat-item">
          <text class="stat-value">{{ healthInfo.appointmentCount || 0 }}</text>
          <text class="stat-label">预约次数</text>
        </view>
        <view class="stat-item">
          <text class="stat-value">{{ healthInfo.completedCount || 0 }}</text>
          <text class="stat-label">完成巡诊</text>
        </view>
      </view>
      <view class="health-info-list">
        <view class="health-info-item">
          <text class="label">过敏史</text>
          <text class="value">{{ healthInfo.allergies || '无' }}</text>
        </view>
        <view class="health-info-item">
          <text class="label">常服药品</text>
          <text class="value">{{ healthInfo.medications || '无' }}</text>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="section-header">
        <text class="section-title">医疗记录</text>
      </view>
      <view class="record-list">
        <view class="record-item" v-for="record in healthRecords" :key="record.id" @click="viewRecordDetail(record)">
          <view class="record-header">
            <text class="record-type">{{ record.appointmentTypeName }}</text>
            <text class="record-status" :class="'status-' + record.status">{{ record.statusName }}</text>
          </view>
          <text class="record-date">{{ formatDate(record.appointmentDate) }}</text>
          <text class="record-symptoms" v-if="record.symptoms">症状：{{ record.symptoms }}</text>
          <text class="record-doctor" v-if="record.doctorName">医生：{{ record.doctorName }}</text>
          <text class="record-diagnosis" v-if="record.diagnosis">诊断：{{ record.diagnosis }}</text>
        </view>
        <view class="empty" v-if="healthRecords.length === 0">
          <text>暂无医疗记录</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { get } from '@/utils/request'
import { onLoad, onShow } from '@dcloudio/uni-app'

const elderlyList = ref([])
const selectedElderly = ref(null)
const healthInfo = ref({})
const healthRecords = ref([])
const initialElderlyId = ref(null)

const elderlyNames = computed(() => elderlyList.value.map(e => e.name))

onLoad((options) => {
  // 从首页传递的老人ID
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
    // 优先使用传递的elderlyId
    if (initialElderlyId.value) {
      selectedElderly.value = elderlyList.value.find(e => e.id == initialElderlyId.value) || elderlyList.value[0]
    } else if (!selectedElderly.value) {
      selectedElderly.value = elderlyList.value[0]
    }
    loadHealth()
  }
})

const onElderlyChange = (e) => {
  selectedElderly.value = elderlyList.value[e.detail.value]
  loadHealth()
}

const loadHealth = async () => {
  if (!selectedElderly.value) return
  try {
    healthInfo.value = await get('/user/health', { elderlyId: selectedElderly.value.id }) || {}
    healthRecords.value = await get('/user/health/records', { elderlyId: selectedElderly.value.id }) || []
  } catch (e) {
    console.error(e)
  }
}

const callElderly = () => {
  if (healthInfo.value.phone) {
    uni.makePhoneCall({ 
      phoneNumber: healthInfo.value.phone, 
      fail: (err) => {
        // 用户取消拨打不提示错误
        if (err.errMsg && err.errMsg.includes('cancel')) return
        console.error('拨打电话失败:', err)
      }
    })
  }
}

const formatDate = (date) => {
  if (!date) return ''
  return date.substring(0, 10)
}

const viewRecordDetail = (record) => {
  uni.showModal({
    title: record.appointmentTypeName,
    content: `日期：${formatDate(record.appointmentDate)}\n症状：${record.symptoms || '无'}\n诊断：${record.diagnosis || '待诊断'}\n处方：${record.prescription || '无'}`,
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

.section {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333333;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20rpx;
}

.info-item {
  background: #f8f9fa;
  border-radius: 16rpx;
  padding: 20rpx;
}

.info-item.full {
  grid-column: span 2;
}

.label {
  font-size: 26rpx;
  color: #999999;
  display: block;
  margin-bottom: 8rpx;
}

.value {
  font-size: 32rpx;
  color: #333333;
  display: block;
}

.value.phone {
  color: #43a047;
}

.health-stats {
  display: flex;
  gap: 20rpx;
  margin-bottom: 24rpx;
}

.stat-item {
  flex: 1;
  background: linear-gradient(135deg, #43a047, #66bb6a);
  border-radius: 16rpx;
  padding: 30rpx;
  text-align: center;
}

.stat-value {
  font-size: 48rpx;
  font-weight: bold;
  color: #ffffff;
  display: block;
}

.stat-label {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.9);
  display: block;
  margin-top: 8rpx;
}

.health-info-list {
  background: #f8f9fa;
  border-radius: 16rpx;
  padding: 20rpx;
}

.health-info-item {
  display: flex;
  justify-content: space-between;
  padding: 16rpx 0;
  border-bottom: 1rpx solid #eee;
}

.health-info-item:last-child {
  border-bottom: none;
}

.record-item {
  background: #f8f9fa;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}

.record-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.record-type {
  font-size: 32rpx;
  font-weight: bold;
  color: #333333;
}

.record-status {
  font-size: 26rpx;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
}

.status-0 { background: #fff7e6; color: #fa8c16; }
.status-1 { background: #e6f7ff; color: #1890ff; }
.status-2 { background: #f6ffed; color: #52c41a; }

.record-date, .record-symptoms, .record-doctor, .record-diagnosis {
  font-size: 28rpx;
  color: #666666;
  display: block;
  margin-top: 8rpx;
}

.empty {
  text-align: center;
  padding: 60rpx 0;
  color: #999999;
  font-size: 28rpx;
}
</style>
