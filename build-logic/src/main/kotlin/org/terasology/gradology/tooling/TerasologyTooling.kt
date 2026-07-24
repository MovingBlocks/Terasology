// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.gradology.tooling

import org.eclipse.jgit.api.errors.GitAPIException
import java.io.File
import java.util.Properties

/**
 * Kotlin port of config/groovy/common.groovy - the engine behind `groovyw <type> <sub-command>`,
 * now driven from a Gradle task instead of a standalone Groovy script. See [ItemType] for the
 * per-type (module/meta/lib/facade) configuration this operates against.
 */
class TerasologyTooling(val itemType: ItemType, val rootDir: File) {

    /** The official default GitHub home (org/user) for the type. */
    val githubDefaultHome: String = itemType.githubDefaultHome(loadOverrideProperties())

    /** The actual target GitHub home (org/user) for the type, as potentially requested by the user. */
    var githubTargetHome: String = githubDefaultHome

    val excludedItems: List<String> get() = itemType.excludedItems
    val targetDirectory: File = File(rootDir, itemType.targetDirName)

    /** Items retrieved so far this run, to avoid double work within a recursive retrieve. */
    private val itemsRetrieved = mutableListOf<String>()

    private var cachedItemList: List<String>? = null

    private val defaultRemote = "origin"

    private fun loadOverrideProperties(): Properties {
        val properties = Properties()
        val gradlePropsFile = File(rootDir, "gradle.properties")
        if (gradlePropsFile.exists()) {
            gradlePropsFile.inputStream().use { properties.load(it) }
        }
        return properties
    }

    fun getUserString(prompt: String): String {
        println("\n*** $prompt\n")
        return readLine().orEmpty()
    }

    /** Primary entry point for retrieving items, kicks off recursively if needed. */
    fun retrieve(items: List<String>, recurse: Boolean) {
        println("Now inside retrieve, user (recursively? $recurse) wants: $items")
        for (itemName in items) {
            println("Starting retrieval for ${itemType.itemType} $itemName, are we recursing? $recurse")
            println("Retrieved so far: $itemsRetrieved")
            retrieveItem(itemName, recurse)
        }
    }

    /**
     * Retrieves a single item via git clone. Considers whether it exists locally first or has
     * already been retrieved this execution.
     */
    fun retrieveItem(itemName: String, recurse: Boolean) {
        val targetDir = File(targetDirectory, itemName)
        println("Request to retrieve ${itemType.itemType} $itemName would store it at $targetDir - exists? ${targetDir.exists()}")
        when {
            targetDir.exists() -> {
                println("That ${itemType.itemType} already had an existing directory locally. If something is wrong with it please delete and try again")
                itemsRetrieved += itemName
            }
            itemsRetrieved.contains(itemName) -> {
                println("We already retrieved $itemName - skipping")
            }
            else -> {
                itemsRetrieved += itemName
                val targetUrl = "https://github.com/$githubTargetHome/$itemName"
                try {
                    println("Retrieving ${itemType.itemType} $itemName from $targetUrl")
                    if (githubTargetHome != githubDefaultHome) {
                        println("Doing a retrieve from a custom remote: $githubTargetHome - will name it as such plus add the $githubDefaultHome remote as '$defaultRemote'")
                        GitOps.clone(targetDir, targetUrl, remoteName = githubTargetHome)
                        println("Primary clone operation complete, about to add the '$defaultRemote' remote for the $githubDefaultHome org address")
                        addRemote(itemName, defaultRemote, "https://github.com/$githubDefaultHome/$itemName")
                    } else {
                        GitOps.clone(targetDir, targetUrl)
                    }
                } catch (exception: GitAPIException) {
                    println(Ansi.color("Unable to clone $itemName, Skipping: ${exception.message}", Ansi.RED))
                    return
                }

                // This step allows the item type to check the newly cloned item and add in extra template stuff.
                itemType.copyInTemplateFiles(rootDir, targetDir)

                if (recurse) {
                    val foundDependencies = itemType.findDependencies(targetDir)
                    if (foundDependencies.isEmpty()) {
                        println("The ${itemType.itemType} $itemName did not appear to have any dependencies we need to worry about")
                    } else {
                        println("The ${itemType.itemType} $itemName has the following ${itemType.itemType} dependencies we care about: $foundDependencies")
                        val uniqueDependencies = foundDependencies - itemsRetrieved.toSet()
                        println("After removing dupes already retrieved we have the remaining dependencies left: $uniqueDependencies")
                        if (uniqueDependencies.isNotEmpty()) {
                            retrieve(uniqueDependencies, true)
                        }
                    }
                }
            }
        }
    }

