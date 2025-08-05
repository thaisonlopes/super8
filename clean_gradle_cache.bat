@echo off
echo 🧹 Limpando cache do Gradle...
echo.

echo 🛑 Parando daemons do Gradle...
gradlew --stop

echo 🗑️  Limpando cache...
if exist "%USERPROFILE%\.gradle\caches" rmdir /s /q "%USERPROFILE%\.gradle\caches"
if exist "%USERPROFILE%\.gradle\daemon" rmdir /s /q "%USERPROFILE%\.gradle\daemon"
if exist "%USERPROFILE%\.gradle\wrapper" rmdir /s /q "%USERPROFILE%\.gradle\wrapper"

echo 🧹 Limpando cache do projeto...
if exist "build" rmdir /s /q "build"
if exist "app\build" rmdir /s /q "app\build"
if exist ".gradle" rmdir /s /q ".gradle"

echo 📥 Baixando dependências...
gradlew clean

echo.
echo ✅ Cache limpo com sucesso!
echo 🔄 Agora você pode tentar compilar o projeto novamente.
echo.
pause 