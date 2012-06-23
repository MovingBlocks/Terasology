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

package org.terasology.logic.world;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.HealthComponent;
import org.terasology.components.world.BlockComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.event.AddComponentEvent;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.events.BlockChangedEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;

import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * @author Immortius
 */
public class EntityAwareWorldProvider extends AbstractWorldProviderDecorator implements BlockEntityRegistry, EventHandlerSystem, UpdateSubscriberSystem {

    private EntityManager entityManager;

    // TODO: Perhaps a better datastructure for spatial lookups
    // TODO: Or perhaps a build in indexing system for entities
    private Map<Vector3i, EntityRef> blockComponentLookup = Maps.newHashMap();

    private List<EntityRef> tempBlocks = Lists.newArrayList();

    private Thread mainThread;
    private Queue<BlockChangedEvent> eventQueue = Queues.newConcurrentLinkedQueue();

    public EntityAwareWorldProvider(WorldProviderCore base) {
        super(base);
        mainThread = Thread.currentThread();
    }

    @Override
    public void initialise() {
        this.entityManager = CoreRegistry.get(EntityManager.class);
        for (EntityRef blockComp : entityManager.iteratorEntities(BlockComponent.class)) {
            BlockComponent comp = blockComp.getComponent(BlockComponent.class);
            blockComponentLookup.put(new Vector3i(comp.getPosition()), blockComp);
        }

        for (EntityRef entity : entityManager.iteratorEntities(BlockComponent.class)) {
            BlockComponent blockComp = entity.getComponent(BlockComponent.class);
            if (blockComp.temporary) {
                HealthComponent health = entity.getComponent(HealthComponent.class);
                if (health == null || health.currentHealth == health.maxHealth || health.currentHealth == 0) {
                    entity.destroy();
                }
            }
        }
    }

    @Override
    public void shutdown() {

    }

    @Override
    public boolean setBlock(int x, int y, int z, Block type, Block oldType) {
        if (super.setBlock(x, y, z, type, oldType)) {
            if (Thread.currentThread().equals(mainThread)) {
                getOrCreateEntityAt(new Vector3i(x, y, z)).send(new BlockChangedEvent(new Vector3i(x, y, z), type, oldType));
            } else {
                eventQueue.add(new BlockChangedEvent(new Vector3i(x, y, z), type, oldType));
            }
            return true;
        }
        return false;
    }

    @Override
    public EntityRef getEntityAt(Vector3i blockPosition) {
        EntityRef result = blockComponentLookup.get(blockPosition);
        return (result == null) ? EntityRef.NULL : result;
    }

    @Override
    public EntityRef getOrCreateEntityAt(Vector3i blockPosition) {
        EntityRef blockEntity = blockComponentLookup.get(blockPosition);
        if (blockEntity == null || !blockEntity.exists()) {
            Block block = getBlock(blockPosition.x, blockPosition.y, blockPosition.z);
            if (block.getId() == 0)
                return EntityRef.NULL;

            blockEntity = entityManager.create(block.getEntityPrefab());
            if (block.isEntityTemporary()) {
                tempBlocks.add(blockEntity);
            }
            blockEntity.addComponent(new BlockComponent(blockPosition, block.isEntityTemporary()));
            // TODO: Get regen and wait from block config?
            if (block.isDestructible()) {
                blockEntity.addComponent(new HealthComponent(block.getHardness(), 2.0f, 1.0f));
            }
        }
        return blockEntity;
    }

    @ReceiveEvent(components = {BlockComponent.class})
    public void onCreate(AddComponentEvent event, EntityRef entity) {
        BlockComponent block = entity.getComponent(BlockComponent.class);
        blockComponentLookup.put(new Vector3i(block.getPosition()), entity);
    }

    @ReceiveEvent(components = {BlockComponent.class})
    public void onDestroy(RemovedComponentEvent event, EntityRef entity) {
        BlockComponent block = entity.getComponent(BlockComponent.class);
        blockComponentLookup.remove(new Vector3i(block.getPosition()));
    }

    @Override
    public void update(float delta) {
        int processed = 0;
        while (!eventQueue.isEmpty() && processed < 32) {
            BlockChangedEvent event = eventQueue.poll();
            getOrCreateEntityAt(event.getBlockPosition()).send(event);
            processed++;
        }
        for (EntityRef entity : tempBlocks) {
            BlockComponent blockComp = entity.getComponent(BlockComponent.class);
            if (blockComp == null || !blockComp.temporary)
                continue;

            HealthComponent healthComp = entity.getComponent(HealthComponent.class);
            if (healthComp == null || healthComp.currentHealth == healthComp.maxHealth) {
                entity.destroy();
            }
        }
        tempBlocks.clear();
    }
}
