println "Hi from util.groovy. Args are $args"

// Import all the methods into a Groovy Object
File sourceFile = new File("config/groovy/functions.groovy")
Class centralClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(sourceFile)
GroovyObject fxScript = (GroovyObject) centralClass.newInstance()

// User didn't enter a type (example: `groovyw`)
if (args.length == 0) {
    println "You need to supply some parameters! See 'groovyw usage' for details"
    return
}

// If the user literally requested usage text then print it!
if (args[0] == "usage") {
    fxScript.printUsage()
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

//Initialize the object.
fxScript.initialize(args[0])
// At this point we have the type script loaded, so don't need that arg anymore
String[] cleanerArgs = args.drop(1)
itemType = fxScript.itemType
println "cleanerArgs is now " + cleanerArgs
excludedDependencies = fxScript.excludedDependencies
targetDirectory = fxScript.targetDirectory

//Recursion is only for modules.
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
            def itemString = fxScript.getUserString("Enter what to get - separate multiple with spaces, CapiTaliZation MatterS): ")
            println "User wants: $itemString"
            String[] itemList = itemString.split("\\s+")
            fxScript.retrieve itemList, recurse
        } else {
            // Note: processCustomRemote also drops one of the array elements from cleanerArgs
            cleanerArgs = fxScript.processCustomRemote(cleanerArgs)
            fxScript.retrieve cleanerArgs, recurse
        }
        break
    case "create":
        println "We're doing a create"
        String name
        if (cleanerArgs.length > 2) {
            println "Received more than one argument. Aborting."
            break
        } else if (cleanerArgs.length == 2) {
            name = cleanerArgs[1]
        } else {
            name = getUserString("Enter $itemType name: ")
        }
        println "User wants to create a $itemType named: $name"
        fxScript.createItem(name)
        println "Created $itemType named $name"
        break
    case "update":
        println "We're updating $itemType"
        String[] itemList
        if (cleanerArgs.length == 1) {
            def itemString = fxScript.getUserString("Enter what to update - separate multiple with spaces, CapiTaliZation MatterS): ")
            itemList = itemString.split("\\s+")
        } else {
            itemList = cleanerArgs.drop(1)
        }
        println "List of items to update: $itemList"
        for (String item : itemList) {
            fxScript.updateItem(item)
        }
        break
    case "update-all":
        println "We're updating every $itemType"
        println "List:"
        new File(targetDirectory).eachDir() { dir ->
            String itemName = dir.getPath().substring(8)
            if (!excludedDependencies.contains(itemName)) {
                fxScript.updateItem(itemName)
            }
        }
        break
    case "add-remote":
        if (cleanerArgs.length == 3) {
            itemName = cleanerArgs[1]
            remoteName = cleanerArgs[2]
            println "Adding Remote for $itemType $itemName"
            fxScript.addRemote(itemName, remoteName)
        } else if (cleanerArgs.length == 4) {
            itemName = cleanerArgs[1]
            remoteName = cleanerArgs[2]
            url = cleanerArgs[3]
            println "Adding Remote for $itemType $itemName"
            fxScript.addRemote(itemName, remoteName, url)
        } else {
            println "Incorrect Syntax"
            println "Usage: 'add-remote (${itemType}) (name)' - adds a remote (name) to the ${itemType} with default URL."
            println "       'add-remote (${itemType}) (name) (url)' - adds a remote (name) to the ${itemType} with the given URL."
        }
        break
    case "list-remotes":
        if (cleanerArgs.length == 2) {
            itemName = cleanerArgs[1]
            println "Listing Remotes for $itemType $itemName"
            fxScript.listRemotes(itemName)
        } else {
            println "Incorrect Syntax"
            println "Usage: 'list-remotes (${itemType})' - lists all remotes for (${itemType})"
        }
        break
    default:
        println "UNRECOGNIZED COMMAND '" + cleanerArgs[0] + "' - please try again or use 'groovyw usage' for help"
}
