// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.cli
import picocli.CommandLine.Command
import picocli.CommandLine.Help.Ansi
// Is in use, IDE may think the Groovy-supplied is in use below and mark this unused
import picocli.CommandLine.Mixin
import picocli.CommandLine.Parameters

@Command(name = "init", description = "Initializes a workspace with some useful things")
class Init extends BaseCommand implements Runnable {

    /** The name of the distro, if given. Optional parameter (the arity = 0..1 bit) */
    @Parameters(paramLabel = "distro", arity = "0..1", defaultValue = "sample", description = "Target module distro to prepare locally")
    String distro

    /** Mix in a variety of supported Git extras */
    @Mixin
    GitOptions gitOptions

    void run() {
        String str = Ansi.AUTO.string("@|bold,green,underline Time to initialize $distro !|@")
        System.out.println(str)
        println "Do we have a Git origin override? " + gitOptions.origin
        println "Can has desired global prop? " + PropHelper.getGlobalProp("alternativeGithubHome")
        // Call logic elsewhere
    }
}
