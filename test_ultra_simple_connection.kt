import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*

fun testUltraSimpleConnection() {
    val dbUrl = "jdbc:postgresql://145.223.30.215:5432/bt"
    val dbUser = "postgres"
    val dbPassword = "alt@2024"
    
    println("🔍 Testando conexão ultra-simples com PostgreSQL...")
    println("URL: $dbUrl")
    println("Usuário: $dbUser")
    
    try {
        // Carregar driver PostgreSQL
        Class.forName("org.postgresql.Driver")
        println("✅ Driver PostgreSQL carregado com sucesso")
        
        // Configurações ultra-simples (igual ao UltraSimplePostgresRepository)
        val props = Properties()
        props.setProperty("user", dbUser)
        props.setProperty("password", dbPassword)
        props.setProperty("ssl", "false")
        props.setProperty("sslmode", "disable")
        props.setProperty("gssEncMode", "disable")  // Desabilitar GSS
        props.setProperty("kerberosServerName", "")  // Desabilitar Kerberos
        props.setProperty("ApplicationName", "Super8UltraSimpleTest")
        
        // Tentar conexão
        val connection: Connection = DriverManager.getConnection(dbUrl, props)
        println("✅ Conexão ultra-simples estabelecida com sucesso!")
        
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
        
        // Testar recursos ultra-simples
        println("\n🧪 Testando recursos ultra-simples...")
        
        // Testar TIMESTAMP simples (sem timezone)
        try {
            val timezoneTest = statement.executeQuery("SELECT CURRENT_TIMESTAMP")
            if (timezoneTest.next()) {
                println("✅ TIMESTAMP simples funcionando")
            }
        } catch (e: SQLException) {
            println("❌ Erro no TIMESTAMP: ${e.message}")
        }
        
        // Testar SERIAL (não BIGSERIAL)
        try {
            val serialTest = statement.executeQuery("SELECT nextval('matches_id_seq')")
            if (serialTest.next()) {
                println("✅ SERIAL funcionando")
            }
        } catch (e: SQLException) {
            println("⚠️  Sequência não encontrada (normal se tabelas não existirem)")
        }
        
        // Testar inserção simples
        try {
            val testInsert = """
                INSERT INTO games (id, game_code, is_finished) 
                VALUES ('test-${System.currentTimeMillis()}', 'TEST${System.currentTimeMillis()}', false)
                ON CONFLICT (id) DO NOTHING
            """.trimIndent()
            
            val insertResult = statement.executeUpdate(testInsert)
            if (insertResult > 0) {
                println("✅ Inserção simples funcionando")
            } else {
                println("⚠️  Inserção ignorada (provavelmente conflito)")
            }
        } catch (e: SQLException) {
            println("❌ Erro na inserção: ${e.message}")
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
    testUltraSimpleConnection()
} 