// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.cli.commands

import org.terasology.cli.managers.ManagedItem
import org.terasology.cli.options.GitOptions
import picocli.CommandLine.ParentCommand
import picocli.CommandLine.Command
import picocli.CommandLine.Mixin
import picocli.CommandLine.Parameters

// Sub-sub-command that works on item-oriented sub-commands
// Would mix-in GitOptions to vary the origin if indicated by the user
// Distinct from the sibling command add-remote which does *not* mix in GitOptions
@Command(name = "recurse",
        description = "Gets one or more items and all their dependencies")
class RecurseCommand extends BaseCommandType implements Runnable {

    @ParentCommand
    ItemCommandType parent

    /** Mix in a variety of supported Git extras */
    @Mixin
    GitOptions gitOptions

    @Parameters(paramLabel = "items", arity = "1", description = "Target item(s) to get, including their dependencies")
    List<String> items

    void run() {
        println "Going to recurse $items! And from origin: " + gitOptions.origin

        // The parent should be a ManagedItem. Make an instance including the possible git origin option
        ManagedItem mi = parent.getManager(gitOptions.origin)
        println "Got a parent command, associated item is: " + mi.getDisplayName()

        // Having prepared an instance of the logic class we call it to actually retrieve stuff
        mi.recurse(items)
    }
}
