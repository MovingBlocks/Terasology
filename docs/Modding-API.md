Modding API
=================

Terasology's engine uses a whitelisting approach to expose an API for modules using two primary methods and a rarely needed third one:

* Classes or packages marked with the `@API` annotation
* Classes or packages in the basic whitelist defined in `ExternalApiWhitelist.java`
* Rarely blocks of code in the engine may be hit in a way requiring use of `AccessController.doPrivileged(...)` - usually module authors do not need to worry about this but once in a while it could explain something quirky.

This is to both protect the user's system from malicious code (for instance you cannot use `java.io.File` directly) and to better document what is available. If you attempt to use a class not on the whitelist you'll get log message like:

`Denied access to class (not allowed with this module's permissions): some.package.and.class`

While modules can themselves use the `@API` annotation to mark interesting code for reuse no special security is attached at this point beyond the engine. Any module can use anything from any other module it declares as a dependency.

For more information of how the module sandbox works see the [Gestalt Module Sandboxing wiki page](https://github.com/MovingBlocks/gestalt/wiki/Module%20Sandboxing), including how to disable security entirely for prototype work.

The `ApiScraper.java` class will output a list of all `@API` marked functionality. A sample is visible at https://github.com/MovingBlocks/Terasology/issues/1975#issuecomment-180944901 as of early February 2016 (more enhancements coming)

As we improve our documentation system expect to eventually see a JavaDoc-like setup highlighting just the modding API related classes. Track status via [#2159](https://github.com/MovingBlocks/Terasology/issues/2159) and [#1975](https://github.com/MovingBlocks/Terasology/issues/1975)

## See also

* [Developing Modules](Developing-Modules.md) - how to edit an existing module or create a new one
* [Testing Modules](Testing-Modules.md) - test your module with unit tests and integration tests
* [Module Dependencies](Module-Dependencies.md) - how to deal with modules that depend on another
* [I/O API for Modules](IO-API-for-Modules.md) - reading and writing files from modules
* [Module Security](Module-Security.md) - which threats does the module sandbox protect against, and how?
