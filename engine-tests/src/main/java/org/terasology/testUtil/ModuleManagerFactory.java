// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.testUtil;

import com.google.common.collect.Sets;
import org.terasology.engine.module.ModuleManager;
import org.terasology.module.Module;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.google.common.base.Verify.verify;
import static org.terasology.engine.TerasologyConstants.ENGINE_MODULE;
import static org.terasology.engine.TerasologyConstants.MODULE_INFO_FILENAME;

public final class ModuleManagerFactory {

    private ModuleManagerFactory() { }

    public static ModuleManager create() throws Exception {
        ModuleManager moduleManager = new ModuleManager("");
        loadUnitTestModule(moduleManager);
        return moduleManager;
    }

    public static void loadUnitTestModule(ModuleManager manager) throws IOException, URISyntaxException {
        Path myPath = Paths.get(ModuleManagerFactory.class.getResource("/" + MODULE_INFO_FILENAME).toURI()).getParent();
        Module testModule = manager.loadClasspathModule(myPath);
        //noinspection UnstableApiUsage
        verify(testModule.getMetadata().getId().toString().equals("unittest"),
                "Intended to load the unittest module but ended up with this instead: %s", testModule);
        manager.loadEnvironment(
                Sets.newHashSet(
                        manager.getRegistry().getLatestModuleVersion(ENGINE_MODULE),
                        testModule
                ), true);
    }
}
