import java.sql.*

fun main() {
    println("=== TESTE SIMPLES DE CONEXÃO ===")
    
    try {
        // Testar diferentes configurações de conexão
        val configs = listOf(
            "jdbc:postgresql://145.223.30.215:5432/bt?ssl=false&sslmode=disable",
            "jdbc:postgresql://145.223.30.215:5432/bt?ssl=false&sslmode=disable&gssEncMode=disable",
            "jdbc:postgresql://145.223.30.215:5432/bt?ssl=false&sslmode=disable&gssEncMode=disable&kerberosServerName=",
            "jdbc:postgresql://145.223.30.215:5432/bt"
        )
        
        for ((index, url) in configs.withIndex()) {
            println("\n--- Teste ${index + 1}: $url ---")
            try {
                Class.forName("org.postgresql.Driver")
                val connection = DriverManager.getConnection(url, "postgres", "alt@2024")
                println("✅ CONEXÃO FUNCIONOU!")
                connection.close()
                break
            } catch (e: Exception) {
                println("❌ Falhou: ${e.message}")
            }
        }
        
    } catch (e: Exception) {
        println("❌ ERRO GERAL: ${e.message}")
        e.printStackTrace()
    }
} 