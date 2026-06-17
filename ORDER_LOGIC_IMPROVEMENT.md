# 订单逻辑改进说明

## 改进目标
保留用户的每一个订单记录，同时维护用户的会员结束日期。

## 主要变更

### 1. 数据库变更
- **用户表(user)**: 添加 `membership_end_time` 字段（DATE类型），用于维护用户的会员有效期
- 执行 `database_migration_add_membership_end_time.sql` 脚本进行数据库迁移

### 2. 后端变更

#### 2.1 实体类修改
- **User.java** (`model/src/main/java/pojo/User.java`)
  - 添加字段: `private LocalDate membershipEndTime;`

#### 2.2 OrderService改进
- **新增UserMapper** (`services/OrderService/src/main/java/order/mapper/UserMapper.java`)
  - 用于操作用户数据

- **OrderServiceImpl.java** 核心逻辑修改:
  
  a) **createOrder()** - 创建订单
  - 不再检查是否已有有效订单
  - 每次调用都创建新的订单记录
  - 根据用户当前的membershipEndTime计算新订单的结束时间：
    - 如果用户已有未过期的会员资格，从当前结束时间延续
    - 否则从现在开始计算
  - 创建订单后更新用户的membershipEndTime
  
  b) **update()** - 续约订单
  - 改为创建新订单记录而不是更新现有订单
  - 检查用户是否有有效的会员资格
  - 从当前membershipEndTime延续计算新的结束时间
  - 创建新订单并更新用户的membershipEndTime
  
  c) **getLegalOrderByUid()** - 获取有效订单
  - 基于用户的membershipEndTime判断是否有有效订单
  - 返回用户最新的订单记录
  
  d) **新增getUserOrderHistory()** - 获取订单历史
  - 支持分页查询用户的所有订单记录
  - 用户只能查看自己的订单，管理员可查看所有

#### 2.3 Controller层
- **OrderController.java**
  - 新增接口: `GET /history?uid={uid}&pageNum={pageNum}&pageSize={pageSize}`
  - 用于获取用户的订单历史记录

### 3. 前端变更

#### 3.1 UserDashBoard.vue
- **新增数据字段**:
  - `showOrderHistory`: 控制是否显示订单历史
  - `orderHistory`: 订单历史列表
  - `orderHistoryPageNum`: 当前页码
  - `orderHistoryPageSize`: 每页数量
  - `orderHistoryTotal`: 总记录数

- **新增方法**:
  - `loadOrderHistory()`: 加载订单历史
  - `fetchOrderHistory()`: 从后端获取订单历史数据
  - `prevPage()`: 上一页
  - `nextPage()`: 下一页

- **UI改进**:
  - 订单页面添加操作按钮区域（加入会员、续约、查看订单历史）
  - 订单历史记录展示区，包含分页功能
  - 每个订单显示：订单ID、套餐ID、开始时间、结束时间、创建时间、状态

- **样式优化**:
  - 添加订单操作按钮样式（primary、secondary、info三种类型）
  - 订单历史列表样式
  - 分页控件样式
  - 订单状态标签样式（有效/已过期）

## 业务流程

### 首次购买会员
1. 用户点击"加入会员"
2. 选择套餐
3. 系统创建新订单（order_start_time = 今天，order_end_time = 今天 + 套餐天数）
4. 更新用户的membershipEndTime = order_end_time

### 续约会员
1. 用户点击"续约"
2. 选择套餐
3. 系统创建新订单（order_start_time = 今天，order_end_time = 当前membershipEndTime + 套餐天数）
4. 更新用户的membershipEndTime = 新的order_end_time

### 查看订单历史
1. 用户点击"查看订单历史"
2. 系统分页加载该用户的所有订单记录
3. 用户可以浏览所有历史订单

## 优势
1. **完整的订单历史**: 每次购买/续约都保留独立的订单记录
2. **清晰的有效期管理**: 通过用户的membershipEndTime统一管理会员有效期
3. **灵活的时间计算**: 续约时自动从当前有效期延续，避免时间重叠或断层
4. **更好的用户体验**: 用户可以查看完整的消费历史

## 注意事项
1. 执行数据库迁移脚本前请备份数据
2. 迁移脚本会自动为有有效订单的用户初始化membershipEndTime
3. 新用户首次购买时，membershipEndTime为NULL，系统会从头开始计算
4. 订单的有效性判断基于用户的membershipEndTime，而非单个订单的orderEndTime
