<template>
  <div class="dashboard-container">
    <!-- 侧边栏 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <div class="user-avatar">👤</div>
        <h2>{{ userName }}</h2>
        <p>欢迎回来</p>
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
      </div>

      <div class="content-area">
        <!-- 个人信息 -->
        <div v-show="currentSection === 'profile'" class="content-section">
          <div class="content-card">
            <div class="profile-header">
              <h3>基本信息</h3>
              <button class="edit-btn" @click="showEditModal">✏️ 修改信息</button>
            </div>
            <div class="profile-info">
              <div class="info-item">
                <div class="info-label">用户名</div>
                <div class="info-value">{{ userProfile.username || '-' }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">邮箱</div>
                <div class="info-value">{{ userProfile.email || '-' }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">用户ID</div>
                <div class="info-value">{{ userProfile.id || '-' }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">注册时间</div>
                <div class="info-value">{{ userProfile.createdTime || '-' }}</div>
              </div>
            </div>
          </div>
        </div>


        // ... existing code ...

        <!-- 预约器材 -->
        <div v-show="currentSection === 'equipment-booking'" class="content-section">
          <div class="content-card">
            <h3>可预约器材</h3>
            <div class="booking-grid">
              <div
                v-for="eq in equipmentList"
                :key="eq.id"
                :class="['booking-card', Number(eq.needCoach) === 1 ? 'coach-required' : 'self-service']"
              >
                <div v-if="Number(eq.needCoach) === 1" class="coach-required-badge">👨‍🏫 需教练</div>
                <h4>{{ eq.name }}</h4>
                <p class="equipment-quantity">📦 总数量：{{ eq.num }} 个</p>
                <p :class="['coach-status', Number(eq.needCoach) === 1 ? 'required' : 'optional']">
                  {{ Number(eq.needCoach) === 1 ? '⚠️ 需要教练指导' : '✅ 可独立使用' }}
                </p>
                <button class="book-btn" @click="openBookingModal('equipment', eq.id, eq.name)">立即预约</button>
              </div>
            </div>
          </div>
        </div>

        // ... existing code ...


        <!-- 预约教练 -->
        <div v-show="currentSection === 'coach-booking'" class="content-section">
          <div class="content-card">
            <h3>可预约教练</h3>
            <div class="booking-grid">
              <div
                  v-for="coach in coachList"
                  :key="coach.id"
                  class="booking-card"
              >
                <h4>{{ coach.name }}</h4>
                <button class="book-btn" @click="openBookingModal('coach', coach.id, coach.name)">立即预约</button>
              </div>
            </div>
          </div>
        </div>

        <!-- 我的预约 -->
        <div v-show="currentSection === 'my-bookings'" class="content-section">
          <div class="content-card">
            <h3>我的预约</h3>
            <div class="booking-tabs">
              <button
                  :class="['booking-tab', { active: currentBookingTab === 'equipment' }]"
                  @click="switchBookingTab('equipment')"
              >
                器材预约
              </button>
              <button
                  :class="['booking-tab', { active: currentBookingTab === 'coach' }]"
                  @click="switchBookingTab('coach')"
              >
                教练预约
              </button>
            </div>
            <div class="my-bookings-list">
              <div v-if="myBookings.length === 0" class="empty-bookings">
                <div class="empty-icon">📭</div>
                <p>暂无{{ currentBookingTab === 'equipment' ? '器材' : '教练' }}预约记录</p>
              </div>
              <div
                  v-for="booking in myBookings"
                  :key="booking.id"
                  class="booking-item"
              >
                <div class="booking-item-info">
                  <h4>{{ currentBookingTab === 'equipment' ? '器材' : '教练' }}：{{ booking.resourceName }}</h4>
                  <p class="booking-date">📅 日期：{{ booking.bookingDate }}</p>
                  <p>⏰ 时间段ID：{{ booking.slotId }}</p>
                  <span :class="['booking-status', booking.status === 1 ? 'active' : 'cancelled']">
                    {{ booking.status === 1 ? '有效' : '已取消' }}
                  </span>
                </div>
                <button
                    v-if="canCancelBooking(booking)"
                    class="cancel-booking-btn"
                    @click="cancelBooking(currentBookingTab, booking.id)"
                >
                  取消预约
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 订单与续约 -->
        <div v-show="currentSection === 'orders'" class="content-section">
          <div class="content-card">
            <h3>我的订单</h3>
            <div class="order-actions">
              <button class="action-btn primary" @click="handleJoinMembership" v-if="!currentOrder">加入会员</button>
              <button class="action-btn secondary" @click="showRenewPackageModal" v-if="currentOrder">续约</button>
              <button class="action-btn info" @click="loadOrderHistory">查看订单历史</button>
            </div>
            <div class="order-list" v-if="!showOrderHistory">
              <div v-if="currentOrder" class="order-item">
                <div class="order-info">
                  <h4>当前有效订单 #{{ currentOrder.id }}</h4>
                  <p>开始时间：{{ currentOrder.orderStartTime }}</p>
                  <p>结束时间：{{ currentOrder.orderEndTime }}</p>
                  <p>状态：{{ currentOrder.status === 1 ? '有效' : '已过期' }}</p>
                </div>
              </div>
              <div v-else>
                <p>暂无有效订单</p>
              </div>
            </div>

            <!-- 订单历史记录 -->
            <div class="order-history" v-if="showOrderHistory">
              <h4>订单历史记录</h4>
              <div class="order-history-list">
                <div v-if="orderHistory.length === 0" class="empty-history">
                  <p>暂无订单历史记录</p>
                </div>
                <div
                    v-for="order in orderHistory"
                    :key="order.id"
                    class="order-history-item"
                >
                  <div class="order-info">
                    <h5>订单 #{{ order.id }}</h5>
                    <p>套餐ID：{{ order.pid }}</p>
                    <p>开始时间：{{ order.orderStartTime }}</p>
                    <p>结束时间：{{ order.orderEndTime }}</p>
                    <p>创建时间：{{ order.createdTime }}</p>
                    <span :class="['order-status', order.status === 1 ? 'active' : 'expired']">
                      {{ order.status === 1 ? '有效' : '已过期' }}
                    </span>
                  </div>
                </div>
              </div>
              <div class="pagination" v-if="orderHistoryTotal > 0">
                <button @click="prevPage" :disabled="orderHistoryPageNum <= 1">上一页</button>
                <span>第 {{ orderHistoryPageNum }} / {{ Math.ceil(orderHistoryTotal / orderHistoryPageSize) }} 页</span>
                <button @click="nextPage" :disabled="orderHistoryPageNum >= Math.ceil(orderHistoryTotal / orderHistoryPageSize)">下一页</button>
              </div>
              <button class="back-btn" @click="showOrderHistory = false">返回</button>
            </div>
          </div>
        </div>

        <!-- AI助手 -->
        <div v-show="currentSection === 'ai-assistant'" class="content-section">
          <div class="content-card">
            <h3>AI健身助手</h3>
            <div class="chat-container">
              <div class="chat-messages" ref="chatMessagesRef">
                <div
                    v-for="(msg, index) in chatMessages"
                    :key="index"
                    :class="['message', msg.type]"
                >
                  <div class="message-avatar">{{ msg.type === 'user' ? '👤' : '🤖' }}</div>
                  <div class="message-content">{{ msg.content }}</div>
                </div>
              </div>
              <div class="chat-input-area">
                <input
                    type="text"
                    class="chat-input"
                    v-model="chatInput"
                    @keypress="handleChatKeypress"
                    placeholder="输入您的问题..."
                />
                <button class="send-btn" @click="sendMessage" :disabled="sendingMessage">发送</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 套餐选择弹窗 -->
    <div
        v-show="showPackageModal"
        class="modal-overlay active"
        @click.self="closePackageModal"
    >
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ modalTitle }}</h3>
          <button class="modal-close" @click="closePackageModal">&times;</button>
        </div>
        <div class="package-list">
          <div
              v-for="pkg in packageList"
              :key="pkg.id"
              class="package-item"
              @click="selectPackage(pkg.id, packageActionType)"
          >
            <div class="package-info">
              <h4>{{ pkg.name || '套餐' + pkg.level }}</h4>
              <p>时长：{{ pkg.duration }} 天</p>
              <p>等级：Lv.{{ pkg.level }}</p>
            </div>
            <div class="package-price">
              <div class="price">¥{{ pkg.price }}</div>
              <div class="unit">点击选择</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 预约弹窗 -->
    <div
        v-show="showBookingModal"
        class="modal-overlay active"
        @click.self="closeBookingModal"
    >
      <div class="modal-content booking-modal-content">
        <div class="modal-header">
          <h3>{{ bookingModalTitle }}</h3>
          <button class="modal-close" @click="closeBookingModal">&times;</button>
        </div>
        <div class="date-selector">
          <label>选择日期：</label>
          <input type="date" class="date-input" v-model="bookingDate" @change="loadAvailableSlots">
        </div>
        <div>
          <label style="display: block; margin-bottom: 8px; color: #666; font-weight: 600;">选择时间段：</label>
          <div class="slot-grid">
            <div
                v-for="slot in timeSlots"
                :key="slot.id"
                :class="getSlotClass(slot)"
                @click="selectSlot(slot.id)"
            >
              <div class="slot-time">{{ slot.startTime.substring(0, 5) }} - {{ slot.endTime.substring(0, 5) }}</div>
              <div class="slot-status">{{ getSlotStatus(slot) }}</div>
            </div>
          </div>
        </div>
        <button
            class="confirm-booking-btn"
            @click="confirmBooking"
            :disabled="!selectedSlotId"
        >
          确认预约
        </button>
      </div>
    </div>

    <!-- 修改个人信息弹窗 -->
    <div
        v-show="showEditProfileModal"
        class="modal-overlay active"
        @click.self="closeEditModal"
    >
      <div class="modal-content edit-profile-modal">
        <div class="modal-header">
          <h3>修改个人信息</h3>
          <button class="modal-close" @click="closeEditModal">&times;</button>
        </div>
        <div class="edit-form">
          <div class="form-group">
            <label>用户名：</label>
            <input
              type="text"
              class="form-input"
              v-model="editForm.username"
              placeholder="请输入用户名"
            >
          </div>
          <div class="form-group">
            <label>邮箱：</label>
            <input
              type="email"
              class="form-input"
              v-model="editForm.email"
              placeholder="请输入邮箱"
            >
          </div>
          <div class="form-group">
            <label>新密码（留空则不修改）：</label>
            <input
              type="password"
              class="form-input"
              v-model="editForm.password"
              placeholder="输入新密码"
            >
          </div>
          <div class="form-group">
            <label>确认密码：</label>
            <input
              type="password"
              class="form-input"
              v-model="editForm.confirmPassword"
              placeholder="再次输入新密码"
            >
          </div>
          <div class="form-actions">
            <button class="cancel-btn" @click="closeEditModal">取消</button>
            <button class="submit-btn" @click="submitEdit" :disabled="submitting">保存</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 订单页面底部操作按钮 -->
    <div v-show="currentSection === 'orders'" class="order-actions-bar">
      <button class="action-btn renew-main-btn" @click="showRenewPackageModal">续约</button>
      <button class="action-btn join-btn" @click="handleJoinMembership">加入会员</button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'

const API_BASE_URL = 'http://localhost:80'
const PYTHON_AI_URL = `${API_BASE_URL}/api/assistant/talkStream`

const token = localStorage.getItem('token')
const role = localStorage.getItem('role')
const userId = localStorage.getItem('userId')

if (!token || role !== 'user') {
  window.location.href = 'login'
}

const currentSection = ref('profile')
const userName = ref('用户')
const pageTitle = ref('个人信息')
const userProfile = reactive({})

const menuItems = [
  { section: 'profile', icon: '👤', text: '个人信息' },
  { section: 'equipment-booking', icon: '🏋️', text: '预约器材' },
  { section: 'coach-booking', icon: '🏃', text: '预约教练' },
  { section: 'my-bookings', icon: '📅', text: '我的预约' },
  { section: 'orders', icon: '📋', text: '订单与续约' },
  { section: 'ai-assistant', icon: '🤖', text: 'AI助手' }
]

const equipmentList = ref([])
const coachList = ref([])
const currentBookingTab = ref('equipment')
const myBookings = ref([])
const currentOrder = ref(null)
const showOrderHistory = ref(false)
const orderHistory = ref([])
const orderHistoryPageNum = ref(1)
const orderHistoryPageSize = ref(10)
const orderHistoryTotal = ref(0)

const showPackageModal = ref(false)
const modalTitle = ref('选择套餐')
const packageList = ref([])
const packageActionType = ref('join')

const showBookingModal = ref(false)
const bookingModalTitle = ref('预约')
const currentBookingType = ref('')
const currentResourceId = ref(null)
const bookingDate = ref('')
const timeSlots = ref([])
const selectedSlotId = ref(null)
const equipmentInfo = ref(null)

const chatMessages = ref([
  { type: 'ai', content: '您好！我是您的AI健身助手，有什么可以帮助您的吗？' }
])
const chatInput = ref('')
const sendingMessage = ref(false)
const chatMessagesRef = ref(null)

// 编辑个人信息相关
const showEditProfileModal = ref(false)
const submitting = ref(false)
const editForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const switchSection = async (section) => {
  currentSection.value = section
  const titles = {
    'profile': '个人信息',
    'equipment-booking': '预约器材',
    'coach-booking': '预约教练',
    'my-bookings': '我的预约',
    'orders': '订单与续约',
    'ai-assistant': 'AI助手'
  }
  pageTitle.value = titles[section]

  if (section === 'profile') {
    await loadUserProfile()
  } else if (section === 'equipment-booking') {
    await loadEquipmentList()
  } else if (section === 'coach-booking') {
    await loadCoachList()
  } else if (section === 'my-bookings') {
    await loadMyBookings()
  } else if (section === 'orders') {
    await loadOrders()
  }
}

const loadUserProfile = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/api/user/${userId}?id=${userId}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    const result = await response.json()

    if (result.code === 200 && result.data) {
      const user = result.data
      userName.value = user.username || '用户'
      Object.assign(userProfile, user)
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

const loadEquipmentList = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/api/resource/equipment/page?pageNum=1&pageSize=10`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    const result = await response.json()

    if (result.code === 200 && result.data && result.data.records) {
      equipmentList.value = result.data.records
      console.log('器材列表数据:', equipmentList.value)
    }
  } catch (error) {
    console.error('加载器材列表失败:', error)
  }
}

const loadCoachList = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/api/resource/coach/page?pageNum=1&pageSize=10`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    const result = await response.json()

    if (result.code === 200 && result.data && result.data.records) {
      coachList.value = result.data.records
    }
  } catch (error) {
    console.error('加载教练列表失败:', error)
  }
}

