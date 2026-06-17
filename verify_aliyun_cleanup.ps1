# ============================================================================
# 阿里云敏感信息清理验证脚本
# 用途: 验证所有阿里云敏感信息已被替换为占位符
# ============================================================================

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "🔍 阿里云敏感信息清理验证" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

$passed = 0
$failed = 0

function Test-Pattern {
    param(
        [string]$Pattern,
        [string]$Description,
        [string[]]$ExcludePatterns = @()
    )
    
    Write-Host -NoNewline "检查 $Description... "
    
    $results = Get-ChildItem -Recurse -Include *.yml,*.yaml,*.properties,*.java,*.xml,*.md |
               Where-Object { 
                   $_.FullName -notmatch '\\target\\' -and 
                   $_.FullName -notmatch '\\node_modules\\' -and 
                   $_.FullName -notmatch '\\.git\\' -and
                   $_.Name -notmatch 'ALIYUN_SENSITIVE_DATA_CLEANUP_REPORT.md'
               } |
               Select-String -Pattern $Pattern
    
    # 排除占位符
    if ($ExcludePatterns.Count -gt 0) {
        $results = $results | Where-Object {
            $line = $_.Line
            $exclude = $false
            foreach ($pattern in $ExcludePatterns) {
                if ($line -match $pattern) {
                    $exclude = $true
                    break
                }
            }
            -not $exclude
        }
    }
    
    if ($null -eq $results) {
        Write-Host "✅ 通过" -ForegroundColor Green
        $passed++
    } else {
        Write-Host "❌ 失败" -ForegroundColor Red
        $failed++
        Write-Host "  发现以下泄露:" -ForegroundColor Yellow
        $results | Select-Object -First 5 | ForEach-Object {
            Write-Host "  - $($_.Path):$($_.LineNumber)" -ForegroundColor Gray
            Write-Host "    $($_.Line.Trim())" -ForegroundColor DarkGray
        }
    }
    Write-Host ""
}

Write-Host "📋 开始验证..." -ForegroundColor Cyan
Write-Host ""

# 检查真实的 AccessKey ID (LTAI 开头)
Test-Pattern -Pattern "LTAI[A-Za-z0-9]{20}" -Description "真实的 AccessKey ID" -ExcludePatterns @("YOUR_ALIYUN_ACCESS_KEY_ID")

# 检查真实的 OSS Endpoint
Test-Pattern -Pattern "oss-cn-[a-z]+\.aliyuncs\.com" -Description "真实的 OSS Endpoint" -ExcludePatterns @("YOUR_OSS_ENDPOINT", "oss-cn-beijing.aliyuncs.com.*示例", "华北2.*北京.*oss-cn-beijing")

# 检查真实的 Bucket 名称
Test-Pattern -Pattern "edge-observer" -Description "真实的 Bucket 名称 (edge-observer)" -ExcludePatterns @("YOUR_OSS_BUCKET_NAME")

# 检查其他常见的 Bucket 命名模式（可能是真实的）
Test-Pattern -Pattern 'bucket:\s+[a-z][a-z0-9-]{2,62}[a-z0-9]' -Description "可能的真实 Bucket 名称" -ExcludePatterns @("YOUR_OSS_BUCKET_NAME", "your-bucket", "bucket-name")

# 检查长字符串（可能是 Secret Key）
Test-Pattern -Pattern 'secret-key:\s+[A-Za-z0-9/+=]{30,}' -Description "可能的真实 Secret Key" -ExcludePatterns @("YOUR_ALIYUN_ACCESS_KEY_SECRET")

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "📊 验证结果汇总" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

if ($failed -eq 0) {
    Write-Host "✅ 恭喜！所有检查通过" -ForegroundColor Green
    Write-Host ""
    Write-Host "已通过 $passed 项检查，未发现阿里云敏感信息泄露。" -ForegroundColor Green
} else {
    Write-Host "❌ 发现 $failed 个问题" -ForegroundColor Red
    Write-Host "✅ 通过 $passed 项检查" -ForegroundColor Green
    Write-Host ""
    Write-Host "建议操作：" -ForegroundColor Yellow
    Write-Host "1. 立即修复上述发现的问题"
    Write-Host "2. 重新运行此验证脚本"
    Write-Host "3. 如果问题已修复但仍报错，检查是否需要更新排除规则"
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "💡 配置提示" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "如需配置真实的阿里云信息，请参考：" -ForegroundColor White
Write-Host "- ALIYUN_OSS_CONFIG_TEMPLATE.md" -ForegroundColor Cyan
Write-Host "- ALIYUN_SENSITIVE_DATA_CLEANUP_REPORT.md" -ForegroundColor Cyan
Write-Host ""
Write-Host "推荐使用环境变量或 Nacos 配置中心管理敏感信息。" -ForegroundColor White
Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan

# 退出码
if ($failed -gt 0) {
    exit 1
} else {
    exit 0
}
