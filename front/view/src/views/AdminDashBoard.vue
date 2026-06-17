<template>
  <el-container class="admin-dashboard">
    <!-- 侧边栏 -->
    <el-aside width="260px" class="sidebar">
      <div class="sidebar-header">
        <el-avatar :size="80" src="https://api.dicebear.com/7.x/avataaars/svg?seed=Admin&backgroundColor=b6e3f4" />
        <h2>管理员</h2>
        <p>健身房管理系统</p>
      </div>

      <el-menu
        :default-active="currentSection"
        @select="switchSection"
        class="sidebar-menu"
      >
        <el-menu-item
          v-for="item in menuItems"
          :key="item.section"
          :index="item.section"
        >
          <span class="menu-icon">{{ item.icon }}</span>
          <span>{{ item.text }}</span>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-footer">
        <el-button type="danger" @click="logout" class="logout-btn">退出登录</el-button>
      </div>
    </el-aside>

    <!-- 主内容区 -->
    <el-container class="main-content">
      <el-header class="top-bar">
        <h1>{{ pageTitle }}</h1>
        <div class="admin-info">
          <span>管理员：{{ adminName }}</span>
        </div>
      </el-header>

      <el-main class="content-area">
        <!-- 首页 - 数据大屏 -->
        <div v-show="currentSection === 'dashboard'" class="content-section">
          <el-row :gutter="20" class="dashboard-grid">
            <el-col :xs="24" :sm="8">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-content">
                  <div class="stat-icon">👥</div>
                  <div class="stat-info">
                    <div class="stat-value">{{ totalMembers }}</div>
                    <div class="stat-label">会员总人数</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="8">
              <el-card class="stat-card" shadow="hover" :class="{ 'alert-card': isOverCapacity }">
                <div class="stat-content">
                  <div class="stat-icon">🏃</div>
                  <div class="stat-info">
                    <div class="stat-value" :class="{ 'alert-text': isOverCapacity }">{{ currentCount }}</div>
                    <div class="stat-label">当前在馆人数</div>
                    <div v-if="isOverCapacity" class="alert-badge">⚠️ 人数超标</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :xs="24" :sm="8">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-content">
                  <div class="stat-icon">📊</div>
                  <div class="stat-info">
                    <div class="stat-value">{{ todayVisits }}</div>
                    <div class="stat-label">今日访问次数</div>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <!-- 折线图 -->
          <el-card class="chart-container" shadow="hover">
            <template #header>
              <h3>近7天每日进入人数</h3>
            </template>
            <canvas ref="chartCanvas" width="800" height="400"></canvas>
          </el-card>
        </div>

        <!-- 人数检测 -->
        <div v-show="currentSection === 'monitor'" class="content-section">
          <el-row :gutter="20">
            <el-col :span="16">
              <el-card class="video-wrapper" shadow="hover">
                <img
                  :src="videoStreamUrl"
                  alt="实时监控"
                  class="video-stream"
                  @error="handleVideoError"
                />
                <el-alert
                  v-if="videoError"
                  title="视频流加载失败，请确保CountService已启动"
                  type="error"
                  :closable="false"
                  show-icon
                  style="position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); width: auto;"
                />
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card class="monitor-info" shadow="hover">
                <div class="count-display" :class="{ warning: isOverCapacity }">
                  <div class="count-number">{{ currentCount }}</div>
                  <div class="count-label">当前人数</div>
                </div>
                <el-divider />
                <div class="capacity-info">
                  <p>场馆容量：{{ capacity }} 人</p>
                  <el-alert
                    v-if="isOverCapacity"
                    title="⚠️ 人数异常！"
                    type="error"
                    :closable="false"
                    show-icon
                  />
                  <el-alert
                    v-else
                    title="✅ 人数正常"
                    type="success"
                    :closable="false"
                    show-icon
                  />
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- 用户信息管理 -->
        <div v-show="currentSection === 'users'" class="content-section">
          <el-card class="table-container" shadow="hover">
            <template #header>
              <div class="table-header">
                <h3>用户列表</h3>
                <el-input
                  v-model="userSearchKeyword"
                  placeholder="搜索用户名..."
                  prefix-icon="Search"
                  clearable
                  style="width: 250px;"
                  @input="searchUsers"
                />
              </div>
            </template>
            <el-table :data="userList" style="width: 100%" stripe>
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="username" label="用户名" />
              <el-table-column prop="email" label="邮箱" />
              <el-table-column label="会员状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.membershipEndTime ? 'success' : 'info'">
                    {{ row.membershipEndTime ? '会员' : '非会员' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="membershipEndTime" label="会员到期时间" />
              <el-table-column prop="createdTime" label="注册时间" />
              <el-table-column label="操作" width="150">
                <template #default="{ row }">
                  <el-button size="small" @click="viewUser(row)">查看</el-button>
                  <el-button size="small" type="danger" @click="deleteUser(row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              v-model:current-page="userPageNum"
              :page-size="userPageSize"
              :total="userTotal"
              layout="prev, pager, next"
              @current-change="loadUsers"
              style="margin-top: 20px; justify-content: center;"
            />
          </el-card>
        </div>

        <!-- 资源管理 -->
        <div v-show="currentSection === 'resources'" class="content-section">
          <el-tabs v-model="currentResourceTab" @tab-change="loadResources">
            <el-tab-pane label="器材管理" name="equipment">
              <el-card shadow="hover">
                <template #header>
                  <div class="panel-header">
                    <h3>器材列表</h3>
                    <el-button type="primary" @click="showAddEquipmentModal">
                      <el-icon><Plus /></el-icon> 添加器材
                    </el-button>
                  </div>
                </template>
                <el-table :data="equipmentList" style="width: 100%" stripe>
                  <el-table-column prop="id" label="ID" width="80" />
                  <el-table-column prop="name" label="名称" />
                  <el-table-column prop="num" label="数量" width="100" />
                  <el-table-column label="需教练" width="100">
                    <template #default="{ row }">
                      <el-tag :type="row.needCoach === 1 ? 'warning' : 'success'">
                        {{ row.needCoach === 1 ? '是' : '否' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="150">
                    <template #default="{ row }">
                      <el-button size="small" @click="editEquipment(row)">编辑</el-button>
                      <el-button size="small" type="danger" @click="deleteEquipment(row.id)">删除</el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <el-pagination
                  v-model:current-page="equipmentPageNum"
                  :page-size="equipmentPageSize"
                  :total="equipmentTotal"
                  layout="prev, pager, next"
                  @current-change="loadEquipment"
                  style="margin-top: 20px; justify-content: center;"
                />
              </el-card>
            </el-tab-pane>

            <el-tab-pane label="教练管理" name="coach">
              <el-card shadow="hover">
                <template #header>
                  <div class="panel-header">
                    <h3>教练列表</h3>
                    <el-button type="primary" @click="showAddCoachModal">
                      <el-icon><Plus /></el-icon> 添加教练
                    </el-button>
                  </div>
                </template>
                <el-table :data="coachList" style="width: 100%" stripe>
                  <el-table-column prop="id" label="ID" width="80" />
                  <el-table-column prop="name" label="姓名" />
                  <el-table-column prop="specialty" label="专长" />
                  <el-table-column prop="description" label="简介" show-overflow-tooltip />
                  <el-table-column label="操作" width="150">
                    <template #default="{ row }">
                      <el-button size="small" @click="editCoach(row)">编辑</el-button>
                      <el-button size="small" type="danger" @click="deleteCoach(row.id)">删除</el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <el-pagination
                  v-model:current-page="coachPageNum"
                  :page-size="coachPageSize"
                  :total="coachTotal"
                  layout="prev, pager, next"
                  @current-change="loadCoaches"
                  style="margin-top: 20px; justify-content: center;"
                />
              </el-card>
            </el-tab-pane>

            <el-tab-pane label="套餐管理" name="package">
              <el-card shadow="hover">
                <template #header>
                  <div class="panel-header">
                    <h3>套餐列表</h3>
                    <el-button type="primary" @click="showAddPackageModal">
                      <el-icon><Plus /></el-icon> 添加套餐
                    </el-button>
                  </div>
                </template>
                <el-table :data="packageList" style="width: 100%" stripe>
                  <el-table-column prop="id" label="ID" width="80" />
                  <el-table-column prop="name" label="名称" />
                  <el-table-column prop="duration" label="时长(天)" width="100" />
                  <el-table-column label="等级" width="100">
                    <template #default="{ row }">
                      Lv.{{ row.level }}
                    </template>
                  </el-table-column>
                  <el-table-column label="价格" width="100">
                    <template #default="{ row }">
                      ¥{{ row.price }}
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="150">
                    <template #default="{ row }">
                      <el-button size="small" @click="editPackage(row)">编辑</el-button>
                      <el-button size="small" type="danger" @click="deletePackage(row.id)">删除</el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-card>
            </el-tab-pane>
          </el-tabs>
        </div>

        <!-- 订单信息 -->
        <div v-show="currentSection === 'orders'" class="content-section">
          <el-card class="table-container" shadow="hover">
            <template #header>
              <h3>订单列表</h3>
            </template>
            <el-table :data="orderList" style="width: 100%" stripe>
              <el-table-column prop="id" label="订单ID" width="100" />
              <el-table-column prop="uid" label="用户ID" width="100" />
              <el-table-column prop="pid" label="套餐ID" width="100" />
              <el-table-column prop="orderStartTime" label="开始时间" />
              <el-table-column prop="orderEndTime" label="结束时间" />
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'info'">
                    {{ row.status === 1 ? '有效' : '已过期' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdTime" label="创建时间" />
            </el-table>
            <el-pagination
              v-model:current-page="orderPageNum"
              :page-size="orderPageSize"
              :total="orderTotal"
              layout="prev, pager, next"
              @current-change="loadOrders"
              style="margin-top: 20px; justify-content: center;"
            />
          </el-card>
        </div>

        <!-- 活动记录 -->
        <div v-show="currentSection === 'activities'" class="content-section">
          <el-card class="table-container" shadow="hover">
            <template #header>
              <h3>活动记录</h3>
            </template>
            <el-table :data="activityList" style="width: 100%" stripe>
              <el-table-column prop="id" label="记录ID" width="100" />
              <el-table-column prop="userId" label="用户ID" width="100" />
              <el-table-column label="到达时间">
                <template #default="{ row }">
                  {{ formatDateTime(row.arrivalTime) }}
                </template>
              </el-table-column>
              <el-table-column label="离开时间">
                <template #default="{ row }">
                  {{ formatDateTime(row.departureTime) }}
                </template>
              </el-table-column>
              <el-table-column label="停留时长">
                <template #default="{ row }">
                  {{ calculateDuration(row.arrivalTime, row.departureTime) }}
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              v-model:current-page="activityPageNum"
              :page-size="activityPageSize"
              :total="activityTotal"
              layout="prev, pager, next"
              @current-change="loadActivities"
              style="margin-top: 20px; justify-content: center;"
            />
          </el-card>
        </div>

        <!-- 预约记录 -->
        <div v-show="currentSection === 'bookings'" class="content-section">
          <el-card shadow="hover">
            <template #header>
              <el-tabs v-model="currentBookingTab" @tab-change="loadBookings">
                <el-tab-pane label="器材预约" name="equipment" />
                <el-tab-pane label="教练预约" name="coach" />
              </el-tabs>
            </template>
            <el-table :data="bookingList" style="width: 100%" stripe>
              <el-table-column prop="id" label="预约ID" width="100" />
              <el-table-column prop="userId" label="用户ID" width="100" />
              <el-table-column :label="currentBookingTab === 'equipment' ? '器材ID' : '教练ID'" width="100">
                <template #default="{ row }">
                  {{ row.equipmentId || row.coachId }}
                </template>
              </el-table-column>
              <el-table-column label="日期">
                <template #default="{ row }">
                  {{ formatDate(row.bookingDate) }}
                </template>
              </el-table-column>
              <el-table-column prop="slotId" label="时间段ID" width="100" />
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === 1 ? 'success' : 'info'">
                    {{ row.status === 1 ? '有效' : '已取消' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="创建时间">
                <template #default="{ row }">
                  {{ formatDateTime(row.createdTime) }}
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              v-model:current-page="bookingPageNum"
              :page-size="bookingPageSize"
              :total="bookingTotal"
              layout="prev, pager, next"
              @current-change="loadBookings"
              style="margin-top: 20px; justify-content: center;"
            />
          </el-card>
        </div>
      </el-main>
    </el-container>

    <!-- 添加/编辑器材弹窗 -->
    <el-dialog
      v-model="showEquipmentModal"
      :title="equipmentModalTitle"
      width="500px"
    >
      <el-form :model="equipmentForm" label-position="top">
        <el-form-item label="名称：">
          <el-input v-model="equipmentForm.name" placeholder="请输入器材名称" />
        </el-form-item>
        <el-form-item label="数量：">
          <el-input-number v-model="equipmentForm.num" :min="0" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="需要教练：">
          <el-radio-group v-model="equipmentForm.needCoach">
            <el-radio :value="0">否</el-radio>
            <el-radio :value="1">是</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeEquipmentModal">取消</el-button>
        <el-button type="primary" @click="submitEquipmentForm">保存</el-button>
      </template>
    </el-dialog>

    <!-- 添加/编辑教练弹窗 -->
    <el-dialog
      v-model="showCoachModal"
      :title="coachModalTitle"
      width="500px"
    >
      <el-form :model="coachForm" label-position="top">
        <el-form-item label="姓名：">
          <el-input v-model="coachForm.name" placeholder="请输入教练姓名" />
        </el-form-item>
        <el-form-item label="专长：">
          <el-input v-model="coachForm.specialty" placeholder="请输入专长" />
        </el-form-item>
        <el-form-item label="简介：">
          <el-input
            v-model="coachForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入简介"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeCoachModal">取消</el-button>
        <el-button type="primary" @click="submitCoachForm">保存</el-button>
      </template>
    </el-dialog>

    <!-- 添加/编辑套餐弹窗 -->
    <el-dialog
      v-model="showPackageModal"
      :title="packageModalTitle"
      width="500px"
    >
      <el-form :model="packageForm" label-position="top">
        <el-form-item label="名称：">
          <el-input v-model="packageForm.name" placeholder="请输入套餐名称" />
        </el-form-item>
        <el-form-item label="时长（天）：">
          <el-input-number v-model="packageForm.duration" :min="1" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="等级：">
          <el-input-number v-model="packageForm.level" :min="1" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="价格：">
          <el-input-number v-model="packageForm.price" :min="0" :precision="2" style="width: 100%;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closePackageModal">取消</el-button>
        <el-button type="primary" @click="submitPackageForm">保存</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, UserFilled } from '@element-plus/icons-vue'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

const API_BASE_URL = 'http://localhost:80'
const VIDEO_STREAM_URL = 'http://localhost:8000/py/video'

const token = localStorage.getItem('token')
const role = localStorage.getItem('role')
const adminId = localStorage.getItem('userId')

if (!token || role !== 'admin') {
  window.location.href = '/login'
}

const currentSection = ref('dashboard')
const adminName = ref('管理员')
const pageTitle = ref('数据大屏')

const menuItems = [
  { section: 'dashboard', icon: '📊', text: '首页' },
  { section: 'monitor', icon: '📹', text: '人数检测' },
  { section: 'users', icon: '👥', text: '用户信息' },
  { section: 'resources', icon: '🏋️', text: '资源管理' },
  { section: 'orders', icon: '📋', text: '订单信息' },
  { section: 'activities', icon: '📝', text: '活动记录' },
  { section: 'bookings', icon: '📅', text: '预约记录' }
]

// 首页数据
const totalMembers = ref(0)
const currentCount = ref(0)
const todayVisits = ref(0)
const weeklyData = ref([])
const chartCanvas = ref(null)

// 人数检测
const videoStreamUrl = ref(VIDEO_STREAM_URL)
const videoError = ref(false)
const capacity = ref(100)
const isOverCapacity = ref(false)

// WebSocket 客户端
let stompClient = null

// 用户管理
const userList = ref([])
const userPageNum = ref(1)
const userPageSize = ref(10)
const userTotal = ref(0)
const userSearchKeyword = ref('')

// 资源管理
const currentResourceTab = ref('equipment')
const equipmentList = ref([])
const equipmentPageNum = ref(1)
const equipmentPageSize = ref(10)
const equipmentTotal = ref(0)
const showEquipmentModal = ref(false)
const equipmentModalTitle = ref('添加器材')
const equipmentForm = reactive({ id: null, name: '', num: 0, needCoach: 0 })

const coachList = ref([])
const coachPageNum = ref(1)
const coachPageSize = ref(10)
const coachTotal = ref(0)
const showCoachModal = ref(false)
const coachModalTitle = ref('添加教练')
const coachForm = reactive({ id: null, name: '', specialty: '', description: '' })

const packageList = ref([])
const showPackageModal = ref(false)
const packageModalTitle = ref('添加套餐')
const packageForm = reactive({ id: null, name: '', duration: 30, level: 1, price: 0 })

// 订单管理
const orderList = ref([])
const orderPageNum = ref(1)
const orderPageSize = ref(10)
const orderTotal = ref(0)

// 活动记录
const activityList = ref([])
const activityPageNum = ref(1)
const activityPageSize = ref(10)
const activityTotal = ref(0)

// 预约记录
const currentBookingTab = ref('equipment')
const bookingList = ref([])
const bookingPageNum = ref(1)
const bookingPageSize = ref(10)
const bookingTotal = ref(0)

const switchSection = async (section) => {
  currentSection.value = section
  const titles = {
    'dashboard': '数据大屏',
    'monitor': '人数检测',
    'users': '用户信息',
    'resources': '资源管理',
    'orders': '订单信息',
    'activities': '活动记录',
    'bookings': '预约记录'
  }
  pageTitle.value = titles[section]

  if (section === 'dashboard') {
    await loadDashboardData()
  } else if (section === 'monitor') {
    await loadCurrentCount()
  } else if (section === 'users') {
    await loadUsers()
  } else if (section === 'resources') {
    await loadResources()
  } else if (section === 'orders') {
    await loadOrders()
  } else if (section === 'activities') {
    await loadActivities()
  } else if (section === 'bookings') {
    await loadBookings()
  }
}

// 加载首页数据
const loadDashboardData = async () => {
  await Promise.all([
    loadTotalMembers(),
    loadCurrentCount(),
    loadTodayVisits(),
    loadWeeklyData()
  ])
  drawChart()
}

const loadTotalMembers = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/api/user/page?pageNum=1&pageSize=1`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const result = await response.json()
    console.log('会员总数响应:', result)
    if (result.code === 200 && result.data) {
      totalMembers.value = result.data.total || 0
      console.log('会员总数:', totalMembers.value)
    } else {
      console.error('获取会员总数失败:', result.msg)
    }
  } catch (error) {
    console.error('加载会员总数失败:', error)
    ElMessage.error('加载会员总数失败')
  }
}

const loadCurrentCount = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/api/count/current`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const result = await response.json()
    console.log('当前人数响应:', result)
    if (result.code === 200) {
      currentCount.value = result.data || 0
      console.log('当前人数:', currentCount.value)
    } else {
      console.error('获取当前人数失败:', result.msg)
    }
  } catch (error) {
    console.error('加载当前人数失败:', error)
    ElMessage.error('加载当前人数失败')
  }
}

const loadTodayVisits = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/api/record/today-count`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const result = await response.json()
    console.log('今日访问响应:', result)
    if (result.code === 200) {
      todayVisits.value = result.data || 0
      console.log('今日访问次数:', todayVisits.value)
    } else {
      console.error('获取今日访问次数失败:', result.msg)
    }
  } catch (error) {
    console.error('加载今日访问失败:', error)
    ElMessage.error('加载今日访问失败')
  }
}

const loadWeeklyData = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/api/record/weekly-stats`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const result = await response.json()
    console.log('近7天统计响应:', result)
    if (result.code === 200 && result.data) {
      weeklyData.value = result.data.map(item => ({
        date: item.date,
        count: item.count
      }))
      console.log('近7天数据:', weeklyData.value)
    } else {
      console.error('获取近7天统计失败:', result.msg)
      weeklyData.value = [
        { date: '周一', count: 45 },
        { date: '周二', count: 52 },
        { date: '周三', count: 48 },
        { date: '周四', count: 61 },
        { date: '周五', count: 73 },
        { date: '周六', count: 89 },
        { date: '周日', count: 67 }
      ]
    }
  } catch (error) {
    console.error('加载近7天数据失败:', error)
    weeklyData.value = [
      { date: '周一', count: 45 },
      { date: '周二', count: 52 },
      { date: '周三', count: 48 },
      { date: '周四', count: 61 },
      { date: '周五', count: 73 },
      { date: '周六', count: 89 },
      { date: '周日', count: 67 }
    ]
  }
}

const drawChart = () => {
  nextTick(() => {
    if (!chartCanvas.value) return

    const canvas = chartCanvas.value
    const ctx = canvas.getContext('2d')
    const width = canvas.width
    const height = canvas.height

    ctx.clearRect(0, 0, width, height)

    const padding = 60
    const chartWidth = width - padding * 2
    const chartHeight = height - padding * 2

    const maxCount = Math.max(...weeklyData.value.map(d => d.count))

    // 绘制坐标轴
    ctx.strokeStyle = '#e0e0e0'
    ctx.lineWidth = 1
    ctx.beginPath()
    ctx.moveTo(padding, padding)
    ctx.lineTo(padding, height - padding)
    ctx.lineTo(width - padding, height - padding)
    ctx.stroke()

    // 绘制折线
    ctx.strokeStyle = '#667eea'
    ctx.lineWidth = 3
    ctx.beginPath()

    weeklyData.value.forEach((data, index) => {
      const x = padding + (index / (weeklyData.value.length - 1)) * chartWidth
      const y = height - padding - (data.count / maxCount) * chartHeight

      if (index === 0) {
        ctx.moveTo(x, y)
      } else {
        ctx.lineTo(x, y)
      }
    })
    ctx.stroke()

    // 绘制数据点
    weeklyData.value.forEach((data, index) => {
      const x = padding + (index / (weeklyData.value.length - 1)) * chartWidth
      const y = height - padding - (data.count / maxCount) * chartHeight

      ctx.fillStyle = '#667eea'
      ctx.beginPath()
      ctx.arc(x, y, 6, 0, Math.PI * 2)
      ctx.fill()

      // 绘制标签
      ctx.fillStyle = '#333'
      ctx.font = '14px Microsoft YaHei'
      ctx.textAlign = 'center'
      ctx.fillText(data.date, x, height - padding + 25)
      ctx.fillText(data.count.toString(), x, y - 15)
    })
  })
}

const handleVideoError = () => {
  videoError.value = true
}

// 加载用户列表
const loadUsers = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/api/user/page?pageNum=${userPageNum.value}&pageSize=${userPageSize.value}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const result = await response.json()
    if (result.code === 200 && result.data) {
      userList.value = result.data.records || []
      userTotal.value = result.data.total || 0
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
  }
}

const searchUsers = () => {
  // TODO: 实现搜索功能
}

const viewUser = (user) => {
  ElMessage.info(`查看用户详情：${user.username}`)
}

const deleteUser = async (userId) => {
  try {
    await ElMessageBox.confirm('确认删除该用户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await fetch(`${API_BASE_URL}/api/user/delete?id=${userId}`, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const result = await response.json()
    if (result.code === 200) {
      ElMessage.success('删除成功')
      await loadUsers()
    } else {
      ElMessage.error(result.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
      ElMessage.error('网络错误')
    }
  }
}

// 加载资源
const loadResources = async () => {
  if (currentResourceTab.value === 'equipment') {
    await loadEquipment()
  } else if (currentResourceTab.value === 'coach') {
    await loadCoaches()
  } else if (currentResourceTab.value === 'package') {
    await loadPackages()
  }
}

const loadEquipment = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/api/resource/equipment/page?pageNum=${equipmentPageNum.value}&pageSize=${equipmentPageSize.value}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const result = await response.json()
    if (result.code === 200 && result.data) {
      equipmentList.value = result.data.records || []
      equipmentTotal.value = result.data.total || 0
    }
  } catch (error) {
    console.error('加载器材列表失败:', error)
    ElMessage.error('加载器材列表失败')
  }
}

const showAddEquipmentModal = () => {
  equipmentModalTitle.value = '添加器材'
  Object.assign(equipmentForm, { id: null, name: '', num: 0, needCoach: 0 })
  showEquipmentModal.value = true
}

const editEquipment = (eq) => {
  equipmentModalTitle.value = '编辑器材'
  Object.assign(equipmentForm, { ...eq })
  showEquipmentModal.value = true
}

const closeEquipmentModal = () => {
  showEquipmentModal.value = false
}

const submitEquipmentForm = async () => {
  try {
    const url = equipmentForm.id ? `${API_BASE_URL}/api/resource/equipment/update` : `${API_BASE_URL}/api/resource/equipment/add`
    const method = equipmentForm.id ? 'PUT' : 'POST'

    const response = await fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(equipmentForm)
    })
    const result = await response.json()

    if (result.code === 200) {
      ElMessage.success('操作成功')
      closeEquipmentModal()
      await loadEquipment()
    } else {
      ElMessage.error(result.msg || '操作失败')
    }
  } catch (error) {
    console.error('提交器材表单失败:', error)
    ElMessage.error('网络错误')
  }
}

