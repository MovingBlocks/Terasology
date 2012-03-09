package org.terasology.logic.systems;

import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import org.terasology.components.BlockComponent;
import org.terasology.components.CharacterSoundComponent;
import org.terasology.components.LocationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandler;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.event.AddComponentEvent;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.events.FootstepEvent;
import org.terasology.logic.audio.Sound;
import org.terasology.logic.manager.AudioManager;
import org.terasology.math.Vector3i;

import javax.vecmath.Vector3d;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class BlockEntityLookup implements EventHandler {

    private EntityManager entityManager;
    
    // TODO: Perhaps a better datastructure for spatial lookups
    private TObjectLongMap<Vector3i> blockComponentLookup = new TObjectLongHashMap<Vector3i>();

    public BlockEntityLookup(EntityManager entityManager) {
        this.entityManager = entityManager;
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
