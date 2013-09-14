package org.terasology.signalling.componentSystem;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.terasology.blockNetwork.*;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.entity.lifecycleEvents.*;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.math.Side;
import org.terasology.math.SideBitFlag;
import org.terasology.math.Vector3i;
import org.terasology.signalling.components.*;
import org.terasology.world.BlockEntityRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BeforeDeactivateBlocks;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.OnActivatedBlocks;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@RegisterSystem(value = RegisterMode.AUTHORITY)
public class SignalSystem implements UpdateSubscriberSystem, NetworkTopologyListener {
    private static final Logger logger = LoggerFactory.getLogger(SignalSystem.class);

    @In
    private Time time;
    @In
    private WorldProvider worldProvider;

    @In
    private BlockEntityRegistry blockEntityRegistry;

    private BlockNetwork signalNetwork;

    // we assume there can be only one consumer, one producer, and/or one conductor per block
    private Map<ImmutableBlockLocation, NetworkNode> signalProducers;
    private Map<ImmutableBlockLocation, NetworkNode> signalConsumers;
    private Map<ImmutableBlockLocation, NetworkNode> signalConductors;

    private Multimap<NetworkNode, Network> producerNetworks = HashMultimap.create();
    private Multimap<Network, NetworkNode> producersInNetwork = HashMultimap.create();

    private Multimap<NetworkNode, Network> consumerNetworks = HashMultimap.create();
    private Multimap<Network, NetworkNode> consumersInNetwork = HashMultimap.create();

    // Used to detect producer changes
    private Map<NetworkNode, Integer> producerSignalStrengths = Maps.newHashMap();
    // Used to store signal for consumer from non-modified networks
    private Map<NetworkNode, Map<Network, NetworkSignals>> consumerSignalInNetworks = Maps.newHashMap();

    private Set<Network> networksToRecalculate = Sets.newHashSet();
    private Set<NetworkNode> consumersToRecalculate = Sets.newHashSet();
    private Set<NetworkNode> producersSignalsChanged = Sets.newHashSet();

    private class NetworkSignals {
        private byte sidesWithSignal;
        private byte sidesWithoutSignal;

        private NetworkSignals(byte sidesWithSignal, byte sidesWithoutSignal) {
            this.sidesWithSignal = sidesWithSignal;
            this.sidesWithoutSignal = sidesWithoutSignal;
        }
    }

    @Override
    public void initialise() {
        signalNetwork = new BlockNetwork();
        signalNetwork.addTopologyListener(this);
        signalProducers = Maps.newHashMap();
        signalConsumers = Maps.newHashMap();
        signalConductors = Maps.newHashMap();
        logger.info("Initialized SignalSystem");
    }

    @Override
    public void shutdown() {
        signalNetwork = null;
        signalProducers = null;
        signalConsumers = null;
        signalConductors = null;
    }

    private long lastUpdate;
    private static final long PROCESSING_MINIMUM_INTERVAL = 0;
    private static final boolean CONSUMER_CAN_POWER_ITSELF = true;

    @Override
    public void update(float delta) {
        long worldTime = time.getGameTimeInMs();
        if (worldTime > lastUpdate + PROCESSING_MINIMUM_INTERVAL) {
            lastUpdate = worldTime;

            // Mark all networks affected by the producer signal change
            for (NetworkNode producerChanges : producersSignalsChanged)
                networksToRecalculate.addAll(producerNetworks.get(producerChanges));

            Set<NetworkNode> consumersToEvaluate = Sets.newHashSet();

            for (Network network : networksToRecalculate) {
                if (signalNetwork.isNetworkActive(network)) {
                    Collection<NetworkNode> consumersInNetwork = this.consumersInNetwork.get(network);
                    for (NetworkNode consumerLocation : consumersInNetwork) {
                        NetworkSignals consumerSignalInNetwork = getConsumerSignalInNetwork(network, consumerLocation);
                        consumerSignalInNetworks.get(consumerLocation).put(network, consumerSignalInNetwork);
                    }
                    consumersToEvaluate.addAll(consumersInNetwork);
                }
            }

            for (NetworkNode modifiedConsumer : consumersToRecalculate) {
                Collection<Network> networks = consumerNetworks.get(modifiedConsumer);
                for (Network network : networks) {
                    NetworkSignals consumerSignalInNetwork = getConsumerSignalInNetwork(network, modifiedConsumer);
                    consumerSignalInNetworks.get(modifiedConsumer).put(network, consumerSignalInNetwork);
                }
                consumersToEvaluate.add(modifiedConsumer);
            }

            // Clearing the changed states
            producersSignalsChanged.clear();
            networksToRecalculate.clear();
            consumersToRecalculate.clear();

            // Send consumer status changes
            for (NetworkNode consumerToEvaluate : consumersToEvaluate) {
                if (signalConsumers.containsValue(consumerToEvaluate)) {
                    final EntityRef blockEntity = blockEntityRegistry.getBlockEntityAt(consumerToEvaluate.location.toVector3i());
                    final SignalConsumerComponent consumerComponent = blockEntity.getComponent(SignalConsumerComponent.class);
                    if (consumerComponent != null) {
                        Map<Network, NetworkSignals> consumerSignals = consumerSignalInNetworks.get(consumerToEvaluate);
                        processSignalConsumerResult(consumerSignals.values(), consumerComponent, blockEntity);
                    }
                }
            }
        }
    }

