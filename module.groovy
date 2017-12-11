// We use GrGit for interacting with Git. This gets a hold of it as a dependency like Gradle would
// TODO: Consider if we should do something to fix/suppress the SLF4J warning that gets logged on first usage?
@GrabResolver(name = 'jcenter', root = 'http://jcenter.bintray.com/')
@Grab(group = 'org.ajoberstar', module = 'grgit', version = '1.9.3')
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.Remote

import groovy.json.JsonSlurper

// Grab override properties from the gradle.properties file (shared with various Gradle commands)
Properties properties = new Properties()
new File("gradle.properties").withInputStream {
    properties.load(it)
}

// Groovy Elvis operator woo! Defaults to "Terasology" if an override isn't set
githubHome = properties.alternativeGithubHome ?: "Terasology"

// For keeping a list of modules retrieved so far
modulesRetrieved = []

// Module dependencies we don't want to retrieve as they live in the main Terasology repo
excludedDependencies = ["engine", "Core", "CoreSampleGameplay", "BuilderSampleGameplay"]

/**
 * Accepts input from the user, showing a descriptive prompt.
 * @param prompt the prompt to show the user
 */
def getUserString(String prompt) {
    println('\n*** ' + prompt + '\n')

    def reader = new BufferedReader(new InputStreamReader(System.in))
    // Note: Do not close reader, it will close System.in (Big no-no)

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
 * Primary entry point for retrieving modules, kicks off recursively if needed.
 * @param modules the modules we want to retrieve
 * @param recurse whether to also retrieve dependencies of the desired modules
 */
def retrieve(String[] modules, boolean recurse) {
    println "Now inside retrieve, user (recursively? $recurse) wants: $modules"
    for (String module : modules) {
        println "Starting retrieval for module $module, are we recursing? $recurse"
        println "Modules retrieved so far: $modulesRetrieved"
        retrieveModule(module, recurse)
    }
}

/**
 * Retrieves a single module via Git Clone. Considers whether it exists locally first or if it has already been retrieved this execution.
 * @param module the target module to retrieve
 * @param recurse whether to also retrieve its dependencies (if so then recurse back into retrieve)
 */
def retrieveModule(String module, boolean recurse) {
    File targetDir = new File("modules/$module")
    println "Request to retrieve module $module would store it at $targetDir - exists? " + targetDir.exists()

    if (targetDir.exists()) {
        println "That module already had an existing directory locally. If something is wrong with it please delete and try again"
        modulesRetrieved << module
    } else if (modulesRetrieved.contains(module)) {
        println "We already retrieved $module - skipping"
    } else {
        // Immediately note the given module as retrieved, since if any failure occurs we don't want to retry
        modulesRetrieved << module
        def targetUrl = "https://github.com/$githubHome/${module}"
        if (!isUrlValid(targetUrl)) {
            println "Can't retrieve module from $targetUrl - URL appears invalid. Typo? Not created yet?"
            return
        }
        println "Retrieving module $module from $targetUrl"

        // Prepare to clone the target repo, adding a secondary remote if it isn't already hosted under the Terasology org
        if (githubHome != "Terasology") {
            println "Doing a retrieve from a custom remote: $githubHome - will name it as such plus add the Terasology remote as 'origin'"
            //noinspection GroovyAssignabilityCheck - GrGit has its own .clone but a warning gets issued for Object.clone
            Grgit.clone dir: targetDir, uri: targetUrl, remote: githubHome
            println "Primary clone operation complete, about to add the 'origin' remote for the Terasology org address"
            addRemote(module, "origin", "https://github.com/Terasology/${module}")
        } else {
            //noinspection GroovyAssignabilityCheck - GrGit has its own .clone but a warning gets issued for Object.clone
            Grgit.clone dir: targetDir, uri: targetUrl
        }

        // TODO: Temporary until build.gradle gets removed from module directories (pending Cervator work)
        File targetBuildGradle = new File(targetDir, 'build.gradle')
        targetBuildGradle.delete()
        targetBuildGradle << new File('templates/build.gradle').text

        File moduleManifest = new File(targetDir, 'module.txt')
        if (!moduleManifest.exists()) {
            def moduleText = new File("templates/module.txt").text
            moduleManifest << moduleText.replaceAll('MODULENAME', module)
            println "WARNING: Module $module did not have a module.txt! One was created, please review and submit to GitHub"
        }

        // Recurse deeper if we are retrieving dependencies as well
        if (recurse) {
            def foundDependencies = readModuleDependencies(new File(targetDir, "module.txt"))
            if (foundDependencies.length == 0) {
                println "Module $module did not appear to have any dependencies we need to worry about"
            } else {
                println "Module $module has the following module dependencies we care about: $foundDependencies"
                String[] uniqueDependencies = foundDependencies - modulesRetrieved
                println "After removing dupes already retrieved we have the remaining dependencies left: $uniqueDependencies"
                if (uniqueDependencies.length > 0) {
                    retrieve(uniqueDependencies, true)
                }
            }
        }
    }
}

/**
 * Reads a given module info file to figure out which if any dependencies it has. Filters out any already retrieved.
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
 * Creates a new module with the given name and adds the necessary .gitignore,
 * build.gradle and module.txt files.
 * @param moduleName the name of the module to be created
 */
def createModule(String moduleName) {
    // Check if the module already exists. If not, create the module directory
    File targetDir = new File("modules/$moduleName")
    if (targetDir.exists()) {
        println "Target directory already exists. Aborting."
        return
    }
    println "Creating target directory"
    targetDir.mkdir()

    // Add gitignore
    println "Creating .gitignore"
    File gitignore = new File(targetDir, ".gitignore")
    def gitignoreText = new File("templates/.gitignore").text
    gitignore << gitignoreText

    // Add build.gradle (temporary until it gets removed)
    println "Creating build.gradle"
    File buildGradle = new File(targetDir, "build.gradle")
    def buildGradleText = new File("templates/build.gradle").text
    buildGradle << buildGradleText

    // Add module.txt
    println "Creating module.txt"
    File moduleManifest = new File(targetDir, "module.txt")
    def moduleText = new File("templates/module.txt").text
    moduleManifest << moduleText.replaceAll('MODULENAME', moduleName)

    // Initialize git
    Grgit.init dir: targetDir, bare: false
    addRemote(moduleName, "origin", "https://github.com/Terasology/${moduleName}.git")
}

/**
 * Update a given module.
 * @param name the name of the module to update
 */
def updateModule(String name) {
    println "Attempting to update module $name"
    File targetDir = new File("modules/$name")
    if (!targetDir.exists()) {
        println "Module \"$name\" not found"
        return
    }

    def moduleGit = Grgit.open(dir: targetDir)
    def clean = moduleGit.status().clean
    println "Is \"$name\" clean? $clean"
    if (!clean) {
        println "Module has uncommitted changes. Aborting."
        return
    }

    println "Updating module $name"
    moduleGit.pull remote: "origin"
}

/**
 * List all existing Git remotes for a given module.
 * @param moduleName the module to list remotes for
 */
def listRemotes(String moduleName) {
    File moduleExistence = new File("modules/$moduleName")
    if (!moduleExistence.exists()) {
        println "Module '$moduleName' not found. Typo? Or run 'groovyw module get $moduleName' first"
        return
    }
    def remoteGit = Grgit.open(dir: "modules/$moduleName")
    def remote = remoteGit.remote.list()
    x = 1
    for (Remote item : remote) {
        println(x + " " + item.name + " " + "(" + item.url + ")")
        x += 1
    }
}

/**
 * Add new Git remotes for the given modules, all using the same remote name.
 * @param modules the modules to add remotes for
 * @param name the name to use for all the Git remotes
 */
def addRemotes(String[] modules, String name) {
    for (String module : modules) {
        addRemote(module, name)
    }
}

/**
 * Add a new Git remote for the given module, deducing a standard URL to the repo.
 * @param moduleName the module to add the remote for
 * @param remoteName the name to give the new remote
 */
def addRemote(String moduleName, String remoteName) {
    addRemote(moduleName, remoteName, "https://github.com/$remoteName/$moduleName" + ".git")
}


/**
 * Add a new Git remote for the given module.
 * @param moduleName the module to add the remote for
 * @param remoteName the name to give the new remote
 * @param URL address to the remote Git repo
 */

def addRemote(String moduleName, String remoteName, String url) {
    File targetModule = new File("modules/$moduleName")
    if (!targetModule.exists()) {
        println "Module '$moduleName' not found. Typo? Or run 'groovyw module get $moduleName' first"
        return
    }
    def remoteGit = Grgit.open(dir: "modules/$moduleName")
    def remote = remoteGit.remote.list()
    def check = remote.find { it.name == "$remoteName" }
    if (!check) {
        // Always add the remote whether it exists or not
        remoteGit.remote.add(name: "$remoteName", url: "$url")
        // But then do a validation check to advise the user and do a fetch if it is valid
        if (isUrlValid(url)) {
            println "Successfully added remote '$remoteName' for '$moduleName' - doing a 'git fetch'"
            remoteGit.fetch remote: remoteName
        } else {
            println "Added the remote '$remoteName' for module '$moduleName' - but the URL $url failed a test lookup. Typo? Not created yet?"
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

    // If we find the remote arg go ahead and process it then remove the related arguments
    if (remoteArg != -1) {
        // If the user didn't we can tell by simply checking the number of elements vs where "-remote" was
        if (arguments.length == (remoteArg + 1)) {
            githubHome = getUserString('Enter Name for the Remote (no spaces)')
            // Drop the "-remote" so the arguments string gets cleaner
            arguments = arguments.dropRight(1)
        } else {
            githubHome = arguments[remoteArg + 1]
            // Drop the "-remote" as well as the value the user supplied
            arguments = arguments.dropRight(2)
        }
    }
    return arguments.drop(1)
}

/**
 * Simply prints usage information.
 */
def printUsage() {
    println ""
    println "Utility script for interacting with modules. Available sub commands:"
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

// Main bit of logic handling the entry points to this script - defers actual work to dedicated methods
//println "Args: $args"
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
        // We just fall through here to the get logic after setting a boolean
        //noinspection GroovyFallthrough
        case "get":
            println "Preparing to get one or more modules"
            if (args.length == 1) {
                // User hasn't supplied any module names, so ask
                def moduleString = getUserString('Enter Module Name(s - separate multiple with spaces, CapiTaliZation MatterS): ')
                println "User wants: $moduleString"
                // Split it on whitespace
                String[] moduleList = moduleString.split("\\s+")
                retrieve moduleList, recurse
            } else {
                // First see if the user included "-remote" and process that if so. Expect a clean array back
                args = processCustomRemote(args)
                retrieve args, recurse
            }
            break
        case "create":
            println "We're doing a create"
            String name

            // Get new module's name
            if (args.length > 2) {
                println "Received more than one argument. Aborting."
                break
            } else if (args.length == 2) {
                name = args[1]
            } else {
                name = getUserString("Enter module name: ")
            }
            println "User wants to create a module named: $name"

            createModule(name)

            println "Created module named $name"
            break
        case "update":
            println "We're updating modules"
            String[] moduleList
            if (args.length == 1) {
                // User hasn't supplied any module names, so ask
                def moduleString = getUserString('Enter Module Name(s - separate multiple with spaces, CapiTaliZation MatterS): ')
                // Split it on whitespace
                moduleList = moduleString.split("\\s+")
            } else {
                // User has supplied one or more module names, so pass them forward (skipping the "get" arg)
                moduleList = args.drop(1)
            }
            println "List of modules to update: $moduleList"
            for (String module : moduleList) {
                updateModule(module)
            }
            break
        case "update-all":
            println "We're updating all modules"
            println "List of modules:"
            new File("modules").eachDir() { dir ->
                String moduleName = dir.getPath().substring(8)
                if (!excludedDependencies.contains(moduleName)) {
                    updateModule(moduleName)
                }
            }
            break
        case "add-remote":
            if (args.length == 3) {
                moduleName = args[1]
                remoteName = args[2]
                println "Adding Remote for module $moduleName"
                addRemote(moduleName, remoteName)
            } else if (args.length == 4) {
                moduleName = args[1]
                remoteName = args[2]
                url = args[3]
                println "Adding Remote for module $moduleName"
                addRemote(moduleName, remoteName, url)
            } else {
                println "Incorrect Syntax"
                println "Usage: 'add-remote (module) (name)' - adds a remote (name) to modules/(module) with default URL."
                println "       'add-remote (module) (name) (url)' - adds a remote to the module with the given URL."
            }
            break
        case "list-remotes":
            if (args.length == 2) {
                moduleName = args[1]
                println "Listing Remotes for module $moduleName"
                listRemotes(moduleName)
            } else {
                println "Incorrect Syntax"
                println "Usage: 'list-remotes (module)' - lists all remotes for (module)"
            }
            break
        default:
            println "UNRECOGNIZED COMMAND - please try again or use 'groovyw module usage' for help"
    }
}
