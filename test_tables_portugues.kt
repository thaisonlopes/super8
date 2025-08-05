import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*

fun testTablesPortugues() {
    val dbUrl = "jdbc:postgresql://145.223.30.215:5432/bt"
    val dbUser = "postgres"
    val dbPassword = "alt@2024"
    
    println("🔍 Testando tabelas em português brasileiro...")
    println("URL: $dbUrl")
    println("Usuário: $dbUser")
    
    try {
        // Carregar driver PostgreSQL
        Class.forName("org.postgresql.Driver")
        println("✅ Driver PostgreSQL carregado com sucesso")
        
        // Configurações simples
        val props = Properties()
        props.setProperty("user", dbUser)
        props.setProperty("password", dbPassword)
        props.setProperty("ssl", "false")
        props.setProperty("sslmode", "disable")
        props.setProperty("ApplicationName", "Super8PortuguesTest")
        
        // Tentar conexão
        val connection: Connection = DriverManager.getConnection(dbUrl, props)
        println("✅ Conexão estabelecida com sucesso!")
        
        // Testar query simples
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT version()")
        
        if (resultSet.next()) {
            val version = resultSet.getString(1)
            println("📊 Versão do PostgreSQL: $version")
        }
        
        // Verificar se as tabelas em português existem
        val tablesQuery = """
            SELECT table_name 
            FROM information_schema.tables 
            WHERE table_schema = 'public' 
            AND table_name IN ('jogos', 'jogadores', 'partidas', 'jogadores_jogo')
            ORDER BY table_name
        """.trimIndent()
        
        val tablesResult = statement.executeQuery(tablesQuery)
        val existingTables = mutableListOf<String>()
        
        while (tablesResult.next()) {
            existingTables.add(tablesResult.getString("table_name"))
        }
        
        println("📋 Tabelas encontradas: ${existingTables.joinToString(", ")}")
        
        if (existingTables.size == 4) {
            println("✅ Todas as tabelas em português estão criadas!")
            
            // Testar inserção em cada tabela
            println("\n🧪 Testando inserções...")
            
            // Testar inserção na tabela jogos
            try {
                val testJogo = """
                    INSERT INTO jogos (id, codigo_jogo, finalizado) 
                    VALUES ('test-${System.currentTimeMillis()}', 'TEST${System.currentTimeMillis()}', false)
                    ON CONFLICT (id) DO NOTHING
                """.trimIndent()
                
                val insertResult = statement.executeUpdate(testJogo)
                if (insertResult > 0) {
                    println("✅ Inserção na tabela 'jogos' funcionando")
                } else {
                    println("⚠️  Inserção na tabela 'jogos' ignorada (provavelmente conflito)")
                }
            } catch (e: SQLException) {
                println("❌ Erro na inserção na tabela 'jogos': ${e.message}")
            }
            
            // Testar inserção na tabela jogadores
            try {
                val testJogador = """
                    INSERT INTO jogadores (id, nome, pontos_totais, jogos_jogados) 
                    VALUES ('player-${System.currentTimeMillis()}', 'Jogador Teste', 0, 0)
                    ON CONFLICT (id) DO NOTHING
                """.trimIndent()
                
                val insertResult = statement.executeUpdate(testJogador)
                if (insertResult > 0) {
                    println("✅ Inserção na tabela 'jogadores' funcionando")
                } else {
                    println("⚠️  Inserção na tabela 'jogadores' ignorada (provavelmente conflito)")
                }
            } catch (e: SQLException) {
                println("❌ Erro na inserção na tabela 'jogadores': ${e.message}")
            }
            
        } else {
            println("⚠️  Algumas tabelas estão faltando.")
            println("Tabelas necessárias: jogos, jogadores, partidas, jogadores_jogo")
            println("Execute o script create_tables_manual.sql para criar as tabelas:")
            println("psql -h 145.223.30.215 -U postgres -d bt -f create_tables_manual.sql")
        }
        
        // Fechar conexão
        connection.close()
        println("🔒 Conexão fechada")
        
    } catch (e: ClassNotFoundException) {
        println("❌ Erro: Driver PostgreSQL não encontrado")
        e.printStackTrace()
    } catch (e: SQLException) {
        println("❌ Erro de SQL: ${e.message}")
        e.printStackTrace()
    } catch (e: Exception) {
        println("❌ Erro inesperado: ${e.message}")
        e.printStackTrace()
    }
}

fun main() {
    testTablesPortugues()
} 