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
switch(cleanerArgs[0]) {
    case "recurse":
        recurse = true
        println "We're retrieving recursively (all the things depended on too)"
    // We just fall through here to the get logic after setting a boolean
    //noinspection GroovyFallthrough
    case "get":
        println "Preparing to get $itemType"
        if (cleanerArgs.length == 1) {
            def itemString = common.getUserString("Enter what to get - separate multiple with spaces, CapiTaliZation MatterS): ")
            println "User wants: $itemString"
            String[] itemList = itemString.split("\\s+")
            common.retrieve itemList, recurse
        } else {
            // Note: processCustomRemote also drops one of the array elements from cleanerArgs
            cleanerArgs = common.processCustomRemote(cleanerArgs)
            common.retrieve cleanerArgs, recurse
        }
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
        println "List:"
        new File(targetDirectory).eachDir() { dir ->
            String itemName = dir.getPath().substring(8)
            if (!common.excludedItems.contains(itemName)) {
                common.updateItem(itemName)
            } else {
                println "Skipping excluded item $itemName"
            }
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

    default:
        println "UNRECOGNIZED COMMAND '" + cleanerArgs[0] + "' - please try again or use 'groovyw usage' for help"
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
    println "- 'get' - retrieves one or more items in source form (separate with spaces)"
    println "- 'recurse' - retrieves the given item(s) *and* their dependencies in source form (really only for modules)"
    println "- 'create' - creates a new item of the given type."
    println "- 'update' - updates an item (git pulls latest from current origin, if workspace is clean"
    println "- 'update-all' - updates all local items of the given type."
    println "- 'add-remote (item) (name)' - adds a remote (name) to (item) with the default URL."
    println "- 'add-remote (item) (name) (URL)' - adds a remote with the given URL"
    println "- 'list-remotes (item)' - lists all remotes for (item) "
    println ""
    println "Available flags:"
    println "-remote [someRemote]' to clone from an alternative remote, also adding the upstream org (like MovingBlocks) repo as 'origin'"
    println "       Note: 'get' + 'recurse' only. This will override an alternativeGithubHome set via gradle.properties."
    println ""
    println "Example: 'groovyw module get Sample -remote jellysnake' - would retrieve Sample from jellysnake's Sample repo on GitHub."
    println ""
    println "Example: 'groovyw module recurse GooeysQuests Sample' - would retrieve those modules plus their dependencies as source"
    println ""
    println "*NOTE*: Item names are case sensitive. If you add items then `gradlew idea` or similar may be needed to refresh your IDE"
    println ""
    println "If you omit further arguments beyond the sub command you'll be prompted for details"
    println ""
    println "For advanced usage see project documentation. For instance you can provide an alternative GitHub home"
    println "A gradle.properties file (one exists under '/templates' in an engine workspace) can provide such overrides"
    println ""
}
