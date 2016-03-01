/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.network.internal;

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
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.EngineTime;
import org.terasology.engine.Time;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.event.Event;
import org.terasology.math.ChunkMath;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.NetMetricSource;
import org.terasology.network.NetworkComponent;
import org.terasology.network.Server;
import org.terasology.network.ServerInfoMessage;
import org.terasology.network.serialization.ClientComponentFieldCheck;
import org.terasology.persistence.serializers.EventSerializer;
import org.terasology.persistence.serializers.NetworkEntitySerializer;
import org.terasology.persistence.typeHandling.DeserializationException;
import org.terasology.persistence.typeHandling.SerializationException;
import org.terasology.protobuf.EntityData;
import org.terasology.protobuf.NetData;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.biomes.Biome;
import org.terasology.world.biomes.BiomeManager;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.BlockUriParseException;
import org.terasology.world.block.internal.BlockManagerImpl;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.internal.ChunkSerializer;
import org.terasology.world.chunks.remoteChunkProvider.RemoteChunkProvider;

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
    private List<NetData.EventMessage> queuedOutgoingEvents = Lists.newArrayList();
    private NetData.ServerInfoMessage serverInfo;

    private EngineEntityManager entityManager;
    private NetworkEntitySerializer entitySerializer;
    private EventSerializer eventSerializer;
    private BlockManagerImpl blockManager;
    private BiomeManager biomeManager;

    private BlockEntityRegistry blockEntityRegistry;
    private RemoteChunkProvider remoteWorldProvider;
    private BlockingQueue<Chunk> chunkQueue = Queues.newLinkedBlockingQueue();
    private TIntSet netDirty = new TIntHashSet();
    private SetMultimap<Integer, Class<? extends Component>> changedComponents = HashMultimap.create();
    private ListMultimap<Vector3i, NetData.BlockChangeMessage> awaitingChunkReadyBlockUpdates = ArrayListMultimap.create();
    private ListMultimap<Vector3i, NetData.BiomeChangeMessage> awaitingChunkReadyBiomeUpdates = ArrayListMultimap.create();

    private EngineTime time;


    public ServerImpl(NetworkSystemImpl system, Channel channel) {
        this.channel = channel;
        metricsSource = (NetMetricSource) channel.getPipeline().get(MetricRecordingHandler.NAME);
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
        biomeManager = CoreRegistry.get(BiomeManager.class);
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
        SocketAddress socketAddress = channel.getRemoteAddress();

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
        logger.trace("Sending with size {}", data.getSerializedSize());
        channel.write(data);
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
                logger.info("Dropping event {} for unavailable entity {}", event.getClass().getSimpleName(), target);
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
            processBiomeChanges(message);
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

    private void processBlockChanges(NetData.NetMessage message) {
        for (NetData.BlockChangeMessage blockChange : message.getBlockChangeList()) {
            Block newBlock = blockManager.getBlock((short) blockChange.getNewBlock());
            logger.debug("Received block change to {}", newBlock);
            // TODO: Store changes to blocks that aren't ready to be modified (the surrounding chunks aren't available)
            WorldProvider worldProvider = CoreRegistry.get(WorldProvider.class);
            Vector3i pos = NetMessageUtil.convert(blockChange.getPos());
            if (worldProvider.isBlockRelevant(pos)) {
                worldProvider.setBlock(pos, newBlock);
            } else {
                awaitingChunkReadyBlockUpdates.put(ChunkMath.calcChunkPos(pos), blockChange);
            }
        }
    }

    private void processBiomeChanges(NetData.NetMessage message) {
        for (NetData.BiomeChangeMessage biomeChange : message.getBiomeChangeList()) {
            logger.debug("Received block change to {}", blockManager.getBlock((short) biomeChange.getNewBiome()));
            // TODO: Store changes to blocks that aren't ready to be modified (the surrounding chunks aren't available)
            WorldProvider worldProvider = CoreRegistry.get(WorldProvider.class);
            Vector3i pos = NetMessageUtil.convert(biomeChange.getPos());
            if (worldProvider.isBlockRelevant(pos)) {
                Biome newBiome = biomeManager.getBiomeByShortId((short) biomeChange.getNewBiome());
                worldProvider.setBiome(pos, newBiome);
            } else {
                awaitingChunkReadyBiomeUpdates.put(ChunkMath.calcChunkPos(pos), biomeChange);
            }
        }
    }

    private void processInvalidatedChunks(NetData.NetMessage message) {
        for (NetData.InvalidateChunkMessage chunk : message.getInvalidateChunkList()) {
            Vector3i chunkPos = NetMessageUtil.convert(chunk.getPos());
            remoteWorldProvider.invalidateChunks(chunkPos);
            awaitingChunkReadyBlockUpdates.removeAll(chunkPos);
            awaitingChunkReadyBiomeUpdates.removeAll(chunkPos);
        }
    }

    private void processReceivedChunks(NetData.NetMessage message) {
        for (EntityData.ChunkStore chunkInfo : message.getChunkInfoList()) {
            Chunk chunk = ChunkSerializer.decode(chunkInfo, blockManager, biomeManager);
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
                    logger.error("Received invalid block uri", blockFamily.getBlockUri(0));
                }
            }
        }
    }

    private void updateEntity(NetData.UpdateEntityMessage updateEntity) {
        EntityRef currentEntity = networkSystem.getEntity(updateEntity.getNetId());
        if (currentEntity.exists()) {
            NetworkComponent netComp = currentEntity.getComponent(NetworkComponent.class);
            if (netComp == null) {
                logger.error("Updating entity with no network component: {}, expected netId {}", currentEntity, updateEntity.getNetId());
                return;
            }
            if (netComp.getNetworkId() != updateEntity.getNetId()) {
                logger.error("Network ID wrong before update");
            }
            boolean blockEntityBefore = currentEntity.hasComponent(BlockComponent.class);
            entitySerializer.deserializeOnto(currentEntity, updateEntity.getEntity());
            BlockComponent blockComponent = currentEntity.getComponent(BlockComponent.class);
            if (blockComponent != null && !blockEntityBefore) {
                if (!blockEntityRegistry.getExistingBlockEntityAt(blockComponent.getPosition()).equals(currentEntity)) {
                    logger.error("Failed to associated new block entity");
                }
            }
            if (netComp.getNetworkId() != updateEntity.getNetId()) {
                logger.error("Network ID lost in update: {}, {} -> {}", currentEntity, updateEntity.getNetId(), netComp.getNetworkId());
            }
        } else {
            logger.warn("Received update for non-existent entity {}", updateEntity.getNetId());
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
    public void onChunkReady(Vector3i chunkPos) {
        WorldProvider worldProvider = CoreRegistry.get(WorldProvider.class);

        List<NetData.BlockChangeMessage> updateBlockMessages = awaitingChunkReadyBlockUpdates.removeAll(chunkPos);
        for (NetData.BlockChangeMessage message : updateBlockMessages) {
            Vector3i pos = NetMessageUtil.convert(message.getPos());
            Block newBlock = blockManager.getBlock((short) message.getNewBlock());
            worldProvider.setBlock(pos, newBlock);
        }

        List<NetData.BiomeChangeMessage> updateBiomeMessages = awaitingChunkReadyBiomeUpdates.removeAll(chunkPos);
        for (NetData.BiomeChangeMessage message : updateBiomeMessages) {
            Vector3i pos = NetMessageUtil.convert(message.getPos());
            Biome newBiome = biomeManager.getBiomeByShortId((short) message.getNewBiome());
            worldProvider.setBiome(pos, newBiome);
        }
    }
}


