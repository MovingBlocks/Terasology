/*
 * Copyright 2013 Moving Blocks
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

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.ComponentContainer;
import org.terasology.entitySystem.EngineEntityManager;
import org.terasology.entitySystem.EntityBuilder;
import org.terasology.entitySystem.EntityChangeSubscriber;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.internal.EntityInfoComponent;
import org.terasology.entitySystem.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.metadata.ComponentMetadata;
import org.terasology.entitySystem.metadata.FieldMetadata;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.health.HealthComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.network.NetworkComponent;
import org.terasology.performanceMonitor.PerformanceMonitor;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.regions.BlockRegionComponent;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

/**
 * @author Immortius
 */
public class EntityAwareWorldProvider extends AbstractWorldProviderDecorator implements BlockEntityRegistry, UpdateSubscriberSystem, EntityChangeSubscriber {

    private static final Set<Class<? extends Component>> COMMON_BLOCK_COMPONENTS = ImmutableSet.of(NetworkComponent.class, BlockComponent.class, LocationComponent.class, HealthComponent.class, EntityInfoComponent.class);

    private EngineEntityManager entityManager;

    // TODO: Perhaps a better datastructure for spatial lookups
    // TODO: Or perhaps a build in indexing system for entities
    private Map<Vector3i, EntityRef> blockEntityLookup = Maps.newHashMap();

    private Map<Vector3i, EntityRef> blockRegionLookup = Maps.newHashMap();
    private Map<EntityRef, Region3i> blockRegions = Maps.newHashMap();

    private Set<EntityRef> temporaryBlockEntities = Sets.newLinkedHashSet();

    private Thread mainThread;
    private BlockingQueue<OnChangedBlock> eventQueue = Queues.newLinkedBlockingQueue();

    public EntityAwareWorldProvider(WorldProviderCore base) {
        super(base);
        mainThread = Thread.currentThread();
        entityManager = (EngineEntityManager) CoreRegistry.get(EntityManager.class);
    }

    public EntityAwareWorldProvider(WorldProviderCore base, EngineEntityManager entityManager) {
        this(base);
        this.entityManager = entityManager;
    }

    @Override
    public void initialise() {
        entityManager.subscribe(this);
    }

    @Override
    public void shutdown() {
        entityManager.unsubscribe(this);
    }

    @Override
    public boolean setBlock(int x, int y, int z, Block type, Block oldType) {
        Vector3i pos = new Vector3i(x, y, z);
        EntityRef blockEntity = getBlockEntityAt(pos);
        if (super.setBlock(x, y, z, type, oldType)) {
            // TODO: fix threading handling
            if (Thread.currentThread().equals(mainThread)) {
                EntityRef regionEntity = blockRegionLookup.get(pos);
                if (regionEntity != null) {
                    regionEntity.send(new OnChangedBlock(pos, type, oldType));
                }
                updateBlockEntity(blockEntity, oldType, type);
                blockEntity.send(new OnChangedBlock(new Vector3i(x, y, z), type, oldType));
            } else {
                eventQueue.add(new OnChangedBlock(new Vector3i(x, y, z), type, oldType));
            }
            return true;
        }
        return false;
    }

    @Override
    public EntityRef getExistingBlockEntityAt(Vector3i blockPosition) {
        EntityRef result = blockEntityLookup.get(blockPosition);
        return (result == null) ? EntityRef.NULL : result;
    }

    @Override
    public EntityRef getBlockEntityAt(Vector3i blockPosition) {
        EntityRef blockEntity = getExistingBlockEntityAt(blockPosition);
        if (!blockEntity.exists() && isBlockRelevant(blockPosition.x, blockPosition.y, blockPosition.z)) {
            Block block = getBlock(blockPosition.x, blockPosition.y, blockPosition.z);
            blockEntity = createBlockEntity(blockPosition, block);
        }
        return blockEntity;
    }

    private boolean isTemporaryBlock(ComponentContainer entity, Block block) {
        return isTemporaryBlock(entity, block, null);
    }

