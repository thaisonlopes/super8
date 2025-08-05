# Super8 - Sistema com PostgreSQL (Versão Simplificada)

## 📋 Visão Geral

Sistema de torneio Super8 com persistência em banco de dados PostgreSQL 15. **Versão simplificada** sem KSP para evitar conflitos de compatibilidade.

## 🗄️ Estrutura do Banco de Dados

### Tabelas Necessárias

- **`jogos`** - Informações dos jogos/torneios
- **`jogadores`** - Dados dos jogadores
- **`partidas`** - Partidas sorteadas
- **`jogadores_jogo`** - Relacionamento entre jogos e jogadores

### Configurações de Compatibilidade

- **minSdk = 26** (Android O) - Para suportar MethodHandle.invoke
- **Driver PostgreSQL 42.6.0** - Versão otimizada para PostgreSQL 15
- **Sem KSP** - Evita conflitos de compatibilidade
- **Configurações ultra-simples** - Evita recursos problemáticos

## 🚀 Como Configurar

### 1. Criar Tabelas Manualmente

Execute o script SQL para criar as tabelas:

```bash
# Conectar ao PostgreSQL
psql -h 145.223.30.215 -U postgres -d bt

# Executar o script
\i create_tables_manual.sql
```

Ou execute diretamente:
```bash
psql -h 145.223.30.215 -U postgres -d bt -f create_tables_manual.sql
```

### 2. Testar Conexão

Execute o script de teste:
```bash
kotlin test_simple_connection.kt
```

### 3. Compilar o Projeto

```bash
./gradlew clean
./gradlew build
```

## ✅ Funcionalidades Implementadas

- ✅ **Código da partida** - Geração e salvamento automático
- ✅ **Cadastro de jogador** - Salva no banco PostgreSQL
- ✅ **Partidas sorteadas** - Persistência completa
- ✅ **Resultado de cada rodada** - Salvamento automático
- ✅ **Ranqueamento da partida** - Com edição de pontuação
- ✅ **Ranking geral** - Soma de todos os pontos
- ✅ **Histórico de jogos** - Consulta por código

## 🏗️ Arquitetura Simplificada

### Camadas do Sistema

1. **UI (Compose)** - Interface do usuário
2. **ViewModel** - Lógica de negócio e estado
3. **Repository** - Abstração de dados
4. **PostgreSQL** - Persistência remota

### Repositório

- **UltraSimplePostgresRepository** - Versão ultra-simplificada
- **Sem Room/Hilt** - Evita conflitos de KSP
- **JDBC direto** - Conexão simples e estável
- **Entidades simples** - Data classes sem anotações

## 🎮 Fluxo do Jogo

1. **Criar Jogo** → Gera código único e salva no banco
2. **Adicionar Jogadores** → Cadastra participantes no PostgreSQL
3. **Sortear Partidas** → Cria rodadas e salva no banco
4. **Registrar Resultados** → Atualiza pontuações em tempo real
5. **Finalizar Torneio** → Salva ranking final
6. **Consultar Histórico** → Busca jogos anteriores

## ✅ Vantagens da Solução Simplificada

- **Sem Conflitos KSP** - Compilação estável
- **Persistência Remota** - Dados seguros no PostgreSQL
- **Compatibilidade** - Funciona em Android 8.0+
- **Funcionalidades Completas** - Todas as features originais mantidas
- **Performance** - Conexão direta com banco
- **Simplicidade** - Menos dependências

## 🔒 Segurança e Logging

- **Logs Detalhados** - Rastreamento completo de operações
- **Tratamento de Erros** - Recuperação automática de falhas
- **Conexões Seguras** - Timeout e retry automático
- **Validação de Dados** - Verificação de integridade

## 🧪 Testes

### Scripts de Teste Disponíveis

1. **test_simple_connection.kt** - Testa conexão básica
2. **create_tables_manual.sql** - Cria tabelas manualmente

### Como Executar Testes

```bash
# Teste de conexão
kotlin test_simple_connection.kt

# Criar tabelas
psql -h 145.223.30.215 -U postgres -d bt -f create_tables_manual.sql
```

## 📱 Compatibilidade

- **Android 8.0+** (API 26+)
- **PostgreSQL 15** (recomendado)
- **Driver JDBC 42.6.0**
- **Sem KSP** - Evita conflitos

## 🆘 Solução de Problemas

### Erro MethodHandle.invoke
- ✅ **Solução**: minSdk = 26 e UltraSimplePostgresRepository
- ✅ **Configurações**: GSS e Kerberos desabilitados

### Erro de Conexão
- Verificar se PostgreSQL está rodando
- Confirmar credenciais corretas
- Verificar se banco 'bt' existe

### Erro de Tabelas
- Execute o script `create_tables_manual.sql`
- Verifique se as 4 tabelas foram criadas: `jogos`, `jogadores`, `partidas`, `jogadores_jogo`

### Erro de Gradle/KSP
- ✅ **Solução**: Removido KSP completamente
- ✅ **Dependências simplificadas**
- ✅ **Sem Room/Hilt**

## 📋 Checklist de Configuração

- [ ] Executar `create_tables_manual.sql`
- [ ] Testar conexão com `test_simple_connection.kt`
- [ ] Compilar projeto com `./gradlew build`
- [ ] Instalar app no dispositivo
- [ ] Testar funcionalidades

---

**🎯 Sistema simplificado e pronto para uso com PostgreSQL 15!** 