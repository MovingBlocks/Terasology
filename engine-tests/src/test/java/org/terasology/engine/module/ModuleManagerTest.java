// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.module;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.terasology.module.ModuleLoader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.terasology.engine.TerasologyConstants.MODULE_INFO_FILENAME;

public class ModuleManagerTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "testdummy-1.0.0-SNAPSHOT.jar",
            "messy-name-directory & $t#f/testmessy-1.0.0-SNAPSHOT.jar"
    })
    void testLoadModuleFromJarURL(String jarLocation) throws IOException, URISyntaxException {
        ModuleManagerImpl mm = new ThisModuleManager("");
        ModuleLoader loader = new ModuleLoader();
        loader.setModuleInfoPath(MODULE_INFO_FILENAME);

        URL url = getClass().getResource(jarLocation);
        assumeTrue(url != null, "test resource not found:" + jarLocation);
        URL jarUrl = new URL("jar", null, url.toString() + "!/" + MODULE_INFO_FILENAME);

        assertNotNull(mm.load(loader, jarUrl));
    }

    @ParameterizedTest
    @ValueSource(strings = "Spacey Parent/SourceModuleFixture/")
    void testLoadModuleFromFileURL(String sourceLocation) throws IOException, URISyntaxException {
        ModuleManagerImpl mm = new ThisModuleManager("");
        ModuleLoader loader = new ModuleLoader();
        loader.setModuleInfoPath(MODULE_INFO_FILENAME);

        URL url = getClass().getResource(Paths.get(sourceLocation).resolve(MODULE_INFO_FILENAME).toString());
        assumeTrue(url != null, "test resource not found:" + sourceLocation);

        assertNotNull(mm.load(loader, url));
    }

    private static class ThisModuleManager extends ModuleManagerImpl {
        ThisModuleManager(String masterServerAddress) {
            super(masterServerAddress);
        }

        @Override
        void loadModulesFromClassPath() {
            // empty implementation so it doesn't fire everything off during the constructor
        }
    }
}
