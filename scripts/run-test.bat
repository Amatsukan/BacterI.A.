@echo off
chcp 65001 >nul

rem Muda para a raiz do projeto (pasta acima de scripts\)
cd /d "%~dp0.."
if not exist "pom.xml" (
    echo [ERRO] pom.xml nao encontrado.
    pause & exit /b 1
)

where mvn >nul 2>&1
if errorlevel 1 (
    echo [ERRO] 'mvn' nao encontrado no PATH.
    echo Instale o Maven e adicione a pasta bin ao PATH.
    pause & exit /b 1
)

echo.
echo ========================================================
echo  bacter-ia :: mvn clean compile
echo ========================================================
echo.

mvn clean compile
if errorlevel 1 (
    echo.
    echo [ERRO] Compilacao falhou. Veja os erros acima.
    pause & exit /b 1
)

echo.
echo [OK] Compilacao concluida com sucesso!
echo.
pause
exit /b 0
