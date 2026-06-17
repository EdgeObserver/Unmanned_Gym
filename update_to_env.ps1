# ============================================================================
# 批量更新 application.yml 为环境变量方式
# ============================================================================

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "🔧 批量更新配置文件为环境变量方式" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

$services = @(
    "services/UserService/src/main/resources/application.yml",
    "services/ResourceService/src/main/resources/application.yml",
    "services/RecordService/src/main/resources/application.yml",
    "services/BookService/src/main/resources/application.yml",
    "services/OrderService/src/main/resources/application.yml",
    "services/CountService/src/main/resources/application.yml",
    "services/KnowledgeService/src/main/resources/application.yml",
    "src/main/resources/application.yml"
)

foreach ($servicePath in $services) {
    $fullPath = Join-Path $PSScriptRoot $servicePath
    
    if (Test-Path $fullPath) {
        Write-Host "处理: $servicePath" -ForegroundColor Yellow
        
        $content = Get-Content $fullPath -Raw -Encoding UTF8
        
        # 替换数据库配置
        $content = $content -replace 'url: jdbc:mysql://localhost:3306/(\w+)', 'url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:$1}'
        $content = $content -replace 'username: root', 'username: ${DB_USERNAME:root}'
        $content = $content -replace 'password: 4114', 'password: ${DB_PASSWORD}'
        
        # 替换 Redis 配置
        $content = $content -replace 'host: localhost\s+port: 6379', 'host: ${REDIS_HOST:localhost}`n      port: ${REDIS_PORT:6379}'
        
        # 替换 RabbitMQ 配置
        $content = $content -replace 'username: guest\s+password: guest', 'username: ${RABBITMQ_USERNAME:guest}`n    password: ${RABBITMQ_PASSWORD:guest}'
        $content = $content -replace 'password: 414406', 'password: ${RABBITMQ_PASSWORD}'
        
        # 替换阿里云 OSS 配置
        $content = $content -replace 'endpoint:\s+YOUR_OSS_ENDPOINT', 'endpoint:   ${OSS_ENDPOINT}'
        $content = $content -replace 'bucket:\s+YOUR_OSS_BUCKET_NAME', 'bucket:     ${OSS_BUCKET_NAME}'
        $content = $content -replace 'access-key:\s+YOUR_ALIYUN_ACCESS_KEY_ID', 'access-key: ${ALIYUN_ACCESS_KEY_ID}'
        $content = $content -replace 'secret-key:\s+YOUR_ALIYUN_ACCESS_KEY_SECRET', 'secret-key: ${ALIYUN_ACCESS_KEY_SECRET}'
        
        Set-Content -Path $fullPath -Value $content -Encoding UTF8 -NoNewline
        
        Write-Host "  ✅ 完成" -ForegroundColor Green
    } else {
        Write-Host "  ⚠️  文件不存在，跳过" -ForegroundColor DarkYellow
    }
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "✅ 所有配置文件更新完成！" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "下一步操作：" -ForegroundColor Yellow
Write-Host "1. 复制 .env.example 为 .env" -ForegroundColor White
Write-Host "2. 填写真实的配置信息" -ForegroundColor White
Write-Host "3. 重启应用使配置生效" -ForegroundColor White
Write-Host ""
