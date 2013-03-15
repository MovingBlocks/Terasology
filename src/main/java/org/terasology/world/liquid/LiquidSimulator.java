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
package org.terasology.world.liquid;

import com.google.common.collect.Queues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.components.world.WorldComponent;
import org.terasology.entitySystem.ComponentSystem;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterSystem;
import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.world.BlockChangedEvent;
import org.terasology.world.WorldProvider;
import org.terasology.world.WorldView;
import org.terasology.world.block.Block;
import org.terasology.world.block.entity.BlockComponent;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkReadyEvent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Rough draft of Minecraft-like behavior of liquids. Will be replaced with some
 * more fancy stuff later on.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
@RegisterSystem
public class LiquidSimulator implements ComponentSystem {

    private static int NUM_THREADS = 2;
    private static byte MAX_LIQUID_DEPTH = 0x7;
    public static final int PROPAGATION_DELAY = 200;

    private static final Logger logger = LoggerFactory.getLogger(LiquidSimulator.class);

    @In
    private WorldProvider world;
    @In
    private BlockManager blockManager;
    private Block air;
    private Block grass;
    private Block snow;
    private Block dirt;
    private Block water;
    private Block lava;
    private BlockingQueue<LiquidSimulationTask> blockQueue;
    private ExecutorService executor;

