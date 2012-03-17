package org.terasology.logic.systems.block;

import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import org.terasology.components.BlockComponent;
import org.terasology.components.HealthComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.componentSystem.EventHandlerSystem;
import org.terasology.entitySystem.event.AddComponentEvent;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.world.IWorldProvider;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class BlockEntityLookup implements EventHandlerSystem {

    private EntityManager entityManager;
    private IWorldProvider worldProvider;

    // TODO: Perhaps a better datastructure for spatial lookups
    private TObjectLongMap<Vector3i> blockComponentLookup = new TObjectLongHashMap<Vector3i>();

    public void initialise() {
        this.entityManager = CoreRegistry.get(EntityManager.class);
        this.worldProvider = CoreRegistry.get(IWorldProvider.class);
        for (EntityRef blockComp : entityManager.iteratorEntities(BlockComponent.class)) {
            BlockComponent comp = blockComp.getComponent(BlockComponent.class);
            blockComponentLookup.put(new Vector3i(comp.getPosition()), blockComp.getId());
        }
    }
    
    public EntityRef getEntityAt(Vector3i blockPosition) {
        return entityManager.get(blockComponentLookup.get(blockPosition));
    }
    
    public EntityRef getOrCreateEntityAt(Vector3i blockPosition) {
        long id = blockComponentLookup.get(blockPosition);
        EntityRef blockEntity;
        if (id == 0) {
            Block block = BlockManager.getInstance().getBlock(worldProvider.getBlock(blockPosition));
            blockEntity = entityManager.create();
            blockEntity.addComponent(new BlockComponent(blockPosition, true));
            blockEntity.addComponent(new HealthComponent(block.getHardness(), 2.0f, 1.0f));
        } else {
            blockEntity = entityManager.get(id);
        }
        return blockEntity;
    }

    @ReceiveEvent(components = {BlockComponent.class})
    public void onCreate(AddComponentEvent event, EntityRef entity) {
        BlockComponent block = entity.getComponent(BlockComponent.class);
        blockComponentLookup.put(new Vector3i(block.getPosition()), entity.getId());
    }

    @ReceiveEvent(components = {BlockComponent.class})
    public void onDestroy(RemovedComponentEvent event, EntityRef entity) {
        BlockComponent block = entity.getComponent(BlockComponent.class);
        blockComponentLookup.remove(new Vector3i(block.getPosition()));
    }
    
}
