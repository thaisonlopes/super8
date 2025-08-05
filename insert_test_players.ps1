# Script para inserir jogadores de teste
$players = @("João", "Maria", "Pedro", "Ana", "Carlos", "Lucia", "Roberto", "Fernanda")

foreach ($player in $players) {
    $body = @{
        nome = $player
    } | ConvertTo-Json

    try {
        $response = Invoke-RestMethod -Uri "http://192.168.0.111:3000/api/players" -Method POST -Body $body -ContentType "application/json"
        Write-Host "✅ Jogador $player inserido com sucesso: $($response.player.id)"
    } catch {
        Write-Host "❌ Erro ao inserir jogador $player : $($_.Exception.Message)"
    }
}

# Listar todos os jogadores
try {
    $response = Invoke-RestMethod -Uri "http://192.168.0.111:3000/api/players" -Method GET
    Write-Host "📋 Jogadores cadastrados:"
    foreach ($player in $response.players) {
        Write-Host "  - $($player.nome) (ID: $($player.id))"
    }
} catch {
    Write-Host "❌ Erro ao listar jogadores: $($_.Exception.Message)"
} 