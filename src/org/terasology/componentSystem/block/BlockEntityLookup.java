package org.terasology.componentSystem.block;

import com.google.common.collect.Maps;
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

import java.util.Map;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class BlockEntityLookup implements EventHandlerSystem {

    private PrefabManager prefabManager;
    private EntityManager entityManager;
    private IWorldProvider worldProvider;

    // TODO: Perhaps a better datastructure for spatial lookups
    // TODO: Or perhaps a build in indexing system for entities
    private Map<Vector3i, EntityRef> blockComponentLookup = Maps.newHashMap();

    public void initialise() {
        this.entityManager = CoreRegistry.get(EntityManager.class);
        this.prefabManager = CoreRegistry.get(PrefabManager.class);
        this.worldProvider = CoreRegistry.get(IWorldProvider.class);
        for (EntityRef blockComp : entityManager.iteratorEntities(BlockComponent.class)) {
            BlockComponent comp = blockComp.getComponent(BlockComponent.class);
            blockComponentLookup.put(new Vector3i(comp.getPosition()), blockComp);
        }
    }
    
    public EntityRef getEntityAt(Vector3i blockPosition) {
        EntityRef result = blockComponentLookup.get(blockPosition);
        return (result == null) ? EntityRef.NULL : result;
    }
    
    public EntityRef getOrCreateEntityAt(Vector3i blockPosition) {
        EntityRef blockEntity = blockComponentLookup.get(blockPosition);
        if (blockEntity == null || !blockEntity.exists()) {
            Block block = BlockManager.getInstance().getBlock(worldProvider.getBlock(blockPosition));
            blockEntity = entityManager.create();
            blockEntity.addComponent(new BlockComponent(blockPosition, block.isEntityRetainedWhenItem() || !block.getEntityPrefab().isEmpty()));
            // TODO: Get regen and wait from block config?
            blockEntity.addComponent(new HealthComponent(block.getHardness(), 2.0f, 1.0f));
            if (!block.getEntityPrefab().isEmpty()) {
                Prefab prefab = prefabManager.getPrefab(block.getEntityPrefab());
                if (prefab != null) {
                    for (Component component : prefab.listComponents()) {
                        blockEntity.addComponent(entityManager.copyComponent(component));
                    }
                }
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
    
}
