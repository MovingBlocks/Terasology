// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.testUtil;

import org.terasology.engine.module.ModuleManager;

public final class ModuleManagerFactory {
    private ModuleManagerFactory() { }

    public static ModuleManager create() throws Exception {
        return new ModuleManager("");
    }
}
