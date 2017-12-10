//Adapted from module.groovy
@GrabResolver(name = 'jcenter', root = 'http://jcenter.bintray.com/')
@Grab(group='org.ajoberstar', module='grgit', version='1.9.3')
import org.ajoberstar.grgit.Grgit
import groovy.json.JsonSlurper

Properties properties = new Properties()
new File("gradle.properties").withInputStream {
    properties.load(it)
}

githubHome = properties.alternativeGithubHome ?: "MetaTerasology"

modulesRetrieved = []


def retrieve(String[] modules) {
    println "Now inside retrieve, user wants: $modules"
    for (String module : modules) {
        println "Starting loop for meta module $module"
        println "Meta Modules retrieved so far: $modulesRetrieved"
        retrieveModule(module)
    }
}

def retrieveModule(String module) {
    File targetDir = new File("meta/$module")
    println "Request to retrieve meta module $module would store it at $targetDir - exists? " + targetDir.exists()

    if (targetDir.exists()) {
        println "That meta module already had an existing directory locally. If something is wrong with it please delete and try again"
        modulesRetrieved << module
    } else if (modulesRetrieved.contains(module)) {
        println "We already retrieved $module - skipping"
    } else {
        println "Retrieving meta module $module - if it doesn't appear to exist (typo for instance) you'll get an auth prompt (in case it is private)"
        //noinspection GroovyAssignabilityCheck - GrGit has its own .clone but a warning gets issued for Object.clone
        Grgit.clone dir: targetDir, uri: "https://github.com/$githubHome/${module}.git"
        modulesRetrieved << module

        // TODO: Temporary until build.gradle gets removed from module directories (pending Cervator work from module.groovy)
        // File targetBuildGradle = new File(targetDir, 'build.gradle')
        // targetBuildGradle.delete()
        // targetBuildGradle << new File('templates/build.gradle').text

    }
}


def createMetaModule(String name) {
    // Check if the module already exists. If not, create the module directory
    File targetDir = new File("meta/$name")
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

    // Initialize git
    Grgit.init dir: targetDir, bare: false
}

def getUserString (String prompt) {
    println ('\n*** ' + prompt + '\n')

    def reader = new BufferedReader(new InputStreamReader(System.in)) // Note: Do not close reader, it will close System.in (Big no-no)

    return reader.readLine()
}

def printUsage() {
    println ""
    println "Utility script for interacting with meta modules. Available sub commands:"
    println "- 'get' - retrieves one or more meta modules in source form (separate with spaces)"
    println "- 'create' - creates a new meta module"
    println "*NOTE*: Module names are case sensitive"
    println ""
    println "If you omit further arguments beyond the sub command you'll be prompted for details"
    println "After changing modules available in your workspace rerun 'gradlew idea' and/or refresh your IDE"
}

if (args.length == 0) {
    printUsage()
} else {
    switch (args[0]) {
        case 'usage':
            printUsage()
            break
        case "get":
            println "Preparing to get one or more meta modules"
            if (args.length == 1) {
                // User hasn't supplied any module names, so ask
                def moduleString = getUserString('Enter Module Name(s - separate multiple with spaces, CapiTaliZation MatterS): ')
                println "User wants: $moduleString"
                // Split it on whitespace
                String[] moduleList = moduleString.split("\\s+")
                println "Now in an array: $moduleList"
                retrieve moduleList
            } else {

            // User has supplied one or more module names, so pass them forward (skipping the "get" arg)
                def adjustedArgs = args.drop(1)
                println "Adjusted args: $adjustedArgs"
                retrieve adjustedArgs
            }
            println "All done retrieving requested modules: $modulesRetrieved"
            break
        case "create":
            println "We're doing a create"
            String name = ""

            // Get new module's name
            if (args.length > 2) {
              println "Received more than one argument. Aborting."
              break
            } else if (args.length == 2) {
              name = args[1]
            } else {
              name = getUserString("Enter module name: ")
            }
            println "User wants to create a meta module named: $name"

            createMetaModule(name)

            println "Created module named $name"
            break
        default:
                println "UNRECOGNIZED COMMAND - please try again or use 'groovyw meta usage' for help"
        }
      }
