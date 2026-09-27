@echo off
title TerraForge RPG - Executando Minecraft Client
cd /d "%~dp0"
echo ========================================================
echo Iniciando TerraForge RPG Client (NeoForge 1.21.1)...
echo ========================================================
call gradlew.bat runClient
pause
