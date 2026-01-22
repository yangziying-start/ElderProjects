<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="aside">
      <div class="logo">适老化社区服务</div>
      <el-menu
        :default-active="route.path"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据概览</span>
        </el-menu-item>
        <el-menu-item index="/user">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/service">
          <el-icon><Service /></el-icon>
          <span>服务管理</span>
        </el-menu-item>
        <el-menu-item index="/order">
          <el-icon><List /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/emergency">
          <el-icon><Bell /></el-icon>
          <span>应急指挥中心</span>
        </el-menu-item>
        <el-sub-menu index="module">
          <template #title>
            <el-icon><Menu /></el-icon>
            <span>模块管理</span>
          </template>
          <el-menu-item index="/meal">
            <el-icon><Food /></el-icon>
            <span>餐饮管理</span>
          </el-menu-item>
          <el-menu-item index="/cleaning">
            <el-icon><Brush /></el-icon>
            <span>保洁管理</span>
          </el-menu-item>
          <el-menu-item index="/medical">
            <el-icon><FirstAidKit /></el-icon>
            <span>医疗管理</span>
          </el-menu-item>
          <el-menu-item index="/building">
            <el-icon><OfficeBuilding /></el-icon>
            <span>楼栋管理</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span>{{ route.meta.title }}</span>
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            管理员 <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { Menu, Food, Brush, FirstAidKit, OfficeBuilding } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('token')
    router.push('/login')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100%;
}

.aside {
  background-color: #304156;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #ffffff;
  font-size: 18px;
  font-weight: bold;
}

.header {
  background: #ffffff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.main {
  background: #f0f2f5;
}
</style>
