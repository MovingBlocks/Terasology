@GrabResolver(name = 'jcenter', root = 'http://jcenter.bintray.com/')
@Grab(group = 'org.ajoberstar', module = 'grgit', version = '1.9.3')
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.Remote
import groovy.json.JsonSlurper

/**
 * Default settings for modules.
 * githubRepo stores the Repository name.
 * targetDirectory stores the working directory.
 * itemType is used to generalize the script.
 */
githubRepo="Terasology"
targetDirectory="modules"
itemType="Module"
gitHubHome="Terasology"
/**
 * Main logic for changing the item type.
 * @param type the type to be initialized
 */
 def initialize(String type) {
    //Load gradlew properties to check for github settings.
    Properties properties = new Properties()
    new File("gradle.properties").withInputStream {
        properties.load(it)
    }
    if (type == "meta") {
        githubRepo="MetaTerasology"
        targetDirectory="metas"
        itemType="Meta"
        //Looking for alternative Github Home (Meta) in gradle properties.
        githubHome=properties.alternativeGithubMetaHome ?: githubRepo
    } else {
        if (type == "lib") {
            githubRepo="MovingBlocks"
            targetDirectory="libs"
            itemType="Library"
        }
        else if (type == "facade") {
            githubRepo="MovingBlocks"
            targetDirectory="facades"
            itemType="Facade"
        }
        //Looking for alternative Github Home in gradle properties.
        githubHome=properties.alternativeGithubHome ?: githubRepo
    }
}

// For keeping a list of items retrieved so far
itemsRetrieved = []

// Dependencies we don't want to retrieve as they live in the main Terasology repo
excludedDependencies = ["engine", "Core", "CoreSampleGameplay", "BuilderSampleGameplay"]

/**
 * Accepts input from the user, showing a descriptive prompt.
 * @param prompt the prompt to show the user
 */
def getUserString(String prompt) {
    println('\n*** ' + prompt + '\n')
    def reader = new BufferedReader(new InputStreamReader(System.in))
    return reader.readLine()
}

/**
 * Tests a URL via a HEAD request (no body) to see if it is valid
 * @param url the URL to test
 * @return boolean indicating whether the URL is valid (code 200) or not
 */
boolean isUrlValid(String url) {
    def code = new URL(url).openConnection().with {
        requestMethod = 'HEAD'
        connect()
        responseCode
    }
    return code.toString() == "200"
}

/**
 * Primary entry point for retrieving items, kicks off recursively if needed.
 * @param items the items we want to retrieve
 * @param recurse whether to also retrieve dependencies of the desired modules (only for modules)
 */
def retrieve(String[] items, boolean recurse) {
    println "Now inside retrieve, user (recursively? $recurse) wants: $items"
    for (String itemName : items) {
        println "Starting retrieval for $itemType $itemName, are we recursing? $recurse"
        println "Retrieved so far: $itemsRetrieved"
        retrieveItem(itemName, recurse)
    }
}

/**
 * Retrieves a single item via Git Clone. Considers whether it exists locally first or if it has already been retrieved this execution.
 * @param itemName the target item to retrieve
 * @param recurse whether to also retrieve its dependencies (if so then recurse back into retrieve) (only for modules)
 */
