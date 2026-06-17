
<template>
  <div class="renew-page">
    <el-card class="renew-container" shadow="always">
      <div class="renew-header">
        <h1>⚠️ 会员续费</h1>
        <p>您的会员已过期，请选择套餐进行续费</p>
      </div>

      <el-alert
        v-if="errorMessage"
        :title="errorMessage"
        type="error"
        show-icon
        closable
        @close="errorMessage = ''"
        style="margin-bottom: 20px;"
      />

      <div class="package-section">
        <h3>选择套餐</h3>
        <el-radio-group v-model="selectedPackageId" class="package-list">
          <el-radio
            v-for="pkg in packageList"
            :key="pkg.id"
            :value="pkg.id"
            size="large"
            class="package-item"
          >
            <div class="package-info">
              <h4>{{ pkg.name || '套餐' + pkg.level }}</h4>
              <p>时长：{{ pkg.duration }} 天</p>
              <p>等级：Lv.{{ pkg.level }}</p>
            </div>
            <div class="package-price">¥{{ pkg.price }}</div>
          </el-radio>
        </el-radio-group>
      </div>

      <div class="action-buttons">
        <el-button @click="handleLogout">退出登录</el-button>
        <el-button
          type="danger"
          @click="handleRenew"
          :disabled="!selectedPackageId || renewing"
          :loading="renewing"
        >
          {{ renewing ? '续费中...' : '确认续费' }}
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const API_BASE_URL = 'http://localhost:80'
const router = useRouter()

interface Package {
  id: number
  name?: string
  level: number
  duration: number
  price: number
}

const errorMessage = ref('')
const renewing = ref(false)
const selectedPackageId = ref<number | null>(null)
const packageList = ref<Package[]>([])

const loadPackageList = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch(`${API_BASE_URL}/api/order/package/all`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    const result = await response.json()

    if (result.code === 200 && result.data) {
      packageList.value = result.data
      const firstPackage = packageList.value[0]
      if (firstPackage && firstPackage.id) {
        selectedPackageId.value = firstPackage.id
      }
    } else {
      errorMessage.value = result.msg || '加载套餐失败'
    }
  } catch (error) {
    console.error('加载套餐列表失败:', error)
    errorMessage.value = '网络错误，请稍后重试'
  }
}

const handleRenew = async () => {
  if (!selectedPackageId.value) {
    ElMessage.warning('请选择一个套餐')
    return
  }

  try {
    await ElMessageBox.confirm('确认续费吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })

    renewing.value = true

    const token = localStorage.getItem('token')

    const response = await fetch(`${API_BASE_URL}/api/order/create`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({ pid: selectedPackageId.value })
    })

    const result = await response.json()

    if (result.code === 200) {
      ElMessage.success('续费成功！')
      router.push('/dashboard')
    } else {
      ElMessage.error(result.msg || '续费失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('续费错误:', error)
      ElMessage.error('网络错误，请稍后重试')
    }
  } finally {
    renewing.value = false
  }
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  localStorage.removeItem('userId')
  localStorage.removeItem('loginTime')
  router.push('/login')
}

onMounted(() => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  if (!token || role !== 'user') {
    router.push('/login')
    return
  }

  loadPackageList()
})
</script>

<style scoped>
.renew-page {
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
  padding: 20px;
  z-index: 1;
}

.renew-page::before {
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

.renew-container {
  width: 700px;
  max-width: 90%;
  min-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
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

.renew-header {
  text-align: center;
  margin-bottom: 30px;
}

.renew-header h1 {
  color: #ffffff;
  font-size: 36px;
  margin-bottom: 10px;
  font-weight: 600;
}

.renew-header p {
  color: #b0b0b0;
  font-size: 18px;
}

.package-section {
  margin-bottom: 30px;
}

.package-section h3 {
  color: #ffffff;
  font-size: 24px;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #4a4a4a;
}

.package-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  max-height: 450px;
  overflow-y: auto;
  width: 100%;
}

.package-item {
  margin: 0 !important;
  padding: 0 !important;
  border: 2px solid #5a5a5a;
  border-radius: 12px;
  transition: all 0.3s;
  min-height: 180px;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  background: rgba(60, 60, 60, 0.6);
}

.package-item:hover {
  border-color: #667eea;
  background: rgba(80, 80, 80, 0.7);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.package-item.is-checked {
  border-color: #667eea;
  border-width: 3px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.3) 0%, rgba(118, 75, 162, 0.3) 100%);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
  transform: translateY(-3px);
}

/* 隐藏 el-radio 的默认圆圈 */
.package-item :deep(.el-radio__input) {
  display: none !important;
}

/* 调整 label 样式 */
.package-item :deep(.el-radio__label) {
  width: 100%;
  padding: 20px !important;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100%;
  box-sizing: border-box;
}

.package-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
  overflow: hidden;
}

.package-info h4 {
  color: #ffffff;
  margin: 0 0 10px 0;
  font-size: 20px;
  font-weight: 600;
  line-height: 1.4;
  word-wrap: break-word;
  overflow-wrap: break-word;
  transition: color 0.3s;
}

.package-info p {
  color: #b0b0b0;
  font-size: 16px;
  margin: 0;
  line-height: 1.6;
  word-wrap: break-word;
  overflow-wrap: break-word;
  transition: color 0.3s;
}

/* 选中状态下的文字颜色 */
.package-item.is-checked .package-info h4 {
  color: #ffffff;
}

.package-item.is-checked .package-info p {
  color: #e0e0e0;
}

.package-price {
  font-size: 26px;
  color: #f5576c;
  font-weight: bold;
  margin-top: 15px;
  text-align: center;
  padding: 12px 0;
  border-top: 1px solid #5a5a5a;
}

.action-buttons {
  display: flex;
  gap: 15px;
  justify-content: flex-end;
  padding-top: 20px;
  border-top: 2px solid #f0f0f0;
}
</style>
