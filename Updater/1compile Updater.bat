@echo off
javac -encoding UTF8 -Xdiags:verbose Updater.java 2>err.txt
set fldr=Z:\java\Durak\
for /R %fldr% %%I In (err.txt) do if %%~zI equ 0 del /F /Q %%~I
if EXIST err.txt start /min /b ..\\Audio.exe Sounds\error.wav
