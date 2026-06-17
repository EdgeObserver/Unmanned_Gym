# ============================================================================
# 快速启动脚本 - 自动配置环境变量并启动应用
# ============================================================================

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "🚀 Unmanned Gym - 快速启动" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# 检查 .env 文件是否存在
$envFile = Join-Path $PSScriptRoot ".env"
if (-not (Test-Path $envFile)) {
    Write-Host "⚠️  .env 文件不存在，正在创建..." -ForegroundColor Yellow
    Copy-Item ".env.example" ".env"
    Write-Host "✅ 已创建 .env 文件" -ForegroundColor Green
    Write-Host ""
    Write-Host "请编辑 .env 文件，填写真实的配置信息：" -ForegroundColor Yellow
    Write-Host "  notepad .env" -ForegroundColor White
    Write-Host ""
    Write-Host "填写完成后，重新运行此脚本。" -ForegroundColor Yellow
    exit 1
}

# 验证 .env 配置
Write-Host "📋 验证 .env 配置..." -ForegroundColor Cyan
& "$PSScriptRoot\verify_env.ps1"

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "❌ 配置验证失败，请先修复配置问题" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "✅ 配置验证通过！" -ForegroundColor Green
Write-Host ""

# 加载环境变量
Write-Host "📦 加载环境变量..." -ForegroundColor Cyan
Get-Content $envFile | ForEach-Object {
    if ($_ -match '^([^#][^=]+)=(.*)$') {
        $name = $matches[1].Trim()
        $value = $matches[2].Trim()
        [Environment]::SetEnvironmentVariable($name, $value, "Process")
        Write-Host "  ✓ $name" -ForegroundColor DarkGray
    }
}

Write-Host ""
Write-Host "✅ 环境变量加载完成！" -ForegroundColor Green
Write-Host ""

# 询问要启动的服务
Write-Host "请选择要启动的服务：" -ForegroundColor Cyan
Write-Host "1. FaceService (人脸识别服务)" -ForegroundColor White
Write-Host "2. UserService (用户服务)" -ForegroundColor White
Write-Host "3. ResourceService (资源服务)" -ForegroundColor White
Write-Host "4. 所有服务（不推荐）" -ForegroundColor White
Write-Host ""

$choice = Read-Host "请输入选项 (1-4)"

switch ($choice) {
    "1" {
        Write-Host ""
        Write-Host "🚀 启动 FaceService..." -ForegroundColor Green
        Set-Location "services/FaceService"
        mvn spring-boot:run
    }
    "2" {
        Write-Host ""
        Write-Host "🚀 启动 UserService..." -ForegroundColor Green
        Set-Location "services/UserService"
        mvn spring-boot:run
    }
    "3" {
        Write-Host ""
        Write-Host "🚀 启动 ResourceService..." -ForegroundColor Green
        Set-Location "services/ResourceService"
        mvn spring-boot:run
    }
    "4" {
        Write-Host ""
        Write-Host "⚠️  启动所有服务会消耗大量资源" -ForegroundColor Yellow
        $confirm = Read-Host "确定要继续吗？(y/n)"
        if ($confirm -eq "y") {
            Write-Host "请使用 IDE 或分别启动各个服务" -ForegroundColor Yellow
        }
    }
    default {
        Write-Host "❌ 无效选项" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "💡 提示" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "如需停止服务，按 Ctrl+C" -ForegroundColor White
Write-Host ""
