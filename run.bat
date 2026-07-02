@echo off
cd /d "%~dp0"
java -cp "out;lib\junit-4.13.2.jar;lib\hamcrest-core-1.3.jar" Main
