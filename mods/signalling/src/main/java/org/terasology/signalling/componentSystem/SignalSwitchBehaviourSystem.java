package org.terasology.signalling.componentSystem;

import gnu.trove.iterator.TObjectLongIterator;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.blockNetwork.ImmutableBlockLocation;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.*;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.lifecycleEvents.*;
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
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Set;

import com.google.common.collect.Sets;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class SignalSwitchBehaviourSystem implements UpdateSubscriberSystem {
    private static final Logger logger = LoggerFactory.getLogger(SignalSystem.class);

    public static final int GATE_MINIMUM_SIGNAL_CHANGE_INTERVAL = 500;
    public static final int NOT_LOADED_BLOCK_RETRY_DELAY = 500;

    @In
    private WorldProvider worldProvider;
    @In
    private EntityManager entityManager;
    @In
    private BlockEntityRegistry blockEntityRegistry;

    private Set<Vector3i> activatedPressurePlates = Sets.newHashSet();
    private PriorityQueue<BlockAtLocationDelayedAction> delayedActions = new PriorityQueue<BlockAtLocationDelayedAction>(11, new ExecutionTimeOrdering());

    private TObjectLongMap<ImmutableBlockLocation> gateLastSignalChangeTime = new TObjectLongHashMap<>();

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
        deleteOldSignalChangesForGates();
    }

    private static final long SIGNAL_CLEANUP_INTERVAL = 10000;
    private long lastSignalCleanupExecuteTime;

    private void deleteOldSignalChangesForGates() {
        long worldTime = worldProvider.getTime();
        if (lastSignalCleanupExecuteTime + SIGNAL_CLEANUP_INTERVAL < worldTime) {
            final TObjectLongIterator<ImmutableBlockLocation> iterator = gateLastSignalChangeTime.iterator();
            while (iterator.hasNext()) {
                iterator.advance();
                if (iterator.value() + GATE_MINIMUM_SIGNAL_CHANGE_INTERVAL < worldTime) {
                    iterator.remove();
                }
            }
            lastSignalCleanupExecuteTime = worldTime;
        }
    }

    private void handleDelayedActionsEvents() {
        long worldTime = worldProvider.getTime();
        BlockAtLocationDelayedAction action;
        while ((action = delayedActions.peek()) != null
                && action.executeTime <= worldTime) {
            action = delayedActions.poll();

            final Vector3i actionLocation = action.blockLocation.toVector3i();
            if (worldProvider.isBlockRelevant(actionLocation)) {
                final Block block = worldProvider.getBlock(actionLocation);
                final BlockFamily blockFamily = block.getBlockFamily();

                final EntityRef blockEntity = blockEntityRegistry.getBlockEntityAt(actionLocation);

                if (blockFamily == signalOnDelayGate) {
                    startProducingSignal(blockEntity, -1);
                } else if (blockFamily == signalOffDelayGate) {
                    stopProducingSignal(blockEntity);
                } else if (blockFamily == signalOrGate || blockFamily == signalAndGate
                        || blockFamily == signalXorGate) {
                    if (processOutputForNormalGate(blockEntity)) {
                        gateLastSignalChangeTime.put(new ImmutableBlockLocation(actionLocation), worldTime);
                    }
                } else if (blockFamily == signalNandGate) {
                    if (processOutputForRevertedGate(blockEntity)) {
                        gateLastSignalChangeTime.put(new ImmutableBlockLocation(actionLocation), worldTime);
                    }
                }

                blockEntity.removeComponent(SignalDelayedActionComponent.class);
            } else {
                action.executeTime += NOT_LOADED_BLOCK_RETRY_DELAY;
                delayedActions.add(action);
            }
        }
    }

    private boolean processOutputForNormalGate(EntityRef blockEntity) {
        boolean hasSignal = blockEntity.getComponent(SignalConsumerStatusComponent.class).hasSignal;
        logger.debug("Processing gate, hasSignal="+hasSignal);
        if (hasSignal) {
            return startProducingSignal(blockEntity, -1);
        } else {
            return stopProducingSignal(blockEntity);
        }
    }

    private boolean processOutputForRevertedGate(EntityRef blockEntity) {
        boolean hasSignal = blockEntity.getComponent(SignalConsumerStatusComponent.class).hasSignal;
        if (!hasSignal) {
            return startProducingSignal(blockEntity, -1);
        } else {
            return stopProducingSignal(blockEntity);
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

    @ReceiveEvent(components = {BlockComponent.class, SignalDelayedActionComponent.class})
    public void addedDelayedAction(OnActivatedComponent event, EntityRef block) {
        final Vector3i location = block.getComponent(BlockComponent.class).getPosition();
        final long executeTime = block.getComponent(SignalDelayedActionComponent.class).executeTime;
        delayedActions.add(new BlockAtLocationDelayedAction(location, executeTime));
    }

//    @ReceiveEvent(components = {BlockComponent.class, SignalDelayedActionComponent.class})
//    public void removedDelayedAction(BeforeDeactivateComponent event, EntityRef block) {
//        final Vector3i location = block.getComponent(BlockComponent.class).getPosition();
//        final long executeTime = block.getComponent(SignalDelayedActionComponent.class).executeTime;
//        delayedActions.remove(new BlockAtLocationDelayedAction(location, executeTime));
//    }

    @ReceiveEvent(components = {SignalConsumerStatusComponent.class})
    public void consumerModified(OnChangedComponent event, EntityRef entity) {
        if (entity.hasComponent(BlockComponent.class)) {
            SignalConsumerStatusComponent consumerStatusComponent = entity.getComponent(SignalConsumerStatusComponent.class);
            Vector3i blockLocation = new Vector3i(entity.getComponent(BlockComponent.class).getPosition());
            Block block = worldProvider.getBlock(blockLocation);
            BlockFamily blockFamily = block.getBlockFamily();
            if (block == lampTurnedOff && consumerStatusComponent.hasSignal) {
                logger.debug("Lamp turning on");
                worldProvider.setBlock(blockLocation, lampTurnedOn, block);
            } else if (block == lampTurnedOn && !consumerStatusComponent.hasSignal) {
                logger.debug("Lamp turning off");
                worldProvider.setBlock(blockLocation, lampTurnedOff, block);
            } else if (blockFamily == signalOrGate || blockFamily == signalAndGate
                    || blockFamily == signalXorGate) {
                logger.debug("Signal changed for gate");
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

    private void signalChangedForNormalGate(EntityRef entity, SignalConsumerStatusComponent consumerStatusComponent) {
        logger.debug("Gate has signal: " + consumerStatusComponent.hasSignal);
        delayGateSignalChangeIfNeeded(entity);
    }

    private void signalChangedForNotGate(EntityRef entity, SignalConsumerStatusComponent consumerStatusComponent) {
        logger.debug("Gate has signal: " + consumerStatusComponent.hasSignal);
        delayGateSignalChangeIfNeeded(entity);
    }

    private void delayGateSignalChangeIfNeeded(EntityRef entity) {
        if (!entity.hasComponent(SignalDelayedActionComponent.class)) {
            // Schedule for the gate to be looked either immediately (during "update" method) or at least
            // GATE_MINIMUM_SIGNAL_CHANGE_INTERVAL from the time it has last changed, whichever is later
            SignalDelayedActionComponent delayedAction = new SignalDelayedActionComponent();
            long whenToLookAt;
            final ImmutableBlockLocation location = new ImmutableBlockLocation(entity.getComponent(BlockComponent.class).getPosition());
            if (gateLastSignalChangeTime.containsKey(location)) {
                whenToLookAt = gateLastSignalChangeTime.get(location) + GATE_MINIMUM_SIGNAL_CHANGE_INTERVAL;
            } else {
                whenToLookAt = worldProvider.getTime();
            }
            delayedAction.executeTime = whenToLookAt;
            entity.saveComponent(delayedAction);
        }
    }

    private boolean startProducingSignal(EntityRef entity, int signalStrength) {
        final SignalProducerComponent producer = entity.getComponent(SignalProducerComponent.class);
        if (producer.signalStrength != signalStrength) {
            producer.signalStrength = signalStrength;
            entity.saveComponent(producer);
            entity.saveComponent(new SignalProducerModifiedComponent());
            return true;
        }
        return false;
    }

    private boolean stopProducingSignal(EntityRef entity) {
        SignalProducerComponent producer = entity.getComponent(SignalProducerComponent.class);
        if (producer.signalStrength != 0) {
            producer.signalStrength = 0;
            entity.saveComponent(producer);
            entity.removeComponent(SignalProducerModifiedComponent.class);
            return true;
        }
        return false;
    }

    private class BlockAtLocationDelayedAction {
        private long executeTime;
        private ImmutableBlockLocation blockLocation;

        private BlockAtLocationDelayedAction(Vector3i location, long executeTime) {
            this.blockLocation = new ImmutableBlockLocation(location);
            this.executeTime = executeTime;
        }

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
    }

    private class ExecutionTimeOrdering implements Comparator<BlockAtLocationDelayedAction> {
        @Override
        public int compare(BlockAtLocationDelayedAction o1, BlockAtLocationDelayedAction o2) {
            return Long.valueOf(o1.executeTime).compareTo(o2.executeTime);
        }
    }
}
