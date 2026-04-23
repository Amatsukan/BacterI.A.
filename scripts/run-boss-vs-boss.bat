@echo off
chcp 65001 >nul

cd /d "%~dp0.."
if not exist "pom.xml" (
    echo [ERRO] pom.xml nao encontrado.
    pause & exit /b 1
)

where mvn >nul 2>&1
if errorlevel 1 (
    echo [ERRO] 'mvn' nao encontrado no PATH.
    pause & exit /b 1
)

echo.
echo ========================================================
echo  bacter-ia :: Boss vs Boss
echo  Replay: http://localhost:8888
echo ========================================================
echo.

mvn test-compile exec:exec@forked-local "-Dexec.mainClass=BossVsBoss"
if errorlevel 1 (
    echo.
    echo [ERRO] Falhou. Veja os erros acima.
    pause & exit /b 1
)
pause
exit /b 0
