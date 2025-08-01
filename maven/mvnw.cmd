@echo off
rem Simple Maven Wrapper 
rem Uses Java to run Maven commands without needing Maven installed

rem Check if Java is installed
if "%JAVA_HOME%" == "" (
    echo Error: JAVA_HOME not set! Please install Java first.
    exit /b 1
)

rem Find where this script is located
set SCRIPT_DIR=%~dp0
set PROJECT_DIR=%SCRIPT_DIR%..

rem Set up Maven variables
set MAVEN_VERSION=3.8.6
set MAVEN_JAR=%PROJECT_DIR%\.mvn\wrapper\maven-wrapper.jar
set MAVEN_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.1.0/maven-wrapper-3.1.0.jar
set MAVEN_PROPS=%PROJECT_DIR%\.mvn\wrapper\maven-wrapper.properties

rem Download Maven wrapper if needed
if not exist "%MAVEN_JAR%" (
    echo Maven wrapper JAR not found. Downloading it now...
    
    rem Create directory if needed
    if not exist "%PROJECT_DIR%\.mvn\wrapper" (
        mkdir "%PROJECT_DIR%\.mvn\wrapper"
    )
    
    rem Download with PowerShell
    powershell -Command "& { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object Net.WebClient).DownloadFile('%MAVEN_URL%', '%MAVEN_JAR%') }"
    
    if not exist "%MAVEN_JAR%" (
        echo Failed to download Maven wrapper! Check your internet connection.
        exit /b 1
    )
)

rem Create default properties file if missing
if not exist "%MAVEN_PROPS%" (
    echo distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip > "%MAVEN_PROPS%"
)

rem Run Maven
"%JAVA_HOME%\bin\java" -Dmaven.multiModuleProjectDirectory="%PROJECT_DIR%" -jar "%MAVEN_JAR%" %*

exit /b %ERRORLEVEL%
