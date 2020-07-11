// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.scm

@GrabResolver(name = 'jcenter', root = 'http://jcenter.bintray.com/')
@Grab(group = 'org.ajoberstar', module = 'grgit', version = '1.9.3')
import org.ajoberstar.grgit.Grgit

import static org.terasology.cli.helpers.KitchenSink.isUrlValid

/**
 * Generic wrapper around SCM operations resulting in the retrieval of a resource
 */
class ScmGet {
    static cloneRepo(String itemName, String targetGitOrigin, File targetDirectory, String displayName) {
        File itemDir = new File(targetDirectory, itemName)
        println "Request to retrieve $displayName $itemName would store it at $itemDir - exists? " + itemDir.exists()
        if (itemDir.exists()) {
            println "That $displayName already had an existing directory locally. If something is wrong with it please delete and try again"
        } else {
            def targetUrl = "https://github.com/${targetGitOrigin}/${itemName}"
            if (!isUrlValid(targetUrl)) {
                println "Can't retrieve $displayName from $targetUrl - URL appears invalid. Typo? Not created yet?"
                return
            }
            println "Retrieving $displayName $itemName from $targetUrl"
            Grgit.clone dir: itemDir, uri: targetUrl, remote: targetGitOrigin
        }
    }
}
