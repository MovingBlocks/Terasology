// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.cli.commands

import org.eclipse.jgit.api.Git
import org.terasology.cli.options.GitOptions
import org.terasology.cli.util.Constants
import picocli.CommandLine.Command
import picocli.CommandLine.Help.Ansi
import picocli.CommandLine.Mixin
import picocli.CommandLine.Parameters
import picocli.CommandLine.ParentCommand

// Is in use, IDE may think the Groovy-supplied is in use below and mark this unused

@Command(name = "init", description = "Initializes a workspace with some useful things")
class InitCommand extends BaseCommandType implements Runnable {

    @ParentCommand
    ItemCommand parent

    /** The name of the distro, if given. Optional parameter (the arity = 0..1 bit) */
    @Parameters(paramLabel = "distro", arity = "0..1", defaultValue = "sample", description = "Target module distro to prepare locally")
    String distro

    /** Mix in a variety of supported Git extras */
    @Mixin
    GitOptions gitOptions


    void run() {
        println Ansi.AUTO.string("@|bold,green,underline Time to initialize $distro !|@")
        def targetDistroURL = "https://raw.githubusercontent.com/$githubOrg/Index/master/distros/$distro/gradle.properties"
        URL distroContent = new URL(targetDistroURL)
        Properties property = new Properties()
        distroContent.withInputStream { strm ->
            property.load(strm)
        }

        if (property.contains("extraModules")) {
            String modules = property.get("extraModules")
            parent.get(gitOptions, true, modules.split(",").toList())
        } else {
            println "[init] ERROR: Distribution does not contain key: \"extraModules=\""
        }
    }
}
