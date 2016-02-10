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

package org.terasology.world.internal;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Context;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.GameThread;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.ComponentContainer;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.entity.internal.EntityChangeSubscriber;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeEntityCreated;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.metadata.ComponentMetadata;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.network.NetworkComponent;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.OnChangedBlock;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.regions.BlockRegionComponent;

import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 */
public class EntityAwareWorldProvider extends AbstractWorldProviderDecorator implements BlockEntityRegistry, UpdateSubscriberSystem, EntityChangeSubscriber {
    private static final Logger logger = LoggerFactory.getLogger(EntityAwareWorldProvider.class);
    private static final Set<Class<? extends Component>> COMMON_BLOCK_COMPONENTS =
            ImmutableSet.of(NetworkComponent.class, BlockComponent.class, LocationComponent.class);
    private static final float BLOCK_REGEN_SECONDS = 4.0f;

    private EngineEntityManager entityManager;

    // TODO: Perhaps a better datastructure for spatial lookups
    // TODO: Or perhaps a build in indexing system for entities
    private Map<Vector3i, EntityRef> blockEntityLookup = Maps.newHashMap();

    private Map<Vector3i, EntityRef> blockRegionLookup = Maps.newHashMap();
    private Map<EntityRef, Region3i> blockRegions = Maps.newHashMap();

    private Set<EntityRef> temporaryBlockEntities = Sets.newLinkedHashSet();

    public EntityAwareWorldProvider(WorldProviderCore base, Context context) {
        super(base);
        entityManager = (EngineEntityManager) context.get(EntityManager.class);
        context.get(ComponentSystemManager.class).register(getTime());
    }

    @Override
    public void initialise() {
        entityManager.subscribeForChanges(this);
    }

    @Override
    public void preBegin() {
    }

    @Override
    public void postBegin() {
    }

    @Override
    public void preSave() {
    }

    @Override
    public void postSave() {
    }

    @Override
    public void shutdown() {
        entityManager.unsubscribe(this);
    }

    @Override
    public Block setBlock(Vector3i pos, Block type) {
        if (GameThread.isCurrentThread()) {
            EntityRef blockEntity = getBlockEntityAt(pos);
            Block oldType = super.setBlock(pos, type);
            if (oldType != null) {
                updateBlockEntity(blockEntity, pos, oldType, type, false, Collections.<Class<? extends Component>>emptySet());
            }
            return oldType;
        }
        return null;
    }

    @Override
    @SafeVarargs
    public final Block setBlockRetainComponent(Vector3i pos, Block type, Class<? extends Component>... components) {
        if (GameThread.isCurrentThread()) {
            EntityRef blockEntity = getBlockEntityAt(pos);
            Block oldType = super.setBlock(pos, type);
            if (oldType != null) {
                updateBlockEntity(blockEntity, pos, oldType, type, false, Sets.newHashSet(components));
            }
            return oldType;
        }
        return null;
    }

    private void updateBlockEntity(EntityRef blockEntity, Vector3i pos, Block oldType, Block type,
                                   boolean forceEntityUpdate, Set<Class<? extends Component>> retainComponents) {
        if (type.isKeepActive()) {
            temporaryBlockEntities.remove(blockEntity);
        } else if (oldType.isKeepActive() && isTemporaryBlock(blockEntity, type)) {
            temporaryBlockEntities.add(blockEntity);
        }
        if (forceEntityUpdate || !(Objects.equal(oldType.getBlockFamily(), type.getBlockFamily()) && Objects.equal(oldType.getPrefab(), type.getPrefab()))) {
            updateBlockEntityComponents(blockEntity, oldType, type, retainComponents);
        }
        EntityRef regionEntity = blockRegionLookup.get(pos);
        if (regionEntity != null) {
            regionEntity.send(new OnChangedBlock(pos, type, oldType));
        }
        blockEntity.send(new OnChangedBlock(new Vector3i(pos), type, oldType));
    }

    @Override
    public EntityRef getExistingBlockEntityAt(Vector3i blockPosition) {
        if (GameThread.isCurrentThread()) {
            EntityRef result = blockEntityLookup.get(blockPosition);
            return (result == null) ? EntityRef.NULL : result;
        }
        logger.error("Attempted to get block entity off-thread");
        return EntityRef.NULL;
    }

