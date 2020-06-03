// Create an instance of the common script to operate on
File sourceFile = new File("config/groovy/common.groovy")
Class sourceClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(sourceFile)
GroovyObject common = (GroovyObject) sourceClass.newInstance()

// User didn't enter a type (example: `groovyw`)
if (args.length == 0) {
    println "You need to supply some parameters! See 'groovyw usage' for details"
    return
}

// If the user literally requested usage text then print it!
if (args[0] == "usage") {
    printUsage()
    return
}

// User entered a type that doesn't correspond to a script (example: `groovyw fish`)
if (!new File("config/groovy/" + args[0] + ".groovy").exists()) {
    println "That type '" + args[0] + "' did not correspond to a defined utility type script. Typo? See 'groovyw usage'"
    return
}

// User didn't enter a sub-command (example: `groovyw module`)
if (args.length == 1) {
    println "You need to supply a sub-command as well as a type of object to act on. See 'groovyw usage' for details"
    return
}

// Initialize the type target to load in things specific to that type
common.initialize(args[0])
itemType = common.itemType

// At this point we have the type script loaded, so don't need that arg anymore
String[] cleanerArgs = args.drop(1)

excludedItems = common.excludedItems
targetDirectory = common.targetDirectory

def recurse = false
switch (cleanerArgs[0]) {
    case "recurse":
        recurse = true
        println "We're retrieving recursively (all the things depended on too)"
// We just fall through here to the get logic after setting a boolean
//noinspection GroovyFallthrough
    case "get":
        println "Preparing to get $itemType"
        //println "cleanerArgs is $cleanerArgs"
        if (cleanerArgs.length == 1) {
            def itemString = common.getUserString("Enter what to get - separate multiple with spaces, CapiTaliZation MatterS): ")
            println "User wants: $itemString"
            String[] itemList = itemString.split("\\s+")
            common.retrieve itemList, recurse
        } else {
            // Note: processCustomRemote also drops one of the array elements from cleanerArgs
            cleanerArgs = common.processCustomRemote(cleanerArgs)
            ArrayList<String> selectedItems = new ArrayList<String>()

            for (String arg : cleanerArgs) {
                //println "Checking arg $arg"
                if (!arg.contains('*') && !arg.contains('?')) {
                    println "Got into the non-wilcard option to fetch a fully specified item for $arg"
                    selectedItems.add(arg)
                } else {
                    println "Got into the wildcard option to fetch something matching a pattern for $arg, may need to cache first"
                    common.cacheItemList()
                    selectedItems.addAll(common.retrieveAvalibleItemsWithWildcardMatch(arg));
                }
            }
            common.unCacheItemList()

            common.retrieve(((String[]) selectedItems.toArray()), recurse)
        }
        break
    case "get-all":
        println "Preparing to get all modules"
        ArrayList<String> selectedItems = new ArrayList<String>()
        common.cacheItemList()
        selectedItems.addAll(common.retrieveAvalibleItemsWithWildcardMatch("*"));
        common.unCacheItemList()
        common.retrieve(((String[]) selectedItems.toArray()), recurse)
        break
    case "create":
        println "We're doing a create"
        String name
        if (cleanerArgs.length > 2) {
            println "Received more than one argument. Aborting."
            break
        } else if (cleanerArgs.length == 2) {
            // User already submitted the name of what they want to create
            name = cleanerArgs[1]
        } else {
            // User hasn't entered a name yet so request it
            name = getUserString("Enter $itemType name: ")
        }
        println "User wants to create a $itemType named: $name"
        common.createItem(name)
        println "Created $itemType named $name"
        break

    case "update":
        println "We're updating $itemType"
        String[] itemList
        if (cleanerArgs.length == 1) {
            def itemString = common.getUserString("Enter what to update - separate multiple with spaces, CapiTaliZation MatterS): ")
            itemList = itemString.split("\\s+")
        } else {
            itemList = cleanerArgs.drop(1)
        }
        println "List of items to update: $itemList"
        for (String item : itemList) {
            common.updateItem(item)
        }
        break

    case "update-all":
        println "We're updating every $itemType"
        println "List of local entries: ${common.retrieveLocalItems()}"
        for (item in common.retrieveLocalItems()) {
            common.updateItem(item)
        }
        break

    case "add-remote":
        if (cleanerArgs.length == 3) {
            itemName = cleanerArgs[1]
            remoteName = cleanerArgs[2]
            println "Adding git remote for $itemType $itemName"
            common.addRemote(itemName, remoteName)
        } else if (cleanerArgs.length == 4) {
            itemName = cleanerArgs[1]
            remoteName = cleanerArgs[2]
            url = cleanerArgs[3]
            println "Adding git remote for $itemType $itemName"
            common.addRemote(itemName, remoteName, url)
        } else {
            println "Incorrect syntax"
            println "Usage: 'groovyw ${itemType} add-remote [${itemType} name] [remote name]' - adds a git remote 'name' to the stated ${itemType} with default URL."
            println "       'groovyw ${itemType} add-remote [${itemType} name] [remote name] [url]' - adds a git remote 'name' to the stated ${itemType} with the given URL."
        }
        break

    case "list-remotes":
        if (cleanerArgs.length == 2) {
            itemName = cleanerArgs[1]
            println "Listing git remotes for $itemType $itemName"
            common.listRemotes(itemName)
        } else {
            println "Incorrect syntax"
            println "Usage: 'groovyw ${itemType} list-remotes [${itemType} name]' - lists all git remotes for that ${itemType}"
        }
        break

    case "list":
        String[] availableItems = common.retrieveAvailableItems()
        String[] localItems = common.retrieveLocalItems()
        String[] downloadableItems = availableItems.minus(localItems)

        ListFormat listFormat = determineListFormat(cleanerArgs)

        if (cleanerArgs.contains("--local")) {
            printListItems(localItems, listFormat)
        } else {
            println "The following items are available for download:"
            if (availableItems.size() == 0) {
                println "No items available for download."
            } else if (downloadableItems.size() == 0) {
                println "All items are already downloaded."
            } else {
                printListItems(downloadableItems, listFormat)
            }
            println "\nThe following items are already downloaded:"
            if (localItems.size() == 0) {
                println "No items downloaded."
            } else {
                printListItems(localItems, listFormat)
            }
        }
        break

    case "refresh":
        println "We're refreshing Gradle for every $itemType"
        common.refreshGradle()
        break

    case "createDependencyDotFile":
        if (itemType != "module") {
            println "Dependency dot file can only be created for modules"
            break
        }
        String source = ""
        if (cleanerArgs.length == 1 || cleanerArgs[1] == "*") {
            // getting dependency dot file for all modules
            source = "all"
        } else if (cleanerArgs.length > 2) {
            println "Please enter only one module or none to create a dependency dot file showing all modules"
        } else if (cleanerArgs[1].contains('*') || cleanerArgs[1].contains('?')) {
            println "Please enter a fully specified item instead of " + cleanerArgs[1] + " - CapiTaliZation MatterS"
        } else {
            // getting dependency dot file for specified module only
            source = cleanerArgs[1]
        }

        if (source != "") {
            def dependencyFile = new File("dependency.dot")
            dependencyFile.write "digraph moduleDependencies {\n"
            if (source == "all") {
                println "Creating the dependency dot file for all modules as \"dependencies.dot\""
                def modules = new File("modules")
                modules.eachFile {
                    common.writeDependencyDotFileForModule(dependencyFile, it)
                }
            } else {
                println "Creating the dependency dot file for module \"$source\" as \"dependencies.dot\""
                common.writeDependencyDotFileForModule(dependencyFile, new File("modules/$source"))
            }
            dependencyFile.append("}")
        }

        break

        // Makes sure a workspace has one or more modules matching user input, most useful for grabbing whole "module distros"
        case "init":
            // TODO: Move most this code into module.groovy, leave an error here if you use it for something else
            if (cleanerArgs.length == 1) {
                println "[init] Checkout default module distribution"
                String[] targetMods = ["CoreSampleGameplay"]
                common.retrieve(targetMods, false)
            } else if (cleanerArgs.length == 2) {
                def targetModuleDistro = cleanerArgs[1]
                println "[init] Checkout module distribution: '$targetModuleDistro'"
                def targetDistroURL = "https://raw.githubusercontent.com/Terasology/Index/master/distros/" + targetModuleDistro + "/gradle.properties"
                if (!common.isUrlValid(targetDistroURL)) {
                    println "[init] Invalid distribution name: '$targetModuleDistro'"
                    println "[init]     See https://github.com/Terasology/Index/tree/master/distros for available distributions"
                    break
                }
                String distroContent = new URL(targetDistroURL).text
                String moduleSnippet = "extraModules="
                int someIndex = distroContent.indexOf(moduleSnippet)
                if (someIndex != -1) {
                    moduleLine = distroContent.substring((someIndex + moduleSnippet.length()), distroContent.indexOf("\n", someIndex))
                    common.retrieve(moduleLine.split(","), false)
                } else {
                    println "[init] ERROR: Distribution does not contain key: '$moduleSnippet'"
                }
            } else {
                println "[init] Too many arguments! Usage: 'grooyw module init [distribution]'"
                println "[init]     See `groovyw usage` for more information."
            }

        break

    default:
        println "UNRECOGNIZED COMMAND '" + cleanerArgs[0] + "' - please try again or use 'groovyw usage' for help"
}