    private void processSignalConsumerResult(Collection<NetworkSignals> networkSignals, SignalConsumerComponent signalConsumerComponent, EntityRef entity) {
        final SignalConsumerComponent.Mode mode = signalConsumerComponent.mode;
        switch (mode) {
            // OR
            case AT_LEAST_ONE: {
                final boolean signal = hasSignalForOr(networkSignals);
                outputSignalToSimpleConsumer(entity, signal);
                return;
            }
            // AND
            case ALL_CONNECTED: {
                final boolean signal = hasSignalForAnd(networkSignals);
                outputSignalToSimpleConsumer(entity, signal);
                return;
            }
            // XOR
            case EXACTLY_ONE: {
                final boolean signal = hasSignalForXor(networkSignals);
                outputSignalToSimpleConsumer(entity, signal);
                return;
            }
            // Special leaving the calculation to the block's system itself
            case SPECIAL: {
                outputSignalToAdvancedConsumer(entity, networkSignals);
                return;
            }
            default:
                throw new IllegalArgumentException("Unknown mode set for SignalConsumerComponent");
        }
    }

    private void outputSignalToAdvancedConsumer(EntityRef entity, Collection<NetworkSignals> networkSignals) {
        final SignalConsumerAdvancedStatusComponent advancedStatusComponent = entity.getComponent(SignalConsumerAdvancedStatusComponent.class);
        byte withoutSignal = 0;
        byte withSignal = 0;
        if (networkSignals != null) {
            for (NetworkSignals networkSignal : networkSignals) {
                withoutSignal|=networkSignal.sidesWithoutSignal;
                withSignal|=networkSignal.sidesWithSignal;
            }
        }
        if (advancedStatusComponent.sidesWithoutSignals != withoutSignal
                || advancedStatusComponent.sidesWithSignals != withSignal) {
            advancedStatusComponent.sidesWithoutSignals = withoutSignal;
            advancedStatusComponent.sidesWithSignals = withSignal;
            entity.saveComponent(advancedStatusComponent);
        }
    }

    private void outputSignalToSimpleConsumer(EntityRef entity, boolean result) {
        final SignalConsumerStatusComponent consumerStatusComponent = entity.getComponent(SignalConsumerStatusComponent.class);
        if (consumerStatusComponent.hasSignal != result) {
            consumerStatusComponent.hasSignal = result;
            entity.saveComponent(consumerStatusComponent);
            logger.debug("Consumer has signal: " + result);
        }
    }

    private boolean hasSignalForXor(Collection<NetworkSignals> networkSignals) {
        if (networkSignals == null)
            return false;
        boolean connected = false;
        for (NetworkSignals networkSignal : networkSignals) {
            if (SideBitFlag.getSides(networkSignal.sidesWithSignal).size() > 1) {
                // More than one side connected in network
                return false;
            } else if (networkSignal.sidesWithSignal > 0) {
                if (connected) {
                    // One side connected in network, but already connected in other network
                    return false;
                } else {
                    connected = true;
                }
            }
        }

        return connected;
    }

    private boolean hasSignalForAnd(Collection<NetworkSignals> networkSignals) {
        if (networkSignals == null)
            return false;
        for (NetworkSignals networkSignal : networkSignals) {
            if (networkSignal.sidesWithoutSignal > 0) return false;
        }
        return true;
    }

