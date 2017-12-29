@GrabResolver(name = 'jcenter', root = 'http://jcenter.bintray.com/')
@Grab(group = 'org.ajoberstar', module = 'grgit', version = '1.9.3')
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.Remote
import groovy.json.JsonSlurper

Properties properties = new Properties()
new File("gradle.properties").withInputStream {
    properties.load(it)
}

//Default settings for modules
githubRepo="Terasology"
targetDirectory="modules"
displayName="Module"
//Main Logic for changing githubRepo, targetDirectory & displayName
def initialize(String type) {

  if (type == "meta") {
    githubRepo="MetaTerasology"
    targetDirectory="metas"
    displayName="meta"
  }
  else if (type == "lib") {
    githubRepo="MovingBlocks"
    targetDirectory="libs"
    displayName="library"
  }
  else if (type == "facade") {
    githubRepo="MovingBlocks"
    targetDirectory="facades"
    displayName="facade"
  }
  githubHome=properties.alternativeGithubHome ?: githubRepo
}

itemsRetrieved = []

excludedDependencies = ["engine", "Core", "CoreSampleGameplay", "BuilderSampleGameplay"]

def getUserString(String prompt) {
    println('\n*** ' + prompt + '\n')

    def reader = new BufferedReader(new InputStreamReader(System.in))

    return reader.readLine()
}

boolean isUrlValid(String url) {
    def code = new URL(url).openConnection().with {
        requestMethod = 'HEAD'
        connect()
        responseCode
    }
    return code.toString() == "200"
}

def retrieve(String[] items, boolean recurse) {
    println "Now inside retrieve, user (recursively? $recurse) wants: $items"
    for (String itemName : items) {
        println "Starting retrieval for $displayName $itemName, are we recursing? $recurse"
        println "Retrieved so far: $itemsRetrieved"
        retrieveItem(itemName, recurse)
    }
}

