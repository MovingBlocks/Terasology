// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.config.flexible;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.core.paths.PathManager;
import org.terasology.engine.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.gestalt.naming.Name;
import org.terasology.unittest.config.flexible.TestAutoConfig;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

//TODO move to testpackagemodule - gestalt v7.
public class AutoConfigManagerTest {
    private static final Name PROVIDING_MODULE = new Name("unittest");

    private final TypeHandlerLibrary typeHandlerLibrary = mock(TypeHandlerLibrary.class);
    private final AutoConfigManager autoConfigManager = new AutoConfigManager(typeHandlerLibrary);

    private final Context context = mock(Context.class);
    private final ModuleManager moduleManager = mock(ModuleManager.class);
    private final ModuleEnvironment environment = mock(ModuleEnvironment.class);

    @BeforeEach
    public void setUp() throws Exception {
        PathManager.getInstance().useOverrideHomePath(
                Paths.get(System.getProperty("java.io.tmpdir"),
                        "terasology",
                        UUID.randomUUID().toString()));

        when(environment.getModuleProviding(any())).thenReturn(PROVIDING_MODULE);
        when(environment.getSubtypesOf(eq(AutoConfig.class))).thenReturn(Collections.singleton(TestAutoConfig.class));

        when(moduleManager.getEnvironment()).thenReturn(environment);

        when(context.get(eq(ModuleManager.class))).thenReturn(moduleManager);
    }

    @Test
    public void testLoad() {
        autoConfigManager.loadConfigsIn(context);

        ArgumentCaptor<TestAutoConfig> argumentCaptor = ArgumentCaptor.forClass(TestAutoConfig.class);
        verify(context).put(eq(TestAutoConfig.class), argumentCaptor.capture());

        TestAutoConfig value = argumentCaptor.getValue();
        assertEquals(new SimpleUri(PROVIDING_MODULE, TestAutoConfig.class.getName()), value.getId());
    }
}
