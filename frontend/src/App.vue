<template>
  <div class="common-layout">
    <el-container>
      <!-- 顶部标题 -->
      <el-header class="header">
        <h2>Linux 进程管理模拟系统 (Group Leader Ver.)</h2>
        <div class="status-bar">
          <el-tag size="large" effect="dark">当前时间: {{ systemStatus.currentTime }}s</el-tag>
          <el-tag size="large" :type="systemStatus.isRunning ? 'success' : 'danger'" style="margin-left: 10px">
            {{ systemStatus.isRunning ? '运行中' : '已暂停' }}
          </el-tag>
        </div>
      </el-header>

      <el-main>
        <!-- 第一行：控制面板 -->
        <el-card class="control-panel">
          <el-form :inline="true">
            <el-form-item label="调度算法">
              <el-select v-model="algorithm" placeholder="选择算法" style="width: 180px" @change="changeAlgorithm">
                <el-option label="先来先服务 (FCFS)" value="FCFS"/>
                <el-option label="优先级调度 (PSA)" value="PSA"/>
                <el-option label="时间片轮转 (RR)" value="RR"/>
                <el-option label="最短剩余时间 (SRTF)" value="SRTF"/>
              </el-select>
            </el-form-item>
            <el-form-item label="时间片(s)" v-if="algorithm === 'RR'">
              <el-input-number v-model="timeSlice" :min="1" :max="10"/>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="dialogVisible = true">添加进程</el-button>
              <el-button type="success" @click="toggleRun">{{ systemStatus.isRunning ? '暂停' : '开始' }}</el-button>
              <el-button type="warning" @click="resetSystem">清空</el-button>
              <el-button type="info" @click="replaySystem">重放</el-button>
              <el-button type="danger" @click="blockProcess" :disabled="!systemStatus.runningProcess">手动阻塞
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 第二行：可视化区域 -->
        <el-row :gutter="20" style="margin-top: 20px">
          <!-- 后备队列 -->
          <el-col :span="6">
            <el-card header="后备队列 (Backup)">
              <div class="queue-box">
                <div v-for="p in systemStatus.backupQueue" :key="p.pid" class="process-block"
                     :style="{background: p.color}">
                  <div class="block-header">
                    <span class="pid-text">{{ p.name }}</span>
                    <el-icon class="close-btn" @click.stop="killProcess(p.pid)">
                      <Close/>
                    </el-icon>
                  </div>
                  <div class="block-info">Arr: {{ p.arrivalTime }}</div>
                </div>
                <div v-if="systemStatus.backupQueue.length === 0" class="empty-tip">空</div>
              </div>
            </el-card>
          </el-col>

          <!-- 就绪队列 -->
          <el-col :span="6">
            <el-card header="就绪队列 (Ready)">
              <div class="queue-box">
                <div v-for="p in systemStatus.readyQueue" :key="p.pid" class="process-block"
                     :style="{background: p.color}">
                  <div class="block-header">
                    <span class="pid-text">{{ p.name }}</span>
                    <el-icon class="close-btn" @click.stop="killProcess(p.pid)">
                      <Close/>
                    </el-icon>
                  </div>
                  <div class="block-info">
                    <span v-if="algorithm === 'PSA'">Pri: {{ p.priority }}</span>
                    <span v-else-if="algorithm === 'SRTF'">Rem: {{ p.remainingTime }}</span>
                    <span v-else>Wait</span>
                  </div>
                </div>
                <div v-if="systemStatus.readyQueue.length === 0" class="empty-tip">空</div>
              </div>
            </el-card>
          </el-col>

          <!-- CPU 运行区 -->
          <el-col :span="6">
            <el-card header="CPU (Running)" class="cpu-card">
              <div v-if="systemStatus.runningProcess" style="width: 100%; text-align: center;">
                <div class="process-block running"
                     :style="{background: systemStatus.runningProcess.color, margin: '0 auto 15px auto'}">
                  <div class="block-header" style="justify-content: center; position: relative;">
                    <h3>{{ systemStatus.runningProcess.name }}</h3>
                    <el-icon class="close-btn-cpu" @click.stop="killProcess(systemStatus.runningProcess.pid)">
                      <Close/>
                    </el-icon>
                  </div>
                  <p>剩余: {{ systemStatus.runningProcess.remainingTime }}s</p>
                </div>
                <el-progress :percentage="calcProgress(systemStatus.runningProcess)" :stroke-width="18" text-inside/>
              </div>
              <div v-else class="cpu-idle">CPU 空闲</div>
            </el-card>
          </el-col>

          <!-- 阻塞队列 -->
          <el-col :span="6">
            <el-card header="阻塞队列 (Blocked)">
              <div class="queue-box">
                <div v-for="p in systemStatus.blockedQueue" :key="p.pid" class="process-block blocked"
                     :style="{background: p.color}">
                  <div class="block-header">
                    <span class="pid-text">{{ p.name }}</span>
                    <el-icon class="close-btn" @click.stop="killProcess(p.pid)">
                      <Close/>
                    </el-icon>
                  </div>
                  <el-button size="small" circle @click="wakeUp(p.pid)" style="margin-top:5px; transform: scale(0.8)">
                    唤醒
                  </el-button>
                </div>
                <div v-if="systemStatus.blockedQueue.length === 0" class="empty-tip">空</div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 第三行：结果统计表格 -->
        <el-card header="运行结果统计" style="margin-top: 20px">
          <el-table :data="allProcesses" style="width: 100%" height="250">
            <el-table-column prop="pid" label="PID" width="80"/>
            <el-table-column prop="name" label="进程名" width="100"/>
            <el-table-column prop="arrivalTime" label="到达时间" width="100"/>
            <el-table-column prop="serviceTime" label="服务时间" width="100"/>
            <el-table-column prop="priority" label="优先级" width="80"/>
            <el-table-column prop="finishTime" label="完成时间" width="100">
              <template #default="scope">
                {{ scope.row.finishTime > 0 ? scope.row.finishTime : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="turnAroundTime" label="周转时间" width="100"/>
            <el-table-column prop="weightedTurnAroundTime" label="带权周转" width="100">
              <template #default="scope">
                {{ scope.row.weightedTurnAroundTime ? scope.row.weightedTurnAroundTime.toFixed(2) : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="state" label="状态">
              <template #default="scope">
                <el-tag :type="getStateType(scope.row.state)">{{ scope.row.state }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

      </el-main>
    </el-container>

    <!-- 添加进程弹窗 -->
    <el-dialog v-model="dialogVisible" title="添加新进程" width="30%">
      <el-form :model="newProcess" label-width="100px">
        <el-form-item label="进程名">
          <el-input v-model="newProcess.name"/>
        </el-form-item>
        <el-form-item label="到达时间">
          <el-input-number v-model="newProcess.arrivalTime" :min="0"/>
        </el-form-item>
        <el-form-item label="服务时间">
          <el-input-number v-model="newProcess.serviceTime" :min="1"/>
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="newProcess.priority" :min="1"/>
        </el-form-item>
        <el-form-item label="颜色">
          <el-color-picker v-model="newProcess.color"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitProcess">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, onMounted, computed} from 'vue'
import axios from 'axios'
import {ElMessage} from 'element-plus'
import {Close} from '@element-plus/icons-vue' // 引入关闭图标

// === 基础数据 ===
// 关键修改：使用相对路径，自动适配服务器 IP
const API_URL = '/api'
const dialogVisible = ref(false)
const algorithm = ref('FCFS')
const timeSlice = ref(2)
const timer = ref(null)

// 新进程表单
const newProcess = ref({
  name: 'P1',
  arrivalTime: 0,
  serviceTime: 5,
  priority: 1,
  color: '#409EFF'
})

// 系统状态 (从后端获取)
const systemStatus = ref({
  currentTime: 0,
  isRunning: false,
  runningProcess: null,
  readyQueue: [],
  blockedQueue: [],
  finishedQueue: [],
  backupQueue: []
})

// 计算属性：所有进程列表 (用于表格展示)
const allProcesses = computed(() => {
  let list = []
  if (systemStatus.value.runningProcess) list.push(systemStatus.value.runningProcess)
  list = list.concat(systemStatus.value.readyQueue)
  list = list.concat(systemStatus.value.blockedQueue)
  list = list.concat(systemStatus.value.finishedQueue)
  list = list.concat(systemStatus.value.backupQueue)
  return list.sort((a, b) => a.pid - b.pid)
})

// === API 调用 ===

const fetchStatus = async () => {
  try {
    const res = await axios.get(`${API_URL}/status`)
    if (res.data.code === 200) {
      systemStatus.value = res.data.data
    }
  } catch (e) {
    console.error(e)
  }
}

const tick = async () => {
  if (systemStatus.value.isRunning) {
    await axios.post(`${API_URL}/control/tick`)
    await fetchStatus()
  }
}

const submitProcess = async () => {
  const pid = Math.floor(Math.random() * 1000)
  const p = {...newProcess.value, pid}
  await axios.post(`${API_URL}/process`, p)
  dialogVisible.value = false
  ElMessage.success('进程添加成功')
  fetchStatus()
}

const toggleRun = async () => {
  const targetState = !systemStatus.value.isRunning
  await axios.post(`${API_URL}/control/run?run=${targetState}`)
  fetchStatus()
}

const resetSystem = async () => {
  await axios.post(`${API_URL}/control/reset`)
  fetchStatus()
  ElMessage.success('系统已清空')
}

const replaySystem = async () => {
  let all = []
  all = all.concat(systemStatus.value.finishedQueue)
  all = all.concat(systemStatus.value.readyQueue)
  all = all.concat(systemStatus.value.blockedQueue)
  if (systemStatus.value.runningProcess) all.push(systemStatus.value.runningProcess)
  all = all.concat(systemStatus.value.backupQueue)

  if (all.length === 0) return ElMessage.warning('没有进程可重放')

  await axios.post(`${API_URL}/control/reset`)

  for (let p of all) {
    // 修复：为重放的进程生成新的随机 PID
    // 如果你想保留原 PID，也可以写 pid: p.pid
    const newPid = Math.floor(Math.random() * 10000)

    const newP = {
      pid: newPid, // <--- 关键！加上这一行
      name: p.name,
      arrivalTime: p.arrivalTime,
      serviceTime: p.serviceTime,
      priority: p.priority,
      color: p.color
    }
    await axios.post(`${API_URL}/process`, newP)
  }

  fetchStatus()
  ElMessage.success('进程已重置，请点击开始')
}

const changeAlgorithm = async () => {
  await axios.post(`${API_URL}/config/algorithm?algorithm=${algorithm.value}&timeSlice=${timeSlice.value}`)
  ElMessage.info(`算法已切换为 ${algorithm.value}`)
}

const blockProcess = async () => {
  await axios.post(`${API_URL}/process/block`)
  fetchStatus()
}

const wakeUp = async (pid) => {
  await axios.post(`${API_URL}/process/wake?pid=${pid}`)
  fetchStatus()
}

// 新增：撤销进程
const killProcess = async (pid) => {
  try {
    await axios.post(`${API_URL}/process/kill?pid=${pid}`)
    ElMessage.success(`进程 ${pid} 已被强制撤销`)
    fetchStatus()
  } catch (e) {
    console.error(e)
  }
}

// === 辅助函数 ===
const calcProgress = (p) => {
  if (!p) return 0
  return Math.floor((p.usedTime / p.serviceTime) * 100)
}

const getStateType = (state) => {
  switch (state) {
    case 'RUNNING':
      return 'success'
    case 'READY':
      return 'primary'
    case 'BLOCKED':
      return 'warning'
    case 'FINISHED':
      return 'info'
    default:
      return ''
  }
}

// === 初始化 ===
onMounted(() => {
  fetchStatus()
  timer.value = setInterval(() => {
    tick()
    if (!systemStatus.value.isRunning) fetchStatus()
  }, 1000)
})
</script>

<style scoped>
.header {
  background: #fff;
  border-bottom: 1px solid #ddd;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.control-panel {
  margin-bottom: 20px;
}

.queue-box {
  min-height: 100px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 10px;
  background: #f9f9f9;
  border-radius: 4px;
}

/* 进程卡片样式优化 */
.process-block {
  width: 80px;
  height: 80px;
  color: #fff;
  padding: 5px;
  border-radius: 4px;
  font-size: 12px;
  display: flex;
  flex-direction: column;
  justify-content: space-between; /* 上下分布 */
  align-items: center;
  text-align: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: relative; /* 为了定位删除按钮 */
}

.block-header {
  display: flex;
  justify-content: space-between;
  width: 100%;
  padding: 0 2px;
}

.pid-text {
  font-weight: bold;
}

.close-btn {
  cursor: pointer;
  font-size: 14px;
}

.close-btn:hover {
  color: red;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 50%;
}

.close-btn-cpu {
  position: absolute;
  right: 5px;
  top: 5px;
  cursor: pointer;
  font-size: 16px;
}

.close-btn-cpu:hover {
  color: red;
}

.cpu-card {
  text-align: center;
  min-height: 200px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.process-block.running {
  width: 100%;
  height: 100px;
  font-size: 16px;
  justify-content: center;
}

.cpu-idle {
  color: #999;
  font-size: 20px;
  padding: 40px;
}

.empty-tip {
  color: #ccc;
  width: 100%;
  text-align: center;
  line-height: 100px;
}
</style>