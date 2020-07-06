// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.cli

import picocli.CommandLine.Command

/**
 * Simple super class for commands with global style options
 */
@Command(
        synopsisHeading      = "%n@|green Usage:|@%n%n",
        descriptionHeading   = "%n@|green Description:|@%n%n",
        parameterListHeading = "%n@|green Parameters:|@%n%n",
        optionListHeading    = "%n@|green Options:|@%n%n",
        commandListHeading   = "%n@|green Commands:|@%n%n")
class BaseCommand {

}