const switchBookingTab = (type) => {
  currentBookingTab.value = type
  loadMyBookings()
}

const loadMyBookings = async () => {
  try {
    const url = currentBookingTab.value === 'equipment'
        ? `${API_BASE_URL}/api/book/equipment/my`
        : `${API_BASE_URL}/api/book/coach/my`

    const response = await fetch(url, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    const result = await response.json()

    if (result.code === 200 && result.data && result.data.length > 0) {
      const bookings = result.data

      const enrichedBookings = await Promise.all(bookings.map(async (booking) => {
        let resourceName = '未知'
        if (currentBookingTab.value === 'equipment') {
          const equipResponse = await fetch(`${API_BASE_URL}/api/resource/equipment/get?id=${booking.equipmentId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
          })
          const equipResult = await equipResponse.json()
          if (equipResult.code === 200 && equipResult.data) {
            resourceName = equipResult.data.name
          }
        } else {
          const coachResponse = await fetch(`${API_BASE_URL}/api/resource/coach/get?id=${booking.coachId}`, {
            headers: { 'Authorization': `Bearer ${token}` }
          })
          const coachResult = await coachResponse.json()
          if (coachResult.code === 200 && coachResult.data) {
            resourceName = coachResult.data.name
          }
        }
        return { ...booking, resourceName }
      }))

      myBookings.value = enrichedBookings
    } else {
      myBookings.value = []
    }
  } catch (error) {
    console.error('加载预约记录失败:', error)
    myBookings.value = []
  }
}

const canCancelBooking = (booking) => {
  return booking.status === 1 && new Date(booking.bookingDate) >= new Date().setHours(0, 0, 0, 0)
}

const cancelBooking = async (type, bookingId) => {
  if (!confirm('确认取消该预约吗？')) return

  try {
    const url = type === 'equipment'
        ? `${API_BASE_URL}/api/book/equipment/${bookingId}`
        : `${API_BASE_URL}/api/book/coach/${bookingId}`

    const response = await fetch(url, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    const result = await response.json()

    if (result.code === 200) {
      alert('取消预约成功！')
      await loadMyBookings()
    } else {
      alert(result.msg || '取消预约失败')
    }
  } catch (error) {
    console.error('取消预约失败:', error)
    alert('网络错误，请稍后重试')
  }
}

const loadOrders = async () => {
  try {
    const timestamp = new Date().getTime()
    const response = await fetch(`${API_BASE_URL}/api/order/getLegalOrderbyUid?uid=${userId}&_t=${timestamp}`, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Cache-Control': 'no-cache'
      }
    })
    const result = await response.json()

    if (result.code === 200 && result.data) {
      currentOrder.value = result.data
    } else {
      currentOrder.value = null
    }
  } catch (error) {
    console.error('加载订单失败:', error)
    currentOrder.value = null
  }
}

const loadOrderHistory = async () => {
  showOrderHistory.value = true
  await fetchOrderHistory()
}

const fetchOrderHistory = async () => {
  try {
    const response = await fetch(
      `${API_BASE_URL}/api/order/history?uid=${userId}&pageNum=${orderHistoryPageNum.value}&pageSize=${orderHistoryPageSize.value}`,
      {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      }
    )
    const result = await response.json()

    if (result.code === 200 && result.data) {
      orderHistory.value = result.data.records || []
      orderHistoryTotal.value = result.data.total || 0
    } else {
      orderHistory.value = []
      orderHistoryTotal.value = 0
    }
  } catch (error) {
    console.error('加载订单历史失败:', error)
    orderHistory.value = []
    orderHistoryTotal.value = 0
  }
}

const prevPage = () => {
  if (orderHistoryPageNum.value > 1) {
    orderHistoryPageNum.value--
    fetchOrderHistory()
  }
}

const nextPage = () => {
  const totalPages = Math.ceil(orderHistoryTotal.value / orderHistoryPageSize.value)
  if (orderHistoryPageNum.value < totalPages) {
    orderHistoryPageNum.value++
    fetchOrderHistory()
  }
}

const handleJoinMembership = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/api/order/getLegalOrderbyUid?uid=${userId}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    const result = await response.json()

    if (result.code === 200 && result.data) {
      alert('您已有有效订单，请使用续约功能延长有效期')
    } else {
      showJoinPackageModal()
    }
  } catch (error) {
    console.error('检查订单状态失败:', error)
    alert('网络错误，请稍后重试')
  }
}

const showJoinPackageModal = async () => {
  modalTitle.value = '选择会员套餐'
  showPackageModal.value = true
  packageActionType.value = 'join'
  await loadPackageList()
}

const showRenewPackageModal = async () => {
  modalTitle.value = '选择续约套餐'
  showPackageModal.value = true
  packageActionType.value = 'renew'
  await loadPackageList()
}

const loadPackageList = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/api/order/package/all`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    const result = await response.json()

    if (result.code === 200 && result.data) {
      packageList.value = result.data
    } else {
      packageList.value = []
    }
  } catch (error) {
    console.error('加载套餐列表失败:', error)
    packageList.value = []
  }
}

const selectPackage = async (packageId, actionType) => {
  if (!confirm('确认选择该套餐吗？')) return

  closePackageModal()

  if (actionType === 'join') {
    await createOrder(packageId)
  } else if (actionType === 'renew') {
    await renewOrder(packageId)
  }
}

const createOrder = async (packageId) => {
  try {
    const response = await fetch(`${API_BASE_URL}/api/order/create`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ pid: packageId })
    })
    const result = await response.json()

    if (result.code === 200) {
      alert('加入会员成功！')
      await loadOrders()
    } else {
      alert(result.msg || '加入会员失败')
    }
  } catch (error) {
    console.error('创建订单失败:', error)
    alert('网络错误，请稍后重试')
  }
}

const renewOrder = async (packageId) => {
  try {
    const response = await fetch(`${API_BASE_URL}/api/order/update`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ pid: packageId })
    })
    const result = await response.json()

    if (result.code === 200) {
      alert('续约成功！')
      await loadOrders()
    } else {
      alert(result.msg || '续约失败')
    }
  } catch (error) {
    console.error('续约失败:', error)
    alert('网络错误，请稍后重试')
  }
}

const closePackageModal = () => {
  showPackageModal.value = false
}

const openBookingModal = (type, resourceId, resourceName) => {
  currentBookingType.value = type
  currentResourceId.value = resourceId
  selectedSlotId.value = null

  const modalTitle = type === 'equipment' ? `预约器材 - ${resourceName}` : `预约教练 - ${resourceName}`
  bookingModalTitle.value = modalTitle

  const tomorrow = new Date()
  tomorrow.setDate(tomorrow.getDate() + 1)
  const dateStr = tomorrow.toISOString().split('T')[0]
  bookingDate.value = dateStr

  showBookingModal.value = true
  loadAvailableSlots()
}

const loadAvailableSlots = async () => {
  try {
    const slotsResponse = await fetch(`${API_BASE_URL}/api/resource/timeslot/all`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    const slotsResult = await slotsResponse.json()

    if (slotsResult.code !== 200 || !slotsResult.data) {
      timeSlots.value = []
      return
    }

    timeSlots.value = slotsResult.data

    if (currentBookingType.value === 'equipment') {
      const equipResponse = await fetch(`${API_BASE_URL}/api/resource/equipment/get?id=${currentResourceId.value}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      })
      const equipResult = await equipResponse.json()
      if (equipResult.code === 200) {
        equipmentInfo.value = equipResult.data
      }
    }

    let bookedData = []
    if (currentBookingType.value === 'equipment') {
      const bookedResponse = await fetch(`${API_BASE_URL}/api/book/equipment/list?equipmentId=${currentResourceId.value}&date=${bookingDate.value}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      })
      const bookedResult = await bookedResponse.json()
      if (bookedResult.code === 200 && bookedResult.data) {
        bookedData = bookedResult.data
      }
    } else {
      const bookedResponse = await fetch(`${API_BASE_URL}/api/book/coach/list?coachId=${currentResourceId.value}&date=${bookingDate.value}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      })
      const bookedResult = await bookedResponse.json()
      if (bookedResult.code === 200 && bookedResult.data) {
        bookedData = bookedResult.data
      }
    }

    timeSlots.value = timeSlots.value.map(slot => {
      const slotBookings = bookedData.filter(b => b.slotId === slot.id)
      return {
        ...slot,
        slotBookings,
        isFullyBooked: currentBookingType.value === 'equipment' && equipmentInfo.value && slotBookings.length >= equipmentInfo.value.num
      }
    })
  } catch (error) {
    console.error('加载时间段失败:', error)
    timeSlots.value = []
  }
}

const getSlotClass = (slot) => {
  let className = 'slot-item'
  if (slot.isFullyBooked || (slot.slotBookings && slot.slotBookings.length > 0 && currentBookingType.value === 'coach')) {
    className += ' disabled'
  } else if (selectedSlotId.value === slot.id) {
    className += ' selected'
  }
  return className
}

const getSlotStatus = (slot) => {
  if (currentBookingType.value === 'equipment' && equipmentInfo.value) {
    const remaining = equipmentInfo.value.num - (slot.slotBookings ? slot.slotBookings.length : 0)
    if (remaining <= 0) {
      return '已约满'
    }
    return `剩余 ${remaining}/${equipmentInfo.value.num}`
  } else {
    if (slot.slotBookings && slot.slotBookings.length > 0) {
      return '已预约'
    }
    return '可选择'
  }
}

const selectSlot = (slotId) => {
  const slot = timeSlots.value.find(s => s.id === slotId)
  if (!slot || slot.isFullyBooked || (slot.slotBookings && slot.slotBookings.length > 0 && currentBookingType.value === 'coach')) {
    return
  }
  selectedSlotId.value = slotId
}

const confirmBooking = async () => {
  if (!bookingDate.value) {
    alert('请选择日期')
    return
  }

  if (!selectedSlotId.value) {
    alert('请选择时间段')
    return
  }

  if (!confirm('确认预约吗？')) return

  const bookingData = {
    bookingDate: bookingDate.value,
    slotId: selectedSlotId.value,
    resourceId: currentResourceId.value
  }

  try {
    const url = currentBookingType.value === 'equipment'
        ? `${API_BASE_URL}/api/book/equipment`
        : `${API_BASE_URL}/api/book/coach`

    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(bookingData)
    })

    const result = await response.json()

    if (result.code === 200) {
      alert(result.msg || '预约成功！')
      closeBookingModal()
      if (currentBookingType.value === 'equipment') {
        await loadEquipmentList()
      } else {
        await loadCoachList()
      }
    } else {
      alert(result.msg || '预约失败')
    }
  } catch (error) {
    console.error('预约失败:', error)
    alert('网络错误，请稍后重试')
  }
}

const closeBookingModal = () => {
  showBookingModal.value = false
  currentBookingType.value = ''
  currentResourceId.value = null
  selectedSlotId.value = null
}

const sendMessage = async () => {
  const message = chatInput.value.trim()
  if (!message) return

  chatMessages.value.push({ type: 'user', content: message })
  chatInput.value = ''
  sendingMessage.value = true

  const aiMessageIndex = chatMessages.value.length
  chatMessages.value.push({ type: 'ai', content: '' })

  let fullResponse = ''

  try {
    const response = await fetch(PYTHON_AI_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'text/plain',
        'Authorization': `Bearer ${token}`
      },
      body: message
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()

      if (done) break

      buffer += decoder.decode(value, { stream: true })

      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        const trimmedLine = line.trim()
        if (!trimmedLine) continue

        if (trimmedLine === '[DONE]') {
          continue
        }

        try {
          let jsonStr = trimmedLine

          if (trimmedLine.startsWith('data:')) {
            jsonStr = trimmedLine.substring(5).trim()
          }

          const jsonData = JSON.parse(jsonStr)

          if (jsonData.type === 'content' && jsonData.data) {
            fullResponse += jsonData.data
            chatMessages.value[aiMessageIndex].content = fullResponse
            scrollToBottom()
          }
        } catch (e) {
          console.warn('JSON 解析失败:', e.message)
        }
      }
    }

    if (!fullResponse) {
      chatMessages.value[aiMessageIndex].content = '抱歉，我暂时无法回答这个问题'
    }
  } catch (error) {
    console.error('AI请求失败:', error)
    chatMessages.value[aiMessageIndex].content = '网络错误，请稍后重试'
  } finally {
    sendingMessage.value = false
    scrollToBottom()
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatMessagesRef.value) {
      chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
    }
  })
}

const handleChatKeypress = (e) => {
  if (e.key === 'Enter') {
    sendMessage()
  }
}

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  localStorage.removeItem('userId')
  localStorage.removeItem('loginTime')
  window.location.href = 'login'
}

// 显示编辑弹窗
const showEditModal = () => {
  editForm.username = userProfile.username || ''
  editForm.email = userProfile.email || ''
  editForm.password = ''
  editForm.confirmPassword = ''
  showEditProfileModal.value = true
}

// 关闭编辑弹窗
const closeEditModal = () => {
  showEditProfileModal.value = false
  editForm.password = ''
  editForm.confirmPassword = ''
}

// 提交编辑
const submitEdit = async () => {
  // 验证表单
  if (!editForm.username || !editForm.username.trim()) {
    alert('用户名不能为空')
    return
  }

  if (!editForm.email || !editForm.email.trim()) {
    alert('邮箱不能为空')
    return
  }

  // 验证邮箱格式
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(editForm.email)) {
    alert('请输入有效的邮箱地址')
    return
  }

  // 如果填写了密码，需要验证
  if (editForm.password) {
    if (editForm.password.length < 6) {
      alert('密码长度至少为6位')
      return
    }

    if (editForm.password !== editForm.confirmPassword) {
      alert('两次输入的密码不一致')
      return
    }
  }

  submitting.value = true

  try {
    const updateData = {
      id: userId,
      username: editForm.username.trim(),
      email: editForm.email.trim()
    }

    // 只有当用户输入了新密码时才包含密码字段
    if (editForm.password) {
      updateData.password = editForm.password
    }

    const response = await fetch(`${API_BASE_URL}/api/user/update`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(updateData)
    })

    const result = await response.json()

    if (result.code === 200) {
      alert('修改成功！')
      closeEditModal()
      // 重新加载用户信息
      await loadUserProfile()
    } else {
      alert(result.msg || '修改失败')
    }
  } catch (error) {
    console.error('修改个人信息失败:', error)
    alert('网络错误，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadUserProfile()
})
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.dashboard-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  display: flex;
  overflow: hidden;
  background: #ffffff;
}

body {
  font-family: 'Microsoft YaHei', Arial, sans-serif;
  background: #ffffff;
  margin: 0;
  padding: 0;
  overflow: hidden;
}


.sidebar {
  width: 260px;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
}

.sidebar-header {
  padding: 30px 20px;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.sidebar-header h2 {
  font-size: 22px;
  margin-bottom: 5px;
}

.sidebar-header p {
  font-size: 12px;
  opacity: 0.8;
}

.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: white;
  margin: 0 auto 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  color: #667eea;
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
  background: rgba(255, 255, 255, 0.2);
  border-left-color: white;
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
  border-top: 1px solid rgba(255, 255, 255, 0.2);
}

.logout-btn {
  width: 100%;
  padding: 12px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid white;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.3s;
  font-size: 14px;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.3);
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
  color: #333;
}

.content-area {
  flex: 1;
  padding: 30px;
  overflow-y: auto;
}

.content-card {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
}

.content-card h3 {
  color: #667eea;
  margin-bottom: 20px;
  font-size: 20px;
}

.profile-info {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.profile-header h3 {
  color: #667eea;
  font-size: 20px;
  margin: 0;
}

.edit-btn {
  padding: 8px 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s;
}

.edit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.info-item {
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.info-label {
  color: #666;
  font-size: 14px;
  margin-bottom: 5px;
}

.info-value {
  color: #333;
  font-size: 16px;
  font-weight: 600;
}

.booking-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.booking-card {
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  background: white;
}

.booking-card:hover {
  border-color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.2);
}

.booking-card.coach-required {
  border-color: #f5576c;
  background: #fff5f7;
}

.booking-card.self-service {
  border-color: #e0e0e0;
  background: white;
}

.coach-required-badge {
  position: absolute;
  top: -8px;
  left: -8px;
  background: linear-gradient(135deg, #f5576c 0%, #f093fb 100%);
  color: white;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(245, 87, 108, 0.3);
}

.equipment-quantity {
  color: #667eea;
  font-size: 13px;
  margin-top: 8px;
  font-weight: 600;
}

.coach-status {
  font-size: 12px;
  margin-top: 4px;
  font-weight: 500;
}

.coach-status.required {
  color: #f5576c;
}

.coach-status.optional {
  color: #67c23a;
}

.booking-card h4 {
  color: #333;
  margin-bottom: 10px;
}

.booking-card p {
  color: #333;
  font-size: 14px;
  margin-bottom: 15px;
}

.book-btn {
  padding: 10px 20px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}

.book-btn:hover {
  background: #5568d3;
}

.booking-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  border-bottom: 2px solid #e0e0e0;
}

.booking-tab {
  padding: 12px 24px;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;
  color: #666;
  border-bottom: 3px solid transparent;
  transition: all 0.3s;
}

.booking-tab.active {
  color: #667eea;
  border-bottom-color: #667eea;
}

.booking-tab:hover {
  color: #667eea;
}

.my-bookings-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.booking-item {
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: all 0.3s;
}

.booking-item:hover {
  border-color: #667eea;
  box-shadow: 0 3px 10px rgba(102, 126, 234, 0.1);
}

.booking-item-info h4 {
  color: #333;
  margin-bottom: 8px;
  font-size: 16px;
}

.booking-item-info p {
  color: #666;
  font-size: 13px;
  margin: 4px 0;
}

.booking-item-info .booking-date {
  color: #667eea;
  font-weight: 600;
}

.booking-status {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  margin-top: 8px;
}

.booking-status.active {
  background: #e8f5e9;
  color: #2e7d32;
}

.booking-status.cancelled {
  background: #ffebee;
  color: #c62828;
}

.cancel-booking-btn {
  padding: 10px 20px;
  background: #f5576c;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s;
}

.cancel-booking-btn:hover {
  background: #e0455a;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(245, 87, 108, 0.3);
}

.empty-bookings {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-bookings .empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.order-item {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-info h4 {
  color: #333;
  margin-bottom: 8px;
}

.order-info p {
  color: #666;
  font-size: 14px;
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: 600px;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  overflow: hidden;
}

.chat-messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f8f9fa;
}

.message {
  margin-bottom: 15px;
  display: flex;
  gap: 10px;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #667eea;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.message.user .message-avatar {
  background: #764ba2;
}

.message-content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  background: white;
  color: #333;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
  font-size: 14px;
  line-height: 1.6;
}

.message.user .message-content {
  background: #667eea;
  color: white;
}

.chat-input-area {
  padding: 20px;
  background: white;
  border-top: 1px solid #e0e0e0;
  display: flex;
  gap: 10px;
}

.chat-input {
  flex: 1;
  padding: 12px 16px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
}

.chat-input:focus {
  outline: none;
  border-color: #667eea;
}

.send-btn {
  padding: 12px 24px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}

.send-btn:hover:not(:disabled) {
  background: #5568d3;
}

.send-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.order-actions-bar {
  position: fixed;
  bottom: 30px;
  right: 30px;
  display: flex;
  gap: 15px;
  z-index: 100;
}

.action-btn {
  padding: 14px 28px;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.join-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.join-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

.renew-main-btn {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.renew-main-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(245, 87, 108, 0.4);
}

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

.package-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.package-item {
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.package-item:hover {
  border-color: #667eea;
  background: #f8f9ff;
  transform: translateX(5px);
}

.package-info h4 {
  color: #333;
  margin-bottom: 8px;
  font-size: 18px;
}

.package-info p {
  color: #666;
  font-size: 14px;
  margin: 4px 0;
}

.package-price {
  text-align: right;
}

.package-price .price {
  font-size: 24px;
  color: #f5576c;
  font-weight: bold;
}

.package-price .unit {
  font-size: 14px;
  color: #999;
}

.booking-modal-content {
  max-width: 700px;
}

.date-selector {
  margin-bottom: 20px;
}

.date-selector label {
  display: block;
  margin-bottom: 8px;
  color: #666;
  font-weight: 600;
}

.date-input {
  width: 100%;
  padding: 12px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
}

.date-input:focus {
  outline: none;
  border-color: #667eea;
}

.slot-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 12px;
  margin-top: 15px;
}

.slot-item {
  padding: 12px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.slot-item:hover:not(.disabled) {
  border-color: #667eea;
  background: #f8f9ff;
}

.slot-item.selected {
  border-color: #667eea;
  background: #667eea;
  color: white;
}

.slot-item.disabled {
  opacity: 0.4;
  cursor: not-allowed;
  background: #f5f5f5;
}

.slot-time {
  font-weight: 600;
  margin-bottom: 4px;
  color: #333;
}

.slot-status {
  font-size: 12px;
  color: #666;
}

.slot-item.selected .slot-status {
  color: rgba(255, 255, 255, 0.9);
}

.confirm-booking-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 20px;
  transition: all 0.3s;
}

.confirm-booking-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
}

.confirm-booking-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 订单操作按钮 */
.order-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.action-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.action-btn.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.action-btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.action-btn.secondary {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.action-btn.secondary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(245, 87, 108, 0.4);
}

.action-btn.info {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.action-btn.info:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(79, 172, 254, 0.4);
}

/* 订单历史 */
.order-history {
  margin-top: 20px;
}

.order-history h4 {
  color: #333;
  margin-bottom: 15px;
  font-size: 18px;
}

.order-history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}

.order-history-item {
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  padding: 15px;
  transition: all 0.3s;
}

.order-history-item:hover {
  border-color: #667eea;
  background: #f8f9ff;
}

.order-history-item .order-info h5 {
  color: #667eea;
  margin-bottom: 8px;
  font-size: 16px;
}

.order-history-item .order-info p {
  color: #666;
  font-size: 14px;
  margin: 4px 0;
}

.order-status {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  margin-top: 8px;
}

.order-status.active {
  background: #d4edda;
  color: #155724;
}

.order-status.expired {
  background: #f8d7da;
  color: #721c24;
}

.empty-history {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin: 20px 0;
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

.back-btn {
  width: 100%;
  padding: 12px;
  background: #f0f0f0;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  color: #666;
}

.back-btn:hover {
  background: #e0e0e0;
}

/* 编辑个人信息弹窗 */
.edit-profile-modal {
  max-width: 500px;
}

.edit-form {
  padding: 10px 0;
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

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
