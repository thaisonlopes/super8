import java.sql.*

fun main() {
    println("=== TESTE DE CONEXÃO DO APP ===")
    
    try {
        // Usar exatamente a mesma configuração do app
        Class.forName("org.postgresql.Driver")
        val connection = DriverManager.getConnection(
            "jdbc:postgresql://145.223.30.215:5432/bt?ssl=false&sslmode=disable&gssEncMode=disable&kerberosServerName=",
            "postgres",
            "alt@2024"
        )
        
        println("✅ Conectado ao banco!")
        
        // Testar inserção de um jogador
        val testId = java.util.UUID.randomUUID().toString()
        val testName = "TESTE_${System.currentTimeMillis()}"
        
        val sql = "INSERT INTO jogadores (id, nome, data_criacao, data_atualizacao) VALUES (?, ?, ?, ?)"
        val stmt = connection.prepareStatement(sql)
        
        stmt.setString(1, testId)
        stmt.setString(2, testName)
        stmt.setTimestamp(3, java.sql.Timestamp(System.currentTimeMillis()))
        stmt.setTimestamp(4, java.sql.Timestamp(System.currentTimeMillis()))
        
        val result = stmt.executeUpdate()
        
        if (result > 0) {
            println("✅ Jogador de teste inserido com sucesso!")
            println("   ID: $testId")
            println("   Nome: $testName")
            
            // Verificar se foi inserido
            val checkStmt = connection.createStatement()
            val rs = checkStmt.executeQuery("SELECT * FROM jogadores WHERE id = '$testId'")
            
            if (rs.next()) {
                println("✅ Jogador encontrado no banco!")
                println("   Nome: ${rs.getString("nome")}")
                println("   Data: ${rs.getTimestamp("data_criacao")}")
            } else {
                println("❌ Jogador NÃO encontrado no banco!")
            }
            
            rs.close()
            checkStmt.close()
            
            // Limpar o teste
            val deleteStmt = connection.prepareStatement("DELETE FROM jogadores WHERE id = ?")
            deleteStmt.setString(1, testId)
            deleteStmt.executeUpdate()
            println("🧹 Jogador de teste removido")
            deleteStmt.close()
            
        } else {
            println("❌ Falha ao inserir jogador de teste")
        }
        
        stmt.close()
        connection.close()
        
    } catch (e: Exception) {
        println("❌ ERRO: ${e.message}")
        e.printStackTrace()
    }
} 