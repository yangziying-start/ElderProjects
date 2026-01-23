<template>
  <div class="admin-manage">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="管理员列表" name="admins">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>管理员列表</span>
              <el-button type="primary" @click="handleAddAdmin" v-if="isSuperAdmin">新增管理员</el-button>
            </div>
          </template>
          
          <el-table :data="adminList" v-loading="loading" stripe>
            <el-table-column type="index" label="序号" width="70" />
            <el-table-column prop="name" label="姓名" width="120" />
            <el-table-column prop="phone" label="手机号" width="140" />
            <el-table-column prop="adminRole" label="角色" width="120">
              <template #default="{ row }">
                <el-tag :type="row.adminRole === 1 ? 'danger' : 'primary'">
                  {{ row.adminRole === 1 ? '超级管理员' : '普通管理员' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '正常' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180" />
            <el-table-column label="操作" width="280" fixed="right" v-if="isSuperAdmin">
              <template #default="{ row }">
                <template v-if="row.adminRole !== 1">
                  <el-button type="primary" link @click="handleEditAdmin(row)">编辑</el-button>
                  <el-button type="warning" link @click="handleResetPwd(row)">重置密码</el-button>
                  <el-button 
                    :type="row.status === 1 ? 'danger' : 'success'" 
                    link 
                    @click="handleToggleStatus(row)"
                  >
                    {{ row.status === 1 ? '禁用' : '启用' }}
                  </el-button>
                  <el-button type="danger" link @click="handleDeleteAdmin(row)">删除</el-button>
                </template>
                <span v-else class="super-admin-tip">超级管理员</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
      
      <el-tab-pane label="操作日志" name="logs">
        <el-card>
          <template #header>
            <el-form :inline="true" :model="logQuery">
              <el-form-item label="管理员" v-if="isSuperAdmin">
                <el-select v-model="logQuery.adminId" placeholder="全部" clearable style="width: 150px">
                  <el-option v-for="admin in adminList" :key="admin.id" :label="admin.name" :value="admin.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="模块">
                <el-select v-model="logQuery.module" placeholder="全部" clearable style="width: 150px">
                  <el-option label="管理员管理" value="管理员管理" />
                  <el-option label="用户管理" value="用户管理" />
                  <el-option label="订单管理" value="订单管理" />
                  <el-option label="服务管理" value="服务管理" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="loadLogs">查询</el-button>
              </el-form-item>
            </el-form>
          </template>
          
          <el-table :data="logList" v-loading="logLoading" stripe>
            <el-table-column type="index" label="序号" width="70" />
            <el-table-column prop="adminName" label="操作人" width="100" />
            <el-table-column prop="module" label="模块" width="120" />
            <el-table-column prop="description" label="操作描述" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ row.status === 1 ? '成功' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="操作时间" width="180" />
          </el-table>
          
          <el-pagination
            v-model:current-page="logQuery.page"
            v-model:page-size="logQuery.size"
            :total="logTotal"
            :page-sizes="[20, 50, 100]"
            layout="total, sizes, prev, pager, next"
            @current-change="loadLogs"
            @size-change="loadLogs"
            style="margin-top: 20px"
          />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 新增/编辑管理员弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑管理员' : '新增管理员'" 
      width="450px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" :disabled="isEdit" maxlength="11" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" maxlength="20" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="角色">
          <el-tag type="primary">普通管理员</el-tag>
          <div class="form-tip">新增的管理员默认为普通管理员</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const activeTab = ref('admins')
const loading = ref(false)
const logLoading = ref(false)
const submitLoading = ref(false)
const adminList = ref([])
const logList = ref([])
const logTotal = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const currentAdmin = ref(null)

const isSuperAdmin = computed(() => currentAdmin.value?.adminRole === 1)

const logQuery = reactive({
  adminId: null,
  module: '',
  page: 1,
  size: 20
})

const form = reactive({
  id: null,
  phone: '',
  name: '',
  password: ''
})

const rules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
}

const loadCurrentAdmin = async () => {
  try {
    currentAdmin.value = await request.get('/admin/user/current')
  } catch (e) {
    console.error(e)
  }
}

const loadAdmins = async () => {
  loading.value = true
  try {
    adminList.value = await request.get('/admin/user/admins') || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const loadLogs = async () => {
  logLoading.value = true
  try {
    const params = {
      page: logQuery.page,
      size: logQuery.size
    }
    if (logQuery.adminId) params.adminId = logQuery.adminId
    if (logQuery.module) params.module = logQuery.module
    
    const res = await request.get('/admin/user/logs', { params })
    logList.value = res.records || []
    logTotal.value = res.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    logLoading.value = false
  }
}

const handleAddAdmin = () => {
  isEdit.value = false
  Object.assign(form, { id: null, phone: '', name: '', password: '' })
  dialogVisible.value = true
}

const handleEditAdmin = (row) => {
  isEdit.value = true
  Object.assign(form, { id: row.id, phone: row.phone, name: row.name, password: '' })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await request.put(`/admin/user/admin/${form.id}`, { name: form.name })
      ElMessage.success('修改成功')
    } else {
      await request.post('/admin/user/admin', {
        phone: form.phone,
        name: form.name,
        password: form.password,
        adminRole: 2
      })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadAdmins()
  } catch (e) {
    console.error(e)
  } finally {
    submitLoading.value = false
  }
}

const handleResetPwd = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入新密码（至少6位）', '重置密码', {
      inputPattern: /^.{6,}$/,
      inputErrorMessage: '密码至少6位',
      inputValue: '123456'
    })
    await request.post(`/admin/user/admin/${row.id}/reset-password`, { password: value })
    ElMessage.success('密码已重置')
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action}管理员 ${row.name} 吗？`, '提示', { type: 'warning' })
    await request.post(`/admin/user/admin/${row.id}/status`, { status: newStatus })
    ElMessage.success(`${action}成功`)
    loadAdmins()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const handleDeleteAdmin = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除管理员 ${row.name} 吗？此操作不可恢复！`, '警告', { type: 'error' })
    await request.delete(`/admin/user/admin/${row.id}`)
    ElMessage.success('删除成功')
    loadAdmins()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

onMounted(async () => {
  await loadCurrentAdmin()
  loadAdmins()
  loadLogs()
})
</script>

<style scoped>
.admin-manage {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.super-admin-tip {
  color: #909399;
  font-size: 12px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
