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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.ComponentSystemManager;
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
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.regions.BlockRegionComponent;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

/**
 * @author Immortius
 */
public class EntityAwareWorldProvider extends AbstractWorldProviderDecorator implements BlockEntityRegistry, UpdateSubscriberSystem, EntityChangeSubscriber {

    private static final Logger logger = LoggerFactory.getLogger(EntityAwareWorldProvider.class);
    private static final Set<Class<? extends Component>> COMMON_BLOCK_COMPONENTS =
            ImmutableSet.of(NetworkComponent.class, BlockComponent.class, LocationComponent.class, HealthComponent.class, EntityInfoComponent.class);

    private EngineEntityManager entityManager;

    // TODO: Perhaps a better datastructure for spatial lookups
    // TODO: Or perhaps a build in indexing system for entities
    private Map<Vector3i, EntityRef> blockEntityLookup = Maps.newHashMap();

    private Map<Vector3i, EntityRef> blockRegionLookup = Maps.newHashMap();
    private Map<EntityRef, Region3i> blockRegions = Maps.newHashMap();

    private Set<EntityRef> temporaryBlockEntities = Sets.newLinkedHashSet();

    private Thread mainThread;

    private BlockingQueue<BlockChange> pendingChanges = Queues.newLinkedBlockingQueue();

    private static class BlockChange {
        private Vector3i position = new Vector3i();
        private Block oldType;
        private Block newType;
        private boolean forceEntityUpdate;
        private Set<Class<? extends Component>> retainComponentTypes;

        public BlockChange(Vector3i pos, Block oldType, Block newType, boolean forceEntityUpdate, Class<? extends Component>... retainComponentTypes) {
            this.position.set(pos);
            this.oldType = oldType;
            this.newType = newType;
            this.forceEntityUpdate = forceEntityUpdate;
            this.retainComponentTypes = Sets.newHashSet(retainComponentTypes);
        }
    }


