# 🚀 快速启动指南 - Element Plus 重构版

## 前置要求

- Node.js >= 16 (推荐 v20.19.0 或更高)
- npm >= 8

## 安装依赖

```bash
cd D:\IdeaProjects\AttendanceSystem\front\view
npm install
```

## 启动开发服务器

```bash
npm run dev
```

启动成功后，访问以下地址：

| 页面 | URL | 说明 |
|------|-----|------|
| 登录页 | http://localhost:5173/login | ✅ 已重构 |
| 注册页 | http://localhost:5173/register | ✅ 已重构 |
| 用户仪表板 | http://localhost:5173/dashboard | ✅ 已重构 |
| 管理员仪表板 | http://localhost:5173/admin | ✅ 已重构 |
| 续费页 | http://localhost:5173/renew | ✅ 已重构 |

## 测试账号

### 普通用户
- 用户名: `testuser`
- 密码: `123456`

### 管理员
- 用户名: `admin`
- 密码: `admin123`

> 注意：具体账号信息请咨询后端开发人员或查看数据库

## 主要改进点

### 1. 登录页面 (LoginView.vue)
- ✨ 使用 el-card 卡片布局
- ✨ el-radio-button 角色切换
- ✨ el-input 带图标和密码显示
- ✨ el-dialog 订单逾期提示
- ✨ ElMessage 错误提示

### 2. 注册页面 (RegisterView.vue)
- ✨ el-upload 头像上传预览
- ✨ el-radio-group 套餐选择
- ✨ el-form 表单验证
- ✨ 响应式布局

### 3. 用户仪表板 (UserDashBoard.vue)
- ✨ el-container 专业后台布局
- ✨ el-menu 侧边栏导航
- ✨ el-table 数据表格
- ✨ el-tabs 标签页切换
- ✨ el-dialog 各种弹窗
- ✨ el-pagination 分页
- ✨ 完整的预约、订单、AI助手功能

### 4. 管理员仪表板 (AdminDashBoard.vue)
- ✨ el-container 专业后台布局
- ✨ el-menu 侧边栏导航
- ✨ el-table 数据表格（7个管理模块）
- ✨ el-tabs 资源管理和预约记录标签页
- ✨ el-dialog 添加/编辑弹窗
- ✨ el-pagination 分页组件
- ✨ el-tag 状态标签
- ✨ el-alert 警告提示
- ✨ WebSocket 实时人数监控

### 5. 续费页面 (RenewView.vue)
- ✨ el-card 卡片容器
- ✨ el-radio-group 套餐选择
- ✨ el-alert 错误提示
- ✨ el-button 加载状态

## 常见问题

### Q1: 页面样式错乱？
**A**: 确保已正确安装 Element Plus：
```bash
npm install element-plus
```

### Q2: 图标不显示？
**A**: 确保已安装图标库：
```bash
npm install @element-plus/icons-vue
```

### Q3: 中文不生效？
**A**: 检查 main.ts 中是否正确配置：
```typescript
import zhCn from 'element-plus/es/locale/lang/zh-cn'
app.use(ElementPlus, { locale: zhCn })
```

### Q4: API 请求失败？
**A**: 确保后端服务已启动，默认地址为 `http://localhost:80`

### Q5: 如何修改主题色？
**A**: 在 `src/assets/main.css` 中添加：
```css
:root {
  --el-color-primary: #667eea;
}
```

## 开发技巧

### 1. 查看组件文档
访问 [Element Plus 官方文档](https://element-plus.org/zh-CN/) 查看所有组件的用法。

### 2. 调试技巧
打开浏览器开发者工具，可以：
- 查看 Element Plus 组件的结构
- 调试 CSS 样式
- 监控网络请求

### 3. 热更新
修改代码后会自动刷新浏览器，无需手动重启。

### 4. 构建生产版本
```bash
npm run build
```

## 下一步计划

所有核心页面已完成重构！🎊

### 后续优化建议

1. **添加更多交互细节**
   - 骨架屏加载
   - 更完善的表单验证
   - 更友好的错误处理

3. **性能优化**
   - 按需引入组件
   - 路由懒加载
   - 图片懒加载

## 获取帮助

- 📖 [Element Plus 文档](https://element-plus.org/zh-CN/)
- 📖 [Vue 3 文档](https://cn.vuejs.org/)
- 📖 [重构指南](./ELEMENT_PLUS_REFACTORING_GUIDE.md)
- 📖 [重构总结](./REFACTORING_SUMMARY.md)

---

**祝开发愉快！** 🎉
