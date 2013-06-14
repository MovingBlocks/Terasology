package org.terasology.signalling.componentSystem;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.terasology.blockNetwork.BlockNetwork;
import org.terasology.blockNetwork.NetworkNode;
import org.terasology.blockNetwork.NetworkTopologyListener;
import org.terasology.blockNetwork.Network;
import org.terasology.entitySystem.*;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.lifecycleEvents.*;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.math.Vector3i;
import org.terasology.signalling.components.SignalConductorComponent;
import org.terasology.signalling.components.SignalConsumerComponent;
import org.terasology.signalling.components.SignalConsumerStatusComponent;
import org.terasology.signalling.components.SignalProducerComponent;
import org.terasology.world.BlockEntityRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.world.block.entity.BlockComponent;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@RegisterSystem(value = RegisterMode.AUTHORITY)
public class SignalSystem implements UpdateSubscriberSystem, NetworkTopologyListener {
    private static final Logger logger = LoggerFactory.getLogger(SignalSystem.class);

    @In
    private BlockEntityRegistry blockEntityRegistry;

    private BlockNetwork signalNetwork;

    // we assume there can be only one consumer, one producer, and/or one conductor per block
    private Map<Vector3i, NetworkNode> signalProducers;
    private Map<Vector3i, NetworkNode> signalConsumers;
    private Map<Vector3i, NetworkNode> signalConductors;

    private Multimap<NetworkNode, Network> producerNetworks = HashMultimap.create();
    private Multimap<Network, NetworkNode> producersInNetwork = HashMultimap.create();

    private Multimap<NetworkNode, Network> consumerNetworks = HashMultimap.create();
    private Multimap<Network, NetworkNode> consumersInNetwork = HashMultimap.create();

    // Used to detect producer changes
    private Map<NetworkNode, Integer> producerSignalStrengths = Maps.newHashMap();
    // Used to store signal for consumer from non-modified networks
    private Map<NetworkNode, Map<Network, Boolean>> consumerSignalInNetworks = Maps.newHashMap();

    private Set<Network> networksToRecalculate = Sets.newHashSet();
    private Set<NetworkNode> consumersToRecalculate = Sets.newHashSet();
    private Set<NetworkNode> producersSignalsChanged = Sets.newHashSet();

    @Override
    public void initialise() {
        signalNetwork = new BlockNetwork();
        signalNetwork.addTopologyListener(this);
        signalProducers = Maps.newHashMap();
        signalConsumers = Maps.newHashMap();
        signalConductors = Maps.newHashMap();
    }

    @Override
    public void shutdown() {
        signalNetwork = null;
        signalProducers = null;
        signalConsumers = null;
        signalConductors = null;
    }

    @Override
    public void update(float delta) {
        // Mark all networks affected by the producer signal change
        for (NetworkNode producerChanges : producersSignalsChanged)
            networksToRecalculate.addAll(producerNetworks.get(producerChanges));

        Set<NetworkNode> consumersToEvaluate = Sets.newHashSet();

        for (Network network : networksToRecalculate) {
            if (signalNetwork.isNetworkActive(network)) {
                Collection<NetworkNode> consumersInNetwork = this.consumersInNetwork.get(network);
                for (NetworkNode consumerLocation : consumersInNetwork) {
                    boolean consumerSignalInNetwork = getConsumerSignalInNetwork(network, consumerLocation);
                    consumerSignalInNetworks.get(consumerLocation).put(network, consumerSignalInNetwork);
                }
                consumersToEvaluate.addAll(consumersInNetwork);
            }
        }

        for (NetworkNode modifiedConsumer : consumersToRecalculate) {
            Collection<Network> networks = consumerNetworks.get(modifiedConsumer);
            for (Network network : networks) {
                boolean consumerSignalInNetwork = getConsumerSignalInNetwork(network, modifiedConsumer);
                consumerSignalInNetworks.get(modifiedConsumer).put(network, consumerSignalInNetwork);
            }
            consumersToEvaluate.add(modifiedConsumer);
        }

        // Send consumer status changes
        for (NetworkNode consumerToEvaluate : consumersToEvaluate) {
            if (signalConsumers.containsValue(consumerToEvaluate)) {
                final EntityRef blockEntity = blockEntityRegistry.getBlockEntityAt(consumerToEvaluate.location.toVector3i());
                final SignalConsumerStatusComponent consumerStatusComponent = blockEntity.getComponent(SignalConsumerStatusComponent.class);
                final SignalConsumerComponent consumerComponent = blockEntity.getComponent(SignalConsumerComponent.class);
                if (consumerComponent != null && consumerStatusComponent != null) {
                    Map<Network, Boolean> consumerSignals = consumerSignalInNetworks.get(consumerToEvaluate);
                    boolean newSignal = false;
                    if (consumerSignals != null)
                        newSignal = calculateResultSignal(consumerSignals.values(), consumerComponent);
                    if (newSignal != consumerStatusComponent.hasSignal) {
                        consumerStatusComponent.hasSignal = newSignal;
                        blockEntity.saveComponent(consumerStatusComponent);
                        logger.info("Consumer has signal: " + newSignal);
                    }
                }
            }
        }

        producersSignalsChanged.clear();
        networksToRecalculate.clear();
        consumersToRecalculate.clear();
    }