    public EntityAwareWorldProvider(WorldProviderCore base) {
        super(base);
        mainThread = Thread.currentThread();
        entityManager = (EngineEntityManager) CoreRegistry.get(EntityManager.class);
        CoreRegistry.get(ComponentSystemManager.class).register(getTime());
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
        if (Thread.currentThread().equals(mainThread)) {
            Vector3i pos = new Vector3i(x, y, z);
            EntityRef blockEntity = getBlockEntityAt(pos);
            if (super.setBlock(x, y, z, type, oldType)) {
                updateBlockEntity(blockEntity, pos, oldType, type, false, Collections.<Class<? extends Component>>emptySet());
                return true;
            } else {
                processOffThreadChanges();
            }
        } else {
            if (super.setBlock(x, y, z, type, oldType)) {
                pendingChanges.add(new BlockChange(new Vector3i(x, y, z), oldType, type, false));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean setBlockForceUpdateEntity(int x, int y, int z, Block type, Block oldType) {
        if (Thread.currentThread().equals(mainThread)) {
            Vector3i pos = new Vector3i(x, y, z);
            EntityRef blockEntity = getBlockEntityAt(pos);
            if (super.setBlock(x, y, z, type, oldType)) {
                updateBlockEntity(blockEntity, pos, oldType, type, true, Collections.<Class<? extends Component>>emptySet());
                return true;
            } else {
                processOffThreadChanges();
            }
        } else {
            if (super.setBlock(x, y, z, type, oldType)) {
                pendingChanges.add(new BlockChange(new Vector3i(x, y, z), oldType, type, true));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean setBlockForceUpdateEntity(Vector3i position, Block type, Block oldType) {
        return setBlockForceUpdateEntity(position.x, position.y, position.z, type, oldType);
    }

    @Override
    public boolean setBlockRetainComponent(Vector3i position, Block type, Block oldType, Class<? extends Component>... components) {
        return setBlockRetainComponent(position.x, position.y, position.z, type, oldType, components);
    }

    @Override
    public boolean setBlockRetainComponent(int x, int y, int z, Block type, Block oldType, Class<? extends Component>... components) {
        if (Thread.currentThread().equals(mainThread)) {
            Vector3i pos = new Vector3i(x, y, z);
            EntityRef blockEntity = getBlockEntityAt(pos);
            if (super.setBlock(x, y, z, type, oldType)) {
                updateBlockEntity(blockEntity, pos, oldType, type, false, Sets.newHashSet(components));
                return true;
            } else {
                processOffThreadChanges();
            }
        } else {
            if (super.setBlock(x, y, z, type, oldType)) {
                pendingChanges.add(new BlockChange(new Vector3i(x, y, z), oldType, type, false, components));
                return true;
            }
        }
        return false;
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
        if (Thread.currentThread() == mainThread) {
            EntityRef result = blockEntityLookup.get(blockPosition);
            return (result == null) ? EntityRef.NULL : result;
        }
        logger.error("Attempted to get block entity off-thread");
        return EntityRef.NULL;
    }

    @Override
    public EntityRef getBlockEntityAt(Vector3i blockPosition) {
        if (Thread.currentThread() == mainThread) {
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
    private void updateBlockEntityComponents(EntityRef blockEntity, Block oldType, Block type, Set<Class<? extends Component>> retainComponents) {
        Prefab oldPrefab = entityManager.getPrefabManager().getPrefab(oldType.getPrefab());
        Prefab newPrefab = entityManager.getPrefabManager().getPrefab(type.getPrefab());

        for (ComponentMetadata<?> metadata : entityManager.getComponentLibrary()) {
            if (!COMMON_BLOCK_COMPONENTS.contains(metadata.getType()) && !metadata.isRetainUnalteredOnBlockChange()
                    && (newPrefab == null || !newPrefab.hasComponent(metadata.getType())) && !retainComponents.contains(metadata.getType())) {
                blockEntity.removeComponent(metadata.getType());
            }
        }

        HealthComponent health = blockEntity.getComponent(HealthComponent.class);
        if (health == null && type.isDestructible()) {
            blockEntity.addComponent(new HealthComponent(type.getHardness(), 2.0f, 1.0f));
        } else if (health != null && !type.isDestructible()) {
            blockEntity.removeComponent(HealthComponent.class);
        } else if (health != null && type.isDestructible()) {
            health.maxHealth = type.getHardness();
            health.currentHealth = Math.min(health.currentHealth, health.maxHealth);
            blockEntity.saveComponent(health);
        }

        if (Objects.equal(newPrefab, oldPrefab)) {
            return;
        }

        if (newPrefab != null) {
            for (Component comp : newPrefab.iterateComponents()) {
                ComponentMetadata<?> metadata = entityManager.getComponentLibrary().getMetadata(comp.getClass());
                if (!blockEntity.hasComponent(comp.getClass())) {
                    blockEntity.addComponent(metadata.clone(comp));
                } else if (!metadata.isRetainUnalteredOnBlockChange() && !retainComponents.contains(metadata.getType())) {
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
        if (Thread.currentThread() == mainThread) {
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
        if (Thread.currentThread() == mainThread) {
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
        if (Thread.currentThread() == mainThread) {
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
        // TODO: This should be handled by the event system?
        PerformanceMonitor.startActivity("BlockChangedEventQueue");
        processOffThreadChanges();
        PerformanceMonitor.endActivity();

        PerformanceMonitor.startActivity("Temp Blocks Cleanup");
        List<EntityRef> toRemove = Lists.newArrayList(temporaryBlockEntities);
        temporaryBlockEntities.clear();
        for (EntityRef entity : toRemove) {
            cleanUpTemporaryEntity(entity);
        }
        PerformanceMonitor.endActivity();
    }

    private void processOffThreadChanges() {
        List<BlockChange> changes = Lists.newArrayListWithExpectedSize(pendingChanges.size());
        pendingChanges.drainTo(changes);
        for (BlockChange change : changes) {
            if (isBlockRelevant(change.position.x, change.position.y, change.position.z)) {
                Block currentType = getBlock(change.position.x, change.position.y, change.position.z);
                if (currentType == change.oldType) {
                    EntityRef blockEntity = getExistingBlockEntityAt(change.position);
                    if (!blockEntity.exists()) {
                        blockEntity = createBlockEntity(change.position, change.oldType);
                        updateBlockEntity(blockEntity, change.position, change.oldType, change.newType, change.forceEntityUpdate, change.retainComponentTypes);
                    }
                }
            }
        }
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
