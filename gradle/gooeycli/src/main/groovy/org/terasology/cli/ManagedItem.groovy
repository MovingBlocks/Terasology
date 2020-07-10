// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.cli
@GrabResolver(name = 'jcenter', root = 'http://jcenter.bintray.com/')
@Grab(group = 'org.ajoberstar', module = 'grgit', version = '1.9.3')
import org.ajoberstar.grgit.Grgit

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

    String githubTargetHome
    abstract String getDefaultItemGitOrigin()

    ManagedItem() {
        displayName = getDisplayName()
        targetDirectory = getTargetDirectory()
        githubTargetHome = calculateGitOrigin(null)
    }

    ManagedItem(String optionGitOrigin) {
        displayName = getDisplayName()
        targetDirectory = getTargetDirectory()
        githubTargetHome = calculateGitOrigin(optionGitOrigin)
    }

    String calculateGitOrigin(String optionOrigin) {
        // If the user indicated a target Git origin via option parameter then use that (primary choice)
        if (optionOrigin != null) {
            println "We have an option set for Git origin so using that: " + optionOrigin
            return optionOrigin
        }

        // Alternatively if the user has a global override set for Git origin then use that (secondary choice)
        String altOrigin = PropHelper.getGlobalProp("alternativeGithubHome")
        if (altOrigin != null) {
            println "There was no option set but we have a global proper override for Git origin: " + altOrigin
            return altOrigin
        }

        // And finally if neither override is set fall back on the default defined by the item type
        println "No option nor global override set for Git origin so using default for the type: " + getDefaultItemGitOrigin()
        return getDefaultItemGitOrigin()
    }

    // TODO: Likely everything below should just delegate to more specific classes to keep things tidy
    // TODO: That would allow these methods to later just figure out exact required operations then delegate
    // TODO: Should make it easier to hide the logic of (for instance) different Git adapters behind the next step

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
    def retrieve(List<String> items, boolean recurse) {
        println "Now inside retrieve, user (recursively? $recurse) wants: $items"
        for (String itemName : items) {
            println "Starting retrieval for $displayName $itemName, are we recursing? $recurse"
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
        File itemDir = new File(targetDirectory, itemName)
        println "Request to retrieve $displayName $itemName would store it at $itemDir - exists? " + itemDir.exists()
        if (itemDir.exists()) {
            println "That $displayName already had an existing directory locally. If something is wrong with it please delete and try again"
            itemsRetrieved << itemName
        } else if (itemsRetrieved.contains(itemName)) {
            println "We already retrieved $itemName - skipping"
        } else {
            itemsRetrieved << itemName
            def targetUrl = "https://github.com/${githubTargetHome}/${itemName}"
            if (!isUrlValid(targetUrl)) {
                println "Can't retrieve $displayName from $targetUrl - URL appears invalid. Typo? Not created yet?"
                return
            }
            println "Retrieving $displayName $itemName from $targetUrl"
            if (githubTargetHome != getDefaultItemGitOrigin()) {
                println "Doing a retrieve from a custom remote: $githubTargetHome - will name it as such plus add the ${getDefaultItemGitOrigin()} remote as '$defaultRemote'"
                Grgit.clone dir: itemDir, uri: targetUrl, remote: githubTargetHome
                println "Primary clone operation complete, about to add the '$defaultRemote' remote for the ${getDefaultItemGitOrigin()} org address"
                //addRemote(itemName, defaultRemote, "https://github.com/${getDefaultItemGitOrigin()}/${itemName}") //TODO: Add me :p
            } else {
                println "Cloning $targetUrl to $itemDir"
                Grgit.clone dir: itemDir, uri: targetUrl
            }
/*
            // This step allows the item type to check the newly cloned item and add in extra template stuff - TODO?
            //itemTypeScript.copyInTemplateFiles(itemDir)

            // Handle also retrieving dependencies if the item type cares about that
            if (recurse) {
                def foundDependencies = itemTypeScript.findDependencies(itemDir)
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
            }*/
        }
    }
}
