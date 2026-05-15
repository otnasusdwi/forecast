@echo off
setlocal

REM Menjalankan aplikasi dari fat-jar hasil build Maven.
REM Syarat: Java (JRE/JDK) sudah terpasang di Windows.

if not exist "target\forecast-app.jar" (
  echo File target\forecast-app.jar tidak ditemukan.
  echo Build dulu di Linux/Windows dengan: mvn clean package
  pause
  exit /b 1
)

java -jar "target\forecast-app.jar"

if errorlevel 1 (
  echo.
  echo Gagal menjalankan aplikasi. Pastikan Java sudah terpasang dan sesuai versi.
  pause
)

endlocal
