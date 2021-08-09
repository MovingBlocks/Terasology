// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands

import org.terasology.cli.options.GitOptions
import picocli.CommandLine

abstract class ItemCommand extends BaseCommandType {
    abstract void get(GitOptions options, boolean recurse, List<String> items);
    abstract void copyInTemplates(File targetDir);
    abstract String[] getDependencies(File targetDir, boolean respectExcludedItems = true);

}
