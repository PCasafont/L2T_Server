@echo off
title Game Server Console
:start
echo Starting L2Tenkai Game Server.
echo.
REM -------------------------------------
REM Default parameters for a basic server.
java -server -Xmx7048m -Xms3048m -Xmn512m -Xss128k -XX:PermSize=64m -XX:+UseThreadPriorities -XX:ParallelGCThreads=20 -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:SurvivorRatio=6 -XX:TargetSurvivorRatio=75 -Xnoclassgc -XX:+AggressiveOpts -XX:+UseBiasedLocking -XX:+UseFastAccessorMethods -XX:+UseStringCache -XX:+OptimizeStringConcat -Djava.util.logging.manager=l2server.util.L2LogManager -cp ./../libs/*;l2jserver.jar l2server.gameserver.GameServer
REM
REM If you have a big server and lots of memory, you could experiment for example with
REM java -server -Xmx1536m -Xms1024m -Xmn512m -XX:PermSize=256m -XX:SurvivorRatio=8 -Xnoclassgc -XX:+AggressiveOpts
REM If you are having troubles on server shutdown (saving data),
REM add this to startup paramethers: -Djava.util.logging.manager=com.l2jserver.L2LogManager. Example:
REM java -Djava.util.logging.manager=com.l2jserver.util.L2LogManager -Xmx1024m -cp ./../libs/*;l2jserver.jar com.l2jserver.gameserver.GameServer
REM -------------------------------------
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
