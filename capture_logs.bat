@echo off
echo Limpando logs...
adb logcat -c
echo.
echo Capturando logs do Super 8...
echo Pressione Ctrl+C para parar
echo.
adb logcat -s "GameViewModel" "PlayerRegistrationScreen" "PlayerInputField" "PlayerListDialog" 