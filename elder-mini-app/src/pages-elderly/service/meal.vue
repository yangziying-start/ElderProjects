<template>
  <view class="container">
    <!-- 代预约提示 -->
    <view class="proxy-banner" v-if="isProxy && elderlyInfo">
      <text class="proxy-icon">👴</text>
      <text class="proxy-text">正在为 <text class="proxy-name">{{ elderlyInfo.name }}</text> 代预约餐饮服务</text>
    </view>

    <!-- 积分显示 -->
    <view class="points-bar">
      <text class="points-label">{{ isProxy ? '老人积分' : '我的积分' }}</text>
      <text class="points-value">{{ userPoints }}</text>
    </view>

    <!-- 功能切换 -->
    <view class="func-tabs">
      <view class="func-tab" :class="{ active: currentTab === 'menu' }" @click="currentTab = 'menu'">预约点餐</view>
      <view class="func-tab" :class="{ active: currentTab === 'orders' }" @click="switchToOrders" v-if="!isProxy">我的订单</view>
    </view>

    <!-- 预约点餐 -->
    <view v-if="currentTab === 'menu'">
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

      <!-- 餐次选择 -->
      <view class="meal-tabs">
        <view 
          class="meal-tab" 
          v-for="m in mealTypes" 
          :key="m.value"
          :class="{ active: selectedMealType === m.value }"
          @click="selectMealType(m.value)"
        >
          {{ m.label }}
        </view>
      </view>

      <!-- 配送说明 -->
      <view class="delivery-notice">
        <text class="notice-icon">🚚</text>
        <text class="notice-text">系统将根据您的楼栋自动分配配送员</text>
      </view>

      <!-- 菜品列表 -->
      <view class="dish-list" v-if="filteredDishes.length > 0">
        <view class="dish-card" v-for="dish in filteredDishes" :key="dish.id">
          <view class="dish-info">
            <text class="dish-name">{{ dish.dishName }}</text>
            <text class="dish-desc" v-if="dish.description">{{ dish.description }}</text>
            <text class="dish-nutrition" v-if="dish.nutritionInfo">{{ dish.nutritionInfo }}</text>
          </view>
          <view class="dish-action">
            <text class="dish-price">{{ dish.price }}积分</text>
            <view class="book-btn" @click="bookDish(dish)">预约</view>
          </view>
        </view>
      </view>
      <view class="empty-tip" v-else>
        <text>暂无菜品</text>
      </view>
    </view>

    <!-- 我的订单 -->
    <view v-if="currentTab === 'orders'">
      <view class="order-list" v-if="orderList.length > 0">
        <view class="order-card" v-for="order in orderList" :key="order.id">
          <view class="order-header">
            <text class="order-no">订单号: {{ order.orderNo }}</text>
            <text class="order-status" :class="'status-' + order.status">{{ getStatusText(order.status) }}</text>
          </view>
          <view class="order-body">
            <text class="order-dish">{{ getOrderDishName(order) }}</text>
            <text class="order-remark" v-if="order.remark && !order.remark.startsWith('菜品:')">{{ order.remark }}</text>
            <text class="order-info">{{ order.dishDate }} {{ getMealTypeText(order.mealType) }}</text>
            <text class="order-amount">{{ order.amount }}积分 ({{ order.quantity }}份)</text>
            <text class="order-code" v-if="order.serviceCode">服务码：{{ order.serviceCode }}</text>
          </view>
          <view class="order-footer" v-if="order.status < 2">
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
import { ref, computed, onMounted } from 'vue'
import { get, post } from '@/utils/request'
import { onLoad } from '@dcloudio/uni-app'

const userPoints = ref(0)
const selectedDate = ref('')
const selectedMealType = ref(2)
const weeklyMenu = ref({})
const dateList = ref([])
const currentTab = ref('menu')
const orderList = ref([])

// 代预约相关
const isProxy = ref(false)
const elderlyId = ref('')
const elderlyInfo = ref(null)

const mealTypes = [
  { value: 1, label: '早餐' },
  { value: 2, label: '午餐' },
  { value: 3, label: '晚餐' }
]

const filteredDishes = computed(() => {
  const dishes = weeklyMenu.value.weeklyDishes?.[selectedDate.value] || []
  return dishes.filter(d => d.mealType === selectedMealType.value)
})

const getStatusText = (status) => ({ 0: '待配送', 1: '配送中', 2: '已送达', 3: '已取消' }[status] || '-')
const getMealTypeText = (type) => ({ 1: '早餐', 2: '午餐', 3: '晚餐' }[type] || '-')

// 获取订单菜品名称，处理合并订单的情况
const getOrderDishName = (order) => {
  // 如果remark以"菜品:"开头，说明是合并订单，显示remark中的菜品信息
  if (order.remark && order.remark.startsWith('菜品:')) {
    return order.remark.replace('菜品: ', '')
  }
  return order.dish?.dishName || '菜品'
}

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
  await loadPoints()
  await loadMenu()
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

const loadPoints = async () => {
  try {
    // 代预约时获取老人的积分
    const url = isProxy.value && elderlyId.value 
      ? `/meal/points?elderlyId=${elderlyId.value}` 
      : '/meal/points'
    const data = await get(url)
    userPoints.value = data?.points || 0
  } catch (e) {
    console.error('获取积分失败:', e)
  }
}

const loadMenu = async () => {
  try {
    const data = await get('/meal/menu', { date: selectedDate.value })
    weeklyMenu.value = data || {}
  } catch (e) {
    console.error('获取菜单失败:', e)
  }
}

