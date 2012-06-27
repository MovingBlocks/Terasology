/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.logic.world.liquid;

import com.google.common.collect.Queues;
import org.terasology.components.world.BlockComponent;
import org.terasology.components.world.WorldComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.logic.world.BlockChangedEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.world.WorldProvider;
import org.terasology.logic.world.WorldView;
import org.terasology.logic.world.chunks.ChunkReadyEvent;
import org.terasology.math.Side;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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

    private static byte MAX_LIQUID_DEPTH = 0x7;

    private Logger logger = Logger.getLogger(getClass().getName());

    private WorldProvider world;
    private Block air;
    private Block grass;
    private Block snow;
    private Block dirt;
    private Block water;
    private Block lava;
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
        water = BlockManager.getInstance().getBlock("Water");
        lava = BlockManager.getInstance().getBlock("Lava");

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
                            WorldView view = world.getWorldViewAround(TeraMath.calcChunkPos(blockPos));
                            if (view != null && view.isValidView()) {
                                simulate(blockPos, view);
                            }
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

    @ReceiveEvent(components = WorldComponent.class)
    public void chunkReady(ChunkReadyEvent event, EntityRef worldEntity) {

    }

    @ReceiveEvent(components = BlockComponent.class)
    public void blockChanged(BlockChangedEvent event, EntityRef blockEntity) {
        if (!event.getNewType().isLiquid()) {
            LiquidData currentState = world.getLiquid(event.getBlockPosition());
            if (currentState.getDepth() > 0) {
                world.setLiquid(event.getBlockPosition(), new LiquidData(), currentState);
            }
            blockQueue.offer(event.getBlockPosition());
        } else {
            LiquidData currentState = world.getLiquid(event.getBlockPosition());
            if (currentState.getDepth() == 0) {
                world.setLiquid(event.getBlockPosition(), new LiquidData((water.equals(event.getNewType())) ? LiquidType.WATER : LiquidType.LAVA, MAX_LIQUID_DEPTH), currentState);
            }

            for (Side side : Side.values()) {
                Vector3i adjPos = new Vector3i(event.getBlockPosition());
                adjPos.add(side.getVector3i());
                blockQueue.offer(adjPos);
            }
        }
    }

    public void simulate(Vector3i blockPos, WorldView view) {
        Block block = view.getBlock(blockPos);
        LiquidData current = view.getLiquid(blockPos);
        LiquidData newState = calcStateFor(blockPos, view);
        if (!newState.equals(current)) {
            logger.log(Level.INFO, "Setting " + blockPos + " to depth " + newState.getDepth());
            if (world.setLiquid(blockPos, newState, current)) {
                world.setBlock(blockPos, ((newState.getType() == LiquidType.WATER) ? water : lava), block);
                Vector3i belowBlockPos = new Vector3i(blockPos.x, blockPos.y - 1, blockPos.z);
                Block belowType = world.getBlock(belowBlockPos);
                if (grass.equals(belowType) || snow.equals(belowType)) {
                    world.setBlock(belowBlockPos, dirt, belowType);
                }
            }
        }
    }

    public static LiquidData calcStateFor(Vector3i pos, WorldView worldView) {
        Block block = worldView.getBlock(pos);
        if (isLiquidBlocking(block)) {
            return new LiquidData();
        }

        // Check if full/source location
        LiquidData currentState = worldView.getLiquid(pos);
        if (currentState.getDepth() == MAX_LIQUID_DEPTH) {
            return currentState;
        }

        LiquidData aboveState = worldView.getLiquid(pos.x, pos.y + 1, pos.z);
        if (aboveState.getDepth() > 0) {
            return new LiquidData(aboveState.getType(), MAX_LIQUID_DEPTH);
        }

        LiquidData h1 = new LiquidData();
        LiquidData h2 = new LiquidData();
        for (Side side : Side.horizontalSides()) {
            Vector3i adjDir = side.getVector3i();
            LiquidData state = worldView.getLiquid(pos.x + adjDir.x, pos.y, pos.z + adjDir.z);
            Block supportingBlock = worldView.getBlock(pos.x + adjDir.x, pos.y - 1, pos.z + adjDir.z);

            // TODO: Improve supporting block calculation (needs to not include grass, but include liquids)
            if (!supportingBlock.isPenetrable()) {
                if (state.getDepth() > h1.getDepth()) {
                    h2 = h1;
                    h1 = state;
                } else if (state.getDepth() > h2.getDepth()) {
                    h2 = state;
                }
            }
        }

        if (h1.getDepth() > 0) {
            if (h2.getDepth() == 0) {
                return new LiquidData(h1.getType(), h1.getDepth() - 1);
            } else if (h1.getType() == h2.getType()) {
                if (h1.getDepth() == h2.getDepth()) {
                    return new LiquidData(h1.getType(), h1.getDepth());
                }
                return new LiquidData(h1.getType(), (byte)(h1.getDepth() - 1));
            } else {
                byte finalDepth = (byte)(h1.getDepth() - h2.getDepth() - 1);
                if (finalDepth > 0) {
                    return new LiquidData(h1.getType(), finalDepth);
                }
            }
        }

        return new LiquidData();
    }

    private static boolean isLiquidBlocking(Block block) {
        return !block.isPenetrable();
    }

}
