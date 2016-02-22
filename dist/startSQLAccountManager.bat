@echo off
cls
title L2J - SQL Account Manager
color 17
java -Djava.util.logging.config.file=console.cfg -cp ./../libs/*;l2jlogin.jar l2server.accountmanager.SQLAccountManager 2> NUL
if %errorlevel% == 0 (
echo.
echo Execution succesful
echo.
) else (
echo.
echo An error has ocurred while running the L2J Account Manager!
echo.
echo Possible reasons for this to happen:
echo.
echo - Missing .jar files or ../libs directory.
echo - MySQL server not running or incorrect MySQL settings:
echo    check ./config/loginserver.properties
echo - Wrong data types or values out of range were provided:
echo    specify correct values for each required field
echo.
)
pause
