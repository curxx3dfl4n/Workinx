@echo off
title WorkInX - Iniciador del Proyecto
echo ========================================================
echo   =€ INICIADOR AUTOMATICO DE WORKINX - SENA ADSO 2026
echo ========================================================
echo.

echo [1/3] Verificando dependencias del Frontend React...
cd /d "%~dp0frontend"
if not exist "node_modules\" (
    echo Instalando librerias de React (npm install)...
    call npm install
) else (
    echo Dependencias de React encontradas.
)

echo.
echo [2/3] Iniciando Servidor Backend Spring Boot (http://localhost:3000)...
cd /d "%~dp0backend"
start "WorkInX Backend" cmd /k "mvnw.cmd spring-boot:run"

echo.
echo [3/3] Iniciando Cliente Frontend React (http://localhost:5173)...
cd /d "%~dp0frontend"
start "WorkInX Frontend" cmd /k "npm run dev"

echo.
echo ========================================================
echo   Proyecto iniciado correctamente en 2 ventanas separadas.
echo  " Backend:  http://localhost:3000
echo  " Frontend: http://localhost:5173/reto-jpa
echo ========================================================
echo.
pause
