// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.cli
import picocli.CommandLine.ParentCommand
import picocli.CommandLine.Command
import picocli.CommandLine.Mixin
import picocli.CommandLine.Parameters

@Command(name = "update",
        description = "Updates one or more items directly")
class Update extends BaseCommand implements Runnable {

    @ParentCommand
    ItemCommand parent

    /** Mix in a variety of supported Git extras */
    @Mixin
    GitOptions gitOptions

    @Parameters(paramLabel = "items", arity = "1", description = "Target item(s) to update")
    List<String> items

    void run() {
        println "Going to update $items! And from origin: " + gitOptions.origin
        println "Command parent is: " + parent.getItemType()
    }
}
