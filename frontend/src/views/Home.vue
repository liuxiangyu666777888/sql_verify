<template>
  <div>
    <h2>欢迎使用 SQL 在线判题系统</h2>
    <el-row :gutter="20" style="margin-top: 24px">
      <el-col :span="6" v-for="card in cards" :key="card.title">
        <el-card shadow="hover" style="text-align: center; cursor: pointer" @click="$router.push(card.path)">
          <el-icon :size="40" :color="card.color"><component :is="card.icon" /></el-icon>
          <h3>{{ card.title }}</h3>
          <p style="color: #999">{{ card.desc }}</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '../stores/user'

const store = useUserStore()
const role = computed(() => store.user?.role)

const cards = computed(() => {
  const all = [
    { title: '题库练习', desc: '在线练习 SQL', icon: 'List', color: '#409EFF', path: '/questions', role: [0,1,2,3] },
    { title: '考试中心', desc: '参加限时考试', icon: 'Timer', color: '#E6A23C', path: '/exams', role: [0,1,2,3] },
    { title: '提交记录', desc: '查看提交与判题', icon: 'Document', color: '#67C23A', path: '/submits', role: [0,1,2,3] },
    { title: '班级管理', desc: '管理班级与学生', icon: 'School', color: '#F56C6C', path: '/classes', role: [1,2] },
    { title: '统计面板', desc: '教学数据概览', icon: 'DataAnalysis', color: '#909399', path: '/dashboard', role: [1,3] },
    { title: '社区', desc: '交流与讨论', icon: 'ChatDotRound', color: '#673AB7', path: '/community', role: [0,1,2,3] }
  ]
  return all.filter(c => c.role.includes(role.value))
})
</script>
