<template>
  <view class="container">
    <!-- 日期选择 -->
    <view class="form-section">
      <view class="section-title">选择预约日期</view>
      <scroll-view scroll-x class="date-scroll">
        <view class="date-list">
          <view 
            v-for="item in dateList" 
            :key="item.date"
            class="date-item"
            :class="{ 'active': form.date === item.date }"
            @click="selectDate(item.date)"
          >
            <text class="date-week">{{ item.week }}</text>
            <text class="date-day">{{ item.day }}</text>
          </view>
        </view>
      </scroll-view>
    </view>
    
    <!-- 时间段选择 -->
    <view class="form-section">
      <view class="section-title">选择预约时间段</view>
      <view class="time-slots" v-if="timeSlots.length > 0">
        <view 
          v-for="slot in timeSlots" 
          :key="slot.id"
          class="time-slot"
          :class="{ 
            'active': form.timeSlotId === slot.id,
            'disabled': slot.isFull
          }"
          @click="selectTimeSlot(slot)"
        >
          <text class="slot-time">{{ slot.displayText }}</text>
          <text class="slot-capacity" :class="{ 'full': slot.isFull }">
            {{ slot.isFull ? '已约满' : '剩余' + slot.remainingCapacity + '个' }}
          </text>
        </view>
      </view>
      <view class="empty-tip" v-else-if="!loading">
        <text>暂无可预约时间段</text>
      </view>
      <view class="empty-tip" v-else>
        <text>加载中...</text>
      </view>
    </view>
    
    <!-- 地址信息 -->
    <view class="form-section">
      <view class="section-title">服务地址</view>
      <view class="form-item">
        <textarea 
          class="textarea" 
          v-model="form.address" 
          placeholder="请输入详细服务地址"
          :maxlength="200"
        />
      </view>
    </view>
    
    <!-- 医疗预约健康信息 -->
    <view class="form-section" v-if="isMedical">
      <view class="section-title">健康信息（医疗预约必填）</view>
      <view class="form-item">
        <text class="label">症状描述</text>
        <textarea 
          class="textarea" 
          v-model="healthInfo.symptoms" 
          placeholder="请描述您的症状"
          :maxlength="500"
        />
      </view>
      <view class="form-item">
        <text class="label">过敏史</text>
        <textarea 
          class="textarea small" 
          v-model="healthInfo.allergies" 
          placeholder="请填写过敏史，如无请填写'无'"
          :maxlength="200"
        />
      </view>
      <view class="form-item">
        <text class="label">常服药品</text>
        <textarea 
          class="textarea small" 
          v-model="healthInfo.medications" 
          placeholder="请填写常服药品，如无请填写'无'"
          :maxlength="200"
        />
      </view>
    </view>
    
    <!-- 备注 -->
    <view class="form-section">
      <view class="section-title">备注信息（选填）</view>
      <view class="form-item">
        <textarea 
          class="textarea small" 
          v-model="form.remark" 
          placeholder="请输入备注信息"
          :maxlength="200"
        />
      </view>
    </view>
    
    <view class="submit-section">
      <view class="submit-btn" :class="{ 'disabled': !canSubmit }" @click="submitOrder">
        确认预约
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { get, post } from '@/utils/request'
import { onLoad } from '@dcloudio/uni-app'

const form = reactive({
  date: '',
  timeSlotId: null,
  address: '',
  remark: ''
})

const healthInfo = reactive({
  symptoms: '',
  allergies: '',
  medications: ''
})

const serviceId = ref('')
const categoryId = ref('')
const elderlyId = ref('')
const isProxy = ref(false)
const isMedical = ref(false)
const timeSlots = ref([])
const dateList = ref([])
const loading = ref(false)
const submitting = ref(false)

// 生成未来7天的日期列表
const generateDateList = () => {
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const list = []
  const today = new Date()
  
  for (let i = 0; i < 7; i++) {
    const date = new Date(today)
    date.setDate(today.getDate() + i)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const dateStr = `${year}-${month}-${day}`
    list.push({
      date: dateStr,
      day: `${date.getMonth() + 1}/${date.getDate()}`,
      week: i === 0 ? '今天' : (i === 1 ? '明天' : weekDays[date.getDay()])
    })
  }
  dateList.value = list
  return list
}

// 是否可以提交
const canSubmit = computed(() => {
  if (!form.date || !form.timeSlotId || !form.address) {
    return false
  }
  if (isMedical.value && !healthInfo.symptoms) {
    return false
  }
  return true
})

// 加载时间段
const loadTimeSlots = async (date) => {
  if (!serviceId.value || !date) return
  
  loading.value = true
  try {
    const data = await get(`/service/items/${serviceId.value}/time-slots`, { date })
    timeSlots.value = data || []
    // 重置时间段选择
    form.timeSlotId = null
  } catch (e) {
    console.error('加载时间段失败:', e)
    timeSlots.value = []
  } finally {
    loading.value = false
  }
}

// 选择日期
const selectDate = async (date) => {
  form.date = date
  await loadTimeSlots(date)
}

