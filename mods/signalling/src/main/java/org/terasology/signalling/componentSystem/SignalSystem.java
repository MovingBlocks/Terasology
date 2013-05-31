package org.terasology.signalling.componentSystem;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.terasology.blockNetwork.BlockNetwork;
import org.terasology.blockNetwork.BlockNetworkTopologyListener;
import org.terasology.blockNetwork.Network;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.*;
import org.terasology.entitySystem.event.AddComponentEvent;
import org.terasology.entitySystem.event.ChangedComponentEvent;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.math.Vector3i;
import org.terasology.signalling.components.SignalConductorComponent;
import org.terasology.signalling.components.SignalConsumerComponent;
import org.terasology.signalling.components.SignalProducerComponent;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.block.BlockComponent;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@RegisterComponentSystem
public class SignalSystem implements EventHandlerSystem, UpdateSubscriberSystem, BlockNetworkTopologyListener {
    @In
    private BlockEntityRegistry blockEntityRegistry;

    private BlockNetwork signalNetwork;

    private Map<Vector3i, Byte> signalProducers;
    private Map<Vector3i, Byte> signalConsumers;

    private Map<Vector3i, Map<Network, Boolean>> consumerSignalInNetworks = Maps.newHashMap();

    private Multimap<Vector3i, Network> producerNetworks = HashMultimap.create();
    private Multimap<Network, Vector3i> producersInNetwork = HashMultimap.create();

    private Map<Vector3i, Integer> producerSignalStrengths = Maps.newHashMap();

    private Set<Vector3i> modifiedProducerSignalsSinceLastUpdate = Sets.newHashSet();
    private Set<Network> modifiedNetworksSinceLastUpdate = Sets.newHashSet();

    @Override
    public void initialise() {
        signalNetwork = new BlockNetwork();
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
        if (!modifiedNetworksSinceLastUpdate.isEmpty()) {
            Set<Vector3i> modifiedConsumers = Sets.newHashSet();

            // Update all the locations of producers, we need to iterate through ALL producers as some might have been
            // added into a network they were not a part of before
            for (Map.Entry<Vector3i, Byte> producer : signalProducers.entrySet()) {
                final Vector3i producerLocation = producer.getKey();
                final byte producerConnections = producer.getValue();
                for (Network network : modifiedNetworksSinceLastUpdate) {
                    if (signalNetwork.isNetworkActive(network) && network.hasLeafNode(producerLocation, producerConnections)) {
                        producerNetworks.put(producerLocation, network);
                        producersInNetwork.put(network, producerLocation);
                    } else {
                        producerNetworks.remove(producerLocation, network);
                        producersInNetwork.remove(network, producerLocation);
                    }
                }
            }

            // Add networks with modified producer strengths to list of modified networks
            for (Vector3i producer : modifiedProducerSignalsSinceLastUpdate)
                modifiedNetworksSinceLastUpdate.addAll(producerNetworks.get(producer));

            // Calculate consumer status changes, we need to iterate through ALL consumers as some might have been
            // added into a network they was not a part of before
            for (Map.Entry<Vector3i, Byte> consumer : signalConsumers.entrySet()) {
                boolean modifiedAnySignal = false;
                final Vector3i consumerLocation = consumer.getKey();
                final byte consumerConnections = consumer.getValue();

                final Map<Network, Boolean> networkSignals = consumerSignalInNetworks.get(consumerLocation);
                for (Network network : modifiedNetworksSinceLastUpdate) {
                    if (signalNetwork.isNetworkActive(network) && network.hasLeafNode(consumerLocation, consumerConnections)) {
                        boolean newSignal = getConsumerSignalInNetwork(network, consumerLocation);
                        final Boolean oldSignal = networkSignals.get(network);
                        if (oldSignal == null || oldSignal != newSignal) {
                            networkSignals.put(network, newSignal);
                            modifiedAnySignal = true;
                        }
                    } else {
                        modifiedAnySignal = (networkSignals.remove(network) != null);
                    }
                }

                if (modifiedAnySignal)
                    modifiedConsumers.add(consumerLocation);
            }

            modifiedProducerSignalsSinceLastUpdate.clear();
            modifiedNetworksSinceLastUpdate.clear();

            // Send consumer status changes
            for (Vector3i modifiedConsumer : modifiedConsumers) {
                final EntityRef blockEntity = blockEntityRegistry.getBlockEntityAt(modifiedConsumer);
                final SignalConsumerComponent consumerComponent = blockEntity.getComponent(SignalConsumerComponent.class);
                boolean newSignal = calculateResultSignal(consumerSignalInNetworks.get(modifiedConsumer).values());
                if (newSignal != consumerComponent.hasSignal) {
                    consumerComponent.hasSignal = newSignal;
                    blockEntity.saveComponent(consumerComponent);
                }
            }

        }
    }