def retrieveItem(String itemName, boolean recurse) {

    File targetDir = new File("$targetDirectory"+"/$itemName")
    println "Request to retrieve $displayName $itemName would store it at $targetDir - exists? " + targetDir.exists()

    if (targetDir.exists()) {
        println "That $displayName already had an existing directory locally. If something is wrong with it please delete and try again"
        itemsRetrieved << itemName
    } else if (itemsRetrieved.contains(itemName)) {
        println "We already retrieved $itemName - skipping"
    } else {
        itemsRetrieved << itemName
        def targetUrl = "https://github.com/$githubHome/${itemName}"
        if (!isUrlValid(targetUrl)) {
            println "Can't retrieve $displayName from $targetUrl - URL appears invalid. Typo? Not created yet?"
            return
        }
        println "Retrieving $displayName $itemName from $targetUrl"

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

        //module.txt and dependencies for Modules
        if(targetDirectory=="modules") {
            File moduleManifest = new File(targetDir, 'module.txt')
            if (!moduleManifest.exists()) {
                def moduleText = new File("templates/module.txt").text
                moduleManifest << moduleText.replaceAll('MODULENAME', module)
                println "WARNING: Module $module did not have a module.txt! One was created, please review and submit to GitHub"
            }

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

//Only for modules
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

def createItem(String itemName) {
    File targetDir = new File("$targetDirectory/$itemName")
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

    println "Creating build.gradle"
    File buildGradle = new File(targetDir, "build.gradle")
    def buildGradleText = new File("templates/build.gradle").text
    buildGradle << buildGradleText

    if (targetDirectory=="modules"){
        println "Creating module.txt"
        File moduleManifest = new File(targetDir, "module.txt")
        def moduleText = new File("templates/module.txt").text
        moduleManifest << moduleText.replaceAll('MODULENAME', moduleName)
    }

    Grgit.init dir: targetDir, bare: false
    addRemote(moduleName, "origin", "https://github.com/${githubRepo}/${itemName}.git")
}

def updateItem(String itemName) {
    println "Attempting to update $displayName $itemName"
    File targetDir = new File("${targetDirectory}/${itemName}")
    if (!targetDir.exists()) {
        println "$displayName \"$itemName\" not found"
        return
    }

    def moduleGit = Grgit.open(dir: targetDir)
    def clean = moduleGit.status().clean
    println "Is \"$itemName\" clean? $clean"
    if (!clean) {
        println "$displayName has uncommitted changes. Aborting."
        return
    }

    println "Updating module $itemName"
    moduleGit.pull remote: "origin"
}

//Now lists remotes for modules,facades,libraries and meta modules.
def listRemotes(String itemName) {
    File moduleExistence = new File("${targetDirectory}/$itemName")
    if (!moduleExistence.exists()) {
        println "$displayName '$itemName' not found. Typo? Or run 'groovyw util $displayName get $itemName' first"
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

def addRemotes(String[] items, String name) {
    for (String item : items) {
        addRemote(item, name)
    }
}

def addRemote(String itemName, String remoteName) {
    addRemote(itemName, remoteName, "https://github.com/$remoteName/$itemName" + ".git")
}

//Now adds remotes for modules,facades,libraries and meta modules.
def addRemote(String itemName, String remoteName, String url) {
    File targetModule = new File("${targetDirectory}/${itemName}")
    if (!targetModule.exists()) {
        println "$displayName '$itemName' not found. Typo? Or run 'groovyw util $displayName get $itemName' first"
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
            println "Added the remote '$remoteName' for $displayName '$itemName' - but the URL $url failed a test lookup. Typo? Not created yet?"
        }
    } else {
        println "Remote already exists"
    }
}


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

// Common code for modules,facades,libraries & meta modules.
def performAction(String[] arguments , boolean recurse , String type){
  initialize(type)
  switch(arguments[0]) {
  case "get":
      println "Preparing to get $displayName"
      if (arguments.length == 1) {
          // User hasn't supplied any module names, so ask
          def itemString = getUserString('Enter $displayName Name(s - separate multiple with spaces, CapiTaliZation MatterS): ')
          println "User wants: $itemString"
          // Split it on whitespace
          String[] itemList = itemString.split("\\s+")
          retrieve itemList, recurse
      } else {
          // First see if the user included "-remote" and process that if so. Expect a clean array back
          arguments = processCustomRemote(arguments)
          retrieve arguments, recurse
      }
      break
  case "create":
      println "We're doing a create"
      String name

      // Get new module's name
      if (arguments.length > 2) {
          println "Received more than one argument. Aborting."
          break
      } else if (arguments.length == 2) {
          name = arguments[1]
      } else {
          name = getUserString("Enter $displayName name: ")
      }
      println "User wants to create a $displayName named: $name"

      createModule(name)

      println "Created $displayName named $name"
      break
  case "update":
      println "We're updating $displayName"
      String[] itemList
      if (arguments.length == 1) {
          def itemString = getUserString('Enter $displayName Name(s - separate multiple with spaces, CapiTaliZation MatterS): ')
          itemList = itemString.split("\\s+")
      } else {
          itemList = arguments.drop(1)
      }
      println "List of items to update: $itemList"
      for (String item : itemList) {
          updateModule(item)
      }
      break
  case "update-all":
      println "We're updating every $displayName"
      println "List:"
      new File(targetDirectory).eachDir() { dir ->
          String itemName = dir.getPath().substring(8)
          if (!excludedDependencies.contains(itemName)) {
              updateModule(itemName)
          }
      }
      break
  case "add-remote":
      if (arguments.length == 3) {
          itemName = arguments[1]
          remoteName = arguments[2]
          println "Adding Remote for $displayName $itemName"
          addRemote(itemName, remoteName)
      } else if (arguments.length == 4) {
          itemName = arguments[1]
          remoteName = arguments[2]
          url = arguments[3]
          println "Adding Remote for $displayName $itemName"
          addRemote(itemName, remoteName, url)
      } else {
          println "Incorrect Syntax"
          println "Usage: 'add-remote (${displayName}) (name)' - adds a remote (name) to the ${displayName} with default URL."
          println "       'add-remote (${displayName}) (name) (url)' - adds a remote (name) to the ${displayName} with the given URL."
      }
      break
   case "list-remotes":
      if (arguments.length == 2) {
          itemName = args[2]
          println "Listing Remotes for $displayName $itemName"
          listRemotes(itemName)
      } else {
          println "Incorrect Syntax"
          println "Usage: 'list-remotes (${displayName})' - lists all remotes for (${displayName})"
      }
      break
   default:
      println "UNRECOGNIZED COMMAND - please try again or use 'groovyw util usage' for help"
    }
}

def printUsage() {
    println ""
    println "PLEASE IGNORE!!! THIS HELP SECTION NEEDS TO BE MODIFIED!!!"
    println "Utility script for interacting with Terasology. Available sub commands:"
    println "- 'get' - retrieves one or more modules in source form (separate with spaces)"
    println "- 'recurse' - retrieves the given module(s) *and* their dependencies in source form"
    println "- 'create' - creates a new module"
    println "- 'update' - updates a module (git pulls latest from current origin, if workspace is clean"
    println "- 'update-all' - updates all local modules"
    println "- 'add-remote (module) (name)' - adds a remote (name) to modules/(module) with the default URL."
    println "- 'add-remote (module) (name) (URL)' - adds a remote with the given URL"
    println "- 'list-remotes (module)' - lists all remotes for (module) "
    println ""
    println "Available flags"
    println "-remote [someRemote]' to clone from an alternative remote, also adding the Terasology repo as 'origin'"
    println "       Note: 'get' + 'recurse' only. This will override an alternativeGithubHome set via gradle.properties."
    println ""
    println "Example: 'groovyw module recurse GooeysQuests Sample' - would retrieve those modules plus their dependencies"
    println ""
    println "*NOTE*: Module names are case sensitive"
    println ""
    println "If you omit further arguments beyond the sub command you'll be prompted for details"
    println "After changing modules available in your workspace rerun 'gradlew idea' and/or refresh your IDE"
    println ""
    println "For advanced usage see project documentation. For instance you can provide an alternative GitHub home"
    println "A gradle.properties file (one exists under '/templates' in an engine workspace) can provide such overrides"
    println ""
}

if (args.length == 0) {
    printUsage()
} else {
    def recurse = false
    switch (args[0]) {
        case 'usage':
            printUsage()
            break
        case "recurse":
            recurse = true
            println "We're retrieving recursively (all the things depended on too)"
        case 'module':
            performAction(args.drop(1),recurse,"module")
            break
        case 'facade':
            performAction(args.drop(1),recurse,"facade")
            break
        case 'meta':
            performAction(args.drop(1),recurse,"meta")
            break
        case 'lib':
            performAction(args.drop(1),recurse,"lib")
            break
        default:
            println "UNRECOGNIZED COMMAND - please try again or use 'groovyw util usage' for help"
    }
}
