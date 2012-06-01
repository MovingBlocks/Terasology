/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package org.terasology.componentSystem.worldSimulation;

import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.terasology.components.world.BlockComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.BlockChangedEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.world.WorldProvider;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.utilities.FastRandom;

import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Rough draft of Minecraft-like behavior of liquids. Will be replaced with some
 * more fancy stuff later on.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
@RegisterComponentSystem
public class LiquidSimulator implements EventHandlerSystem {

    private Logger logger = Logger.getLogger(getClass().getName());

    private WorldProvider world;
    private Block air;
    private Block grass;
    private Block snow;
    private Block dirt;
    private BlockingQueue<Vector3i> blockQueue;
    private ExecutorService executor;

    private AtomicBoolean running = new AtomicBoolean();

    @Override
    public void initialise() {
        world = CoreRegistry.get(WorldProvider.class);
        air = BlockManager.getInstance().getAir();
        grass = BlockManager.getInstance().getBlock("Grass");
        snow = BlockManager.getInstance().getBlock("Snow");
        dirt = BlockManager.getInstance().getBlock("Dirt");
        blockQueue = Queues.newLinkedBlockingQueue();
        running.set(true);

        executor = Executors.newFixedThreadPool(1);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                while (running.get()) {
                    try {
                        Vector3i blockPos = blockQueue.take();
                        if (world.isBlockActive(blockPos)) {
                            simulate(blockPos);
                        }
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        logger.log(Level.INFO, "Interrupted");
                    }
                }
            }
        });
    }

    @Override
    public void shutdown() {
        running.set(false);
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Interrupted awaiting shutdown");
        }
        if (blockQueue.isEmpty()) {
            executor.shutdownNow();
        }
    }

    @ReceiveEvent(components = BlockComponent.class)
    public void blockChanged(BlockChangedEvent event, EntityRef blockEntity) {
        if (event.getNewType().isLiquid()) {
            blockQueue.offer(event.getBlockPosition());
        } else if (event.getNewType() == BlockManager.getInstance().getAir()) {
            Block block = fillWithLiquid(event.getBlockPosition());
            if (block.isLiquid()) {
                world.setBlock(event.getBlockPosition(), block, event.getNewType());
                event.cancel();
            }
        } else if (event.getOldType().isLiquid()) {
            Vector3i belowPos = new Vector3i(event.getBlockPosition());
            belowPos.y -= 1;
            if (world.getBlock(belowPos).isLiquid()) {
                blockQueue.offer(belowPos);
            }
            for (Side side : Side.horizontalSides()) {
                Vector3i adjPos = new Vector3i(event.getBlockPosition());
                adjPos.add(side.getVector3i());
                if (world.getBlock(adjPos).isLiquid()) {
                    blockQueue.offer(belowPos);
                }
            }
        }
    }

    public void simulate(Vector3i blockPos) {
        Vector3i belowBlockPos = new Vector3i(blockPos.x, blockPos.y - 1, blockPos.z);
        Block type = world.getBlock(blockPos);

        if (!checkValidForLiquid(blockPos, type)) {
            world.setBlock(blockPos, air, type);
            if (world.getBlock(belowBlockPos).equals(type)) {
                blockQueue.offer(belowBlockPos);
            }
            for (Side side : Side.horizontalSides()) {
                Vector3i adjPos = new Vector3i(blockPos);
                adjPos.add(side.getVector3i());
                if (world.getBlock(adjPos).equals(type)) {
                    blockQueue.offer(adjPos);
                }
            }
            return;
        }


        Block belowType = world.getBlock(belowBlockPos);
        if (belowType.equals(air) || belowType.getBlockForm() == Block.BLOCK_FORM.BILLBOARD) {
            world.setBlock(belowBlockPos, type, belowType);
        }
        for (Side side : Side.horizontalSides()) {
            Vector3i adjPos = new Vector3i(blockPos);
            adjPos.add(side.getVector3i());
            Block adjType = world.getBlock(adjPos);
            if ((adjType.equals(air) || adjType.getBlockForm() == Block.BLOCK_FORM.BILLBOARD) && checkValidForLiquid(adjPos, type)) {
                world.setBlock(adjPos, type, adjType);
            }
        }

        // Convert grass and snow to dirt if water is above
        if (grass.equals(belowType) || snow.equals(belowType)) {
            world.setBlock(belowBlockPos, dirt, belowType);
        }
    }

    private boolean checkValidForLiquid(Vector3i blockPos, Block type) {
        Vector3i aboveBlockPos = new Vector3i(blockPos.x, blockPos.y + 1, blockPos.z);
        if (world.getBlock(aboveBlockPos).equals(type)) {
            return true;
        } else {
            int adjacent = 0;
            int adjacentGaps = 0;
            for (Side side : Side.horizontalSides()) {
                Vector3i adjPos = new Vector3i(blockPos);
                adjPos.add(side.getVector3i());

                Block adjType = world.getBlock(adjPos);
                if (adjType.equals(type)) {
                    adjPos.y -= 1;
                    Block adjSupport = world.getBlock(adjPos);
                    if (!(adjSupport.isLiquid() || adjSupport.equals(air))) {
                        adjacent++;
                    } else {
                        adjacentGaps++;
                    }
                } else if (adjType.equals(air) || adjType.getBlockForm() == Block.BLOCK_FORM.BILLBOARD) {
                    adjacentGaps++;
                }
            }
            return adjacentGaps == 0 || (adjacentGaps == 1 && adjacent > 0) || adjacent >= 1;
        }
    }

    private Block fillWithLiquid(Vector3i blockPos) {
        Vector3i aboveBlockPos = new Vector3i(blockPos.x, blockPos.y + 1, blockPos.z);
        Block aboveType = world.getBlock(aboveBlockPos);
        if (aboveType.isLiquid()) {
            return aboveType;
        }
        TObjectIntMap<Block> adjacencyMap = new TObjectIntHashMap<Block>();
        int gaps = 0;
        for (Side side : Side.horizontalSides()) {
            Vector3i adjPos = new Vector3i(blockPos);
            adjPos.add(side.getVector3i());

            Block adjType = world.getBlock(adjPos);
            if (adjType.isLiquid()) {
                adjPos.y -= 1;
                Block adjSupport = world.getBlock(adjPos);
                if (!(adjSupport.isLiquid() || adjSupport.equals(air))) {
                    adjacencyMap.adjustOrPutValue(adjType, 1, 1);
                }
            } else if (adjType.equals(air) || adjType.getBlockForm() == Block.BLOCK_FORM.BILLBOARD) {
                gaps++;
            }
        }
        if (adjacencyMap.size() > 0) {
            Block adjType = air;
            int count = 0;
            for (Block block : adjacencyMap.keySet()) {
                if (adjacencyMap.get(block) > count) {
                    adjType = block;
                    count = adjacencyMap.get(block);
                }
            }
            if (count > 1 || (count == 1 && gaps <= 1)) {
                return adjType;
            }
        }
        return air;
    }

}