    /** Creates a new item with the given name and adds the necessary .gitignore file plus more if the type desires. */
    fun createItem(itemName: String) {
        val targetDir = File(targetDirectory, itemName)
        if (targetDir.exists()) {
            println("Target directory already exists. Aborting.")
            return
        }
        println("Creating target directory")
        targetDir.mkdirs()

        println("Creating .gitignore")
        val gitignore = File(targetDir, ".gitignore")
        gitignore.appendText(File(rootDir, "templates/.gitignore").readText())

        itemType.copyInTemplateFiles(rootDir, targetDir)

        GitOps.init(targetDir)
        addRemote(itemName, defaultRemote, "https://github.com/$githubDefaultHome/$itemName.git")
    }

    /** Update a given item: git pull latest from its 'origin' remote, if the workspace is clean. */
    fun updateItem(itemName: String, skipRecentUpdates: Boolean = false) {
        val targetDir = File(targetDirectory, itemName)
        if (itemName.isEmpty() || !itemName[0].isLetterOrDigit()) {
            println(Ansi.color("Skipping update for $itemName: starts with non-alphanumeric symbol", Ansi.YELLOW))
            return
        }
        if (!targetDir.exists()) {
            println(Ansi.color("${itemType.itemType} \"$itemName\" not found", Ansi.RED))
            return
        }
        try {
            GitOps.withRepo(targetDir) { git ->
                val targetUrl = git.remoteList().call().find { it.name == defaultRemote }
                if (targetUrl == null) {
                    println(Ansi.color("While updating $itemName remote `$defaultRemote` is not found.", Ansi.RED))
                    return@withRepo
                }

                val clean = git.status().call().isClean
                val branchName = git.repository.fullBranch ?: "unknown"

                print("${itemType.itemType} '$itemName' [$branchName]: ")

                if (!clean) {
                    println(Ansi.color("uncommitted changes. Skipping.", Ansi.YELLOW))
                    return@withRepo
                }

                println(Ansi.color("updating ${itemType.itemType} $itemName", Ansi.GREEN))

                val fetchHead = File(targetDir, ".git/FETCH_HEAD")
                if (fetchHead.exists()) {
                    val tenMinutesMs = 10 * 60 * 1000L
                    if (skipRecentUpdates && System.currentTimeMillis() - fetchHead.lastModified() < tenMinutesMs) {
                        println(Ansi.color("Skipping update for $itemName: updated within last 10 minutes", Ansi.YELLOW))
                        return@withRepo
                    }
                    fetchHead.setLastModified(System.currentTimeMillis())
                }

                try {
                    val beforeCommit = git.log().setMaxCount(1).call().firstOrNull()
                    git.pull().setRemote(defaultRemote).call()
                    val afterCommit = git.log().setMaxCount(1).call().firstOrNull()

                    if (beforeCommit != null && afterCommit != null && beforeCommit.name != afterCommit.name) {
                        val before = git.repository.newObjectReader().use { it.abbreviate(beforeCommit, 8).name() }
                        val after = git.repository.newObjectReader().use { it.abbreviate(afterCommit, 8).name() }
                        println(Ansi.color("Updating $before..$after", Ansi.GREEN))
                        val commits = git.log().addRange(beforeCommit, afterCommit).call().toList().asReversed()
                        for (commit in commits) {
                            val shortId = git.repository.newObjectReader().use { it.abbreviate(commit, 8).name() }
                            println("----$shortId---- ${commit.shortMessage}")
                        }
                    } else {
                        println(Ansi.color("No changes found", Ansi.YELLOW))
                    }
                } catch (exception: GitAPIException) {
                    println(Ansi.color("Unable to update $itemName, Skipping: ${exception.message}", Ansi.RED))
                }
            }
        } catch (exception: GitOps.NoRepository) {
            println(Ansi.color("Skipping update for $itemName: no repository found (probably engine module)", Ansi.LIGHT_YELLOW))
        }
    }

    /** List all existing Git remotes for a given item. */
    fun listRemotes(itemName: String) {
        if (!File(targetDirectory, itemName).exists()) {
            println("${itemType.itemType} '$itemName' not found. Typo? Or run 'gradle terasology ${itemType.itemType} get $itemName' first")
            return
        }
        val remotes = GitOps.withRepo(File(targetDirectory, itemName)) { git -> git.remoteList().call() }
        remotes.forEachIndexed { index, remote ->
            println("${index + 1} ${remote.name} (${remote.urIs.firstOrNull()})")
        }
    }

    fun addRemote(itemName: String, remoteName: String) {
        addRemote(itemName, remoteName, "https://github.com/$remoteName/$itemName.git")
    }