    private Boolean calculateResultSignal(Collection<Boolean> values) {
        for (Boolean value : values) {
            if (value)
                return true;
        }

        return false;
    }

    private boolean getConsumerSignalInNetwork(Network network, Vector3i location) {
        // Check for infinite signal strength (-1), if there - it powers whole network
        final Collection<Vector3i> producers = producersInNetwork.get(network);
        for (Vector3i producer : producers) {
            final int signalStrength = producerSignalStrengths.get(producer);
            if (signalStrength==-1)
                return true;
        }

        for (Vector3i producer : producers) {
            final int signalStrength = producerSignalStrengths.get(producer);
            // TODO check if consumer block is within the specified signal strength
        }

        return false;
    }

    @Override
    public void networkAdded(Network newNetwork) {
        modifiedNetworksSinceLastUpdate.add(newNetwork);
    }

    @Override
    public void networkUpdated(Network network) {
        modifiedNetworksSinceLastUpdate.add(network);
    }

    @Override
    public void networkRemoved(Network network) {
        modifiedNetworksSinceLastUpdate.add(network);
        producersInNetwork.removeAll(network);
    }

    @Override
    public void networkSplit(Network sourceNetwork, Collection<? extends Network> resultNetwork) {
        modifiedNetworksSinceLastUpdate.add(sourceNetwork);
        producersInNetwork.removeAll(sourceNetwork);
        modifiedNetworksSinceLastUpdate.addAll(resultNetwork);
    }

    @Override
    public void networksMerged(Network mainNetwork, Network mergedNetwork) {
        modifiedNetworksSinceLastUpdate.add(mainNetwork);
        modifiedNetworksSinceLastUpdate.add(mergedNetwork);
        producersInNetwork.removeAll(mergedNetwork);
    }

    /**
     * ****************************** Conductor events ********************************
     */

    @ReceiveEvent(components = {BlockComponent.class, LocationComponent.class, SignalConductorComponent.class})
    public void conductorAdded(AddComponentEvent event, EntityRef block) {
        final LocationComponent location = block.getComponent(LocationComponent.class);
        byte connectingOnSides = block.getComponent(SignalConductorComponent.class).connectionSides;

        signalNetwork.addNetworkingBlock(new Vector3i(location.getLocalPosition()), connectingOnSides);
    }

    @ReceiveEvent(components = {SignalConductorComponent.class})
    public void conductorUpdated(ChangedComponentEvent event, EntityRef block) {
        if (block.hasComponent(BlockComponent.class) && block.hasComponent(LocationComponent.class)) {
            final LocationComponent location = block.getComponent(LocationComponent.class);
            byte connectingOnSides = block.getComponent(SignalConductorComponent.class).connectionSides;

            signalNetwork.updateNetworkingBlock(new Vector3i(location.getLocalPosition()), connectingOnSides);
        }
    }

    @ReceiveEvent(components = {BlockComponent.class, LocationComponent.class, SignalConductorComponent.class})
    public void conductorRemoved(RemovedComponentEvent event, EntityRef block) {
        final LocationComponent location = block.getComponent(LocationComponent.class);

        signalNetwork.removeNetworkingBlock(new Vector3i(location.getLocalPosition()));
    }

