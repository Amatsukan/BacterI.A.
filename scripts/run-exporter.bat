@echo off
chcp 65001 >nul
cd /d "%~dp0.."

if not exist "target\referee.jar" (
  echo [ERRO] target\referee.jar nao existe. Corra primeiro:
  echo   mvn -f pom-plugin.xml clean package -DskipTests
  pause & exit /b 1
)

rem JDK 17+: Guice/cglib inside Exporter needs --add-opens (same as Surefire / .mvn/jvm.config)
set "JAVA_TOOL_OPTIONS=--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED"

java -cp "target\referee.jar" com.codingame.gameengine.runner.Exporter "%CD%"
set "JAVA_TOOL_OPTIONS="
exit /b %ERRORLEVEL%
