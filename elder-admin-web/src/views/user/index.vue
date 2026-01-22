<template>
  <div class="user-manage">
    <el-card>
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="query">
        <el-form-item label="用户类型">
          <el-select v-model="query.userType" placeholder="全部" clearable style="width: 120px" @change="handleUserTypeChange">
            <el-option label="老人" :value="1" />
            <el-option label="子女" :value="2" />
            <el-option label="服务人员" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务人员类型" v-if="query.userType === 3">
          <el-select v-model="query.workerType" placeholder="全部" clearable style="width: 120px">
            <el-option label="配送员" :value="1" />
            <el-option label="保洁员" :value="2" />
            <el-option label="医疗人员" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="query.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="query.name" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button type="success" @click="handleAdd">新增用户</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="70" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="idCard" label="身份证号" width="180" />
        <el-table-column prop="relatedIdCard" label="关联身份证号" width="180" />
        <el-table-column prop="userType" label="用户类型" width="100">
          <template #default="{ row }">
            <el-tag :type="userTypeTagMap[row.userType]">
              {{ userTypeMap[row.userType] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="workerType" label="服务人员类型" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.userType === 3 && row.workerType" :type="workerTypeTagMap[row.workerType]">
              {{ workerTypeMap[row.workerType] }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="address" label="地址" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" width="340" fixed="right">
          <template #default="{ row }">
            <el-button type="success" link @click="handlePoints(row)" v-if="row.userType === 1">积分</el-button>
            <el-button type="info" link @click="handleContacts(row)" v-if="row.userType === 1">联系人</el-button>
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link @click="handleResetPwd(row)">重置密码</el-button>
            <el-button 
              :type="row.status === 1 ? 'danger' : 'success'" 
              link 
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑用户' : '新增用户'" 
      width="500px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" :disabled="isEdit" maxlength="11" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" maxlength="20" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="请输入身份证号" maxlength="18" />
          <div class="form-tip" v-if="!isEdit">初始密码为身份证后6位</div>
        </el-form-item>
        <el-form-item label="关联身份证号" prop="relatedIdCard" v-if="form.userType === 1 || form.userType === 2">
          <el-input v-model="form.relatedIdCard" placeholder="请输入关联的老人/子女身份证号" maxlength="18" />
          <div class="form-tip">{{ form.userType === 1 ? '填写子女身份证号以建立关联' : '填写老人身份证号以建立关联' }}</div>
        </el-form-item>
        <el-form-item label="用户类型" prop="userType">
          <el-select v-model="form.userType" placeholder="请选择" :disabled="isEdit" style="width: 100%" @change="handleFormUserTypeChange">
            <el-option label="老人" :value="1" />
            <el-option label="子女" :value="2" />
            <el-option label="服务人员" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务人员类型" prop="workerType" v-if="form.userType === 3">
          <el-select v-model="form.workerType" placeholder="请选择服务人员类型" style="width: 100%">
            <el-option label="配送员" :value="1" />
            <el-option label="保洁员" :value="2" />
            <el-option label="医疗人员" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 积分管理弹窗 -->
    <el-dialog v-model="pointsDialogVisible" title="积分管理" width="600px">
      <div class="points-info" v-if="currentUserPoints">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户">{{ currentUserPoints.userName }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentUserPoints.phone }}</el-descriptions-item>
          <el-descriptions-item label="当前积分">
            <span style="font-size: 24px; color: #67c23a; font-weight: bold;">{{ currentUserPoints.points }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="累计获得">{{ currentUserPoints.totalEarned }}</el-descriptions-item>
          <el-descriptions-item label="累计消费">{{ currentUserPoints.totalSpent }}</el-descriptions-item>
        </el-descriptions>
      </div>
      
      <el-divider>积分操作</el-divider>
      
      <el-form :model="pointsForm" label-width="80px">
        <el-form-item label="操作类型">
          <el-radio-group v-model="pointsForm.type">
            <el-radio label="recharge">充值</el-radio>
            <el-radio label="deduct">扣除</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="积分数量">
          <el-input-number v-model="pointsForm.amount" :min="1" :max="100000" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="pointsForm.remark" placeholder="请输入备注" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitPointsOp" :loading="pointsLoading">
            {{ pointsForm.type === 'recharge' ? '确认充值' : '确认扣除' }}
          </el-button>
        </el-form-item>
      </el-form>

      <el-divider>积分流水</el-divider>
      
      <el-table :data="transactionList" max-height="300" size="small">
        <el-table-column prop="createTime" label="时间" width="160" />
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="transTypeMap[row.type]?.tag" size="small">{{ transTypeMap[row.type]?.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="积分" width="80">
          <template #default="{ row }">
            <span :style="{ color: [1,3].includes(row.type) ? '#67c23a' : '#f56c6c' }">
              {{ [1,3].includes(row.type) ? '+' : '-' }}{{ row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="余额" width="80" />
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      </el-table>
    </el-dialog>

    <!-- 紧急联系人管理弹窗 -->
    <el-dialog v-model="contactsDialogVisible" title="紧急联系人管理" width="600px">
      <div class="contacts-header">
        <span>老人：{{ currentElderly?.name }} ({{ currentElderly?.phone }})</span>
      </div>
      
      <el-divider>已绑定的联系人</el-divider>
      
      <el-table :data="contactsList" size="small" v-loading="contactsLoading">
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="idCard" label="身份证号" width="180" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="danger" link @click="handleUnbindContact(row)">解绑</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="empty-contacts" v-if="contactsList.length === 0 && !contactsLoading">
        暂无紧急联系人
      </div>
      
      <el-divider>添加联系人</el-divider>
      
      <el-form :model="contactForm" label-width="100px">
        <el-form-item label="查找方式">
          <el-radio-group v-model="contactForm.searchType">
            <el-radio label="phone">手机号</el-radio>
            <el-radio label="idCard">身份证号</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="contactForm.searchType === 'phone' ? '手机号' : '身份证号'">
          <el-input 
            v-model="contactForm.searchValue" 
            :placeholder="contactForm.searchType === 'phone' ? '请输入子女手机号' : '请输入子女身份证号'"
            :maxlength="contactForm.searchType === 'phone' ? 11 : 18"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleBindContact" :loading="bindLoading">绑定联系人</el-button>
        </el-form-item>
      </el-form>
      
      <div class="contact-tip">
        提示：只能绑定已注册的子女用户作为紧急联系人
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

// 积分管理相关
const pointsDialogVisible = ref(false)
const pointsLoading = ref(false)
const currentUserPoints = ref(null)
const transactionList = ref([])
const pointsForm = reactive({
  userId: null,
  type: 'recharge',
  amount: 100,
  remark: ''
})

// 紧急联系人管理相关
const contactsDialogVisible = ref(false)
const contactsLoading = ref(false)
const bindLoading = ref(false)
const currentElderly = ref(null)
const contactsList = ref([])
const contactForm = reactive({
  searchType: 'phone',
  searchValue: ''
})

const transTypeMap = {
  1: { label: '充值', tag: 'success' },
  2: { label: '消费', tag: 'danger' },
  3: { label: '退款', tag: 'warning' },
  4: { label: '扣除', tag: 'info' }
}

const query = reactive({
  userType: null,
  workerType: null,
  phone: '',
  name: '',
  page: 1,
  size: 10
})

const form = reactive({
  id: null,
  phone: '',
  name: '',
  idCard: '',
  relatedIdCard: '',
  userType: 1,
  workerType: null,
  password: '',
  address: ''
})

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' }
  ],
  relatedIdCard: [{ pattern: /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/, message: '身份证号格式不正确', trigger: 'blur' }],
  userType: [{ required: true, message: '请选择用户类型', trigger: 'change' }],
  workerType: [{ required: true, message: '请选择服务人员类型', trigger: 'change' }]
}

const userTypeMap = {
  1: '老人',
  2: '子女',
  3: '服务人员',
  4: '管理员'
}

const userTypeTagMap = {
  1: 'success',
  2: 'primary',
  3: 'warning',
  4: 'danger'
}

const workerTypeMap = {
  1: '配送员',
  2: '保洁员',
  3: '医疗人员'
}

const workerTypeTagMap = {
  1: 'primary',
  2: 'success',
  3: 'danger'
}

// 用户类型变化时清空服务人员类型
const handleUserTypeChange = () => {
  if (query.userType !== 3) {
    query.workerType = null
  }
}

// 表单用户类型变化时清空服务人员类型
const handleFormUserTypeChange = () => {
  if (form.userType !== 3) {
    form.workerType = null
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: query.page,
      size: query.size
    }
    if (query.userType) params.userType = query.userType
    if (query.workerType) params.workerType = query.workerType
    if (query.phone) params.phone = query.phone
    if (query.name) params.name = query.name
    
    const res = await request.get('/user/page', { params })
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    phone: '',
    name: '',
    idCard: '',
    relatedIdCard: '',
    userType: 1,
    workerType: null,
    password: '',
    address: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    phone: row.phone,
    name: row.name,
    idCard: row.idCard || '',
    relatedIdCard: row.relatedIdCard || '',
    userType: row.userType,
    workerType: row.workerType || null,
    password: '',
    address: row.address || ''
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await request.put(`/user/${form.id}`, {
        name: form.name,
        idCard: form.idCard,
        relatedIdCard: form.relatedIdCard,
        address: form.address,
        workerType: form.userType === 3 ? form.workerType : null
      })
      ElMessage.success('更新成功')
    } else {
      await request.post('/user', {
        phone: form.phone,
        name: form.name,
        idCard: form.idCard,
        relatedIdCard: form.relatedIdCard,
        userType: form.userType,
        workerType: form.userType === 3 ? form.workerType : null,
        address: form.address
        // 不传password，后端会自动使用身份证后6位
      })
      ElMessage.success('新增成功，初始密码为身份证后6位')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  } finally {
    submitLoading.value = false
  }
}

const handleResetPwd = async (row) => {
  const defaultPwd = row.idCard && row.idCard.length >= 6 ? row.idCard.substring(row.idCard.length - 6) : '123456'
  try {
    await ElMessageBox.confirm(`确定要重置用户 ${row.name} 的密码为 ${defaultPwd} 吗？`, '提示', {
      type: 'warning'
    })
    await request.post(`/user/${row.id}/reset-password`, {})
    ElMessage.success(`密码已重置为 ${defaultPwd}`)
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}用户 ${row.name} 吗？`, '提示', {
      type: 'warning'
    })
    await request.post(`/user/${row.id}/status`, { status: newStatus })
    ElMessage.success(`${action}成功`)
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

// 积分管理
const handlePoints = async (row) => {
  pointsForm.userId = row.id
  pointsForm.type = 'recharge'
  pointsForm.amount = 100
  pointsForm.remark = ''
  pointsDialogVisible.value = true
  
  // 加载用户积分信息
  try {
    currentUserPoints.value = await request.get(`/admin/points/user/${row.id}`)
    const res = await request.get(`/admin/points/user/${row.id}/transactions`, { params: { size: 50 } })
    transactionList.value = res.records || []
  } catch (e) {
    console.error(e)
  }
}

const submitPointsOp = async () => {
  if (pointsForm.amount <= 0) {
    ElMessage.warning('积分数量必须大于0')
    return
  }
  
  pointsLoading.value = true
  try {
    const url = pointsForm.type === 'recharge' ? '/admin/points/recharge' : '/admin/points/deduct'
    await request.post(url, {
      userId: pointsForm.userId,
      amount: pointsForm.amount,
      remark: pointsForm.remark || (pointsForm.type === 'recharge' ? '管理员充值' : '管理员扣除')
    })
    ElMessage.success(pointsForm.type === 'recharge' ? '充值成功' : '扣除成功')
    
    // 刷新积分信息
    currentUserPoints.value = await request.get(`/admin/points/user/${pointsForm.userId}`)
    const res = await request.get(`/admin/points/user/${pointsForm.userId}/transactions`, { params: { size: 50 } })
    transactionList.value = res.records || []
  } catch (e) {
    console.error(e)
  } finally {
    pointsLoading.value = false
  }
}

onMounted(() => {
  loadData()
})

// 紧急联系人管理
const handleContacts = async (row) => {
  currentElderly.value = row
  contactForm.searchType = 'phone'
  contactForm.searchValue = ''
  contactsDialogVisible.value = true
  await loadContacts(row.id)
}

const loadContacts = async (elderlyId) => {
  contactsLoading.value = true
  try {
    contactsList.value = await request.get(`/user/admin/contacts/${elderlyId}`) || []
  } catch (e) {
    console.error(e)
    contactsList.value = []
  } finally {
    contactsLoading.value = false
  }
}

const handleBindContact = async () => {
  if (!contactForm.searchValue) {
    ElMessage.warning(contactForm.searchType === 'phone' ? '请输入手机号' : '请输入身份证号')
    return
  }
  
  bindLoading.value = true
  try {
    const params = {
      elderlyId: currentElderly.value.id
    }
    if (contactForm.searchType === 'phone') {
      params.contactPhone = contactForm.searchValue
    } else {
      params.contactIdCard = contactForm.searchValue
    }
    
    await request.post('/user/admin/bindContact', params)
    ElMessage.success('绑定成功')
    contactForm.searchValue = ''
    await loadContacts(currentElderly.value.id)
  } catch (e) {
    console.error(e)
  } finally {
    bindLoading.value = false
  }
}

const handleUnbindContact = async (contact) => {
  try {
    await ElMessageBox.confirm(`确定要解绑联系人 ${contact.name} 吗？`, '提示', {
      type: 'warning'
    })
    await request.post('/user/admin/unbindContact', {
      elderlyId: currentElderly.value.id,
      contactId: contact.id
    })
    ElMessage.success('解绑成功')
    await loadContacts(currentElderly.value.id)
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}
</script>

<style scoped>
.user-manage {
  padding: 20px;
}

.points-info {
  margin-bottom: 20px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  margin-top: 4px;
}

.contacts-header {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.empty-contacts {
  text-align: center;
  padding: 20px;
  color: #909399;
  font-size: 14px;
}

.contact-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 10px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
}
</style>
