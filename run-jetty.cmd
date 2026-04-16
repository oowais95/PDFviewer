@echo off
setlocal ENABLEDELAYEDEXPANSION
cd /d "%~dp0"

if not "%JAVA_HOME%"=="" goto run

for /f "delims=" %%H in ('powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\detect-java-home.ps1"') do set "JAVA_HOME=%%H"

if "%JAVA_HOME%"=="" (
  echo JAVA_HOME is not set and could not be detected from `java`.
  echo Install a JDK and either:
  echo   set JAVA_HOME=C:\Path\To\Your\JDK
  echo or ensure `java` is on PATH, then try again.
  exit /b 1
)

echo Using JAVA_HOME=%JAVA_HOME%

:run
REM Spring Boot embedded Tomcat — same context path /pdfViewerWebApp as before
REM If 8080 is busy: mvnw.cmd spring-boot:run -Dserver.port=8081
call "%~dp0mvnw.cmd" spring-boot:run %*
exit /b %ERRORLEVEL%