    private boolean hasSignalForOr(Collection<NetworkSignals> networkSignals) {
        if (networkSignals == null)
            return false;
        for (NetworkSignals networkSignal : networkSignals) {
            if (networkSignal.sidesWithSignal > 0) return true;
        }
        return false;
    }

    //
    private NetworkSignals getConsumerSignalInNetwork(Network network, NetworkNode consumerNode) {
        // Check for infinite signal strength (-1), if there - it powers whole network
        final Collection<NetworkNode> producers = producersInNetwork.get(network);
        for (NetworkNode producer : producers) {
            if (CONSUMER_CAN_POWER_ITSELF || !producer.location.equals(consumerNode.location)) {
                final int signalStrength = producerSignalStrengths.get(producer);
                if (signalStrength == -1)
                    return new NetworkSignals(network.getLeafSidesInNetwork(consumerNode), (byte) 0);
            }
        }

        byte sidesInNetwork = network.getLeafSidesInNetwork(consumerNode);
        byte sidesWithSignal = 0;

        for (Side sideInNetwork : SideBitFlag.getSides(sidesInNetwork)) {
            if (hasSignalInNetworkOnSide(network, consumerNode, producers, sideInNetwork)) {
                sidesWithSignal += SideBitFlag.getSide(sideInNetwork);
                break;
            }
        }

        return new NetworkSignals(sidesWithSignal, (byte) (sidesInNetwork - sidesWithSignal));
    }

    private boolean hasSignalInNetworkOnSide(Network network, NetworkNode consumerNode, Collection<NetworkNode> producers, Side sideInNetwork) {
        for (NetworkNode producer : producers) {
            if (CONSUMER_CAN_POWER_ITSELF || !producer.location.equals(consumerNode.location)) {
                final int signalStrength = producerSignalStrengths.get(producer);
                if (network.isInDistanceWithSide(signalStrength, producer, consumerNode, sideInNetwork))
                    return true;
            }
        }
        return false;
    }

    @Override
    public void networkAdded(Network newNetwork) {
    }

    @Override
    public void networkingNodesAdded(Network network, Set<NetworkNode> networkingNodes) {
        logger.debug("Cable added to network");
        networksToRecalculate.add(network);
    }

    @Override
    public void networkingNodesRemoved(Network network, Set<NetworkNode> networkingNodes) {
        logger.debug("Cable removed from network");
        networksToRecalculate.add(network);
    }

    @Override
    public void leafNodesAdded(Network network, Set<NetworkNode> leafNodes) {
        for (NetworkNode modifiedLeafNode : leafNodes) {
            if (((SignalNetworkNode) modifiedLeafNode).getType() == SignalNetworkNode.Type.PRODUCER) {
                logger.debug("Producer added to network");
                networksToRecalculate.add(network);
                producerNetworks.put(modifiedLeafNode, network);
                producersInNetwork.put(network, modifiedLeafNode);
            } else {
                logger.debug("Consumer added to network");
                consumersToRecalculate.add(modifiedLeafNode);
                consumerNetworks.put(modifiedLeafNode, network);
                consumersInNetwork.put(network, modifiedLeafNode);
            }
        }
    }

    @Override
    public void leafNodesRemoved(Network network, Set<NetworkNode> leafNodes) {
        for (NetworkNode modifiedLeafNode : leafNodes) {
            if (((SignalNetworkNode) modifiedLeafNode).getType() == SignalNetworkNode.Type.PRODUCER) {
                logger.debug("Producer removed from network");
                networksToRecalculate.add(network);
                producerNetworks.remove(modifiedLeafNode, network);
                producersInNetwork.remove(network, modifiedLeafNode);
            } else {
                logger.debug("Consumer removed from network");
                consumersToRecalculate.add(modifiedLeafNode);
                consumerNetworks.remove(modifiedLeafNode, network);
                consumersInNetwork.remove(network, modifiedLeafNode);
                consumerSignalInNetworks.get(modifiedLeafNode).remove(network);
            }
        }
    }

    @Override
    public void networkRemoved(Network network) {
    }

    private SignalNetworkNode toNode(Vector3i location, byte directions, SignalNetworkNode.Type type) {
        return new SignalNetworkNode(location, directions, type);
    }

