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

// Groovy Elvis operator woo! Defaults to "MovingBlocks" if an override isn't set
githubHome = properties.alternativeGithubHome ?: "MovingBlocks"

// For keeping a list of facades retrieved so far
facadesRetrieved = []

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
 * Primary entry point for retrieving facades.
 * @param facades the facades we want to retrieve
 */
def retrieve(String[] facades) {
    println "Now inside retrieve, user wants: $facades"
    for (String facade : facades) {
        println "Starting retrieval for facade $facade"
        println "Facades retrieved so far: $facadesRetrieved"
        retrieveFacade(facade)
    }
}

/**
 * Retrieves a single facade via Git Clone. Considers whether it exists locally first or if it has already been retrieved this execution.
 * @param facade the target facade to retrieve
 */
def retrieveFacade(String facade) {
    File targetDir = new File("facades/$facade")
    println "Request to retrieve facade $facade would store it at $targetDir - exists? " + targetDir.exists()

    if (targetDir.exists()) {
        println "That facade already had an existing directory locally. If something is wrong with it please delete and try again"
        facadesRetrieved << facade
    } else if (facadesRetrieved.contains(facade)) {
        println "We already retrieved $facade - skipping"
    } else {
        // Immediately note the given facade as retrieved, since if any failure occurs we don't want to retry
        facadesRetrieved << facade
        def targetUrl = "https://github.com/$githubHome/Facade${facade}"
        if (!isUrlValid(targetUrl)) {
            println "Can't retrieve facade from $targetUrl - URL appears invalid. Typo? Not created yet?"
            return
        }
        println "Retrieving facade $facade from $targetUrl"

        // Prepare to clone the target repo, adding a secondary remote if it isn't already hosted under the Terasology org
        if (githubHome != "Terasology") {
            println "Doing a retrieve from a custom remote: $githubHome - will name it as such plus add the Terasology remote as 'origin'"
            //noinspection GroovyAssignabilityCheck - GrGit has its own .clone but a warning gets issued for Object.clone
            Grgit.clone dir: targetDir, uri: targetUrl, remote: githubHome
            println "Primary clone operation complete, [AUTO-ADD REMOTES NOT IMPLEMENTED]"
        } else {
            //noinspection GroovyAssignabilityCheck - GrGit has its own .clone but a warning gets issued for Object.clone
            Grgit.clone dir: targetDir, uri: targetUrl
        }

        // TODO: Temporary until build.gradle gets removed from facade directories (pending Cervator work)
        File targetBuildGradle = new File(targetDir, 'build.gradle')
        targetBuildGradle.delete()
        targetBuildGradle << new File('templates/build.gradle').text

        File moduleManifest = new File(targetDir, 'module.txt')
        if (!moduleManifest.exists()) {
            def moduleText = new File("templates/module.txt").text
            moduleManifest << moduleText.replaceAll('MODULENAME', facade)
            println "WARNING: Facade $facade did not have a module.txt! One was created, please review and submit to GitHub"
        }
    }
}


/**
 * Creates a new facade with the given name and adds the necessary .gitignore,
 * build.gradle and facade.txt files.
 * @param facadeName the name of the facade to be created
 */
def createFacade(String facadeName) {
    // Check if the facade already exists. If not, create the facades directory
    File targetDir = new File("facades/$facadeName")
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
    moduleManifest << moduleText.replaceAll('MODULENAME', facadeName)

    // Initialize git
    Grgit.init dir: targetDir, bare: false
    println "[AUTO-ADD REMOTES NOT IMPLEMENTED]"
}

/**
 * Simply prints usage information.
 */
def printUsage() {
    println ""
    println "Utility script for interacting with facades. Available sub commands:"
    println "- 'get' - retrieves one or more facades in source form (separate with spaces)"
    println "- 'create' - creates a new facade"
    println ""
    println "*NOTE*: Facade names are case sensitive"
    println ""
    println "If you omit further arguments beyond the sub command you'll be prompted for details"
    println "After changing facades available in your workspace rerun 'gradlew idea' and/or refresh your IDE"
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
    switch (args[0]) {
        case 'usage':
            printUsage()
            break
        case "get":
            println "Preparing to get one or more facades"
            if (args.length == 1) {
                // User hasn't supplied any facade names, so ask
                def facadeString = getUserString('Enter Facade Name(s - separate multiple with spaces, CapiTaliZation MatterS): ')
                println "User wants: $facadeString"
                // Split it on whitespace
                String[] facadeList = facadeString.split("\\s+")
                retrieve(facadeList)
            } else {
                println "User wants: ${args[1]}"
                retrieveFacade(args[1])
            }
            break
        case "create":
            println "We're doing a create"
            String name

            // Get new facade's name
            if (args.length > 2) {
                println "Received more than one argument. Aborting."
                break
            } else if (args.length == 2) {
                name = args[1]
            } else {
                name = getUserString("Enter facade name: ")
            }
            println "User wants to create a facade named: $name"

            createFacade(name)

            println "Created facade named $name"
            break
        default:
            println "UNRECOGNIZED COMMAND - please try again or use 'groovyw facade usage' for help"
    }
}
