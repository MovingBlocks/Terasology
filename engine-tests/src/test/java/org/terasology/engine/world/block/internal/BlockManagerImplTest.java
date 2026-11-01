// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.terasology.engine.TerasologyTestingEnvironment;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.engine.world.block.tiles.NullWorldAtlas;
import org.terasology.gestalt.assets.management.AssetManager;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Regression tests: a client-side {@link BlockManagerImpl} (generateNewIds=false) queried before any block
 * family is registered used to recurse forever between getBlock(BlockUri) and getAirBlock() (StackOverflowError).
 */
@Tag("TteTest")
public class BlockManagerImplTest extends TerasologyTestingEnvironment {

    private BlockManagerImpl blockManager;

    @BeforeEach
    public void setup() throws Exception {
        super.setup();
        blockManager = new BlockManagerImpl(new NullWorldAtlas(), CoreRegistry.get(AssetManager.class), false);
    }

    @Test
    public void getBlockOfAirIdReturnsNullInsteadOfRecursing() {
        assertNull(blockManager.getBlock(BlockManager.AIR_ID));
    }

    @Test
    public void getAirBlockFallbackThrowsInsteadOfRecursing() {
        assertThrows(IllegalStateException.class, () -> blockManager.getBlock(new BlockUri("engine:doesnotexist")));
    }
}
