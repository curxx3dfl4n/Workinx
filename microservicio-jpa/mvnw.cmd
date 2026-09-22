@echo off
@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script (JDK 21 Auto-Detection)
@REM ----------------------------------------------------------------------------

set ERROR_CODE=0

if exist "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\java.exe" (
  set "JAVA_EXE=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\java.exe"
) else if exist "C:\Users\User\.jdks\temurin-21.0.12.1\bin\java.exe" (
  set "JAVA_EXE=C:\Users\User\.jdks\temurin-21.0.12.1\bin\java.exe"
) else if exist "C:\Users\User\.jdks\ms-21.0.12.1\bin\java.exe" (
  set "JAVA_EXE=C:\Users\User\.jdks\ms-21.0.12.1\bin\java.exe"
) else if not "%JAVA_HOME%"=="" (
  set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) else (
  set "JAVA_EXE=java"
)

set "PROJECT_DIR=%~dp0"
if "%PROJECT_DIR:~-1%"=="\" set "PROJECT_DIR=%PROJECT_DIR:~0,-1%"

set "WRAPPER_JAR=%PROJECT_DIR%\.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain"

"%JAVA_EXE%" "-Dmaven.multiModuleProjectDirectory=%PROJECT_DIR%" -classpath "%WRAPPER_JAR%" %WRAPPER_LAUNCHER% %*
if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
cmd /c exit /b %ERROR_CODE%