    /**
     * ****************************** Producer events ********************************
     */

    @ReceiveEvent(components = {BlockComponent.class, LocationComponent.class, SignalProducerComponent.class})
    public void producerAdded(AddComponentEvent event, EntityRef block) {
        Vector3i location = new Vector3i(block.getComponent(LocationComponent.class).getWorldPosition());
        final SignalProducerComponent producerComponent = block.getComponent(SignalProducerComponent.class);
        byte connectingOnSides = producerComponent.connectionSides;

        signalProducers.put(location, connectingOnSides);
        producerSignalStrengths.put(location, producerComponent.signalStrength);
        signalNetwork.addLeafBlock(location, connectingOnSides);
    }

    @ReceiveEvent(components = {SignalProducerComponent.class})
    public void producerUpdated(ChangedComponentEvent event, EntityRef block) {
        if (block.hasComponent(BlockComponent.class) && block.hasComponent(LocationComponent.class)) {
            Vector3i location = new Vector3i(block.getComponent(LocationComponent.class).getWorldPosition());
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
                modifiedProducerSignalsSinceLastUpdate.add(location);
            }
        }
    }

    @ReceiveEvent(components = {BlockComponent.class, LocationComponent.class, SignalProducerComponent.class})
    public void producerRemoved(RemovedComponentEvent event, EntityRef block) {
        Vector3i location = new Vector3i(block.getComponent(LocationComponent.class).getWorldPosition());
        byte connectingOnSides = block.getComponent(SignalProducerComponent.class).connectionSides;

        signalProducers.remove(location);
        final Collection<Network> networks = producerNetworks.removeAll(location);
        for (Network network : networks)
            producersInNetwork.remove(network, location);

        producerSignalStrengths.remove(location);
        signalNetwork.removeLeafBlock(location, connectingOnSides);
    }

    /**
     * ****************************** Consumer events ********************************
     */

    @ReceiveEvent(components = {BlockComponent.class, LocationComponent.class, SignalConsumerComponent.class})
    public void consumerAdded(AddComponentEvent event, EntityRef block) {
        Vector3i location = new Vector3i(block.getComponent(LocationComponent.class).getWorldPosition());
        byte connectingOnSides = block.getComponent(SignalConsumerComponent.class).connectionSides;

        signalConsumers.put(location, connectingOnSides);
        consumerSignalInNetworks.put(location, Maps.<Network, Boolean>newHashMap());
        signalNetwork.addLeafBlock(location, connectingOnSides);
    }

    @ReceiveEvent(components = {SignalConsumerComponent.class})
    public void consumerUpdated(ChangedComponentEvent event, EntityRef block) {
        if (block.hasComponent(BlockComponent.class) && block.hasComponent(LocationComponent.class)) {
            Vector3i location = new Vector3i(block.getComponent(LocationComponent.class).getWorldPosition());
            final SignalConsumerComponent consumerComponent = block.getComponent(SignalConsumerComponent.class);

            // We need to figure out, what exactly was changed
            final Byte oldConnectionSides = signalConsumers.get(location);
            byte newConnectionSides = consumerComponent.connectionSides;
            if (oldConnectionSides != newConnectionSides) {
                signalConsumers.put(location, newConnectionSides);
                signalNetwork.updateLeafBlock(location, oldConnectionSides, newConnectionSides);
            }
        }
    }

    @ReceiveEvent(components = {BlockComponent.class, LocationComponent.class, SignalConsumerComponent.class})
    public void consumerRemoved(RemovedComponentEvent event, EntityRef block) {
        Vector3i location = new Vector3i(block.getComponent(LocationComponent.class).getWorldPosition());
        byte connectingOnSides = block.getComponent(SignalConsumerComponent.class).connectionSides;

        signalConsumers.remove(location);
        consumerSignalInNetworks.put(location, Maps.<Network, Boolean>newHashMap());
        signalNetwork.removeLeafBlock(location, connectingOnSides);
    }
}
