NEW_FILE_CODE
<template>
  <div class="login-page">
    <el-card class="login-container" shadow="always">
      <div class="login-header">
        <h1>🏋️ 健身房管理系统</h1>
        <p>欢迎回来，请登录您的账户</p>
      </div>

      <el-radio-group v-model="currentRole" class="role-selector">
        <el-radio-button value="user">👤 用户登录</el-radio-button>
        <el-radio-button value="admin">🔑 管理员登录</el-radio-button>
      </el-radio-group>

      <el-alert
        v-if="errorMessage"
        :title="errorMessage"
        type="error"
        show-icon
        closable
        @close="hideError"
        style="margin-bottom: 20px;"
      />

      <el-form :model="loginForm" @submit.prevent="handleLogin" label-position="top">
        <el-form-item label="用户名" required>
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            clearable
            autocomplete="username"
          />
        </el-form-item>

        <el-form-item label="密码" required>
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            autocomplete="current-password"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            native-type="submit"
            :loading="loading"
            class="login-btn"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-link">
        <p>还没有账户？<el-link type="primary" @click="goToRegister">立即注册</el-link></p>
      </div>
    </el-card>

    <!-- 订单逾期提示弹窗 -->
    <el-dialog
      v-model="showRenewModal"
      title="⚠️ 订单已逾期"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="modal-body">
        <el-alert
          title="您的会员订单已过期"
          type="warning"
          :closable="false"
          show-icon
        />
        <p class="hint-text">需要续费后才能继续使用服务</p>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeRenewModal">退出登录</el-button>
          <el-button type="danger" @click="goToRenew">去续费</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const API_BASE_URL = 'http://localhost:80'
const router = useRouter()

const currentRole = ref('user')
const loading = ref(false)
const errorMessage = ref('')
const showRenewModal = ref(false)
const selectedPackageId = ref<number | null>(null)
const packageList = ref<any[]>([])
const currentUserId = ref<number>(0)

const loginForm = reactive({
  username: '',
  password: ''
})

const roles = [
  { value: 'user', label: '👤 用户登录' },
  { value: 'admin', label: '🔑 管理员登录' }
]

const selectRole = (role: string) => {
  currentRole.value = role
  hideError()
}

const showError = (message: string) => {
  errorMessage.value = message
  ElMessage.error(message)
}

const hideError = () => {
  errorMessage.value = ''
}

const saveLoginInfo = (token: string, role: string, userId: string) => {
  localStorage.setItem('token', token)
  localStorage.setItem('role', role)
  localStorage.setItem('userId', userId)
  localStorage.setItem('loginTime', new Date().getTime().toString())
}

// ... existing code ...
const decodeToken = (token: string) => {
  try {
    const parts = token.split('.')
    if (parts.length === 3 && parts[1]) {
      const payload = JSON.parse(atob(parts[1]))
      return {
        id: payload.id,
        role: payload.role
      }
    }
  } catch (e) {
    console.error('Token 解析失败:', e)
  }
  return {}
}
// ... existing code ...


