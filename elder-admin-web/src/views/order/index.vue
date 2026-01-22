<template>
  <div class="order-manage">
    <el-card>
      <el-form :inline="true" :model="query">
        <el-form-item label="订单状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="待接单" :value="0" />
            <el-option label="已接单" :value="1" />
            <el-option label="服务中" :value="2" />
            <el-option label="待确认" :value="3" />
            <el-option label="已完成" :value="4" />
            <el-option label="已取消" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单号">
          <el-input v-model="query.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="elderlyName" label="老人" width="100" />
        <el-table-column prop="serviceName" label="服务项目" />
        <el-table-column prop="workerName" label="服务人员" width="100">
          <template #default="{ row }">{{ row.workerName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="appointmentTime" label="预约时间" width="160" />
        <el-table-column prop="address" label="服务地址" show-overflow-tooltip />
        <el-table-column prop="amount" label="金额" width="80">
          <template #default="{ row }">¥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
            <el-button type="warning" link v-if="row.status === 0" @click="handleAssign(row)">分配</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="loadData"
        @size-change="loadData"
        style="margin-top: 20px"
      />
    </el-card>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentOrder.status)">{{ getStatusText(currentOrder.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="老人姓名">{{ currentOrder.elderlyName }}</el-descriptions-item>
        <el-descriptions-item label="老人电话">{{ currentOrder.elderlyPhone }}</el-descriptions-item>
        <el-descriptions-item label="服务项目">{{ currentOrder.serviceName }}</el-descriptions-item>
        <el-descriptions-item label="金额">¥{{ currentOrder.amount }}</el-descriptions-item>
        <el-descriptions-item label="预约时间" :span="2">{{ currentOrder.appointmentTime }}</el-descriptions-item>
        <el-descriptions-item label="服务地址" :span="2">{{ currentOrder.address }}</el-descriptions-item>
        <el-descriptions-item label="服务人员">{{ currentOrder.workerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="服务人员电话">{{ currentOrder.workerPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="服务码">{{ currentOrder.serviceCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentOrder.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 分配服务人员弹窗 -->
    <el-dialog v-model="assignVisible" title="分配服务人员" width="400px">
      <el-form :model="assignForm" label-width="100px">
        <el-form-item label="服务人员">
          <el-select v-model="assignForm.workerId" placeholder="请选择服务人员" style="width: 100%">
            <el-option v-for="worker in workerList" :key="worker.id" :label="worker.name" :value="worker.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAssign">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const detailVisible = ref(false)
const assignVisible = ref(false)
const currentOrder = ref({})
const workerList = ref([])

const query = reactive({ status: null, orderNo: '', page: 1, size: 10 })
const assignForm = reactive({ orderId: null, workerId: null })

const statusMap = {
  0: { text: '待接单', type: 'info' },
  1: { text: '已接单', type: 'primary' },
  2: { text: '服务中', type: 'warning' },
  3: { text: '待确认', type: 'warning' },
  4: { text: '已完成', type: 'success' },
  5: { text: '已取消', type: 'danger' }
}

const getStatusText = (status) => statusMap[status]?.text || '未知'
const getStatusType = (status) => statusMap[status]?.type || 'info'

const loadData = async () => {
  loading.value = true
  try {
    const params = { page: query.page, size: query.size }
    if (query.status !== null) params.status = query.status
    if (query.orderNo) params.orderNo = query.orderNo
    const res = await request.get('/order/page', { params })
    
    // 获取订单详情（包含老人和服务信息）
    const orders = res.records || []
    const detailedOrders = await Promise.all(orders.map(async (order) => {
      try {
        const detail = await request.get(`/order/${order.id}`)
        return { ...order, ...detail }
      } catch (e) {
        return order
      }
    }))
    
    tableData.value = detailedOrders
    total.value = res.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleDetail = async (row) => {
  try {
    currentOrder.value = await request.get(`/order/${row.id}`)
    detailVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

const handleAssign = async (row) => {
  assignForm.orderId = row.id
  assignForm.workerId = null
  try {
    // 获取服务人员列表
    const res = await request.get('/user/page', { params: { userType: 3, size: 100 } })
    workerList.value = res.records || []
    assignVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

const submitAssign = async () => {
  if (!assignForm.workerId) {
    ElMessage.warning('请选择服务人员')
    return
  }
  try {
    await request.post(`/order/${assignForm.orderId}/assign`, { workerId: assignForm.workerId })
    ElMessage.success('分配成功')
    assignVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.order-manage { padding: 20px; }
</style>