    @Override
    public void initialise() {
        air = blockManager.getAir();
        grass = blockManager.getBlock("engine:Grass");
        snow = blockManager.getBlock("engine:Snow");
        dirt = blockManager.getBlock("engine:Dirt");
        water = blockManager.getBlock("engine:Water");
        lava = blockManager.getBlock("engine:Lava");

        blockQueue = Queues.newLinkedBlockingQueue();

        executor = Executors.newFixedThreadPool(NUM_THREADS);
        for (int i = 0; i < NUM_THREADS; ++i) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                    while (true) {
                        try {
                            LiquidSimulationTask task = blockQueue.take();
                            if (task.shutdownThread()) {
                                break;
                            }
                            task.run();
                        } catch (InterruptedException e) {
                            logger.debug("Interrupted");
                        } catch (Exception e) {
                            logger.error("Error in water simulation", e);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void shutdown() {
        executor.shutdown();
        for (int i = 0; i < NUM_THREADS; ++i) {
            blockQueue.offer(new LiquidSimulationTask() {
                @Override
                public boolean shutdownThread() {
                    return true;
                }

                @Override
                public void run() {
                }
            });
        }
        try {
            executor.awaitTermination(1, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error("Interrupted awaiting shutdown");
        }
        if (blockQueue.isEmpty()) {
            executor.shutdownNow();
        }
    }

    @ReceiveEvent(components = WorldComponent.class)
    public void chunkReady(ChunkReadyEvent event, EntityRef worldEntity) {
        blockQueue.offer(new ReviewChunk(event.getChunkPos()));
    }

    @ReceiveEvent(components = BlockComponent.class)
    public void blockChanged(BlockChangedEvent event, EntityRef blockEntity) {
        if (!event.getNewType().isLiquid()) {
            LiquidData currentState = world.getLiquid(event.getBlockPosition());
            if (currentState.getDepth() > 0) {
                world.setLiquid(event.getBlockPosition(), new LiquidData(), currentState);
            }
            if (event.getNewType().isPenetrable()) {
                blockQueue.offer(new SimulateBlock(event.getBlockPosition(), world.getTime() + PROPAGATION_DELAY));
            }
            for (Side side : Side.values()) {
                Vector3i adjPos = new Vector3i(event.getBlockPosition());
                adjPos.add(side.getVector3i());
                blockQueue.offer(new SimulateBlock(adjPos, world.getTime() + PROPAGATION_DELAY));
            }
        } else {
            LiquidData currentState = world.getLiquid(event.getBlockPosition());
            if (currentState.getDepth() == 0) {
                world.setLiquid(event.getBlockPosition(), new LiquidData((water.equals(event.getNewType())) ? LiquidType.WATER : LiquidType.LAVA, MAX_LIQUID_DEPTH), currentState);
            }

            for (Side side : Side.values()) {
                Vector3i adjPos = new Vector3i(event.getBlockPosition());
                adjPos.add(side.getVector3i());
                blockQueue.offer(new SimulateBlock(adjPos, world.getTime() + PROPAGATION_DELAY));
            }
        }
    }

    public void simulate(Vector3i blockPos, WorldView view) {
        Block block = view.getBlock(blockPos);
        LiquidData current = view.getLiquid(blockPos);
        LiquidData newState = calcStateFor(blockPos, view);
        if (!newState.equals(current)) {
            view.lock();
            try {
                if (view.isValidView() && world.setLiquid(blockPos, newState, current)) {
                    if (newState.getDepth() > 0) {
                        world.setBlock(blockPos, ((newState.getType() == LiquidType.WATER) ? water : lava), block);
                        Vector3i belowBlockPos = new Vector3i(blockPos.x, blockPos.y - 1, blockPos.z);
                        Block belowType = world.getBlock(belowBlockPos);
                        if (grass.equals(belowType) || snow.equals(belowType)) {
                            world.setBlock(belowBlockPos, dirt, belowType);
                        }
                    } else {
                        world.setBlock(blockPos, air, block);
                    }
                }
            } finally {
                view.unlock();
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
            return new LiquidData(aboveState.getType(), MAX_LIQUID_DEPTH - 1);
        }

        LiquidData h1 = new LiquidData();
        LiquidData h2 = new LiquidData();
        for (Side side : Side.horizontalSides()) {
            Vector3i adjPos = new Vector3i(side.getVector3i());
            adjPos.add(pos);
            Block supportingBlock = worldView.getBlock(adjPos.x, adjPos.y - 1, adjPos.z);

            // TODO: Improve supporting block calculation (needs to not include grass, but include liquids)
            if (!supportingBlock.isPenetrable()) {
                LiquidData state = getOutgoingLiquid(adjPos, worldView);
                if (state.getType() != currentState.getType() || state.getDepth() >= currentState.getDepth()) {
                    if (state.getDepth() > h1.getDepth()) {
                        h2 = h1;
                        h1 = state;
                    } else if (state.getDepth() > h2.getDepth()) {
                        h2 = state;
                    }
                }
            }
        }

        if (h1.getDepth() > 0) {
            if (h1.getType() == h2.getType() || h2.getDepth() == 0) {
                return new LiquidData(h1.getType(), h1.getDepth());
            } else {
                byte finalDepth = (byte) (h1.getDepth() - h2.getDepth());
                if (finalDepth > 0) {
                    return new LiquidData(h1.getType(), finalDepth);
                }
            }
        }

        return new LiquidData();
    }

    /**
     * Map of outgoing amounts of water, by number of available spaces (0-4) and depth (0-7)
     */
    private static byte[][] OUTGOING_FLOW = new byte[][]{
            // No where to go
            {0, 0, 0, 0, 0, 0, 0, 0},
            // One space
            {0, 0, 1, 2, 3, 4, 5, 6},
            // Two spaces
            {0, 0, 1, 2, 2, 3, 3, 4},
            // Three spaces
            {0, 0, 1, 1, 1, 2, 2, 3},
            // Four spaces
            {0, 0, 1, 1, 1, 1, 2, 2}
    };

    private static LiquidData getOutgoingLiquid(Vector3i pos, WorldView worldView) {
        LiquidData currentState = worldView.getLiquid(pos);
        if (currentState.getDepth() == 0) {
            return new LiquidData();
        }

        int availableSpaces = 0;
        for (Side side : Side.horizontalSides()) {
            Vector3i adjPos = new Vector3i(pos);
            adjPos.add(side.getVector3i());
            Block block = worldView.getBlock(pos);
            if (block.isPenetrable()) {
                LiquidData adjState = worldView.getLiquid(adjPos);
                if (adjState.getDepth() < currentState.getDepth()) {
                    availableSpaces++;
                }
            }
        }

        return new LiquidData(currentState.getType(), OUTGOING_FLOW[availableSpaces][currentState.getDepth()]);
    }

    private static boolean isLiquidBlocking(Block block) {
        return !block.isPenetrable();
    }

    private class SimulateBlock implements LiquidSimulationTask {

        private Vector3i blockPos;
        private long waitForTime;

        public SimulateBlock(Vector3i blockPos, long time) {
            this.blockPos = blockPos;
            this.waitForTime = time;
        }

        @Override
        public boolean shutdownThread() {
            return false;
        }

        @Override
        public void run() {
            if (world.getTime() < waitForTime) {
                blockQueue.offer(this);
            } else if (world.isBlockActive(blockPos)) {
                WorldView view = world.getWorldViewAround(TeraMath.calcChunkPos(blockPos));
                if (view != null && view.isValidView()) {
                    simulate(blockPos, view);
                }
            }
        }
    }

    private class ReviewChunk implements LiquidSimulationTask {
        private Vector3i chunkPos;

        public ReviewChunk(Vector3i chunkPos) {
            this.chunkPos = chunkPos;
        }

        @Override
        public boolean shutdownThread() {
            return false;
        }

        @Override
        public void run() {
            WorldView view = world.getLocalView(chunkPos);
            if (view != null) {
                for (Vector3i pos : Region3i.createFromMinAndSize(new Vector3i(-1, 0, -1), new Vector3i(Chunk.SIZE_X + 2, Chunk.SIZE_Y, Chunk.SIZE_Z + 2))) {
                    LiquidData state = view.getLiquid(pos);
                    LiquidData newState = calcStateFor(pos, view);
                    if (!newState.equals(state)) {
                        blockQueue.offer(new SimulateBlock(view.toWorldPos(pos), 0));
                    }
                }
            }
        }
    }

}
