# Teste da API
Write-Host "Testando API..."

try {
    $response = Invoke-RestMethod -Uri "http://192.168.0.111:3000/api/players" -Method GET
    Write-Host "✅ API funcionando! Jogadores encontrados: $($response.players.Count)"
    $response.players | ForEach-Object { Write-Host "- $($_.nome)" }
} catch {
    Write-Host "❌ Erro na API: $($_.Exception.Message)"
} 