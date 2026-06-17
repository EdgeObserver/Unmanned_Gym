#!/bin/bash
# ============================================================================
# 敏感信息检查脚本
# 用途: 扫描项目中可能泄露的敏感信息
# ============================================================================

echo "=========================================="
echo "🔍 敏感信息安全检查"
echo "=========================================="
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查结果计数
WARNINGS=0
CRITICAL=0

# 检查函数
check_pattern() {
    local pattern=$1
    local description=$2
    local severity=$3
    
    echo -n "检查 $description... "
    
    results=$(grep -r --include="*.yml" --include="*.yaml" --include="*.properties" --include="*.java" \
              --exclude-dir=target \
              --exclude-dir=node_modules \
              -E "$pattern" . 2>/dev/null | grep -v "YOUR_" | grep -v ".git")
    
    if [ -z "$results" ]; then
        echo -e "${GREEN}✅ 通过${NC}"
    else
        if [ "$severity" = "CRITICAL" ]; then
            echo -e "${RED}❌ 发现严重问题${NC}"
            CRITICAL=$((CRITICAL + 1))
            echo "$results" | head -5
        else
            echo -e "${YELLOW}⚠️  警告${NC}"
            WARNINGS=$((WARNINGS + 1))
            echo "$results" | head -5
        fi
    fi
    echo ""
}

echo "📋 开始扫描..."
echo ""

# 检查阿里云 AccessKey
check_pattern "LTAI[A-Za-z0-9]{20}" "阿里云 AccessKey ID" "CRITICAL"
check_pattern "(access-key|accessKey).*:.*[A-Za-z0-9]{30,}" "AccessKey 配置" "CRITICAL"

# 检查密码
check_pattern "password:.*[^{]$" "明文密码" "WARNING"
check_pattern "secret-key:.*[^{]$" "Secret Key" "CRITICAL"

# 检查数据库连接字符串中的密码
check_pattern "jdbc:.*:password=[^$]" "JDBC URL 中的密码" "WARNING"

# 检查 API Key
check_pattern "(api.key|apiKey|api_key).*:.*[A-Za-z0-9]{20,}" "API Key" "WARNING"

# 检查 Token
check_pattern "(token|TOKEN).*:.*eyJ[A-Za-z0-9]" "JWT Token" "WARNING"

# 检查私钥
check_pattern "-----BEGIN (RSA |EC |DSA )?PRIVATE KEY-----" "私钥文件" "CRITICAL"

# 检查邮箱和密码组合
check_pattern "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}:.{6,}" "邮箱密码组合" "WARNING"

echo "=========================================="
echo "📊 检查结果汇总"
echo "=========================================="
echo ""

if [ $CRITICAL -eq 0 ] && [ $WARNINGS -eq 0 ]; then
    echo -e "${GREEN}✅ 恭喜！未发现敏感信息泄露${NC}"
else
    if [ $CRITICAL -gt 0 ]; then
        echo -e "${RED}❌ 发现 $CRITICAL 个严重问题${NC}"
    fi
    if [ $WARNINGS -gt 0 ]; then
        echo -e "${YELLOW}⚠️  发现 $WARNINGS 个警告${NC}"
    fi
    echo ""
    echo "建议操作："
    echo "1. 立即替换所有硬编码的密钥为占位符"
    echo "2. 使用环境变量或配置中心管理敏感信息"
    echo "3. 将包含敏感信息的文件添加到 .gitignore"
    echo "4. 如果已提交到 Git，请重写历史移除敏感信息"
fi

echo ""
echo "=========================================="
echo "💡 安全建议"
echo "=========================================="
echo ""
echo "1. 使用环境变量:"
echo "   export ALIYUN_ACCESS_KEY_ID='your-key'"
echo ""
echo "2. 使用 Nacos 配置中心管理敏感配置"
echo ""
echo "3. 定期轮换 AccessKey 和密码"
echo ""
echo "4. 遵循最小权限原则分配 RAM 权限"
echo ""
echo "5. 启用操作审计和监控告警"
echo ""
echo "=========================================="

# 退出码
if [ $CRITICAL -gt 0 ]; then
    exit 1
else
    exit 0
fi