    private Boolean calculateResultSignal(Collection<Boolean> values, SignalConsumerComponent signalConsumerComponent) {
        final SignalConsumerComponent.Mode mode = signalConsumerComponent.mode;
        switch (mode) {
            case AT_LEAST_ONE: {
                for (Boolean value : values) {
                    if (value)
                        return true;
                }
                return false;
            }
            case ALL_CONNECTED: {
                for (Boolean value : values) {
                    if (!value)
                        return false;
                }
                return true;
            }
            default:
                throw new IllegalArgumentException("Unknown mode set for SignalConsumerComponent");
        }
    }

    //
    private boolean getConsumerSignalInNetwork(Network network, NetworkNode location) {
        // Check for infinite signal strength (-1), if there - it powers whole network
        final Collection<NetworkNode> producers = producersInNetwork.get(network);
        for (NetworkNode producer : producers) {
            final int signalStrength = producerSignalStrengths.get(producer);
            if (signalStrength == -1)
                return true;
        }

        for (NetworkNode producerLocation : producers) {
            final int signalStrength = producerSignalStrengths.get(producerLocation);
            if (network.isInDistance(signalStrength, producerLocation, location))
                return true;
        }

        return false;
    }

    @Override
    public void networkAdded(Network newNetwork) {
    }

    @Override
    public void networkingNodesAdded(Network network, Set<NetworkNode> networkingNodes) {
        logger.info("Cable added to network");
        networksToRecalculate.add(network);
    }

    @Override
    public void networkingNodesRemoved(Network network, Set<NetworkNode> networkingNodes) {
        logger.info("Cable removed from network");
        networksToRecalculate.add(network);
    }

    @Override
    public void leafNodesAdded(Network network, Set<NetworkNode> leafNodes) {
        for (NetworkNode modifiedLeafNode : leafNodes) {
            if (signalProducers.containsValue(modifiedLeafNode)) {
                logger.info("Producer added to network");
                networksToRecalculate.add(network);
                producerNetworks.put(modifiedLeafNode, network);
                producersInNetwork.put(network, modifiedLeafNode);
            } else {
                logger.info("Consumer added to network");
                consumersToRecalculate.add(modifiedLeafNode);
                consumerNetworks.put(modifiedLeafNode, network);
                consumersInNetwork.put(network, modifiedLeafNode);
            }
        }
    }

