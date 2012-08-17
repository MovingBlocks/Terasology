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

package org.terasology.componentSystem.worldSimulation;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.terasology.world.block.BlockComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.game.CoreRegistry;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.world.BlockChangedEvent;
import org.terasology.world.WorldBiomeProvider;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.Chunk;

import com.google.common.collect.Queues;

/**
 * @author Immortius
 */
@RegisterComponentSystem
public class GrowthSimulator implements EventHandlerSystem {

    private Logger logger = Logger.getLogger(getClass().getName());

    private WorldProvider world;
    private Block air;
    private Block grass;
    private Block dirt;
    private BlockingQueue<Vector3i> blockQueue;
    private ExecutorService executor;

    private AtomicBoolean running = new AtomicBoolean();

    @Override
    public void initialise() {
        world = CoreRegistry.get(WorldProvider.class);
        air = BlockManager.getInstance().getAir();
        grass = BlockManager.getInstance().getBlock("engine:grass");
        dirt = BlockManager.getInstance().getBlock("engine:dirt");
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
                            if (simulate(blockPos)) {
                                Thread.sleep(5000);
                            }
                        }
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
        if (dirt.equals(event.getNewType())) {
            blockQueue.offer(event.getBlockPosition());
        }

        if (dirt.equals(event.getNewType()) || air.equals(event.getNewType())) {
            for (Side side : Side.values()) {
                Vector3i adjPos = new Vector3i(event.getBlockPosition());
                adjPos.add(side.getVector3i());
                if (dirt.equals(world.getBlock(adjPos))) {
                    blockQueue.offer(event.getBlockPosition());
                }
            }
        }
    }

    public boolean simulate(Vector3i blockPos) {
        if (dirt.equals(world.getBlock(blockPos)) && world.getSunlight(new Vector3i(blockPos.x, blockPos.y + 1, blockPos.z)) == Chunk.MAX_LIGHT) {
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
