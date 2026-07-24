// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.gradology.tooling

import java.io.File

/**
 * Kotlin port of config/groovy/util.groovy - parses and dispatches
 * `gradle terasology --type=... --command=... --params="..."` (previously `groovyw <type>
 * <sub-command> <params...>`) to the [TerasologyTooling] engine.
 */
object Cli {

    /** Single source of truth for the sub-command dispatch below and TerasologyToolingTask's --command @OptionValues. */
    val subCommands: List<String> = listOf(
        "init", "get", "get-all", "recurse", "list", "create", "update", "update-all",
        "add-remote", "list-remotes", "refresh", "createDependencyDotFile"
    )

    fun run(args: List<String>, rootDir: File) {
        if (args.isEmpty()) {
            println("You need to supply some parameters! See 'gradle terasology --usage' for details")
            return
        }

        if (args[0] == "usage") {
            printUsage()
            return
        }

        val itemType = ItemType.byName[args[0]]
        if (itemType == null) {
            println("That type '${args[0]}' did not correspond to a defined utility type. Typo? See 'gradle terasology --usage'")
            return
        }

        if (args.size == 1) {
            println("You need to supply a sub-command as well as a type of object to act on. See 'gradle terasology --usage' for details")
            return
        }

        val common = TerasologyTooling(itemType, rootDir)
        val cleanerArgs = args.drop(1)

        when (cleanerArgs[0]) {
            "recurse", "get" -> {
                val recurse = cleanerArgs[0] == "recurse"
                if (recurse) {
                    println("We're retrieving recursively (all the things depended on too)")
                }
                println("Preparing to get ${itemType.itemType}")
                if (cleanerArgs.size == 1) {
                    val itemString = common.getUserString("Enter what to get - separate multiple with spaces, CapiTaliZation MatterS): ")
                    println("User wants: $itemString")
                    common.retrieve(itemString.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }, recurse)
                } else {
                    val remainingArgs = common.processCustomRemote(cleanerArgs)
                    val selectedItems = mutableListOf<String>()
                    for (arg in remainingArgs) {
                        if (!arg.contains('*') && !arg.contains('?')) {
                            println("Got into the non-wilcard option to fetch a fully specified item for $arg")
                            selectedItems += arg
                        } else {
                            println("Got into the wildcard option to fetch something matching a pattern for $arg, may need to cache first")
                            selectedItems += common.retrieveAvailableItemsWithWildcardMatch(arg)
                        }
                    }
                    common.uncacheItemList()
                    common.retrieve(selectedItems, recurse)
                }
            }

            "get-all" -> {
                println("Preparing to get all ${itemType.itemType}s")
                val selectedItems = common.retrieveAvailableItemsWithWildcardMatch("*")
                common.uncacheItemList()
                common.retrieve(selectedItems, false)
            }

            "create" -> {
                if (cleanerArgs.size > 2) {
                    println("Received more than one argument. Aborting.")
                } else {
                    val name = if (cleanerArgs.size == 2) cleanerArgs[1] else common.getUserString("Enter ${itemType.itemType} name: ")
                    println("User wants to create a ${itemType.itemType} named: $name")
                    common.createItem(name)
                    println("Created ${itemType.itemType} named $name")
                }
            }

            "update" -> {
                println("We're updating ${itemType.itemType}")
                val itemList = if (cleanerArgs.size == 1) {
                    common.getUserString("Enter what to update - separate multiple with spaces, CapiTaliZation MatterS): ")
                        .trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
                } else {
                    cleanerArgs.drop(1)
                }
                println("List of items to update: $itemList")
                for (item in itemList) {
                    common.updateItem(item)
                }
            }

            "update-all" -> {
                println("We're updating every ${itemType.itemType}")
                val localItems = common.retrieveLocalItems()
                println("List of local entries: $localItems")
                val skipRecentlyUpdated = cleanerArgs.contains("-skip-recently-updated")
                for (item in localItems) {
                    common.updateItem(item, skipRecentlyUpdated)
                }
            }

            "add-remote" -> {
                when (cleanerArgs.size) {
                    3 -> {
                        println("Adding git remote for ${itemType.itemType} ${cleanerArgs[1]}")
                        common.addRemote(cleanerArgs[1], cleanerArgs[2])
                    }
                    4 -> {
                        println("Adding git remote for ${itemType.itemType} ${cleanerArgs[1]}")
                        common.addRemote(cleanerArgs[1], cleanerArgs[2], cleanerArgs[3])
                    }
                    else -> {
                        println("Incorrect syntax")
                        println("Usage: gradle terasology --type=${itemType.itemType} --command=add-remote --params=\"[${itemType.itemType} name] [remote name]\" - adds a git remote 'name' to the stated ${itemType.itemType} with default URL.")
                        println("       gradle terasology --type=${itemType.itemType} --command=add-remote --params=\"[${itemType.itemType} name] [remote name] [url]\" - adds a git remote 'name' to the stated ${itemType.itemType} with the given URL.")
                    }
                }
            }

            "list-remotes" -> {
                if (cleanerArgs.size == 2) {
                    println("Listing git remotes for ${itemType.itemType} ${cleanerArgs[1]}")
                    common.listRemotes(cleanerArgs[1])
                } else {
                    println("Incorrect syntax")
                    println("Usage: gradle terasology --type=${itemType.itemType} --command=list-remotes --params=\"[${itemType.itemType} name]\" - lists all git remotes for that ${itemType.itemType}")
                }
            }

            "list" -> {
                val availableItems = common.retrieveAvailableItems()
                val localItems = common.retrieveLocalItems()
                val downloadableItems = availableItems - localItems.toSet()
                val listFormat = determineListFormat(cleanerArgs)

                if (cleanerArgs.contains("--local")) {
                    printListItems(localItems, listFormat)
                } else {
                    println("The following items are available for download:")
                    when {
                        availableItems.isEmpty() -> println("No items available for download.")
                        downloadableItems.isEmpty() -> println("All items are already downloaded.")
                        else -> printListItems(downloadableItems, listFormat)
                    }
                    println("\nThe following items are already downloaded:")
                    if (localItems.isEmpty()) println("No items downloaded.") else printListItems(localItems, listFormat)
                }
            }

            "refresh" -> {
                println("We're refreshing Gradle for every ${itemType.itemType}")
                common.refreshGradle()
            }

            "createDependencyDotFile" -> {
                if (itemType.itemType != "module") {
                    println("Dependency dot file can only be created for modules")
                } else {
                    val source = when {
                        cleanerArgs.size == 1 || cleanerArgs[1] == "*" -> "all"
                        cleanerArgs.size > 2 -> {
                            println("Please enter only one module or none to create a dependency dot file showing all modules")
                            ""
                        }
                        cleanerArgs[1].contains('*') || cleanerArgs[1].contains('?') -> {
                            println("Please enter a fully specified item instead of ${cleanerArgs[1]} - CapiTaliZation MatterS")
                            ""
                        }
                        else -> cleanerArgs[1]
                    }
                    if (source != "") {
                        val dependencyFile = File(rootDir, "dependency.dot")
                        dependencyFile.writeText("digraph moduleDependencies {\n")
                        if (source == "all") {
                            println("Creating the dependency dot file for all modules as \"dependencies.dot\"")
                            common.targetDirectory.listFiles()?.forEach { common.writeDependencyDotFileForModule(dependencyFile, it) }
                        } else {
                            println("Creating the dependency dot file for module \"$source\" as \"dependencies.dot\"")
                            common.writeDependencyDotFileForModule(dependencyFile, File(common.targetDirectory, source))
                        }
                        dependencyFile.appendText("}")
                    }
                }
            }

            "init" -> {
                // TODO: Move most of this into module-specific config, leave an error here if used for something else.
                when (cleanerArgs.size) {
                    1 -> {
                        println("[init] Checkout default module distribution")
                        common.retrieve(listOf("CoreSampleGameplay"), false)
                    }
                    2 -> {
                        val targetModuleDistro = cleanerArgs[1]
                        println("[init] Checkout module distribution: '$targetModuleDistro'")
                        val targetDistroUrl = "https://raw.githubusercontent.com/Terasology/Index/master/distros/$targetModuleDistro/gradle.properties"
                        if (!GitHubApi.isUrlValid(targetDistroUrl)) {
                            println("[init] Invalid distribution name: '$targetModuleDistro'")
                            println("[init]     See https://github.com/Terasology/Index/tree/master/distros for available distributions")
                        } else {
                            val distroContent = GitHubApi.getText(targetDistroUrl)
                            val moduleSnippet = "extraModules="
                            val someIndex = distroContent.indexOf(moduleSnippet)
                            if (someIndex != -1) {
                                val lineEnd = distroContent.indexOf("\n", someIndex).let { if (it == -1) distroContent.length else it }
                                val moduleLine = distroContent.substring(someIndex + moduleSnippet.length, lineEnd)
                                common.retrieve(moduleLine.split(","), false)
                            } else {
                                println("[init] ERROR: Distribution does not contain key: '$moduleSnippet'")
                            }
                        }
                    }
                    else -> {
                        println("[init] Too many arguments! Usage: gradle terasology --type=module --command=init --params=\"[distribution]\"")
                        println("[init]     See `gradle terasology --usage` for more information.")
                    }
                }
            }

            else -> println("UNRECOGNIZED COMMAND '${cleanerArgs[0]}' - please try again or use 'gradle terasology --usage' for help")
        }
    }

    private enum class ListFormat { DEFAULT, SIMPLE, CONDENSED }

    private fun determineListFormat(args: List<String>): ListFormat =
        ListFormat.entries.find { args.contains("-${it.name.lowercase()}-list-format") } ?: ListFormat.DEFAULT

    private fun printListItems(items: List<String>, listFormat: ListFormat) {
        val defaultFormatCondensationThreshold = 50
        when (listFormat) {
            ListFormat.SIMPLE -> printListItemsSimple(items)
            ListFormat.CONDENSED -> printListItemsCondensed(items)
            ListFormat.DEFAULT -> if (items.size < defaultFormatCondensationThreshold) printListItemsSimple(items) else printListItemsCondensed(items)
        }
    }

    private fun printListItemsSimple(items: List<String>) {
        for (item in items.sorted()) {
            println(item)
        }
    }

    private fun printListItemsCondensed(items: List<String>) {
        for ((letter, group) in items.groupBy { it.take(1).uppercase() }.toSortedMap()) {
            println("--$letter: " + group.sorted().joinToString(", "))
        }
    }

    private fun printUsage() {
        println(
            """
            Utility for interacting with Terasology. General syntax:
              gradle terasology --type=(type) --command=(sub-command) [--params="..."]
            - '--type' may be module, meta, lib or facade.
            - All arguments are named Gradle options (not positional) - see 'gradle help --task terasology'
              for full option docs, including tab-completable values for --type and --command.

            Available --command values:
            - 'init' - retrieves a given module distro, or a default sample source module (modules only)
            - 'get' - retrieves one or more items in source form (put multiple, space-separated, in --params)
            - 'get-all' - retrieves all modules that can be found on the configured remote locations
            - 'recurse' - retrieves the given item(s) *and* their dependencies in source form (really only for modules)
            - 'list' - lists items that are available for download or downloaded already.
            - 'create' - creates a new item of the given type.
            - 'update' - updates an item (git pulls latest from current origin, if workspace is clean)
            - 'update-all' - updates all local items of the given type.
            - 'add-remote' with --params="(item) (name)" - adds a remote (name) to (item) with the default URL.
            - 'add-remote' with --params="(item) (name) (URL)" - adds a remote with the given URL
            - 'list-remotes' with --params="(item)" - lists all remotes for (item)
            - 'refresh' - replaces the Gradle build file for all items of the given type from the latest template
            - 'createDependencyDotFile' - creates a dot file recursively listing dependencies of given locally available module, can be visualized with e.g. graphviz

            Available flags (put these inside --params, space-separated along with any item names):
            '-remote [someRemote]' to clone from an alternative remote, also adding the upstream org (like MovingBlocks) repo as 'origin'
                   Note: 'get' + 'recurse' only. This will override an alternativeGithubHome set via gradle.properties.
            '-simple-list-format' to print one item per row for the 'list' sub-command, even for large numbers of items
            '-condensed-list-format' to group items by starting letter for the 'list' sub-command (default with many items)
            '-skip-recently-updated' (Only for update-all) to skip updating modules that have already been updated within 10 minutes

            Example: gradle terasology --type=module --command=init --params="iota" - retrieves all the modules in the Iota module distro from GitHub.
            Example: gradle terasology --type=module --command=get --params="Sample -remote jellysnake" - would retrieve Sample from jellysnake's Sample repo on GitHub.
            Example: gradle terasology --type=module --command=get-all - would retrieve all the modules in the Terasology organisation on GitHub.
            Example: gradle terasology --type=module --command=get --params="Sa??l*" - would retrieve all the modules in the Terasology organisation on GitHub
             that start with "Sa", have any two characters after that, then an "l" and then end with anything else.
             This should retrieve the Sample repository from the Terasology organisation on GitHub.

            *NOTE*: wildcard patterns inside --params must be quoted/escaped so your shell doesn't expand them itself.

            Example: gradle terasology --type=module --command=recurse --params="GooeysQuests Sample" - would retrieve those modules plus their dependencies as source
            Example: gradle terasology --type=lib --command=list - would list library projects compatible with being embedded in a Terasology workspace
            Example: gradle terasology --type=module --command=createDependencyDotFile --params="JoshariasSurvival" - would create a dot file with JS' dependencies and all their dependencies - if locally available

            *NOTE*: Item names are case sensitive. If you add items then `gradle idea` or similar may be needed to refresh your IDE

            If you omit --params where the sub-command needs it, you'll be prompted for details interactively

            For advanced usage see project documentation. For instance you can provide an alternative GitHub home
            A gradle.properties file (one exists under '/templates' in an engine workspace) can provide such overrides

            This is a Kotlin/Gradle-native port of the older `groovyw` script - same commands, same behavior,
            but runs inside Gradle itself (as real, named, tab-completable Gradle task options) rather than
            bootstrapping a separate Groovy/Grape environment.
            """.trimIndent()
        )
    }
}
