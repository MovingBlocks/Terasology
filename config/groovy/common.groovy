@GrabResolver(name = 'jcenter', root = 'http://jcenter.bintray.com/')
@Grab(group = 'org.ajoberstar', module = 'grgit', version = '1.9.3')
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.Remote

class common {

    /** For preparing an instance of the target item type utility class we want to work with */
    GroovyObject itemTypeScript

    /** The official default GitHub home (org/user) for the type */
    def githubDefaultHome

    /** The actual target GitHub home (org/user) for the type, as potentially requested by the user */
    def githubTargetHome

    /** The target directory for the type */
    def targetDirectory

    /** The clean human readable name for the type */
    def itemType

    /** Things we don't want to retrieve/update as they live in the main MovingBlocks/Terasology repo */
    def excludedItems

    /** For keeping a list of items retrieved so far */
    def itemsRetrieved = []

    /**
     * Initialize defaults to match the target item type
     * @param type the type to be initialized
     */
    def initialize(String type) {
        // Look for a gradle.properties to check for a variety of override configuration
        Properties properties = new Properties()
        File gradlePropsFile = new File("gradle.properties")
        if (gradlePropsFile.exists()) {
            gradlePropsFile.withInputStream {
                properties.load(it)
            }
        }

        File itemTypeScriptFile = new File("config/groovy/${type}.groovy")
        Class targetClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(itemTypeScriptFile)
        itemTypeScript = (GroovyObject) targetClass.newInstance()

        excludedItems = itemTypeScript.excludedItems
        githubDefaultHome = itemTypeScript.getGithubDefaultHome(properties)
        githubTargetHome = githubDefaultHome
        targetDirectory = itemTypeScript.targetDirectory
        itemType = itemTypeScript.itemType
    }

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
        for (String itemName: items) {
            println "Starting retrieval for $itemType $itemName, are we recursing? $recurse"
            println "Retrieved so far: $itemsRetrieved"
            retrieveItem(itemName, recurse)
        }
    }

    /**
     * Retrieves a single item via Git Clone. Considers whether it exists locally first or if it has already been retrieved this execution.
     * @param itemName the target item to retrieve
     * @param recurse whether to also retrieve its dependencies (if so then recurse back into retrieve)
     */
    def retrieveItem(String itemName, boolean recurse) {
        File targetDir = new File("$targetDirectory" + "/$itemName")
        println "Request to retrieve $itemType $itemName would store it at $targetDir - exists? " + targetDir.exists()
        if (targetDir.exists()) {
            println "That $itemType already had an existing directory locally. If something is wrong with it please delete and try again"
            itemsRetrieved << itemName
        } else if (itemsRetrieved.contains(itemName)) {
            println "We already retrieved $itemName - skipping"
        } else {
            itemsRetrieved << itemName
            def targetUrl = "https://github.com/${githubTargetHome}/${itemName}"
            if (!isUrlValid(targetUrl)) {
                println "Can't retrieve $itemType from $targetUrl - URL appears invalid. Typo? Not created yet?"
                return
            }
            println "Retrieving $itemType $itemName from $targetUrl"
            if (githubTargetHome != githubDefaultHome) {
                println "Doing a retrieve from a custom remote: $githubTargetHome - will name it as such plus add the $githubDefaultHome remote as 'origin'"
                Grgit.clone dir: targetDir, uri: targetUrl, remote: githubTargetHome
                println "Primary clone operation complete, about to add the 'origin' remote for the $githubDefaultHome org address"
                addRemote(itemName, "origin", "https://github.com/${githubDefaultHome}/${itemName}")
            } else {
                Grgit.clone dir: targetDir, uri: targetUrl
            }

            // This step allows the item type to check the newly cloned item and add in extra template stuff
            itemTypeScript.copyInTemplateFiles(targetDir)

            // Handle also retrieving dependencies if the item type cares about that
            if (recurse) {
                def foundDependencies = itemTypeScript.findDependencies(targetDir)
                if (foundDependencies.length == 0) {
                    println "The $itemType $itemName did not appear to have any dependencies we need to worry about"
                } else {
                    println "The $itemType $itemName has the following $itemType dependencies we care about: $foundDependencies"
                    String[] uniqueDependencies = foundDependencies - itemsRetrieved
                    println "After removing dupes already retrieved we have the remaining dependencies left: $uniqueDependencies"
                    if (uniqueDependencies.length > 0) {
                        retrieve(uniqueDependencies, true)
                    }
                }
            }
        }
    }

    /**
     * Creates a new item with the given name and adds the necessary .gitignore file plus more if the itemType desires
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

        // For now everything gets the same .gitignore, but beyond that defer to the itemType for specifics
        println "Creating .gitignore"
        File gitignore = new File(targetDir, ".gitignore")
        def gitignoreText = new File("templates/.gitignore").text
        gitignore << gitignoreText

        itemTypeScript.copyInTemplateFiles(targetDir)

        Grgit.init dir: targetDir, bare: false
        addRemote(itemName, "origin", "https://github.com/${githubDefaultHome}/${itemName}.git")
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
        if (!new File(targetDirectory, itemName).exists()) {
            println "$itemType '$itemName' not found. Typo? Or run 'groovyw $itemType get $itemName' first"
            return
        }
        def remoteGit = Grgit.open(dir: "${targetDirectory}/${itemName}")
        def remote = remoteGit.remote.list()
        def index = 1
        for (Remote item: remote) {
            println(index + " " + item.name + " (" + item.url + ")")
            index++
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
            println "$itemType '$itemName' not found. Typo? Or run 'groovyw $itemType get $itemName' first"
            return
        }
        def remoteGit = Grgit.open(dir: "${targetDirectory}/${itemName}")
        def remote = remoteGit.remote.list()
        def check = remote.find {
            it.name == "$remoteName"
        }
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
        def remoteArg = arguments.findLastIndexOf {
            it == "-remote"
        }
        if (remoteArg != -1) {
            if (arguments.length == (remoteArg + 1)) {
                githubTargetHome = getUserString('Enter name for the git remote (no spaces)')
                arguments = arguments.dropRight(1)
            } else {
                githubTargetHome = arguments[remoteArg + 1]
                arguments = arguments.dropRight(2)
            }
        }
        return arguments.drop(1)
    }
}
