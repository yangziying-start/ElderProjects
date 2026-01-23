<template>
  <div class="medical-manage">
    <el-tabs v-model="activeTab">
      <!-- 医生管理 -->
      <el-tab-pane label="医生管理" name="doctor">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>医生列表</span>
              <el-button type="primary" @click="handleAddDoctor">新增医生</el-button>
            </div>
          </template>
          <el-table :data="doctorList" v-loading="doctorLoading">
            <el-table-column type="index" label="序号" width="70" />
            <el-table-column prop="name" label="姓名" width="100" />
            <el-table-column prop="title" label="职称" width="100" />
            <el-table-column prop="specialty" label="专长" />
            <el-table-column prop="maxQueueLimit" label="最大排队数" width="100" />
            <el-table-column label="当前排队" width="90">
              <template #default="{ row }">
                <el-tag :type="row.currentQueueCount >= row.maxQueueLimit ? 'danger' : 'success'" size="small">
                  {{ row.currentQueueCount || 0 }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="熔断状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.isCircuitOpen ? 'danger' : 'success'" size="small">
                  {{ row.isCircuitOpen ? '已熔断' : '正常' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '正常' : '停诊' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleEditDoctor(row)">编辑</el-button>
                <el-button :type="row.status === 1 ? 'danger' : 'success'" link @click="handleToggleDoctorStatus(row)">
                  {{ row.status === 1 ? '停诊' : '开诊' }}
                </el-button>
                <el-button type="danger" link @click="handleDeleteDoctor(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 值班排班 -->
      <el-tab-pane label="值班排班" name="duty">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>医生值班排班</span>
              <el-button type="primary" @click="handleAddDuty">新增值班</el-button>
            </div>
          </template>
          <el-form :inline="true" :model="dutyQuery" style="margin-bottom: 16px">
            <el-form-item label="医生">
              <el-select v-model="dutyQuery.doctorId" clearable placeholder="全部" style="width: 150px">
                <el-option v-for="d in doctorList" :key="d.id" :label="d.name" :value="d.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="日期范围">
              <el-date-picker v-model="dutyQuery.dateRange" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始" end-placeholder="结束" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadDuties">查询</el-button>
            </el-form-item>
          </el-form>
          <el-table :data="dutyList" v-loading="dutyLoading">
            <el-table-column type="index" label="序号" width="70" />
            <el-table-column label="医生" width="100">
              <template #default="{ row }">{{ getDoctorName(row.doctorId) }}</template>
            </el-table-column>
            <el-table-column prop="dutyDate" label="值班日期" width="110" />
            <el-table-column prop="dutyType" label="值班类型" width="100">
              <template #default="{ row }">
                <el-tag :type="row.dutyType === 1 ? 'primary' : 'warning'" size="small">
                  {{ row.dutyType === 1 ? '日间' : '夜间急诊' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="时间段" width="140">
              <template #default="{ row }">{{ row.startTime }} - {{ row.endTime }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '值班中' : '休息' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleEditDuty(row)">编辑</el-button>
                <el-button type="danger" link @click="handleDeleteDuty(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 熔断监控 -->
      <el-tab-pane label="熔断监控" name="circuit">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>医生熔断状态监控</span>
              <el-button type="primary" @click="loadCircuitBreakers">刷新</el-button>
            </div>
          </template>
          <div style="margin-bottom: 16px; color: #909399; font-size: 13px;">
            <p>熔断规则：日间(8:00-20:00)排队人数 > Max_Limit 时触发熔断；排队人数 < 5人时自动恢复</p>
            <p>夜间急诊模式不设熔断限制</p>
          </div>
          <el-table :data="circuitList" v-loading="circuitLoading">
            <el-table-column prop="doctorId" label="医生ID" width="80" />
            <el-table-column prop="doctorName" label="医生姓名" width="120" />
            <el-table-column prop="title" label="职称" width="100" />
            <el-table-column prop="maxQueueLimit" label="最大排队数" width="100" />
            <el-table-column prop="currentQueueCount" label="当前排队数" width="100">
              <template #default="{ row }">
                <el-tag :type="row.currentQueueCount >= row.maxQueueLimit ? 'danger' : 'success'">
                  {{ row.currentQueueCount }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="熔断状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.isCircuitOpen ? 'danger' : 'success'">
                  {{ row.isCircuitOpen ? '已熔断' : '正常' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="warning" link @click="handleResetCircuit(row)" v-if="row.isCircuitOpen">重置熔断</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 医疗预约 -->
      <el-tab-pane label="医疗预约" name="appointment">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>医疗预约</span>
              <div class="stats">
                <el-tag>今日预约: {{ appointmentStats.todayCount || 0 }}</el-tag>
                <el-tag type="warning">排队中: {{ appointmentStats.queueingCount || 0 }}</el-tag>
                <el-tag type="success">巡诊中: {{ appointmentStats.visitingCount || 0 }}</el-tag>
              </div>
            </div>
          </template>
          <el-form :inline="true" :model="appointmentQuery" style="margin-bottom: 16px">
            <el-form-item label="日期">
              <el-date-picker v-model="appointmentQuery.appointmentDate" type="date" value-format="YYYY-MM-DD" clearable />
            </el-form-item>
            <el-form-item label="医生">
              <el-select v-model="appointmentQuery.doctorId" clearable placeholder="全部" style="width: 150px">
                <el-option v-for="d in doctorList" :key="d.id" :label="d.name" :value="d.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="appointmentQuery.status" clearable style="width: 100px">
                <el-option label="排队中" :value="0" />
                <el-option label="巡诊中" :value="1" />
                <el-option label="已完成" :value="2" />
                <el-option label="已取消" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadAppointments">查询</el-button>
            </el-form-item>
          </el-form>
          <el-table :data="appointmentList" v-loading="appointmentLoading">
            <el-table-column prop="orderNo" label="订单号" width="180" />
            <el-table-column prop="appointmentDate" label="预约日期" width="110" />
            <el-table-column prop="appointmentType" label="类型" width="90">
              <template #default="{ row }">
                <el-tag :type="row.appointmentType === 1 ? 'primary' : 'warning'" size="small">
                  {{ row.appointmentType === 1 ? '日间巡诊' : '夜间急诊' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="医生" width="100">
              <template #default="{ row }">{{ row.doctor?.name || '-' }}</template>
            </el-table-column>
            <el-table-column prop="queueNumber" label="排队号" width="80" />
            <el-table-column prop="symptoms" label="症状" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="getAppointmentStatusType(row.status)" size="small">{{ getAppointmentStatusName(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleViewAppointment(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination v-model:current-page="appointmentQuery.page" :total="appointmentTotal" layout="total, prev, pager, next" @current-change="loadAppointments" style="margin-top: 16px" />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 医生编辑弹窗 -->
    <el-dialog v-model="doctorDialogVisible" :title="doctorForm.id ? '编辑医生' : '新增医生'" width="500px">
      <el-form :model="doctorForm" label-width="120px">
        <el-form-item label="关联医疗人员">
          <el-select v-model="doctorForm.userId" placeholder="请选择医疗人员" style="width: 100%" clearable>
            <el-option v-for="w in medicalWorkerList" :key="w.id" :label="w.name || w.phone" :value="w.id">
              <span>{{ w.name || '未设置姓名' }}</span>
              <span style="color: #909399; margin-left: 8px;">{{ w.phone }}</span>
            </el-option>
          </el-select>
          <div style="color: #909399; font-size: 12px; margin-top: 4px;">关联后医疗人员可在小程序接收巡诊任务</div>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="doctorForm.name" />
        </el-form-item>
        <el-form-item label="职称">
          <el-input v-model="doctorForm.title" />
        </el-form-item>
        <el-form-item label="专长">
          <el-input v-model="doctorForm.specialty" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="doctorForm.introduction" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="最大排队人数">
          <el-input-number v-model="doctorForm.maxQueueLimit" :min="1" />
          <span style="margin-left: 8px; color: #909399;">熔断阈值</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="doctorDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitDoctor">确定</el-button>
      </template>
    </el-dialog>

    <!-- 值班编辑弹窗 -->
    <el-dialog v-model="dutyDialogVisible" :title="dutyForm.id ? '编辑值班' : '新增值班'" width="450px">
      <el-form :model="dutyForm" label-width="100px">
        <el-form-item label="医生">
          <el-select v-model="dutyForm.doctorId" style="width: 100%">
            <el-option v-for="d in doctorList" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="值班日期">
          <el-date-picker v-model="dutyForm.dutyDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="值班类型">
          <el-select v-model="dutyForm.dutyType" style="width: 100%" @change="onDutyTypeChange">
            <el-option label="日间(8:00-20:00)" :value="1" />
            <el-option label="夜间急诊(20:00-8:00)" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-time-picker v-model="dutyForm.startTime" format="HH:mm" value-format="HH:mm:ss" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-time-picker v-model="dutyForm.endTime" format="HH:mm" value-format="HH:mm:ss" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="dutyForm.status" style="width: 100%">
            <el-option label="值班中" :value="1" />
            <el-option label="休息" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dutyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitDuty">确定</el-button>
      </template>
    </el-dialog>

    <!-- 预约详情弹窗 -->
    <el-dialog v-model="appointmentDetailVisible" title="预约详情" width="500px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="订单号">{{ currentAppointment.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="预约日期">{{ currentAppointment.appointmentDate }}</el-descriptions-item>
        <el-descriptions-item label="预约类型">{{ currentAppointment.appointmentType === 1 ? '日间巡诊' : '夜间急诊' }}</el-descriptions-item>
        <el-descriptions-item label="医生">{{ currentAppointment.doctor?.name }}</el-descriptions-item>
        <el-descriptions-item label="排队号">{{ currentAppointment.queueNumber }}</el-descriptions-item>
        <el-descriptions-item label="症状描述">{{ currentAppointment.symptoms }}</el-descriptions-item>
        <el-descriptions-item label="诊断结果">{{ currentAppointment.diagnosis || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处方建议">{{ currentAppointment.prescription || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ getAppointmentStatusName(currentAppointment.status) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const activeTab = ref('doctor')

// 医生
const doctorLoading = ref(false)
const doctorList = ref([])
const doctorDialogVisible = ref(false)
const doctorForm = reactive({ id: null, userId: null, name: '', title: '', specialty: '', introduction: '', maxQueueLimit: 10 })

// 医疗人员列表（用于关联医生）
const medicalWorkerList = ref([])

// 值班
const dutyLoading = ref(false)
const dutyList = ref([])
const dutyDialogVisible = ref(false)
const dutyForm = reactive({ id: null, doctorId: null, dutyDate: '', dutyType: 1, startTime: '08:00:00', endTime: '20:00:00', status: 1 })
const dutyQuery = reactive({ doctorId: null, dateRange: null })

// 熔断
const circuitLoading = ref(false)
const circuitList = ref([])

// 预约
const appointmentLoading = ref(false)
const appointmentList = ref([])
const appointmentTotal = ref(0)
const appointmentStats = ref({})
const appointmentQuery = reactive({ page: 1, size: 10, appointmentDate: null, doctorId: null, status: null })
const appointmentDetailVisible = ref(false)
const currentAppointment = ref({})

const getDoctorName = (id) => doctorList.value.find(d => d.id === id)?.name || '-'
const getAppointmentStatusName = (status) => ({ 0: '排队中', 1: '巡诊中', 2: '已完成', 3: '已取消' }[status] || '-')
const getAppointmentStatusType = (status) => ({ 0: 'warning', 1: 'primary', 2: 'success', 3: 'info' }[status] || 'info')

const loadDoctors = async () => {
  doctorLoading.value = true
  try {
    doctorList.value = await request.get('/admin/medical/doctors') || []
  } catch (e) { console.error(e) }
  doctorLoading.value = false
}

const loadMedicalWorkers = async () => {
  try {
    medicalWorkerList.value = await request.get('/admin/medical/medical-workers') || []
  } catch (e) { console.error(e) }
}

const handleAddDoctor = () => {
  Object.assign(doctorForm, { id: null, userId: null, name: '', title: '', specialty: '', introduction: '', maxQueueLimit: 10 })
  doctorDialogVisible.value = true
}

const handleEditDoctor = (row) => {
  Object.assign(doctorForm, row)
  doctorDialogVisible.value = true
}

const submitDoctor = async () => {
  try {
    if (doctorForm.id) {
      await request.put(`/admin/medical/doctors/${doctorForm.id}`, doctorForm)
    } else {
      await request.post('/admin/medical/doctors', doctorForm)
    }
    ElMessage.success('保存成功')
    doctorDialogVisible.value = false
    loadDoctors()
  } catch (e) { console.error(e) }
}

const handleToggleDoctorStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await request.post(`/admin/medical/doctors/${row.id}/status`, { status: newStatus })
    ElMessage.success(newStatus === 1 ? '已开诊' : '已停诊')
    loadDoctors()
  } catch (e) { console.error(e) }
}

const handleDeleteDoctor = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该医生？', '提示', { type: 'warning' })
    await request.delete(`/admin/medical/doctors/${row.id}`)
    ElMessage.success('删除成功')
    loadDoctors()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const loadDuties = async () => {
  dutyLoading.value = true
  try {
    const params = {}
    if (dutyQuery.doctorId) params.doctorId = dutyQuery.doctorId
    if (dutyQuery.dateRange && dutyQuery.dateRange.length === 2) {
      params.startDate = dutyQuery.dateRange[0]
      params.endDate = dutyQuery.dateRange[1]
    }
    dutyList.value = await request.get('/admin/medical/duties', { params }) || []
  } catch (e) { console.error(e) }
  dutyLoading.value = false
}

const handleAddDuty = () => {
  Object.assign(dutyForm, { id: null, doctorId: null, dutyDate: '', dutyType: 1, startTime: '08:00:00', endTime: '20:00:00', status: 1 })
  dutyDialogVisible.value = true
}

const handleEditDuty = (row) => {
  Object.assign(dutyForm, row)
  dutyDialogVisible.value = true
}

const onDutyTypeChange = (type) => {
  if (type === 1) {
    dutyForm.startTime = '08:00:00'
    dutyForm.endTime = '20:00:00'
  } else {
    dutyForm.startTime = '20:00:00'
    dutyForm.endTime = '08:00:00'
  }
}

// 检查值班时间段是否冲突
const checkDutyConflict = () => {
  const { doctorId, dutyDate, startTime, endTime, id, dutyType } = dutyForm
  if (!doctorId || !dutyDate || !startTime || !endTime) return null
  
  // 转换时间为分钟数便于比较
  const toMinutes = (timeStr) => {
    if (!timeStr) return 0
    const parts = timeStr.split(':')
    return parseInt(parts[0]) * 60 + parseInt(parts[1])
  }
  
  const newStart = toMinutes(startTime)
  const newEnd = toMinutes(endTime)
  
  // 查找同一医生同一天的其他值班
  for (const duty of dutyList.value) {
    // 跳过当前编辑的值班
    if (id && duty.id === id) continue
    // 检查是否同一医生同一天
    if (duty.doctorId === doctorId && duty.dutyDate === dutyDate) {
      const existStart = toMinutes(duty.startTime)
      const existEnd = toMinutes(duty.endTime)
      
      // 夜间值班特殊处理（跨天）
      if (dutyType === 2 || duty.dutyType === 2) {
        // 夜间值班与日间值班不冲突（时间段不重叠）
        // 但同一天不能有两个夜间值班
        if (dutyType === 2 && duty.dutyType === 2) {
          return `该医生在 ${dutyDate} 已有夜间值班排班`
        }
        continue
      }
      
      // 日间值班检查时间段是否重叠
      if (!(newEnd <= existStart || newStart >= existEnd)) {
        return `该医生在 ${dutyDate} ${duty.startTime}-${duty.endTime} 已有值班排班`
      }
    }
  }
  return null
}

const submitDuty = async () => {
  // 检查时间段冲突
  const conflict = checkDutyConflict()
  if (conflict) {
    ElMessage.warning(conflict)
    return
  }
  
  try {
    if (dutyForm.id) {
      await request.put(`/admin/medical/duties/${dutyForm.id}`, dutyForm)
    } else {
      await request.post('/admin/medical/duties', dutyForm)
    }
    ElMessage.success('保存成功')
    dutyDialogVisible.value = false
    loadDuties()
  } catch (e) { console.error(e) }
}

const handleDeleteDuty = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该值班？', '提示', { type: 'warning' })
    await request.delete(`/admin/medical/duties/${row.id}`)
    ElMessage.success('删除成功')
    loadDuties()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const loadCircuitBreakers = async () => {
  circuitLoading.value = true
  try {
    circuitList.value = await request.get('/admin/medical/circuit-breakers') || []
  } catch (e) { console.error(e) }
  circuitLoading.value = false
}

const handleResetCircuit = async (row) => {
  try {
    await ElMessageBox.confirm('确定重置该医生的熔断状态？', '提示')
    await request.post(`/admin/medical/circuit-breakers/${row.doctorId}/reset`)
    ElMessage.success('重置成功')
    loadCircuitBreakers()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const loadAppointments = async () => {
  appointmentLoading.value = true
  try {
    const params = { page: appointmentQuery.page, size: appointmentQuery.size }
    if (appointmentQuery.appointmentDate) params.appointmentDate = appointmentQuery.appointmentDate
    if (appointmentQuery.doctorId) params.doctorId = appointmentQuery.doctorId
    if (appointmentQuery.status != null) params.status = appointmentQuery.status
    const res = await request.get('/admin/medical/appointments', { params })
    appointmentList.value = res.records || []
    appointmentTotal.value = res.total || 0
  } catch (e) { console.error(e) }
  appointmentLoading.value = false
}

const loadAppointmentStats = async () => {
  try {
    appointmentStats.value = await request.get('/admin/medical/appointments/stats') || {}
  } catch (e) { console.error(e) }
}

const handleViewAppointment = (row) => {
  currentAppointment.value = row
  appointmentDetailVisible.value = true
}

onMounted(() => {
  loadDoctors()
  loadMedicalWorkers()
  loadDuties()
  loadCircuitBreakers()
  loadAppointments()
  loadAppointmentStats()
})
</script>

<style scoped>
.medical-manage { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.stats { display: flex; gap: 10px; }
</style>
