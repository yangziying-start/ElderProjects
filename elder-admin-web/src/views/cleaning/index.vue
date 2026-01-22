<template>
  <div class="cleaning-manage">
    <el-tabs v-model="activeTab">
      <!-- 保洁服务项目 -->
      <el-tab-pane label="服务项目" name="service">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>保洁服务项目</span>
              <el-button type="primary" @click="handleAddService">新增服务</el-button>
            </div>
          </template>
          <el-table :data="serviceList" v-loading="serviceLoading">
            <el-table-column type="index" label="序号" width="70" />
            <el-table-column prop="name" label="服务名称" />
            <el-table-column prop="referenceDuration" label="参考耗时" width="100">
              <template #default="{ row }">{{ row.referenceDuration }}分钟</template>
            </el-table-column>
            <el-table-column prop="pricePer30min" label="每30分钟价格" width="120">
              <template #default="{ row }">{{ row.pricePer30min }}积分</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleEditService(row)">编辑</el-button>
                <el-button :type="row.status === 1 ? 'danger' : 'success'" link @click="handleToggleServiceStatus(row)">
                  {{ row.status === 1 ? '下架' : '上架' }}
                </el-button>
                <el-button type="danger" link @click="handleDeleteService(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 保洁员排班 -->
      <el-tab-pane label="保洁员排班" name="schedule">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>保洁员排班</span>
              <el-button type="primary" @click="handleAddSchedule">新增排班</el-button>
            </div>
          </template>
          <el-form :inline="true" :model="scheduleQuery" style="margin-bottom: 16px">
            <el-form-item label="保洁员">
              <el-select v-model="scheduleQuery.workerId" clearable placeholder="全部" style="width: 150px">
                <el-option v-for="w in workerList" :key="w.id" :label="w.name || w.phone" :value="w.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="日期范围">
              <el-date-picker v-model="scheduleQuery.dateRange" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始" end-placeholder="结束" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadSchedules">查询</el-button>
            </el-form-item>
          </el-form>
          <el-table :data="scheduleList" v-loading="scheduleLoading">
            <el-table-column type="index" label="序号" width="70" />
            <el-table-column label="保洁员" width="120">
              <template #default="{ row }">{{ row.worker?.name || row.worker?.phone || '-' }}</template>
            </el-table-column>
            <el-table-column prop="scheduleDate" label="日期" width="110" />
            <el-table-column prop="startTime" label="开始时间" width="100" />
            <el-table-column prop="endTime" label="结束时间" width="100" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '可预约' : '休息' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleEditSchedule(row)">编辑</el-button>
                <el-button type="danger" link @click="handleDeleteSchedule(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 保洁订单 -->
      <el-tab-pane label="保洁订单" name="order">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>保洁订单</span>
              <div class="stats">
                <el-tag>今日订单: {{ orderStats.todayCount || 0 }}</el-tag>
                <el-tag type="warning">待服务: {{ orderStats.pendingCount || 0 }}</el-tag>
                <el-tag type="success">服务中: {{ orderStats.inProgressCount || 0 }}</el-tag>
              </div>
            </div>
          </template>
          <el-form :inline="true" :model="orderQuery" style="margin-bottom: 16px">
            <el-form-item label="日期">
              <el-date-picker v-model="orderQuery.serviceDate" type="date" value-format="YYYY-MM-DD" clearable />
            </el-form-item>
            <el-form-item label="保洁员">
              <el-select v-model="orderQuery.workerId" clearable placeholder="全部" style="width: 150px">
                <el-option v-for="w in workerList" :key="w.id" :label="w.name || w.phone" :value="w.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="orderQuery.status" clearable style="width: 100px">
                <el-option label="待服务" :value="0" />
                <el-option label="服务中" :value="1" />
                <el-option label="待确认" :value="2" />
                <el-option label="已完成" :value="3" />
                <el-option label="已取消" :value="4" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadOrders">查询</el-button>
            </el-form-item>
          </el-form>
          <el-table :data="orderList" v-loading="orderLoading">
            <el-table-column prop="orderNo" label="订单号" width="180" />
            <el-table-column prop="serviceDate" label="服务日期" width="110" />
            <el-table-column label="时段" width="120">
              <template #default="{ row }">{{ row.startTime }} - {{ row.endTime }}</template>
            </el-table-column>
            <el-table-column label="服务项目" width="100">
              <template #default="{ row }">{{ row.service?.name || '-' }}</template>
            </el-table-column>
            <el-table-column label="保洁员" width="100">
              <template #default="{ row }">{{ row.worker?.name || '-' }}</template>
            </el-table-column>
            <el-table-column prop="amount" label="金额(积分)" width="100" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="getOrderStatusType(row.status)" size="small">{{ getOrderStatusName(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="下单时间" width="160" />
          </el-table>
          <el-pagination v-model:current-page="orderQuery.page" :total="orderTotal" layout="total, prev, pager, next" @current-change="loadOrders" style="margin-top: 16px" />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 服务编辑弹窗 -->
    <el-dialog v-model="serviceDialogVisible" :title="serviceForm.id ? '编辑服务' : '新增服务'" width="450px">
      <el-form :model="serviceForm" label-width="120px">
        <el-form-item label="服务名称">
          <el-input v-model="serviceForm.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="serviceForm.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="参考耗时(分钟)">
          <el-input-number v-model="serviceForm.referenceDuration" :min="30" :step="30" />
        </el-form-item>
        <el-form-item label="每30分钟价格">
          <el-input-number v-model="serviceForm.pricePer30min" :min="0" />
          <span style="margin-left: 8px">积分</span>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="serviceForm.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="serviceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitService">确定</el-button>
      </template>
    </el-dialog>

    <!-- 排班编辑弹窗 -->
    <el-dialog v-model="scheduleDialogVisible" :title="scheduleForm.id ? '编辑排班' : '新增排班'" width="450px">
      <el-form :model="scheduleForm" label-width="100px">
        <el-form-item label="保洁员">
          <el-select v-model="scheduleForm.workerId" style="width: 100%">
            <el-option v-for="w in workerList" :key="w.id" :label="w.name || w.phone" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="scheduleForm.scheduleDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-time-picker v-model="scheduleForm.startTime" format="HH:mm" value-format="HH:mm:ss" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-time-picker v-model="scheduleForm.endTime" format="HH:mm" value-format="HH:mm:ss" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="scheduleForm.status" style="width: 100%">
            <el-option label="可预约" :value="1" />
            <el-option label="休息" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scheduleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSchedule">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const activeTab = ref('service')

// 服务项目
const serviceLoading = ref(false)
const serviceList = ref([])
const serviceDialogVisible = ref(false)
const serviceForm = reactive({ id: null, name: '', description: '', referenceDuration: 30, pricePer30min: 20, sort: 0 })

// 保洁员
const workerList = ref([])

// 排班
const scheduleLoading = ref(false)
const scheduleList = ref([])
const scheduleDialogVisible = ref(false)
const scheduleForm = reactive({ id: null, workerId: null, scheduleDate: '', startTime: '08:00:00', endTime: '18:00:00', status: 1 })
const scheduleQuery = reactive({ workerId: null, dateRange: null })

// 订单
const orderLoading = ref(false)
const orderList = ref([])
const orderTotal = ref(0)
const orderStats = ref({})
const orderQuery = reactive({ page: 1, size: 10, serviceDate: null, workerId: null, status: null })

const getOrderStatusName = (status) => ({ 0: '待服务', 1: '服务中', 2: '待确认', 3: '已完成', 4: '已取消' }[status] || '-')
const getOrderStatusType = (status) => ({ 0: 'warning', 1: 'primary', 2: 'info', 3: 'success', 4: 'info' }[status] || 'info')

const loadServices = async () => {
  serviceLoading.value = true
  try {
    serviceList.value = await request.get('/admin/cleaning/services') || []
  } catch (e) { console.error(e) }
  serviceLoading.value = false
}

const handleAddService = () => {
  Object.assign(serviceForm, { id: null, name: '', description: '', referenceDuration: 30, pricePer30min: 20, sort: 0 })
  serviceDialogVisible.value = true
}

const handleEditService = (row) => {
  Object.assign(serviceForm, row)
  serviceDialogVisible.value = true
}

const submitService = async () => {
  try {
    if (serviceForm.id) {
      await request.put(`/admin/cleaning/services/${serviceForm.id}`, serviceForm)
    } else {
      await request.post('/admin/cleaning/services', serviceForm)
    }
    ElMessage.success('保存成功')
    serviceDialogVisible.value = false
    loadServices()
  } catch (e) { console.error(e) }
}

const handleToggleServiceStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await request.post(`/admin/cleaning/services/${row.id}/status`, { status: newStatus })
    ElMessage.success(newStatus === 1 ? '已上架' : '已下架')
    loadServices()
  } catch (e) { console.error(e) }
}

const handleDeleteService = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该服务项目？', '提示', { type: 'warning' })
    await request.delete(`/admin/cleaning/services/${row.id}`)
    ElMessage.success('删除成功')
    loadServices()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const loadWorkers = async () => {
  try {
    workerList.value = await request.get('/admin/cleaning/workers') || []
  } catch (e) { console.error(e) }
}

const loadSchedules = async () => {
  scheduleLoading.value = true
  try {
    const params = {}
    if (scheduleQuery.workerId) params.workerId = scheduleQuery.workerId
    if (scheduleQuery.dateRange && scheduleQuery.dateRange.length === 2) {
      params.startDate = scheduleQuery.dateRange[0]
      params.endDate = scheduleQuery.dateRange[1]
    }
    scheduleList.value = await request.get('/admin/cleaning/schedules', { params }) || []
  } catch (e) { console.error(e) }
  scheduleLoading.value = false
}

const handleAddSchedule = () => {
  Object.assign(scheduleForm, { id: null, workerId: null, scheduleDate: '', startTime: '08:00:00', endTime: '18:00:00', status: 1 })
  scheduleDialogVisible.value = true
}

const handleEditSchedule = (row) => {
  Object.assign(scheduleForm, row)
  scheduleDialogVisible.value = true
}

const submitSchedule = async () => {
  try {
    if (scheduleForm.id) {
      await request.put(`/admin/cleaning/schedules/${scheduleForm.id}`, scheduleForm)
    } else {
      await request.post('/admin/cleaning/schedules', scheduleForm)
    }
    ElMessage.success('保存成功')
    scheduleDialogVisible.value = false
    loadSchedules()
  } catch (e) { console.error(e) }
}

const handleDeleteSchedule = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该排班？', '提示', { type: 'warning' })
    await request.delete(`/admin/cleaning/schedules/${row.id}`)
    ElMessage.success('删除成功')
    loadSchedules()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const loadOrders = async () => {
  orderLoading.value = true
  try {
    const params = { page: orderQuery.page, size: orderQuery.size }
    if (orderQuery.serviceDate) params.serviceDate = orderQuery.serviceDate
    if (orderQuery.workerId) params.workerId = orderQuery.workerId
    if (orderQuery.status != null) params.status = orderQuery.status
    const res = await request.get('/admin/cleaning/orders', { params })
    orderList.value = res.records || []
    orderTotal.value = res.total || 0
  } catch (e) { console.error(e) }
  orderLoading.value = false
}

const loadOrderStats = async () => {
  try {
    orderStats.value = await request.get('/admin/cleaning/orders/stats') || {}
  } catch (e) { console.error(e) }
}

onMounted(() => {
  loadServices()
  loadWorkers()
  loadSchedules()
  loadOrders()
  loadOrderStats()
})
</script>

<style scoped>
.cleaning-manage { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.stats { display: flex; gap: 10px; }
</style>
