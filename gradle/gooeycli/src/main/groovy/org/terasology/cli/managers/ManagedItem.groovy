// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.cli.managers

import org.terasology.cli.helpers.PropHelper
import org.terasology.cli.scm.ScmGet

import static org.terasology.cli.helpers.KitchenSink.green
import static org.terasology.cli.helpers.KitchenSink.yellow

/**
 * Utility class for dealing with items managed in a developer workspace.
 *
 * Primarily assists with the retrieval, creation, and updating of nested Git roots representing application elements.
 */
abstract class ManagedItem {

    /** For keeping a list of items retrieved so far (retrieval calls may be recursive) */
    def itemsRetrieved = []

    /** The default name of a git remote we might want to work on or keep handy */
    String defaultRemote = "origin" // TODO: Consider always naming remotes after their origin / account name?

    String displayName
    abstract String getDisplayName()

    File targetDirectory
    abstract File getTargetDirectory()

    String targetGitOrigin
    abstract String getDefaultItemGitOrigin()

    ManagedItem() {
        displayName = getDisplayName()
        targetDirectory = getTargetDirectory()
        targetGitOrigin = calculateGitOrigin(null)
    }

    ManagedItem(String optionGitOrigin) {
        displayName = getDisplayName()
        targetDirectory = getTargetDirectory()
        targetGitOrigin = calculateGitOrigin(optionGitOrigin)
    }

    /**
     * Initializer for the target origin to get resources from - option, workspace setting, or a default.
     * @param optionOrigin an alternative if the user submitted a git option via CLI
     * @return the highest priority override or the default
     */
    String calculateGitOrigin(String optionOrigin) {
        // If the user indicated a target Git origin via option parameter then use that (primary choice)
        if (optionOrigin != null) {
            green "We have an option set for Git origin so using that: " + optionOrigin
            return optionOrigin
        }

        // Alternatively if the user has a global override set for Git origin then use that (secondary choice)
        String altOrigin = PropHelper.getGlobalProp("alternativeGithubHome")
        if (altOrigin != null) {
            green "There was no option set but we have a global proper override for Git origin: " + altOrigin
            return altOrigin
        }

        // And finally if neither override is set fall back on the default defined by the item type
        println "No option nor global override set for Git origin so using default for the type: " + getDefaultItemGitOrigin()
        return getDefaultItemGitOrigin()
    }

    /**
     * The Get command simply retrieves one or more resources in one go. Easy!
     * @param items to retrieve
     */
    def get(List<String> items) {
        for (String itemName : items) {
            println "Going to retrieve $displayName $itemName"
            getItem(itemName)
        }
    }

    /**
     * More advanced version of the Get command that also retrieves any dependencies defined by the items.
     * @param items to retrieve
     * @return discovered dependencies from one round of processing (if used recursively)
     */
    List<String> recurse(List<String> items) {
        List<String> dependencies = []
        println "Going to retrieve the following $displayName item(s) recursively: $items"
        for (String item : items) {
            // Check for circular dependencies - we should only ever act on a request to *retrieve* an item once
            if (itemsRetrieved.contains(item)) {
                yellow  "Uh oh, we got told to re-retrieve the same thing for $item - somebody wrote a circular dependency? Skipping"
            } else {
                // We didn't already try to retrieve this item: get it (if we already have it then getItem will just be a no-op)
                getItem(item)
                // Then goes and checks the item on disk and parses the thing to see if it has dependencies (even if we already had it)
                def newDependencyCandidates = ((DependencyProvider) this).parseDependencies(targetDirectory, item)
                println "Got new dependency candidates: " + newDependencyCandidates
                dependencies += newDependencyCandidates - itemsRetrieved - dependencies
                println "Storing them without those already retrieved: " + dependencies
            }
            // Mark this item as retrieved - that way we'll disqualify it if it comes up again in the future
            itemsRetrieved << item
        }

        println "Finished recursively retrieving the following list: " + items
        dependencies -= itemsRetrieved
        println "After disqualifying any dependencies that were already in that list the following remains: " + dependencies

        // If we parsed any dependencies, retrieve them recursively (and even if we already got them check them for dependencies as well)
        if (!dependencies.isEmpty()) {
            println "Got dependencies to fetch so we'll recurse and go again!"
            return recurse(dependencies)
        }

        green "Finally done recursing, both initial items and any parsed dependencies"
        return null
    }

    /**
     * Simple one-item execution point for attempting to get a resource.
     * @param item the resource to get
     */
    void getItem(String item) {
        println "Processing get request for $item via SCM"
        // Logic for a single retrieve, no dependency parsing involved, nor Git origin tweaking - already handled
        ScmGet.cloneRepo(item, targetGitOrigin, targetDirectory, displayName)

        // TODO: Consider supporting copying in template files at this point if the type requests that
    }
}
