@echo off
rem **************************************************************
rem *  This is an MS-DOS BATCH file for uploading the
rem *  ISPHERE save file to a System i server.
rem **************************************************************
set TEMPFILE=ftp_upload.ftpcmds
set HOST=%1
set USER=%2
set PASSWORD=%3
set LIB=QGPL
set RMT_FILE=ISPHERE
set LCL_FILE=

if "%HOST%"=="" goto help
if "%USER%"=="" goto help
if "%PASSWORD%"=="" goto help

:findFile
set count=0
for %%f in (*.SAVF) do (
   set /a count+=1
   set LCL_FILE=%%f
)
if not "%count%"=="1" (goto tooMuchFiles)



:askUser
cls
echo *************************************************************
echo   You are about to uploaded %LCL_FILE% to
echo   file %LIB%/%RMT_FILE% on host %HOST%.
echo.
echo   If the file %LIB%/%RMT_FILE% does already exist
echo   it will be deleted.
echo *************************************************************
set choice=
set /p choice=Do you want to continue? (y/n)
if /i "%choice%"=="y" goto continue
if /i "%choice%"=="n" goto end
goto askUser

:continue
echo %USER%> %TEMPFILE%
echo %PASSWORD%>> %TEMPFILE%
echo quote site namefmt 0 >> %TEMPFILE%
echo quote DLTF FILE(%LIB%/%RMT_FILE%) >> %TEMPFILE%
echo quote rcmd CRTSAVF FILE(%LIB%/%RMT_FILE%) >> %TEMPFILE%
echo binary >> %TEMPFILE%
echo put %LCL_FILE% %LIB%/%RMT_FILE% >> %TEMPFILE%

echo quit >> %TEMPFILE%
ftp -s:%TEMPFILE% %HOST%
del %TEMPFILE%

echo *************************************************************
echo   Successfully uploaded %LCL_FILE% to library %LIB% on
echo   host %HOST%.
echo   Now log on to host %HOST% and execute the
echo   following commands:
echo      1. RSTLIB SAVLIB(ISPHERE) DEV(*SAVF)
echo            SAVF(%LIB%/%RMT_FILE%)
echo *************************************************************
goto end

:tooMuchFiles
echo.
echo. Error: Too much files with extension *.SAVF found!
for %%f in (*.SAVF) do (
   echo.        %%f
)
echo.
echo. Stopped uploading ISPHERE to host: %HOST%.
echo.
goto end

:help
echo.
echo Uploads the ISPHERE save file to your iSeries.
echo.
echo. upload_savf.cmd HOST USER PASSWORD
echo.
echo      HOST = FTP host you want to upload to (as400.example.com)
echo      USER = UserID to log in with
echo  PASSWORD = Password to log in with
echo.
:end
