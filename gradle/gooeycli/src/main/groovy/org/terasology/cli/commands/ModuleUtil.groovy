// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.cli.commands

import org.terasology.cli.util.Constants

class ModuleUtil {
    static List<ModuleItem> allModules() {
        List<ModuleItem> result = []
        Constants.ModuleDirectory.eachDir({ dir ->
            result << new ModuleItem(dir.getName())
        })
        return result
    }

}
