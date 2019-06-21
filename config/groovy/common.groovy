import groovy.json.JsonSlurper

@Grab(group='org.slf4j', module='slf4j-api', version='1.6.1')
@Grab(group='org.slf4j', module='slf4j-nop', version='1.6.1')

@GrabResolver(name = 'jcenter', root = 'http://jcenter.bintray.com/')
@Grab(group = 'org.ajoberstar', module = 'grgit', version = '1.9.3')
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.exception.GrgitException
import org.ajoberstar.grgit.Remote

class common {

    /** For preparing an instance of the target item type utility class we want to work with */
    GroovyObject itemTypeScript

    /** The official default GitHub home (org/user) for the type */
    def githubDefaultHome

    /** The actual target GitHub home (org/user) for the type, as potentially requested by the user */
    def githubTargetHome

    /** The target directory for the type */
    File targetDirectory

    /** The clean human readable name for the type */
    def itemType

    /** Things we don't want to retrieve/update as they live in the main MovingBlocks/Terasology repo */
    def excludedItems

    /** For keeping a list of items retrieved so far */
    def itemsRetrieved = []

    /** The default name of a git remote we might want to work on or keep handy */
    String defaultRemote = "origin"

    /** Should we cache the list of remote items */
    boolean itemListCached = false

    /** For keeping a list of remote items that can be retrieved */
    String[] cachedItemList

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
     * @param recurse whether to also retrieve dependencies of the desired items (only really for modules ...)
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
        File targetDir = new File(targetDirectory, itemName)
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
                println "Doing a retrieve from a custom remote: $githubTargetHome - will name it as such plus add the $githubDefaultHome remote as '$defaultRemote'"
                Grgit.clone dir: targetDir, uri: targetUrl, remote: githubTargetHome
                println "Primary clone operation complete, about to add the '$defaultRemote' remote for the $githubDefaultHome org address"
                addRemote(itemName, defaultRemote, "https://github.com/${githubDefaultHome}/${itemName}")
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
        File targetDir = new File(targetDirectory, itemName)
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
        addRemote(itemName, defaultRemote, "https://github.com/${githubDefaultHome}/${itemName}.git")
    }

    /**
     * Update a given item.
     * @param itemName the name of the item to update
     */
    def updateItem(String itemName) {
        println "Attempting to update $itemType $itemName"
        File targetDir = new File(targetDirectory, itemName)
        if (!targetDir.exists()) {
            println "$itemType \"$itemName\" not found"
            return
        }
        def itemGit = Grgit.open(dir: targetDir)

        // Do a check for the default remote before we attempt to update
        def remotes = itemGit.remote.list()
        def targetUrl = remotes.find{
            it.name == defaultRemote
        }?.url
        if (targetUrl == null || !isUrlValid(targetUrl)) {
            println "While updating $itemName found its '$defaultRemote' remote invalid or its URL unresponsive: $targetUrl"
            return
        }

        // At this point we should have a valid remote to pull from. If local repo is clean then pull!
        def clean = itemGit.status().clean
        println "Is \"$itemName\" clean? $clean"
        if (!clean) {
            println "$itemType has uncommitted changes. Skipping."
            return
        }
        println "Updating $itemType $itemName"

        try {
            itemGit.pull remote: defaultRemote
        } catch (GrgitException exception) {
            println "Unable to update $itemName, Skipping: ${exception.getMessage()}"
        }
    }

    /**
     * List all existing Git remotes for a given item.
     * @param itemName the item to list remotes for
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
        File targetModule = new File(targetDirectory, itemName)
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

    /**
     * Retrieves all the available items for the target type in the form of a list.
     * @return a String[] containing the names of items available for download.
     */
    String[] retrieveAvailableItems() {
        if (itemListCached) {
            return cachedItemList
        }

        // TODO: We need better ways to display the result especially when it contains a lot of items
        // However, in some cases heavy filtering could still mean that very few items will actually display ...
        // Another consideration is if we should be more specific in the API request, like only retrieving name + description
        def githubHomeApiUrl = "https://api.github.com/users/$githubTargetHome/repos?per_page=99" //Note: 99 instead of 100  - see TODO below ..

        if(!isUrlValid(githubHomeApiUrl)){
            println "Deduced GitHub API URL $githubHomeApiUrl seems inaccessible."
            return []
        }

        // Make a temporary map of found repos (possible items) and the associated repo description (for filter options)
        def mappedPossibleItems = [:]
        def currentPageUrl = githubHomeApiUrl
        def slurper = new JsonSlurper()
        while (currentPageUrl) {
            //println "currentPageUrl: $currentPageUrl"
            new URL(currentPageUrl).openConnection().with { connection ->
                connection.content.withReader { reader ->
                    slurper.parseText(reader.text).each { item ->
                        mappedPossibleItems.put(item.name, item.description)
                        //println "Want to get item " + item.name
                    }
                }
                currentPageUrl = getLink(connection, "next")
                // TODO: This comparison is vulnerable to a page request size of "100" or anything that starts with a 1, but just using 99 above ..
                if (currentPageUrl.contains("page=1")) {
                    //println "The pagination warped back to page 1, we're done!"
                    currentPageUrl = null
                }
            }
        }

        String[] items = itemTypeScript.filterItemsFromApi(mappedPossibleItems)

        return items;
    }

    /**
     * Retrieves all the available items for the target type in the form of a list that match the specified regex.
     *
     * Forming the regex:
     * "\Q" starts an explicit quote (which includes all reserved characters as well)
     * "\E" ends an explicit quote
     * "\w*" is equivalent to "*" - it selects anything that has the same characters
     * (in the range of a-z, A-Z or 1-9) as before and after the asterisk
     * "." is equivalent to "?" - it selects anything that has the rest of the pattern but any
     * character in the "?" symbol's position
     * So, "\Q<INPUT_PART1>\E\w*\Q\<INPUT_PART1>E", selects anything that starts with INPUT_PART1
     * and ends with INPUT_PART2 - This regex expression is equivalent to the input argument
     * "INPUT_PART1*INPUT_PART2"
     *
     * @return a String[] containing the names of items available for download.
     * @param regex the regex that the retrieved items should match
     */
    String[] retrieveAvailableItemsWithRegexMatch(String regex) {
        ArrayList<String> selectedItems = new ArrayList<String>()
        String[] itemList = retrieveAvailableItems()
        for (String item : itemList) {
            if (item.matches(regex)) {
                selectedItems.add(item)
            }
        }

        return ((String[]) selectedItems.toArray());
    }

    /**
     * Retrieves all the available items for the target type in the form of a list that match the specified regex.
     *
     * @param wildcardPattern the wildcard pattern that the retrieved items should match
     * @return a String[] containing the names of items available for download.
     */
    String[] retrieveAvalibleItemsWithWildcardMatch(String wildcardPattern) {
        String regex = ("\\Q" + wildcardPattern.replace("*", "\\E\\w*\\Q").replace("?", "\\E.\\Q") + "\\E")
        return retrieveAvailableItemsWithRegexMatch(regex);
    }

    /**
     * Retrieves link from HTTP headers (RFC 5988).
     * @param connection connection to retrieve link from
     * @param relation relation type of requested link
     * @return link with the requested relation type
     */
    private static String getLink(URLConnection connection, String relation) {
        def links = connection.getHeaderField("Link")
        def linkMatcher = links =~ /<(.*)>;\s*rel="${relation}"/
        linkMatcher.find() ? linkMatcher.group(1) : null
    }

    /**
     * Retrieves all the downloaded items in the form of a list.
     * @return a String[] containing the names of downloaded items.
     */
    String[] retrieveLocalItems(){
        def localItems =[]
        targetDirectory.eachDir() { dir ->
            String itemName = dir.getName()
            // Don't consider excluded items
            if(!(excludedItems.contains(itemName))){
                localItems << itemName
            }
        }
        return localItems
    }

    void cacheItemList() {
        if (!itemListCached) {
            cachedItemList = retrieveAvailableItems()
        }
        itemListCached = true
    }

    void unCacheItemList() {
        itemListCached = false
    }
}
