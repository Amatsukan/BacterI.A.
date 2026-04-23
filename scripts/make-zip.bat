@echo off
chcp 65001 >nul
cd /d "%~dp0.."

set "ROOT=%CD%"
set "ZIP_NAME=BacterI.A.-main"
set "OUT=%ROOT%\%ZIP_NAME%.zip"

echo.
echo ========================================================
echo  bacter-ia :: Gerar ZIP para submissao no CodinGame
echo  Destino: %OUT%
echo ========================================================
echo.

powershell -ExecutionPolicy Bypass -NoProfile -Command ^
    "$root   = '%ROOT%';" ^
    "$name   = '%ZIP_NAME%';" ^
    "$out    = '%OUT%';" ^
    "$tmpDir = Join-Path $env:TEMP $name;" ^
    "if (Test-Path $tmpDir) { Remove-Item $tmpDir -Recurse -Force };" ^
    "New-Item -ItemType Directory -Path $tmpDir -Force | Out-Null;" ^
    "$items = @('pom.xml','config','src');" ^
    "foreach ($i in $items) {" ^
    "    $src = Join-Path $root $i;" ^
    "    $dst = Join-Path $tmpDir $i;" ^
    "    if (Test-Path $src -PathType Container) { Copy-Item $src $dst -Recurse -Force }" ^
    "    elseif (Test-Path $src) { Copy-Item $src $dst -Force } };" ^
    "$scriptsDst = Join-Path $tmpDir 'scripts';" ^
    "New-Item -ItemType Directory -Path $scriptsDst -Force | Out-Null;" ^
    "Get-ChildItem (Join-Path $root 'scripts') -Filter '*.bat' | ForEach-Object { Copy-Item $_.FullName (Join-Path $scriptsDst $_.Name) -Force };" ^
    "if (Test-Path $out) { Remove-Item $out -Force };" ^
    "Add-Type -AssemblyName System.IO.Compression.FileSystem;" ^
    "[System.IO.Compression.ZipFile]::CreateFromDirectory($tmpDir, $out);" ^
    "Remove-Item $tmpDir -Recurse -Force;" ^
    "$size = [math]::Round((Get-Item $out).Length / 1KB, 1);" ^
    "Write-Host \"[OK] $out ($size KB)\""

if errorlevel 1 (
    echo [ERRO] Falhou ao gerar o ZIP.
    pause & exit /b 1
)

echo.
pause
exit /b 0