def retrieveItem(String itemName, boolean recurse) {
    File targetDir = new File("$targetDirectory"+"/$itemName")
    println "Request to retrieve $itemType $itemName would store it at $targetDir - exists? " + targetDir.exists()
    if (targetDir.exists()) {
        println "That $itemType already had an existing directory locally. If something is wrong with it please delete and try again"
        itemsRetrieved << itemName
    } else if (itemsRetrieved.contains(itemName)) {
        println "We already retrieved $itemName - skipping"
    } else {
        itemsRetrieved << itemName
        //Special Logic for target URLs. If type is facade, 'Facade' is used as prefix. Same for  Libraries and 'Tera'.
        def targetUrl = "https://github.com/${githubHome}/${itemName}"
        if (itemType=="Library"){
            targetUrl = "https://github.com/${githubHome}/Tera${itemName}"
        }
        else if (itemType=="Facade"){
            targetUrl = "https://github.com/${githubHome}/Facade${itemName}"
        }
        if (!isUrlValid(targetUrl)) {
            println "Can't retrieve $itemType from $targetUrl - URL appears invalid. Typo? Not created yet?"
            return
        }
        println "Retrieving $itemType $itemName from $targetUrl"
        if (githubHome != githubRepo) {
            println "Doing a retrieve from a custom remote: $githubHome - will name it as such plus add the $githubRepo remote as 'origin'"
            Grgit.clone dir: targetDir, uri: targetUrl, remote: githubHome
            println "Primary clone operation complete, about to add the 'origin' remote for the $githubRepo org address"
            addRemote(module, "origin", "https://github.com/${githubRepo}/${module}")
        } else {
            Grgit.clone dir: targetDir, uri: targetUrl
        }
        File targetBuildGradle = new File(targetDir, 'build.gradle')
        targetBuildGradle.delete()
        targetBuildGradle << new File('templates/build.gradle').text
        //If the type is module, module.text is retrieved.
        if(targetDirectory=="modules") {
            File moduleManifest = new File(targetDir, 'module.txt')
            if (!moduleManifest.exists()) {
                def moduleText = new File("templates/module.txt").text
                moduleManifest << moduleText.replaceAll('MODULENAME', module)
                println "WARNING: Module $module did not have a module.txt! One was created, please review and submit to GitHub"
            }
            //This is only for modules.
            if (recurse) {
                def foundDependencies = readModuleDependencies(new File(targetDir, "module.txt"))
                if (foundDependencies.length == 0) {
                    println "Module $module did not appear to have any dependencies we need to worry about"
                } else {
                    println "Module $module has the following module dependencies we care about: $foundDependencies"
                    String[] uniqueDependencies = foundDependencies - itemsRetrieved
                    println "After removing dupes already retrieved we have the remaining dependencies left: $uniqueDependencies"
                    if (uniqueDependencies.length > 0) {
                        retrieve(uniqueDependencies, true)
                    }
                }
            }
        }
    }
}

/**
 * Reads a given module info file to figure out which if any dependencies it has. Filters out any already retrieved.
 * This method is only for modules.
 * @param targetModuleInfo the target file to check (a module.txt file or similar)
 * @return a String[] containing the next level of dependencies, if any
 */
String[] readModuleDependencies(File targetModuleInfo) {
    def qualifiedDependencies = []
    if (!targetModuleInfo.exists()) {
        println "The module info file did not appear to exist - can't calculate dependencies"
        return qualifiedDependencies
    }
    def slurper = new JsonSlurper()
    def moduleConfig = slurper.parseText(targetModuleInfo.text)
    for (dependency in moduleConfig.dependencies) {
        if (excludedDependencies.contains(dependency.id)) {
            println "Skipping listed dependency $dependency as it is in the exclude list (shipped with primary project)"
        } else {
            println "Accepting listed dependency $dependency"
            qualifiedDependencies << dependency.id
        }
    }
    return qualifiedDependencies
}

/**
 * Creates a new item with the given name and adds the necessary .gitignore,
 * build.gradle and module.txt files.
 * module.txt is only for modules.
 * @param itemName the name of the item to be created
 */
def createItem(String itemName) {
    File targetDir = new File("${targetDirectory}/$itemName")
    if (targetDir.exists()) {
        println "Target directory already exists. Aborting."
        return
    }
    println "Creating target directory"
    targetDir.mkdir()
    println "Creating .gitignore"
    File gitignore = new File(targetDir, ".gitignore")
    def gitignoreText = new File("templates/.gitignore").text
    gitignore << gitignoreText
    //If the type is facade, facades.gradle is created else build.gradle is created.
    if (targetDirectory=="facades"){
        println "Creating facades.gradle"
        File facadesGradle = new File(targetDir, "facades.gradle")
        def facadesGradleText = new File("templates/facades.gradle").text
        facadesGradle << facadesGradleText
    } else {
        println "Creating build.gradle"
        File buildGradle = new File(targetDir, "build.gradle")
        def buildGradleText = new File("templates/build.gradle").text
        buildGradle << buildGradleText
    }
    //If the type is module, module.text is created.
    if (targetDirectory=="modules"){
        println "Creating module.txt"
        File moduleManifest = new File(targetDir, "module.txt")
        def moduleText = new File("templates/module.txt").text
        moduleManifest << moduleText.replaceAll('MODULENAME', itemName)
    }
    Grgit.init dir: targetDir, bare: false
    addRemote(itemName, "origin", "https://github.com/${githubRepo}/${itemName}.git")
}

/**
 * Update a given item.
 * @param itemName the name of the item to update
 */
def updateItem(String itemName) {
    println "Attempting to update $itemType $itemName"
    File targetDir = new File("${targetDirectory}/${itemName}")
    if (!targetDir.exists()) {
        println "$itemType \"$itemName\" not found"
        return
    }
    def itemGit = Grgit.open(dir: targetDir)
    def clean = itemGit.status().clean
    println "Is \"$itemName\" clean? $clean"
    if (!clean) {
        println "$itemType has uncommitted changes. Aborting."
        return
    }

    println "Updating $itemType $itemName"
    itemGit.pull remote: "origin"
}

