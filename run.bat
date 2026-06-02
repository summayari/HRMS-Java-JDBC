@echo off
title HRMS v5 - Role Based
javac -encoding UTF-8 -cp "lib\*" -sourcepath src -d out src\hrms\Main.java
if %errorlevel% neq 0 (echo Compilation failed! & pause & exit)
java -cp "out;lib\*" hrms.Main
pause
