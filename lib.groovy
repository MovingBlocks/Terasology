@GrabResolver(name = 'jcenter', root = 'http://jcenter.bintray.com/')
@Grab(group = 'org.ajoberstar', module = 'grgit', version = '1.9.3')
import org.ajoberstar.grgit.Grgit
import groovy.json.JsonSlurper

Properties properties = new Properties()
new File("gradle.properties").withInputStream {
    properties.load(it)
}

githubHome = properties.alternativeGithubHome ?: "MovingBlocks"

libsRetrieved = []

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

def retrieve(String[] libs) {
    println "Now inside retrieve, user wants: $libs"
    for (String lib : libs) {
        println "Starting retrieval for library $lib"
        println "Libraries retrieved so far: $libsRetrieved"
        retrieveLib(lib)
    }
}

def retrieveLib(String lib) {
    File targetDir = new File("libs/$lib")
    println "Request to retrieve library $lib would store it at $targetDir - exists? " + targetDir.exists()

    if (targetDir.exists()) {
        println "That library already had an existing directory locally. If something is wrong with it please delete and try again"
        libsRetrieved << lib
    } else if (libsRetrieved.contains(lib)) {
        println "We already retrieved $lib - skipping"
    } else {
        libsRetrieved << lib
        def targetUrl = "https://github.com/$githubHome/${lib}"
        if (!isUrlValid(targetUrl)) {
            println "Can't retrieve library from $targetUrl - URL appears invalid. Typo? Not created yet?"
            return
        }
        println "Retrieving library $lib from $targetUrl"

        if (githubHome != "MovingBlocks") {
            println "Doing a retrieve from a custom remote: $githubHome - will name it as such plus add the MovingBlocks remote as 'origin'"
            Grgit.clone dir: targetDir, uri: targetUrl, remote: githubHome
            println "Primary clone operation complete, [AUTO-ADD REMOTES NOT IMPLEMENTED]"
        } else {
            Grgit.clone dir: targetDir, uri: targetUrl
        }
        File targetBuildGradle = new File(targetDir, 'build.gradle')
        targetBuildGradle.delete()
        targetBuildGradle << new File('templates/build.gradle').text
        }
}

def createLib(String libName) {
    File targetDir = new File("libs/$libName")
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

    Grgit.init dir: targetDir, bare: false
    println "[AUTO-ADD REMOTES NOT IMPLEMENTED]"
}

def printUsage() {
    println ""
    println "Utility script for interacting with libraries. Available sub-commands:"
    println "  'get' - retrieves one or more libraries in source form (separate with spaces)"
    println "  'create' - creates a new library"
    println ""
    println "*NOTE*: Library names are case sensitive"
    println ""
    println "If you omit further arguments beyond the sub command you'll be prompted for details"
    println "After changing libraries available in your workspace rerun 'gradlew idea' and/or refresh your IDE"
    println ""
    println "For advanced usage see project documentation. For instance you can provide an alternative GitHub home"
    println "A gradle.properties file (one exists under '/templates' in an engine workspace) can provide such overrides"
    println ""
}

if (args.length == 0) {
    printUsage()
} else {
    switch (args[0]) {
        case 'usage':
            printUsage()
            break
        case "get":
            println "Preparing to get one or more libraries"
            if (args.length == 1) {
                def libString = getUserString('Enter Library Name(s - separate multiple with spaces, CapiTaliZation MatterS): ')
                println "User wants: $libString"
                String[] libList = libString.split("\\s+")
                retrieve(libList)
            } else {
                println "User wants: ${args[1]}"
                retrieveLib(args[1])
            }
            break
        case "create":
            println "We're creating a Library!"
            String name
            if (args.length > 2) {
                println "Received more than one argument. Aborting."
                break
            } else if (args.length == 2) {
                name = args[1]
            } else {
                name = getUserString("Enter library name: ")
            }
            println "User wants to create a library named: $name"
            createLib(name)
            println "Created library named $name"
            break
        default:
            println "UNRECOGNIZED COMMAND - please try again or use 'groovyw lib usage' for help"
    }
}
