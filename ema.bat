@echo off
rem c:\bin\emacs-24.3\bin\runemacs.exe %*
tasklist /fi "imagename eq emacs.exe" | find /i "emacs.exe" > nul
if not errorlevel 1 (
rem   c:\bin\emacs-24.3\bin\emacsclient.exe -n %* ) else (
rem   c:\bin\emacs-24.3\bin\runemacs.exe %*
rem 24.4
c:\bin\emacs-24.4\bin\emacsclient.exe -n %* ) else (
c:\bin\emacs-24.4\bin\runemacs.exe %*
)