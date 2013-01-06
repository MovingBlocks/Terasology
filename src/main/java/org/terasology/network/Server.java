package org.terasology.network;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import gnu.trove.iterator.TIntIterator;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Event;
import org.terasology.entitySystem.PersistableEntityManager;
import org.terasology.entitySystem.metadata.extension.EntityRefTypeHandler;
import org.terasology.entitySystem.persistence.EventSerializer;
import org.terasology.network.serialization.NetworkComponentFieldCheck;
import org.terasology.protobuf.EntityData;
import org.terasology.protobuf.NetData;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Used to interact with a remote server (from client end)
 *
 * @author Immortius
 */
public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private NetworkSystem networkSystem;
    private Channel channel;
    private EventSerializer eventSerializer;
    private BlockingQueue<NetData.EventMessage> queuedEvents = Queues.newLinkedBlockingQueue();
    private PersistableEntityManager entityManager;

    public Server(NetworkSystem system, Channel channel) {
        this.channel = channel;
        this.networkSystem = system;
    }

    void setEntityManager(PersistableEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    void setEventSerializer(EventSerializer eventSerializer) {
        this.eventSerializer = eventSerializer;
    }

    public void send(Event event, int targetId) {
        NetData.NetMessage message = NetData.NetMessage.newBuilder()
                .setType(NetData.NetMessage.Type.EVENT)
                .setEvent(NetData.EventMessage.newBuilder()
                        .setEvent(eventSerializer.serialize(event))
                        .setTargetId(targetId))
                .build();
        channel.write(message);
    }

    public void update() {
        if (entityManager != null) {
            EntityRefTypeHandler.setNetworkMode(networkSystem);

            processEvents();

            EntityRefTypeHandler.setEntityManagerMode(entityManager);
        }
    }

    private void processEvents() {
        List<NetData.EventMessage> messages = Lists.newArrayListWithExpectedSize(queuedEvents.size());
        queuedEvents.drainTo(messages);

        for (NetData.EventMessage message : messages) {
            Event event = eventSerializer.deserialize(message.getEvent());
            logger.info("Received event {} for target {}", event, message.getTargetId());
            EntityRef target = networkSystem.getEntity(message.getTargetId());
            if (target.exists()) {
                target.send(event);
            } else {
                queuedEvents.offer(message);
            }
        }
    }

    void queueEvent(NetData.EventMessage message) {
        queuedEvents.offer(message);
    }

}
