<template>
  <div class="order-manage">
    <el-card>
      <el-form :inline="true" :model="query">
        <el-form-item label="订单类型">
          <el-select v-model="query.orderType" placeholder="全部" clearable style="width: 120px">
            <el-option label="保洁订单" value="cleaning" />
            <el-option label="餐饮订单" value="meal" />
            <el-option label="医疗预约" value="medical" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="待服务" :value="0" />
            <el-option label="服务中" :value="2" />
            <el-option label="待确认" :value="3" />
            <el-option label="已完成" :value="4" />
            <el-option label="已取消" :value="5" />
            <el-option label="争议中" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单号">
          <el-input v-model="query.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="filteredData" v-loading="loading" stripe>
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="orderType" label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="getOrderTypeTag(row.orderType)" size="small">{{ getOrderTypeName(row.orderType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="elderlyName" label="老人" width="100" />
        <el-table-column prop="serviceName" label="服务项目" show-overflow-tooltip />
        <el-table-column prop="workerName" label="服务人员" width="100">
          <template #default="{ row }">{{ row.workerName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="appointmentTime" label="预约时间" width="160" />
        <el-table-column prop="address" label="服务地址" show-overflow-tooltip />
        <el-table-column prop="amount" label="金额" width="80">
          <template #default="{ row }">{{ row.amount ? row.amount + '积分' : '-' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.orderType, row.status)" size="small">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="迟到" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isLate === 1" type="danger" size="small">迟到{{ row.lateMinutes }}分钟</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
            <el-button v-if="isDisputed(row)" type="success" link @click="handleResolve(row)">处理争议</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 20px; color: #999; font-size: 14px;">
        共 {{ filteredData.length }} 条记录
      </div>
    </el-card>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单类型">
          <el-tag :type="getOrderTypeTag(currentOrder.orderType)">{{ getOrderTypeName(currentOrder.orderType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentOrder.orderType, currentOrder.status)">{{ currentOrder.statusText }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="迟到">
          <el-tag v-if="currentOrder.isLate === 1" type="danger">迟到{{ currentOrder.lateMinutes }}分钟</el-tag>
          <span v-else>否</span>
        </el-descriptions-item>
        <el-descriptions-item label="老人姓名">{{ currentOrder.elderlyName }}</el-descriptions-item>
        <el-descriptions-item label="老人电话">{{ currentOrder.elderlyPhone }}</el-descriptions-item>
        <el-descriptions-item label="服务项目" :span="2">{{ currentOrder.serviceName }}</el-descriptions-item>
        <el-descriptions-item label="金额">{{ currentOrder.amount ? currentOrder.amount + '积分' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="服务人员">{{ currentOrder.workerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="预约时间" :span="2">{{ currentOrder.appointmentTime }}</el-descriptions-item>
        <el-descriptions-item label="服务地址" :span="2">{{ currentOrder.address }}</el-descriptions-item>
        <el-descriptions-item label="服务码">{{ currentOrder.serviceCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentOrder.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer v-if="isDisputed(currentOrder)">
        <el-button type="primary" @click="handleResolve(currentOrder)">处理争议完成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const detailVisible = ref(false)
const currentOrder = ref({})

const query = reactive({ orderType: null, status: null, orderNo: '' })

// 争议状态映射
const disputeStatusMap = {
  cleaning: 5,  // 保洁争议状态
  meal: 4,      // 餐饮争议状态
  medical: 4    // 医疗争议状态
}

// 判断订单是否处于争议中
const isDisputed = (order) => {
  if (!order || !order.orderType) return false
  return order.status === disputeStatusMap[order.orderType]
}

// 过滤数据（支持争议状态筛选）
const filteredData = computed(() => {
  if (query.status === 6) {
    // 筛选争议中的订单
    return tableData.value.filter(order => isDisputed(order))
  }
  return tableData.value
})

const getOrderTypeName = (type) => {
  const map = { cleaning: '保洁', meal: '餐饮', medical: '医疗' }
  return map[type] || '未知'
}

const getOrderTypeTag = (type) => {
  const map = { cleaning: 'success', meal: 'warning', medical: 'primary' }
  return map[type] || 'info'
}

const getStatusType = (orderType, status) => {
  // 检查是否是争议状态
  if (isDisputed({ orderType, status })) {
    return 'danger'
  }
  
  // 根据不同订单类型返回状态标签类型
  if (orderType === 'cleaning') {
    // 保洁: 0-待服务 1-服务中 2-待确认 3-已完成 4-已取消 5-争议中
    const map = { 0: 'info', 1: 'warning', 2: 'warning', 3: 'success', 4: 'danger', 5: 'danger' }
    return map[status] || 'info'
  } else if (orderType === 'meal') {
    // 餐饮: 0-待配送 1-配送中 2-待确认 3-已完成 4-争议中 5-已取消
    const map = { 0: 'info', 1: 'warning', 2: 'warning', 3: 'success', 4: 'danger', 5: 'danger' }
    return map[status] || 'info'
  } else if (orderType === 'medical') {
    // 医疗: 0-排队中 1-巡诊中 2-待确认 3-已完成 4-争议中 5-已取消
    const map = { 0: 'info', 1: 'warning', 2: 'warning', 3: 'success', 4: 'danger', 5: 'danger' }
    return map[status] || 'info'
  }
  return 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {}
    if (query.orderType) params.orderType = query.orderType
    // 争议状态特殊处理，不传给后端
    if (query.status !== null && query.status !== 6) params.status = query.status
    if (query.orderNo) params.orderNo = query.orderNo
    
    tableData.value = await request.get('/order/all', { params }) || []
  } catch (e) {
    console.error(e)
    tableData.value = []
  } finally {
    loading.value = false
  }
}

const handleDetail = (row) => {
  currentOrder.value = row
  detailVisible.value = true
}

const handleResolve = (row) => {
  ElMessageBox.confirm(
    '确认该争议订单已线下处理完成？处理后订单将变为已完成状态。',
    '处理争议',
    {
      confirmButtonText: '确认处理完成',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await request.post(`/order/${row.id}/resolve-dispute?orderType=${row.orderType}`)
      ElMessage.success('争议处理成功')
      detailVisible.value = false
      loadData()
    } catch (e) {
      console.error(e)
      ElMessage.error('处理失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.order-manage { padding: 20px; }
</style>
