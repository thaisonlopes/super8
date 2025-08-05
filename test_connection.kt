import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

fun testPostgresConnection() {
    val dbUrl = "jdbc:postgresql://145.223.30.215:5432/bt"
    val dbUser = "postgres"
    val dbPassword = "alt@2024"
    
    println("🔍 Testando conexão com PostgreSQL...")
    println("URL: $dbUrl")
    println("Usuário: $dbUser")
    
    try {
        // Carregar driver PostgreSQL
        Class.forName("org.postgresql.Driver")
        println("✅ Driver PostgreSQL carregado com sucesso")
        
        // Tentar conexão
        val connection: Connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        println("✅ Conexão estabelecida com sucesso!")
        
        // Testar query simples
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT version()")
        
        if (resultSet.next()) {
            val version = resultSet.getString(1)
            println("📊 Versão do PostgreSQL: $version")
        }
        
        // Verificar se as tabelas existem
        val tablesQuery = """
            SELECT table_name 
            FROM information_schema.tables 
            WHERE table_schema = 'public' 
            AND table_name IN ('games', 'players', 'matches', 'game_players')
            ORDER BY table_name
        """.trimIndent()
        
        val tablesResult = statement.executeQuery(tablesQuery)
        val existingTables = mutableListOf<String>()
        
        while (tablesResult.next()) {
            existingTables.add(tablesResult.getString("table_name"))
        }
        
        println("📋 Tabelas encontradas: ${existingTables.joinToString(", ")}")
        
        if (existingTables.size == 4) {
            println("✅ Todas as tabelas necessárias estão criadas!")
        } else {
            println("⚠️  Algumas tabelas estão faltando. Execute o script database_schema.sql")
            println("Tabelas necessárias: games, players, matches, game_players")
        }
        
        // Fechar conexão
        connection.close()
        println("🔒 Conexão fechada")
        
    } catch (e: ClassNotFoundException) {
        println("❌ Erro: Driver PostgreSQL não encontrado")
        println("Certifique-se de que a dependência org.postgresql:postgresql está no build.gradle")
        e.printStackTrace()
    } catch (e: SQLException) {
        println("❌ Erro de SQL: ${e.message}")
        println("Verifique:")
        println("- Se o servidor PostgreSQL está rodando")
        println("- Se as credenciais estão corretas")
        println("- Se o banco 'bt' existe")
        e.printStackTrace()
    } catch (e: Exception) {
        println("❌ Erro inesperado: ${e.message}")
        e.printStackTrace()
    }
}

fun main() {
    testPostgresConnection()
} 