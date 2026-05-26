<template>
  <el-container style="height: 100vh">
    <el-aside width="200px" style="background: #304156">
      <div style="color: #fff; text-align: center; padding: 16px; font-size: 18px; font-weight: bold">
        SQL OJ
      </div>
      <el-menu
        :default-active="route.path"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
      >
        <el-menu-item index="/home">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/questions">
          <el-icon><List /></el-icon>
          <span>题库</span>
        </el-menu-item>
        <el-menu-item index="/exams">
          <el-icon><Timer /></el-icon>
          <span>考试</span>
        </el-menu-item>
        <el-menu-item index="/submits">
          <el-icon><Document /></el-icon>
          <span>提交记录</span>
        </el-menu-item>
        <el-menu-item v-if="role === 1 || role === 2" index="/classes">
          <el-icon><School /></el-icon>
          <span>班级管理</span>
        </el-menu-item>
        <el-menu-item v-if="role === 1 || role === 3" index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>统计面板</span>
        </el-menu-item>
        <el-menu-item index="/community">
          <el-icon><ChatDotRound /></el-icon>
          <span>社区</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #ddd">
        <span style="font-size: 16px">SQL 在线判题系统</span>
        <div>
          <el-tag style="margin-right: 12px">{{ roleName }}</el-tag>
          <span>{{ username }}</span>
          <el-button type="danger" text style="margin-left: 12px" @click="handleLogout">退出</el-button>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { logout as logoutApi } from '../api/auth'

const router = useRouter()
const route = useRoute()
const store = useUserStore()

const username = computed(() => store.user?.username || '')
const role = computed(() => store.user?.role)
const roleName = computed(() => store.roleName)

async function handleLogout() {
  try { await logoutApi() } catch {}
  store.logout()
  router.push('/login')
}
</script>
