@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  Groovy Wrapper version of the Gradle Wrapper
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:init
@rem Get command-line arguments, handling Windows variants

if not "%OS%" == "Windows_NT" goto win9xME_args

:win9xME_args
@rem Slurp the command line arguments.
set CMD_LINE_ARGS=
set _SKIP=2

:win9xME_args_slurp
if "x%~1" == "x" goto execute

@REM Escapes all arguments containing either "*" or "?" before passing them to the groovy process
:process_args
@REM If we have no more arguments to process then exit the loop
IF "%1"=="" GOTO end
SET ARG=%~1
@REM "echo "%ARG%" will simply output the argument as an escaped text string
@REM for piping to other applications. This is needed to ensure that the arguments are not expanded.
@REM findstr is a built-in Windows utility (it has been since Windows 2000)
@REM that finds matches in strings based on regular expressions.
@REM The "&&" operator is followed if the findstr program returns an exit code of 0
@REM Otherwise, the "||" operator is followed instead.
echo "%ARG%" | findstr /C:"\*" 1>nul && (
    SET CMD_LINE_ARGS=%CMD_LINE_ARGS% "%ARG%"
) || (
    echo "%ARG%" | findstr /C:"\?" 1>nul && (
        SET CMD_LINE_ARGS=%CMD_LINE_ARGS% "%ARG%"
    ) || (
        SET CMD_LINE_ARGS=%CMD_LINE_ARGS% %ARG%
    )
)
SHIFT
GOTO process_args

:end
@REM For some reason, the escaped arguments always contain an extra space at the end, which confuses groovy.
@REM This should remove it.
SET CMD_LINE_ARGS=%CMD_LINE_ARGS:~1%

:execute
@rem Setup the command line
set CLASSPATH=%APP_HOME%gradle\wrapper\gradle-wrapper.jar;%APP_HOME%gradle\wrapper\groovy-wrapper.jar;%APP_HOME%gradle\gooeycli\src\main\groovy;


@rem Execute Groovy via the Gradle Wrapper
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -cp "%CLASSPATH%" org.gradle.wrapper.GroovyWrapperMain gradle/gooeycli/src/main/groovy/org/terasology/cli/GooeyCLI.groovy %CMD_LINE_ARGS%

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable GRADLE_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%GRADLE_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