const deleteEquipment = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除该器材吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await fetch(`${API_BASE_URL}/api/resource/equipment/delete?id=${id}`, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const result = await response.json()
    if (result.code === 200) {
      ElMessage.success('删除成功')
      await loadEquipment()
    } else {
      ElMessage.error(result.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除器材失败:', error)
      ElMessage.error('网络错误')
    }
  }
}

const loadCoaches = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/api/resource/coach/page?pageNum=${coachPageNum.value}&pageSize=${coachPageSize.value}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const result = await response.json()
    if (result.code === 200 && result.data) {
      coachList.value = result.data.records || []
      coachTotal.value = result.data.total || 0
    }
  } catch (error) {
    console.error('加载教练列表失败:', error)
    ElMessage.error('加载教练列表失败')
  }
}

const showAddCoachModal = () => {
  coachModalTitle.value = '添加教练'
  Object.assign(coachForm, { id: null, name: '', specialty: '', description: '' })
  showCoachModal.value = true
}

const editCoach = (coach) => {
  coachModalTitle.value = '编辑教练'
  Object.assign(coachForm, { ...coach })
  showCoachModal.value = true
}

const closeCoachModal = () => {
  showCoachModal.value = false
}

const submitCoachForm = async () => {
  try {
    const url = coachForm.id ? `${API_BASE_URL}/api/resource/coach/update` : `${API_BASE_URL}/api/resource/coach/add`
    const method = coachForm.id ? 'PUT' : 'POST'

    const response = await fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(coachForm)
    })
    const result = await response.json()

    if (result.code === 200) {
      ElMessage.success('操作成功')
      closeCoachModal()
      await loadCoaches()
    } else {
      ElMessage.error(result.msg || '操作失败')
    }
  } catch (error) {
    console.error('提交教练表单失败:', error)
    ElMessage.error('网络错误')
  }
}

