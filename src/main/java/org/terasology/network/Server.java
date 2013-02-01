package org.terasology.network;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.SetMultimap;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Event;
import org.terasology.entitySystem.PersistableEntityManager;
import org.terasology.entitySystem.persistence.EventSerializer;
import org.terasology.entitySystem.persistence.FieldSerializeCheck;
import org.terasology.entitySystem.persistence.PackedEntitySerializer;
import org.terasology.game.CoreRegistry;
import org.terasology.game.Timer;
import org.terasology.math.Vector3i;
import org.terasology.network.serialization.ClientComponentFieldCheck;
import org.terasology.protobuf.EntityData;
import org.terasology.protobuf.NetData;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.remoteChunkProvider.RemoteChunkProvider;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

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
    private BlockingQueue<NetData.EventMessage> queuedReceivedEvents = Queues.newLinkedBlockingQueue();
    private List<NetData.EventMessage> queuedOutgoingEvents = Lists.newArrayList();
    private NetData.ServerInfoMessage serverInfo;

    private PersistableEntityManager entityManager;
    private PackedEntitySerializer entitySerializer;
    private EventSerializer eventSerializer;

    private BlockEntityRegistry blockEntityRegistry;
    private RemoteChunkProvider remoteWorldProvider;
    private BlockingQueue<Chunk> chunkQueue = Queues.newLinkedBlockingQueue();
    private TIntSet netDirty = new TIntHashSet();
    private SetMultimap<Integer, Class<? extends Component>> changedComponents = HashMultimap.create();

    private EntityRef clientEntity = EntityRef.NULL;

    private AtomicInteger receivedMessages = new AtomicInteger();
    private AtomicInteger receivedBytes = new AtomicInteger();
    private AtomicInteger sentMessages = new AtomicInteger();
    private AtomicInteger sentBytes = new AtomicInteger();

    private Timer timer;

    public Server(NetworkSystem system, Channel channel) {
        this.channel = channel;
        this.networkSystem = system;
        this.timer = CoreRegistry.get(Timer.class);
    }

    void connectToEntitySystem(PersistableEntityManager entityManager, PackedEntitySerializer entitySerializer, EventSerializer eventSerializer, BlockEntityRegistry blockEntityRegistry) {
        this.entityManager = entityManager;
        this.eventSerializer = eventSerializer;
        this.entitySerializer = entitySerializer;
        this.blockEntityRegistry = blockEntityRegistry;
    }

    void setServerInfo(NetData.ServerInfoMessage serverInfo) {
        this.serverInfo = serverInfo;
        clientEntity = new NetEntityRef(serverInfo.getClientId(), networkSystem);
    }

    public EntityRef getEntity() {
        return clientEntity;
    }

    public NetData.ServerInfoMessage getInfo() {
        return serverInfo;
    }

    public void send(Event event, int targetId) {
        queuedOutgoingEvents.add(NetData.EventMessage.newBuilder()
                .setEvent(eventSerializer.serialize(event, FieldSerializeCheck.NullCheck.<Event>newInstance()))
                .setTargetId(targetId).build());
    }

    public void update(boolean netTick) {
        processReceivedChunks();
        if (entityManager != null) {
            if (netTick) {
                NetData.NetMessage.Builder message = NetData.NetMessage.newBuilder();
                message.setTime(timer.getTimeInMs());
                sendEntities(message);
                sendEvents(message);
                send(message.build());
            } else if (!queuedOutgoingEvents.isEmpty()) {
                NetData.NetMessage.Builder message = NetData.NetMessage.newBuilder();
                message.setTime(timer.getTimeInMs());
                sendEntities(message);
                send(message.build());
            }

            processMessages();
            processEvents();
        }
    }

    private void sendEvents(NetData.NetMessage.Builder message) {
        for (NetData.EventMessage event : queuedOutgoingEvents) {
            message.addEvent(event);
        }
        queuedOutgoingEvents.clear();
    }

    private void processReceivedChunks() {
        if (remoteWorldProvider != null) {
            List<Chunk> chunks = Lists.newArrayListWithExpectedSize(chunkQueue.size());
            chunkQueue.drainTo(chunks);
            for (Chunk chunk : chunks) {
                remoteWorldProvider.receiveChunk(chunk);
            }
        }
    }

    private void send(NetData.NetMessage data) {
        logger.trace("Sending with size {}", data.getSerializedSize());
        sentMessages.incrementAndGet();
        sentBytes.addAndGet(data.getSerializedSize());
        channel.write(data);
    }

    private void sendEntities(NetData.NetMessage.Builder message) {
        TIntIterator dirtyIterator = netDirty.iterator();
        while (dirtyIterator.hasNext()) {
            int netId = dirtyIterator.next();
            EntityRef entity = networkSystem.getEntity(netId);
            if (isOwned(entity)) {
                EntityData.PackedEntity entityData = entitySerializer.serialize(entity, Collections.EMPTY_SET, changedComponents.get(netId), Collections.EMPTY_SET, new ClientComponentFieldCheck());
                if (entityData != null) {
                    message.addUpdateEntity(NetData.UpdateEntityMessage.newBuilder().setEntity(entityData).setNetId(netId));
                }
            }
        }
        netDirty.clear();
    }

    private boolean isOwned(EntityRef entity) {
        EntityRef owner = networkSystem.getOwnerEntity(entity);
        return clientEntity.equals(owner);
    }


    private void processEvents() {
        List<NetData.EventMessage> messages = Lists.newArrayListWithExpectedSize(queuedReceivedEvents.size());
        queuedReceivedEvents.drainTo(messages);

        for (NetData.EventMessage message : messages) {
            Event event = eventSerializer.deserialize(message.getEvent());
            EntityRef target = networkSystem.getEntity(message.getTargetId());
            if (target.exists()) {
                target.send(event);
            } else {
                queuedReceivedEvents.offer(message);
            }
        }
    }

    void queueEvent(NetData.EventMessage message) {
        queuedReceivedEvents.offer(message);
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
            if (message.hasTime()) {
                timer.updateServerTime(message.getTime(), false);
            }
            for (NetData.RemoveEntityMessage removeEntity : message.getRemoveEntityList()) {
                int netId = removeEntity.getNetId();
                EntityRef entity = networkSystem.getEntity(netId);
                networkSystem.unregisterNetworkEntity(entity);
                entity.destroy();
            }
            for (NetData.CreateEntityMessage createEntity : message.getCreateEntityList()) {
                createEntityMessage(createEntity);
            }
            for (NetData.UpdateEntityMessage updateEntity : message.getUpdateEntityList()) {
                EntityRef currentEntity = networkSystem.getEntity(updateEntity.getNetId());
                if (currentEntity.exists()) {
                    logger.info("Updating entity: {}", currentEntity);
                    entitySerializer.deserializeOnto(currentEntity, updateEntity.getEntity());
                }
            }

        }

    }

    private void createEntityMessage(NetData.CreateEntityMessage message) {
        EntityRef newEntity;
        if (message.hasBlockPos()) {
            newEntity = blockEntityRegistry.getOrCreateBlockEntityAt(NetworkUtil.convert(message.getBlockPos()));
            entitySerializer.deserializeOnto(newEntity, message.getEntity());
        } else {
            newEntity = entitySerializer.deserialize(message.getEntity());
        }
        logger.info("Received new entity: {} with net id {}", newEntity, newEntity.getComponent(NetworkComponent.class).networkId);
        networkSystem.registerNetworkEntity(newEntity);
    }

    public void queueMessage(NetData.NetMessage message) {
        queuedMessages.offer(message);
    }

    public void setComponentDirty(int netId, Class<? extends Component> componentType) {
        netDirty.add(netId);
        changedComponents.put(netId, componentType);
    }

    public void receivedMessageWithSize(int serializedSize) {
        receivedBytes.addAndGet(serializedSize);
        receivedMessages.incrementAndGet();
    }

    public int getReceivedMessagesSinceLastCall() {
        return receivedMessages.getAndSet(0);
    }

    public int getReceivedBytesSinceLastCall() {
        return receivedBytes.getAndSet(0);
    }

    public int getSentMessagesSinceLastCall() {
        return sentMessages.getAndSet(0);
    }

    public int getSentBytesSinceLastCall() {
        return sentBytes.getAndSet(0);
    }
}