    @Override
    public void leafNodesRemoved(Network network, Set<NetworkNode> leafNodes) {
        for (NetworkNode modifiedLeafNode : leafNodes) {
            if (signalProducers.containsValue(modifiedLeafNode)) {
                logger.info("Producer removed from network");
                networksToRecalculate.add(network);
                producerNetworks.remove(modifiedLeafNode, network);
                producersInNetwork.remove(network, modifiedLeafNode);
            } else {
                logger.info("Consumer removed from network");
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

    private NetworkNode toNode(Vector3i location, byte directions) {
        return new NetworkNode(location, directions);
    }

    /**
     * ****************************** Conductor events ********************************
     */

    @ReceiveEvent(components = {BlockComponent.class, SignalConductorComponent.class})
    public void conductorAdded(OnActivatedComponent event, EntityRef block) {
        byte connectingOnSides = block.getComponent(SignalConductorComponent.class).connectionSides;

        final Vector3i location = new Vector3i(block.getComponent(BlockComponent.class).getPosition());
        final NetworkNode conductorNode = toNode(location, connectingOnSides);

        signalConductors.put(location, conductorNode);
        signalNetwork.addNetworkingBlock(conductorNode);
    }

    @ReceiveEvent(components = {SignalConductorComponent.class})
    public void conductorUpdated(OnChangedComponent event, EntityRef block) {
        if (block.hasComponent(BlockComponent.class)) {
            byte connectingOnSides = block.getComponent(SignalConductorComponent.class).connectionSides;

            final Vector3i location = new Vector3i(block.getComponent(BlockComponent.class).getPosition());

            final NetworkNode oldConductorNode = signalConductors.get(location);
            if (oldConductorNode != null) {
                final NetworkNode newConductorNode = toNode(new Vector3i(location), connectingOnSides);
                signalConductors.put(location, newConductorNode);
                signalNetwork.updateNetworkingBlock(oldConductorNode, newConductorNode);
            }
        }
    }

    @ReceiveEvent(components = {BlockComponent.class, SignalConductorComponent.class})
    public void conductorRemoved(BeforeDeactivateComponent event, EntityRef block) {
        byte connectingOnSides = block.getComponent(SignalConductorComponent.class).connectionSides;

        final Vector3i location = new Vector3i(block.getComponent(BlockComponent.class).getPosition());
        signalConductors.remove(location);
        signalNetwork.removeNetworkingBlock(toNode(location, connectingOnSides));
    }

    /**
     * ****************************** Producer events ********************************
     */

    @ReceiveEvent(components = {BlockComponent.class, SignalProducerComponent.class})
    public void producerAdded(OnActivatedComponent event, EntityRef block) {
        Vector3i location = new Vector3i(block.getComponent(BlockComponent.class).getPosition());
        final SignalProducerComponent producerComponent = block.getComponent(SignalProducerComponent.class);
        byte connectingOnSides = producerComponent.connectionSides;

        final NetworkNode producerNode = toNode(location, connectingOnSides);

        signalProducers.put(location, producerNode);
        producerSignalStrengths.put(producerNode, producerComponent.signalStrength);
        signalNetwork.addLeafBlock(producerNode);
    }

    @ReceiveEvent(components = {SignalProducerComponent.class})
    public void producerUpdated(OnChangedComponent event, EntityRef block) {
        if (block.hasComponent(BlockComponent.class)) {
            Vector3i location = new Vector3i(block.getComponent(BlockComponent.class).getPosition());
            final SignalProducerComponent producerComponent = block.getComponent(SignalProducerComponent.class);

            // We need to figure out, what exactly was changed
            final byte oldConnectionSides = signalProducers.get(location).connectionSides;
            byte newConnectionSides = producerComponent.connectionSides;

            NetworkNode node = toNode(location, newConnectionSides);
            if (oldConnectionSides != newConnectionSides) {
                signalProducers.put(location, node);
                final NetworkNode oldNode = toNode(location, oldConnectionSides);
                signalNetwork.updateLeafBlock(oldNode, node);
            }

            int oldSignalStrength = producerSignalStrengths.get(node);
            int newSignalStrength = producerComponent.signalStrength;
            if (oldSignalStrength != newSignalStrength) {
                producerSignalStrengths.put(node, newSignalStrength);
                producersSignalsChanged.add(node);
            }
        }
    }

    @ReceiveEvent(components = {BlockComponent.class, SignalProducerComponent.class})
    public void producerRemoved(BeforeDeactivateComponent event, EntityRef block) {
        Vector3i location = new Vector3i(block.getComponent(BlockComponent.class).getPosition());
        byte connectingOnSides = block.getComponent(SignalProducerComponent.class).connectionSides;

        final NetworkNode producer = toNode(location, connectingOnSides);
        signalNetwork.removeLeafBlock(producer);
        signalProducers.remove(location);
        producerSignalStrengths.remove(producer);
    }

    /**
     * ****************************** Consumer events ********************************
     */

    @ReceiveEvent(components = {BlockComponent.class, SignalConsumerComponent.class})
    public void consumerAdded(OnActivatedComponent event, EntityRef block) {
        Vector3i location = new Vector3i(block.getComponent(BlockComponent.class).getPosition());
        byte connectingOnSides = block.getComponent(SignalConsumerComponent.class).connectionSides;

        NetworkNode consumerNode = toNode(location, connectingOnSides);

        signalConsumers.put(location, consumerNode);
        consumerSignalInNetworks.put(consumerNode, Maps.<Network, Boolean>newHashMap());
        signalNetwork.addLeafBlock(consumerNode);
    }

    @ReceiveEvent(components = {SignalConsumerComponent.class})
    public void consumerUpdated(OnChangedComponent event, EntityRef block) {
        if (block.hasComponent(BlockComponent.class)) {
            Vector3i location = new Vector3i(block.getComponent(BlockComponent.class).getPosition());
            final SignalConsumerComponent consumerComponent = block.getComponent(SignalConsumerComponent.class);

            // We need to figure out, what exactly was changed
            final byte oldConnectionSides = signalConsumers.get(location).connectionSides;
            byte newConnectionSides = consumerComponent.connectionSides;

            NetworkNode node = toNode(location, newConnectionSides);
            if (oldConnectionSides != newConnectionSides) {
                signalConsumers.put(location, node);
                signalNetwork.updateLeafBlock(toNode(location, oldConnectionSides), node);
            }
            // Mode could have changed
            consumersToRecalculate.add(node);
        }
    }

    @ReceiveEvent(components = {BlockComponent.class, SignalConsumerComponent.class})
    public void consumerRemoved(BeforeDeactivateComponent event, EntityRef block) {
        Vector3i location = new Vector3i(block.getComponent(BlockComponent.class).getPosition());
        byte connectingOnSides = block.getComponent(SignalConsumerComponent.class).connectionSides;

        final NetworkNode consumer = toNode(location, connectingOnSides);
        signalNetwork.removeLeafBlock(consumer);
        signalConsumers.remove(location);
        consumerSignalInNetworks.remove(consumer);
    }
}
