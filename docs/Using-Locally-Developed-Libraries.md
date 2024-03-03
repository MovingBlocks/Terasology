Terasology has close relationships with some of its dependencies, such as [TeraNUI](https://github.com/MovingBlocks/TeraNUI) for the user interface and [gestalt](https://github.com/MovingBlocks/gestalt) for its module system.

When you're developing a feature in one of those libraries and you want to see how Terasology runs with it, that's when to make use of Gradle's [composite builds](https://docs.gradle.org/current/userguide/composite_builds.html#composite_builds) feature.

Let's look at [TeraNUI's developer documentation](https://github.com/MovingBlocks/TeraNUI/#-development) for an example:

> You can get the TeraNUI library as source into your local Terasology workspace as follows:
>
> `groovyw lib get TeraNUI`
>
> This will place the TeraNUI source code into `/libs/TeraNUI`. Please note that you may need to reload/refresh your gradle workspace in IntelliJ IDEA.

`groovyw lib get` is a helpful shortcut for certain common dependencies, but know that _any_ gradle project checked out to a subdirectory of `/libs/` will be included in the composite build.

Make sure to `git switch` in that directory to the branch of that project you're working with.

## How do I know the library's build is being included?

Run gradlew with the `--info` flag and look for this in the output:

> libs/TeraNUI will be included in the composite build.

You can also build a report of your dependencies:

```shell
gradlew :engine:htmlDependencyReport
```

in which dependencies filled by subprojects or included builds will be marked with "project" like this:

>  org.terasology:gestalt-module:5.1.5 ➡ project :gestalt:gestalt-module

Or you can check your build's list of projects on its [build scan](https://scans.gradle.com/).


## Terminology

Gradle has a lot of terminology around how builds are organized. We'll break it down with some concrete examples here:

* A **project** is defined by a [settings.gradle](https://github.com/MovingBlocks/Terasology/blob/develop/settings.gradle). Our project is defined in the top-level directory of our git repository, and it is [named `Terasology`](https://github.com/MovingBlocks/Terasology/blob/5cfb0c66f5457d16cffe1246a697314ccc1cd328/settings.gradle#L3).
* Terasology is the **root project** of a [**multi-project build**](https://docs.gradle.org/current/userguide/multi_project_builds.html). Its **subprojects** are things such as [`engine`](https://github.com/MovingBlocks/Terasology/blob/develop/engine/build.gradle) and the [facades](https://github.com/MovingBlocks/Terasology/blob/develop/facades/PC/build.gradle.kts).

  Subprojects can be separate from one another but they are always built within the _root project;_ `engine` is not meant to be built on its own. The root project may configure its subprojects and define dependencies between them.
* TeraNUI is an independent **project**. It can be built without any reference to Terasology whatsoever. When we want our Terasology build to make use of a local TeraNUI build, TeraNUI is the **included build**. The technique of combining builds this way is called a **composite build**.

  A _composite build_ is any build that involves _included builds_, there's nothing else we need to do to make it a “composite.”

Subprojects and included builds are similar in many respects, but the root project isn't able to meddle with included builds to the same extent as it can with its own subprojects.


## Implementation Details

Our [`libs/subprojects.settings.gradle.kts`](https://github.com/MovingBlocks/Terasology/blob/develop/libs/subprojects.settings.gradle.kts) checks each subdirectory
to see if it contains files that look like a Gradle root project, and if it finds them it [adds that directory as an included build](https://docs.gradle.org/current/dsl/org.gradle.api.initialization.Settings.html#org.gradle.api.initialization.Settings:includeBuild(java.lang.Object)).
