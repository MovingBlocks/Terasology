Modules can depend on other modules, just like Java libraries and projects can have dependencies, usually configured via Gradle or Maven.

For Terasology we've developed a module system later extracted into its [own library "Gestalt Module"](https://github.com/MovingBlocks/gestalt) that our engine project then itself depends on.

## module.txt

Rather than a `pom.xml` or Gradle build file our modules use a simple `module.txt` file to hold all configuration for the module including its dependencies.

For examples simply look at any module in our [module-holding Terasology organization on GitHub](https://github.com/Terasology)

For more details of how it works you can check out the [Gestalt wiki](https://github.com/MovingBlocks/gestalt/wiki)


## Resolving dependencies

You may end up fetching or creating a module that depends on other modules. Again our Gradle setup makes this super easy to handle!

If you fetch a module `X` that has a dependency on module `Y` the next execution of any `gradlew` command will automatically fetch a binary copy of module `Y` including in turn any of *its* dependencies (a "transitive" dependency).

Any binary module dependency will be stored both in your local Gradle cache as well as in `/modules` where the game will use it from at runtime.

If you later fetch the source code for module `Y` it will automatically take precedence over any old binary copy of `Y`
 
You can delete any binary copies of modules at any time then rerun `gradlew` to have them re-fetched.


## Using dependencies

To declare a dependency of your own simply name the module you need in `module.txt` and rerun something like `gradlew idea` - Gradle will handle the rest!

To define a single dependency to the _Gooey_ module in `0.54.*` you would set the dependencies field to:

```json
    "dependencies" : [
            {
                "id" : "Gooey",
                "minVersion" : "0.54.0"
            }
    ],
```

If the first major part of the version number is 0 like in the example, then the exclusive maximum version is one minor version higher. Thus the following would also specify a `0.54.*` dependency:

```json
    "dependencies" : [
            {
                "id" : "Gooey",
                "minVersion" : "0.54.0",
                "maxVersion" : "0.55.0"
            }
    ],
```

The above version range includes the version `0.54.0` (but not `0.54.0-SNAPSHOT`) and excludes the version `0.55.0` and `0.55.0-SNAPSHOT`.
Any other `0.54.*` version like `0.54.2` is also included.

You can include version requirements. Usually this is just whatever is the latest at the time you declare the dependency.


All modules which are used directly should be listed as dependency.
Direct use includes dependencies on code, but also prefabs listing components from other modules.

A dependency can be declared as **optional** by adding `"optional": true` to the dependency information. 
An optional module needs to be present at compile-time, but is not required at runtime. 

```json
    "dependencies" : [
            {
                "id" : "Gooey",
                "minVersion" : "0.54.0",
                "maxVersion" : "0.55.0",
                "optional": true
            }
    ],
```

## Visualizing module dependencies

To inspect the direct and recursive dependencies of one specific or all locally (as source) available modules, you can use the `groovyw module createDependencyDotFile` command from inside the root folder of the Terasology repository.
Append a module name to only create the dependency file for the direct and recursive dependencies of this module. Please note, the modules that are not available locally or only as .jar files are skipped.

The resulting dependency file is stored as `dependency.dot`. DOT is a simple graph description language that allows to define nodes and edges. Based on these, a visualization program like GraphViz can create a visual graph.
If you want to use GraphViz for the visualization, you can [download](https://www.graphviz.org/download/) and install it. Afterwards, you can run `dot -Tsvg <path-to>/dependency.dot > dependency.svg` on the dot file created earlier to create a vector graphic of the dependency graph described in `dependency.dot`. [Other formats](https://graphviz.gitlab.io/_pages/doc/info/output.html) are available as well.
