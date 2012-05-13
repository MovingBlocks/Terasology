package org.terasology.componentSystem.block;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.BlockComponent;
import org.terasology.components.HealthComponent;
import org.terasology.entitySystem.*;
import org.terasology.entitySystem.event.AddComponentEvent;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.world.IWorldProvider;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;

import java.util.List;
import java.util.Map;

/**
 * Manages creation and lookup of entities linked to blocks
 * @author Immortius <immortius@gmail.com>
 */
public class BlockEntityRegistry implements EventHandlerSystem, UpdateSubscriberSystem {

    private PrefabManager prefabManager;
    private EntityManager entityManager;
    private IWorldProvider worldProvider;

    // TODO: Perhaps a better datastructure for spatial lookups
    // TODO: Or perhaps a build in indexing system for entities
    private Map<Vector3i, EntityRef> blockComponentLookup = Maps.newHashMap();

    private List<EntityRef> tempBlocks = Lists.newArrayList();

    public void initialise() {
        this.entityManager = CoreRegistry.get(EntityManager.class);
        this.prefabManager = CoreRegistry.get(PrefabManager.class);
        this.worldProvider = CoreRegistry.get(IWorldProvider.class);
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
    
    public EntityRef getEntityAt(Vector3i blockPosition) {
        EntityRef result = blockComponentLookup.get(blockPosition);
        return (result == null) ? EntityRef.NULL : result;
    }
    
    public EntityRef getOrCreateEntityAt(Vector3i blockPosition) {
        EntityRef blockEntity = blockComponentLookup.get(blockPosition);
        if (blockEntity == null || !blockEntity.exists()) {
            byte id = worldProvider.getBlock(blockPosition);
            if (id == 0)
                return EntityRef.NULL;

            Block block = BlockManager.getInstance().getBlock(id);

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
