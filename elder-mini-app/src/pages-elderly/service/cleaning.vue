<template>
  <view class="container">
    <!-- 代预约提示 -->
    <view class="proxy-banner" v-if="isProxy && elderlyInfo">
      <text class="proxy-icon">👴</text>
      <text class="proxy-text">正在为 <text class="proxy-name">{{ elderlyInfo.name }}</text> 代预约保洁服务</text>
    </view>

    <!-- 功能切换 -->
    <view class="func-tabs">
      <view class="func-tab" :class="{ active: currentTab === 'book' }" @click="currentTab = 'book'">预约保洁</view>
      <view class="func-tab" :class="{ active: currentTab === 'orders' }" @click="switchToOrders" v-if="!isProxy">我的订单</view>
    </view>

    <!-- 预约保洁 -->
    <view v-if="currentTab === 'book'">
      <!-- 日期选择 -->
      <view class="date-section">
        <view class="date-title">选择日期</view>
        <scroll-view scroll-x class="date-scroll">
          <view 
            class="date-item" 
            v-for="(d, idx) in dateList" 
            :key="idx"
            :class="{ active: selectedDate === d.date }"
            @click="selectDate(d.date)"
          >
            <text class="date-week">{{ d.week }}</text>
            <text class="date-day">{{ d.day }}</text>
          </view>
        </scroll-view>
      </view>

      <!-- 服务项目选择 -->
      <view class="section-title">选择服务</view>
      <view class="service-list">
        <view 
          class="service-item" 
          v-for="s in serviceList" 
          :key="s.id"
          :class="{ active: selectedService?.id === s.id }"
          @click="selectService(s)"
        >
          <text class="service-name">{{ s.name }}</text>
          <text class="service-duration">参考耗时：约{{ s.referenceDuration }}分钟</text>
          <text class="service-price">{{ s.pricePer30min }}积分/30分钟</text>
        </view>
      </view>

      <!-- 保洁员选择 -->
      <view class="section-title">选择保洁员</view>
      <view class="worker-list" v-if="workerList.length > 0">
        <view 
          class="worker-card" 
          v-for="w in workerList" 
          :key="w.workerId"
          :class="{ 
            active: selectedWorker?.workerId === w.workerId,
            disabled: w.offDuty || (w.availableSlots && w.availableSlots.length === 0)
          }"
          @click="selectWorker(w)"
        >
          <view class="worker-info">
            <text class="worker-name">{{ w.workerName }}</text>
            <text class="worker-time">{{ w.scheduleStartTime }} - {{ w.scheduleEndTime }}</text>
            <view class="worker-status" v-if="w.offDuty">
              <text class="status-tag off-duty">已下班</text>
            </view>
            <view class="worker-status" v-else-if="w.availableSlots && w.availableSlots.length === 0">
              <text class="status-tag fully-booked">已约满</text>
            </view>
          </view>
          <!-- 显示已预约时段 -->
          <view class="booked-slots" v-if="w.bookedSlots && w.bookedSlots.length > 0 && !w.offDuty">
            <text class="booked-label">已预约时段：</text>
            <view class="slot-tags">
              <text class="slot-tag" v-for="(slot, idx) in w.bookedSlots" :key="idx">
                {{ formatTime(slot.startTime) }}-{{ formatTime(slot.endTime) }}
              </text>
            </view>
          </view>
        </view>
      </view>
      <view class="empty-tip" v-else>
        <text>当日暂无可预约保洁员</text>
      </view>

      <!-- 时间选择 -->
      <view class="section-title" v-if="selectedWorker">选择时间</view>
      <view class="time-section" v-if="selectedWorker">
        <picker mode="time" :value="startTime" @change="onTimeChange">
          <view class="time-picker">
            <text>开始时间：{{ startTime || '请选择' }}</text>
          </view>
        </picker>
        <view class="duration-section">
          <text>服务时长：</text>
          <view class="duration-btns">
            <view 
              class="duration-btn" 
              v-for="d in [30, 60, 90, 120]" 
              :key="d"
              :class="{ active: duration === d }"
              @click="duration = d"
            >
              {{ d }}分钟
            </view>
          </view>
        </view>
      </view>

      <!-- 预约按钮 -->
      <view class="book-section" v-if="selectedService && selectedWorker && startTime">
        <view class="total-price">
          预计消耗：<text class="price">{{ totalPrice }}</text> 积分
        </view>
        <view class="book-btn" @click="submitBooking">确认预约</view>
      </view>
    </view>

    <!-- 我的订单 -->
    <view v-if="currentTab === 'orders'">
      <view class="order-list" v-if="orderList.length > 0">
        <view class="order-card" v-for="order in orderList" :key="order.id">
          <view class="order-header">
            <text class="order-no">{{ order.service?.name || '保洁服务' }}</text>
            <text class="order-status" :class="'status-' + order.status">{{ getStatusText(order.status) }}</text>
          </view>
          <view class="order-body">
            <text class="order-info">{{ order.serviceDate }} {{ order.startTime }}-{{ order.endTime }}</text>
            <text class="order-worker">保洁员：{{ order.worker?.name || '-' }}</text>
            <text class="order-amount">{{ order.amount }}积分</text>
            <text class="order-code" v-if="order.status === 0">服务码：{{ order.serviceCode }}</text>
          </view>
          <view class="order-footer" v-if="order.status === 0">
            <view class="cancel-btn" @click="cancelOrder(order)">取消订单</view>
          </view>
        </view>
      </view>
      <view class="empty-tip" v-else>
        <text>暂无订单</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { get, post } from '@/utils/request'
