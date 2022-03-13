// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network;

import org.terasology.engine.core.bootstrap.eventSystem.AbstractEventSystemDecorator;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.internal.EventSystem;
import org.terasology.engine.entitySystem.metadata.EventLibrary;
import org.terasology.engine.entitySystem.metadata.EventMetadata;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.entitysystem.event.Event;

/**
 * Decorator for replicate event to network.
 */
public class NetworkEventSystemDecorator extends AbstractEventSystemDecorator {
    private final NetworkSystem networkSystem;
    private final EventLibrary eventLibrary;

    public NetworkEventSystemDecorator(EventSystem eventSystem, NetworkSystem networkSystem,
                                       EventLibrary eventLibrary) {
        super(eventSystem);
        this.networkSystem = networkSystem;
        this.eventLibrary = eventLibrary;
    }

    @Override
    public void registerEvent(ResourceUrn uri, Class<? extends Event> eventType) {
        super.registerEvent(uri, eventType);
        if (shouldAddToLibrary(eventType)) {
            eventLibrary.register(uri, eventType);
        }
    }

    /**
     * Events are added to the event library if they have a network annotation
     *
     * @param eventType the type of the event to be checked
     * @return Whether the event should be added to the event library
     */
    private boolean shouldAddToLibrary(Class<? extends Event> eventType) {
        return eventType.getAnnotation(ServerEvent.class) != null
                || eventType.getAnnotation(OwnerEvent.class) != null
                || eventType.getAnnotation(BroadcastEvent.class) != null;
    }

    @Override
    public void send(EntityRef entity, Event event) {
        if (currentThreadIsMain()) {
            networkReplicate(entity, event);
        }
        super.send(entity, event);
    }

    private <T extends Event> void networkReplicate(EntityRef entity, T event) {
        EventMetadata<T> metadata = eventLibrary.getMetadata(event);
        if (metadata != null && metadata.isNetworkEvent()) {
            switch (metadata.getNetworkEventType()) {
                case BROADCAST:
                    broadcastEvent(entity, event, metadata);
                    break;
                case OWNER:
                    sendEventToOwner(entity, event);
                    break;
                case SERVER:
                    sendEventToServer(entity, event);
                    break;
                default:
                    break;
            }
        }
    }

    private void sendEventToServer(EntityRef entity, Event event) {
        if (networkSystem.getMode() == NetworkMode.CLIENT) {
            NetworkComponent netComp = entity.getComponent(NetworkComponent.class);
            if (netComp != null) {
                networkSystem.getServer().send(event, entity);
            }
        }
    }

    private void sendEventToOwner(EntityRef entity, Event event) {
        if (networkSystem.getMode().isServer()) {
            NetworkComponent netComp = entity.getComponent(NetworkComponent.class);
            if (netComp != null) {
                Client client = networkSystem.getOwner(entity);
                if (client != null) {
                    client.send(event, entity);
                }
            }
        }
    }

    private <T extends Event> void broadcastEvent(EntityRef entity, T event, EventMetadata<T> metadata) {
        if (networkSystem.getMode().isServer()) {
            NetworkComponent netComp = entity.getComponent(NetworkComponent.class);
            BlockComponent blockComp = entity.getComponent(BlockComponent.class);
            if (netComp != null || blockComp != null) {
                Client instigatorClient = null;
                if (metadata.isSkipInstigator() && event instanceof NetworkEvent) {
                    instigatorClient = networkSystem.getOwner(((NetworkEvent) event).getInstigator());
                }
                for (Client client : networkSystem.getPlayers()) {
                    if (!client.equals(instigatorClient)) {
                        client.send(event, entity);
                    }
                }
            }
        }
    }
}
