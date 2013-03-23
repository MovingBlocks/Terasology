package org.terasology.network;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import org.terasology.entitySystem.persistence.PackedEntitySerializer;
import org.terasology.game.CoreRegistry;
import org.terasology.game.Timer;
import org.terasology.math.Vector3i;
import org.terasology.network.serialization.ClientComponentFieldCheck;
import org.terasology.protobuf.ChunksProtobuf;
import org.terasology.protobuf.EntityData;
import org.terasology.protobuf.NetData;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.entity.BlockComponent;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.block.management.BlockManagerClient;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.Chunks;
import org.terasology.world.chunks.remoteChunkProvider.RemoteChunkProvider;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * Used to interact with a remote server (from client end)
 *
 * @author Immortius
 */
public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private NetworkSystemImpl networkSystem;
    private Channel channel;
    private NetMetricSource metricsSource;
    private BlockingQueue<NetData.NetMessage> queuedMessages = Queues.newLinkedBlockingQueue();
    private BlockingQueue<NetData.EventMessage> queuedReceivedEvents = Queues.newLinkedBlockingQueue();
    private List<NetData.EventMessage> queuedOutgoingEvents = Lists.newArrayList();
    private NetData.ServerInfoMessage serverInfo;

    private PersistableEntityManager entityManager;
    private PackedEntitySerializer entitySerializer;
    private EventSerializer eventSerializer;
    private BlockManagerClient blockManagerClient;

    private BlockEntityRegistry blockEntityRegistry;
    private RemoteChunkProvider remoteWorldProvider;
    private BlockingQueue<Chunk> chunkQueue = Queues.newLinkedBlockingQueue();
    private TIntSet netDirty = new TIntHashSet();
    private SetMultimap<Integer, Class<? extends Component>> changedComponents = HashMultimap.create();

    private EntityRef clientEntity = EntityRef.NULL;

    private Timer timer;

    public Server(NetworkSystemImpl system, Channel channel) {
        this.channel = channel;
        metricsSource = (NetMetricSource) channel.getPipeline().get(MetricRecordingHandler.NAME);
        this.networkSystem = system;
        this.timer = CoreRegistry.get(Timer.class);
    }

    void connectToEntitySystem(PersistableEntityManager entityManager, PackedEntitySerializer entitySerializer, EventSerializer eventSerializer, BlockEntityRegistry blockEntityRegistry) {
        this.entityManager = entityManager;
        this.eventSerializer = eventSerializer;
        this.entitySerializer = entitySerializer;
        this.blockEntityRegistry = blockEntityRegistry;
        blockManagerClient = (BlockManagerClient) CoreRegistry.get(BlockManager.class);
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

    public void send(Event event, EntityRef target) {
        NetworkComponent netComp = target.getComponent(NetworkComponent.class);
        if (netComp != null) {
            queuedOutgoingEvents.add(NetData.EventMessage.newBuilder()
                    .setEvent(eventSerializer.serialize(event))
                    .setTargetId(netComp.getNetworkId()).build());
        }
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
                sendEvents(message);
                send(message.build());
            }

            processMessages();
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


    private void processEvent(NetData.EventMessage message) {
        Event event = eventSerializer.deserialize(message.getEvent());
        EntityRef target = EntityRef.NULL;
        if (message.hasTargetBlockPos()) {
            target = blockEntityRegistry.getOrCreateBlockEntityAt(NetworkUtil.convert(message.getTargetBlockPos()));
        } else if (message.hasTargetId()) {
            target = networkSystem.getEntity(message.getTargetId());
        }
        if (target.exists()) {
            target.send(event);
        } else {
            queuedReceivedEvents.offer(message);
        }
    }

    void queueEvent(NetData.EventMessage message) {
        queuedReceivedEvents.offer(message);
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
            processBlockRegistrations(message);
            processReceivedChunks(message);
            processInvalidatedChunks(message);
            processBlockChanges(message);
            processRemoveEntities(message);
            for (NetData.CreateEntityMessage createEntity : message.getCreateEntityList()) {
                createEntityMessage(createEntity);
            }
            for (NetData.UpdateEntityMessage updateEntity : message.getUpdateEntityList()) {
                updateEntity(updateEntity);
            }
            for (NetData.EventMessage event : message.getEventList()) {
                processEvent(event);
            }
        }
    }

    private void processRemoveEntities(NetData.NetMessage message) {
        for (NetData.RemoveEntityMessage removeEntity : message.getRemoveEntityList()) {
            int netId = removeEntity.getNetId();
            EntityRef entity = networkSystem.getEntity(netId);
            if (entity.exists()) {
                networkSystem.unregisterNetworkEntity(entity);
                logger.info("Destroying entity: {}", entity);
                entity.destroy();
            }
        }
    }

    private void processBlockChanges(NetData.NetMessage message) {
        for (NetData.BlockChangeMessage blockChange : message.getBlockChangeList()) {
            BlockManager blockManager = CoreRegistry.get(BlockManager.class);
            logger.debug("Received block change to {}", blockManager.getBlock((byte) blockChange.getNewBlock()));
            // TODO: Store changes to blocks that aren't ready to be modified (the surrounding chunks aren't available)
            WorldProvider worldProvider = CoreRegistry.get(WorldProvider.class);
            Vector3i pos = NetworkUtil.convert(blockChange.getPos());
            Block oldBlock = worldProvider.getBlock(pos);
            Block newBlock = blockManager.getBlock((byte) blockChange.getNewBlock());
            if (!worldProvider.setBlock(pos, newBlock, oldBlock)) {
                logger.error("Failed to enact block update from server - {} to {}", pos, newBlock);
            }
        }
    }

    private void processInvalidatedChunks(NetData.NetMessage message) {
        for (NetData.InvalidateChunkMessage chunk : message.getInvalidateChunkList()) {
            remoteWorldProvider.invalidateChunks(NetworkUtil.convert(chunk.getPos()));
        }
    }

    private void processReceivedChunks(NetData.NetMessage message) {
        for (ChunksProtobuf.Chunk chunkInfo : message.getChunkInfoList()) {
            logger.debug("Received chunk {}, {}, {}", chunkInfo.getX(), chunkInfo.getY(), chunkInfo.getZ());
            Chunk chunk = Chunks.getInstance().decode(chunkInfo);
            chunkQueue.offer(chunk);
        }
    }

    private void processBlockRegistrations(NetData.NetMessage message) {
        for (NetData.BlockFamilyRegisteredMessage blockFamily : message.getBlockFamilyRegisteredList()) {
            if (blockFamily.getBlockIdCount() != blockFamily.getBlockUriCount()) {
                logger.error("Received block registration with mismatched id<->uri mapping");
            } else if (blockFamily.getBlockUriCount() == 0) {
                logger.error("Received empty block registration");
            } else {
                BlockUri family = new BlockUri(blockFamily.getBlockUri(0)).getFamilyUri();
                Map<String, Integer> registrationMap = Maps.newHashMap();
                for (int i = 0; i < blockFamily.getBlockIdCount(); ++i) {
                    registrationMap.put(blockFamily.getBlockUri(i), blockFamily.getBlockId(i));
                }
                blockManagerClient.receiveFamilyRegistration(family, registrationMap);
            }
        }
    }

    private void updateEntity(NetData.UpdateEntityMessage updateEntity) {
        EntityRef currentEntity = networkSystem.getEntity(updateEntity.getNetId());
        if (currentEntity.exists()) {
            if (currentEntity.getComponent(NetworkComponent.class) == null) {
                logger.error("Updating entity with no network component: {}, expected netId {}", currentEntity, updateEntity.getNetId());
            }
            if (currentEntity.getComponent(NetworkComponent.class).getNetworkId() != updateEntity.getNetId()) {
                logger.error("Network ID wrong before update");
            }
            boolean blockEntityBefore = currentEntity.hasComponent(BlockComponent.class);
            entitySerializer.deserializeOnto(currentEntity, updateEntity.getEntity());
            BlockComponent blockComponent = currentEntity.getComponent(BlockComponent.class);
            if (blockComponent != null && !blockEntityBefore) {
                if (!blockEntityRegistry.getBlockEntityAt(blockComponent.getPosition()).equals(currentEntity)) {
                    logger.error("Failed to associated new block entity");
                }
            }
            if (currentEntity.getComponent(NetworkComponent.class).getNetworkId() != updateEntity.getNetId()) {
                logger.error("Network ID lost in update: {}, {} -> {}", currentEntity, updateEntity.getNetId(), currentEntity.getComponent(NetworkComponent.class).getNetworkId());
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
        if (newEntity == null) {
            logger.error("Received entity is null");
        } else if (newEntity.getComponent(NetworkComponent.class) == null) {
            logger.error("Received entity with no NetworkComponent: {}", newEntity);
        } else if (newEntity.getComponent(NetworkComponent.class).getNetworkId() == 0) {
            logger.error("Received entity with null network id: {}", newEntity);
        }
        logger.info("Received new entity: {} with net id {}", newEntity, newEntity.getComponent(NetworkComponent.class).getNetworkId());
        networkSystem.registerNetworkEntity(newEntity);
    }

    public void queueMessage(NetData.NetMessage message) {
        queuedMessages.offer(message);
    }

    public void setComponentDirty(int netId, Class<? extends Component> componentType) {
        netDirty.add(netId);
        changedComponents.put(netId, componentType);
    }

    public NetMetricSource getMetrics() {
        return metricsSource;
    }
}


