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

package org.terasology.world;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.HealthComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.event.AddComponentEvent;
import org.terasology.entitySystem.event.ChangedComponentEvent;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.performanceMonitor.PerformanceMonitor;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockEntityMode;
import org.terasology.world.block.BlockRegionComponent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * @author Immortius
 */
public class EntityAwareWorldProvider extends AbstractWorldProviderDecorator implements BlockEntityRegistry, EventHandlerSystem, UpdateSubscriberSystem {
    @In
    EntityManager entityManager;

    // TODO: Perhaps a better datastructure for spatial lookups
    // TODO: Or perhaps a build in indexing system for entities
    private Map<Vector3i, EntityRef> blockComponentLookup = Maps.newHashMap();

    private Map<Vector3i, EntityRef> blockRegionLookup = Maps.newHashMap();
    private Map<EntityRef, Region3i> blockRegions = Maps.newHashMap();

    private List<EntityRef> tempBlocks = Lists.newArrayList();

    private Thread mainThread;
    private BlockingQueue<BlockChangedEvent> eventQueue = Queues.newLinkedBlockingQueue();

    public EntityAwareWorldProvider(WorldProviderCore base) {
        super(base);
        mainThread = Thread.currentThread();
    }

    @Override
    public void initialise() {
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
    public EntityRef getBlockEntityAt(Vector3i blockPosition) {
        EntityRef result = blockComponentLookup.get(blockPosition);
        return (result == null) ? EntityRef.NULL : result;
    }

    @Override
    public EntityRef getOrCreateBlockEntityAt(Vector3i blockPosition) {
        EntityRef blockEntity = getBlockEntityAt(blockPosition);
        if (!blockEntity.exists()) {
            Block block = getBlock(blockPosition.x, blockPosition.y, blockPosition.z);
            blockEntity = entityManager.create(block.getEntityPrefab());
            if (block.getEntityMode() == BlockEntityMode.ON_INTERACTION) {
                tempBlocks.add(blockEntity);
            }
            setupBlockEntity(blockPosition, blockEntity, block);
        }
        return blockEntity;
    }

    private void setupBlockEntity(Vector3i blockPosition, EntityRef blockEntity, Block block) {
        blockEntity.addComponent(new LocationComponent(blockPosition.toVector3f()));
        blockEntity.addComponent(new BlockComponent(blockPosition, block.getEntityMode() == BlockEntityMode.ON_INTERACTION));
        // TODO: Get regen and wait from block config?
        if (block.isDestructible()) {
            blockEntity.addComponent(new HealthComponent(block.getHardness(), 2.0f, 1.0f));
        }
    }

    @Override
    public EntityRef getEntityAt(Vector3i blockPosition) {
        EntityRef result = blockRegionLookup.get(blockPosition);
        if (result == null) {
            return getBlockEntityAt(blockPosition);
        }
        return result;
    }

    @Override
    public EntityRef getOrCreateEntityAt(Vector3i blockPosition) {
        EntityRef entity = getEntityAt(blockPosition);
        if (!entity.exists()) {
            return getOrCreateBlockEntityAt(blockPosition);
        }
        return entity;
    }

    @Override
    public void replaceEntityAt(Vector3i blockPosition, EntityRef entity) {
        replaceEntityAt(blockPosition, entity, getBlock(blockPosition.x, blockPosition.y, blockPosition.z));
    }

    private void replaceEntityAt(Vector3i blockPosition, EntityRef entity, Block blockType) {
        EntityRef oldEntity = blockComponentLookup.put(blockPosition, entity);
        if (oldEntity != null) {
            oldEntity.destroy();
        }
        setupBlockEntity(blockPosition, entity, blockType);
    }

    @Override
    public boolean setBlock(int x, int y, int z, Block type, Block oldType, EntityRef entity) {
        return setBlock(new Vector3i(x, y, z), type, oldType, entity);
    }

    @Override
    public boolean setBlock(Vector3i pos, Block type, Block oldType, EntityRef entity) {
        if (setBlock(pos.x, pos.y, pos.z, type, oldType)) {
            replaceEntityAt(pos, entity, type);
            return true;
        }
        return false;
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

    @ReceiveEvent(components = {BlockRegionComponent.class})
    public void onNewBlockRegion(AddComponentEvent event, EntityRef entity) {
        BlockRegionComponent regionComp = entity.getComponent(BlockRegionComponent.class);
        blockRegions.put(entity, regionComp.region);
        for (Vector3i pos : regionComp.region) {
            blockRegionLookup.put(pos, entity);
        }
    }

    @ReceiveEvent(components = {BlockRegionComponent.class})
    public void onBlockRegionChanged(ChangedComponentEvent event, EntityRef entity) {
        Region3i oldRegion = blockRegions.get(entity);
        for (Vector3i pos : oldRegion) {
            blockRegionLookup.remove(pos);
        }
        BlockRegionComponent regionComp = entity.getComponent(BlockRegionComponent.class);
        blockRegions.put(entity, regionComp.region);
        for (Vector3i pos : regionComp.region) {
            blockRegionLookup.put(pos, entity);
        }
    }

    @ReceiveEvent(components = {BlockRegionComponent.class})
    public void onBlockRegionRemoved(RemovedComponentEvent event, EntityRef entity) {
        Region3i oldRegion = blockRegions.get(entity);
        for (Vector3i pos : oldRegion) {
            blockRegionLookup.remove(pos);
        }
        blockRegions.remove(entity);
    }

    @Override
    public void update(float delta) {
        int processed = 0;
        PerformanceMonitor.startActivity("BlockChangedEventQueue");
        BlockChangedEvent event = eventQueue.poll();
        while (event != null) {
            getOrCreateEntityAt(event.getBlockPosition()).send(event);
            if (processed++ >= 4) {
                break;
            }
            event = eventQueue.poll();
        }
        PerformanceMonitor.endActivity();
        PerformanceMonitor.startActivity("Temp Blocks Cleanup");
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
        PerformanceMonitor.endActivity();
    }
}
