package org.terasology.network;

import org.terasology.entitySystem.ComponentSystem;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.event.AddComponentEvent;
import org.terasology.entitySystem.event.ChangedComponentEvent;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.network.events.ChangeViewRangeRequest;
import org.terasology.rendering.world.WorldRenderer;

/**
 * @author Immortius
 */
public class NetworkEntitySystem implements ComponentSystem {

    @In
    private EntityManager entityManager;

    @In
    private WorldRenderer worldRenderer;

    private NetworkSystemImpl networkSystem;

    public NetworkEntitySystem(NetworkSystemImpl networkSystem) {
        this.networkSystem = networkSystem;
    }

    @Override
    public void initialise() {
        for (EntityRef entity : entityManager.iteratorEntities(NetworkComponent.class)) {
            networkSystem.registerNetworkEntity(entity);
        }
    }

    @ReceiveEvent(components = NetworkComponent.class)
    public void onAddNetworkComponent(AddComponentEvent event, EntityRef entity) {
        if (networkSystem.getMode() == NetworkMode.SERVER) {
            networkSystem.registerNetworkEntity(entity);
        }
    }

    @ReceiveEvent(components = NetworkComponent.class)
    public void onNetworkComponentChanged(ChangedComponentEvent event, EntityRef entity) {
        networkSystem.updateNetworkEntity(entity);
    }

    @ReceiveEvent(components = NetworkComponent.class)
    public void onRemoveNetworkComponent(RemovedComponentEvent event, EntityRef entity) {
        networkSystem.unregisterNetworkEntity(entity);
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onChangeViewRequest(ChangeViewRangeRequest request, EntityRef entity) {
        if (networkSystem.getMode().isAuthority()) {
            NetClient netClient = networkSystem.getNetOwner(entity);
            if (netClient != null) {
                netClient.setViewDistanceMode(request.getNewViewRange());
                ClientComponent clientComp = netClient.getEntity().getComponent(ClientComponent.class);
                if (clientComp != null && clientComp.character.exists()) {
                    worldRenderer.getChunkProvider().updateRelevanceEntity(clientComp.character, netClient.getViewDistance());
                }
            }
        }
    }

    @Override
    public void shutdown() {

    }
}
