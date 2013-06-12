package org.terasology.signalling.componentSystem;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.terasology.blockNetwork.BlockNetwork;
import org.terasology.blockNetwork.NetworkTopologyListener;
import org.terasology.blockNetwork.Network;
import org.terasology.entitySystem.*;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.lifecycleEvents.OnAddedEvent;
import org.terasology.entitySystem.lifecycleEvents.OnChangedEvent;
import org.terasology.entitySystem.lifecycleEvents.OnRemovedEvent;
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

@RegisterSystem(value=RegisterMode.AUTHORITY)
public class SignalSystem implements UpdateSubscriberSystem, NetworkTopologyListener {
    private static final Logger logger = LoggerFactory.getLogger(SignalSystem.class);

    @In
    private BlockEntityRegistry blockEntityRegistry;

    private BlockNetwork signalNetwork;

    private Map<Vector3i, Byte> signalProducers;
    private Map<Vector3i, Byte> signalConsumers;

    private Multimap<Vector3i, Network> producerNetworks = HashMultimap.create();
    private Multimap<Network, Vector3i> producersInNetwork = HashMultimap.create();

    private Multimap<Vector3i, Network> consumerNetworks = HashMultimap.create();
    private Multimap<Network, Vector3i> consumersInNetwork = HashMultimap.create();

    // Used to detect producer changes
    private Map<Vector3i, Integer> producerSignalStrengths = Maps.newHashMap();
    // Used to store signal for consumer from non-modified networks
    private Map<Vector3i, Map<Network, Boolean>> consumerSignalInNetworks = Maps.newHashMap();

    private Set<Network> networksToRecalculate = Sets.newHashSet();
    private Set<Vector3i> consumersToRecalculate = Sets.newHashSet();
    private Set<Vector3i> producersSignalsChanged = Sets.newHashSet();

    @Override
    public void initialise() {
        signalNetwork = new BlockNetwork();
        signalNetwork.addTopologyListener(this);
        signalProducers = Maps.newHashMap();
        signalConsumers = Maps.newHashMap();

    }

    @Override
    public void shutdown() {
        signalNetwork = null;
        signalProducers = null;
        signalConsumers = null;
    }

