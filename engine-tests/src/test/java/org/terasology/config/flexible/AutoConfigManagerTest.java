// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.config.flexible;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.PathManager;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.persistence.typeHandling.gson.GsonPersistedDataReader;
import org.terasology.engine.persistence.typeHandling.gson.GsonPersistedDataSerializer;
import org.terasology.engine.persistence.typeHandling.gson.GsonPersistedDataWriter;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.gestalt.naming.Name;
import org.terasology.persistence.serializers.Serializer;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;

import java.nio.file.Path;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AutoConfigManagerTest {
    private static final Name PROVIDING_MODULE = new Name("unittest");
    private final Gson gson = new Gson();

    private final TypeHandlerLibrary typeHandlerLibrary = mock(TypeHandlerLibrary.class);
    private final AutoConfigManager autoConfigManager = new AutoConfigManager(new Serializer<>(
            typeHandlerLibrary,
            new GsonPersistedDataSerializer(),
            new GsonPersistedDataWriter(gson),
            new GsonPersistedDataReader(gson)
    ));

    private final Context context = mock(Context.class);
    private final ModuleManager moduleManager = mock(ModuleManager.class);
    private final ModuleEnvironment environment = mock(ModuleEnvironment.class);

    @BeforeEach
    public void setUp(@TempDir Path tempHome) throws Exception {
        PathManager.getInstance().useOverrideHomePath(tempHome);

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
