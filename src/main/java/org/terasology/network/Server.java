package org.terasology.network;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Event;
import org.terasology.entitySystem.PersistableEntityManager;
import org.terasology.entitySystem.persistence.EntitySerializer;
import org.terasology.entitySystem.persistence.EventSerializer;
import org.terasology.math.Vector3i;
import org.terasology.network.serialization.ClientComponentFieldCheck;
import org.terasology.network.serialization.ServerComponentFieldCheck;
import org.terasology.protobuf.EntityData;
import org.terasology.protobuf.NetData;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.remoteChunkProvider.RemoteChunkProvider;

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
    private BlockingQueue<NetData.NetMessage> queuedMessages = Queues.newLinkedBlockingQueue();
    private BlockingQueue<NetData.EventMessage> queuedEvents = Queues.newLinkedBlockingQueue();
    private NetData.ServerInfoMessage serverInfo;

    private PersistableEntityManager entityManager;
    private EntitySerializer entitySerializer;
    private EventSerializer eventSerializer;

    private RemoteChunkProvider remoteWorldProvider;
    private BlockingQueue<Chunk> chunkQueue = Queues.newLinkedBlockingQueue();
    private TIntSet netDirty = new TIntHashSet();

    private EntityRef clientEntity = EntityRef.NULL;

    public Server(NetworkSystem system, Channel channel) {
        this.channel = channel;
        this.networkSystem = system;
    }

    void connectToEntitySystem(PersistableEntityManager entityManager, EntitySerializer entitySerializer, EventSerializer eventSerializer) {
        this.entityManager = entityManager;
        this.eventSerializer = eventSerializer;
        this.entitySerializer = entitySerializer;
    }

    void setServerInfo(NetData.ServerInfoMessage serverInfo) {
        this.serverInfo = serverInfo;
        clientEntity = new NetEntityRef(serverInfo.getClientId(), networkSystem);
    }

    public NetData.ServerInfoMessage getInfo() {
        return serverInfo;
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
        if (remoteWorldProvider != null) {
            List<Chunk> chunks = Lists.newArrayListWithExpectedSize(chunkQueue.size());
            chunkQueue.drainTo(chunks);
            for (Chunk chunk : chunks) {
                remoteWorldProvider.receiveChunk(chunk);
            }
        }
        if (entityManager != null) {
            processEntities();
            processMessages();
            processEvents();
        }
    }

    private void send(NetData.NetMessage data) {
        channel.write(data);
    }

    private void processEntities() {
        TIntIterator dirtyIterator = netDirty.iterator();
        while (dirtyIterator.hasNext()) {
            int netId = dirtyIterator.next();
            EntityRef entity = networkSystem.getEntity(netId);
            if (isOwned(entity)) {
                EntityData.Entity entityData = entitySerializer.serialize(entity, false, new ClientComponentFieldCheck());
                NetData.NetMessage message = NetData.NetMessage.newBuilder()
                        .setType(NetData.NetMessage.Type.UPDATE_ENTITY)
                        .setUpdateEntity(NetData.UpdateEntityMessage.newBuilder().setEntity(entityData).setNetId(netId))
                        .build();
                send(message);
            }
        }
        netDirty.clear();
    }

    private boolean isOwned(EntityRef entity) {
        if (entity.equals(clientEntity)) {
            return true;
        }
        // TODO: Recursive
        NetworkComponent netComp = entity.getComponent(NetworkComponent.class);
        if (netComp != null) {
            return netComp.owner.equals(clientEntity);
        }
        return false;
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


    public void invalidateChunks(Vector3i pos) {
        // TODO: Queue this, and enact on main thread
        remoteWorldProvider.invalidateChunks(pos);
    }

    public void receiveChunk(Chunk chunk) {
        chunkQueue.offer(chunk);
    }

    void setRemoteWorldProvider(RemoteChunkProvider remoteWorldProvider) {
        this.remoteWorldProvider = remoteWorldProvider;
    }

    private void processMessages() {
        List<NetData.NetMessage> messages = Lists.newArrayListWithExpectedSize(queuedMessages.size());
        queuedMessages.drainTo(messages);

        for (NetData.NetMessage message : messages) {
            switch (message.getType()) {
                case CREATE_ENTITY:
                    EntityRef newEntity = entitySerializer.deserialize(message.getCreateEntity().getEntity());
                    logger.info("Received new entity: {} with net id {}", newEntity, newEntity.getComponent(NetworkComponent.class).networkId);
                    networkSystem.registerNetworkEntity(newEntity);
                    break;
                case UPDATE_ENTITY:
                    EntityRef currentEntity = networkSystem.getEntity(message.getUpdateEntity().getNetId());
                    if (currentEntity.exists()) {
                        entitySerializer.deserializeOnto(currentEntity, message.getUpdateEntity().getEntity());
                    }
                    break;
                case REMOVE_ENTITY:
                    int netId = message.getRemoveEntity().getNetId();
                    EntityRef entity = networkSystem.getEntity(netId);
                    networkSystem.unregisterNetworkEntity(entity);
                    entity.destroy();
                    break;
                case EVENT:
                    Event event = eventSerializer.deserialize(message.getEvent().getEvent());
                    EntityRef target = networkSystem.getEntity(message.getEvent().getTargetId());
                    target.send(event);
                    break;
            }
        }

    }

    public void queueMessage(NetData.NetMessage message) {
        queuedMessages.offer(message);
    }

    public void setNetDirty(int netId) {
        netDirty.add(netId);
    }
}