const loadPackageList = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/api/order/package/all`)
    const result = await response.json()

    if (result.code === 200 && result.data) {
      packageList.value = result.data
      if (packageList.value.length > 0) {
        selectedPackageId.value = packageList.value[0].id
      }
    }
  } catch (error) {
    console.error('加载套餐列表失败:', error)
  }
}

const handleLogin = async () => {
  hideError()

  if (!loginForm.username || !loginForm.password) {
    showError('请输入用户名和密码')
    return
  }

  loading.value = true

  try {
    const loginUrl = currentRole.value === 'user'
      ? `${API_BASE_URL}/api/user/login`
      : `${API_BASE_URL}/api/user/managerLogin`

    const params = new URLSearchParams()
    params.append('userId', loginForm.username)
    params.append('password', loginForm.password)

    const response = await fetch(loginUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: params
    })

    const result = await response.json()

    if (result.code === 200 && result.data) {
      const token = result.data
      const userInfo = decodeToken(token)

      saveLoginInfo(token, currentRole.value, userInfo.id)

      if (currentRole.value === 'user') {
        // 检查后端返回的消息中是否包含会员过期提示
        if (result.msg && result.msg.includes('会员已过期')) {
          // 会员已过期，显示续费弹窗
          currentUserId.value = userInfo.id
          await loadPackageList()
          showRenewModal.value = true
        } else {
          // 会员有效，直接进入仪表板
          router.push('/dashboard')
        }
      } else {
        // 管理员直接登录
        router.push('/admin')
      }
    } else {
      showError(result.msg || '登录失败，请检查用户名和密码')
    }
  } catch (error) {
    console.error('登录错误:', error)
    showError('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

const checkUserOrder = async (userId: number) => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`${API_BASE_URL}/api/user/checkOrder?id=${userId}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    const result = await response.json()

    if (result.code === 200) {
      // 订单有效
      console.log('订单有效，剩余天数:', result.data)
      return true
    } else {
      // 订单已逾期或不存在
      console.log('订单检查失败:', result.msg)
      currentUserId.value = userId
      await loadPackageList()
      showRenewModal.value = true
      return false
    }
  } catch (error) {
    console.error('检查订单失败:', error)
    // 出错时也弹出续费框，让用户可以选择
    currentUserId.value = userId
    await loadPackageList()
    showRenewModal.value = true
    return false
  }
}

const closeRenewModal = () => {
  showRenewModal.value = false
  selectedPackageId.value = null
  // 清除登录信息并退出
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  localStorage.removeItem('userId')
  localStorage.removeItem('loginTime')
  errorMessage.value = '订单已逾期，请先续费后再登录'
}

const goToRenew = () => {
  showRenewModal.value = false
  // 跳转到独立的续费页面
  router.push('/renew')
}

const goToRegister = () => {
  router.push('/register')
}

onMounted(() => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  if (token && role) {
    if (role === 'user') {
      router.push('/dashboard')
    } else {
      router.push('/admin')
    }
  }
})
</script>

<style scoped>
.login-page {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #2c2c2c;
  font-family: 'Microsoft YaHei', Arial, sans-serif;
  z-index: 1;
}

.login-page::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: url('@/assets/gym-background.jpg') no-repeat center center fixed;
  background-size: cover;
  opacity: 0.3;
  z-index: -1;
}

.login-container {
  width: 500px;
  max-width: 90%;
  min-width: 400px;
  animation: slideUp 0.5s ease-out;
  background: rgba(44, 44, 44, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  color: #ffffff;
  font-size: 36px;
  margin-bottom: 10px;
  font-weight: 600;
}

.login-header p {
  color: #b0b0b0;
  font-size: 18px;
}

.role-selector {
  display: flex;
  width: 100%;
  margin-bottom: 30px;
}

.role-selector :deep(.el-radio-button) {
  flex: 1;
}

.role-selector :deep(.el-radio-button__inner) {
  width: 100%;
  padding: 12px 20px;
  background: #ffffff;
  color: #333333;
  border: 1px solid #d0d0d0;
  transition: all 0.3s ease;
}

.role-selector :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, #2c2c2c 0%, #1a1a1a 100%);
  color: #ffffff;
  border-color: #2c2c2c;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

/* 覆盖 Element Plus 输入框的默认焦点样式 */
:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #b0b0b0 inset !important;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #909090 inset !important;
}

.login-btn {
  width: 100%;
  background: linear-gradient(135deg, #d0d0d0 0%, #b0b0b0 100%);
  border: none;
  color: #333333;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 2px;
  transition: all 0.3s ease;
}

.login-btn:hover {
  background: linear-gradient(135deg, #e0e0e0 0%, #c0c0c0 100%);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.register-link {
  text-align: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #4a4a4a;
}

.register-link p {
  color: #b0b0b0;
}

.register-link :deep(.el-link) {
  color: #ffffff;
}

.modal-body {
  text-align: center;
}

.hint-text {
  color: #666;
  font-size: 16px;
  margin-top: 15px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
