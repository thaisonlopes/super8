@echo off
echo Limpando projeto Super 8 Beach Tennis...
echo.

echo Parando daemons do Gradle...
gradlew --stop

echo.
echo Executando clean forçado...
gradlew clean --no-daemon

echo.
echo Clean concluído!
echo.
pause 