    /*
     * ****************************** Conductor events ********************************
     */

    @ReceiveEvent(components = {SignalConductorComponent.class})
    public void prefabConductorLoaded(OnActivatedBlocks event, EntityRef blockType) {
        byte connectingOnSides = blockType.getComponent(SignalConductorComponent.class).connectionSides;
        Set<NetworkNode> conductorNodes = Sets.newHashSet();
        for (Vector3i location : event.getBlockPositions()) {
            final NetworkNode conductorNode = toNode(location, connectingOnSides, SignalNetworkNode.Type.CONDUCTOR);
            conductorNodes.add(conductorNode);

            signalConductors.put(conductorNode.location, conductorNode);
        }
        signalNetwork.addNetworkingBlocks(conductorNodes);
    }

    @ReceiveEvent(components = {SignalConductorComponent.class})
    public void prefabConductorUnloaded(BeforeDeactivateBlocks event, EntityRef blockType) {
        byte connectingOnSides = blockType.getComponent(SignalConductorComponent.class).connectionSides;
        Set<NetworkNode> conductorNodes = Sets.newHashSet();
        // Quite messy due to the order of operations, need to check if the order is important
        for (Vector3i location : event.getBlockPositions()) {
            final NetworkNode conductorNode = toNode(location, connectingOnSides, SignalNetworkNode.Type.CONDUCTOR);
            conductorNodes.add(conductorNode);
        }
        signalNetwork.removeNetworkingBlocks(conductorNodes);
        for (NetworkNode conductorNode : conductorNodes) {
            signalConductors.remove(conductorNode.location);
        }
    }

    @ReceiveEvent(components = {BlockComponent.class, SignalConductorComponent.class})
    public void conductorAdded(OnActivatedComponent event, EntityRef block) {
        byte connectingOnSides = block.getComponent(SignalConductorComponent.class).connectionSides;

        final Vector3i location = new Vector3i(block.getComponent(BlockComponent.class).getPosition());
        final NetworkNode conductorNode = toNode(location, connectingOnSides, SignalNetworkNode.Type.CONDUCTOR);

        signalConductors.put(conductorNode.location, conductorNode);
        signalNetwork.addNetworkingBlock(conductorNode);
    }

    @ReceiveEvent(components = {SignalConductorComponent.class})
    public void conductorUpdated(OnChangedComponent event, EntityRef block) {
        if (block.hasComponent(BlockComponent.class)) {
            byte connectingOnSides = block.getComponent(SignalConductorComponent.class).connectionSides;

            final Vector3i location = new Vector3i(block.getComponent(BlockComponent.class).getPosition());

            final ImmutableBlockLocation blockLocation = new ImmutableBlockLocation(location);
            final NetworkNode oldConductorNode = signalConductors.get(blockLocation);
            if (oldConductorNode != null) {
                final NetworkNode newConductorNode = toNode(new Vector3i(location), connectingOnSides, SignalNetworkNode.Type.CONDUCTOR);
                signalConductors.put(newConductorNode.location, newConductorNode);
                signalNetwork.updateNetworkingBlock(oldConductorNode, newConductorNode);
            }
        }
    }

    @ReceiveEvent(components = {BlockComponent.class, SignalConductorComponent.class})
    public void conductorRemoved(BeforeDeactivateComponent event, EntityRef block) {
        byte connectingOnSides = block.getComponent(SignalConductorComponent.class).connectionSides;

        final Vector3i location = new Vector3i(block.getComponent(BlockComponent.class).getPosition());
        final NetworkNode conductorNode = toNode(location, connectingOnSides, SignalNetworkNode.Type.CONDUCTOR);
        signalNetwork.removeNetworkingBlock(conductorNode);
        signalConductors.remove(conductorNode.location);
    }

    /*
     * ****************************** Producer events ********************************
     */

    @ReceiveEvent(components = {SignalProducerComponent.class})
    public void prefabProducerLoaded(OnActivatedBlocks event, EntityRef blockType) {
        final SignalProducerComponent producerComponent = blockType.getComponent(SignalProducerComponent.class);
        byte connectingOnSides = producerComponent.connectionSides;
        int signalStrength = producerComponent.signalStrength;
        Set<NetworkNode> producerNodes = Sets.newHashSet();
        for (Vector3i location : event.getBlockPositions()) {
            final NetworkNode producerNode = toNode(location, connectingOnSides, SignalNetworkNode.Type.PRODUCER);

            signalProducers.put(producerNode.location, producerNode);
            producerSignalStrengths.put(producerNode, signalStrength);
            producerNodes.add(producerNode);
        }
        signalNetwork.addLeafBlocks(producerNodes);
    }