const deleteCoach = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除该教练吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await fetch(`${API_BASE_URL}/api/resource/coach/delete?id=${id}`, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const result = await response.json()
    if (result.code === 200) {
      ElMessage.success('删除成功')
      await loadCoaches()
    } else {
      ElMessage.error(result.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除教练失败:', error)
      ElMessage.error('网络错误')
    }
  }
}

const loadPackages = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/api/order/package/all`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const result = await response.json()
    if (result.code === 200 && result.data) {
      packageList.value = result.data
    }
  } catch (error) {
    console.error('加载套餐列表失败:', error)
    ElMessage.error('加载套餐列表失败')
  }
}

const showAddPackageModal = () => {
  packageModalTitle.value = '添加套餐'
  Object.assign(packageForm, { id: null, name: '', duration: 30, level: 1, price: 0 })
  showPackageModal.value = true
}

const editPackage = (pkg) => {
  packageModalTitle.value = '编辑套餐'
  Object.assign(packageForm, { ...pkg })
  showPackageModal.value = true
}

const closePackageModal = () => {
  showPackageModal.value = false
}

const submitPackageForm = async () => {
  ElMessage.warning('套餐管理接口待实现')
}

const deletePackage = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除该套餐吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    ElMessage.warning('套餐删除接口待实现')
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

// 加载订单
const loadOrders = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/api/order/page?pageNum=${orderPageNum.value}&pageSize=${orderPageSize.value}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const result = await response.json()
    if (result.code === 200 && result.data) {
      orderList.value = result.data.records || []
      orderTotal.value = result.data.total || 0
    }
  } catch (error) {
    console.error('加载订单列表失败:', error)
    ElMessage.error('加载订单列表失败')
  }
}

// 加载活动记录
const loadActivities = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/api/record/page?pageNum=${activityPageNum.value}&pageSize=${activityPageSize.value}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const result = await response.json()
    if (result.code === 200 && result.data) {
      activityList.value = result.data.records || []
      activityTotal.value = result.data.total || 0
    }
  } catch (error) {
    console.error('加载活动记录失败:', error)
    ElMessage.error('加载活动记录失败')
  }
}

const calculateDuration = (arrival, departure) => {
  if (!departure) return '-'
  
  let start, end
  
  // 处理数组格式
  if (Array.isArray(arrival) && Array.isArray(departure)) {
    const [ay, am, ad, ah, ami] = arrival
    const [dy, dm, dd, dh, dmi] = departure
    start = new Date(ay, am - 1, ad, ah, ami)
    end = new Date(dy, dm - 1, dd, dh, dmi)
  } else {
    // 处理字符串格式
    start = new Date(arrival)
    end = new Date(departure)
  }
  
  const diff = (end - start) / 1000 / 60
  return `${Math.floor(diff)} 分钟`
}

// 格式化日期时间为 YYYY-MM-DD HH:mm
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  
  // 处理数组格式 [year, month, day, hour, minute, second, nano]
  if (Array.isArray(dateTime)) {
    const [year, month, day, hour, minute] = dateTime
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')} ${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`
  }
  
  // 处理字符串格式
  const date = new Date(dateTime)
  if (isNaN(date.getTime())) return '-'
  
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 格式化日期为 YYYY-MM-DD
const formatDate = (date) => {
  if (!date) return '-'
  
  // 处理数组格式
  if (Array.isArray(date)) {
    const [year, month, day] = date
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
  }
  
  // 处理字符串格式
  const d = new Date(date)
  if (isNaN(d.getTime())) return '-'
  
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 加载预约记录
const loadBookings = async () => {
  try {
    const url = currentBookingTab.value === 'equipment'
      ? `${API_BASE_URL}/api/book/equipment/all?pageNum=${bookingPageNum.value}&pageSize=${bookingPageSize.value}`
      : `${API_BASE_URL}/api/book/coach/all?pageNum=${bookingPageNum.value}&pageSize=${bookingPageSize.value}`

    const response = await fetch(url, {
      method: 'GET',
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const result = await response.json()
    if (result.code === 200 && result.data) {
      bookingList.value = result.data.records || []
      bookingTotal.value = result.data.total || 0
    }
  } catch (error) {
    console.error('加载预约记录失败:', error)
    ElMessage.error('加载预约记录失败')
  }
}

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  localStorage.removeItem('userId')
  window.location.href = '/login'
}

// 初始化 WebSocket 连接
const initWebSocket = () => {
  try {
    console.log('[WebSocket] 正在连接到: http://localhost:5200/ws')
    const socket = new SockJS('http://localhost:5200/ws')
    stompClient = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      onConnect: () => {
        console.log('[WebSocket] ✅ 连接成功')

        // 订阅人数颜色变化
        stompClient.subscribe('/topic/color', (message) => {
          const data = JSON.parse(message.body)
          console.log('[WebSocket] 📨 收到人数状态推送:', data)

          // 直接根据颜色设置警报状态
          if (data.color === 'red') {
            console.log('[WebSocket] ⚠️ 检测到人数超标！')
            isOverCapacity.value = true
          } else {
            console.log('[WebSocket] ✅ 人数正常')
            isOverCapacity.value = false
          }
        })
      },
      onStompError: (frame) => {
        console.error('[WebSocket] ❌ STOMP 错误:', frame)
      },
      onDisconnect: () => {
        console.warn('[WebSocket] ⚠️ 连接已断开')
      }
    })

    stompClient.activate()
    console.log('[WebSocket] 客户端已激活')
  } catch (error) {
    console.error('[WebSocket] ❌ 初始化失败:', error)
  }
}

