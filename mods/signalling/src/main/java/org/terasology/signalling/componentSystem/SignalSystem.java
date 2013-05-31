package org.terasology.signalling.componentSystem;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.terasology.blockNetwork.BlockNetwork;
import org.terasology.blockNetwork.Network;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.ItemComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.entitySystem.event.AddComponentEvent;
import org.terasology.entitySystem.event.ChangedComponentEvent;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.logic.door.DoorComponent;
import org.terasology.math.Vector3i;
import org.terasology.signalling.components.SignalConductorComponent;
import org.terasology.signalling.components.SignalConsumerComponent;
import org.terasology.signalling.components.SignalProducerComponent;
import org.terasology.world.block.BlockComponent;

import java.util.Map;
import java.util.Set;

@RegisterComponentSystem
public class SignalSystem implements EventHandlerSystem, UpdateSubscriberSystem {
    private BlockNetwork signalNetwork;

    private Map<Vector3i, Byte> signalProducers;
    private Map<Vector3i, Byte> signalConsumers;

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
    }

    @ReceiveEvent(components = {BlockComponent.class, LocationComponent.class, SignalConductorComponent.class})
    public void conductorAdded(AddComponentEvent event, EntityRef block) {
        final LocationComponent location = block.getComponent(LocationComponent.class);
        byte connectingOnSides = block.getComponent(SignalConductorComponent.class).connectingOnSides;

        signalNetwork.addNetworkingBlock(new Vector3i(location.getLocalPosition()), connectingOnSides);
    }

    @ReceiveEvent(components = {SignalConductorComponent.class})
    public void conductorUpdated(ChangedComponentEvent event, EntityRef block) {
        if (block.hasComponent(BlockComponent.class) && block.hasComponent(LocationComponent.class)) {
            final LocationComponent location = block.getComponent(LocationComponent.class);
            byte connectingOnSides = block.getComponent(SignalConductorComponent.class).connectingOnSides;

            signalNetwork.updateNetworkingBlock(new Vector3i(location.getLocalPosition()), connectingOnSides);
        }
    }

    @ReceiveEvent(components = {BlockComponent.class, LocationComponent.class, SignalConductorComponent.class})
    public void conductorRemoved(RemovedComponentEvent event, EntityRef block) {
        final LocationComponent location = block.getComponent(LocationComponent.class);

        signalNetwork.removeNetworkingBlock(new Vector3i(location.getLocalPosition()));
    }

    @ReceiveEvent(components = {BlockComponent.class, LocationComponent.class, SignalProducerComponent.class})
    public void producerAdded(AddComponentEvent event, EntityRef block) {
        Vector3i location = new Vector3i(block.getComponent(LocationComponent.class).getWorldPosition());
        byte connectingOnSides = block.getComponent(SignalConductorComponent.class).connectingOnSides;

        signalProducers.put(location, connectingOnSides);
        signalNetwork.addLeafBlock(location, connectingOnSides);
    }

    @ReceiveEvent(components = {SignalProducerComponent.class})
    public void producerUpdated(ChangedComponentEvent event, EntityRef block) {
        if (block.hasComponent(BlockComponent.class) && block.hasComponent(LocationComponent.class)) {
            Vector3i location = new Vector3i(block.getComponent(LocationComponent.class).getWorldPosition());
            byte connectingOnSides = block.getComponent(SignalConductorComponent.class).connectingOnSides;

            final Byte oldConnectingOnSides = signalProducers.get(location);
            signalProducers.put(location, connectingOnSides);
            signalNetwork.updateLeafBlock(location, oldConnectingOnSides, connectingOnSides);
        }
    }

    @ReceiveEvent(components = {BlockComponent.class, LocationComponent.class, SignalProducerComponent.class})
    public void producerRemoved(RemovedComponentEvent event, EntityRef block) {
        Vector3i location = new Vector3i(block.getComponent(LocationComponent.class).getWorldPosition());
        byte connectingOnSides = block.getComponent(SignalConductorComponent.class).connectingOnSides;

        signalProducers.remove(location);
        signalNetwork.removeLeafBlock(location, connectingOnSides);
    }

    @ReceiveEvent(components = {BlockComponent.class, LocationComponent.class, SignalConsumerComponent.class})
    public void consumerAdded(AddComponentEvent event, EntityRef block) {
        Vector3i location = new Vector3i(block.getComponent(LocationComponent.class).getWorldPosition());
        byte connectingOnSides = block.getComponent(SignalConductorComponent.class).connectingOnSides;

        signalConsumers.put(location, connectingOnSides);
        signalNetwork.addLeafBlock(location, connectingOnSides);
    }

    @ReceiveEvent(components = {BlockComponent.class, LocationComponent.class, SignalConsumerComponent.class})
    public void consumerRemoved(RemovedComponentEvent event, EntityRef block) {
        Vector3i location = new Vector3i(block.getComponent(LocationComponent.class).getWorldPosition());
        byte connectingOnSides = block.getComponent(SignalConductorComponent.class).connectingOnSides;

        signalConsumers.remove(location);
        signalNetwork.removeLeafBlock(location, connectingOnSides);
    }
}
