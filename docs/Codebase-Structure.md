This is an overview of the different parts of our primary codebase and will be primarily of interest to developers/modders. See also [Project Overview](Project-Overview.md) for a higher level view across more different projects.

Gradle
---------

Most of our projects are organized using [Gradle](http://www.gradle.org), a build automation tool similar to [Maven](http://maven.apache.org), but using [Groovy](http://groovy.codehaus.org) as its language. This allows for some pretty powerful scripting and customization of a local developer setup while leveraging maven repositories for dependency resolution.

You interact with Gradle using a "gradlew" wrapper script that downloads and installs the correct project-specific version of Gradle itself. No need to install anything locally yourself. You simply run the command "gradlew" on any OS in the directory you've cloned the primary Terasology engine repo to and the correct version will be fetched and executed.

Gradle works great with multiple sub-projects and builds a "project tree" before any functional work begins. This is great for dealing with dependencies but does also mean that if the project structure changes on one execution (such as when fetching a new module from GitHub) the new sub-project will only be included on the next execution.

As you work on Terasology you might find out that you need source or binaries for other sub-projects, which can then be added easily to your existing workspace through an assortment of Gradle tasks. Keep reading to learn more.

Note: On Linux and Mac OS you may have to first mark the "gradlew" file as executable.

To speed up gradle on linux add org.gradle.daemon=true to ~/.gradle/gradle.properties


Engine
---------

The heart and soul of Terasology. All facades and modules depend on the engine.

The primary repo containing the engine is the first and only thing you need to run the game and allows you to use Gradle commands to fetch everything else. Always assume you need to execute "gradlew" from the root directory of this project unless stated otherwise. This is the one repo you need to clone manually:

`git clone https://github.com/MovingBlocks/Terasology.git`

Along with the engine you get the PC Facade and a few embedded modules. Everything else lives in independent GitHub repos that can be added to your local workspace as sub-projects. You can edit several at once, commit files across multiple Git roots, and push it all to each affected repo on GitHub in one action.

It also comes with several utility tasks, such as project file generation for [IDEA IntelliJ](http://www.jetbrains.com/idea). This is our recommended IDE (with a [freebie Ultimate Edition available](http://forum.terasology.org/threads/how-to-use-this-forum.33/#post-8778) for contributors) and the customized setup for it is fairly detailed.

You can also use [Eclipse](http://www.eclipse.org/), [NetBeans](https://netbeans.org/), or anything else you like, but you might have to figure out some of the setup details yourself - and if you do please post them in the [forum](http://forum.terasology.org/forums/developer-portal.5/) so we can improve the instructions to set up with alternative IDEs :-)

IntelliJ (versions 2020.1 and newer) will load the configuration from gradle after you "Import Gradle Project." The portions of the project configuration not detected from gradle are stored in the `/.idea` directory.

The configuration includes:

* Several run configurations including "TerasologyPC" will be created and immediately be available for execution. These run configurations includes memory settings and the "--homedir" parameter that makes game data files save in the install directory rather than the data directory (easier for development)
* Checkstyle integration with IntelliJ will be enabled. This allows you to catch style violations that clash with the project conventions. Please check these before committing :D
* You'll be able to automatically insert the Project Header (copyright etc) in new files (TODO: More details)
* Annotations, which are used extensively in the project, will get some special consideration when IntelliJ looks at whether code is used or not

The biggest architectural piece of the engine is our [Entity System](Entity-System-Architecture.md) which powers just about everything, much more than just creatures (typically considered "movable entities" or "mobs")


Facades
---------

The engine alone is not executable. It needs to run in some sort of context - "facades" (front-ends) supply that by wrapping the engine.

The most basic one is the "PC Facade" which simply runs the game normally as an application on Windows, Linux, or Macs. This facade is bundled with the engine repo and available right out of the box.

Another one runs the headless game server with added functionality meant to be used from a website (such as chat or a world map). To be able to work with a separate facade you can fetch it automatically:

`groovyw facade get FacadeServer`

Facades are hosted in the MovingBlocks organization on GitHub and have their own custom build scripts.

Rarely should a new facade be needed, one could be made with:

`groovyw facade create DevFacade`

This would create a "facades/DevFacade" sub-project initialized with a few template files and its own Git repo

Modules
---------

If the heart of Terasology is the engine and the facades make up its different faces then the content modules are the different organs - delicious!

While usually "mods" are user-created modifications to a game our modules are a little more fundamental. Modules are containers for code and assets that can be used by game types, mods or other higher-level concepts. Even the systems and various bits of content in the base game are stored in modules that can be enabled, disabled, or replaced.

Modules have access to a limited part of the engine through the Modding API. Each is sandboxed for security. Other than the few engine-embedded modules (which will eventually go away) the modules do not get their own custom build file, instead a template is copied in that builds all modules equally. If you change that instance of the file you can refresh all generated module build files with:

`groovyw module refresh`

A module can define a "Gameplay Template" which is similar to a "modpack". Several such modes are also supplied in the base game like "JoshariasSurvival" which offers some basic survival with a small tech tree.

Your workspace starts with very few modules. Here's an example that fetches JoshariasSurvival module and *all* its dependencies as source:

`groovyw module recurse JoshariasSurvival`

On the next execution of Gradle the new module will be detected and scanned. If it has dependencies on other modules not yet present then Gradle will attempt to resolve them.

* Does the dependency exist as another local source module? If so then use that.
* If not then look for a local binary version present in the modules directory. If you later fetch a source module the binary version will be ignored.
* If a dependency is still not found go to our Artifactory instance and look there. If found the binary is copied to the local modules directory to be available at runtime. This will resolve as a local binary next time.

You can update all your modules to the latest source via the command:

`groovyw module update-all`

You can also create a brand new module and have it filled in with some template content:

`groovyw module create MyNewModule`

This would create "modules/MyNewModule" and initialize a new Git repo for it locally. 

After the next Gradle execution (like "gradlew idea" to regenerate IntelliJ files to know about the new module) you can immediately run the game and see the new module available. It won't do much yet though!

For more on modules see:

* [Module.txt](Module.txt.md) the manifest for a module. Includes description stuff, author info, dependencies, etc.
* [Module Versioning](Release-Modules.md#versioning)
* [Semantic Versioning](http://semver.org) (SemVer).
* Modding Guide
* [Gestalt Modules](https://github.com/MovingBlocks/gestalt/wiki/Modules) - In-depth discussion of Modules (non-Terasology-specific)

Libraries
---------

`libs/` is a directory you can use for including [Locally Developed Libraries](Using-Locally-Developed-Libraries.md).

Other File Types
---------

Beyond code, we have a few major groups of files

* **Game assets** - actual content used by the game. Might be textures, block shapes, models, etc. See Asset Types for more information.
* **[Protobuf](http://code.google.com/p/protobuf)** - this is a framework for structured data provided by Google. It is used to store data in a binary format for efficient transfer over network connection.
* **Module manifests** - as mentioned above. Each module has a manifest that describes it and any dependencies it has. This includes versioning information.

We heavily use [JSON](http://www.json.org/) throughout our projects to store text data / assets, configuration, meta-data, and so on. Rather than using json as the file extension, each asset uses an extension relevant to what it is, such as:

* `.block` = Block definition files. See Block Architecture for more details. Might be outdated at the moment though :-(
* `.shape` = Defines a 3d shape that a block may have. These can be exported from Blender using a bundled addon.
* `.prefab` = "Prefabricated" object, a recipe for creating entities in our entity system. They can also be used to define static information without generating an entity.
* `.texinfo` = Added configuration for how textures should behave.

Common Issues and Other Notes
---------

* If your command window loses focus while working on something that'll pop up an SSH passphrase prompt it may load in the background and be easy to miss. This can leave you with an incomplete sub-project dir
 * Incomplete or otherwise corrupt nested git directories (modules, facades ..) may cause trouble, such as complaining about "cannot find task 'classes' on [name]". When in doubt: Delete and fetch again
 * Same issue may cause tasks to think you already have a particular module locally - again just delete the partial directory and try again
* If you get compile errors on trying to run with the provided configuration immediately after setting up IntelliJ try doing a full Project Rebuild (may be a poor dependency order compilation issue - or IntelliJ just hasn't finished its initial background build)

Related Pages
---------

* [Contributor Quick Start Guide](Contributor-Quick-Start.md)
* [Code Conventions](Code-Conventions.md)
* [Modding API](Modding-API.md)
* [Entity System Architecture](Entity-System-Architecture.md)
* [Shape Architecture](Block-Shapes.md)
