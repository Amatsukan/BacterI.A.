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
echo  bacter-ia :: Testes unitarios  (mvn test)
echo ========================================================
echo.

mvn test
set "RESULT=%ERRORLEVEL%"

echo.
if not "%RESULT%"=="0" (
    echo [FALHOU] Testes unitarios falharam.
    pause & exit /b %RESULT%
)

echo [PASSOU] Todos os testes passaram.
echo.
echo ========================================================
echo  Iniciar simulacao local? (WaitBot vs WaitBot)
echo  Replay: http://localhost:8888
echo ========================================================
echo.
set /p SIMULAR="  Correr simulacao? [S/N]: "
if /i "%SIMULAR%"=="S" (
    echo.
    echo A iniciar simulacao...
    echo.
    mvn test-compile exec:java
)
echo.
pause
exit /b 0