/**
 * List all existing Git remotes for a given item.
 * @param itemName the module to list remotes for
 */
def listRemotes(String itemName) {
    File moduleExistence = new File("${targetDirectory}/$itemName")
    if (!moduleExistence.exists()) {
        println "$itemType '$itemName' not found. Typo? Or run 'groovyw util $itemType get $itemName' first"
        return
    }
    def remoteGit = Grgit.open(dir: "${targetDirectory}/${itemName}")
    def remote = remoteGit.remote.list()
    x = 1
    for (Remote item : remote) {
        println(x + " " + item.name + " " + "(" + item.url + ")")
        x += 1
    }
}

/**
 * Add new Git remotes for the given items, all using the same remote name.
 * @param items the items to add remotes for
 * @param name the name to use for all the Git remotes
 */
def addRemotes(String[] items, String name) {
    for (String item : items) {
        addRemote(item, name)
    }
}

/**
 * Add a new Git remote for the given item, deducing a standard URL to the repo.
 * @param itemName the item to add the remote for
 * @param remoteName the name to give the new remote
 */
def addRemote(String itemName, String remoteName) {
    addRemote(itemName, remoteName, "https://github.com/$remoteName/$itemName" + ".git")
}

/**
 * Add a new Git remote for the given item.
 * @param itemName the item to add the remote for
 * @param remoteName the name to give the new remote
 * @param URL address to the remote Git repo
 */
def addRemote(String itemName, String remoteName, String url) {
    File targetModule = new File("${targetDirectory}/${itemName}")
    if (!targetModule.exists()) {
        println "$itemType '$itemName' not found. Typo? Or run 'groovyw util $itemType get $itemName' first"
        return
    }
    def remoteGit = Grgit.open(dir: "${targetDirectory}/${itemName}")
    def remote = remoteGit.remote.list()
    def check = remote.find { it.name == "$remoteName" }
    if (!check) {
        remoteGit.remote.add(name: "$remoteName", url: "$url")
        if (isUrlValid(url)) {
            println "Successfully added remote '$remoteName' for '$itemName' - doing a 'git fetch'"
            remoteGit.fetch remote: remoteName
        } else {
            println "Added the remote '$remoteName' for $itemType '$itemName' - but the URL $url failed a test lookup. Typo? Not created yet?"
        }
    } else {
        println "Remote already exists"
    }
}

/**
 * Considers given arguments for the presence of a custom remote, setting that up right if found, tidying up the arguments.
 * @param arguments the args passed into the script
 * @return the adjusted arguments without any found custom remote details and the commmand name itself (get or recurse)
 */
def processCustomRemote(String[] arguments) {
    def remoteArg = arguments.findLastIndexOf { it == "-remote" }
    if (remoteArg != -1) {
        if (arguments.length == (remoteArg + 1)) {
            githubHome = getUserString('Enter Name for the Remote (no spaces)')
            arguments = arguments.dropRight(1)
        } else {
            githubHome = arguments[remoteArg + 1]
            arguments = arguments.dropRight(2)
        }
    }
    return arguments.drop(1)
}

/**
 * This is the main method that recognises the sub-commands and acts accordingly.
 * @param arguments the sub-commands
 * @param type the type of command  : module, meta, facade or lib
 */
