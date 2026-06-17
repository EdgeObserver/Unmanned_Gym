# ============================================================================
# .env 文件配置验证脚本
# ============================================================================

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "🔍 .env 文件配置验证" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

$envFile = Join-Path $PSScriptRoot ".env"
$envExampleFile = Join-Path $PSScriptRoot ".env.example"

# 检查 .env 文件是否存在
if (-not (Test-Path $envFile)) {
    Write-Host "❌ .env 文件不存在" -ForegroundColor Red
    Write-Host ""
    Write-Host "请执行以下命令创建：" -ForegroundColor Yellow
    Write-Host "  Copy-Item .env.example .env" -ForegroundColor White
    Write-Host ""
    exit 1
} else {
    Write-Host "✅ .env 文件存在" -ForegroundColor Green
}

# 检查 .env 是否在 .gitignore 中
$gitignoreFile = Join-Path $PSScriptRoot ".gitignore"
if (Test-Path $gitignoreFile) {
    $gitignoreContent = Get-Content $gitignoreFile -Raw
    if ($gitignoreContent -match "^\.env$" -or $gitignoreContent -match "^\.env/") {
        Write-Host "✅ .env 已在 .gitignore 中" -ForegroundColor Green
    } else {
        Write-Host "⚠️  .env 未在 .gitignore 中，可能会被提交到 Git" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "📋 检查必需的配置项..." -ForegroundColor Cyan
Write-Host ""

$requiredVars = @(
    "DB_PASSWORD",
    "REDIS_PASSWORD",
    "RABBITMQ_PASSWORD",
    "ALIYUN_ACCESS_KEY_ID",
    "ALIYUN_ACCESS_KEY_SECRET",
    "OSS_ENDPOINT",
    "OSS_BUCKET_NAME"
)

$missingVars = @()
$placeholderVars = @()

$envContent = Get-Content $envFile -Raw

foreach ($var in $requiredVars) {
    # 检查变量是否存在
    if ($envContent -notmatch "$var=") {
        $missingVars += $var
        Write-Host "❌ 缺少: $var" -ForegroundColor Red
    } else {
        # 检查是否还是占位符
        $pattern = "$var=(your_|example|placeholder|changeme)"
        if ($envContent -match $pattern) {
            $placeholderVars += $var
            Write-Host "⚠️  需替换: $var (仍是占位符)" -ForegroundColor Yellow
        } else {
            Write-Host "✅ 已配置: $var" -ForegroundColor Green
        }
    }
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "📊 验证结果汇总" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

if ($missingVars.Count -eq 0 -and $placeholderVars.Count -eq 0) {
    Write-Host "✅ 所有必需配置项已正确设置！" -ForegroundColor Green
    Write-Host ""
    Write-Host "可以启动应用了。" -ForegroundColor Green
} else {
    if ($missingVars.Count -gt 0) {
        Write-Host "❌ 缺少的配置项:" -ForegroundColor Red
        foreach ($var in $missingVars) {
            Write-Host "   - $var" -ForegroundColor Red
        }
        Write-Host ""
    }
    
    if ($placeholderVars.Count -gt 0) {
        Write-Host "⚠️  需要替换的配置项（仍是占位符）:" -ForegroundColor Yellow
        foreach ($var in $placeholderVars) {
            Write-Host "   - $var" -ForegroundColor Yellow
        }
        Write-Host ""
    }
    
    Write-Host "建议操作：" -ForegroundColor Cyan
    Write-Host "1. 打开 .env 文件" -ForegroundColor White
    Write-Host "2. 填写真实的配置信息" -ForegroundColor White
    Write-Host "3. 重新运行此验证脚本" -ForegroundColor White
    Write-Host ""
}

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "💡 提示" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "参考文档：" -ForegroundColor White
Write-Host "- ENV_SETUP_GUIDE.md" -ForegroundColor Cyan
Write-Host "- .env.example" -ForegroundColor Cyan
Write-Host ""
Write-Host "安全提醒：" -ForegroundColor Yellow
Write-Host "- 不要将 .env 文件提交到 Git" -ForegroundColor White
Write-Host "- 定期轮换密钥和密码" -ForegroundColor White
Write-Host "- 使用最小权限原则配置账号" -ForegroundColor White
Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan

# 退出码
if ($missingVars.Count -gt 0 -or $placeholderVars.Count -gt 0) {
    exit 1
} else {
    exit 0
}