    @ReceiveEvent(components = {SignalProducerComponent.class})
    public void prefabProducerUnloaded(BeforeDeactivateBlocks event, EntityRef blockType) {
        byte connectingOnSides = blockType.getComponent(SignalProducerComponent.class).connectionSides;
        // Quite messy due to the order of operations, need to check if the order is important
        Set<NetworkNode> producerNodes = Sets.newHashSet();
        for (Vector3i location : event.getBlockPositions()) {
            final NetworkNode producerNode = toNode(location, connectingOnSides, SignalNetworkNode.Type.PRODUCER);
            producerNodes.add(producerNode);
        }

        signalNetwork.removeLeafBlocks(producerNodes);
        for (NetworkNode producerNode : producerNodes) {
            signalProducers.remove(producerNode.location);
            producerSignalStrengths.remove(producerNode);
        }
    }

    @ReceiveEvent(components = {BlockComponent.class, SignalProducerComponent.class})
    public void producerAdded(OnActivatedComponent event, EntityRef block) {
        Vector3i location = new Vector3i(block.getComponent(BlockComponent.class).getPosition());
        final SignalProducerComponent producerComponent = block.getComponent(SignalProducerComponent.class);
        final int signalStrength = producerComponent.signalStrength;
        byte connectingOnSides = producerComponent.connectionSides;

        final NetworkNode producerNode = toNode(location, connectingOnSides, SignalNetworkNode.Type.PRODUCER);

        signalProducers.put(producerNode.location, producerNode);
        producerSignalStrengths.put(producerNode, signalStrength);
        signalNetwork.addLeafBlock(producerNode);
    }

    @ReceiveEvent(components = {SignalProducerComponent.class})
    public void producerUpdated(OnChangedComponent event, EntityRef block) {
        logger.debug("Producer updated: " + block.getParentPrefab());
        if (block.hasComponent(BlockComponent.class)) {
            Vector3i location = new Vector3i(block.getComponent(BlockComponent.class).getPosition());
            ImmutableBlockLocation blockLocation = new ImmutableBlockLocation(location);
            final SignalProducerComponent producerComponent = block.getComponent(SignalProducerComponent.class);

            // We need to figure out, what exactly was changed
            final byte oldConnectionSides = signalProducers.get(blockLocation).connectionSides;
            byte newConnectionSides = producerComponent.connectionSides;

            NetworkNode node = toNode(location, newConnectionSides, SignalNetworkNode.Type.PRODUCER);
            NetworkNode oldNode = toNode(location, oldConnectionSides, SignalNetworkNode.Type.PRODUCER);
            if (oldConnectionSides != newConnectionSides) {
                producerSignalStrengths.put(node, producerComponent.signalStrength);
                signalProducers.put(node.location, node);
                signalNetwork.updateLeafBlock(oldNode, node);
                producerSignalStrengths.remove(oldNode);
            } else {
                int oldSignalStrength = producerSignalStrengths.get(oldNode);
                int newSignalStrength = producerComponent.signalStrength;
                if (oldSignalStrength != newSignalStrength) {
                    producerSignalStrengths.put(node, newSignalStrength);
                    producersSignalsChanged.add(node);
                }
            }
        }
    }

    @ReceiveEvent(components = {BlockComponent.class, SignalProducerComponent.class})
    public void producerRemoved(BeforeDeactivateComponent event, EntityRef block) {
        Vector3i location = new Vector3i(block.getComponent(BlockComponent.class).getPosition());
        byte connectingOnSides = block.getComponent(SignalProducerComponent.class).connectionSides;

        final NetworkNode producerNode = toNode(location, connectingOnSides, SignalNetworkNode.Type.PRODUCER);
        signalNetwork.removeLeafBlock(producerNode);
        signalProducers.remove(producerNode.location);
        producerSignalStrengths.remove(producerNode);
    }

    /*
     * ****************************** Consumer events ********************************
     */

