# Testing

* The [Malicious] module executes its [malicious tests] when its system is initialized. Look for messages from this class in the Terasology log file.
* The [Sample] module provides a `ReallyCrashGameBlock` that attempts [unauthorized use of System.out](https://github.com/Terasology/Sample/blob/develop/src/main/java/org/terasology/sample/crash/CrashGameBlockSystem.java) when you use it.
  - use the console to `give ReallyCrashGameBlock`
  - place the block on the ground
  - hit the `Use` key

[Malicious]: https://github.com/Terasology/Malicious
[malicious tests]: https://github.com/Terasology/Malicious/blob/develop/src/main/java/org/terasology/maliciousUsageTests/MaliciousTestSystem.java
[Sample]: https://github.com/Terasology/Sample


# Threat Models

## Threats from local execution of untrusted modules

### Accessing a local resource

For example:
* a local file
* capture your desktop (outside the game window)
* snoop on local devices (keyboard, webcam, USB drives)


### Accessing your local network
* smartphones and other computers
* printers and other Internet-connected Things


### Exfiltration and Exploitation of Remote Networks
* uploading data to a third-party server
* using network resources to attack a remote target

⚠ A module _will_ send data to the game server you are connected to. The thing to prevent is sending information to a third party without the consent of either client or server.


## Threats from network input from untrusted clients

The game creates new objects and executes methods on them in response to network input. An attacker may attempt to craft a message that tricks the server in to executing an unsafe method.


# Security Mechanisms

Terasology relies on [Gestalt Module Sandboxing](https://github.com/MovingBlocks/gestalt/wiki/Module%20Sandboxing) to protect from these risks of running untrusted JVM code. However, it's up to the application to make sure the sandbox is configured and applied correctly.

* [o.t.engine.core.module.ExternalApiWhitelist](https://github.com/MovingBlocks/Terasology/blob/develop/engine/src/main/java/org/terasology/engine/core/module/ExternalApiWhitelist.java) defines a hardcoded list of allowable packages and classes.

## ClassLoaders

* `ModuleManager.setupSandbox` configures a PermissionProviderFactory with modules and the allowable packages and classes.
* `ModuleManager.loadEnvironment` constructs a gestalt.module.ModuleEnvironment with that PermissionProviderFactory.


## Java Security Manager

`o.t.engine.core.ModuleManager.setupSandbox` installs the gestalt ModuleSecurityPolicy and ModuleSecurityManager.

The restrictions of ModuleSecurityPolicy apply to classes which were loaded using a ModuleClassLoader.

⚠ This API is proposed for removal from a future version of the JDK ([JEP 411]). If it's first deprecated in JDK 17, it will be quite a while yet before it's removed entirely, but eventually will come a time when we'll want the features of a new JDK and the Security Manager is no longer available.

[JEP 411]: https://openjdk.java.net/jeps/411 "JEP 411: Deprecate the Security Manager for Removal"


## Type Registry

* The [nui-reflect TypeRegistry](https://github.com/MovingBlocks/TeraNUI/blob/ff5ec35083520d8bb986f410fda482ea6bb5ca93/nui-reflect/src/main/java/org/terasology/reflection/TypeRegistry.java#L73) uses lists of allowable classes and packages to guard against ⎵⎵⎵⎵⎵.
* an `o.t.persistence.typeHandling.TypeHandlerLibrary` makes use of both a nui-reflect TypeRegistry _and_ a gestalt ModuleEnvironment.


# Related:
* [Modding API](Modding-API.md)
* [IO API for Modules](IO-API-for-Modules.md) for local persistent storage


# Threats not addressed

* local denial of service attack (excessive CPU and RAM consumption)
* exploiting local computing resources (crypto mining)
* …?
