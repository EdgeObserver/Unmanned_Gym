<template>
  <div class="register-page">
    <el-card class="register-container" shadow="always">
      <div class="register-header">
        <h1>🏋️ 健身房管理系统</h1>
        <p>创建新账户,开始您的健身之旅</p>
      </div>

      <el-alert
        v-if="errorMessage"
        :title="errorMessage"
        type="error"
        show-icon
        closable
        @close="hideError"
        style="margin-bottom: 20px;"
      />

      <el-form :model="registerForm" @submit.prevent="handleRegister" label-position="top">
        <el-form-item label="用户名" required>
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名(2-20位)"
            minlength="2"
            maxlength="20"
            autocomplete="username"
            clearable
          />
        </el-form-item>

        <el-form-item label="密码" required>
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码(5-20位)"
            minlength="5"
            maxlength="20"
            autocomplete="new-password"
            show-password
          />
        </el-form-item>

        <el-form-item label="邮箱" required>
          <el-input
            v-model="registerForm.email"
            type="email"
            placeholder="请输入邮箱地址"
            autocomplete="email"
            clearable
          />
        </el-form-item>

        <el-form-item label="头像照片" required>
          <el-upload
            class="avatar-uploader"
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleAvatarChange"
            accept="image/*"
          >
            <img v-if="avatarPreview" :src="avatarPreview" class="avatar-preview" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item label="选择套餐" required>
          <el-radio-group v-model="selectedPackageId" class="package-selection">
            <el-radio
              v-for="pkg in packageList"
              :key="pkg.id"
              :value="pkg.id"
              size="large"
              class="package-card"
            >
              <div class="package-info">
                <h4>{{ pkg.name || '套餐' + pkg.level }}</h4>
                <p>时长:{{ pkg.duration }} 天</p>
                <p>等级:Lv.{{ pkg.level }}</p>
              </div>
              <div class="package-price">¥{{ pkg.price }}</div>
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            native-type="submit"
            :loading="loading"
            class="register-btn"
          >
            {{ loading ? '注册中...' : '注 册' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-link">
        <p>已有账户?<el-link type="primary" @click="goToLogin">立即登录</el-link></p>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const API_BASE_URL = 'http://localhost:80'
const router = useRouter()

interface Package {
  id: number
  name?: string
  level: number
  duration: number
  price: number
}

const loading = ref(false)
const errorMessage = ref('')
const avatarFile = ref<File | null>(null)
const avatarPreview = ref<string>('')
const selectedPackageId = ref<number | null>(null)
const packageList = ref<Package[]>([])

const registerForm = reactive({
  username: '',
  password: '',
  email: ''
})

const handleAvatarChange = (file: any) => {
  if (file.raw) {
    avatarFile.value = file.raw

    // 创建预览
    const reader = new FileReader()
    reader.onload = (e) => {
      avatarPreview.value = e.target?.result as string
    }
    reader.readAsDataURL(file.raw)
  }
}

const selectPackage = (packageId: number) => {
  selectedPackageId.value = packageId
}

const loadPackageList = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/api/order/package/all`)
    const result = await response.json()

    if (result.code === 200 && result.data) {
      packageList.value = result.data
      const firstPackage = packageList.value[0]
      if (firstPackage) {
        selectedPackageId.value = firstPackage.id
      }
    }
  } catch (error) {
    console.error('加载套餐列表失败:', error)
  }
}


const showError = (message: string) => {
  errorMessage.value = message
  ElMessage.error(message)
}

const hideError = () => {
  errorMessage.value = ''
}

const handleRegister = async () => {
  hideError()

  if (!registerForm.username || !registerForm.password || !registerForm.email) {
    showError('请填写所有必填字段')
    return
  }

  if (!avatarFile.value) {
    showError('请上传头像照片')
    return
  }

  if (!selectedPackageId.value) {
    showError('请选择一个套餐')
    return
  }

  loading.value = true

  try {
    const formData = new FormData()
    formData.append('username', registerForm.username)
    formData.append('password', registerForm.password)
    formData.append('email', registerForm.email)
    formData.append('pic', avatarFile.value)
    formData.append('packageId', selectedPackageId.value!.toString())

    const response = await fetch(`${API_BASE_URL}/api/user/register`, {
      method: 'POST',
      body: formData
    })

    const result = await response.json()

    if (result.code === 200) {
      ElMessage.success('注册成功!请登录')
      router.push('/login')
    } else {
      showError(result.msg || '注册失败')
    }
  } catch (error) {
    console.error('注册错误:', error)
    showError('网络错误,请稍后重试')
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}

onMounted(() => {
  loadPackageList()
})
</script>

<style scoped>
.register-page {
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
  overflow-y: auto;
  padding: 20px 0;
}

.register-page::before {
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

.register-container {
  width: 600px;
  max-width: 90%;
  min-width: 450px;
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

.register-header {
  text-align: center;
  margin-bottom: 30px;
}

.register-header h1 {
  color: #ffffff;
  font-size: 36px;
  margin-bottom: 10px;
  font-weight: 600;
}

.register-header p {
  color: #b0b0b0;
  font-size: 18px;
}

.avatar-uploader .avatar-preview {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #667eea;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
  line-height: 100px;
  border: 2px dashed #667eea;
  border-radius: 50%;
}

.package-selection {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 15px;
  max-height: 400px;
  overflow-y: auto;
  width: 100%;
}

.package-card {
  margin: 0 !important;
  padding: 20px;
  border: 2px solid #5a5a5a;
  border-radius: 12px;
  transition: all 0.3s;
  min-height: 180px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  cursor: pointer;
  background: rgba(60, 60, 60, 0.6);
}

.package-card:hover {
  border-color: #667eea;
  background: rgba(80, 80, 80, 0.7);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.package-card.is-checked {
  border-color: #667eea;
  border-width: 3px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.3) 0%, rgba(118, 75, 162, 0.3) 100%);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.4);
  transform: translateY(-3px);
}

.package-card :deep(.el-radio__input) {
  display: none;
}

.package-card :deep(.el-radio__label) {
  width: 100%;
  padding: 0;
}

.package-card :deep(.el-radio__content) {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.package-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
  text-align: center;
  width: 100%;
}

.package-info h4 {
  color: #ffffff;
  margin-bottom: 10px;
  font-size: 18px;
  font-weight: 600;
  transition: color 0.3s;
}

.package-info p {
  color: #b0b0b0;
  font-size: 14px;
  margin: 3px 0;
  line-height: 1.4;
  transition: color 0.3s;
}

/* 选中状态下的文字颜色 */
.package-card.is-checked .package-info h4 {
  color: #ffffff;
}

.package-card.is-checked .package-info p {
  color: #e0e0e0;
}

.package-price {
  font-size: 24px;
  color: #f5576c;
  font-weight: bold;
  margin-top: 10px;
  text-align: center;
  width: 100%;
}

.register-btn {
  width: 100%;
}

.login-link {
  text-align: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e0e0e0;
}
</style>