    @Override
    public Block setBlockForceUpdateEntity(Vector3i pos, Block type) {
        if (GameThread.isCurrentThread()) {
            EntityRef blockEntity = getBlockEntityAt(pos);
            Block oldType = super.setBlock(pos, type);
            if (oldType != null) {
                updateBlockEntity(blockEntity, pos, oldType, type, true, Collections.<Class<? extends Component>>emptySet());
            }
            return oldType;
        }
        return null;
    }

    @Override
    public EntityRef getBlockEntityAt(Vector3f position) {
        Vector3i pos = new Vector3i(position, RoundingMode.HALF_UP);
        return getBlockEntityAt(pos);
    }

    @Override
    public EntityRef getBlockEntityAt(Vector3i blockPosition) {
        if (GameThread.isCurrentThread()) {
            EntityRef blockEntity = getExistingBlockEntityAt(blockPosition);
            if (!blockEntity.exists() && isBlockRelevant(blockPosition.x, blockPosition.y, blockPosition.z)) {
                Block block = getBlock(blockPosition.x, blockPosition.y, blockPosition.z);
                blockEntity = createBlockEntity(blockPosition, block);
            }
            return blockEntity;
        }
        logger.error("Attempted to get block entity off-thread");
        return EntityRef.NULL;
    }

    private boolean isTemporaryBlock(ComponentContainer entity, Block block) {
        return isTemporaryBlock(entity, block, null);
    }

