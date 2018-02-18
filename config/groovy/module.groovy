// Import all the methods into a Groovy Object
File sourceFile = new File("config/groovy/functions.groovy")
Class centralClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(sourceFile)
GroovyObject fxScript = (GroovyObject) centralClass.newInstance()

//Initialize the object.
fxScript.initialize("module")
itemType = fxScript.itemType
excludedDependencies = fxScript.excludedDependencies
targetDirectory = fxScript.targetDirectory

if(args.length == 0){
        fxScript.printUsage()
} else {
    //Recursion is only for modules.
    def recurse = false
    switch(args[0]) {
    	case "recurse":
            recurse = true
            println "We're retrieving recursively (all the things depended on too)"
        // We just fall through here to the get logic after setting a boolean
        //noinspection GroovyFallthrough
        case "get":
            println "Preparing to get $itemType"
            if (args.length == 1) {
                def itemString = fxScript.getUserString("Enter $itemType Name(s - separate multiple with spaces, CapiTaliZation MatterS): ")
                println "User wants: $itemString"
                String[] itemList = itemString.split("\\s+")
                fxScript.retrieve itemList, recurse
            } else {
                args = fxScript.processCustomRemote(args)
                fxScript.retrieve args, recurse
            }
            break
        case "create":
            println "We're doing a create"
            String name
            if (args.length > 2) {
                println "Received more than one argument. Aborting."
                break
            } else if (args.length == 2) {
                name = args[1]
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
            if (args.length == 1) {
                def itemString = getUserString("Enter $itemType Name(s - separate multiple with spaces, CapiTaliZation MatterS): ")
                itemList = itemString.split("\\s+")
            } else {
                itemList = args.drop(1)
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
            if (args.length == 3) {
                itemName = args[1]
                remoteName = args[2]
                println "Adding Remote for $itemType $itemName"
                fxScript.addRemote(itemName, remoteName)
            } else if (args.length == 4) {
                itemName = args[1]
                remoteName = args[2]
                url = args[3]
                println "Adding Remote for $itemType $itemName"
                fxScript.addRemote(itemName, remoteName, url)
            } else {
                println "Incorrect Syntax"
                println "Usage: 'add-remote (${itemType}) (name)' - adds a remote (name) to the ${itemType} with default URL."
                println "       'add-remote (${itemType}) (name) (url)' - adds a remote (name) to the ${itemType} with the given URL."
            }
            break
        case "list-remotes":
            if (args.length == 2) {
                itemName = args[1]
                println "Listing Remotes for $itemType $itemName"
                fxScript.listRemotes(itemName)
            } else {
                println "Incorrect Syntax"
                println "Usage: 'list-remotes (${itemType})' - lists all remotes for (${itemType})"
            }
            break
        default:
            println "UNRECOGNIZED COMMAND - please try again or use 'groovyw util usage' for help"
    }
}