const loadOrders = async () => {
  try {
    const data = await get('/meal/orders')
    orderList.value = data || []
  } catch (e) {
    console.error('获取订单失败:', e)
  }
}

const switchToOrders = async () => {
  currentTab.value = 'orders'
  await loadOrders()
}

const selectDate = async (date) => {
  selectedDate.value = date
  if (!weeklyMenu.value.weeklyDishes?.[date]) {
    await loadMenu()
  }
}

const selectMealType = (type) => {
  selectedMealType.value = type
}

const bookDish = async (dish) => {
  if (userPoints.value < dish.price) {
    uni.showToast({ title: '积分不足', icon: 'none' })
    return
  }

  uni.showModal({
    title: '确认预约',
    content: `预约${dish.dishName}，消耗${dish.price}积分`,
    success: async (res) => {
      if (res.confirm) {
        try {
          const orderData = {
            dishId: dish.id,
            dishDate: selectedDate.value,
            mealType: selectedMealType.value,
            quantity: 1
          }
          
          // 代预约时传递老人ID
          if (isProxy.value && elderlyId.value) {
            orderData.elderlyId = elderlyId.value
          }
          
          const result = await post('/meal/order', orderData)
          
          // 根据是否合并订单显示不同提示
          if (result.isMerged) {
            uni.showToast({ 
              title: `已合并到现有订单\n服务码: ${result.serviceCode}`, 
              icon: 'none',
              duration: 2500
            })
          } else {
            uni.showToast({ 
              title: `预约成功\n服务码: ${result.serviceCode}`, 
              icon: 'success',
              duration: 2000
            })
          }
          
          await loadPoints()
          
          if (isProxy.value) {
            setTimeout(() => uni.navigateBack(), 1500)
          }
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
    content: '送餐前>1小时免费取消；1小时~10分钟扣5%；<10分钟扣10%',
    success: async (res) => {
      if (res.confirm) {
        try {
          const result = await post(`/meal/order/${order.id}/cancel`)
          uni.showToast({ title: `已取消，退还${result.refund}积分`, icon: 'none', duration: 2000 })
          await loadPoints()
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

.points-bar {
  background: linear-gradient(135deg, #43a047, #66bb6a);
  border-radius: 20rpx;
  padding: 30rpx 40rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #fff;
  margin-bottom: 30rpx;
}

.points-label { font-size: 32rpx; }
.points-value { font-size: 48rpx; font-weight: bold; }

.date-section { margin-bottom: 30rpx; }
.date-title { font-size: 32rpx; font-weight: bold; margin-bottom: 20rpx; }
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

.date-item.active {
  background: #43a047;
  color: #fff;
}

.date-week { font-size: 28rpx; }
.date-day { font-size: 32rpx; font-weight: bold; margin-top: 8rpx; }

.meal-tabs {
  display: flex;
  background: #fff;
  border-radius: 16rpx;
  padding: 10rpx;
  margin-bottom: 30rpx;
}

.meal-tab {
  flex: 1;
  text-align: center;
  padding: 20rpx;
  font-size: 32rpx;
  border-radius: 12rpx;
}

.meal-tab.active {
  background: #43a047;
  color: #fff;
}

.delivery-notice {
  background: #e8f5e9;
  border-radius: 12rpx;
  padding: 20rpx 24rpx;
  display: flex;
  align-items: center;
  margin-bottom: 30rpx;
}

.notice-icon {
  font-size: 32rpx;
  margin-right: 16rpx;
}

.notice-text {
  font-size: 26rpx;
  color: #43a047;
}

.section-title {
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 20rpx;
}

.worker-scroll {
  white-space: nowrap;
  margin-bottom: 30rpx;
}

.worker-item {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 20rpx 40rpx;
  background: #fff;
  border-radius: 16rpx;
  margin-right: 20rpx;
  border: 2rpx solid transparent;
}

.worker-item.active {
  border-color: #43a047;
  background: #e8f5e9;
}

.worker-name {
  font-size: 28rpx;
  color: #333;
}

.worker-item.active .worker-name {
  color: #43a047;
  font-weight: bold;
}

.dish-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.dish-info { flex: 1; }
.dish-name { font-size: 36rpx; font-weight: bold; display: block; }
.dish-desc { font-size: 28rpx; color: #666; margin-top: 10rpx; display: block; }
.dish-nutrition { font-size: 24rpx; color: #999; margin-top: 8rpx; display: block; }

.dish-action { text-align: right; }
.dish-price { font-size: 36rpx; color: #ff4d4f; font-weight: bold; display: block; }

.book-btn {
  background: #43a047;
  color: #fff;
  padding: 16rpx 40rpx;
  border-radius: 12rpx;
  font-size: 30rpx;
  margin-top: 16rpx;
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

.order-no {
  font-size: 26rpx;
  color: #999;
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

.order-dish {
  font-size: 34rpx;
  font-weight: bold;
  display: block;
  margin-bottom: 10rpx;
}

.order-remark {
  font-size: 26rpx;
  color: #666;
  display: block;
  margin-bottom: 10rpx;
  background: #f5f5f5;
  padding: 10rpx 16rpx;
  border-radius: 8rpx;
}

.order-info {
  font-size: 28rpx;
  color: #666;
  display: block;
  margin-bottom: 10rpx;
}

.order-amount {
  font-size: 32rpx;
  color: #ff4d4f;
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

.order-code {
  font-size: 30rpx;
  color: #43a047;
  display: block;
  margin-top: 10rpx;
  font-weight: bold;
}
</style>
