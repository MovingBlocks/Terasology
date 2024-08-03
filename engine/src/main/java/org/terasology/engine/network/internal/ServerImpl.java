// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network.internal;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.SetMultimap;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import io.netty.channel.Channel;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.EngineTime;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.network.NetMetricSource;
import org.terasology.engine.network.NetworkComponent;
import org.terasology.engine.network.Server;
import org.terasology.engine.network.ServerInfoMessage;
import org.terasology.engine.network.serialization.ClientComponentFieldCheck;
import org.terasology.engine.persistence.serializers.EventSerializer;
import org.terasology.engine.persistence.serializers.NetworkEntitySerializer;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.engine.world.block.BlockUriParseException;
import org.terasology.engine.world.block.internal.BlockManagerImpl;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.engine.world.chunks.internal.ChunkSerializer;
import org.terasology.engine.world.chunks.remoteChunkProvider.RemoteChunkProvider;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.gestalt.entitysystem.event.Event;
import org.terasology.persistence.typeHandling.DeserializationException;
import org.terasology.persistence.typeHandling.SerializationException;
import org.terasology.protobuf.EntityData;
import org.terasology.protobuf.NetData;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

/**
 * Used to interact with a remote server (from client end)
 *
 */
public class ServerImpl implements Server {
    private static final Logger logger = LoggerFactory.getLogger(ServerImpl.class);

    private int clientEntityNetId;

    private final Channel channel;
    private Context context;
    private final NetworkSystemImpl networkSystem;
    private final EngineTime time;

    private NetMetricSource metricsSource;
    private BlockingQueue<NetData.NetMessage> queuedMessages = Queues.newLinkedBlockingQueue();
    private List<NetData.EventMessage> queuedOutgoingEvents = Lists.newArrayList();
    private NetData.ServerInfoMessage serverInfo;

    private EngineEntityManager entityManager;
    private NetworkEntitySerializer entitySerializer;
    private EventSerializer eventSerializer;
    private BlockManagerImpl blockManager;
    private ExtraBlockDataManager extraDataManager;

    private BlockEntityRegistry blockEntityRegistry;
    private RemoteChunkProvider remoteWorldProvider;
    private BlockingQueue<Chunk> chunkQueue = Queues.newLinkedBlockingQueue();
    private TIntSet netDirty = new TIntHashSet();
    private SetMultimap<Integer, Class<? extends Component>> changedComponents = HashMultimap.create();
    private ListMultimap<Vector3i, NetData.BlockChangeMessage> awaitingChunkReadyBlockUpdates = ArrayListMultimap.create();
    private ListMultimap<Vector3i, NetData.ExtraDataChangeMessage> awaitingChunkReadyExtraDataUpdates = ArrayListMultimap.create();


    public ServerImpl(NetworkSystemImpl system, EngineTime time, Channel channel, Context context) {
        this.channel = channel;
        metricsSource = (NetMetricSource) channel.pipeline().get(MetricRecordingHandler.NAME);
        this.networkSystem = system;
        this.time = time;
        this.context = context;
    }

    void connectToEntitySystem(EngineEntityManager newEntityManager, NetworkEntitySerializer newEntitySerializer,
                               EventSerializer newEventSerializer, BlockEntityRegistry newBlockEntityRegistry, Context newContext) {
        this.entityManager = newEntityManager;
        this.eventSerializer = newEventSerializer;
        this.entitySerializer = newEntitySerializer;
        this.blockEntityRegistry = newBlockEntityRegistry;
        this.context = newContext;
        blockManager = (BlockManagerImpl) newContext.getValue(BlockManager.class);
        extraDataManager = newContext.getValue(ExtraBlockDataManager.class);
    }

    void setServerInfo(NetData.ServerInfoMessage serverInfo) {
        this.serverInfo = serverInfo;
    }

    void setClientId(int id) {
        clientEntityNetId = id;
    }

    @Override
    public EntityRef getClientEntity() {
        return networkSystem.getEntity(clientEntityNetId);
    }

    @Override
    public String getRemoteAddress() {
        SocketAddress socketAddress = channel.remoteAddress();

        // Cast to InetSocketAddress to retrieve remote address
        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;

        return inetSocketAddress.getHostName() + "-" + inetSocketAddress.getAddress().getHostAddress() + "-" + inetSocketAddress.getPort();
    }

    @Override
    public ServerInfoMessage getInfo() {
        return new ServerInfoMessageImpl(serverInfo);
    }

    public NetData.ServerInfoMessage getRawInfo() {
        return serverInfo;
    }

    @Override
    public void send(Event event, EntityRef target) {
        NetworkComponent netComp = target.getComponent(NetworkComponent.class);
        if (netComp != null) {
            try {
                queuedOutgoingEvents.add(NetData.EventMessage.newBuilder()
                        .setEvent(eventSerializer.serialize(event))
                        .setTargetId(netComp.getNetworkId()).build());
            } catch (SerializationException e) {
                logger.error("Failed to serialize event", e);
            }
        }
    }