onMounted(() => {
  loadDashboardData()
  initWebSocket()
})

onUnmounted(() => {
  if (stompClient) {
    stompClient.deactivate()
    console.log('[WebSocket] 连接已关闭')
  }
})
</script>

<style scoped>
.admin-dashboard {
  height: 100vh;
}

.sidebar {
  background: linear-gradient(180deg, #2c3e50 0%, #34495e 100%);
  color: white;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 30px 20px;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.sidebar-header h2 {
  font-size: 28px;
  margin: 15px 0 5px;
}

.sidebar-header p {
  font-size: 16px;
  opacity: 0.7;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
  background: transparent;
}

.sidebar-menu :deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.8);
}

.sidebar-menu :deep(.el-menu-item:hover),
.sidebar-menu :deep(.el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.15) !important;
  color: white;
}

.menu-icon {
  margin-right: 10px;
  font-size: 24px;
}

.sidebar-footer {
  padding: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.logout-btn {
  width: 100%;
}

.top-bar {
  background: white;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px;
}

.top-bar h1 {
  font-size: 32px;
  color: #2c3e50;
  margin: 0;
}

.admin-info {
  color: #666;
  font-size: 18px;
}

.content-area {
  background: #f5f7fa;
  padding: 30px;
}

.content-section {
  animation: fadeIn 0.3s;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.dashboard-grid {
  margin-bottom: 30px;
}

.stat-card {
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  font-size: 48px;
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 42px;
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 18px;
  color: #999;
}

.alert-card {
  border: 2px solid #e74c3c;
  animation: borderPulse 1s infinite;
}

@keyframes borderPulse {
  0%, 100% { border-color: #e74c3c; }
  50% { border-color: #ff6b6b; }
}

.alert-text {
  color: #e74c3c !important;
  animation: textPulse 1s infinite;
}

@keyframes textPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.alert-badge {
  color: #e74c3c;
  font-weight: bold;
  font-size: 18px;
  margin-top: 8px;
  animation: badgePulse 1s infinite;
}

@keyframes badgePulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.chart-container {
  margin-top: 20px;
}

.chart-container h3 {
  color: #2c3e50;
  margin: 0;
  font-size: 24px;
}

.monitor-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.count-display {
  text-align: center;
  margin-bottom: 30px;
}

.count-number {
  font-size: 80px;
  font-weight: bold;
  color: #2c3e50;
}

.count-label {
  font-size: 20px;
  color: #999;
  margin-top: 10px;
}

.count-display.warning .count-number {
  color: #e74c3c;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.capacity-info {
  text-align: center;
  width: 100%;
}

.capacity-info p {
  font-size: 20px;
  color: #666;
  margin: 10px 0;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-header h3 {
  color: #2c3e50;
  font-size: 24px;
  margin: 0;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-header h3 {
  color: #2c3e50;
  font-size: 24px;
  margin: 0;
}

.video-wrapper {
  position: relative;
}

.video-stream {
  width: 100%;
  border-radius: 8px;
  display: block;
}
</style>