import { onLoad } from '@dcloudio/uni-app'

const currentTab = ref('book')
const dateList = ref([])
const selectedDate = ref('')
const serviceList = ref([])
const selectedService = ref(null)
const workerList = ref([])
const selectedWorker = ref(null)
const startTime = ref('')
const duration = ref(60)
const orderList = ref([])

// 代预约相关
const isProxy = ref(false)
const elderlyId = ref('')
const elderlyInfo = ref(null)

const totalPrice = computed(() => {
  if (!selectedService.value) return 0
  return selectedService.value.pricePer30min * (duration.value / 30)
})

const getStatusText = (status) => ({ 0: '待服务', 1: '服务中', 2: '待确认', 3: '已完成', 4: '已取消' }[status] || '-')

onLoad(async (options) => {
  // 处理代预约参数
  if (options.proxy === '1' && options.elderlyId) {
    isProxy.value = true
    elderlyId.value = options.elderlyId
    elderlyInfo.value = uni.getStorageSync('selectedElderly') || { name: '老人' }
  }
})

onMounted(async () => {
  initDateList()
  await loadServices()
  await loadWorkers()
})

watch(selectedDate, () => {
  loadWorkers()
  selectedWorker.value = null
  startTime.value = ''
})

const initDateList = () => {
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const list = []
  for (let i = 0; i < 7; i++) {
    const d = new Date()
    d.setDate(d.getDate() + i)
    list.push({
      date: formatDate(d),
      week: i === 0 ? '今天' : weekDays[d.getDay()],
      day: `${d.getMonth() + 1}/${d.getDate()}`
    })
  }
  dateList.value = list
  selectedDate.value = list[0].date
}

