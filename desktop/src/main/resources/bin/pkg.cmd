@echo off
setlocal

set "CMD=%~1"
set "PKG=%~2"

if "%CMD%"=="" goto usage
if "%CMD%"=="--help" goto usage
if "%CMD%"=="-h" goto usage
if "%CMD%"=="help" goto usage

if /i "%CMD%"=="install" goto do_install
if /i "%CMD%"=="search" goto do_search
if /i "%CMD%"=="uninstall" goto do_uninstall
if /i "%CMD%"=="list-installed" goto do_list
if /i "%CMD%"=="list" goto do_list
if /i "%CMD%"=="upgrade" goto do_upgrade
if /i "%CMD%"=="update" goto do_upgrade

echo Unknown command: %CMD%
goto usage

:do_install
if "%PKG%"=="" (
    echo Usage: pkg install ^<package^>
    exit /b 1
)
set "TARGET=%PKG%"
if /i "%PKG%"=="git" set "TARGET=Git.Git"
if /i "%PKG%"=="vim" set "TARGET=vim.vim"
if /i "%PKG%"=="nano" set "TARGET=GNU.Nano"
if /i "%PKG%"=="jq" set "TARGET=jqlang.jq"
if /i "%PKG%"=="python" set "TARGET=Python.Python.3.12"
if /i "%PKG%"=="python3" set "TARGET=Python.Python.3.12"
if /i "%PKG%"=="curl" set "TARGET=cURL.cURL"
if /i "%PKG%"=="wget" set "TARGET=JernejSimoncic.Wget"
if /i "%PKG%"=="7z" set "TARGET=7zip.7zip"
if /i "%PKG%"=="7zip" set "TARGET=7zip.7zip"

echo Installing %TARGET% via winget...
winget install --exact --id %TARGET%
exit /b %ERRORLEVEL%

:do_search
if "%PKG%"=="" (
    echo Usage: pkg search ^<query^>
    exit /b 1
)
winget search %PKG%
exit /b %ERRORLEVEL%

:do_uninstall
if "%PKG%"=="" (
    echo Usage: pkg uninstall ^<package^>
    exit /b 1
)
set "TARGET=%PKG%"
if /i "%PKG%"=="git" set "TARGET=Git.Git"
if /i "%PKG%"=="vim" set "TARGET=vim.vim"
if /i "%PKG%"=="nano" set "TARGET=GNU.Nano"
if /i "%PKG%"=="jq" set "TARGET=jqlang.jq"
if /i "%PKG%"=="python" set "TARGET=Python.Python.3.12"
if /i "%PKG%"=="python3" set "TARGET=Python.Python.3.12"
winget uninstall %TARGET%
exit /b %ERRORLEVEL%

:do_list
winget list
exit /b %ERRORLEVEL%

:do_upgrade
if not "%PKG%"=="" (
    winget upgrade %PKG%
) else (
    winget upgrade --all
)
exit /b %ERRORLEVEL%

:usage
echo pkg on Windows wraps the Windows Package Manager (winget).
echo Usage: pkg ^<command^> [package]
echo.
echo Commands:
echo   install ^<package^>    Install a package via winget (e.g. git, vim, jq, python3)
echo   search ^<query^>       Search packages with winget search
echo   list-installed       List installed packages with winget list
echo   upgrade              Upgrade packages with winget upgrade
echo   uninstall ^<package^>  Uninstall a package with winget uninstall
exit /b 0
