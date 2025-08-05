import java.sql.*

fun main() {
    println("=== VERIFICANDO TABELAS ===")
    
    try {
        // Conectar ao banco
        Class.forName("org.postgresql.Driver")
        val connection = DriverManager.getConnection(
            "jdbc:postgresql://145.223.30.215:5432/bt?ssl=false&sslmode=disable",
            "postgres",
            "alt@2024"
        )
        
        println("✅ Conectado ao banco!")
        
        // Verificar tabelas
        val tables = listOf("jogadores", "jogos", "partidas", "pontuacoes_jogo")
        
        for (table in tables) {
            try {
                val stmt = connection.createStatement()
                val rs = stmt.executeQuery("SELECT COUNT(*) FROM $table")
                if (rs.next()) {
                    val count = rs.getInt(1)
                    println("✅ $table: $count registros")
                }
                rs.close()
                stmt.close()
            } catch (e: Exception) {
                println("❌ $table: ${e.message}")
            }
        }
        
        connection.close()
        
    } catch (e: Exception) {
        println("❌ ERRO: ${e.message}")
    }
} 