const formatDate = (d) => {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

const loadServices = async () => {
  try {
    const data = await get('/cleaning/services')
    serviceList.value = data || []
    if (serviceList.value.length > 0) {
      selectedService.value = serviceList.value[0]
    }
  } catch (e) {
    console.error('获取服务列表失败:', e)
  }
}

const loadWorkers = async () => {
  try {
    const data = await get('/cleaning/workers', { date: selectedDate.value })
    workerList.value = data || []
  } catch (e) {
    console.error('获取保洁员列表失败:', e)
  }
}

const loadOrders = async () => {
  try {
    const data = await get('/cleaning/orders')
    orderList.value = data || []
  } catch (e) {
    console.error('获取订单失败:', e)
  }
}

const switchToOrders = async () => {
  currentTab.value = 'orders'
  await loadOrders()
}

const selectDate = (date) => { selectedDate.value = date }
const selectService = (s) => { selectedService.value = s }
const selectWorker = (w) => { 
  // 检查是否可选
  if (w.offDuty) {
    uni.showToast({ title: '该保洁员今日已下班', icon: 'none' })
    return
  }
  if (w.availableSlots && w.availableSlots.length === 0) {
    uni.showToast({ title: '该保洁员今日已约满', icon: 'none' })
    return
  }
  selectedWorker.value = w 
}
const onTimeChange = (e) => { startTime.value = e.detail.value }

// 格式化时间显示
const formatTime = (time) => {
  if (!time) return ''
  // 处理 "HH:mm:ss" 或 "HH:mm" 格式
  return time.substring(0, 5)
}

const submitBooking = async () => {
  // 验证预约时间不能是过去的时间
  const now = new Date()
  const bookingDateTime = new Date(`${selectedDate.value} ${startTime.value}`)
  if (bookingDateTime <= now) {
    uni.showToast({ title: '预约时间不能早于当前时间', icon: 'none' })
    return
  }
  
  // 验证预约时间是否在保洁员工作时间范围内
  const worker = selectedWorker.value
  const startTimeStr = startTime.value
  const scheduleStart = worker.scheduleStartTime?.substring(0, 5) || '08:00'
  const scheduleEnd = worker.scheduleEndTime?.substring(0, 5) || '18:00'
  
  if (startTimeStr < scheduleStart) {
    uni.showToast({ title: `预约时间不能早于保洁员上班时间（${scheduleStart}）`, icon: 'none' })
    return
  }
  
  // 计算结束时间
  const [hours, minutes] = startTimeStr.split(':').map(Number)
  const endMinutes = hours * 60 + minutes + duration.value
  const endHours = Math.floor(endMinutes / 60)
  const endMins = endMinutes % 60
  const endTimeStr = `${String(endHours).padStart(2, '0')}:${String(endMins).padStart(2, '0')}`
  
  if (endTimeStr > scheduleEnd) {
    uni.showToast({ title: `预约结束时间不能晚于保洁员下班时间（${scheduleEnd}）`, icon: 'none' })
    return
  }
  
  // 检查是否与已预约时段冲突
  if (worker.bookedSlots && worker.bookedSlots.length > 0) {
    for (const slot of worker.bookedSlots) {
      const slotStart = slot.startTime?.substring(0, 5)
      const slotEnd = slot.endTime?.substring(0, 5)
      // 检查时间段是否重叠
      if (!(endTimeStr <= slotStart || startTimeStr >= slotEnd)) {
        uni.showToast({ title: `该时间段（${slotStart}-${slotEnd}）已被预约`, icon: 'none' })
        return
      }
    }
  }
  
  uni.showModal({
    title: '确认预约',
    content: `预约${selectedService.value.name}，消耗${totalPrice.value}积分`,
    success: async (res) => {
      if (res.confirm) {
        try {
          const orderData = {
            workerId: selectedWorker.value.workerId,
            serviceId: selectedService.value.id,
            serviceDate: selectedDate.value,
            startTime: startTime.value + ':00',
            duration: duration.value
          }
          
          // 代预约时传递老人ID
          if (isProxy.value && elderlyId.value) {
            orderData.elderlyId = elderlyId.value
          }
          
          await post('/cleaning/order', orderData)
          uni.showToast({ title: '预约成功', icon: 'success' })
          setTimeout(() => {
            if (isProxy.value) {
              uni.navigateBack()
            } else {
              currentTab.value = 'orders'
              loadOrders()
            }
          }, 1500)
        } catch (e) {
          console.error('预约失败:', e)
        }
      }
    }
  })
}

const cancelOrder = async (order) => {
  uni.showModal({
    title: '取消订单',
    content: '服务开始前30分钟免费取消，30分钟内取消扣除5%积分',
    success: async (res) => {
      if (res.confirm) {
        try {
          const result = await post(`/cleaning/order/${order.id}/cancel`)
          uni.showToast({ title: `已取消，退还${result.refund}积分`, icon: 'none', duration: 2000 })
          await loadOrders()
        } catch (e) {
          console.error('取消失败:', e)
        }
      }
    }
  })
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 30rpx;
  padding-bottom: 200rpx;
}

.proxy-banner {
  background: linear-gradient(135deg, #43a047, #66bb6a);
  border-radius: 20rpx;
  padding: 30rpx;
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
  color: #fff;
}

.proxy-icon {
  font-size: 50rpx;
  margin-right: 20rpx;
}

.proxy-text {
  font-size: 30rpx;
}

.proxy-name {
  font-weight: bold;
  font-size: 34rpx;
}

.date-section { margin-bottom: 30rpx; }
.date-title, .section-title { font-size: 32rpx; font-weight: bold; margin-bottom: 20rpx; }
.date-scroll { white-space: nowrap; }

.date-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx 30rpx;
  background: #fff;
  border-radius: 16rpx;
  margin-right: 20rpx;
}

.date-item.active { background: #43a047; color: #fff; }
.date-week { font-size: 28rpx; }
.date-day { font-size: 32rpx; font-weight: bold; margin-top: 8rpx; }

.service-list {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
  margin-bottom: 30rpx;
}

.service-item {
  background: #fff;
  padding: 24rpx 32rpx;
  border-radius: 16rpx;
  border: 2rpx solid #eee;
}

.service-item.active { border-color: #43a047; background: #e8f5e9; }
.service-name { font-size: 30rpx; font-weight: bold; display: block; }
.service-duration { font-size: 24rpx; color: #ff9800; margin-top: 6rpx; display: block; }
.service-price { font-size: 26rpx; color: #666; margin-top: 6rpx; display: block; }

.worker-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  display: flex;
  flex-direction: column;
  border: 2rpx solid #eee;
}

.worker-card.active { border-color: #43a047; background: #e8f5e9; }
.worker-card.disabled { 
  background: #f5f5f5; 
  opacity: 0.7;
  border-color: #ddd;
}
.worker-info { flex: 1; }
.worker-name { font-size: 34rpx; font-weight: bold; display: block; }
.worker-time { font-size: 28rpx; color: #666; margin-top: 8rpx; display: block; }

.worker-status {
  margin-top: 10rpx;
}

.status-tag {
  font-size: 24rpx;
  padding: 4rpx 16rpx;
  border-radius: 8rpx;
  display: inline-block;
}

.status-tag.off-duty {
  background: #ffebee;
  color: #f44336;
}

.status-tag.fully-booked {
  background: #fff3e0;
  color: #ff9800;
}

.booked-slots {
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1rpx solid #eee;
}

.booked-label {
  font-size: 26rpx;
  color: #999;
  display: block;
  margin-bottom: 10rpx;
}

.slot-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}

.slot-tag {
  font-size: 24rpx;
  padding: 6rpx 16rpx;
  background: #ffebee;
  color: #f44336;
  border-radius: 8rpx;
}

.time-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
}

.time-picker {
  padding: 20rpx 0;
  font-size: 32rpx;
  border-bottom: 1rpx solid #eee;
}

.duration-section {
  padding-top: 20rpx;
  font-size: 32rpx;
}

.duration-btns {
  display: flex;
  gap: 20rpx;
  margin-top: 16rpx;
}

.duration-btn {
  padding: 16rpx 32rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  font-size: 28rpx;
}

.duration-btn.active { background: #43a047; color: #fff; }

.book-section {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 30rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 -4rpx 16rpx rgba(0,0,0,0.05);
}

.total-price { font-size: 32rpx; }
.price { color: #ff4d4f; font-weight: bold; font-size: 40rpx; }

.book-btn {
  background: #43a047;
  color: #fff;
  padding: 24rpx 60rpx;
  border-radius: 16rpx;
  font-size: 34rpx;
}

.empty-tip {
  text-align: center;
  padding: 60rpx;
  color: #999;
  font-size: 30rpx;
}

/* 功能切换 */
.func-tabs {
  display: flex;
  background: #fff;
  border-radius: 16rpx;
  padding: 8rpx;
  margin-bottom: 30rpx;
}

.func-tab {
  flex: 1;
  text-align: center;
  padding: 20rpx;
  font-size: 32rpx;
  border-radius: 12rpx;
  color: #666;
}

.func-tab.active {
  background: #43a047;
  color: #fff;
}

/* 订单列表 */
.order-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #eee;
}

.order-no {
  font-size: 32rpx;
  font-weight: bold;
}

.order-status {
  font-size: 28rpx;
  font-weight: bold;
}

.order-status.status-0 { color: #ff9800; }
.order-status.status-1 { color: #2196f3; }
.order-status.status-2 { color: #9c27b0; }
.order-status.status-3 { color: #4caf50; }
.order-status.status-4 { color: #999; }

.order-body {
  margin-bottom: 20rpx;
}

.order-info, .order-worker {
  font-size: 28rpx;
  color: #666;
  display: block;
  margin-bottom: 10rpx;
}

.order-amount {
  font-size: 32rpx;
  color: #ff4d4f;
  font-weight: bold;
  display: block;
}

.order-code {
  font-size: 28rpx;
  color: #43a047;
  display: block;
  margin-top: 10rpx;
  font-weight: bold;
}

.order-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 20rpx;
  border-top: 1rpx solid #eee;
}

.cancel-btn {
  padding: 16rpx 40rpx;
  border: 1rpx solid #ff4d4f;
  color: #ff4d4f;
  border-radius: 12rpx;
  font-size: 28rpx;
}
</style>
