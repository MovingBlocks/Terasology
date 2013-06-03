package org.terasology.signalling.componentSystem;

import org.terasology.entitySystem.*;
import org.terasology.entitySystem.event.ChangedComponentEvent;
import org.terasology.signalling.components.SignalConsumerComponent;
import org.terasology.signalling.components.SignalProducerComponent;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.Block;
import org.terasology.world.block.management.BlockManager;
import org.terasology.components.world.LocationComponent;
import org.terasology.components.PlayerComponent;
import org.terasology.math.Vector3i;
import org.terasology.events.ActivateEvent;
import org.terasology.componentSystem.UpdateSubscriberSystem;

import javax.vecmath.Vector3f;
import java.util.Set;

import com.google.common.collect.Sets;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterComponentSystem
public class SignalSwitchBehaviourSystem implements EventHandlerSystem, UpdateSubscriberSystem {
    @In
    private WorldProvider worldProvider;
    @In
    private EntityManager entityManager;
    @In
    private BlockEntityRegistry blockEntityRegistry;

    private Set<Vector3i> activatedPressurePlates = Sets.newHashSet();

    @Override
    public void update(float delta) {
        Set<Vector3i> toRemoveSignal = Sets.newHashSet(activatedPressurePlates);

        Iterable<EntityRef> players = entityManager.iteratorEntities(PlayerComponent.class, LocationComponent.class);
        for (EntityRef player : players) {
            Vector3f playerLocation = player.getComponent(LocationComponent.class).getWorldPosition();
            Vector3i locationBeneathPlayer = new Vector3i(playerLocation.x+0.5f, playerLocation.y - 0.5f, playerLocation.z+0.5f);
            Block blockBeneathPlayer = worldProvider.getBlock(locationBeneathPlayer);
            if (blockBeneathPlayer.getEntityPrefab().equals("signalling:PressurePlate")) {
                EntityRef entityBeneathPlayer = blockEntityRegistry.getBlockEntityAt(locationBeneathPlayer);
                SignalProducerComponent signalProducer = entityBeneathPlayer.getComponent(SignalProducerComponent.class);
                if (signalProducer != null) {
                    if (signalProducer.signalStrength==0) {
                        signalProducer.signalStrength=-1;
                        entityBeneathPlayer.saveComponent(signalProducer);
                        activatedPressurePlates.add(locationBeneathPlayer);
                    } else {
                        toRemoveSignal.remove(locationBeneathPlayer);
                    }
                }
            }
        }

        for (Vector3i pressurePlateLocation : toRemoveSignal) {
            EntityRef entityBeneathPlayer = blockEntityRegistry.getBlockEntityAt(pressurePlateLocation);
            SignalProducerComponent signalProducer = entityBeneathPlayer.getComponent(SignalProducerComponent.class);
            if (signalProducer != null) {
                if (signalProducer.signalStrength==-1) {
                    signalProducer.signalStrength=0;
                    entityBeneathPlayer.saveComponent(signalProducer);
                    activatedPressurePlates.remove(pressurePlateLocation);
                }
            }
        }
    }

    @Override
    public void initialise() {

    }

    @Override
    public void shutdown() {

    }

    @ReceiveEvent(components = {BlockComponent.class, LocationComponent.class, SignalProducerComponent.class}) 
    public void producerActivated(ActivateEvent event, EntityRef entity){
        SignalProducerComponent producerComponent = entity.getComponent(SignalProducerComponent.class);
        Vector3i blockLocation = new Vector3i(entity.getComponent(LocationComponent.class).getWorldPosition());
        Block blockAtLocation = worldProvider.getBlock(blockLocation);
        String entityPrefab = blockAtLocation.getEntityPrefab();
        if (entityPrefab.equals("signalling:SignalTransformer")) {
            int result = producerComponent.signalStrength+1;
            if (result == 11)
                result = 0;
            producerComponent.signalStrength = result;
            entity.saveComponent(producerComponent);
        }
    }

    @ReceiveEvent(components = {SignalConsumerComponent.class})
    public void consumerModified(ChangedComponentEvent event, EntityRef entity) {
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