    private boolean isTemporaryBlock(ComponentContainer entity, Block block, Class<? extends Component> ignoreComponent) {
        if (block.isKeepActive()) {
            return false;
        }

        for (ComponentMetadata<?> metadata : entityManager.getComponentLibrary()) {
            if (metadata.isForceBlockActive() && ignoreComponent != metadata.getType()) {
                if (entity.hasComponent(metadata.getType())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Transforms a block entity with the change of block type. This is driven from the delta between the old and new
     * block type prefabs, but takes into account changes made to the block entity.
     *
     * @param blockEntity The entity to update
     * @param oldType     The previous type of the block
     * @param type        The new type of the block
     */
    private void updateBlockEntity(EntityRef blockEntity, Block oldType, Block type) {
        Prefab oldPrefab = entityManager.getPrefabManager().getPrefab(oldType.getPrefab());
        Prefab newPrefab = entityManager.getPrefabManager().getPrefab(type.getPrefab());

        for (ComponentMetadata<?> metadata : entityManager.getComponentLibrary()) {
            if (!COMMON_BLOCK_COMPONENTS.contains(metadata.getType()) && !metadata.isRetainUnalteredOnBlockChange()
                    && (newPrefab == null || !newPrefab.hasComponent(metadata.getType()))) {
                blockEntity.removeComponent(metadata.getType());
            }
        }

        if (Objects.equal(newPrefab, oldPrefab)) {
            return;
        }

        if (newPrefab != null) {
            for (Component comp : newPrefab.iterateComponents()) {
                ComponentMetadata<?> metadata = entityManager.getComponentLibrary().getMetadata(comp.getClass());
                if (!blockEntity.hasComponent(comp.getClass())) {
                    blockEntity.addComponent(metadata.clone(comp));
                } else if (!metadata.isRetainUnalteredOnBlockChange()) {
                    updateComponent(blockEntity, metadata, comp);
                }
            }

            EntityInfoComponent entityInfo = blockEntity.getComponent(EntityInfoComponent.class);
            if (entityInfo == null) {
                entityInfo = new EntityInfoComponent();
                entityInfo.parentPrefab = newPrefab.getName();
                blockEntity.addComponent(entityInfo);
            } else if (!entityInfo.parentPrefab.equals(newPrefab.getName())) {
                entityInfo.parentPrefab = newPrefab.getName();
                blockEntity.saveComponent(entityInfo);
            }
        } else if (oldPrefab != null) {
            EntityInfoComponent entityInfo = blockEntity.getComponent(EntityInfoComponent.class);
            if (entityInfo != null) {
                entityInfo.parentPrefab = "";
                blockEntity.saveComponent(entityInfo);
            }
        }
    }

    private void updateComponent(EntityRef blockEntity, ComponentMetadata<?> metadata, Component targetComponent) {
        Component currentComp = blockEntity.getComponent(targetComponent.getClass());
        if (currentComp != null) {
            boolean changed = false;
            for (FieldMetadata field : metadata.iterateFields()) {
                Object newVal = field.getValue(targetComponent);
                if (!Objects.equal(field.getValue(currentComp), newVal)) {
                    field.setValue(currentComp, newVal);
                    changed = true;
                }
            }
            if (changed) {
                blockEntity.saveComponent(currentComp);
            }
        }
    }

    private EntityRef createBlockEntity(Vector3i blockPosition, Block block) {
        EntityBuilder builder = entityManager.newBuilder(block.getPrefab());
        builder.addComponent(new LocationComponent(blockPosition.toVector3f()));
        builder.addComponent(new BlockComponent(blockPosition));
        if (block.isDestructible() && !builder.hasComponent(HealthComponent.class)) {
            builder.addComponent(new HealthComponent(block.getHardness(), 2.0f, 1.0f));
        }
        boolean isTemporary = isTemporaryBlock(builder, block);
        if (!isTemporary && !builder.hasComponent(NetworkComponent.class)) {
            builder.addComponent(new NetworkComponent());
        }

        EntityRef blockEntity;
        if (isTemporary) {
            blockEntity = builder.buildNoEvents();
            temporaryBlockEntities.add(blockEntity);
        } else {
            blockEntity = builder.build();
        }

        blockEntityLookup.put(new Vector3i(blockPosition), blockEntity);
        return blockEntity;
    }

    @Override
    public EntityRef getExistingEntityAt(Vector3i blockPosition) {
        EntityRef result = blockRegionLookup.get(blockPosition);
        if (result == null) {
            return getExistingBlockEntityAt(blockPosition);
        }
        return result;
    }

    @Override
    public EntityRef getEntityAt(Vector3i blockPosition) {
        EntityRef entity = getExistingEntityAt(blockPosition);
        if (!entity.exists()) {
            return getBlockEntityAt(blockPosition);
        }
        return entity;
    }

    @ReceiveEvent(components = {BlockComponent.class})
    public void onActivateBlock(OnActivatedComponent event, EntityRef entity) {
        BlockComponent block = entity.getComponent(BlockComponent.class);
        EntityRef oldEntity = blockEntityLookup.put(new Vector3i(block.getPosition()), entity);
        // If this is a client, then an existing block entity may exist. Destroy it.
        if (oldEntity != null && !Objects.equal(oldEntity, entity)) {
            oldEntity.destroy();
        }
    }

    @ReceiveEvent(components = {BlockComponent.class})
    public void onDeactivateBlock(BeforeDeactivateComponent event, EntityRef entity) {
        BlockComponent block = entity.getComponent(BlockComponent.class);
        Vector3i pos = new Vector3i(block.getPosition());
        if (blockEntityLookup.get(pos) == entity) {
            blockEntityLookup.remove(pos);
        }
    }

    @ReceiveEvent(components = {BlockRegionComponent.class})
    public void onBlockRegionActivated(OnActivatedComponent event, EntityRef entity) {
        BlockRegionComponent regionComp = entity.getComponent(BlockRegionComponent.class);
        blockRegions.put(entity, regionComp.region);
        for (Vector3i pos : regionComp.region) {
            blockRegionLookup.put(pos, entity);
        }
    }

    @ReceiveEvent(components = {BlockRegionComponent.class})
    public void onBlockRegionChanged(OnChangedComponent event, EntityRef entity) {
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
    public void onBlockRegionDeactivated(BeforeDeactivateComponent event, EntityRef entity) {
        Region3i oldRegion = blockRegions.get(entity);
        for (Vector3i pos : oldRegion) {
            blockRegionLookup.remove(pos);
        }
        blockRegions.remove(entity);
    }

    @Override
    public void update(float delta) {
        // TODO: This should be handled by the event system?
        int processed = 0;
        PerformanceMonitor.startActivity("BlockChangedEventQueue");
        OnChangedBlock event = eventQueue.poll();
        while (event != null) {
            getEntityAt(event.getBlockPosition()).send(event);
            if (processed++ >= 4) {
                break;
            }
            event = eventQueue.poll();
        }
        PerformanceMonitor.endActivity();

        PerformanceMonitor.startActivity("Temp Blocks Cleanup");
        for (EntityRef entity : temporaryBlockEntities) {
            cleanUpTemporaryEntity(entity);
        }
        temporaryBlockEntities.clear();
        PerformanceMonitor.endActivity();
    }

    private void cleanUpTemporaryEntity(EntityRef entity) {
        Prefab prefab = entity.getParentPrefab();
        if (prefab == null) {
            for (Component comp : entity.iterateComponents()) {
                if (!COMMON_BLOCK_COMPONENTS.contains(comp.getClass())) {
                    entity.removeComponent(comp.getClass());
                }
            }
        } else {
            for (Component comp : entity.iterateComponents()) {
                if (!COMMON_BLOCK_COMPONENTS.contains(comp.getClass()) && !prefab.hasComponent(comp.getClass())) {
                    entity.removeComponent(comp.getClass());
                }
            }
            for (Component comp : prefab.iterateComponents()) {
                Component currentComp = entity.getComponent(comp.getClass());
                if (currentComp == null) {
                    entity.addComponent(entityManager.getComponentLibrary().getMetadata(comp.getClass()).clone(comp));
                } else {
                    ComponentMetadata<?> metadata = entityManager.getComponentLibrary().getMetadata(comp.getClass());
                    boolean changed = false;
                    for (FieldMetadata field : metadata.iterateFields()) {
                        Object expected = field.getValue(comp);
                        if (!Objects.equal(expected, field.getValue(currentComp))) {
                            field.setValue(currentComp, expected);
                            changed = true;
                        }
                    }
                    if (changed) {
                        entity.saveComponent(currentComp);
                    }
                }
            }
        }
        entityManager.destroyEntityWithoutEvents(entity);
    }


    @Override
    public void onEntityComponentAdded(EntityRef entity, Class<? extends Component> component) {
        if (temporaryBlockEntities.contains(entity) && entityManager.getComponentLibrary().getMetadata(component).isForceBlockActive()) {
            temporaryBlockEntities.remove(entity);
            if (!entity.hasComponent(NetworkComponent.class)) {
                entity.addComponent(new NetworkComponent());
            }
        }
    }

    @Override
    public void onEntityComponentChange(EntityRef entity, Class<? extends Component> component) {
    }

    @Override
    public void onEntityComponentRemoved(EntityRef entity, Class<? extends Component> component) {
        if (entityManager.getComponentLibrary().getMetadata(component).isForceBlockActive()) {
            BlockComponent blockComp = entity.getComponent(BlockComponent.class);
            if (blockComp != null) {
                Block block = getBlock(blockComp.getPosition().x, blockComp.getPosition().y, blockComp.getPosition().z);
                if (isTemporaryBlock(entity, block, component)) {
                    temporaryBlockEntities.add(entity);
                }
            }
        }
    }
}
