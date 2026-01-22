<template>
  <view class="container">
    <!-- 代预约提示 -->
    <view class="proxy-banner" v-if="isProxy && elderlyInfo">
      <text class="proxy-icon">👴</text>
      <text class="proxy-text">正在为 <text class="proxy-name">{{ elderlyInfo.name }}</text> 代预约医疗服务</text>
    </view>

    <!-- 功能切换 -->
    <view class="func-tabs">
      <view class="func-tab" :class="{ active: currentTab === 'book' }" @click="currentTab = 'book'">预约巡诊</view>
      <view class="func-tab" :class="{ active: currentTab === 'orders' }" @click="switchToOrders" v-if="!isProxy">我的预约</view>
    </view>

    <!-- 预约巡诊 -->
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

      <!-- 医生列表 -->
      <view class="section-title">选择医生</view>
      <view class="doctor-list" v-if="doctorList.length > 0">
        <view 
          class="doctor-card" 
          v-for="doc in doctorList" 
          :key="doc.doctorId"
        >
          <view class="doctor-header">
            <view class="doctor-info">
              <view class="doctor-name-row">
                <text class="doctor-name">{{ doc.name }}</text>
                <view class="night-tag" v-if="doc.isNightShift">夜间急诊</view>
              </view>
              <text class="doctor-title">{{ doc.title }}</text>
              <text class="doctor-specialty">{{ doc.specialty }}</text>
            </view>
          </view>
          
          <view class="doctor-status">
            <view class="queue-info">
              <text class="queue-label">当前排队</text>
              <text class="queue-count" :class="{ warning: doc.currentQueueCount >= doc.maxQueueLimit * 0.8 }">
                {{ doc.currentQueueCount }}/{{ doc.maxQueueLimit }}
              </text>
            </view>
            <view class="duty-time">
              <text>值班时间：{{ doc.startTime }} - {{ doc.endTime }}</text>
            </view>
          </view>

          <view class="doctor-action">
            <view 
              class="book-btn" 
              :class="{ disabled: !doc.canBook }"
              @click="bookDoctor(doc)"
            >
              {{ getBookBtnText(doc) }}
            </view>
          </view>
        </view>
      </view>
      <view class="empty-tip" v-else>
        <text>当日暂无值班医生</text>
      </view>
    </view>

    <!-- 我的预约 -->
    <view v-if="currentTab === 'orders'">
      <view class="order-list" v-if="orderList.length > 0">
        <view class="order-card" v-for="order in orderList" :key="order.id">
          <view class="order-header">
            <text class="order-doctor">{{ order.doctor?.name || '医生' }} - {{ order.doctor?.title || '' }}</text>
            <text class="order-status" :class="'status-' + order.status">{{ getStatusText(order.status) }}</text>
          </view>
          <view class="order-body">
            <text class="order-info">预约日期：{{ order.appointmentDate }}</text>
            <text class="order-info">排队号：{{ order.queueNumber }}</text>
            <text class="order-code" v-if="order.serviceCode">服务码：{{ order.serviceCode }}</text>
            <text class="order-symptoms" v-if="order.symptoms">症状：{{ order.symptoms }}</text>
            <text class="order-diagnosis" v-if="order.diagnosis">诊断：{{ order.diagnosis }}</text>
          </view>
          <view class="order-footer" v-if="order.status === 0">
            <view class="cancel-btn" @click="cancelOrder(order)">取消预约</view>
          </view>
        </view>
      </view>
      <view class="empty-tip" v-else>
        <text>暂无预约记录</text>
      </view>
    </view>

    <!-- 预约弹窗 -->
    <view class="modal" v-if="showModal" @click="showModal = false">
      <view class="modal-content" @click.stop>
        <view class="modal-title">预约巡诊</view>
        <view class="modal-body">
          <view class="form-item">
            <text class="form-label">症状描述 <text class="required">*</text></text>
            <textarea 
              class="form-textarea" 
              v-model="bookingForm.symptoms" 
              placeholder="请描述您的症状（将归档至健康档案）..."
            />
          </view>
          <view class="form-item">
            <text class="form-label">巡诊地址</text>
            <input 
              class="form-input" 
              v-model="bookingForm.address" 
              placeholder="请输入地址"
            />
          </view>
          <view class="form-item">
            <text class="form-label">备注</text>
            <input 
              class="form-input" 
              v-model="bookingForm.remark" 
              placeholder="其他需要说明的情况"
            />
          </view>
        </view>
        <view class="modal-footer">
          <view class="modal-btn cancel" @click="showModal = false">取消</view>
          <view class="modal-btn confirm" @click="submitBooking">确认预约</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { get, post } from '@/utils/request'
