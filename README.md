# 🏖️ Super 8 Beach Tennis

Aplicativo Android para gerenciamento de torneios de Beach Tennis com 8 jogadores.

## 🚀 Funcionalidades

- ✅ **Tela Inicial** - Menu principal com estatísticas
- ✅ **Código do Jogo** - Criar/entrar em jogos
- ✅ **Cadastro de Jogadores** - 8 jogadores com seleção de salvos
- ✅ **Tela de Jogo** - Inserção de placares e navegação entre rodadas
- ✅ **Design Moderno** - Interface Material 3 com gradientes
- ✅ **Navegação Fluida** - Transições suaves entre telas

## 🛠️ Tecnologias

- **Kotlin** + **Jetpack Compose**
- **Material 3** Design System
- **Navigation Compose**
- **ViewModel** + **StateFlow**
- **Coroutines**
- **Java 17** (Zulu 17)

## 📱 Como Usar

1. **Novo Jogo** - Clique no botão principal
2. **Código** - Digite um código ou deixe gerar automaticamente
3. **Jogadores** - Preencha os 8 jogadores (ou selecione salvos)
4. **Jogo** - Insira os placares das rodadas
5. **Finalizar** - Complete o torneio e veja o vencedor

## 🔧 Desenvolvimento

### Pré-requisitos
- Android Studio Hedgehog ou superior
- JDK 17 (Zulu 17)
- Android SDK 36

### Build
```bash
# Build normal
./gradlew assembleDebug

# Clean (se houver problemas)
./gradlew clean --no-daemon

# Ou use o script
clean.bat
```

### Problemas Comuns

#### Clean não funciona
Se o `./gradlew clean` falhar no Windows:

1. **Use o script:** `clean.bat`
2. **Ou execute manualmente:**
   ```bash
   ./gradlew --stop
   ./gradlew clean --no-daemon
   ```

#### Botões não funcionam
- Os botões foram convertidos de Cards para Buttons
- Todos os cliques têm logs de debug
- Verifique os logs com: `adb logcat | grep -E "(HomeScreen|GameCodeScreen)"`

## 🎨 Design

- **Cores:** Verde praia, azul oceano, dourado
- **Gradientes:** Suaves e modernos
- **Cards:** Elevação e sombras
- **Ícones:** Material Design 3
- **Tipografia:** Roboto com pesos variados

## 📊 Estrutura do Projeto

```
app/src/main/java/com/beach/super8/
├── model/           # Data classes
├── viewmodel/       # GameViewModel
├── navigation/      # Screen routes
├── ui/screens/      # Telas do app
└── ui/theme/        # Cores e temas
```

## 🎯 Próximas Funcionalidades

- [ ] Histórico de jogos
- [ ] Ranking de jogadores
- [ ] Persistência local
- [ ] Tie-break
- [ ] Edição manual de placares

## 📝 Logs de Debug

Para ver logs detalhados:
```bash
adb logcat | grep -E "(HomeScreen|GameCodeScreen|PlayerRegistrationScreen|GameViewModel)"
```

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature
3. Commit suas mudanças
4. Push para a branch
5. Abra um Pull Request

---

**Desenvolvido com ❤️ para a comunidade de Beach Tennis** 