package org.terasology.signalling.componentSystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.blockNetwork.ImmutableBlockLocation;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.*;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.lifecycleEvents.BeforeRemoveComponent;
import org.terasology.entitySystem.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.signalling.components.*;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.management.BlockManager;
import org.terasology.math.Vector3i;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;

import javax.vecmath.Vector3f;
import java.util.PriorityQueue;
import java.util.Set;

import com.google.common.collect.Sets;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class SignalSwitchBehaviourSystem implements UpdateSubscriberSystem {
    private static final Logger logger = LoggerFactory.getLogger(SignalSystem.class);

    @In
    private WorldProvider worldProvider;
    @In
    private EntityManager entityManager;
    @In
    private BlockEntityRegistry blockEntityRegistry;

    private Set<Vector3i> activatedPressurePlates = Sets.newHashSet();
    private PriorityQueue<BlockAtLocationDelayedAction> delayedActions = new PriorityQueue<BlockAtLocationDelayedAction>();

    private Block lampTurnedOff;
    private Block lampTurnedOn;
    private Block signalTransformer;
    private Block signalPressurePlate;
    private Block signalSwitch;
    private Block signalLimitedSwitch;

    private BlockFamily signalOrGate;
    private BlockFamily signalAndGate;
    private BlockFamily signalXorGate;
    private BlockFamily signalNandGate;

    private BlockFamily signalOnDelayGate;
    private BlockFamily signalOffDelayGate;

    @Override
    public void update(float delta) {
        handlePressurePlateEvents();
        handleDelayedActionsEvents();
    }

    private void handleDelayedActionsEvents() {
        long worldTime = worldProvider.getTime();
        BlockAtLocationDelayedAction action;
        while ((action = delayedActions.peek()) != null
                && action.executeTime >= worldTime) {
            action = delayedActions.poll();

            final Vector3i actionLocation = action.blockLocation.toVector3i();
            final Block block = worldProvider.getBlock(actionLocation);
            final BlockFamily blockFamily = block.getBlockFamily();

            final EntityRef blockEntity = blockEntityRegistry.getBlockEntityAt(actionLocation);

            if (blockFamily == signalOnDelayGate) {
                startProducingSignal(blockEntity, -1);
            } else if (blockFamily == signalOffDelayGate) {
                stopProducingSignal(blockEntity);
            }
            blockEntity.removeComponent(SignalDelayedActionComponent.class);
        }
    }

    private void handlePressurePlateEvents() {
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
                        startProducingSignal(entityBeneathPlayer, -1);
                        activatedPressurePlates.add(locationBeneathPlayer);
                    } else {
                        toRemoveSignal.remove(locationBeneathPlayer);
                    }
                }
            }
        }

        for (Vector3i pressurePlateLocation : toRemoveSignal) {
            EntityRef pressurePlate = blockEntityRegistry.getBlockEntityAt(pressurePlateLocation);
            SignalProducerComponent signalProducer = pressurePlate.getComponent(SignalProducerComponent.class);
            if (signalProducer != null) {
                stopProducingSignal(pressurePlate);
                activatedPressurePlates.remove(pressurePlateLocation);
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
        signalSwitch = blockManager.getBlock("signalling:SignalSwitch");
        signalLimitedSwitch = blockManager.getBlock("signalling:SignalLimitedSwitch");

        signalOrGate = blockManager.getBlockFamily("signalling:SignalOrGate");
        signalAndGate = blockManager.getBlockFamily("signalling:SignalAndGate");
        signalXorGate = blockManager.getBlockFamily("signalling:SignalXorGate");
        signalNandGate = blockManager.getBlockFamily("signalling:SignalNandGate");

        signalOnDelayGate = blockManager.getBlockFamily("signalling:SignalOnDelayGate");
        signalOffDelayGate = blockManager.getBlockFamily("signalling:SignalOffDelayGate");
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
        } else if (blockAtLocation == signalSwitch) {
            signalSwitchActivated(entity, producerComponent);
        } else if (blockAtLocation == signalLimitedSwitch) {
            signalLimitedSwitchActivated(entity, producerComponent);
        }
    }

    private void signalLimitedSwitchActivated(EntityRef entity, SignalProducerComponent producerComponent) {
        switchFlipped(5, entity, producerComponent);
    }

    private void signalSwitchActivated(EntityRef entity, SignalProducerComponent producerComponent) {
        switchFlipped(-1, entity, producerComponent);
    }

    private void switchFlipped(int onSignalStrength, EntityRef entity, SignalProducerComponent producerComponent) {
        int currentSignalStrength = producerComponent.signalStrength;
        if (currentSignalStrength == 0) {
            startProducingSignal(entity, onSignalStrength);
        } else {
            stopProducingSignal(entity);
        }
    }

    private void signalTransformerActivated(EntityRef entity, SignalProducerComponent producerComponent) {
        int result = producerComponent.signalStrength + 1;
        if (result == 11)
            result = 0;
        if (result > 0) {
            startProducingSignal(entity, result);
        } else {
            stopProducingSignal(entity);
        }
    }

    @ReceiveEvent(components = {SignalConsumerStatusComponent.class})
    public void consumerModified(OnChangedComponent event, EntityRef entity) {
        if (entity.hasComponent(BlockComponent.class)) {
            SignalConsumerStatusComponent consumerStatusComponent = entity.getComponent(SignalConsumerStatusComponent.class);
            Vector3i blockLocation = new Vector3i(entity.getComponent(BlockComponent.class).getPosition());
            Block block = worldProvider.getBlock(blockLocation);
            BlockFamily blockFamily = block.getBlockFamily();
            if (block == lampTurnedOff && consumerStatusComponent.hasSignal) {
                logger.info("Lamp turning on");
                worldProvider.setBlock(blockLocation, lampTurnedOn, block);
            } else if (block == lampTurnedOn && !consumerStatusComponent.hasSignal) {
                logger.info("Lamp turning off");
                worldProvider.setBlock(blockLocation, lampTurnedOff, block);
            } else if (blockFamily == signalOrGate || blockFamily == signalAndGate
                    || blockFamily == signalXorGate) {
                signalChangedForNormalGate(entity, consumerStatusComponent);
            } else if (blockFamily == signalNandGate) {
                signalChangedForNotGate(entity, consumerStatusComponent);
            } else if (blockFamily == signalOnDelayGate) {
                signalChangedForDelayOnGate(entity, consumerStatusComponent);
            } else if (blockFamily == signalOffDelayGate) {
                signalChangedForDelayOffGate(entity, consumerStatusComponent);
            }
        }
    }

    private void signalChangedForDelayOffGate(EntityRef entity, SignalConsumerStatusComponent consumerStatusComponent) {
        SignalTimeDelayComponent delay = entity.getComponent(SignalTimeDelayComponent.class);
        if (consumerStatusComponent.hasSignal) {
            // Remove any signal-delayed actions on the entity and turn on signal from it, if it doesn't have any
            entity.removeComponent(SignalDelayedActionComponent.class);
            startProducingSignal(entity, -1);
        } else {
            // Schedule for the gate to be looked at when the time passes
            SignalDelayedActionComponent delayedAction = new SignalDelayedActionComponent();
            delayedAction.executeTime = worldProvider.getTime() + delay.delaySetting;
            entity.saveComponent(delayedAction);
        }
    }

    private void signalChangedForDelayOnGate(EntityRef entity, SignalConsumerStatusComponent consumerStatusComponent) {
        SignalTimeDelayComponent delay = entity.getComponent(SignalTimeDelayComponent.class);
        if (consumerStatusComponent.hasSignal) {
            // Schedule for the gate to be looked at when the time passes
            SignalDelayedActionComponent delayedAction = new SignalDelayedActionComponent();
            delayedAction.executeTime = worldProvider.getTime() + delay.delaySetting;
            entity.saveComponent(delayedAction);
        } else {
            // Remove any signal-delayed actions on the entity and turn off signal from it, if it has any
            entity.removeComponent(SignalDelayedActionComponent.class);
            stopProducingSignal(entity);
        }
    }

    @ReceiveEvent(components = {BlockComponent.class, SignalDelayedActionComponent.class})
    public void addedDelayedAction(OnAddedComponent event, EntityRef block) {

    }

    @ReceiveEvent(components = {BlockComponent.class, SignalDelayedActionComponent.class})
    public void removedDelayedAction(BeforeRemoveComponent event, EntityRef block) {

    }

    private void signalChangedForNormalGate(EntityRef entity, SignalConsumerStatusComponent consumerStatusComponent) {
        logger.info("Gate has signal: " + consumerStatusComponent.hasSignal);
        if (consumerStatusComponent.hasSignal) {
            startProducingSignal(entity, -1);
        } else {
            stopProducingSignal(entity);
        }
    }

    private void startProducingSignal(EntityRef entity, int signalStrength) {
        final SignalProducerComponent producer = entity.getComponent(SignalProducerComponent.class);
        if (producer.signalStrength == 0) {
            producer.signalStrength = signalStrength;
            entity.saveComponent(producer);
            entity.addComponent(new SignalProducerModifiedComponent());
        }
    }

    private void stopProducingSignal(EntityRef entity) {
        SignalProducerComponent producer = entity.getComponent(SignalProducerComponent.class);
        if (producer.signalStrength != 0) {
            producer.signalStrength = 0;
            entity.saveComponent(producer);
            entity.removeComponent(SignalProducerModifiedComponent.class);
        }
    }

    private void signalChangedForNotGate(EntityRef entity, SignalConsumerStatusComponent consumerStatusComponent) {
        SignalProducerComponent producerComponent = entity.getComponent(SignalProducerComponent.class);
        logger.info("Gate has signal: " + consumerStatusComponent.hasSignal);
        if (consumerStatusComponent.hasSignal) {
            producerComponent.signalStrength = 0;
            entity.saveComponent(producerComponent);
            entity.saveComponent(new SignalProducerModifiedComponent());
        } else {
            producerComponent.signalStrength = -1;
            entity.saveComponent(producerComponent);
            entity.removeComponent(SignalProducerModifiedComponent.class);
        }
    }

    private class BlockAtLocationDelayedAction implements Comparable<BlockAtLocationDelayedAction> {
        private long executeTime;
        private ImmutableBlockLocation blockLocation;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BlockAtLocationDelayedAction that = (BlockAtLocationDelayedAction) o;

            if (executeTime != that.executeTime) return false;
            if (blockLocation != null ? !blockLocation.equals(that.blockLocation) : that.blockLocation != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (int) (executeTime ^ (executeTime >>> 32));
            result = 31 * result + (blockLocation != null ? blockLocation.hashCode() : 0);
            return result;
        }

        @Override
        public int compareTo(BlockAtLocationDelayedAction o) {
            if (executeTime < o.executeTime)
                return -1;
            else if (executeTime > o.executeTime)
                return 1;
            else
                return 0;
        }
    }
}
