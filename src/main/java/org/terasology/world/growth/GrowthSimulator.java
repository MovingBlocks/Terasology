/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.world.growth;

import com.google.common.collect.Queues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.RegisterMode;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.monitoring.ThreadActivity;
import org.terasology.monitoring.ThreadMonitor;
import org.terasology.world.OnChangedBlock;
import org.terasology.world.WorldBiomeProvider;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.Chunk;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Immortius
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class GrowthSimulator implements ComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(GrowthSimulator.class);

    @In
    private BlockManager blockManager;

    @In
    private WorldProvider world;

    private Block air;
    private Block grass;
    private Block dirt;
    private BlockingQueue<Vector3i> blockQueue;
    private ExecutorService executor;

    private AtomicBoolean running = new AtomicBoolean();

    @Override
    public void initialise() {
        air = BlockManager.getAir();
        grass = blockManager.getBlock("engine:grass");
        dirt = blockManager.getBlock("engine:dirt");
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
                        try (ThreadActivity ignored = ThreadMonitor.startThreadActivity("Simulate")) {
                            if (world.isBlockRelevant(blockPos)) {
                                if (simulate(blockPos)) {
                                    Thread.sleep(5000);
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        ThreadMonitor.addError(e);
                        logger.debug("Thread Interrupted", e);
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
            logger.error("Interrupted awaiting shutdown", e);
        }
        if (blockQueue.isEmpty()) {
            executor.shutdownNow();
        }
    }

    @ReceiveEvent(components = BlockComponent.class)
    public void blockChanged(OnChangedBlock event, EntityRef blockEntity) {
        if (dirt.equals(event.getNewType())) {
            blockQueue.offer(event.getBlockPosition());
        }

        if (dirt.equals(event.getNewType()) || air.equals(event.getNewType())) {
            for (Side side : Side.values()) {
                Vector3i adjPos = new Vector3i(event.getBlockPosition());
                adjPos.add(side.getVector3i());
                if (dirt.equals(world.getBlock(adjPos))) {
                    blockQueue.offer(adjPos);
                }
            }
        }
    }

    public boolean simulate(Vector3i blockPos) {
        final byte sunlight = world.getSunlight(new Vector3i(blockPos.x, blockPos.y + 1, blockPos.z));
        final boolean isDirt = dirt.equals(world.getBlock(blockPos));
        if (isDirt && sunlight == Chunk.MAX_LIGHT) {
            WorldBiomeProvider.Biome biome = world.getBiomeProvider().getBiomeAt(blockPos.x, blockPos.z);

            if (biome.isVegetationFriendly()) {
                Block bLeft = world.getBlock(blockPos.x - 1, blockPos.y, blockPos.z);
                Block bRight = world.getBlock(blockPos.x + 1, blockPos.y, blockPos.z);
                Block bUp = world.getBlock(blockPos.x, blockPos.y, blockPos.z + 1);
                Block bDown = world.getBlock(blockPos.x, blockPos.y, blockPos.z - 1);

                if (bLeft == grass || bRight == grass || bDown == grass || bUp == grass) {
                    if (world.setBlock(blockPos, grass, dirt)) {

                        if (bLeft == dirt) {
                            blockQueue.offer(new Vector3i(blockPos.x - 1, blockPos.y, blockPos.z));
                        }

                        if (bRight == dirt) {
                            blockQueue.offer(new Vector3i(blockPos.x + 1, blockPos.y, blockPos.z));
                        }

                        if (bUp == dirt) {
                            blockQueue.offer(new Vector3i(blockPos.x, blockPos.y, blockPos.z + 1));
                        }

                        if (bDown == dirt) {
                            blockQueue.offer(new Vector3i(blockPos.x, blockPos.y, blockPos.z - 1));
                        }

                        return true;
                    }
                }
            }
        }
        return false;
    }
}
