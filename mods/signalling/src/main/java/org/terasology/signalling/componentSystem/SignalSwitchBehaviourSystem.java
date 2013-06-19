package org.terasology.signalling.componentSystem;

import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.*;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.signalling.components.SignalConsumerStatusComponent;
import org.terasology.signalling.components.SignalProducerComponent;
import org.terasology.signalling.components.SignalTransformerHasSignalComponent;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.management.BlockManager;
import org.terasology.math.Vector3i;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;

import javax.vecmath.Vector3f;
import java.util.Set;

import com.google.common.collect.Sets;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(value=RegisterMode.AUTHORITY)
public class SignalSwitchBehaviourSystem implements UpdateSubscriberSystem {
    @In
    private WorldProvider worldProvider;
    @In
    private EntityManager entityManager;
    @In
    private BlockEntityRegistry blockEntityRegistry;

    private Set<Vector3i> activatedPressurePlates = Sets.newHashSet();

    private Block lampTurnedOff;
    private Block lampTurnedOn;
    private Block signalTransformer;
    private Block signalPressurePlate;

    @Override
    public void update(float delta) {
        Set<Vector3i> toRemoveSignal = Sets.newHashSet(activatedPressurePlates);

        Iterable<EntityRef> players = entityManager.getEntitiesWith(CharacterComponent.class, LocationComponent.class);
        for (EntityRef player : players) {
            Vector3f playerLocation = player.getComponent(LocationComponent.class).getWorldPosition();
            Vector3i locationBeneathPlayer = new Vector3i(playerLocation.x + 0.5f, playerLocation.y - 0.5f, playerLocation.z + 0.5f);
            Block blockBeneathPlayer = worldProvider.getBlock(locationBeneathPlayer);
            if (blockBeneathPlayer == signalPressurePlate) {
                EntityRef entityBeneathPlayer = blockEntityRegistry.getBlockEntityAt(locationBeneathPlayer);
                SignalProducerComponent signalProducer = entityBeneathPlayer.getComponent(SignalProducerComponent.class);
                if (signalProducer != null) {
                    if (signalProducer.signalStrength == 0) {
                        signalProducer.signalStrength = -1;
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
                if (signalProducer.signalStrength == -1) {
                    signalProducer.signalStrength = 0;
                    entityBeneathPlayer.saveComponent(signalProducer);
                    activatedPressurePlates.remove(pressurePlateLocation);
                }
            }
        }
    }

    @Override
    public void initialise() {
        final BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        lampTurnedOff = blockManager.getBlock("signalling:SignalLampOff");
        lampTurnedOn = blockManager.getBlock("signalling:SignalLampOn");
        signalTransformer = blockManager.getBlock("signalling:SignalTransformer");
        signalPressurePlate = blockManager.getBlock("signalling:SignalPressurePlate");
    }

    @Override
    public void shutdown() {

    }

    @ReceiveEvent(components = {BlockComponent.class, SignalProducerComponent.class})
    public void producerActivated(ActivateEvent event, EntityRef entity) {
        SignalProducerComponent producerComponent = entity.getComponent(SignalProducerComponent.class);
        Vector3i blockLocation = new Vector3i(entity.getComponent(BlockComponent.class).getPosition());
        Block blockAtLocation = worldProvider.getBlock(blockLocation);
        if (blockAtLocation == signalTransformer) {
            signalTransformerActivated(entity, producerComponent);
        }
    }

    private void signalTransformerActivated(EntityRef entity, SignalProducerComponent producerComponent) {
        int result = producerComponent.signalStrength + 1;
        if (result == 11)
            result = 0;
        producerComponent.signalStrength = result;
        entity.saveComponent(producerComponent);
        if (producerComponent.signalStrength == 1) {
            entity.saveComponent(new SignalTransformerHasSignalComponent());
        } else if (producerComponent.signalStrength == 0) {
            entity.removeComponent(SignalTransformerHasSignalComponent.class);
        }
    }

    @ReceiveEvent(components = {SignalConsumerStatusComponent.class})
    public void consumerModified(OnChangedComponent event, EntityRef entity) {
        if (entity.hasComponent(BlockComponent.class)) {
            SignalConsumerStatusComponent consumerStatusComponent = entity.getComponent(SignalConsumerStatusComponent.class);
            Vector3i blockLocation = new Vector3i(entity.getComponent(BlockComponent.class).getPosition());
            Block blockAtLocation = worldProvider.getBlock(blockLocation);
            if (blockAtLocation == lampTurnedOff && consumerStatusComponent.hasSignal) {
                worldProvider.setBlock(blockLocation, lampTurnedOn, blockAtLocation);
            } else if (blockAtLocation == lampTurnedOn && !consumerStatusComponent.hasSignal) {
                worldProvider.setBlock(blockLocation, lampTurnedOff, blockAtLocation);
            }
        }
    }
}