enum ListFormat {
    DEFAULT, SIMPLE, CONDENSED
}

;

private ListFormat determineListFormat(String[] args) {
    for (listFormat in ListFormat.values()) {
        if (args.contains("-${listFormat.name().toLowerCase()}-list-format"))
            return listFormat;
    }
    return ListFormat.DEFAULT;
}

private void printListItems(String[] items, ListFormat listFormat) {
    def final DEFAULT_FORMAT_CONDENSATION_THRESHOLD = 50
    switch (listFormat) {
        case ListFormat.SIMPLE: printListItemsSimple(items); break;
        case ListFormat.CONDENSED: printListItemsCondensed(items); break;
        default: items.size() < DEFAULT_FORMAT_CONDENSATION_THRESHOLD ?
                printListItemsSimple(items) :
                printListItemsCondensed(items)
    }
}

private void printListItemsSimple(String[] items) {
    for (item in items.sort()) {
        println "$item"
    }
}

private void printListItemsCondensed(String[] items) {
    for (group in items.groupBy { it[0].toUpperCase() }) {
        println "--" + group.key + ": " + group.value.sort().join(", ")
    }
}

/**
 * Simply prints usage information.
 */
def printUsage() {
    println ""
    println "Utility script for interacting with Terasology. General syntax:"
    println "  groovyw (type) (sub-command)"
    println "- 'type' may be module,meta,lib or facade."
    println ""
    println "Available sub-commands:"
    println "- 'init' - retrieves a given module distro, or a default sample source module (modules only)"
    println "- 'get' - retrieves one or more items in source form (separate with spaces)"
    println "- 'get-all' - retrieves all modules that can be found on the configured remote locations"
    println "- 'recurse' - retrieves the given item(s) *and* their dependencies in source form (really only for modules)"
    println "- 'list' - lists items that are available for download or downloaded already."
    println "- 'create' - creates a new item of the given type."
    println "- 'update' - updates an item (git pulls latest from current origin, if workspace is clean"
    println "- 'update-all' - updates all local items of the given type."
    println "- 'add-remote (item) (name)' - adds a remote (name) to (item) with the default URL."
    println "- 'add-remote (item) (name) (URL)' - adds a remote with the given URL"
    println "- 'list-remotes (item)' - lists all remotes for (item) "
    println "- 'refresh' - replaces the Gradle build file for all items of the given type from the latest template"
    println "- 'createDependencyDotFile' - creates a dot file recursively listing dependencies of given locally available module, can be visualized with e.g. graphviz"
    println ""
    println "Available flags:"
    println "'-remote [someRemote]' to clone from an alternative remote, also adding the upstream org (like MovingBlocks) repo as 'origin'"
    println "       Note: 'get' + 'recurse' only. This will override an alternativeGithubHome set via gradle.properties."
    println "'-simple-list-format' to print one item per row for the 'list' sub-command, even for large numbers of items"
    println "'-condensed-list-format' to group items by starting letter for the 'list' sub-command (default with many items)"
    println ""
    println "Example: 'groovyw module init iota' - retrieves all the modules in the Iota module distro from GitHub."
    println "Example: 'groovyw module get Sample -remote jellysnake' - would retrieve Sample from jellysnake's Sample repo on GitHub."
    println "Example: 'groovyw module get-all' - would retrieve all the modules in the Terasology organisation on GitHub."
    println "Example: 'groovyw module get Sa??l*' - would retrieve all the modules in the Terasology organisation on GitHub" +
            " that start with \"Sa\", have any two characters after that, then an \"l\" and then end with anything else." +
            " This should retrieve the Sample repository from the Terasology organisation on GitHub."
    println ""
    println "*NOTE*: On UNIX platforms (MacOS and Linux), the wildcard arguments must be escaped with single quotes e.g. groovyw module get '*'."
    println ""
    println "Example: 'groovyw module recurse GooeysQuests Sample' - would retrieve those modules plus their dependencies as source"
    println "Example: 'groovyw lib list' - would list library projects compatible with being embedded in a Terasology workspace"
    println "Example: 'groovyw module createDependencyDotFile JoshariasSurvival' - would create a dot file with JS' dependencies and all their dependencies - if locally available"
    println ""
    println "*NOTE*: Item names are case sensitive. If you add items then `gradlew idea` or similar may be needed to refresh your IDE"
    println ""
    println "If you omit further arguments beyond the sub-command you'll be prompted for details"
    println ""
    println "For advanced usage see project documentation. For instance you can provide an alternative GitHub home"
    println "A gradle.properties file (one exists under '/templates' in an engine workspace) can provide such overrides"
    println ""
}
