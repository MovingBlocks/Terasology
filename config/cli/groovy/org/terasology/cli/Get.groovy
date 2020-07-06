// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.cli
import picocli.CommandLine.ParentCommand
import picocli.CommandLine.Command
// Actually in use, annotation below may show syntax error due to Groovy's annotation by the same name. Works fine
import picocli.CommandLine.Mixin
import picocli.CommandLine.Parameters

/**
 * Sub-sub-command that works on item-oriented sub-commands.
 * Mixes-in GitOptions to vary the origin if indicated by the user
 * Distinct from the sibling command add-remote which does *not* mix in GitOptions since the command itself involves git remote
 */
@Command(name = "get", description = "Gets one or more items directly")
class Get extends BaseCommand implements Runnable {

    /** Reference to the parent item command so we can figure out what type it is */
    @ParentCommand
    ItemCommand parent

    /** Mix in a variety of supported Git extras */
    @Mixin
    GitOptions gitOptions

    @Parameters(paramLabel = "items", arity = "1", description = "Target item(s) to get")
    List<String> items

    void run() {
        println "Going to get $items! And from origin: " + gitOptions.origin

        // The parent should be a ManagedItem. Make an instace including the possible git origin option
        ManagedItem mi = parent.getManager(gitOptions.origin)

        // Having prepared an instance of the logic class we call it to actually retrieve stuff
        mi.retrieve(items, false)
    }
}
