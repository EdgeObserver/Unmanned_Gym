<template>
  <div class="admin-dashboard">
    <!-- 侧边栏 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <div class="admin-avatar">👨‍💼</div>
        <h2>管理员</h2>
        <p>健身房管理系统</p>
      </div>

      <div class="sidebar-menu">
        <div
          v-for="item in menuItems"
          :key="item.section"
          :class="['menu-item', { active: currentSection === item.section }]"
          @click="switchSection(item.section)"
        >
          <span class="menu-icon">{{ item.icon }}</span>
          <span class="menu-text">{{ item.text }}</span>
        </div>
      </div>

      <div class="sidebar-footer">
        <button class="logout-btn" @click="logout">退出登录</button>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="main-content">
      <div class="top-bar">
        <h1>{{ pageTitle }}</h1>
        <div class="admin-info">
          <span>管理员：{{ adminName }}</span>
        </div>
      </div>

      <div class="content-area">
        <!-- 首页 - 数据大屏 -->
        <div v-show="currentSection === 'dashboard'" class="content-section">
          <div class="dashboard-grid">
            <!-- 统计卡片 -->
            <div class="stat-card">
              <div class="stat-icon">👥</div>
              <div class="stat-info">
                <div class="stat-value">{{ totalMembers }}</div>
                <div class="stat-label">会员总人数</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">🏃</div>
              <div class="stat-info">
                <div class="stat-value">{{ currentCount }}</div>
                <div class="stat-label">当前在馆人数</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">📊</div>
              <div class="stat-info">
                <div class="stat-value">{{ todayVisits }}</div>
                <div class="stat-label">今日访问次数</div>
              </div>
            </div>
          </div>

          <!-- 折线图 -->
          <div class="chart-container">
            <h3>近7天每日进入人数</h3>
            <canvas ref="chartCanvas" width="800" height="400"></canvas>
          </div>
        </div>

        <!-- 人数检测 -->
        <div v-show="currentSection === 'monitor'" class="content-section">
          <div class="monitor-container">
            <div class="video-wrapper">
              <img
                :src="videoStreamUrl"
                alt="实时监控"
                class="video-stream"
                @error="handleVideoError"
              />
              <div v-if="videoError" class="video-error">
                <p>视频流加载失败，请确保CountService已启动</p>
              </div>
            </div>
            <div class="monitor-info">
              <div class="count-display" :class="{ warning: isOverCapacity }">
                <div class="count-number">{{ currentCount }}</div>
                <div class="count-label">当前人数</div>
              </div>
              <div class="capacity-info">
                <p>场馆容量：{{ capacity }} 人</p>
                <p v-if="isOverCapacity" class="warning-text">⚠️ 人数异常！</p>
                <p v-else class="normal-text">✅ 人数正常</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 用户信息管理 -->
        <div v-show="currentSection === 'users'" class="content-section">
          <div class="table-container">
            <div class="table-header">
              <h3>用户列表</h3>
              <div class="search-box">
                <input
                  type="text"
                  v-model="userSearchKeyword"
                  placeholder="搜索用户名..."
                  @input="searchUsers"
                />
              </div>
            </div>
            <table class="data-table">
              <thead>
              <tr>
                <th>ID</th>
                <th>用户名</th>
                <th>邮箱</th>
                <th>会员状态</th>
                <th>会员到期时间</th>
                <th>注册时间</th>
                <th>操作</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="user in userList" :key="user.id">
                <td>{{ user.id }}</td>
                <td>{{ user.username }}</td>
                <td>{{ user.email }}</td>
                <td>
                    <span :class="['status-badge', user.membershipEndTime ? 'active' : 'inactive']">
                      {{ user.membershipEndTime ? '会员' : '非会员' }}
                    </span>
                </td>
                <td>{{ user.membershipEndTime || '-' }}</td>
                <td>{{ user.createdTime }}</td>
                <td>
                  <button class="btn-sm btn-view" @click="viewUser(user)">查看</button>
                  <button class="btn-sm btn-delete" @click="deleteUser(user.id)">删除</button>
                </td>
              </tr>
              </tbody>
            </table>
            <div class="pagination">
              <button @click="prevUserPage" :disabled="userPageNum <= 1">上一页</button>
              <span>第 {{ userPageNum }} / {{ Math.ceil(userTotal / userPageSize) }} 页</span>
              <button @click="nextUserPage" :disabled="userPageNum >= Math.ceil(userTotal / userPageSize)">下一页</button>
            </div>
          </div>
        </div>

        <!-- 资源管理 -->
        <div v-show="currentSection === 'resources'" class="content-section">
          <div class="resource-tabs">
            <button
              :class="['resource-tab', { active: currentResourceTab === 'equipment' }]"
              @click="switchResourceTab('equipment')"
            >
              器材管理
            </button>
            <button
              :class="['resource-tab', { active: currentResourceTab === 'coach' }]"
              @click="switchResourceTab('coach')"
            >
              教练管理
            </button>
            <button
              :class="['resource-tab', { active: currentResourceTab === 'package' }]"
              @click="switchResourceTab('package')"
            >
              套餐管理
            </button>
          </div>

          <!-- 器材管理 -->
          <div v-show="currentResourceTab === 'equipment'" class="resource-panel">
            <div class="panel-header">
              <h3>器材列表</h3>
              <button class="btn-add" @click="showAddEquipmentModal">+ 添加器材</button>
            </div>
            <table class="data-table">
              <thead>
              <tr>
                <th>ID</th>
                <th>名称</th>
                <th>数量</th>
                <th>需教练</th>
                <th>操作</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="eq in equipmentList" :key="eq.id">
                <td>{{ eq.id }}</td>
                <td>{{ eq.name }}</td>
                <td>{{ eq.num }}</td>
                <td>{{ eq.needCoach === 1 ? '是' : '否' }}</td>
                <td>
                  <button class="btn-sm btn-edit" @click="editEquipment(eq)">编辑</button>
                  <button class="btn-sm btn-delete" @click="deleteEquipment(eq.id)">删除</button>
                </td>
              </tr>
              </tbody>
            </table>
            <div class="pagination">
              <button @click="prevEquipmentPage" :disabled="equipmentPageNum <= 1">上一页</button>
              <span>第 {{ equipmentPageNum }} / {{ Math.ceil(equipmentTotal / equipmentPageSize) }} 页</span>
              <button @click="nextEquipmentPage" :disabled="equipmentPageNum >= Math.ceil(equipmentTotal / equipmentPageSize)">下一页</button>
            </div>
          </div>

          <!-- 教练管理 -->
          <div v-show="currentResourceTab === 'coach'" class="resource-panel">
            <div class="panel-header">
              <h3>教练列表</h3>
              <button class="btn-add" @click="showAddCoachModal">+ 添加教练</button>
            </div>
            <table class="data-table">
              <thead>
              <tr>
                <th>ID</th>
                <th>姓名</th>
                <th>专长</th>
                <th>简介</th>
                <th>操作</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="coach in coachList" :key="coach.id">
                <td>{{ coach.id }}</td>
                <td>{{ coach.name }}</td>
                <td>{{ coach.specialty || '-' }}</td>
                <td>{{ coach.description || '-' }}</td>
                <td>
                  <button class="btn-sm btn-edit" @click="editCoach(coach)">编辑</button>
                  <button class="btn-sm btn-delete" @click="deleteCoach(coach.id)">删除</button>
                </td>
              </tr>
              </tbody>
            </table>
            <div class="pagination">
              <button @click="prevCoachPage" :disabled="coachPageNum <= 1">上一页</button>
              <span>第 {{ coachPageNum }} / {{ Math.ceil(coachTotal / coachPageSize) }} 页</span>
              <button @click="nextCoachPage" :disabled="coachPageNum >= Math.ceil(coachTotal / coachPageSize)">下一页</button>
            </div>
          </div>

          <!-- 套餐管理 -->
          <div v-show="currentResourceTab === 'package'" class="resource-panel">
            <div class="panel-header">
              <h3>套餐列表</h3>
              <button class="btn-add" @click="showAddPackageModal">+ 添加套餐</button>
            </div>
            <table class="data-table">
              <thead>
              <tr>
                <th>ID</th>
                <th>名称</th>
                <th>时长(天)</th>
                <th>等级</th>
                <th>价格</th>
                <th>操作</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="pkg in packageList" :key="pkg.id">
                <td>{{ pkg.id }}</td>
                <td>{{ pkg.name }}</td>
                <td>{{ pkg.duration }}</td>
                <td>Lv.{{ pkg.level }}</td>
                <td>¥{{ pkg.price }}</td>
                <td>
                  <button class="btn-sm btn-edit" @click="editPackage(pkg)">编辑</button>
                  <button class="btn-sm btn-delete" @click="deletePackage(pkg.id)">删除</button>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 订单信息 -->
        <div v-show="currentSection === 'orders'" class="content-section">
          <div class="table-container">
            <div class="table-header">
              <h3>订单列表</h3>
            </div>
            <table class="data-table">
              <thead>
              <tr>
                <th>订单ID</th>
                <th>用户ID</th>
                <th>套餐ID</th>
                <th>开始时间</th>
                <th>结束时间</th>
                <th>状态</th>
                <th>创建时间</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="order in orderList" :key="order.id">
                <td>{{ order.id }}</td>
                <td>{{ order.uid }}</td>
                <td>{{ order.pid }}</td>
                <td>{{ order.orderStartTime }}</td>
                <td>{{ order.orderEndTime }}</td>
                <td>
                    <span :class="['status-badge', order.status === 1 ? 'active' : 'expired']">
                      {{ order.status === 1 ? '有效' : '已过期' }}
                    </span>
                </td>
                <td>{{ order.createdTime }}</td>
              </tr>
              </tbody>
            </table>
            <div class="pagination">
              <button @click="prevOrderPage" :disabled="orderPageNum <= 1">上一页</button>
              <span>第 {{ orderPageNum }} / {{ Math.ceil(orderTotal / orderPageSize) }} 页</span>
              <button @click="nextOrderPage" :disabled="orderPageNum >= Math.ceil(orderTotal / orderPageSize)">下一页</button>
            </div>
          </div>
        </div>

        <!-- 活动记录 -->
        <div v-show="currentSection === 'activities'" class="content-section">
          <div class="table-container">
            <div class="table-header">
              <h3>活动记录</h3>
            </div>
            <table class="data-table">
              <thead>
              <tr>
                <th>记录ID</th>
                <th>用户ID</th>
                <th>到达时间</th>
                <th>离开时间</th>
                <th>停留时长</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="record in activityList" :key="record.id">
                <td>{{ record.id }}</td>
                <td>{{ record.userId }}</td>
                <td>{{ record.arrivalTime }}</td>
                <td>{{ record.leaveTime || '-' }}</td>
                <td>{{ calculateDuration(record.arrivalTime, record.leaveTime) }}</td>
              </tr>
              </tbody>
            </table>
            <div class="pagination">
              <button @click="prevActivityPage" :disabled="activityPageNum <= 1">上一页</button>
              <span>第 {{ activityPageNum }} / {{ Math.ceil(activityTotal / activityPageSize) }} 页</span>
              <button @click="nextActivityPage" :disabled="activityPageNum >= Math.ceil(activityTotal / activityPageSize)">下一页</button>
            </div>
          </div>
        </div>

        <!-- 预约记录 -->
        <div v-show="currentSection === 'bookings'" class="content-section">
          <div class="booking-tabs">
            <button
              :class="['booking-tab', { active: currentBookingTab === 'equipment' }]"
              @click="switchAdminBookingTab('equipment')"
            >
              器材预约
            </button>
            <button
              :class="['booking-tab', { active: currentBookingTab === 'coach' }]"
              @click="switchAdminBookingTab('coach')"
            >
              教练预约
            </button>
          </div>
          <div class="table-container">
            <table class="data-table">
              <thead>
              <tr>
                <th>预约ID</th>
                <th>用户ID</th>
                <th v-if="currentBookingTab === 'equipment'">器材ID</th>
                <th v-else>教练ID</th>
                <th>日期</th>
                <th>时间段ID</th>
                <th>状态</th>
                <th>创建时间</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="booking in bookingList" :key="booking.id">
                <td>{{ booking.id }}</td>
                <td>{{ booking.userId }}</td>
                <td>{{ booking.equipmentId || booking.coachId }}</td>
                <td>{{ booking.bookingDate }}</td>
                <td>{{ booking.slotId }}</td>
                <td>
                    <span :class="['status-badge', booking.status === 1 ? 'active' : 'cancelled']">
                      {{ booking.status === 1 ? '有效' : '已取消' }}
                    </span>
                </td>
                <td>{{ booking.createdTime }}</td>
              </tr>
              </tbody>
            </table>
            <div class="pagination">
              <button @click="prevBookingPage" :disabled="bookingPageNum <= 1">上一页</button>
              <span>第 {{ bookingPageNum }} / {{ Math.ceil(bookingTotal / bookingPageSize) }} 页</span>
              <button @click="nextBookingPage" :disabled="bookingPageNum >= Math.ceil(bookingTotal / bookingPageSize)">下一页</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 添加/编辑器材弹窗 -->
    <div v-show="showEquipmentModal" class="modal-overlay active" @click.self="closeEquipmentModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ equipmentModalTitle }}</h3>
          <button class="modal-close" @click="closeEquipmentModal">&times;</button>
        </div>
        <div class="form-group">
          <label>名称：</label>
          <input type="text" v-model="equipmentForm.name" class="form-input" />
        </div>
        <div class="form-group">
          <label>数量：</label>
          <input type="number" v-model="equipmentForm.num" class="form-input" />
        </div>
        <div class="form-group">
          <label>需要教练：</label>
          <select v-model="equipmentForm.needCoach" class="form-input">
            <option :value="0">否</option>
            <option :value="1">是</option>
          </select>
        </div>
        <div class="form-actions">
          <button class="cancel-btn" @click="closeEquipmentModal">取消</button>
          <button class="submit-btn" @click="submitEquipmentForm">保存</button>
        </div>
      </div>
    </div>

    <!-- 添加/编辑教练弹窗 -->
    <div v-show="showCoachModal" class="modal-overlay active" @click.self="closeCoachModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ coachModalTitle }}</h3>
          <button class="modal-close" @click="closeCoachModal">&times;</button>
        </div>
        <div class="form-group">
          <label>姓名：</label>
          <input type="text" v-model="coachForm.name" class="form-input" />
        </div>
        <div class="form-group">
          <label>专长：</label>
          <input type="text" v-model="coachForm.specialty" class="form-input" />
        </div>
        <div class="form-group">
          <label>简介：</label>
          <textarea v-model="coachForm.description" class="form-input" rows="4"></textarea>
        </div>
        <div class="form-actions">
          <button class="cancel-btn" @click="closeCoachModal">取消</button>
          <button class="submit-btn" @click="submitCoachForm">保存</button>
        </div>
      </div>
    </div>

    <!-- 添加/编辑套餐弹窗 -->
    <div v-show="showPackageModal" class="modal-overlay active" @click.self="closePackageModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ packageModalTitle }}</h3>
          <button class="modal-close" @click="closePackageModal">&times;</button>
        </div>
        <div class="form-group">
          <label>名称：</label>
          <input type="text" v-model="packageForm.name" class="form-input" />
        </div>
        <div class="form-group">
          <label>时长（天）：</label>
          <input type="number" v-model="packageForm.duration" class="form-input" />
        </div>
        <div class="form-group">
          <label>等级：</label>
          <input type="number" v-model="packageForm.level" class="form-input" />
        </div>
        <div class="form-group">
          <label>价格：</label>
          <input type="number" v-model="packageForm.price" class="form-input" />
        </div>
        <div class="form-actions">
          <button class="cancel-btn" @click="closePackageModal">取消</button>
          <button class="submit-btn" @click="submitPackageForm">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, onUnmounted } from 'vue'
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
const isOverCapacity = ref(false)  // 直接使用 WebSocket 推送的状态

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
      // 使用模拟数据作为后备
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
    // 使用模拟数据作为后备
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
  }
}

