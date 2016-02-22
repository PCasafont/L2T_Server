@echo off
title Login Server Console
:start
echo Starting L2Tenkai Login Server.
echo.
java -Xms128m -Xmx128m  -cp ./../libs/*;l2jlogin.jar l2server.loginserver.L2LoginServer
if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
goto end
:restart
echo.
echo Admin Restart ...
echo.
goto start
:error
echo.
echo Server terminated abnormaly
echo.
:end
echo.
echo server terminated
echo.
pause
