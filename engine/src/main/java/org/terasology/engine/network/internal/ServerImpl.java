// Copyright 2021 The Terasology Foundation
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
import org.terasology.engine.core.EngineTime;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.NetMetricSource;
import org.terasology.engine.network.NetworkComponent;
import org.terasology.engine.network.Server;
import org.terasology.engine.network.ServerInfoMessage;
import org.terasology.engine.network.serialization.ClientComponentFieldCheck;
import org.terasology.engine.persistence.ChunkStore;
import org.terasology.engine.persistence.serializers.EventSerializer;
import org.terasology.engine.persistence.serializers.NetworkEntitySerializer;
import org.terasology.engine.registry.CoreRegistry;
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

    private NetworkSystemImpl networkSystem;
    private Channel channel;
    private NetMetricSource metricsSource;
    private BlockingQueue<NetData.NetMessage> queuedMessages = Queues.newLinkedBlockingQueue();
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

    private EngineTime time;

    public ServerImpl(NetworkSystemImpl system, Channel channel) {
        this.channel = channel;
        metricsSource = (NetMetricSource) channel.pipeline().get(MetricRecordingHandler.NAME);
        this.networkSystem = system;
        this.time = (EngineTime) CoreRegistry.get(Time.class);
    }

    void connectToEntitySystem(EngineEntityManager newEntityManager, NetworkEntitySerializer newEntitySerializer,
                               EventSerializer newEventSerializer, BlockEntityRegistry newBlockEntityRegistry) {
        this.entityManager = newEntityManager;
        this.eventSerializer = newEventSerializer;
        this.entitySerializer = newEntitySerializer;
        this.blockEntityRegistry = newBlockEntityRegistry;
        blockManager = (BlockManagerImpl) CoreRegistry.get(BlockManager.class);
        extraDataManager = CoreRegistry.get(ExtraBlockDataManager.class);
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
                NetData.NetMessage.Builder message = NetData.NetMessage.newBuilder();
                message.getEventMessageBuilder()
                        .setTargetId(netComp.getNetworkId())
                        .setEvent(eventSerializer.serialize(event));
                send(message.build());
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
                sendHeartBeat();
                sendEntities();
                this.channel.flush();
            }
            processMessages();
        }
    }

    private void sendEntities() {
        TIntIterator dirtyIterator = netDirty.iterator();
        while (dirtyIterator.hasNext()) {
            int netId = dirtyIterator.next();
            EntityRef entity = networkSystem.getEntity(netId);
            if (getClientEntity().equals(networkSystem.getOwner(entity))) {
                Set<Class<? extends Component>> emptyComponentClassSet = Collections.emptySet();
                EntityData.PackedEntity entityData = entitySerializer.serialize(entity, emptyComponentClassSet, changedComponents.get(netId),
                        emptyComponentClassSet, new ClientComponentFieldCheck());
                if (entityData != null) {
                    NetData.NetMessage.Builder message = NetData.NetMessage.newBuilder();
                    message.getUpdateEntityBuilder().setEntity(entityData).setNetId(netId);
                    send(message.build());
                }
            }
        }
        netDirty.clear();
    }


    private void  sendHeartBeat() {
        NetData.NetMessage.Builder message = NetData.NetMessage.newBuilder();
        message.getHeartBeatBuilder().setTime(time.getGameTimeInMs());
        send(message.build());
    }

    private void processReceivedChunks() {
        if (remoteWorldProvider != null) {
            Chunk chunk = null;
            while ((chunk = chunkQueue.poll()) != null) {
                remoteWorldProvider.receiveChunk(chunk);
            }
        }
    }

    private void send(NetData.NetMessage data) {
        logger.trace("Sending with size {}", data.getSerializedSize());
        channel.write(data);
    }



    void setRemoteWorldProvider(RemoteChunkProvider remoteWorldProvider) {
        this.remoteWorldProvider = remoteWorldProvider;
        remoteWorldProvider.subscribe(this);
    }

    private void processMessages() {
        NetData.NetMessage message = null;
        while ((message = queuedMessages.poll()) != null) {
            if (message.hasHeartBeat()) {
                time.updateTimeFromServer(message.getHeartBeat().getTime());
            } else if (message.hasBlockFamilyRegistered()) {
                processBlockFamilyRegistered(message.getBlockFamilyRegistered());
            } else if (message.hasChunkInfo()) {
                Chunk chunk = ChunkSerializer.decode(message.getChunkInfo(), blockManager, extraDataManager);
                chunkQueue.offer(chunk);
            } else if (message.hasExtraDataChange()) {
                processExtraDataChanged(message.getExtraDataChange());
            } else if (message.hasCreateEntity()) {
                entitySerializer.deserialize(message.getCreateEntity().getEntity());
            } else if (message.hasInvalidateChunk()) {
                processInvalidChunks(message.getInvalidateChunk());
            } else if (message.hasUpdateEntity()) {
                processUpdateEntity(message.getUpdateEntity());
            } else if (message.hasRemoveEntity()) {
                processRemoveEntity(message.getRemoveEntity());
            } else if (message.hasBlockChange()) {
                processBlockChange(message.getBlockChange());
            } else if (message.hasEventMessage()) {
                processEvent(message.getEventMessage());
            } else if (message.hasBlockEventMessage()) {
                processBlockEvent(message.getBlockEventMessage());
            }
        }
    }

    private void processEvent(NetData.EventMessage pkt) {
        try {
            Event event = eventSerializer.deserialize(pkt.getEvent());
            EntityRef target = networkSystem.getEntity(pkt.getTargetId());
            if (target.exists()) {
                target.send(event);
            } else {
                logger.info("Dropping event {} for unavailable entity {}", event.getClass().getSimpleName(), target);
            }
        } catch (DeserializationException e) {
            logger.error("Failed to deserialize event", e);
        }
    }

    private void processBlockEvent(NetData.BlockEventMessage pkt) {
        try {
            Event event = eventSerializer.deserialize(pkt.getEvent());
            EntityRef target = blockEntityRegistry.getBlockEntityAt(NetMessageUtil.convert(pkt.getTargetBlockPos()));
            if (target.exists()) {
                target.send(event);
            } else {
                logger.info("Dropping event {} for unavailable entity {}", event.getClass().getSimpleName(), target);
            }
        } catch (DeserializationException e) {
            logger.error("Failed to deserialize event", e);
        }
    }



    private void processBlockChange(NetData.BlockChangeMessage pkt) {
        Block newBlock = blockManager.getBlock((short) pkt.getNewBlock());
        logger.debug("Received block change to {}", newBlock);
        // TODO: Store changes to blocks that aren't ready to be modified (the surrounding chunks aren't available)
        WorldProvider worldProvider = CoreRegistry.get(WorldProvider.class);
        Vector3i pos = NetMessageUtil.convert(pkt.getPos());
        if (worldProvider.isBlockRelevant(pos)) {
            worldProvider.setBlock(pos, newBlock);
        } else {
            awaitingChunkReadyBlockUpdates.put(Chunks.toChunkPos(pos), pkt);
        }
    }

    private void processRemoveEntity(NetData.RemoveEntityMessage pkt) {
        for (int netId : pkt.getNetIdList()) {
            EntityRef entity = networkSystem.getEntity(netId);
            if (entity.exists()) {
                logger.info("Destroying entity: {}", entity);
                entity.destroy();
                networkSystem.unregisterClientNetworkEntity(netId);
            }
        }
    }

    private void processUpdateEntity(NetData.UpdateEntityMessage pkt) {
        EntityRef currentEntity = networkSystem.getEntity(pkt.getNetId());
        if (currentEntity.exists()) {
            NetworkComponent netComp = currentEntity.getComponent(NetworkComponent.class);
            if (netComp == null) {
                logger.error("Updating entity with no network component: {}, expected netId {}", currentEntity, pkt.getNetId());
                return;
            }
            if (netComp.getNetworkId() != pkt.getNetId()) {
                logger.error("Network ID wrong before update");
            }
            boolean blockEntityBefore = currentEntity.hasComponent(BlockComponent.class);
            entitySerializer.deserializeOnto(currentEntity, pkt.getEntity());
            BlockComponent blockComponent = currentEntity.getComponent(BlockComponent.class);
            if (blockComponent != null && !blockEntityBefore) {
                if (!blockEntityRegistry.getExistingBlockEntityAt(blockComponent.getPosition()).equals(currentEntity)) {
                    logger.error("Failed to associated new block entity");
                }
            }
            if (netComp.getNetworkId() != pkt.getNetId()) {
                logger.error("Network ID lost in update: {}, {} -> {}", currentEntity, pkt.getNetId(), netComp.getNetworkId());
            }
        } else {
            logger.warn("Received update for non-existent entity {}", pkt.getNetId());
        }
    }

    private void processInvalidChunks(NetData.InvalidateChunkMessage pkt) {
        Vector3i chunkPos = NetMessageUtil.convert(pkt.getPos());
        remoteWorldProvider.invalidateChunks(chunkPos);
        awaitingChunkReadyBlockUpdates.removeAll(chunkPos);
        awaitingChunkReadyExtraDataUpdates.removeAll(chunkPos);
    }

    private void processExtraDataChanged(NetData.ExtraDataChangeMessage pkt) {
        WorldProvider worldProvider = CoreRegistry.get(WorldProvider.class);
        Vector3i pos = NetMessageUtil.convert(pkt.getPos());
        if (worldProvider.isBlockRelevant(pos)) {
            worldProvider.setExtraData(pkt.getIndex(), pos, pkt.getNewData());
        } else {
            awaitingChunkReadyExtraDataUpdates.put(Chunks.toChunkPos(pos), pkt);
        }
    }


    private void processBlockFamilyRegistered(NetData.BlockFamilyRegisteredMessage pkt) {
        if (pkt.getBlockIdCount() != pkt.getBlockUriCount()) {
            logger.error("Received block registration with mismatched id<->uri mapping");
        } else if (pkt.getBlockUriCount() == 0) {
            logger.error("Received empty block registration");
        } else {
            BlockUri family = new BlockUri(pkt.getBlockUri(0)).getFamilyUri();
            Map<String, Integer> registrationMap = Maps.newHashMap();
            for (int i = 0; i < pkt.getBlockIdCount(); i++) {
                registrationMap.put(pkt.getBlockUri(i), pkt.getBlockId(i));
            }
            blockManager.receiveFamilyRegistration(family, registrationMap);
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
        WorldProvider worldProvider = CoreRegistry.get(WorldProvider.class);

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


