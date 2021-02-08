// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.module;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.terasology.module.ModuleLoader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.terasology.engine.TerasologyConstants.MODULE_INFO_FILENAME;

public class ClasspathSupportingModuleLoaderTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "testdummy-1.0.0-SNAPSHOT.jar",
            "messy-name-directory & $t#f/testmessy-1.0.0-SNAPSHOT.jar",
            "Spacey Parent/SourceModuleFixture/"
    })
    void testLoadModuleFromJarURL(String jarLocation) throws IOException, URISyntaxException {
        ModuleLoader loader = new ClasspathSupportingModuleLoader(true, false);
        loader.setModuleInfoPath(MODULE_INFO_FILENAME);

        Path modulePath = Paths.get(getClass().getResource(jarLocation).toURI());

        assertNotNull(loader.load(modulePath));
    }
}
