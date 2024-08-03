// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.internal;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.joml.RoundingMode;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.core.GameThread;
import org.terasology.engine.entitySystem.ComponentContainer;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.entity.internal.EntityChangeSubscriber;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeEntityCreated;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.engine.entitySystem.metadata.ComponentMetadata;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.common.RetainComponentsComponent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.monitoring.PerformanceMonitor;
import org.terasology.engine.network.NetworkComponent;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.OnChangedBlock;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.block.regions.BlockRegionComponent;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.reflection.metadata.FieldMetadata;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class EntityAwareWorldProvider extends AbstractWorldProviderDecorator
        implements BlockEntityRegistry, UpdateSubscriberSystem, EntityChangeSubscriber {
    private static final Logger logger = LoggerFactory.getLogger(EntityAwareWorldProvider.class);
    private static final Set<Class<? extends Component>> COMMON_BLOCK_COMPONENTS =
        ImmutableSet.of(NetworkComponent.class, BlockComponent.class, LocationComponent.class);

    private final EngineEntityManager entityManager;

    // TODO: Perhaps a better datastructure for spatial lookups
    // TODO: Or perhaps a build in indexing system for entities
    private final Map<Vector3ic, EntityRef> blockEntityLookup = Maps.newHashMap();

    private final Map<Vector3ic, EntityRef> blockRegionLookup = Maps.newHashMap();
    private final Map<EntityRef, BlockRegion> blockRegions = Maps.newHashMap();

    private final Set<EntityRef> temporaryBlockEntities = Sets.newLinkedHashSet();

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
    public Block setBlock(Vector3ic pos, Block type) {
        if (GameThread.isCurrentThread()) {
            EntityRef blockEntity = getBlockEntityAt(pos);
            Block oldType = super.setBlock(pos, type);
            final Set<Class<? extends Component>> retainComponents =
                    Optional.ofNullable(blockEntity.getComponent(RetainComponentsComponent.class))
                            .map(retainComponentsComponent -> retainComponentsComponent.components)
                            .orElse(Collections.emptySet());
            if (oldType != null) {
                updateBlockEntity(blockEntity, pos, oldType, type, false, retainComponents);
            }
            return oldType;
        }
        return null;
    }


    //SetBlocks, not SetBlock, is currently triggered by the engine whenever a player places a block.
    //This allows for several useful features, such as quickly synchronizing placement across networks.
    //However, this means that even if only one block is placed, this is the method being called.
    //It must be overridden here to allow an OnChangedBlock event to be properly sent for placed blocks.
    @Override
    public Map<Vector3ic, Block> setBlocks(Map<? extends Vector3ic, Block> blocks) {
        if (GameThread.isCurrentThread()) {
            Map<Vector3ic, Block> oldBlocks = super.setBlocks(blocks);
            for (Vector3ic vec : oldBlocks.keySet()) {
                if (oldBlocks.get(vec) != null) {
                    EntityRef blockEntity = getBlockEntityAt(vec);

                    // check for components to be retained when updating the block entity
                    final Set<Class<? extends Component>> retainComponents =
                            Optional.ofNullable(blockEntity.getComponent(RetainComponentsComponent.class))
                                    .map(retainComponentsComponent -> retainComponentsComponent.components)
                                    .orElse(Collections.emptySet());
                    updateBlockEntity(blockEntity, vec, oldBlocks.get(vec), blocks.get(vec), false, retainComponents);
                }
            }
            return oldBlocks;
        }
        return null;
    }

    @Override
    @SafeVarargs
    public final Block setBlockRetainComponent(Vector3ic position, Block type, Class<? extends Component>... components) {
        if (GameThread.isCurrentThread()) {
            EntityRef blockEntity = getBlockEntityAt(position);
            Block oldType = super.setBlock(position, type);
            if (oldType != null) {
                updateBlockEntity(blockEntity, position, oldType, type, false, Sets.newHashSet(components));
            }
            return oldType;
        }
        return null;
    }

    private void updateBlockEntity(EntityRef blockEntity, Vector3ic pos, Block oldType, Block type,
                                   boolean forceEntityUpdate, Set<Class<? extends Component>> retainComponents) {
        if (type.isKeepActive()) {
            temporaryBlockEntities.remove(blockEntity);
        } else if (oldType.isKeepActive() && isTemporaryBlock(blockEntity, type)) {
            temporaryBlockEntities.add(blockEntity);
        }
        if (forceEntityUpdate
                || !(Objects.equal(oldType.getBlockFamily(), type.getBlockFamily())
                && Objects.equal(oldType.getPrefab(), type.getPrefab()))) {
            updateBlockEntityComponents(blockEntity, oldType, type, retainComponents);
        }

        OnChangedBlock changedEvent = new OnChangedBlock(pos, type, oldType);
        EntityRef regionEntity = blockRegionLookup.get(pos);
        if (regionEntity != null) {
            regionEntity.send(changedEvent);
        }
        blockEntity.send(changedEvent);
    }

    @Override
    public EntityRef setPermanentBlockEntity(Vector3ic blockPosition, EntityRef blockEntity) {
        if (GameThread.isCurrentThread()) {
            EntityRef oldEntity = getExistingBlockEntityAt(blockPosition);
            blockEntityLookup.put(new Vector3i(blockPosition), blockEntity);
            temporaryBlockEntities.remove(blockEntity);
            return oldEntity;
        }
        logger.error("Attempted to set block entity off-thread");
        return EntityRef.NULL;
    }

    @Override
    public EntityRef getExistingBlockEntityAt(Vector3ic blockPosition) {
        if (GameThread.isCurrentThread()) {
            EntityRef result = blockEntityLookup.get(blockPosition);
            return (result == null) ? EntityRef.NULL : result;
        }
        logger.error("Attempted to get block entity off-thread");
        return EntityRef.NULL;
    }

    @Override
    public Block setBlockForceUpdateEntity(Vector3ic position, Block type) {
        if (GameThread.isCurrentThread()) {
            EntityRef blockEntity = getBlockEntityAt(position);
            Block oldType = super.setBlock(position, type);
            if (oldType != null) {
                updateBlockEntity(blockEntity, position, oldType, type, true, Collections.<Class<? extends Component>>emptySet());
            }
            return oldType;
        }
        return null;
    }

    @Override
    public EntityRef getBlockEntityAt(Vector3fc position) {
        Vector3i pos = new Vector3i(position, RoundingMode.HALF_UP);
        return getBlockEntityAt(pos);
    }

    @Override
    public EntityRef getBlockEntityAt(Vector3ic blockPosition) {
        if (GameThread.isCurrentThread()) {
            EntityRef blockEntity = getExistingBlockEntityAt(blockPosition);
            if ((!blockEntity.exists()
                    || !blockEntity.hasComponent(NetworkComponent.class))
                    && isBlockRelevant(blockPosition.x(), blockPosition.y(), blockPosition.z())) {
                Block block = getBlock(blockPosition.x(), blockPosition.y(), blockPosition.z());
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
            if (metadata.isForceBlockActive() && ignoreComponent != metadata.getType() && entity.hasComponent(metadata.getType())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Transforms a block entity with the change of block type. This is driven from the delta between the old and new
     * block type prefabs, but takes into account changes made to the block entity.
     * Components contained in `blockEntity` that
     * <ul>
     *     <li>are not "common block components" (e.g. `NetworkComponent`)</li>
     *     <li>don't have `reatinUnalteredOnBlockChange` metadata</li>
     *     <li>are not listed in the block prefab</li>
     *     <li>are not listed in the set of components to be retained</li>
     * </ul>
     * will be removed.
     *
     * @param blockEntity      The entity to update
     * @param oldType          The previous type of the block
     * @param type             The new type of the block
     * @param retainComponents List of components to be retained
     */
    private void updateBlockEntityComponents(EntityRef blockEntity, Block oldType, Block type, Set<Class<? extends Component>> retainComponents) {
        BlockComponent blockComponent = blockEntity.getComponent(BlockComponent.class);

        Optional<Prefab> oldPrefab = oldType.getPrefab();
        EntityBuilder oldEntityBuilder = entityManager.newBuilder(oldPrefab.orElse(null));
        oldEntityBuilder.addComponent(new BlockComponent(oldType, blockComponent.getPosition()));
        BeforeEntityCreated oldEntityEvent = new BeforeEntityCreated(oldPrefab.orElse(null), oldEntityBuilder.iterateComponents());
        blockEntity.send(oldEntityEvent);
        for (Component comp : oldEntityEvent.getResultComponents()) {
            oldEntityBuilder.addComponent(comp);
        }

        Optional<Prefab> newPrefab = type.getPrefab();
        EntityBuilder newEntityBuilder = entityManager.newBuilder(newPrefab.orElse(null));
        newEntityBuilder.addComponent(new BlockComponent(type, blockComponent.getPosition()));
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

        BlockComponent newBlockComponent = new BlockComponent(type, blockComponent.getPosition());
        blockEntity.saveComponent(newBlockComponent);

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

    private EntityRef createBlockEntity(Vector3ic blockPosition, Block block) {
        EntityBuilder builder = entityManager.newBuilder(block.getPrefab().orElse(null));
        builder.addComponent(new LocationComponent(new Vector3f(blockPosition)));
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
    public EntityRef getExistingEntityAt(Vector3ic blockPosition) {
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
    public EntityRef getEntityAt(Vector3ic blockPosition) {
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
    public boolean hasPermanentBlockEntity(Vector3ic blockPos) {
        if (GameThread.isCurrentThread()) {
            EntityRef blockEntity = blockEntityLookup.get(blockPos);
            return blockEntity != null && !temporaryBlockEntities.contains(blockEntity);
        }
        logger.error("Attempted check whether a block entity is permanent, off thread");
        return false;
    }

    @ReceiveEvent(components = BlockComponent.class)
    public void onActivateBlock(OnActivatedComponent event, EntityRef entity) {
        BlockComponent block = entity.getComponent(BlockComponent.class);
        EntityRef oldEntity = blockEntityLookup.put(block.getPosition(new Vector3i()), entity);
        // If this is a client, then an existing block entity may exist. Destroy it.
        if (oldEntity != null && !Objects.equal(oldEntity, entity)) {
            oldEntity.destroy();
        }
    }

    @ReceiveEvent(components = BlockComponent.class)
    public void onDeactivateBlock(BeforeDeactivateComponent event, EntityRef entity) {
        BlockComponent block = entity.getComponent(BlockComponent.class);
        if (blockEntityLookup.get(block.getPosition()) == entity) {
            blockEntityLookup.remove(block.getPosition());
        }
    }

    @ReceiveEvent(components = BlockRegionComponent.class)
    public void onBlockRegionActivated(OnActivatedComponent event, EntityRef entity) {
        BlockRegionComponent regionComp = entity.getComponent(BlockRegionComponent.class);
        blockRegions.put(entity, regionComp.region);
        for (Vector3ic pos : regionComp.region) {
            blockRegionLookup.put(new Vector3i(pos), entity);
        }
    }

    @ReceiveEvent(components = BlockRegionComponent.class)
    public void onBlockRegionChanged(OnChangedComponent event, EntityRef entity) {
        BlockRegion oldRegion = blockRegions.get(entity);
        for (Vector3ic pos : oldRegion) {
            blockRegionLookup.remove(pos);
        }
        BlockRegionComponent regionComp = entity.getComponent(BlockRegionComponent.class);
        blockRegions.put(entity, regionComp.region);
        for (Vector3ic pos : regionComp.region) {
            blockRegionLookup.put(new Vector3i(pos), entity);
        }
    }

    @ReceiveEvent(components = BlockRegionComponent.class)
    public void onBlockRegionDeactivated(BeforeDeactivateComponent event, EntityRef entity) {
        BlockRegion oldRegion = blockRegions.get(entity);
        for (Vector3ic pos : oldRegion) {
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
            //TODO: should this also check for components listed in `RetainComponentsComponent`?
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
                Vector3ic blockPosition = blockComp.getPosition();
                Block block = getBlock(blockPosition.x(), blockPosition.y(), blockPosition.z());
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
