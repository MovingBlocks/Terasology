/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.core.debug;

import java.util.HashMap;
import java.util.Map;

import org.terasology.context.Context;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.Region3i;
import org.joml.Vector3i;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;

/**
 * Can benchmark either {@link WorldProvider#setBlock(Vector3i, Block)} or {@link WorldProvider#setBlocks(Map)}
 * depending on a constructor argument.
 */
class BlockPlacementBenchmark extends AbstractBenchmarkInstance {
    private final WorldProvider worldProvider;
    private final Region3i region3i;
    private final Block air;
    private final Block stone;
    private final boolean useSetBlocksInsteadOfSetBlock;
    private Block blockToPlace;

    BlockPlacementBenchmark(Context context, boolean useSetBlocksInsteadOfSetBlock) {
        this.worldProvider = context.get(org.terasology.world.WorldProvider.class);
        LocalPlayer localPlayer = context.get(LocalPlayer.class);
        this.region3i = BenchmarkScreen.getChunkRegionAbove(localPlayer.getPosition());
        BlockManager blockManager = context.get(BlockManager.class);
        this.stone = blockManager.getBlock("CoreBlocks:Stone");
        this.useSetBlocksInsteadOfSetBlock = useSetBlocksInsteadOfSetBlock;
        this.air = blockManager.getBlock("engine:air");
        blockToPlace = stone;
    }

    @Override
    public void runStep() {
        if (useSetBlocksInsteadOfSetBlock) {
            Map<Vector3i, Block> blocksToPlace = new HashMap<>();
            for (Vector3i v : region3i) {
                blocksToPlace.put(v, blockToPlace);
            }
            worldProvider.setBlocks(blocksToPlace);
        } else {
            for (Vector3i v : region3i) {
                worldProvider.setBlock(v, blockToPlace);
            }
        }
        if (blockToPlace == stone) {
            blockToPlace = air;
        } else {
            blockToPlace = stone;
        }
    }
}
