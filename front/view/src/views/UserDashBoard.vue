 <template>
  <el-container class="dashboard-container">
    <!-- 侧边栏 -->
    <el-aside width="260px" class="sidebar">
      <div class="sidebar-header">
        <el-avatar :size="64" :src="userAvatar">
          <el-icon><UserFilled /></el-icon>
        </el-avatar>
        <h2>{{ userName }}</h2>
        <p>欢迎回来</p>
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
      </el-header>

      <el-main class="content-area">
        <!-- 个人信息 -->
        <div v-show="currentSection === 'profile'" class="content-section">
          <el-card class="content-card">
            <template #header>
              <div class="card-header">
                <h3>基本信息</h3>
                <el-button type="primary" @click="showEditModal">
                  <el-icon><Edit /></el-icon> 修改信息
                </el-button>
              </div>
            </template>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="用户名">
                {{ userProfile.username || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="邮箱">
                {{ userProfile.email || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="用户ID">
                {{ userProfile.id || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="注册时间">
                {{ userProfile.createdTime || '-' }}
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </div>

        <!-- 预约器材 -->
        <div v-show="currentSection === 'equipment-booking'" class="content-section">
          <el-card class="content-card">
            <template #header>
              <h3>可预约器材</h3>
            </template>
            <el-row :gutter="20">
              <el-col
                v-for="eq in equipmentList"
                :key="eq.id"
                :xs="24"
                :sm="12"
                :md="8"
                :lg="6"
              >
                <el-card
                  :class="Number(eq.needCoach) === 1 ? 'coach-required' : 'self-service'"
                  shadow="hover"
                  class="booking-card"
                >
                  <el-tag
                    v-if="Number(eq.needCoach) === 1"
                    type="warning"
                    class="coach-required-badge"
                  >
                    👨‍🏫 需教练
                  </el-tag>
                  <h4>{{ eq.name }}</h4>
                  <p class="equipment-quantity">📦 总数量：{{ eq.num }} 个</p>
                  <p :class="['coach-status', Number(eq.needCoach) === 1 ? 'required' : 'optional']">
                    {{ Number(eq.needCoach) === 1 ? '⚠️ 需要教练指导' : '✅ 可独立使用' }}
                  </p>
                  <el-button
                    type="primary"
                    class="book-btn"
                    @click="openBookingModal('equipment', eq.id, eq.name)"
                  >
                    立即预约
                  </el-button>
                </el-card>
              </el-col>
            </el-row>
          </el-card>
        </div>

        <!-- 预约教练 -->
        <div v-show="currentSection === 'coach-booking'" class="content-section">
          <el-card class="content-card">
            <template #header>
              <h3>可预约教练</h3>
            </template>
            <el-row :gutter="20">
              <el-col
                v-for="coach in coachList"
                :key="coach.id"
                :xs="24"
                :sm="12"
                :md="8"
                :lg="6"
              >
                <el-card shadow="hover" class="booking-card">
                  <h4>{{ coach.name }}</h4>
                  <el-button
                    type="primary"
                    @click="openBookingModal('coach', coach.id, coach.name)"
                  >
                    立即预约
                  </el-button>
                </el-card>
              </el-col>
            </el-row>
          </el-card>
        </div>

        <!-- 我的预约 -->
        <div v-show="currentSection === 'my-bookings'" class="content-section">
          <el-card class="content-card">
            <template #header>
              <h3>我的预约</h3>
            </template>
            <el-tabs v-model="currentBookingTab" @tab-change="switchBookingTab">
              <el-tab-pane label="器材预约" name="equipment">
                <el-empty v-if="equipmentBookings.length === 0" description="暂无器材预约记录" />
                <el-table v-else :data="equipmentBookings" style="width: 100%">
                  <el-table-column prop="resourceName" label="器材" />
                  <el-table-column prop="bookingDate" label="日期" />
                  <el-table-column prop="slotId" label="时间段ID" />
                  <el-table-column label="状态">
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
                  <el-table-column label="操作">
                    <template #default="{ row }">
                      <el-button
                        v-if="canCancelBooking(row)"
                        type="danger"
                        size="small"
                        @click="cancelBooking('equipment', row.id)"
                      >
                        取消预约
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-tab-pane>
              <el-tab-pane label="教练预约" name="coach">
                <el-empty v-if="coachBookings.length === 0" description="暂无教练预约记录" />
                <el-table v-else :data="coachBookings" style="width: 100%">
                  <el-table-column prop="resourceName" label="教练" />
                  <el-table-column prop="bookingDate" label="日期" />
                  <el-table-column prop="slotId" label="时间段ID" />
                  <el-table-column label="状态">
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
                  <el-table-column label="操作">
                    <template #default="{ row }">
                      <el-button
                        v-if="canCancelBooking(row)"
                        type="danger"
                        size="small"
                        @click="cancelBooking('coach', row.id)"
                      >
                        取消预约
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </el-tab-pane>
            </el-tabs>
          </el-card>
        </div>

        <!-- 订单与续约 -->
        <div v-show="currentSection === 'orders'" class="content-section">
          <el-card class="content-card">
            <template #header>
              <h3>我的订单</h3>
            </template>
            <div class="order-actions">
              <el-button
                v-if="!currentOrder"
                type="primary"
                @click="handleJoinMembership"
              >
                加入会员
              </el-button>
              <el-button
                v-if="currentOrder"
                type="success"
                @click="showRenewPackageModal"
              >
                续约
              </el-button>
              <el-button type="info" @click="loadOrderHistory">查看订单历史</el-button>
            </div>

            <div v-if="!showOrderHistory">
              <el-result
                v-if="currentOrder"
                icon="success"
                title="当前有效订单"
                :sub-title="`订单 #${currentOrder.id}`"
              >
                <template #extra>
                  <el-descriptions :column="1" border>
                    <el-descriptions-item label="开始时间">
                      {{ currentOrder.orderStartTime }}
                    </el-descriptions-item>
                    <el-descriptions-item label="结束时间">
                      {{ currentOrder.orderEndTime }}
                    </el-descriptions-item>
                    <el-descriptions-item label="状态">
                      <el-tag :type="currentOrder.status === 1 ? 'success' : 'info'">
                        {{ currentOrder.status === 1 ? '有效' : '已过期' }}
                      </el-tag>
                    </el-descriptions-item>
                  </el-descriptions>
                </template>
              </el-result>
              <el-empty v-else description="暂无有效订单" />
            </div>

            <!-- 订单历史记录 -->
            <div v-if="showOrderHistory" class="order-history">
              <h4>订单历史记录</h4>
              <el-empty v-if="orderHistory.length === 0" description="暂无订单历史记录" />
              <el-table v-else :data="orderHistory" style="width: 100%">
                <el-table-column prop="id" label="订单号" width="100" />
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
              </el-table>
              <el-pagination
                v-if="orderHistoryTotal > 0"
                v-model:current-page="orderHistoryPageNum"
                :page-size="orderHistoryPageSize"
                :total="orderHistoryTotal"
                layout="prev, pager, next"
                @current-change="fetchOrderHistory"
                style="margin-top: 20px; justify-content: center;"
              />
              <el-button @click="showOrderHistory = false" style="margin-top: 20px;">
                返回
              </el-button>
            </div>
          </el-card>
        </div>

        <!-- AI助手 -->
        <div v-show="currentSection === 'ai-assistant'" class="content-section">
          <el-card class="content-card">
            <template #header>
              <h3>AI健身助手</h3>
            </template>
            <div class="chat-container">
              <div class="chat-messages" ref="chatMessagesRef">
                <div
                  v-for="(msg, index) in chatMessages"
                  :key="index"
                  :class="['message', msg.type]"
                >
                  <el-avatar :src="msg.type === 'ai' ? 'https://api.dicebear.com/7.x/bottts/svg?seed=fitness-ai' : ''" :icon="msg.type === 'user' ? UserFilled : undefined" />
                  <div class="message-content">{{ msg.content }}</div>
                </div>
              </div>
              <div class="chat-input-area">
                <el-input
                  v-model="chatInput"
                  @keypress="handleChatKeypress"
                  placeholder="输入您的问题..."
                  clearable
                />
                <el-button
                  type="primary"
                  @click="sendMessage"
                  :loading="sendingMessage"
                >
                  发送
                </el-button>
              </div>
            </div>
          </el-card>
        </div>
      </el-main>
    </el-container>

    <!-- 套餐选择弹窗 -->
    <el-dialog
      v-model="showPackageModal"
      :title="modalTitle"
      width="70%"
    >
      <el-row :gutter="20">
        <el-col
          v-for="pkg in packageList"
          :key="pkg.id"
          :xs="24"
          :sm="12"
          :md="8"
        >
          <el-card
            shadow="hover"
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
          </el-card>
        </el-col>
      </el-row>
    </el-dialog>

    <!-- 预约弹窗 -->
    <el-dialog
      v-model="showBookingModal"
      :title="bookingModalTitle"
      width="600px"
    >
      <el-form label-position="top">
        <el-form-item label="选择日期：">
          <el-date-picker
            v-model="bookingDate"
            type="date"
            placeholder="选择日期"
            @change="loadAvailableSlots"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="选择时间段：">
          <el-radio-group v-model="selectedSlotId" class="slot-grid">
            <el-radio
              v-for="slot in timeSlots"
              :key="slot.id"
              :value="slot.id"
              :disabled="getSlotDisabled(slot)"
              class="slot-item"
              :class="getSlotClass(slot)"
            >
              <div class="slot-time">
                {{ slot.startTime.substring(0, 5) }} - {{ slot.endTime.substring(0, 5) }}
              </div>
              <div class="slot-status">{{ getSlotStatus(slot) }}</div>
            </el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeBookingModal">取消</el-button>
        <el-button
          type="primary"
          @click="confirmBooking"
          :disabled="!selectedSlotId"
        >
          确认预约
        </el-button>
      </template>
    </el-dialog>

    <!-- 修改个人信息弹窗 -->
    <el-dialog
      v-model="showEditProfileModal"
      title="修改个人信息"
      width="500px"
    >
      <el-form :model="editForm" label-position="top">
        <el-form-item label="用户名：">
          <el-input
            v-model="editForm.username"
            placeholder="请输入用户名"
            clearable
          />
        </el-form-item>
        <el-form-item label="邮箱：">
          <el-input
            v-model="editForm.email"
            placeholder="请输入邮箱"
            clearable
          />
        </el-form-item>
        <el-form-item label="新密码（留空则不修改）：">
          <el-input
            v-model="editForm.password"
            type="password"
            placeholder="输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码：">
          <el-input
            v-model="editForm.confirmPassword"
            type="password"
            placeholder="再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeEditModal">取消</el-button>
        <el-button
          type="primary"
          @click="submitEdit"
          :loading="submitting"
        >
          保存
        </el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, UserFilled, ChatDotRound } from '@element-plus/icons-vue'

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
const userAvatar = ref('')
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
const equipmentBookings = ref([])
const coachBookings = ref([])
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
      userAvatar.value = user.userPic || ''
      Object.assign(userProfile, user)
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    ElMessage.error('加载用户信息失败')
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
    ElMessage.error('加载器材列表失败')
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
    ElMessage.error('加载教练列表失败')
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
    console.log('预约记录响应:', result)

    if (result.code === 200 && result.data) {
      // 处理可能的分页格式或直接数组格式
      let bookings = Array.isArray(result.data) ? result.data : (result.data.records || [])
      
      if (bookings.length === 0) {
        if (currentBookingTab.value === 'equipment') {
          equipmentBookings.value = []
        } else {
          coachBookings.value = []
        }
        return
      }

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

      if (currentBookingTab.value === 'equipment') {
        equipmentBookings.value = enrichedBookings
      } else {
        coachBookings.value = enrichedBookings
      }
    } else {
      if (currentBookingTab.value === 'equipment') {
        equipmentBookings.value = []
      } else {
        coachBookings.value = []
      }
    }
  } catch (error) {
    console.error('加载预约记录失败:', error)
    if (currentBookingTab.value === 'equipment') {
      equipmentBookings.value = []
    } else {
      coachBookings.value = []
    }
    ElMessage.error('加载预约记录失败')
  }
}

// 格式化日期时间为 YYYY-MM-DD HH:mm
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  
  // 处理数组格式 [year, month, day, hour, minute, second, nano]
  if (Array.isArray(dateTime)) {
    const [year, month, day, hour = 0, minute = 0] = dateTime
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

const canCancelBooking = (booking) => {
  if (booking.status !== 1) return false
  
  // 处理 bookingDate 可能是数组格式的情况
  let bookingDateObj
  if (Array.isArray(booking.bookingDate)) {
    const [year, month, day] = booking.bookingDate
    bookingDateObj = new Date(year, month - 1, day)
  } else {
    bookingDateObj = new Date(booking.bookingDate)
  }
  
  // 获取今天的日期（零点）
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  
  return bookingDateObj >= today
}

const cancelBooking = async (type, bookingId) => {
  try {
    await ElMessageBox.confirm('确认取消该预约吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

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
      ElMessage.success('取消预约成功！')
      await loadMyBookings()
    } else {
      ElMessage.error(result.msg || '取消预约失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消预约失败:', error)
      ElMessage.error('网络错误，请稍后重试')
    }
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
    ElMessage.error('加载订单失败')
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
    ElMessage.error('加载订单历史失败')
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
      ElMessage.warning('您已有有效订单，请使用续约功能延长有效期')
    } else {
      showJoinPackageModal()
    }
  } catch (error) {
    console.error('检查订单状态失败:', error)
    ElMessage.error('网络错误，请稍后重试')
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
    ElMessage.error('加载套餐列表失败')
  }
}

const selectPackage = async (packageId, actionType) => {
  try {
    await ElMessageBox.confirm('确认选择该套餐吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })

    closePackageModal()

    if (actionType === 'join') {
      await createOrder(packageId)
    } else if (actionType === 'renew') {
      await renewOrder(packageId)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
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
      ElMessage.success('加入会员成功！')
      await loadOrders()
    } else {
      ElMessage.error(result.msg || '加入会员失败')
    }
  } catch (error) {
    console.error('创建订单失败:', error)
    ElMessage.error('网络错误，请稍后重试')
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
      ElMessage.success('续约成功！')
      await loadOrders()
    } else {
      ElMessage.error(result.msg || '续约失败')
    }
  } catch (error) {
    console.error('续约失败:', error)
    ElMessage.error('网络错误，请稍后重试')
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
    ElMessage.error('加载时间段失败')
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

const getSlotDisabled = (slot) => {
  return slot.isFullyBooked || (slot.slotBookings && slot.slotBookings.length > 0 && currentBookingType.value === 'coach')
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
    ElMessage.warning('请选择日期')
    return
  }

  if (!selectedSlotId.value) {
    ElMessage.warning('请选择时间段')
    return
  }

  try {
    await ElMessageBox.confirm('确认预约吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })

    const bookingData = {
      bookingDate: bookingDate.value,
      slotId: selectedSlotId.value,
      resourceId: currentResourceId.value
    }

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
      ElMessage.success(result.msg || '预约成功！')
      closeBookingModal()
      if (currentBookingType.value === 'equipment') {
        await loadEquipmentList()
      } else {
        await loadCoachList()
      }
    } else {
      ElMessage.error(result.msg || '预约失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('预约失败:', error)
      ElMessage.error('网络错误，请稍后重试')
    }
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
    ElMessage.warning('用户名不能为空')
    return
  }

  if (!editForm.email || !editForm.email.trim()) {
    ElMessage.warning('邮箱不能为空')
    return
  }

  // 验证邮箱格式
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(editForm.email)) {
    ElMessage.warning('请输入有效的邮箱地址')
    return
  }

  // 如果填写了密码，需要验证
  if (editForm.password) {
    if (editForm.password.length < 6) {
      ElMessage.warning('密码长度至少为6位')
      return
    }

    if (editForm.password !== editForm.confirmPassword) {
      ElMessage.warning('两次输入的密码不一致')
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
      ElMessage.success('修改成功！')
      closeEditModal()
      // 重新加载用户信息
      await loadUserProfile()
    } else {
      ElMessage.error(result.msg || '修改失败')
    }
  } catch (error) {
    console.error('修改个人信息失败:', error)
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadUserProfile()
})
</script>

<style scoped>
.dashboard-container {
  height: 100vh;
}

.sidebar {
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.sidebar-header h2 {
  margin: 15px 0 5px;
  font-size: 26px;
}

.sidebar-header p {
  margin: 0;
  font-size: 18px;
  opacity: 0.8;
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
  background: rgba(255, 255, 255, 0.2) !important;
  color: white;
}

.menu-icon {
  margin-right: 10px;
}

.sidebar-footer {
  padding: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
}

.logout-btn {
  width: 100%;
}

.top-bar {
  background: white;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  align-items: center;
  padding: 0 30px;
}

.top-bar h1 {
  font-size: 32px;
  color: #333;
  margin: 0;
}

.content-area {
  background: #f5f7fa;
  padding: 20px;
}

.content-section {
  width: 100%;
  max-width: 1600px;
  margin: 0 auto;
  padding: 0 20px;
}

.content-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 24px;
}

.booking-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.booking-card:hover {
  transform: translateY(-5px);
}

.booking-card h4 {
  margin: 10px 0;
  font-size: 20px;
}

.equipment-quantity {
  color: #666;
  font-size: 18px;
}

.coach-status {
  font-size: 18px;
  margin: 10px 0;
}

.coach-status.required {
  color: #f56c6c;
}

.coach-status.optional {
  color: #67c23a;
}

.book-btn {
  width: 100%;
  margin-top: 10px;
}

.coach-required-badge {
  position: absolute;
  top: 10px;
  right: 10px;
}

.order-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.order-history {
  margin-top: 20px;
}

.order-history h4 {
  margin-bottom: 15px;
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: 500px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 15px;
}

.message {
  display: flex;
  align-items: flex-start;
  margin-bottom: 15px;
  gap: 10px;
}

.message.user {
  flex-direction: row-reverse;
}

.message-content {
  max-width: 70%;
  padding: 10px 15px;
  border-radius: 8px;
  background: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.message.user .message-content {
  background: #667eea;
  color: white;
}

.chat-input-area {
  display: flex;
  gap: 10px;
}

.chat-input-area .el-input {
  flex: 1;
}

.package-item {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.package-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.package-info h4 {
  margin: 0 0 10px;
  font-size: 20px;
}

.package-info p {
  margin: 5px 0;
  color: #666;
  font-size: 18px;
}

.package-price {
  margin-top: 15px;
  text-align: center;
}

.package-price .price {
  font-size: 28px;
  color: #f56c6c;
  font-weight: bold;
}

.package-price .unit {
  font-size: 16px;
  color: #999;
  margin-top: 5px;
}

.slot-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 10px;
  width: 100%;
}

.slot-item {
  margin: 0 !important;
  padding: 12px 8px !important;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  text-align: center;
  transition: all 0.3s;
  height: auto !important;
  display: flex !important;
  flex-direction: column !important;
  align-items: center !important;
  justify-content: center !important;
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
  opacity: 0.5;
  cursor: not-allowed;
}

/* 隐藏 el-radio 的默认圆圈 */
.slot-item :deep(.el-radio__input) {
  display: none !important;
}

/* 调整 label 样式 */
.slot-item :deep(.el-radio__label) {
  width: 100%;
  padding: 0 !important;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.slot-time {
  font-weight: bold;
  font-size: 18px;
  line-height: 1.4;
}

.slot-status {
  font-size: 16px;
  line-height: 1.2;
  margin-top: 4px;
}
</style>