    @ReceiveEvent(components = {SignalConsumerComponent.class})
    public void prefabConsumerLoaded(OnActivatedBlocks event, EntityRef blockType) {
        byte connectingOnSides = blockType.getComponent(SignalConsumerComponent.class).connectionSides;
        Set<NetworkNode> consumerNodes = Sets.newHashSet();
        for (Vector3i location : event.getBlockPositions()) {
            NetworkNode consumerNode = toNode(location, connectingOnSides, SignalNetworkNode.Type.CONSUMER);

            signalConsumers.put(consumerNode.location, consumerNode);
            consumerSignalInNetworks.put(consumerNode, Maps.<Network, NetworkSignals>newHashMap());
            consumerNodes.add(consumerNode);
        }
        signalNetwork.addLeafBlocks(consumerNodes);
    }

    @ReceiveEvent(components = {SignalConsumerComponent.class})
    public void prefabConsumerUnloaded(BeforeDeactivateBlocks event, EntityRef blockType) {
        byte connectingOnSides = blockType.getComponent(SignalConsumerComponent.class).connectionSides;
        Set<NetworkNode> consumerNodes = Sets.newHashSet();

        // Quite messy due to the order of operations, need to check if the order is important
        for (Vector3i location : event.getBlockPositions()) {
            NetworkNode consumerNode = toNode(location, connectingOnSides, SignalNetworkNode.Type.CONSUMER);
            consumerNodes.add(consumerNode);
        }

        signalNetwork.removeLeafBlocks(consumerNodes);
        for (NetworkNode consumerNode : consumerNodes) {
            signalConsumers.remove(consumerNode.location);
            consumerSignalInNetworks.remove(consumerNode);
        }
    }

    @ReceiveEvent(components = {BlockComponent.class, SignalConsumerComponent.class})
    public void consumerAdded(OnActivatedComponent event, EntityRef block) {
        Vector3i location = new Vector3i(block.getComponent(BlockComponent.class).getPosition());
        byte connectingOnSides = block.getComponent(SignalConsumerComponent.class).connectionSides;

        NetworkNode consumerNode = toNode(location, connectingOnSides, SignalNetworkNode.Type.CONSUMER);

        signalConsumers.put(consumerNode.location, consumerNode);
        consumerSignalInNetworks.put(consumerNode, Maps.<Network, NetworkSignals>newHashMap());
        signalNetwork.addLeafBlock(consumerNode);
    }

    @ReceiveEvent(components = {SignalConsumerComponent.class})
    public void consumerUpdated(OnChangedComponent event, EntityRef block) {
        if (block.hasComponent(BlockComponent.class)) {
            Vector3i location = new Vector3i(block.getComponent(BlockComponent.class).getPosition());
            ImmutableBlockLocation blockLocation = new ImmutableBlockLocation(location);
            final SignalConsumerComponent consumerComponent = block.getComponent(SignalConsumerComponent.class);

            // We need to figure out, what exactly was changed
            final byte oldConnectionSides = signalConsumers.get(blockLocation).connectionSides;
            byte newConnectionSides = consumerComponent.connectionSides;

            NetworkNode node = toNode(location, newConnectionSides, SignalNetworkNode.Type.CONSUMER);
            if (oldConnectionSides != newConnectionSides) {
                signalConsumers.put(node.location, node);
                SignalNetworkNode oldNode = toNode(location, oldConnectionSides, SignalNetworkNode.Type.CONSUMER);
                consumerSignalInNetworks.put(node, Maps.<Network, NetworkSignals>newHashMap());

                signalNetwork.updateLeafBlock(oldNode, node);

                consumerSignalInNetworks.remove(oldNode);
            }
            // Mode could have changed
            consumersToRecalculate.add(node);
        }
    }

    @ReceiveEvent(components = {BlockComponent.class, SignalConsumerComponent.class})
    public void consumerRemoved(BeforeDeactivateComponent event, EntityRef block) {
        Vector3i location = new Vector3i(block.getComponent(BlockComponent.class).getPosition());
        byte connectingOnSides = block.getComponent(SignalConsumerComponent.class).connectionSides;

        final NetworkNode consumerNode = toNode(location, connectingOnSides, SignalNetworkNode.Type.CONSUMER);
        signalNetwork.removeLeafBlock(consumerNode);
        signalConsumers.remove(consumerNode.location);
        consumerSignalInNetworks.remove(consumerNode);
    }

}