def performAction(String[] arguments , String type){
    if(arguments.length == 0){
        printUsage()
    } else {
    //Configuring the script for the type of command.
    initialize(type)
    //Recursion is only for modules.
    def recurse = false
    switch(arguments[0]) {
      case "recurse":
          recurse = true
          println "We're retrieving recursively (all the things depended on too)"
      // We just fall through here to the get logic after setting a boolean
      //noinspection GroovyFallthrough
      case "get":
          println "Preparing to get $itemType"
          if (arguments.length == 1) {
              def itemString = getUserString("Enter $itemType Name(s - separate multiple with spaces, CapiTaliZation MatterS): ")
              println "User wants: $itemString"
              String[] itemList = itemString.split("\\s+")
              retrieve itemList, recurse
          } else {
              arguments = processCustomRemote(arguments)
              retrieve arguments, recurse
          }
          break
      case "create":
          println "We're doing a create"
          String name
          if (arguments.length > 2) {
              println "Received more than one argument. Aborting."
              break
          } else if (arguments.length == 2) {
              name = arguments[1]
          } else {
              name = getUserString("Enter $itemType name: ")
          }
          println "User wants to create a $itemType named: $name"
          createItem(name)
          println "Created $itemType named $name"
          break
      case "update":
          println "We're updating $itemType"
          String[] itemList
          if (arguments.length == 1) {
              def itemString = getUserString("Enter $itemType Name(s - separate multiple with spaces, CapiTaliZation MatterS): ")
              itemList = itemString.split("\\s+")
          } else {
              itemList = arguments.drop(1)
          }
          println "List of items to update: $itemList"
          for (String item : itemList) {
              updateItem(item)
          }
          break
      case "update-all":
          println "We're updating every $itemType"
          println "List:"
          new File(targetDirectory).eachDir() { dir ->
              String itemName = dir.getPath().substring(8)
              if (!excludedDependencies.contains(itemName)) {
                  updateItem(itemName)
              }
          }
          break
      case "add-remote":
            if (arguments.length == 3) {
              itemName = arguments[1]
              remoteName = arguments[2]
              println "Adding Remote for $itemType $itemName"
              addRemote(itemName, remoteName)
          } else if (arguments.length == 4) {
              itemName = arguments[1]
              remoteName = arguments[2]
              url = arguments[3]
              println "Adding Remote for $itemType $itemName"
              addRemote(itemName, remoteName, url)
          } else {
              println "Incorrect Syntax"
              println "Usage: 'add-remote (${itemType}) (name)' - adds a remote (name) to the ${itemType} with default URL."
              println "       'add-remote (${itemType}) (name) (url)' - adds a remote (name) to the ${itemType} with the given URL."
          }
          break
       case "list-remotes":
          if (arguments.length == 2) {
              itemName = args[2]
              println "Listing Remotes for $itemType $itemName"
              listRemotes(itemName)
          } else {
              println "Incorrect Syntax"
              println "Usage: 'list-remotes (${itemType})' - lists all remotes for (${itemType})"
          }
          break
       default:
          println "UNRECOGNIZED COMMAND - please try again or use 'groovyw util usage' for help"
        }
    }
}

  /**
   * Simply prints usage information.
   */
def printUsage() {
    println ""
    println "Utility script for interacting with Terasology. General Syntax:"
    println "  groovyw util (type) (sub-command)"
    println "- 'type' may be module,meta,lib or facade."
    println ""
    println "Available sub-commands:"
    println "- 'get' - retrieves one or more items in source form (separate with spaces)"
    println "- 'recurse' - retrieves the given item(s) *and* their dependencies in source form (only for modules)"
    println "- 'create' - creates a new item of the given type."
    println "- 'update' - updates an item (git pulls latest from current origin, if workspace is clean"
    println "- 'update-all' - updates all local items of the given type."
    println "- 'add-remote (item) (name)' - adds a remote (name) to (item) with the default URL."
    println "- 'add-remote (item) (name) (URL)' - adds a remote with the given URL"
    println "- 'list-remotes (item)' - lists all remotes for (item) "
    println ""
    println "Available flags:"
    println "-remote [someRemote]' to clone from an alternative remote, also adding the Terasology/MovingBlocks/MetaTerasology (depends on the type) repo as 'origin'"
    println "       Note: 'get' + 'recurse' only. This will override an alternativeGithubHome set via gradle.properties."
    println ""
    println "Example: 'groovyw util module recurse GooeysQuests Sample' - would retrieve those modules plus their dependencies"
    println ""
    println "*NOTE*: Item names are case sensitive"
    println ""
    println "If you omit further arguments beyond the sub command you'll be prompted for details"
    println "After changing modules available in your workspace rerun 'gradlew idea' and/or refresh your IDE"
    println ""
    println "For advanced usage see project documentation. For instance you can provide an alternative GitHub home"
    println "A gradle.properties file (one exists under '/templates' in an engine workspace) can provide such overrides"
    println ""
}

// Main bit of logic handling the entry points to this script.
if (args.length == 0) {
    printUsage()
} else {
    //Drop the type from the arguments to pass on to the method.
    new_args = args.drop(1)
    switch (args[0]) {
        case 'usage':
            printUsage()
            break
        case 'module':
            performAction(new_args,"module")
            break
        case 'facade':
            performAction(new_args,"facade")
            break
        case 'meta':
            performAction(new_args,"meta")
            break
        case 'lib':
            performAction(new_args,"lib")
            break
        default:
            println "UNRECOGNIZED COMMAND - please try again or use 'groovyw util usage' for help"
    }
}
