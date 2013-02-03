package org.terasology.componentSystem.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.ComponentSystem;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterSystem;
import org.terasology.events.messaging.ChatMessageEvent;
import org.terasology.events.messaging.SendChatMessage;
import org.terasology.logic.console.MessageEvent;
import org.terasology.logic.manager.MessageManager;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkSystem;

/**
 * @author Immortius
 */
@RegisterSystem
public class MessagingSystem implements ComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(MessagingSystem.class);

    @In
    private NetworkSystem networkSystem;

    @In
    private EntityManager entityManager;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onReceiveMessage(SendChatMessage event, EntityRef entity) {
        if (networkSystem.getMode().isAuthority()) {
            logger.info("Received message from {} : '{}'", entity, event.getMessage());
            for (EntityRef client : entityManager.iteratorEntities(ClientComponent.class)) {
                client.send(new ChatMessageEvent(event.getMessage(), entity.getComponent(ClientComponent.class).clientInfo));
            }
        }

    }

    @ReceiveEvent(components = ClientComponent.class)
    public void onChatMessage(MessageEvent event, EntityRef entity) {
        ClientComponent client = entity.getComponent(ClientComponent.class);
        if (client.local) {
            logger.info("Message Received : '{}'", event.getFormattedMessage());
            MessageManager.getInstance().addMessage(event.getFormattedMessage());
        }
    }
}
