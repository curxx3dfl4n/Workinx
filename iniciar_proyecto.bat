@echo off
title WorkInX - Iniciador del Proyecto
echo ========================================================
echo   INICIADOR AUTOMATICO DE WORKINX - SENA ADSO 2026
echo ========================================================
echo.

echo [1/4] Verificando dependencias del Frontend React...
cd /d "%~dp0frontend"
if not exist "node_modules\" (
    echo Instalando librerias de React (npm install)...
    call npm install
) else (
    echo Dependencias de React encontradas.
)

echo.
echo [2/4] Iniciando Servidor Backend Principal (http://localhost:3000)...
cd /d "%~dp0backend"
start "WorkInX Backend (3000)" cmd /k "mvnw.cmd spring-boot:run"

echo.
echo [3/4] Iniciando Microservicio JPA Autonomo (http://localhost:8081)...
cd /d "%~dp0microservicio-jpa"
start "WorkInX Microservicio JPA (8081)" cmd /k "mvnw.cmd spring-boot:run"

echo.
echo [4/4] Iniciando Cliente Frontend React (http://localhost:5173)...
cd /d "%~dp0frontend"
start "WorkInX Frontend (5173)" cmd /k "npm run dev"

echo.
echo ========================================================
echo   Proyecto iniciado correctamente en ventanas separadas:
echo   - Backend Principal:  http://localhost:3000
echo   - Microservicio JPA:  http://localhost:8081 (Frontend Morado)
echo   - Frontend React:     http://localhost:5173
echo ========================================================
echo.
pause
