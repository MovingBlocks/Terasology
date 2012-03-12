package org.terasology.logic.systems;

import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import org.terasology.components.BlockComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.componentSystem.ComponentSystem;
import org.terasology.entitySystem.componentSystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.event.AddComponentEvent;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.math.Vector3i;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class BlockEntityLookup implements EventHandlerSystem {

    private EntityManager entityManager;

    // TODO: Perhaps a better datastructure for spatial lookups
    private TObjectLongMap<Vector3i> blockComponentLookup = new TObjectLongHashMap<Vector3i>();

    public void initialise() {
        this.entityManager = CoreRegistry.get(EntityManager.class);
        for (EntityRef blockComp : entityManager.iteratorEntities(BlockComponent.class)) {
            BlockComponent comp = blockComp.getComponent(BlockComponent.class);
            blockComponentLookup.put(new Vector3i(comp.getPosition()), blockComp.getId());
        }
    }
    
    public EntityRef getEntityAt(Vector3i blockPosition) {
        return entityManager.get(blockComponentLookup.get(blockPosition));
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