    private boolean isTemporaryBlock(ComponentContainer entity, Block block, Class<? extends Component> ignoreComponent) {
        if (block.isKeepActive()) {
            return false;
        }

        for (ComponentMetadata<?> metadata : entityManager.getComponentLibrary().iterateComponentMetadata()) {
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
    private void updateBlockEntityComponents(EntityRef blockEntity, Block oldType, Block type, Set<Class<? extends Component>> retainComponents) {
        BlockComponent blockComponent = blockEntity.getComponent(BlockComponent.class);

        Optional<Prefab> oldPrefab = oldType.getPrefab();
        EntityBuilder oldEntityBuilder = entityManager.newBuilder(oldPrefab.orElse(null));
        oldEntityBuilder.addComponent(new BlockComponent(oldType, new Vector3i(blockComponent.getPosition())));
        BeforeEntityCreated oldEntityEvent = new BeforeEntityCreated(oldPrefab.orElse(null), oldEntityBuilder.iterateComponents());
        blockEntity.send(oldEntityEvent);
        for (Component comp : oldEntityEvent.getResultComponents()) {
            oldEntityBuilder.addComponent(comp);
        }

        Optional<Prefab> newPrefab = type.getPrefab();
        EntityBuilder newEntityBuilder = entityManager.newBuilder(newPrefab.orElse(null));
        newEntityBuilder.addComponent(new BlockComponent(type, new Vector3i(blockComponent.getPosition())));
        BeforeEntityCreated newEntityEvent = new BeforeEntityCreated(newPrefab.orElse(null), newEntityBuilder.iterateComponents());
        blockEntity.send(newEntityEvent);
        for (Component comp : newEntityEvent.getResultComponents()) {
            newEntityBuilder.addComponent(comp);
        }

        for (Component component : blockEntity.iterateComponents()) {
            if (!COMMON_BLOCK_COMPONENTS.contains(component.getClass())
                    && !entityManager.getComponentLibrary().getMetadata(component.getClass()).isRetainUnalteredOnBlockChange()
                    && !newEntityBuilder.hasComponent(component.getClass()) && !retainComponents.contains(component.getClass())) {
                blockEntity.removeComponent(component.getClass());
            }
        }


        blockComponent.setBlock(type);
        blockEntity.saveComponent(blockComponent);

        for (Component comp : newEntityBuilder.iterateComponents()) {
            copyIntoPrefab(blockEntity, comp, retainComponents);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Component> void copyIntoPrefab(EntityRef blockEntity, T comp, Set<Class<? extends Component>> retainComponents) {
        ComponentMetadata<T> metadata = entityManager.getComponentLibrary().getMetadata((Class<T>) comp.getClass());
        if (!blockEntity.hasComponent(comp.getClass())) {
            blockEntity.addComponent(metadata.copyRaw(comp));
        } else if (!metadata.isRetainUnalteredOnBlockChange() && !retainComponents.contains(metadata.getType())) {
            updateComponent(blockEntity, metadata, comp);
        }
    }

    private <T extends Component> void updateComponent(EntityRef blockEntity, ComponentMetadata<T> metadata, T targetComponent) {
        T currentComp = blockEntity.getComponent(metadata.getType());
        if (currentComp != null) {
            boolean changed = false;
            for (FieldMetadata<T, ?> field : metadata.getFields()) {
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
        EntityBuilder builder = entityManager.newBuilder(block.getPrefab().orElse(null));
        builder.addComponent(new LocationComponent(blockPosition.toVector3f()));
        builder.addComponent(new BlockComponent(block, blockPosition));
        boolean isTemporary = isTemporaryBlock(builder, block);
        if (!isTemporary && !builder.hasComponent(NetworkComponent.class)) {
            builder.addComponent(new NetworkComponent());
        }

        EntityRef blockEntity;
        if (isTemporary) {
            blockEntity = builder.buildWithoutLifecycleEvents();
            temporaryBlockEntities.add(blockEntity);
        } else {
            blockEntity = builder.build();
        }

        blockEntityLookup.put(new Vector3i(blockPosition), blockEntity);
        return blockEntity;
    }

    @Override
    public EntityRef getExistingEntityAt(Vector3i blockPosition) {
        if (GameThread.isCurrentThread()) {
            EntityRef result = blockRegionLookup.get(blockPosition);
            if (result == null) {
                return getExistingBlockEntityAt(blockPosition);
            }
            return result;
        }
        logger.error("Attempted to get block entity off-thread");
        return EntityRef.NULL;
    }

    @Override
    public EntityRef getEntityAt(Vector3i blockPosition) {
        if (GameThread.isCurrentThread()) {
            EntityRef entity = getExistingEntityAt(blockPosition);
            if (!entity.exists()) {
                return getBlockEntityAt(blockPosition);
            }
            return entity;
        }
        logger.error("Attempted to get block entity off-thread");
        return EntityRef.NULL;
    }

    @Override
    public boolean hasPermanentBlockEntity(Vector3i blockPos) {
        if (GameThread.isCurrentThread()) {
            EntityRef blockEntity = blockEntityLookup.get(blockPos);
            return blockEntity != null && !temporaryBlockEntities.contains(blockEntity);
        }
        logger.error("Attempted check whether a block entity is permanent, off thread");
        return false;
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
        PerformanceMonitor.startActivity("Temp Blocks Cleanup");
        List<EntityRef> toRemove = Lists.newArrayList(temporaryBlockEntities);
        temporaryBlockEntities.clear();
        toRemove.forEach(this::cleanUpTemporaryEntity);
        PerformanceMonitor.endActivity();
    }

    private void cleanUpTemporaryEntity(EntityRef entity) {
        Prefab prefab = entity.getParentPrefab();

        for (Component comp : entity.iterateComponents()) {
            if (!COMMON_BLOCK_COMPONENTS.contains(comp.getClass()) && (prefab == null || !prefab.hasComponent(comp.getClass()))) {
                entity.removeComponent(comp.getClass());
            }
        }
        entity.removeComponent(NetworkComponent.class);

        if (prefab != null) {
            for (Component comp : prefab.iterateComponents()) {
                Component currentComp = entity.getComponent(comp.getClass());
                if (currentComp == null) {
                    entity.addComponent(entityManager.getComponentLibrary().copy(comp));
                } else {
                    ComponentMetadata<?> metadata = entityManager.getComponentLibrary().getMetadata(comp.getClass());
                    boolean changed = false;
                    for (FieldMetadata field : metadata.getFields()) {
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

    @Override
    public void onReactivation(EntityRef entity, Collection<Component> components) {
        // TODO check if implementation makes sense
    }

    @Override
    public void onBeforeDeactivation(EntityRef entity, Collection<Component> components) {
        // TODO check if implementation makes sense
    }
}
