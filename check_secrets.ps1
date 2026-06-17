# ============================================================================
# 敏感信息检查脚本 (PowerShell 版本)
# 用途: 扫描项目中可能泄露的敏感信息
# ============================================================================

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "🔍 敏感信息安全检查" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# 检查结果计数
$warnings = 0
$critical = 0

# 检查函数
function Check-Pattern {
    param(
        [string]$Pattern,
        [string]$Description,
        [string]$Severity
    )
    
    Write-Host -NoNewline "检查 $Description... "
    
    $results = Get-ChildItem -Recurse -Include *.yml,*.yaml,*.properties,*.java,*.xml |
               Where-Object { $_.FullName -notmatch '\\target\\' -and $_.FullName -notmatch '\\node_modules\\' -and $_.FullName -notmatch '\\.git\\' } |
               Select-String -Pattern $Pattern |
               Where-Object { $_.Line -notmatch 'YOUR_' }
    
    if ($null -eq $results) {
        Write-Host "✅ 通过" -ForegroundColor Green
    } else {
        if ($Severity -eq "CRITICAL") {
            Write-Host "❌ 发现严重问题" -ForegroundColor Red
            $critical++
            $results | Select-Object -First 5 | ForEach-Object {
                Write-Host "  $($_.Path):$($_.LineNumber)" -ForegroundColor Yellow
                Write-Host "  $($_.Line.Trim())" -ForegroundColor Gray
            }
        } else {
            Write-Host "⚠️  警告" -ForegroundColor Yellow
            $warnings++
            $results | Select-Object -First 5 | ForEach-Object {
                Write-Host "  $($_.Path):$($_.LineNumber)" -ForegroundColor Yellow
                Write-Host "  $($_.Line.Trim())" -ForegroundColor Gray
            }
        }
    }
    Write-Host ""
}

Write-Host "📋 开始扫描..." -ForegroundColor Cyan
Write-Host ""

# 检查阿里云 AccessKey
Check-Pattern -Pattern "LTAI[A-Za-z0-9]{20}" -Description "阿里云 AccessKey ID" -Severity "CRITICAL"
Check-Pattern -Pattern "(access-key|accessKey).*:.*[A-Za-z0-9]{30,}" -Description "AccessKey 配置" -Severity "CRITICAL"

# 检查密码
Check-Pattern -Pattern "password:.*[^{]$" -Description "明文密码" -Severity "WARNING"
Check-Pattern -Pattern "secret-key:.*[^{]$" -Description "Secret Key" -Severity "CRITICAL"

# 检查数据库连接字符串中的密码
Check-Pattern -Pattern "jdbc:.*:password=[^$]" -Description "JDBC URL 中的密码" -Severity "WARNING"

# 检查 API Key
Check-Pattern -Pattern "(api.key|apiKey|api_key).*:.*[A-Za-z0-9]{20,}" -Description "API Key" -Severity "WARNING"

# 检查 Token
Check-Pattern -Pattern "(token|TOKEN).*:.*eyJ[A-Za-z0-9]" -Description "JWT Token" -Severity "WARNING"

# 检查私钥
Check-Pattern -Pattern "-----BEGIN (RSA |EC |DSA )?PRIVATE KEY-----" -Description "私钥文件" -Severity "CRITICAL"

# 检查邮箱和密码组合
Check-Pattern -Pattern "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}:.{6,}" -Description "邮箱密码组合" -Severity "WARNING"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "📊 检查结果汇总" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

if ($critical -eq 0 -and $warnings -eq 0) {
    Write-Host "✅ 恭喜！未发现敏感信息泄露" -ForegroundColor Green
} else {
    if ($critical -gt 0) {
        Write-Host "❌ 发现 $critical 个严重问题" -ForegroundColor Red
    }
    if ($warnings -gt 0) {
        Write-Host "⚠️  发现 $warnings 个警告" -ForegroundColor Yellow
    }
    Write-Host ""
    Write-Host "建议操作：" -ForegroundColor Cyan
    Write-Host "1. 立即替换所有硬编码的密钥为占位符"
    Write-Host "2. 使用环境变量或配置中心管理敏感信息"
    Write-Host "3. 将包含敏感信息的文件添加到 .gitignore"
    Write-Host "4. 如果已提交到 Git，请重写历史移除敏感信息"
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "💡 安全建议" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. 使用环境变量:" -ForegroundColor White
Write-Host "   `$env:ALIYUN_ACCESS_KEY_ID='your-key'" -ForegroundColor Gray
Write-Host ""
Write-Host "2. 使用 Nacos 配置中心管理敏感配置" -ForegroundColor White
Write-Host ""
Write-Host "3. 定期轮换 AccessKey 和密码" -ForegroundColor White
Write-Host ""
Write-Host "4. 遵循最小权限原则分配 RAM 权限" -ForegroundColor White
Write-Host ""
Write-Host "5. 启用操作审计和监控告警" -ForegroundColor White
Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan

# 退出码
if ($critical -gt 0) {
    exit 1
} else {
    exit 0
}
