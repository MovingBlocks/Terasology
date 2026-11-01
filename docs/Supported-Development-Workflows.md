# About

This document is aimed at those who are testing the development environment itself, i.e. to make sure that a change to the build configuration hasn't broken another contributor's workflow.

## Help Wanted: 🧩

A 🧩 symbol in this document indicates an incomplete section. This is a wiki, please help here you can!

If you have questions about what's needed or you want to someone to look over what you've written before you commit it, ask us in the `#logistics` channel of [Discord](Home.md#joining-the-community--asking-for-help).

# Engine, Subsystems, Facade

## Gradle `game`

`gradlew game` builds the PC facade, the engine, and its subsystems.

It rebuilds any classes that have had source changes in any of these.
It does not spend time rebuilding things that have not changed.

It starts a new process running the PC facade.
All dependencies are provided on the classpath.

## Run _TerasologyPC_

In IntelliJ IDEA, running the `TerasologyPC` configuration serves the same purpose as `gradlew game` does.

Some implementation details may differ, such as:

* the exact construction of the classpath

If IntelliJ is building after a previous gradle build, IntelliJ **MAY** decide to build some things itself rather than relying on gradle's previous build result, but it **SHOULD NOT** spend time re-building things it already built if they have not changed. (Unless you explicitly _Rebuild Project_.)

## Distribution

🧩 notes about this in [#4347](https://github.com/MovingBlocks/Terasology/pull/4347).


## Debugging

### Breakpoints

🧩

### Method Reloading

🧩

## reflections.cache

🧩 Is currently [not built for local classes at all](https://github.com/MovingBlocks/Terasology/pull/4157#issuecomment-708589181), only for jars? That MAY be changed, but we might end up with an alternate implementation that uses something else before that happens.


# Modules

## Gradle `game`

In addition to the subprojects mentioned in the **Engine** section above, `gradlew game` _also_ builds all module subprojects present in the `/modules` directory.

If a subproject under `/modules` depends on a module that is _not_ part of the current build,  
a jar file for that dependency will be put in `/cachedModules`.

The `/modules` projects and `/cachedModules` **MUST NOT** be on the classpath of the game process. (Modules being on the classpath broke the sandbox, see [#4375](https://github.com/MovingBlocks/Terasology/issues/4375).)

All locally-built modules and the modules they depend on must appear in the Advanced Game Setup screen.

(🧩 and also in the logs somewhere?)

If a module is present as a local subproject, that **MUST** be the version of the module used by the game. (i.e. it must not load those classes from a jar it got from another repository.)

## Run _TerasologyPC_

(same)


## Asset Reloading

🧩 TO DOCUMENT: there's some asset-reloading stuff that's supposed to happen _without_ using any IntelliJ debugger features and _without_ needing to (re)build a jar file.

Does it apply equally to all types of assets? e.g. prefabs, meshes, textures, translations. Probably not: [DW notes only a few asset types are possible](https://github.com/MovingBlocks/Terasology/pull/4157#issuecomment-708589181).

See [note about asset location during editing](https://github.com/MovingBlocks/Terasology/pull/4157#issuecomment-735016251).

## Versions

The version number of a module is defined by the `version` field in its `module.txt`. Its version number **MUST** appear identically in `gradlew 🧩` and in-game in the Advanced Game Setup screen.

## Module Downloader

🧩 something at runtime sometimes puts `.jar` files in `$homeDir/modules/`? and if they overlap with something provided by gradle dependencies, then it should explode, or use one, or the other?


# Other Dependencies

Running Terasology while developing one of its dependencies outside of the `MovingBlocks/Terasology` project, such as TeraNUI.

See [Using Locally Developed Libraries](Using-Locally-Developed-Libraries.md).
