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
if "%OS%"=="Windows_NT" setlocal enabledelayedexpansion

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

set CMD_LINE_ARGS=%*

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar;%APP_HOME%\gradle\wrapper\groovy-wrapper.jar;
set GROOVY_MAIN_CLASS=org.gradle.wrapper.GroovyWrapperMain

@rem NOTE: the wrapper's own org.gradle.wrapper.GroovyWrapperMain / GroovyBootstrapMainStarter (the
@rem fallback if nothing below applies) is broken against modern (Gradle 9+) distributions - its
@rem classpath assembly looks for a jar matching an old naming pattern ("junit") that no longer
@rem exists, whether the distribution was freshly downloaded or already cached. So below we try to
@rem find an already-available matching distribution (installed or wrapper-cached) and drive Groovy
@rem directly, only falling through to the known-broken path as an absolute last resort.

set PINNED_VERSION=
set DIST_FILE=
for /f "usebackq tokens=1,* delims==" %%a in (`findstr /b "distributionUrl" "%APP_HOME%\gradle\wrapper\gradle-wrapper.properties"`) do (
    for %%f in ("%%b") do set DIST_FILE=%%~nf
)
if defined DIST_FILE (
    set PINNED_VERSION=!DIST_FILE:gradle-=!
    set PINNED_VERSION=!PINNED_VERSION:-bin=!
)

set GRADLE_FOUND=false

@rem 1) A system 'gradle' matching the wrapper's pinned version.
if defined PINNED_VERSION (
    where gradle >NUL 2>&1
    if "!ERRORLEVEL!" == "0" (
        set GRADLE_CMD=
        for /f "usebackq delims=" %%g in (`where gradle`) do (
            if not defined GRADLE_CMD set GRADLE_CMD=%%g
        )
        set INSTALLED_VERSION=
        for /f "tokens=2" %%v in ('"!GRADLE_CMD!" --version 2^>NUL ^| findstr /b "Gradle "') do set INSTALLED_VERSION=%%v
        if "!INSTALLED_VERSION!" == "!PINNED_VERSION!" (
            for %%p in ("!GRADLE_CMD!") do set GRADLE_BIN_DIR=%%~dpp
            for %%p in ("!GRADLE_BIN_DIR!..") do set GRADLE_INSTALL=%%~fp
            call :useGradleInstall "!GRADLE_INSTALL!"
            if "!USE_RESULT!" == "0" set GRADLE_FOUND=true
        )
    )
)

@rem 2) Failing that, a matching distribution gradlew itself already downloaded and cached (so we
@rem    still avoid a second, redundant download even with no system gradle on PATH).
if "!GRADLE_FOUND!" == "false" if defined PINNED_VERSION (
    if defined GRADLE_USER_HOME (
        set GRADLE_USER_HOME_DIR=!GRADLE_USER_HOME!
    ) else (
        set GRADLE_USER_HOME_DIR=!USERPROFILE!\.gradle
    )
    for /d %%h in ("!GRADLE_USER_HOME_DIR!\wrapper\dists\gradle-!PINNED_VERSION!-bin\*") do (
        if "!GRADLE_FOUND!" == "false" if exist "%%h\gradle-!PINNED_VERSION!\lib" (
            call :useGradleInstall "%%h\gradle-!PINNED_VERSION!"
            if "!USE_RESULT!" == "0" set GRADLE_FOUND=true
        )
    )
)

@rem Execute Groovy - either directly via groovy.ui.GroovyMain (if a usable distribution was found
@rem above) or by falling through to the Gradle Wrapper's own (possibly download-triggering) bootstrap
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%CLASSPATH%" %GROOVY_MAIN_CLASS% config/groovy/util.groovy %CMD_LINE_ARGS%

goto end

:useGradleInstall
@rem Sets CLASSPATH/GROOVY_MAIN_CLASS from a Gradle distribution root (%1, a dir containing lib\,
@rem lib\plugins\) and sets USE_RESULT to 0, or to 1 if %1 doesn't look like one.
set _DIR=%~1
if not exist "%_DIR%\lib" if exist "%_DIR%\libexec\lib" set _DIR=%_DIR%\libexec
if not exist "%_DIR%\lib" (
    set USE_RESULT=1
    goto :eof
)
@rem Only the specific jars actually needed to run these scripts (core Groovy + JsonSlurper's module
@rem + Ivy for Grape's @Grab resolution) - not the rest of Gradle's own lib\ (which bundles its own
@rem JGit etc. that could otherwise shadow and conflict with what @Grab-ed scripts, like common.groovy's
@rem grgit, need) nor Groovy's other unused modules (groovydoc, ant, astbuilder...). Startup with a cold
@rem OS file cache (e.g. a wrapper-cache distribution nobody else reads regularly) pays real disk I/O
@rem per jar on the classpath, so keeping this list short matters more than it would look like it should.
@rem (findstr, not the "for" wildcard, does the digit-vs-module-name distinguishing here since batch's
@rem plain wildcards have no character-class support like groovy-[0-9]*.jar would in a POSIX shell.)
set _JARS=
for %%f in ("%_DIR%\lib\groovy*.jar") do (
    set _REST=%%~nf
    set _REST=!_REST:groovy-=!
    echo !_REST!| findstr /r "^[0-9]" >NUL
    if "!ERRORLEVEL!"=="0" set _JARS=!_JARS!%%f;
    echo !_REST!| findstr /b "json-" >NUL
    if "!ERRORLEVEL!"=="0" set _JARS=!_JARS!%%f;
)
for %%f in ("%_DIR%\lib\plugins\ivy-*.jar") do set _JARS=!_JARS!%%f;
if not defined _JARS (
    set USE_RESULT=1
    goto :eof
)
set CLASSPATH=!_JARS!
set GROOVY_MAIN_CLASS=groovy.ui.GroovyMain
set USE_RESULT=0
goto :eof

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