    @Override
    public void update(float delta) {
        // Mark all networks affected by the producer signal change
        for (Vector3i producerChanges : producersSignalsChanged)
            networksToRecalculate.addAll(producerNetworks.get(producerChanges));

        Set<Vector3i> consumersToEvaluate = Sets.newHashSet();

        for (Network network : networksToRecalculate) {
            if (signalNetwork.isNetworkActive(network)) {
                Collection<Vector3i> consumersInNetwork = this.consumersInNetwork.get(network);
                for (Vector3i consumerLocation : consumersInNetwork) {
                    boolean consumerSignalInNetwork = getConsumerSignalInNetwork(network, consumerLocation);
                    consumerSignalInNetworks.get(consumerLocation).put(network, consumerSignalInNetwork);
                }
                consumersToEvaluate.addAll(consumersInNetwork);
            }
        }

        for (Vector3i modifiedConsumer : consumersToRecalculate) {
            Collection<Network> networks = consumerNetworks.get(modifiedConsumer);
            for (Network network : networks) {
                boolean consumerSignalInNetwork = getConsumerSignalInNetwork(network, modifiedConsumer);
                consumerSignalInNetworks.get(modifiedConsumer).put(network, consumerSignalInNetwork);
            }
            consumersToEvaluate.add(modifiedConsumer);
        }

        // Send consumer status changes
        for (Vector3i consumerToEvaluate : consumersToEvaluate) {
            if (signalConsumers.containsKey(consumerToEvaluate)) {
                final EntityRef blockEntity = blockEntityRegistry.getBlockEntityAt(consumerToEvaluate);
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
    private boolean getConsumerSignalInNetwork(Network network, Vector3i location) {
        // Check for infinite signal strength (-1), if there - it powers whole network
        final Collection<Vector3i> producers = producersInNetwork.get(network);
        for (Vector3i producer : producers) {
            final int signalStrength = producerSignalStrengths.get(producer);
            if (signalStrength == -1)
                return true;
        }

        for (Vector3i producerLocation : producers) {
            final int signalStrength = producerSignalStrengths.get(producerLocation);
            if (network.isInDistance(signalStrength, producerLocation, signalProducers.get(producerLocation), location, signalConsumers.get(location)))
                return true;
        }

        return false;
    }

    @Override
    public void networkAdded(Network newNetwork) {
    }

    @Override
    public void networkingNodesAdded(Network network, Map<Vector3i, Byte> networkingNodes) {
        logger.info("Cable added to network");
        networksToRecalculate.add(network);
    }

    @Override
    public void networkingNodesRemoved(Network network, Map<Vector3i, Byte> networkingNodes) {
        logger.info("Cable removed from network");
        networksToRecalculate.add(network);
    }

    @Override
    public void leafNodesAdded(Network network, Multimap<Vector3i, Byte> leafNodes) {
        for (Map.Entry<Vector3i, Byte> modifiedLeafNode : leafNodes.entries()) {
            Vector3i nodeLocation = modifiedLeafNode.getKey();
            Byte producerConnectingSides = signalProducers.get(nodeLocation);
            if (producerConnectingSides != null && producerConnectingSides.byteValue() == modifiedLeafNode.getValue()) {
                logger.info("Producer added to network");
                networksToRecalculate.add(network);
                producerNetworks.put(nodeLocation, network);
                producersInNetwork.put(network, nodeLocation);
            } else {
                logger.info("Consumer added to network");
                consumersToRecalculate.add(nodeLocation);
                consumerNetworks.put(nodeLocation, network);
                consumersInNetwork.put(network, nodeLocation);
            }
        }
    }

    @Override
    public void leafNodesRemoved(Network network, Multimap<Vector3i, Byte> leafNodes) {
        for (Map.Entry<Vector3i, Byte> modifiedLeafNode : leafNodes.entries()) {
            Vector3i nodeLocation = modifiedLeafNode.getKey();
            Byte producerConnectingSides = signalProducers.get(nodeLocation);
            if (producerConnectingSides != null && producerConnectingSides.byteValue() == modifiedLeafNode.getValue()) {
                logger.info("Producer removed from network");
                networksToRecalculate.add(network);
                producerNetworks.remove(nodeLocation, network);
                producersInNetwork.remove(network, nodeLocation);
            } else {
                logger.info("Consumer removed from network");
                consumersToRecalculate.add(nodeLocation);
                consumerNetworks.remove(nodeLocation, network);
                consumersInNetwork.remove(network, nodeLocation);
                consumerSignalInNetworks.get(nodeLocation).remove(network);
            }
        }
    }

    @Override
    public void networkRemoved(Network network) {
    }

    /**
     * ****************************** Conductor events ********************************
     */

    @ReceiveEvent(components = {BlockComponent.class, SignalConductorComponent.class})
    public void conductorAdded(OnAddedEvent event, EntityRef block) {
        byte connectingOnSides = block.getComponent(SignalConductorComponent.class).connectionSides;

        signalNetwork.addNetworkingBlock(new Vector3i(block.getComponent(BlockComponent.class).getPosition()), connectingOnSides);
    }

    @ReceiveEvent(components = {SignalConductorComponent.class})
    public void conductorUpdated(OnChangedEvent event, EntityRef block) {
        if (block.hasComponent(BlockComponent.class)) {
            byte connectingOnSides = block.getComponent(SignalConductorComponent.class).connectionSides;

            signalNetwork.updateNetworkingBlock(new Vector3i(block.getComponent(BlockComponent.class).getPosition()), connectingOnSides);
        }
    }

    @ReceiveEvent(components = {BlockComponent.class, SignalConductorComponent.class})
    public void conductorRemoved(OnRemovedEvent event, EntityRef block) {
        signalNetwork.removeNetworkingBlock(new Vector3i(block.getComponent(BlockComponent.class).getPosition()));
    }

    /**
     * ****************************** Producer events ********************************
     */

    @ReceiveEvent(components = {BlockComponent.class, SignalProducerComponent.class})
    public void producerAdded(OnAddedEvent event, EntityRef block) {
        Vector3i location = new Vector3i(block.getComponent(BlockComponent.class).getPosition());
        final SignalProducerComponent producerComponent = block.getComponent(SignalProducerComponent.class);
        byte connectingOnSides = producerComponent.connectionSides;

        signalProducers.put(location, connectingOnSides);
        producerSignalStrengths.put(location, producerComponent.signalStrength);
        signalNetwork.addLeafBlock(location, connectingOnSides);
    }

    @ReceiveEvent(components = {SignalProducerComponent.class})
    public void producerUpdated(OnChangedEvent event, EntityRef block) {
        if (block.hasComponent(BlockComponent.class)) {
            Vector3i location = new Vector3i(block.getComponent(BlockComponent.class).getPosition());
            final SignalProducerComponent producerComponent = block.getComponent(SignalProducerComponent.class);

            // We need to figure out, what exactly was changed
            final Byte oldConnectionSides = signalProducers.get(location);
            byte newConnectionSides = producerComponent.connectionSides;
            if (oldConnectionSides != newConnectionSides) {
                signalProducers.put(location, newConnectionSides);
                signalNetwork.updateLeafBlock(location, oldConnectionSides, newConnectionSides);
            }

            int oldSignalStrength = producerSignalStrengths.get(location);
            int newSignalStrength = producerComponent.signalStrength;
            if (oldSignalStrength != newSignalStrength) {
                producerSignalStrengths.put(location, newSignalStrength);
                producersSignalsChanged.add(location);
            }
        }
    }

    @ReceiveEvent(components = {BlockComponent.class, SignalProducerComponent.class})
    public void producerRemoved(OnRemovedEvent event, EntityRef block) {
        Vector3i location = new Vector3i(block.getComponent(BlockComponent.class).getPosition());
        byte connectingOnSides = block.getComponent(SignalProducerComponent.class).connectionSides;

        signalNetwork.removeLeafBlock(location, connectingOnSides);
        signalProducers.remove(location);
        producerSignalStrengths.remove(location);
    }

    /**
     * ****************************** Consumer events ********************************
     */

    @ReceiveEvent(components = {BlockComponent.class, SignalConsumerComponent.class})
    public void consumerAdded(OnAddedEvent event, EntityRef block) {
        Vector3i location = new Vector3i(block.getComponent(BlockComponent.class).getPosition());
        byte connectingOnSides = block.getComponent(SignalConsumerComponent.class).connectionSides;

        signalConsumers.put(location, connectingOnSides);
        consumerSignalInNetworks.put(location, Maps.<Network, Boolean>newHashMap());
        signalNetwork.addLeafBlock(location, connectingOnSides);
    }

    @ReceiveEvent(components = {SignalConsumerComponent.class})
    public void consumerUpdated(OnChangedEvent event, EntityRef block) {
        if (block.hasComponent(BlockComponent.class)) {
            Vector3i location = new Vector3i(block.getComponent(BlockComponent.class).getPosition());
            final SignalConsumerComponent consumerComponent = block.getComponent(SignalConsumerComponent.class);

            // We need to figure out, what exactly was changed
            final Byte oldConnectionSides = signalConsumers.get(location);
            byte newConnectionSides = consumerComponent.connectionSides;
            if (oldConnectionSides != newConnectionSides) {
                signalConsumers.put(location, newConnectionSides);
                signalNetwork.updateLeafBlock(location, oldConnectionSides, newConnectionSides);
            }
            // Mode could have changed
            consumersToRecalculate.add(location);
        }
    }

    @ReceiveEvent(components = {BlockComponent.class, SignalConsumerComponent.class})
    public void consumerRemoved(OnRemovedEvent event, EntityRef block) {
        Vector3i location = new Vector3i(block.getComponent(BlockComponent.class).getPosition());
        byte connectingOnSides = block.getComponent(SignalConsumerComponent.class).connectionSides;

        signalNetwork.removeLeafBlock(location, connectingOnSides);
        signalConsumers.remove(location);
        consumerSignalInNetworks.remove(location);
    }
}
