import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*

fun testPostgres15Connection() {
    val dbUrl = "jdbc:postgresql://145.223.30.215:5432/bt"
    val dbUser = "postgres"
    val dbPassword = "alt@2024"
    
    println("🔍 Testando conexão com PostgreSQL 15...")
    println("URL: $dbUrl")
    println("Usuário: $dbUser")
    
    try {
        // Carregar driver PostgreSQL
        Class.forName("org.postgresql.Driver")
        println("✅ Driver PostgreSQL carregado com sucesso")
        
        // Configurações otimizadas para PostgreSQL 15
        val props = Properties()
        props.setProperty("user", dbUser)
        props.setProperty("password", dbPassword)
        props.setProperty("ssl", "false")
        props.setProperty("sslmode", "disable")
        props.setProperty("tcpKeepAlive", "true")
        props.setProperty("socketTimeout", "30")
        props.setProperty("connectTimeout", "10")
        props.setProperty("ApplicationName", "Super8Test")
        
        // Tentar conexão
        val connection: Connection = DriverManager.getConnection(dbUrl, props)
        println("✅ Conexão estabelecida com sucesso!")
        
        // Testar query simples
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT version()")
        
        if (resultSet.next()) {
            val version = resultSet.getString(1)
            println("📊 Versão do PostgreSQL: $version")
            
            // Verificar se é PostgreSQL 15
            if (version.contains("PostgreSQL 15")) {
                println("✅ PostgreSQL 15 detectado - Perfeito!")
            } else {
                println("⚠️  Versão diferente do PostgreSQL 15 detectada")
            }
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
        
        // Testar recursos específicos do PostgreSQL 15
        println("\n🧪 Testando recursos do PostgreSQL 15...")
        
        // Testar TIMESTAMP WITH TIME ZONE
        try {
            val timezoneTest = statement.executeQuery("SELECT CURRENT_TIMESTAMP")
            if (timezoneTest.next()) {
                println("✅ TIMESTAMP WITH TIME ZONE funcionando")
            }
        } catch (e: SQLException) {
            println("❌ Erro no TIMESTAMP WITH TIME ZONE: ${e.message}")
        }
        
        // Testar BIGSERIAL
        try {
            val serialTest = statement.executeQuery("SELECT nextval('matches_id_seq')")
            if (serialTest.next()) {
                println("✅ BIGSERIAL funcionando")
            }
        } catch (e: SQLException) {
            println("⚠️  Sequência não encontrada (normal se tabelas não existirem)")
        }
        
        // Fechar conexão
        connection.close()
        println("🔒 Conexão fechada")
        
    } catch (e: ClassNotFoundException) {
        println("❌ Erro: Driver PostgreSQL não encontrado")
        println("Certifique-se de que a dependência org.postgresql:postgresql:42.6.0 está no build.gradle")
        e.printStackTrace()
    } catch (e: SQLException) {
        println("❌ Erro de SQL: ${e.message}")
        println("Verifique:")
        println("- Se o servidor PostgreSQL 15 está rodando")
        println("- Se as credenciais estão corretas")
        println("- Se o banco 'bt' existe")
        e.printStackTrace()
    } catch (e: Exception) {
        println("❌ Erro inesperado: ${e.message}")
        e.printStackTrace()
    }
}

fun main() {
    testPostgres15Connection()
} 