    @Override
    public void update(boolean netTick) {
        processReceivedChunks();
        if (entityManager != null) {
            if (netTick) {
                NetData.NetMessage.Builder message = NetData.NetMessage.newBuilder();
                message.setTime(time.getGameTimeInMs());
                sendEntities(message);
                sendEvents(message);
                send(message.build());
            } else if (!queuedOutgoingEvents.isEmpty()) {
                NetData.NetMessage.Builder message = NetData.NetMessage.newBuilder();
                message.setTime(time.getGameTimeInMs());
                sendEvents(message);
                send(message.build());
            }

            processMessages();
        }
    }

    private void sendEvents(NetData.NetMessage.Builder message) {
        queuedOutgoingEvents.forEach(message::addEvent);
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
        logger.atTrace().log("Sending with size {}", data.getSerializedSize());
        channel.writeAndFlush(data);
    }

    private void sendEntities(NetData.NetMessage.Builder message) {
        TIntIterator dirtyIterator = netDirty.iterator();
        while (dirtyIterator.hasNext()) {
            int netId = dirtyIterator.next();
            EntityRef entity = networkSystem.getEntity(netId);
            if (isOwned(entity)) {
                Set<Class<? extends Component>> emptyComponentClassSet = Collections.emptySet();
                EntityData.PackedEntity entityData = entitySerializer.serialize(entity, emptyComponentClassSet, changedComponents.get(netId),
                        emptyComponentClassSet, new ClientComponentFieldCheck());
                if (entityData != null) {
                    message.addUpdateEntity(NetData.UpdateEntityMessage.newBuilder().setEntity(entityData).setNetId(netId));
                }
            }
        }
        netDirty.clear();
    }

    private boolean isOwned(EntityRef entity) {
        EntityRef owner = networkSystem.getOwnerEntity(entity);
        return getClientEntity().equals(owner);
    }


    private void processEvent(NetData.EventMessage message) {
        try {
            Event event = eventSerializer.deserialize(message.getEvent());
            EntityRef target = EntityRef.NULL;
            if (message.hasTargetBlockPos()) {
                target = blockEntityRegistry.getBlockEntityAt(NetMessageUtil.convert(message.getTargetBlockPos()));
            } else if (message.hasTargetId()) {
                target = networkSystem.getEntity(message.getTargetId());
            }
            if (target.exists()) {
                target.send(event);
            } else {
                logger.atInfo().log("Dropping event {} for unavailable entity {}", event.getClass().getSimpleName(), target);
            }
        } catch (DeserializationException e) {
            logger.error("Failed to deserialize event", e);
        }
    }

    void setRemoteWorldProvider(RemoteChunkProvider remoteWorldProvider) {
        this.remoteWorldProvider = remoteWorldProvider;
        remoteWorldProvider.subscribe(this);
    }

    private void processMessages() {
        List<NetData.NetMessage> messages = Lists.newArrayListWithExpectedSize(queuedMessages.size());
        queuedMessages.drainTo(messages);

        for (NetData.NetMessage message : messages) {
            if (message.hasTime()) {
                time.updateTimeFromServer(message.getTime());
            }
            processBlockRegistrations(message);
            processReceivedChunks(message);
            processInvalidatedChunks(message);
            processBlockChanges(message);
            processExtraDataChanges(message);
            processRemoveEntities(message);
            message.getCreateEntityList().forEach(this::createEntityMessage);
            message.getUpdateEntityList().forEach(this::updateEntity);
            for (NetData.EventMessage event : message.getEventList()) {
                try {
                    processEvent(event);
                } catch (RuntimeException e) {
                    logger.error("Error processing server event", e);
                }
            }
        }
    }

    private void processRemoveEntities(NetData.NetMessage message) {
        for (NetData.RemoveEntityMessage removeEntity : message.getRemoveEntityList()) {
            int netId = removeEntity.getNetId();
            EntityRef entity = networkSystem.getEntity(netId);
            if (entity.exists()) {
                logger.info("Destroying entity: {}", entity);
                entity.destroy();
                networkSystem.unregisterClientNetworkEntity(netId);
            }
        }
    }

    /**
     * Apply the block changes from the message to the local world.
     */
    private void processBlockChanges(NetData.NetMessage message) {
        WorldProvider worldProvider = context.get(WorldProvider.class);
        for (NetData.BlockChangeMessage blockChange : message.getBlockChangeList()) {
            Block newBlock = blockManager.getBlock((short) blockChange.getNewBlock());
            logger.debug("Received block change to {}", newBlock);
            // TODO: Store changes to blocks that aren't ready to be modified (the surrounding chunks aren't available)
            Vector3i pos = NetMessageUtil.convert(blockChange.getPos());
            if (worldProvider.isBlockRelevant(pos)) {
                worldProvider.setBlock(pos, newBlock);
            } else {
                awaitingChunkReadyBlockUpdates.put(Chunks.toChunkPos(pos), blockChange);
            }
        }
    }