    /** Add a new Git remote for the given item. */
    fun addRemote(itemName: String, remoteName: String, url: String) {
        val targetModule = File(targetDirectory, itemName)
        if (!targetModule.exists()) {
            println("${itemType.itemType} '$itemName' not found. Typo? Or run 'gradle terasology ${itemType.itemType} get $itemName' first")
            return
        }
        GitOps.withRepo(targetModule) { git ->
            val exists = git.remoteList().call().any { it.name == remoteName }
            if (!exists) {
                git.remoteAdd().setName(remoteName).setUri(org.eclipse.jgit.transport.URIish(url)).call()
                if (GitHubApi.isUrlValid(url)) {
                    println("Successfully added remote '$remoteName' for '$itemName' - doing a 'git fetch'")
                    git.fetch().setRemote(remoteName).call()
                } else {
                    println("Added the remote '$remoteName' for ${itemType.itemType} '$itemName' - but the URL $url failed a test lookup. Typo? Not created yet?")
                }
            } else {
                println("Remote already exists, fetching latest")
                git.fetch().setRemote(remoteName).call()
            }
        }
    }

    /**
     * Considers given arguments for the presence of a custom remote, setting that up right if
     * found, tidying up the arguments.
     * @return the adjusted arguments without any found custom remote details or the command name itself.
     */
    fun processCustomRemote(arguments: List<String>): List<String> {
        var args = arguments
        val remoteArgIndex = args.lastIndexOf("-remote")
        if (remoteArgIndex != -1) {
            if (args.size == remoteArgIndex + 1) {
                githubTargetHome = getUserString("Enter name for the git remote (no spaces)")
                args = args.dropLast(1)
            } else {
                githubTargetHome = args[remoteArgIndex + 1]
                args = args.dropLast(2)
            }
        }
        return args.drop(1)
    }

    /** All available items for the target type, from the GitHub API (cached once per invocation). */
    fun retrieveAvailableItems(): List<String> {
        cachedItemList?.let { return it }

        val apiUrl = "https://api.github.com/users/$githubTargetHome/repos?per_page=99"
        if (!GitHubApi.isUrlValid(apiUrl)) {
            println("Deduced GitHub API URL $apiUrl seems inaccessible.")
            return emptyList()
        }

        val possibleItems = GitHubApi.listRepos(githubTargetHome)
        val items = itemType.filterItemsFromApi(possibleItems)
        cachedItemList = items
        return items
    }

    /**
     * Available items matching [wildcardPattern] (`*`/`?` glob syntax).
     */
    fun retrieveAvailableItemsWithWildcardMatch(wildcardPattern: String): List<String> {
        val regex = Regex(
            "\\Q" + wildcardPattern.replace("*", "\\E\\w*\\Q").replace("?", "\\E.\\Q") + "\\E"
        )
        return retrieveAvailableItems().filter { regex.matches(it) }
    }

    fun uncacheItemList() {
        cachedItemList = null
    }

    /** All locally-downloaded items for this type. */
    fun retrieveLocalItems(): List<String> =
        targetDirectory.listFiles { file -> file.isDirectory }
            ?.map { it.name }
            ?.filterNot { it in excludedItems }
            ?: emptyList()

    fun writeDependencyDotFileForModule(dependencyFile: File, module: File) {
        when {
            module.name.contains(".") -> {
                println("\"${module.name}\" is not a valid source (non-jar) module - skipping")
            }
            itemsRetrieved.contains(module.name) -> {
                println("Module \"${module.name}\" was already handled - skipping")
            }
            !module.exists() -> {
                println("Module \"${module.name}\" is not locally available - skipping")
                itemsRetrieved += module.name
            }
            else -> {
                val foundDependencies = itemType.findDependencies(module, respectExcludedItems = false)
                if (foundDependencies.isEmpty()) {
                    dependencyFile.appendText("  \"${module.name}\" -> \"engine\"\n")
                    itemsRetrieved += module.name
                } else {
                    for (dependency in foundDependencies) {
                        dependencyFile.appendText("  \"${module.name}\" -> \"$dependency\"\n")
                    }
                    itemsRetrieved += module.name

                    val uniqueDependencies = foundDependencies - itemsRetrieved.toSet()
                    for (dependency in uniqueDependencies) {
                        writeDependencyDotFileForModule(dependencyFile, File(targetDirectory, dependency))
                    }
                }
            }
        }
    }

    fun refreshGradle() {
        val localItems = targetDirectory.listFiles { file -> file.isDirectory }.orEmpty()
        for (dir in localItems) {
            if (dir.name in excludedItems) {
                println("Skipping $dir as it is in the exclude list")
            } else {
                itemType.refreshGradle(rootDir, dir)
            }
        }
    }
}
