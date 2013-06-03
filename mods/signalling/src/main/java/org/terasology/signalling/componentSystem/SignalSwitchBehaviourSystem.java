package org.terasology.signalling.componentSystem;

import org.terasology.entitySystem.*;
import org.terasology.entitySystem.event.ChangedComponentEvent;
import org.terasology.signalling.components.SignalConsumerComponent;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.Block;
import org.terasology.world.block.management.BlockManager;
import org.terasology.components.world.LocationComponent;
import org.terasology.math.Vector3i;

import javax.vecmath.Vector3f;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterComponentSystem
public class SignalSwitchBehaviourSystem implements EventHandlerSystem {
    @In
    private WorldProvider worldProvider;

    @Override
    public void initialise() {

    }

    @Override
    public void shutdown() {

    }

    @ReceiveEvent(components = {SignalConsumerComponent.class})
    public void signalSwitched(ChangedComponentEvent event, EntityRef entity) {
        if (entity.hasComponent(BlockComponent.class) && entity.hasComponent(LocationComponent.class)) {
            SignalConsumerComponent consumerComponent = entity.getComponent(SignalConsumerComponent.class);
            Vector3i blockLocation = new Vector3i(entity.getComponent(LocationComponent.class).getWorldPosition());
            Block blockAtLocation = worldProvider.getBlock(blockLocation);
            String entityPrefab = blockAtLocation.getEntityPrefab();
            if (entityPrefab.equals("signalling:SignalLamp") && consumerComponent.hasSignal) {
                worldProvider.setBlock(blockLocation, BlockManager.getInstance().getBlock("signalling:SignalLampLighted"), blockAtLocation);
            } else if (entityPrefab.equals("signalling:SignalLampLighted") && !consumerComponent.hasSignal) {
                worldProvider.setBlock(blockLocation, BlockManager.getInstance().getBlock("signalling:SignalLamp"), blockAtLocation);
            }
        }
    }
}