import { onLoad } from '@dcloudio/uni-app'

const currentTab = ref('book')
const dateList = ref([])
const selectedDate = ref('')
const doctorList = ref([])
const orderList = ref([])
const showModal = ref(false)
const selectedDoctor = ref(null)
const bookingForm = ref({
  symptoms: '',
  address: '',
  remark: ''
})

// 代预约相关
const isProxy = ref(false)
const elderlyId = ref('')
const elderlyInfo = ref(null)

const getStatusText = (status) => ({ 0: '待巡诊', 1: '巡诊中', 2: '已完成', 3: '已取消' }[status] || '-')

onLoad(async (options) => {
  // 处理代预约参数
  if (options.proxy === '1' && options.elderlyId) {
    isProxy.value = true
    elderlyId.value = options.elderlyId
    elderlyInfo.value = uni.getStorageSync('selectedElderly') || { name: '老人' }
  }
})

onMounted(() => {
  initDateList()
  loadDoctors()
})

watch(selectedDate, () => {
  loadDoctors()
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

const loadDoctors = async () => {
  try {
    const data = await get('/medical/doctors', { date: selectedDate.value })
    doctorList.value = data || []
  } catch (e) {
    console.error('获取医生列表失败:', e)
  }
}

const loadOrders = async () => {
  try {
    const data = await get('/medical/appointments')
    orderList.value = data || []
  } catch (e) {
    console.error('获取预约列表失败:', e)
  }
}

const switchToOrders = async () => {
  currentTab.value = 'orders'
  await loadOrders()
}

const selectDate = (date) => {
  selectedDate.value = date
}

const getBookBtnText = (doc) => {
  if (doc.isNightShift) {
    return '预约急诊'
  }
  if (doc.isOpen) {
    return '已熔断'
  }
  if (!doc.canBook) {
    return doc.currentQueueCount >= doc.maxQueueLimit ? '已满' : '非接诊时段'
  }
  return '预约巡诊'
}

const bookDoctor = (doc) => {
  if (!doc.canBook) {
    let msg = '暂时无法预约'
    if (doc.isOpen) {
      msg = '医生已熔断，暂停接诊'
    } else if (doc.currentQueueCount >= doc.maxQueueLimit) {
      msg = '排队已满'
    } else {
      msg = '非接诊时段（日间8:00-20:00）'
    }
    uni.showToast({ title: msg, icon: 'none' })
    return
  }
  selectedDoctor.value = doc
  showModal.value = true
}

const submitBooking = async () => {
  if (!bookingForm.value.symptoms) {
    uni.showToast({ title: '请描述症状', icon: 'none' })
    return
  }

  try {
    const orderData = {
      doctorId: selectedDoctor.value.doctorId,
      appointmentDate: selectedDate.value,
      appointmentType: selectedDoctor.value.isNightShift ? 2 : 1,
      symptoms: bookingForm.value.symptoms,
      address: bookingForm.value.address,
      remark: bookingForm.value.remark
    }
    
    // 代预约时传递老人ID
    if (isProxy.value && elderlyId.value) {
      orderData.elderlyId = elderlyId.value
    }
    
    await post('/medical/appointment', orderData)
    uni.showToast({ title: '预约成功', icon: 'success' })
    showModal.value = false
    bookingForm.value = { symptoms: '', address: '', remark: '' }
    await loadDoctors()
    
    if (isProxy.value) {
      setTimeout(() => uni.navigateBack(), 1500)
    }
  } catch (e) {
    console.error('预约失败:', e)
  }
}

const cancelOrder = async (order) => {
  uni.showModal({
    title: '取消预约',
    content: '确定要取消该预约吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await post(`/medical/appointment/${order.id}/cancel`)
          uni.showToast({ title: '已取消', icon: 'success' })
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

.doctor-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
}

.doctor-header {
  display: flex;
  align-items: center;
  padding-bottom: 20rpx;
  border-bottom: 1rpx solid #eee;
}

.doctor-info { flex: 1; }
.doctor-name-row { display: flex; align-items: center; gap: 16rpx; }
.doctor-name { font-size: 36rpx; font-weight: bold; }
.night-tag {
  background: #ff6b6b;
  color: #fff;
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
}
.doctor-title { font-size: 28rpx; color: #43a047; margin-top: 8rpx; display: block; }
.doctor-specialty { font-size: 26rpx; color: #666; margin-top: 8rpx; display: block; }

.doctor-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
}

.queue-info { display: flex; align-items: center; }
.queue-label { font-size: 28rpx; color: #666; margin-right: 16rpx; }
.queue-count { font-size: 32rpx; font-weight: bold; color: #43a047; }
.queue-count.warning { color: #ff9800; }
.duty-time { font-size: 26rpx; color: #999; }

.doctor-action { padding-top: 20rpx; }

.book-btn {
  background: #43a047;
  color: #fff;
  text-align: center;
  padding: 24rpx;
  border-radius: 16rpx;
  font-size: 34rpx;
}

.book-btn.disabled {
  background: #ccc;
}

.empty-tip {
  text-align: center;
  padding: 100rpx;
  color: #999;
  font-size: 32rpx;
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

.order-doctor {
  font-size: 32rpx;
  font-weight: bold;
}

.order-status {
  font-size: 28rpx;
  font-weight: bold;
}

.order-status.status-0 { color: #ff9800; }
.order-status.status-1 { color: #2196f3; }
.order-status.status-2 { color: #4caf50; }
.order-status.status-3 { color: #999; }

.order-body {
  margin-bottom: 20rpx;
}

.order-info {
  font-size: 28rpx;
  color: #666;
  display: block;
  margin-bottom: 10rpx;
}

.order-symptoms {
  font-size: 28rpx;
  color: #333;
  display: block;
  margin-top: 10rpx;
}

.order-diagnosis {
  font-size: 28rpx;
  color: #43a047;
  display: block;
  margin-top: 10rpx;
}

.order-code {
  font-size: 30rpx;
  color: #ff6b6b;
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

/* 弹窗样式 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.modal-content {
  width: 90%;
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;
}

.modal-title {
  text-align: center;
  font-size: 36rpx;
  font-weight: bold;
  padding: 30rpx;
  border-bottom: 1rpx solid #eee;
}

.modal-body { padding: 30rpx; }

.form-item { margin-bottom: 30rpx; }
.form-label { font-size: 30rpx; color: #333; display: block; margin-bottom: 16rpx; }
.form-label .required { color: #ff4d4f; }
.form-input {
  width: 100%;
  padding: 24rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  font-size: 30rpx;
}

.form-textarea {
  width: 100%;
  height: 200rpx;
  padding: 24rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  font-size: 30rpx;
}

.modal-footer {
  display: flex;
  border-top: 1rpx solid #eee;
}

.modal-btn {
  flex: 1;
  text-align: center;
  padding: 30rpx;
  font-size: 34rpx;
}

.modal-btn.cancel { color: #666; }
.modal-btn.confirm { color: #43a047; font-weight: bold; }
</style>
