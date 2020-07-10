// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.cli
import picocli.CommandLine.ParentCommand
import picocli.CommandLine.Command
import picocli.CommandLine.Mixin
import picocli.CommandLine.Parameters

// Sub-sub-command that works on item-oriented sub-commands
// Would mix-in GitOptions to vary the origin if indicated by the user
// Distinct from the sibling command add-remote which does *not* mix in GitOptions
@Command(name = "recurse",
        description = "Gets one or more items and all their dependencies")
class Recurse extends BaseCommand implements Runnable {

    @ParentCommand
    ItemCommand parent

    /** Mix in a variety of supported Git extras */
    @Mixin
    GitOptions gitOptions

    @Parameters(paramLabel = "items", arity = "1", description = "Target item(s) to get, including their dependencies")
    List<String> items

    void run() {
        println "Going to recurse $items! And from origin: " + gitOptions.origin
        println "Command parent is: " + parent.getItemType()
    }
}
