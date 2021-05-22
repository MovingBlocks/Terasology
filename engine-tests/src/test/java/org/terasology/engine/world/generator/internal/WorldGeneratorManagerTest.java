// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.generator.internal;

import com.google.common.truth.Correspondence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.engine.context.Context;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.testUtil.ModuleManagerFactory;

import static com.google.common.truth.Truth.assertThat;

class WorldGeneratorManagerTest {

    Context context;
    WorldGeneratorManager manager;

    @BeforeEach
    void provideContext() throws Exception {
        context = new ContextImpl();
        context.put(ModuleManager.class, ModuleManagerFactory.create());
    }

    @Test
    void hasUnittestWorldGenerator() {
        manager = new WorldGeneratorManager(context);
        assertThat(manager.getWorldGenerators())
                .comparingElementsUsing(
                        Correspondence.transforming(WorldGeneratorInfo::getUri, "info"))
                .contains(new SimpleUri("unittest:stub"));
    }
}
