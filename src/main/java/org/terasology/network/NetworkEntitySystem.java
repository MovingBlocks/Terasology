package org.terasology.network;

import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.ComponentSystem;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.event.AddComponentEvent;
import org.terasology.entitySystem.event.ChangedComponentEvent;
import org.terasology.entitySystem.event.RemovedComponentEvent;

/**
 * @author Immortius
 */
public class NetworkEntitySystem implements ComponentSystem {

    @In
    private NetworkSystem networkSystem;
    @In
    private EntityManager entityManager;

    public NetworkEntitySystem() {
    }

    @Override
    public void initialise() {
        for (EntityRef entity : entityManager.iteratorEntities(NetworkComponent.class)) {
            networkSystem.registerNetworkEntity(entity);
        }
    }

    @ReceiveEvent(components = NetworkComponent.class)
    public void onAddNetworkComponent(AddComponentEvent event, EntityRef entity) {
        networkSystem.registerNetworkEntity(entity);
    }

    @ReceiveEvent(components = NetworkComponent.class)
    public void onNetworkComponentChanged(ChangedComponentEvent event, EntityRef entity) {
        networkSystem.updateNetworkEntity(entity);
    }

    @ReceiveEvent(components = NetworkComponent.class)
    public void onRemoveNetworkComponent(RemovedComponentEvent event, EntityRef entity) {
        networkSystem.unregisterNetworkEntity(entity);
    }

    @Override
    public void shutdown() {

    }
}
