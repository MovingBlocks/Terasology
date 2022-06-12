// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.integrationenvironment;

import org.junit.jupiter.api.Test;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.gestalt.assets.management.AssetManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.terasology.engine.testUtil.Assertions.assertNotEmpty;

@IntegrationEnvironment
public class AssetLoadingTest {

    @In
    private BlockManager blockManager;
    @In
    private AssetManager assetManager;

    @Test
    public void blockPrefabLoadingTest() {
        Block block = blockManager.getBlock("engine:air");
        assertNotNull(block);
        assertEquals(0, block.getHardness());
        assertEquals("Air", block.getDisplayName());
    }

    @Test
    public void simpleLoadingTest() {
        assertNotEmpty(assetManager.getAsset("engine:test", Prefab.class));
    }
}
