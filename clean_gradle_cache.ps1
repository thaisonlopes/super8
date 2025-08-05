# Script para limpar cache do Gradle e resolver problemas de compatibilidade
# Execute este script no PowerShell como administrador

Write-Host "🧹 Limpando cache do Gradle..." -ForegroundColor Green

# Parar todos os daemons do Gradle
Write-Host "🛑 Parando daemons do Gradle..." -ForegroundColor Yellow
./gradlew --stop

# Limpar cache do Gradle
Write-Host "🗑️  Limpando cache..." -ForegroundColor Yellow
Remove-Item -Path "$env:USERPROFILE\.gradle\caches" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "$env:USERPROFILE\.gradle\daemon" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "$env:USERPROFILE\.gradle\wrapper" -Recurse -Force -ErrorAction SilentlyContinue

# Limpar cache do projeto
Write-Host "🧹 Limpando cache do projeto..." -ForegroundColor Yellow
Remove-Item -Path "build" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "app\build" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path ".gradle" -Recurse -Force -ErrorAction SilentlyContinue

# Baixar dependências novamente
Write-Host "📥 Baixando dependências..." -ForegroundColor Yellow
./gradlew clean

Write-Host "✅ Cache limpo com sucesso!" -ForegroundColor Green
Write-Host "🔄 Agora você pode tentar compilar o projeto novamente." -ForegroundColor Cyan 