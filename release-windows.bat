@echo off
setlocal enabledelayedexpansion

echo ================================================
echo   AuraPlay - Windows Release Script
echo ================================================
echo.

:: Check if we're in the correct directory
if not exist "gradlew.bat" (
    echo [ERROR] gradlew.bat not found!
    echo Please run this script from the AuraPlay project root folder.
    pause
    exit /b 1
)

:: Ask for version
set /p VERSION="Enter version tag (e.g. v1.0.0): "
if "%VERSION%"=="" (
    echo [ERROR] Version cannot be empty.
    pause
    exit /b 1
)

:: Ask what to build
echo.
echo What would you like to build?
echo 1. APK only
echo 2. AAB only (recommended for Play Store)
echo 3. Both APK and AAB
set /p BUILD_CHOICE="Choose option [1-3]: "

:: Clean previous builds
echo.
echo [INFO] Cleaning previous builds...
call gradlew.bat clean

:: Build based on choice
if "%BUILD_CHOICE%"=="1" (
    echo [INFO] Building release APK...
    call gradlew.bat assembleRelease
    set "OUTPUT_PATH=app\build\outputs\apk\release"
) else if "%BUILD_CHOICE%"=="2" (
    echo [INFO] Building release AAB...
    call gradlew.bat bundleRelease
    set "OUTPUT_PATH=app\build\outputs\bundle\release"
) else if "%BUILD_CHOICE%"=="3" (
    echo [INFO] Building release APK and AAB...
    call gradlew.bat assembleRelease
    call gradlew.bat bundleRelease
    set "OUTPUT_PATH=app\build\outputs"
) else (
    echo [ERROR] Invalid choice.
    pause
    exit /b 1
)

echo.
echo [SUCCESS] Build completed!
echo Output location: %OUTPUT_PATH%
echo.

:: Ask if user wants to create git tag
set /p CREATE_TAG="Create and push git tag %VERSION%? (y/n): "
if /i "%CREATE_TAG%"=="y" (
    echo [INFO] Creating git tag...
    git tag %VERSION%
    
    echo [INFO] Pushing tag to GitHub...
    git push origin %VERSION%
    
    echo [SUCCESS] Tag %VERSION% pushed!
) else (
    echo [INFO] Skipping tag creation.
)

echo.
echo ================================================
echo   Release process finished!
echo ================================================
echo.
echo Next steps:
echo 1. Go to: https://github.com/Mylittlestories/AuraPlay/releases/new
echo 2. Select tag: %VERSION%
echo 3. Upload the built file(s) from: %OUTPUT_PATH%
echo.
pause
endlocal