const searchUsers = () => {
  // TODO: 实现搜索功能
}

const prevUserPage = () => {
  if (userPageNum.value > 1) {
    userPageNum.value--
    loadUsers()
  }
}

const nextUserPage = () => {
  const totalPages = Math.ceil(userTotal.value / userPageSize.value)
  if (userPageNum.value < totalPages) {
    userPageNum.value++
    loadUsers()
  }
}

const viewUser = (user) => {
  alert(`查看用户详情：${user.username}`)
}

const deleteUser = async (userId) => {
  if (!confirm('确认删除该用户吗？')) return

  try {
    const response = await fetch(`${API_BASE_URL}/api/user/delete?id=${userId}`, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const result = await response.json()
    if (result.code === 200) {
      alert('删除成功')
      await loadUsers()
    } else {
      alert(result.msg || '删除失败')
    }
  } catch (error) {
    console.error('删除用户失败:', error)
    alert('网络错误')
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

const switchResourceTab = (tab) => {
  currentResourceTab.value = tab
  loadResources()
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
      alert('操作成功')
      closeEquipmentModal()
      await loadEquipment()
    } else {
      alert(result.msg || '操作失败')
    }
  } catch (error) {
    console.error('提交器材表单失败:', error)
    alert('网络错误')
  }
}

const deleteEquipment = async (id) => {
  if (!confirm('确认删除该器材吗？')) return

  try {
    const response = await fetch(`${API_BASE_URL}/api/resource/equipment/delete?id=${id}`, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const result = await response.json()
    if (result.code === 200) {
      alert('删除成功')
      await loadEquipment()
    } else {
      alert(result.msg || '删除失败')
    }
  } catch (error) {
    console.error('删除器材失败:', error)
  }
}

const prevEquipmentPage = () => {
  if (equipmentPageNum.value > 1) {
    equipmentPageNum.value--
    loadEquipment()
  }
}

const nextEquipmentPage = () => {
  const totalPages = Math.ceil(equipmentTotal.value / equipmentPageSize.value)
  if (equipmentPageNum.value < totalPages) {
    equipmentPageNum.value++
    loadEquipment()
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
      alert('操作成功')
      closeCoachModal()
      await loadCoaches()
    } else {
      alert(result.msg || '操作失败')
    }
  } catch (error) {
    console.error('提交教练表单失败:', error)
    alert('网络错误')
  }
}

const deleteCoach = async (id) => {
  if (!confirm('确认删除该教练吗？')) return

  try {
    const response = await fetch(`${API_BASE_URL}/api/resource/coach/delete?id=${id}`, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const result = await response.json()
    if (result.code === 200) {
      alert('删除成功')
      await loadCoaches()
    } else {
      alert(result.msg || '删除失败')
    }
  } catch (error) {
    console.error('删除教练失败:', error)
  }
}

const prevCoachPage = () => {
  if (coachPageNum.value > 1) {
    coachPageNum.value--
    loadCoaches()
  }
}

const nextCoachPage = () => {
  const totalPages = Math.ceil(coachTotal.value / coachPageSize.value)
  if (coachPageNum.value < totalPages) {
    coachPageNum.value++
    loadCoaches()
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
  alert('套餐管理接口待实现')
}

const deletePackage = async (id) => {
  if (!confirm('确认删除该套餐吗？')) return
  alert('套餐删除接口待实现')
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
  }
}

const prevOrderPage = () => {
  if (orderPageNum.value > 1) {
    orderPageNum.value--
    loadOrders()
  }
}

const nextOrderPage = () => {
  const totalPages = Math.ceil(orderTotal.value / orderPageSize.value)
  if (orderPageNum.value < totalPages) {
    orderPageNum.value++
    loadOrders()
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
  }
}

const calculateDuration = (arrival, leave) => {
  if (!leave) return '-'
  const start = new Date(arrival)
  const end = new Date(leave)
  const diff = (end - start) / 1000 / 60
  return `${Math.floor(diff)} 分钟`
}

const prevActivityPage = () => {
  if (activityPageNum.value > 1) {
    activityPageNum.value--
    loadActivities()
  }
}

const nextActivityPage = () => {
  const totalPages = Math.ceil(activityTotal.value / activityPageSize.value)
  if (activityPageNum.value < totalPages) {
    activityPageNum.value++
    loadActivities()
  }
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
  }
}

const switchAdminBookingTab = (tab) => {
  currentBookingTab.value = tab
  loadBookings()
}

const prevBookingPage = () => {
  if (bookingPageNum.value > 1) {
    bookingPageNum.value--
    loadBookings()
  }
}

const nextBookingPage = () => {
  const totalPages = Math.ceil(bookingTotal.value / bookingPageSize.value)
  if (bookingPageNum.value < totalPages) {
    bookingPageNum.value++
    loadBookings()
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
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.admin-dashboard {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  display: flex;
  overflow: hidden;
  background: #f5f7fa;
}

.sidebar {
  width: 260px;
  background: linear-gradient(180deg, #2c3e50 0%, #34495e 100%);
  color: white;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
}

.sidebar-header {
  padding: 30px 20px;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.sidebar-header h2 {
  font-size: 22px;
  margin-bottom: 5px;
}

.sidebar-header p {
  font-size: 12px;
  opacity: 0.7;
}

.admin-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: white;
  margin: 0 auto 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
}

.sidebar-menu {
  flex: 1;
  padding: 20px 0;
  overflow-y: auto;
}

.menu-item {
  padding: 15px 25px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 12px;
  border-left: 4px solid transparent;
}

.menu-item:hover {
  background: rgba(255, 255, 255, 0.1);
  border-left-color: rgba(255, 255, 255, 0.5);
}

.menu-item.active {
  background: rgba(255, 255, 255, 0.15);
  border-left-color: #3498db;
}

.menu-icon {
  font-size: 20px;
  width: 24px;
  text-align: center;
}

.menu-text {
  font-size: 15px;
}

.sidebar-footer {
  padding: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.logout-btn {
  width: 100%;
  padding: 12px;
  background: rgba(231, 76, 60, 0.8);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.3s;
  font-size: 14px;
}

.logout-btn:hover {
  background: rgba(231, 76, 60, 1);
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.top-bar {
  background: white;
  padding: 20px 30px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.top-bar h1 {
  font-size: 24px;
  color: #2c3e50;
}

.admin-info {
  color: #666;
  font-size: 14px;
}

.content-area {
  flex: 1;
  padding: 30px;
  overflow-y: auto;
}

.content-section {
  animation: fadeIn 0.3s;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 数据大屏 */
.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 30px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
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
  font-size: 36px;
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #999;
}

/* 警报样式 */
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
  font-size: 14px;
  margin-top: 8px;
  animation: badgePulse 1s infinite;
}

@keyframes badgePulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.chart-container {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.chart-container h3 {
  color: #2c3e50;
  margin-bottom: 20px;
  font-size: 20px;
}

/* 人数检测 */
.monitor-container {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}

.video-wrapper {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  position: relative;
}

.video-stream {
  width: 100%;
  border-radius: 8px;
  display: block;
}

.video-error {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
}

.monitor-info {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
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
  font-size: 72px;
  font-weight: bold;
  color: #2c3e50;
}

.count-label {
  font-size: 16px;
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
}

.capacity-info p {
  font-size: 16px;
  color: #666;
  margin: 10px 0;
}

.warning-text {
  color: #e74c3c !important;
  font-weight: bold;
  font-size: 18px !important;
}

.normal-text {
  color: #27ae60 !important;
  font-weight: bold;
  font-size: 18px !important;
}

/* 表格样式 */
.table-container {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.table-header h3 {
  color: #2c3e50;
  font-size: 20px;
}

.search-box input {
  padding: 10px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  width: 250px;
}

.search-box input:focus {
  outline: none;
  border-color: #667eea;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 20px;
}

.data-table thead {
  background: #f8f9fa;
}

.data-table th {
  padding: 15px;
  text-align: left;
  font-weight: 600;
  color: #2c3e50;
  border-bottom: 2px solid #e0e0e0;
}

.data-table td {
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
  color: #666;
}

.data-table tbody tr:hover {
  background: #f8f9ff;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.status-badge.active {
  background: #d4edda;
  color: #155724;
}

.status-badge.inactive {
  background: #f8d7da;
  color: #721c24;
}

.status-badge.expired {
  background: #fff3cd;
  color: #856404;
}

.status-badge.cancelled {
  background: #f8d7da;
  color: #721c24;
}

.btn-sm {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  margin-right: 5px;
  transition: all 0.3s;
}

.btn-view {
  background: #3498db;
  color: white;
}

.btn-view:hover {
  background: #2980b9;
}

.btn-edit {
  background: #f39c12;
  color: white;
}

.btn-edit:hover {
  background: #e67e22;
}

.btn-delete {
  background: #e74c3c;
  color: white;
}

.btn-delete:hover {
  background: #c0392b;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-top: 20px;
}

.pagination button {
  padding: 8px 16px;
  border: 2px solid #667eea;
  background: white;
  color: #667eea;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 600;
}

.pagination button:hover:not(:disabled) {
  background: #667eea;
  color: white;
}

.pagination button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.pagination span {
  color: #666;
  font-size: 14px;
}

/* 资源管理 */
.resource-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.resource-tab {
  padding: 12px 24px;
  background: white;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;
  color: #666;
  transition: all 0.3s;
}

.resource-tab.active {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.resource-tab:hover:not(.active) {
  border-color: #667eea;
  color: #667eea;
}

.resource-panel {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.panel-header h3 {
  color: #2c3e50;
  font-size: 20px;
}

.btn-add {
  padding: 10px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s;
}

.btn-add:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

/* 预约标签 */
.booking-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.booking-tab {
  padding: 12px 24px;
  background: white;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;
  color: #666;
  transition: all 0.3s;
}

.booking-tab.active {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

/* 弹窗 */
.modal-overlay {
  display: none;
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  justify-content: center;
  align-items: center;
}

.modal-overlay.active {
  display: flex;
}

.modal-content {
  background: white;
  border-radius: 16px;
  padding: 30px;
  max-width: 600px;
  width: 90%;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f0f0f0;
}

.modal-header h3 {
  color: #667eea;
  font-size: 22px;
  margin: 0;
}

.modal-close {
  background: none;
  border: none;
  font-size: 28px;
  color: #999;
  cursor: pointer;
  transition: color 0.3s;
}

.modal-close:hover {
  color: #333;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #666;
  font-weight: 600;
  font-size: 14px;
}

.form-input {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

textarea.form-input {
  resize: vertical;
  font-family: inherit;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 30px;
}

.cancel-btn {
  flex: 1;
  padding: 12px 24px;
  background: #f0f0f0;
  color: #666;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s;
}

.cancel-btn:hover {
  background: #e0e0e0;
}

.submit-btn {
  flex: 1;
  padding: 12px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}
</style>
