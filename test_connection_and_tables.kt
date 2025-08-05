import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

fun main() {
    println("=== TESTE DE CONEXÃO E TABELAS ===")
    
    val DB_URL = "jdbc:postgresql://145.223.30.215:5432/bt"
    val DB_USER = "postgres"
    val DB_PASSWORD = "alt@2024"
    
    try {
        // Testar conexão
        println("1. Testando conexão...")
        Class.forName("org.postgresql.Driver")
        val connection = DriverManager.getConnection(
            "$DB_URL?ssl=false&sslmode=disable&gssEncMode=disable&kerberosServerName=",
            DB_USER,
            DB_PASSWORD
        )
        println("✅ Conexão estabelecida com sucesso!")
        
        // Verificar se as tabelas existem
        println("\n2. Verificando tabelas...")
        val tables = listOf("jogadores", "jogos", "partidas", "pontuacoes_jogo")
        
        for (table in tables) {
            try {
                val stmt = connection.createStatement()
                val rs = stmt.executeQuery("SELECT COUNT(*) FROM $table")
                if (rs.next()) {
                    val count = rs.getInt(1)
                    println("✅ Tabela '$table' existe com $count registros")
                }
                rs.close()
                stmt.close()
            } catch (e: Exception) {
                println("❌ Tabela '$table' NÃO existe: ${e.message}")
            }
        }
        
        // Verificar dados na tabela jogadores
        println("\n3. Verificando dados na tabela jogadores...")
        try {
            val stmt = connection.createStatement()
            val rs = stmt.executeQuery("SELECT id, nome, data_criacao FROM jogadores ORDER BY data_criacao DESC LIMIT 5")
            
            var found = false
            while (rs.next()) {
                found = true
                val id = rs.getString("id")
                val nome = rs.getString("nome")
                val data = rs.getTimestamp("data_criacao")
                println("   - ID: $id, Nome: $nome, Data: $data")
            }
            
            if (!found) {
                println("   📭 Nenhum jogador encontrado na tabela")
            }
            
            rs.close()
            stmt.close()
        } catch (e: Exception) {
            println("❌ Erro ao consultar jogadores: ${e.message}")
        }
        
        connection.close()
        println("\n✅ Teste concluído!")
        
    } catch (e: Exception) {
        println("❌ ERRO: ${e.message}")
        e.printStackTrace()
    }
} 