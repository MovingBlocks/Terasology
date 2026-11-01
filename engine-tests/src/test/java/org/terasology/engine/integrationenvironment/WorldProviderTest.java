// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.integrationenvironment;

import org.joml.Vector3i;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockManager;

@IntegrationEnvironment
public class WorldProviderTest {

    @In
    WorldProvider worldProvider;
    @In
    BlockManager blockManager;
    @In
    MainLoop mainLoop;

    @Test
    public void defaultWorldSetBlockTest() {
        mainLoop.forceAndWaitForGeneration(new Vector3i());

        // this will change if the worldgenerator changes or the seed is altered, the main point is that this is a real
        // block type and not engine:unloaded
        Assertions.assertEquals("engine:air", worldProvider.getBlock(0, 0, 0).getURI().toString());

        // also verify that we can set and immediately get blocks from the worldprovider
        worldProvider.setBlock(new Vector3i(), blockManager.getBlock("engine:unloaded"));
        Assertions.assertEquals("engine:unloaded", worldProvider.getBlock(0, 0, 0).getURI().toString());
    }
}
