## Terasology

### How to enable debug logs

When troubleshooting Terasology, debug logs can help you identify issues or get additional information you can use to narrow down the origin of an issue to then investigate further, for instance using the IntelliJ debugger.

You can adjust the log level(s) for Terasology in `facades/PC/src/main/resources/logback.xml`.
The default setting (currently) looks as follows:

```xml
  <logger name="org.terasology" level="${logOverrideLevel:-info}" />
```

The first part, `logOverrideLevel`, refers to the log level that can be configured in the launcher's advanced settings.
When running Terasology from source, the second part will be used instead.

To enable debug logs, set the second part to "debug":

```xml
  <logger name="org.terasology" level="${logOverrideLevel:-debug}" />
```

Debug logs for all of Terasology including the engine and all modules can be a bit overwhelming.
This is why, you can add fine granular custom log-level overrides.
This works in both ways, i.e., you can increase or decrease the log level for specific packages or classes by adding lines similar to the following:

```xml
  <logger name="org.terasology.engine.world" level="debug" />
  <logger name="org.terasology.module.health.systems.HealthAuthoritySystem" level="error" />
```

### Failed to create window

Symptom: You cannot start Terasology as it will crash and show you an error popup stating "Failed to create window"

Logs:
```
[main] ERROR GLFW - Received error. Code: 65543, Description: GLX: Failed to create context: BadValue (integer parameter out of range for operation)
[main] ERROR o.terasology.engine.TerasologyEngine - Failed to initialise Terasology
java.lang.RuntimeException: Failed to create window
    at org.terasology.engine.subsystem.lwjgl.LwjglGraphics.initWindow(LwjglGraphics.java:155)
    at org.terasology.engine.subsystem.lwjgl.LwjglGraphics.postInitialise(LwjglGraphics.java:81)
    at org.terasology.engine.TerasologyEngine.postInitSubsystems(TerasologyEngine.java:295)
```

This can happen if you switched monitors, reconfigured your monitor's resolution or updated your OS.
To resolve this, you can try to restart your system, delete `config.cfg` from your Terasology workspace root directory and re-attempt to start it.

## IntelliJ
 
* Do use IntelliJ's "Import Gradle Project" offering. Terasology supports this with IntelliJ IDEA versions 2020.1 and later.
* For a first time install of IntelliJ you may not have a defined JDK available at a platform level. Look at `File -> Project Structure -> SDKs` and use the green + if needed to point to a local JDK 11. If not you may get errors about basic Java language features missing.
  * After setting this up or if you name your JDK something other than the default "11" you may have to set that to the Project SDK, again on the Project Structure page.
* Any time you change the overall project structure by adding/deleting modules or pull updates from elsewhere that contain library changes, IntelliJ will have to re-sync with gradle to learn about the new configuration. If it doesn't do so automatically, use the 🔄 _Load Gradle Changes_ action in the Gradle tool window.

### Stale Build State

Sometimes IntelliJ will end up in a stale build state. One of the symptoms you'll see if that is the case are problems with `/splash/splash_1.png` on trying to start / debug Terasology.

In order to resolve this stale build state, go to the IntelliJ menu bar, click on "Build" and then on "Rebuild project".
Wait until the project was rebuilt and then try to start / debug Terasology again.

### Resetting a buggy IntelliJ Config

Sometimes your workspace seems to be messed up, e.g. you're facing missing run configurations, buggy code navigation, or similar issues.
In these cases, you can try to reset your IntelliJ config in order to get a clean state. You can do so by following these steps:
1. Close your project inside IntelliJ and then close IntelliJ itself
1. Run `gradlew cleanIdea` to wipe any existing IntelliJ config in your workspace
1. Open IntelliJ (should be at its non-project screen with short-cuts to create or open a project)
1. Click on "Open project" and select the build.gradle at the root of your Terasology workspace (make sure you open it as a project / allow it to import via Gradle)
1. When it opens it will take some time to index all the files and import everything properly

### Compile Errors relating to Protobuf

IntelliJ sometimes circumvents tasks that generate resources required for compilations, e.g. protobuf sources.
If you're seeing compilation errors relating to protobuf then you might need to run `gradlew genProto` to generate these sources.


## Java

* It can be tricky to make sure the right version of Java is being used in different situations (within an IDE, via command line, on a server ..) - check the following options if needed:
  * System `JAVA_HOME` environment variable
  * System `PATH` environment variable
  * The `update-alternatives` or `update-java-alternatives` commands available on some Linux systems can override what's set via `JAVA_HOME`. See [this Ask Ubuntu thread](http://askubuntu.com/questions/159575/how-do-i-make-java-default-to-a-manually-installed-jre-jdk) or check Google.
