// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.testUtil;

import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.module.ModuleManagerImpl;

public final class ModuleManagerFactory {
    private ModuleManagerFactory() { }

    public static ModuleManager create() throws Exception {
        return new ModuleManagerImpl("");
    }
}