// 选择时间段
const selectTimeSlot = (slot) => {
  if (slot.isFull) {
    uni.showToast({ title: '该时间段已约满', icon: 'none' })
    return
  }
  form.timeSlotId = slot.id
}

// 提交订单
const submitOrder = async () => {
  if (!canSubmit.value) {
    if (!form.date || !form.timeSlotId) {
      uni.showToast({ title: '请选择预约时间', icon: 'none' })
    } else if (!form.address) {
      uni.showToast({ title: '请输入服务地址', icon: 'none' })
    } else if (isMedical.value && !healthInfo.symptoms) {
      uni.showToast({ title: '请填写症状描述', icon: 'none' })
    }
    return
  }
  
  if (submitting.value) return
  submitting.value = true
  
  try {
    const orderData = {
      serviceItemId: serviceId.value,
      timeSlotId: form.timeSlotId,
      appointmentDate: form.date,
      address: form.address,
      remark: form.remark
    }
    
    if (elderlyId.value) {
      orderData.elderlyId = elderlyId.value
    }
    
    // 医疗预约添加健康信息
    if (isMedical.value) {
      orderData.healthInfo = {
        symptoms: healthInfo.symptoms,
        allergies: healthInfo.allergies || '无',
        medications: healthInfo.medications || '无'
      }
    }
    
    await post('/order/book', orderData)
    
    uni.showModal({
      title: '预约成功',
      content: '您的预约已提交，请等待服务人员接单',
      showCancel: false,
      success: () => {
        uni.navigateBack()
      }
    })
  } catch (e) {
    console.error('预约失败:', e)
    uni.showToast({ title: e.message || '预约失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

onLoad(async (options) => {
  console.log('book页面参数:', options)
  serviceId.value = options.id || ''
  categoryId.value = options.categoryId || ''
  elderlyId.value = options.elderlyId || ''
  isProxy.value = options.proxy === '1'
  
  // 生成日期列表
  const dates = generateDateList()
  
  // 默认选中今天并加载时间段
  if (dates.length > 0) {
    form.date = dates[0].date
    await loadTimeSlots(form.date)
  }
  
  // 检查是否为医疗类服务
  if (categoryId.value) {
    try {
      const result = await get(`/service/categories/${categoryId.value}/is-medical`)
      isMedical.value = result === true
    } catch (e) {
      console.error('检查医疗类型失败:', e)
    }
  }
})
</script>

<style scoped>
.container {
  padding: 30rpx;
  background: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 200rpx;
}

.form-section {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
}

.section-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #333333;
  margin-bottom: 30rpx;
}

.date-scroll {
  white-space: nowrap;
}

.date-list {
  display: flex;
  gap: 20rpx;
}

.date-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24rpx 36rpx;
  background: #f5f5f5;
  border-radius: 16rpx;
  min-width: 120rpx;
}

.date-item.active {
  background: #43a047;
}

.date-week {
  font-size: 28rpx;
  color: #666666;
}

.date-day {
  font-size: 32rpx;
  color: #333333;
  margin-top: 8rpx;
  font-weight: 500;
}

.date-item.active .date-week,
.date-item.active .date-day {
  color: #ffffff;
}

.time-slots {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.time-slot {
  width: calc(50% - 10rpx);
  padding: 30rpx 20rpx;
  background: #f5f5f5;
  border-radius: 16rpx;
  text-align: center;
  border: 2rpx solid transparent;
}

.time-slot.active {
  background: #e8f5e9;
  border-color: #43a047;
}

.time-slot.disabled {
  background: #f0f0f0;
  opacity: 0.6;
}

.slot-time {
  font-size: 34rpx;
  color: #333333;
  display: block;
  font-weight: 500;
}

.slot-capacity {
  font-size: 26rpx;
  color: #43a047;
  display: block;
  margin-top: 10rpx;
}

.slot-capacity.full {
  color: #ff4d4f;
}

.time-slot.active .slot-time {
  color: #43a047;
}

.empty-tip {
  text-align: center;
  padding: 60rpx 0;
  color: #999999;
  font-size: 32rpx;
}

.form-item {
  margin-bottom: 30rpx;
}

.form-item:last-child {
  margin-bottom: 0;
}

.label {
  font-size: 32rpx;
  color: #333333;
  display: block;
  margin-bottom: 16rpx;
}

.textarea {
  width: 100%;
  height: 180rpx;
  background: #f5f5f5;
  border-radius: 16rpx;
  padding: 24rpx;
  font-size: 34rpx;
  box-sizing: border-box;
}

.textarea.small {
  height: 120rpx;
}

.submit-section {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 30rpx;
  background: #ffffff;
  padding-bottom: calc(30rpx + env(safe-area-inset-bottom));
}

.submit-btn {
  background: #43a047;
  color: #ffffff;
  text-align: center;
  padding: 36rpx;
  border-radius: 20rpx;
  font-size: 40rpx;
  font-weight: bold;
}

.submit-btn.disabled {
  background: #cccccc;
}
</style>