* The Java installation picker (which download you get from https://www.java.com/en/download) may give you a 32-bit installer if you're using a 32-bit browser on a 64-bit system. Use 64-bit if you can!
  * Use `java -version` to find out what you have. This in turn may also depend on your OS. For instance, Windows may default a command prompt to `C:\Windows\System32` which will give you bad info. Try from `C:\` or anywhere else instead.
  * The 64-bit version explicitly states it is 64-bit (like `64-Bit Server VM`) where 32-bit may call itself something like `Client VM`
* If you do development and have something like Android Studio installed you may have had your system classpath modified. This can cause trouble if a system version of some Java dependency gets used in favor of a version we bundle. 
  * Example: If you get an error like `Caused by: java.lang.UnsatisfiedLinkError: Can't obtain static method fromNative(Method, Object) from class com.sun.jna.Native` then try running the game with `java -Djna.nosys=true -jar libs/Terasology.jar --homedir=.` to ignore the system-provided version of JNA
* You can get the JVM in different implementations, e.g., _HotSpot_ or _OpenJ9_. Even though these Java virtual machine implementations are fully compliant with the JVM Specification this may lead to issues when running Terasology. **There is a known issue with OpenJ9 when starting a game** (April 2021). We recommend to us a JDK with a HotSpot JVM.

  ```
  21:00:03.779 [main] ERROR o.t.module.sandbox.ModuleClassLoader - Denied access to class (not allowed with this module's permissions): java.io.Serializable
  ```  

## Linux

* Watch out for line-ending issues. For instance, the `gradlew` script might throw an error like `bash: ./gradlew: /bin/bash^M: bad interpreter: No such file or directory` if it somehow ends up with Windows-style line endings.
* If having issues with SSL when using `gradlew`, run `update-ca-certificates -f` as root (or using `sudo`).
* If xrandr is being used to set a custom resolution, the game could possibly crash on launch due to lwjgl's xrandr configuration parsing. To fix, make sure the configuration mode name follows the format `"3000x2000"` instead of `"3000x2000_60.00"` or `"3000by2000"`.
* Sometimes while using the debugger, the mouse could get 'grabbed' by LWJGL making it hard to go through variable values. This handy little trick allows you to create another mouse pointer. The game only grabs one leaving you with the other to use. https://stackoverflow.com/questions/23795010/lwjgl-grabbed-mouse-debug-if-application-hangs-or-when-breakpoint-hits-with-gr/36991052#36991052

### ld.so dl-lookup failures

> Inconsistency detected by ld.so: dl-lookup.c: 111: check_match: Assertion version->filename == NULL || ! _dl_name_match_p (version->filename, map)' failed!

The process quits with this error message when attempting to load the lwjgl library under _some_ OpenJDK distributions. Please consult the distributor of your JDK about this error. It may be useful for them to refer to previous bug reports on the topic:

* [Ubuntu #1764701](https://bugs.launchpad.net/ubuntu/+source/gcc-7/+bug/1764701)
* [Ubuntu #1838740](https://bugs.launchpad.net/ubuntu/+source/openjdk-lts/+bug/1838740)
* ✍ (if you find a well-written bug report about this for your jdk, do add it here!)

If one of those links is to the JDK you're using, you can let them know by clicking the bug tracker's "this affects me too" link.

In the meantime, use a different JDK. We've found [AdoptOpenJDK](https://adoptopenjdk.net/?variant=openjdk11&jvmVariant=hotspot) to work well and it can be easily installed from [IntelliJ IDEA's Download JDK](https://www.jetbrains.com/help/idea/sdk.html#manage_sdks) menu. (Hotspot, version 11.)

### Arch/Gentoo random problems

Both Arch and Gentoo do not include java jfx (needed for the launcher) by default when installing java.

On Arch you need to install the `java-openjfx` package, and on Gentoo you need to use the `javafx` USE flag when compiling java.


xrandr is a generic linux dependency for terasology, but the cases where you have it uninstalled usually only happen on arch and gentoo. 

## Windows

It is possible to end up getting Gradle into a fight with the Windows specific `Thumbs.db` files. If it is not an option to disable the generation of these files (relates to icon previewing) or even once after it is disabled you can clean a directory and its subdirs with one of the following commands:

* `del /s /q /f /a:h Thumbs.db` while in the top-level directory in a plain command prompt
* `Get-ChildItem -Path . -Include Thumbs.db -Recurse -Name -Force | Remove-Item -Force` likewise, with Powershell

Solutions sourced from https://github.com/MarcusBarnes/mik/wiki/Cookbook:-Removing-.Thumbs.db-files-from-a-directory
