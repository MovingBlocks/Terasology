package org.terasology.signalling.componentSystem;

import com.google.common.collect.Sets;
import org.terasology.blockNetwork.BlockNetwork;
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

import java.util.Set;

@RegisterComponentSystem
public class SignalSystem implements EventHandlerSystem, UpdateSubscriberSystem {
    private BlockNetwork signalNetwork;
    private Set<Vector3i> signalProducers;
    private Set<Vector3i> signalConsumers;

    @Override
    public void initialise() {
        signalNetwork = new BlockNetwork();
        signalProducers = Sets.newHashSet();
        signalConsumers = Sets.newHashSet();
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

    @ReceiveEvent(components = {BlockComponent.class, LocationComponent.class, SignalConductorComponent.class})
    public void conductorUpdated(ChangedComponentEvent event, EntityRef block) {
        final LocationComponent location = block.getComponent(LocationComponent.class);
        byte connectingOnSides = block.getComponent(SignalConductorComponent.class).connectingOnSides;

        signalNetwork.updateNetworkingBlock(new Vector3i(location.getLocalPosition()), connectingOnSides);
    }

    @ReceiveEvent(components = {BlockComponent.class, LocationComponent.class, SignalConductorComponent.class})
    public void conductorRemoved(RemovedComponentEvent event, EntityRef block) {
        final LocationComponent location = block.getComponent(LocationComponent.class);

        signalNetwork.removeNetworkingBlock(new Vector3i(location.getLocalPosition()));
    }

    @ReceiveEvent(components = {BlockComponent.class, LocationComponent.class, SignalProducerComponent.class})
    public void producerAdded(AddComponentEvent event, EntityRef block) {
        final LocationComponent location = block.getComponent(LocationComponent.class);
        byte connectingOnSides = block.getComponent(SignalConductorComponent.class).connectingOnSides;

        signalNetwork.addLeafBlock(new Vector3i(location.getLocalPosition()), connectingOnSides);
    }

    @ReceiveEvent(components = {BlockComponent.class, LocationComponent.class, SignalProducerComponent.class})
    public void producerUpdated(ChangedComponentEvent event, EntityRef block) {
        final LocationComponent location = block.getComponent(LocationComponent.class);
        byte connectingOnSides = block.getComponent(SignalConductorComponent.class).connectingOnSides;

        signalNetwork.updateLeafBlock(new Vector3i(location.getLocalPosition()), connectingOnSides);
    }

    @ReceiveEvent(components = {BlockComponent.class, LocationComponent.class, SignalProducerComponent.class})
    public void producerRemoved(RemovedComponentEvent event, EntityRef block) {
        final LocationComponent location = block.getComponent(LocationComponent.class);
        byte connectingOnSides = block.getComponent(SignalConductorComponent.class).connectingOnSides;

        signalNetwork.removeLeafBlock(new Vector3i(location.getLocalPosition()));
    }

    @ReceiveEvent(components = {BlockComponent.class, LocationComponent.class, SignalConsumerComponent.class})
    public void consumerAdded(AddComponentEvent event, EntityRef block) {
        final LocationComponent location = block.getComponent(LocationComponent.class);
        byte connectingOnSides = block.getComponent(SignalConductorComponent.class).connectingOnSides;

        signalNetwork.addLeafBlock(new Vector3i(location.getLocalPosition()), connectingOnSides);
    }

    @ReceiveEvent(components = {BlockComponent.class, LocationComponent.class, SignalConsumerComponent.class})
    public void consumerUpdated(ChangedComponentEvent event, EntityRef block) {
        final LocationComponent location = block.getComponent(LocationComponent.class);
        byte connectingOnSides = block.getComponent(SignalConductorComponent.class).connectingOnSides;

        signalNetwork.updateLeafBlock(new Vector3i(location.getLocalPosition()), connectingOnSides);
    }

    @ReceiveEvent(components = {BlockComponent.class, LocationComponent.class, SignalConsumerComponent.class})
    public void consumerRemoved(RemovedComponentEvent event, EntityRef block) {
        final LocationComponent location = block.getComponent(LocationComponent.class);
        byte connectingOnSides = block.getComponent(SignalConductorComponent.class).connectingOnSides;

        signalNetwork.removeLeafBlock(new Vector3i(location.getLocalPosition()));
    }
}