    /**
     * Apply the extra-data changes from the message to the local world.
     */
    private void processExtraDataChanges(NetData.NetMessage message) {
        WorldProvider worldProvider = context.get(WorldProvider.class);
        for (NetData.ExtraDataChangeMessage extraDataChange : message.getExtraDataChangeList()) {
            Vector3i pos = NetMessageUtil.convert(extraDataChange.getPos());
            if (worldProvider.isBlockRelevant(pos)) {
                worldProvider.setExtraData(extraDataChange.getIndex(), pos, extraDataChange.getNewData());
            } else {
                awaitingChunkReadyExtraDataUpdates.put(Chunks.toChunkPos(pos), extraDataChange);
            }
        }
    }

    private void processInvalidatedChunks(NetData.NetMessage message) {
        for (NetData.InvalidateChunkMessage chunk : message.getInvalidateChunkList()) {
            Vector3i chunkPos = NetMessageUtil.convert(chunk.getPos());
            remoteWorldProvider.invalidateChunks(chunkPos);
            awaitingChunkReadyBlockUpdates.removeAll(chunkPos);
            awaitingChunkReadyExtraDataUpdates.removeAll(chunkPos);
        }
    }

    private void processReceivedChunks(NetData.NetMessage message) {
        for (EntityData.ChunkStore chunkInfo : message.getChunkInfoList()) {
            Chunk chunk = ChunkSerializer.decode(chunkInfo, blockManager, extraDataManager);
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
                try {
                    BlockUri family = new BlockUri(blockFamily.getBlockUri(0)).getFamilyUri();
                    Map<String, Integer> registrationMap = Maps.newHashMap();
                    for (int i = 0; i < blockFamily.getBlockIdCount(); ++i) {
                        registrationMap.put(blockFamily.getBlockUri(i), blockFamily.getBlockId(i));
                    }
                    blockManager.receiveFamilyRegistration(family, registrationMap);
                } catch (BlockUriParseException e) {
                    logger.error("Received invalid block uri {}", blockFamily.getBlockUri(0)); //NOPMD
                }
            }
        }
    }

    private void updateEntity(NetData.UpdateEntityMessage updateEntity) {
        int entityNetId = updateEntity.getNetId();
        EntityRef currentEntity = networkSystem.getEntity(entityNetId);
        if (currentEntity.exists()) {
            NetworkComponent netComp = currentEntity.getComponent(NetworkComponent.class);
            if (netComp == null) {
                logger.error("Updating entity with no network component: {}, expected netId {}", currentEntity, entityNetId);
                return;
            }
            int networkId = netComp.getNetworkId();
            if (networkId != entityNetId) {
                logger.error("Network ID wrong before update");
            }
            boolean blockEntityBefore = currentEntity.hasComponent(BlockComponent.class);
            entitySerializer.deserializeOnto(currentEntity, updateEntity.getEntity());
            BlockComponent blockComponent = currentEntity.getComponent(BlockComponent.class);
            if (blockComponent != null && !blockEntityBefore
                    && !blockEntityRegistry.getExistingBlockEntityAt(blockComponent.getPosition()).equals(currentEntity)) {
                logger.error("Failed to associated new block entity");
            }
            if (networkId != entityNetId) {
                logger.error("Network ID lost in update: {}, {} -> {}", currentEntity, entityNetId, networkId);
            }
        } else {
            logger.warn("Received update for non-existent entity {}", entityNetId);
        }
    }

    private void createEntityMessage(NetData.CreateEntityMessage message) {
        entitySerializer.deserialize(message.getEntity());
    }

    @Override
    public void queueMessage(NetData.NetMessage message) {
        queuedMessages.offer(message);
    }

    @Override
    public void setComponentDirty(int netId, Class<? extends Component> componentType) {
        netDirty.add(netId);
        changedComponents.put(netId, componentType);
    }

    @Override
    public NetMetricSource getMetrics() {
        return metricsSource;
    }

    @Override
    public void onChunkReady(Vector3ic chunkPos) {
        WorldProvider worldProvider = context.get(WorldProvider.class);

        List<NetData.BlockChangeMessage> updateBlockMessages = awaitingChunkReadyBlockUpdates.removeAll(new Vector3i(chunkPos));
        for (NetData.BlockChangeMessage message : updateBlockMessages) {
            Vector3i pos = NetMessageUtil.convert(message.getPos());
            Block newBlock = blockManager.getBlock((short) message.getNewBlock());
            worldProvider.setBlock(pos, newBlock);
        }

        List<NetData.ExtraDataChangeMessage> updateExtraDataMessages = awaitingChunkReadyExtraDataUpdates.removeAll(new Vector3i(chunkPos));
        for (NetData.ExtraDataChangeMessage message : updateExtraDataMessages) {
            Vector3i pos = NetMessageUtil.convert(message.getPos());
            int newValue = message.getNewData();
            int i = message.getIndex();
            worldProvider.setExtraData(i, pos, newValue);
        }
    }
}


