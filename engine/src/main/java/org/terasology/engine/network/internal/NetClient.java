// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network.internal;

import com.google.common.base.Objects;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import io.netty.channel.Channel;
import org.joml.RoundingMode;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.entitySystem.metadata.EventLibrary;
import org.terasology.engine.entitySystem.metadata.EventMetadata;
import org.terasology.engine.entitySystem.metadata.NetworkEventType;
import org.terasology.engine.identity.PublicIdentityCertificate;
import org.terasology.engine.logic.characters.PredictionSystem;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.network.Client;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.network.ColorComponent;
import org.terasology.engine.network.NetMetricSource;
import org.terasology.engine.network.NetworkComponent;
import org.terasology.engine.network.serialization.ServerComponentFieldCheck;
import org.terasology.engine.persistence.serializers.EventSerializer;
import org.terasology.engine.persistence.serializers.NetworkEntitySerializer;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.rendering.world.viewDistance.ViewDistance;
import org.terasology.engine.world.WorldChangeListener;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.nui.Color;
import org.terasology.persistence.typeHandling.DeserializationException;
import org.terasology.persistence.typeHandling.SerializationException;
import org.terasology.protobuf.EntityData;
import org.terasology.protobuf.NetData;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A remote client.
 *
 */
public class NetClient extends AbstractClient implements WorldChangeListener {
    private static final Logger logger = LoggerFactory.getLogger(NetClient.class);
    private static final float NET_TICK_RATE = 0.05f;

    private Time time;
    private NetworkSystemImpl networkSystem;
    private Channel channel;
    private NetworkEntitySerializer entitySerializer;
    private EventSerializer eventSerializer;
    private EventLibrary eventLibrary;
    private NetMetricSource metricSource;

    // Relevance
    private Set<Vector3i> relevantChunks = Sets.newHashSet();
    private TIntSet netRelevant = new TIntHashSet();

    // Entity replication data
    private TIntSet netInitial = new TIntHashSet();
    private TIntSet netDirty = new TIntHashSet();
    private TIntSet netRemoved = new TIntHashSet();
    private SetMultimap<Integer, Class<? extends Component>> dirtyComponents = LinkedHashMultimap.create();
    private SetMultimap<Integer, Class<? extends Component>> addedComponents = LinkedHashMultimap.create();
    private SetMultimap<Integer, Class<? extends Component>> removedComponents = LinkedHashMultimap.create();

    private String preferredName = "Player";
    private long lastReceivedTime;
    private ViewDistance viewDistance = ViewDistance.NEAR;
    private float chunkSendCounter = 1.0f;

    private float chunkSendRate = 0.05469f;

    private PublicIdentityCertificate identity;

    // Outgoing messages
    private final List<BlockFamily> newlyRegisteredFamilies = Lists.newArrayList();

    private Map<Vector3i, Chunk> readyChunks = Maps.newLinkedHashMap();
    private Set<Vector3i> invalidatedChunks = Sets.newLinkedHashSet();


    // Incoming messages
    private BlockingQueue<NetData.NetMessage> queuedIncomingMessage = Queues.newLinkedBlockingQueue();

    // Metrics
    private AtomicInteger receivedMessages = new AtomicInteger();
    private AtomicInteger receivedBytes = new AtomicInteger();
    private AtomicInteger sentMessages = new AtomicInteger();
    private AtomicInteger sentBytes = new AtomicInteger();
    private Color color;

    /**
     * Sets up a new net client with metrics, time, identity, and a world provider.
     * @param channel
     * @param networkSystem
     * @param identity Publice certificate for the client.
     */
    public NetClient(Channel channel, NetworkSystemImpl networkSystem, PublicIdentityCertificate identity) {
        this.channel = channel;
        metricSource = (NetMetricSource) channel.pipeline().get(MetricRecordingHandler.NAME);
        this.networkSystem = networkSystem;
        this.time = CoreRegistry.get(Time.class);
        this.identity = identity;
        WorldProvider worldProvider = CoreRegistry.get(WorldProvider.class);
        if (worldProvider != null) {
            worldProvider.registerListener(this);
        }
    }

    @Override
    public String getName() {
        ClientComponent clientComp = getEntity().getComponent(ClientComponent.class);
        if (clientComp != null) {
            DisplayNameComponent displayInfo = clientComp.clientInfo.getComponent(DisplayNameComponent.class);
            if (displayInfo != null) {
                return displayInfo.name;
            }
        }
        return "Unknown Player";
    }

    @Override
    public Color getColor() {
        ClientComponent clientComp = getEntity().getComponent(ClientComponent.class);
        if (clientComp != null) {
            ColorComponent colorComp = clientComp.clientInfo.getComponent(ColorComponent.class);
            if (colorComp != null) {
                return colorComp.color;
            }
        }
        return color;
    }

    @Override
    public String getId() {
        return identity.getId();
    }

    public void setColor(Color color) {
        this.color = color;

        ClientComponent client = getEntity().getComponent(ClientComponent.class);
        if (client != null) {
            ColorComponent colorInfo = client.clientInfo.getComponent(ColorComponent.class);
            if (colorInfo != null) {
                colorInfo.color = color;
                client.clientInfo.saveComponent(colorInfo);
            }
        }

    }

    /**
     * @param preferredName the name the player would like to use.
     */
    public void setPreferredName(String preferredName) {
        this.preferredName = preferredName;
    }

    @Override
    public void disconnect() {
        super.disconnect();

        if (channel.isOpen()) {
            channel.close().awaitUninterruptibly();
        }

        WorldProvider worldProvider = CoreRegistry.get(WorldProvider.class);
        if (worldProvider != null) {
            worldProvider.unregisterListener(this);
        }
    }

    @Override
    public void update(boolean netTick) {
        if (netTick) {
            sendHeartBeat();
            sendRegisteredBlocks();
            sendNewChunks();
            sendChunkInvalidations();
            sendRemovedEntities();
            sendInitialEntities();
            sendDirtyEntities();
            this.channel.flush();
        }
        processReceivedMessages();
    }

    private void  sendHeartBeat() {
        NetData.NetMessage.Builder message = NetData.NetMessage.newBuilder();
        message.getHeartBeatBuilder().setTime(time.getGameTimeInMs());
        send(message.build());
    }


    private void sendRegisteredBlocks() {
        synchronized (newlyRegisteredFamilies) {
            for (BlockFamily family : newlyRegisteredFamilies) {
                NetData.NetMessage.Builder message = NetData.NetMessage.newBuilder();
                NetData.BlockFamilyRegisteredMessage.Builder pkt = message.getBlockFamilyRegisteredBuilder();
                for (Block block : family.getBlocks()) {
                    pkt.addBlockUri(block.getURI().toString());
                    pkt.addBlockId(block.getId());
                }
                send(message.build());
            }
            newlyRegisteredFamilies.clear();
        }
    }

    private void sendNewChunks() {
        if (!readyChunks.isEmpty()) {
            chunkSendCounter += chunkSendRate * NET_TICK_RATE * networkSystem.getBandwidthPerClient();
            if (chunkSendCounter > 1.0f) {
                chunkSendCounter -= 1.0f;
                Vector3i center = new Vector3i();
                LocationComponent loc = getEntity().getComponent(ClientComponent.class).character.getComponent(LocationComponent.class);
                if (loc != null) {
                    Vector3f target = loc.getWorldPosition(new Vector3f());
                    if (target.isFinite()) {
                        center.set(target, RoundingMode.HALF_UP); // use center as temporary variable
                        Chunks.toChunkPos(center, center); // update center to chunkPos
                    }
                }
                Vector3i pos = null;
                long distance = Integer.MAX_VALUE;
                for (Vector3i chunkPos : readyChunks.keySet()) {
                    long chunkDistance = chunkPos.distanceSquared(center);
                    if (pos == null || chunkDistance < distance) {
                        pos = chunkPos;
                        distance = chunkDistance;
                    }
                }
                Chunk chunk = readyChunks.remove(pos);
                relevantChunks.add(pos);
                NetData.NetMessage.Builder message = NetData.NetMessage.newBuilder();
                message.setChunkInfo(chunk.encode());
                send(message.build());
            }
        } else {
            chunkSendCounter = 1.0f;
        }
    }

    private void sendChunkInvalidations() {
        Iterator<Vector3i> i = invalidatedChunks.iterator();
        while (i.hasNext()) {
            Vector3i pos = i.next();
            i.remove();
            relevantChunks.remove(pos);

            NetData.NetMessage.Builder message = NetData.NetMessage.newBuilder();
            message.getInvalidateChunkBuilder().setPos(NetMessageUtil.convert(pos));
            send(message.build());
        }
        invalidatedChunks.clear();
    }

    public void setNetInitial(int netId) {
        netInitial.add(netId);
    }

    public void setNetRemoved(int netId) {
        if (!netInitial.remove(netId)) {
            netRemoved.add(netId);
        }
        dirtyComponents.keySet().remove(netId);
        addedComponents.keySet().remove(netId);
        removedComponents.keySet().remove(netId);
        netDirty.remove(netId);
        netRelevant.remove(netId);
    }

    public void setComponentAdded(int networkId, Class<? extends Component> component) {
        if (netRelevant.contains(networkId) && !netInitial.contains(networkId)) {
            if (removedComponents.remove(networkId, component)) {
                dirtyComponents.put(networkId, component);
            } else {
                addedComponents.put(networkId, component);
                netDirty.add(networkId);
            }
        }
    }

    public void setComponentRemoved(int networkId, Class<? extends Component> component) {
        if (netRelevant.contains(networkId) && !netInitial.contains(networkId)) {
            if (!addedComponents.remove(networkId, component)) {
                removedComponents.put(networkId, component);
                if (!dirtyComponents.remove(networkId, component)) {
                    netDirty.add(networkId);
                }
            }
        }
    }

    public void setComponentDirty(int netId, Class<? extends Component> componentType) {
        if (netRelevant.contains(netId) && !netInitial.contains(netId) && !addedComponents.get(netId).contains(componentType)) {
            dirtyComponents.put(netId, componentType);
            netDirty.add(netId);
        }
    }

    public void connected(EntityManager entityManager, NetworkEntitySerializer newEntitySerializer,
                          EventSerializer newEventSerializer, EventLibrary newEventLibrary) {
        this.entitySerializer = newEntitySerializer;
        this.eventSerializer = newEventSerializer;
        this.eventLibrary = newEventLibrary;

        createEntity(preferredName, color, entityManager);
    }

    @Override
    public void send(Event event, EntityRef target) {
        try {
            BlockComponent blockComp = target.getComponent(BlockComponent.class);
            NetData.NetMessage.Builder message = NetData.NetMessage.newBuilder();

            if (blockComp != null) {
                if (relevantChunks.contains(Chunks.toChunkPos(blockComp.getPosition(), new Vector3i()))) {
                    message.getBlockEventMessageBuilder()
                            .setTargetBlockPos(NetMessageUtil.convert(blockComp.getPosition()))
                            .setEvent(eventSerializer.serialize(event));
                }
            } else {
                NetworkComponent networkComponent = target.getComponent(NetworkComponent.class);
                if (networkComponent != null) {
                    if (netRelevant.contains(networkComponent.getNetworkId()) || netInitial.contains(networkComponent.getNetworkId())) {
                        message.getEventMessageBuilder()
                                .setTargetId(networkComponent.getNetworkId())
                                .setEvent(eventSerializer.serialize(event));
                    }
                }
            }
            send(message.build());
        } catch (SerializationException e) {
            logger.error("Failed to serialize event", e);
        }
    }

    @Override
    public ViewDistance getViewDistance() {
        return viewDistance;
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    void send(NetData.NetMessage data) {
        logger.trace("Sending packet with size {}", data.getSerializedSize());
        sentMessages.incrementAndGet();
        sentBytes.addAndGet(data.getSerializedSize());
        channel.write(data);
    }

    @Override
    public void onChunkRelevant(Vector3ic pos, Chunk chunk) {
        Vector3i result = new Vector3i(pos);
        invalidatedChunks.remove(result);
        readyChunks.put(result, chunk);
    }

    @Override
    public void onChunkIrrelevant(Vector3ic pos) {
        Vector3i result = new Vector3i(pos);
        readyChunks.remove(result);
        invalidatedChunks.add(result);
    }


    @Override
    public void onBlockChanged(Vector3ic pos, Block newBlock, Block originalBlock) {
        Vector3i chunkPos = Chunks.toChunkPos(pos, new Vector3i());

        if (relevantChunks.contains(chunkPos)) {
            NetData.NetMessage.Builder message = NetData.NetMessage.newBuilder();
            message.getBlockChangeBuilder()
                    .setPos(NetMessageUtil.convert(pos))
                    .setNewBlock(newBlock.getId());
            send(message.build());
        }
    }

    @Override
    public void onExtraDataChanged(int i, Vector3ic pos, int newData, int oldData) {
        Vector3i chunkPos = Chunks.toChunkPos(pos, new Vector3i());
        if (relevantChunks.contains(chunkPos)) {
            NetData.NetMessage.Builder message = NetData.NetMessage.newBuilder();
            message.getExtraDataChangeBuilder()
                    .setIndex(i)
                    .setPos(NetMessageUtil.convert(pos))
                    .setNewData(newData);
        }
    }



    private void processReceivedMessages() {
        NetData.NetMessage message = null;
        while ((message = queuedIncomingMessage.poll()) != null) {
            if (message.hasHeartBeat()) {
                processHeartbeatMessage(message.getHeartBeat());
            } else if (message.hasEventMessage()) {
                processNetEventMessage(message.getEventMessage());
            } else if (message.hasUpdateEntity()) {
                processUpdateMessage(message.getUpdateEntity());
            }
        }
    }
    private void processUpdateMessage(NetData.UpdateEntityMessage pkt) {
        EntityRef currentEntity = networkSystem.getEntity(pkt.getNetId());
        if (networkSystem.getOwner(currentEntity) == this) {
            entitySerializer.deserializeOnto(currentEntity, pkt.getEntity(),
                    new ServerComponentFieldCheck(false, true));
        }
    }
    private void processHeartbeatMessage(NetData.HeartBeatMessage  pkt) {
        PredictionSystem predictionSystem = CoreRegistry.get(PredictionSystem.class);
        if (pkt.getTime() > lastReceivedTime) {
            lastReceivedTime = pkt.getTime();
        }
        if (predictionSystem != null) {
            predictionSystem.restoreToPresent();
        }
    }

    private void  processNetEventMessage(NetData.EventMessage pkt) {
        try {
            Event event = eventSerializer.deserialize(pkt.getEvent());
            EventMetadata<?> metadata = eventLibrary.getMetadata(event.getClass());
            if (metadata.getNetworkEventType() != NetworkEventType.SERVER) {
                logger.warn("Received non-server event '{}' from client '{}'", metadata, getName());
                return;
            }
            EntityRef target = networkSystem.getEntity(pkt.getTargetId());
            if (target.exists()) {
                if (Objects.equal(networkSystem.getOwner(target), this)) {
                    target.send(event);
                } else {
                    logger.warn("Received event {} for non-owned entity {} from {}", event, target, this);
                }
            }
        } catch (DeserializationException e) {
            logger.error("Failed to deserialize event", e);
        } catch (RuntimeException e) {
            logger.error("Error processing event", e);
        }
    }


    private void sendDirtyEntities() {
        if (netDirty.size() > 0) {
            TIntIterator dirtyIterator = netDirty.iterator();
            while (dirtyIterator.hasNext()) {
                int netId = dirtyIterator.next();
                EntityRef entity = networkSystem.getEntity(netId);
                if (!entity.exists()) {
                    logger.error("Sending non-existent entity update for netId {}", netId);
                }
                boolean isOwner = networkSystem.getOwner(entity) == this;
                EntityData.PackedEntity entityData = entitySerializer.serialize(entity, addedComponents.get(netId),
                        dirtyComponents.get(netId), removedComponents.get(netId),
                        new ServerComponentFieldCheck(isOwner, false));
                if (entityData != null) {
                    NetData.NetMessage.Builder message = NetData.NetMessage.newBuilder();
                    message.getUpdateEntityBuilder().setEntity(entityData).setNetId(netId);
                    send(message.build());
                }
            }
            netDirty.clear();
        }
        addedComponents.clear();
        removedComponents.clear();
        dirtyComponents.clear();
    }

    private void sendRemovedEntities() {
        if (netRemoved.size() > 0) {
            NetData.NetMessage.Builder message = NetData.NetMessage.newBuilder();
            NetData.RemoveEntityMessage.Builder pkt = message.getRemoveEntityBuilder();
            TIntIterator initialIterator = netRemoved.iterator();
            while (initialIterator.hasNext()) {
                pkt.addNetId(initialIterator.next());
            }
            send(message.build());
            netRemoved.clear();
        }
    }

    private void sendInitialEntities() {
        int[] initial = netInitial.toArray();
        netInitial.clear();
        Arrays.sort(initial);
        for (int netId : initial) {
            netRelevant.add(netId);
            EntityRef entity = networkSystem.getEntity(netId);
            if (!entity.hasComponent(NetworkComponent.class)) {
                logger.error("Sending net entity with no network component: {} - {}", netId, entity);
                continue;
            }
            // Note: Send owner->server fields on initial create
            Client owner = networkSystem.getOwner(entity);
            EntityData.PackedEntity entityData = entitySerializer.serialize(entity, true,
                    new ServerComponentFieldCheck(owner == this, true)).build();
            NetData.NetMessage.Builder message = NetData.NetMessage.newBuilder();
            NetData.CreateEntityMessage.Builder pkt = message.getCreateEntityBuilder()
                    .setEntity(entityData);
//            BlockComponent blockComponent = entity.getComponent(BlockComponent.class);
//            if (blockComponent != null) {
//                pkt.setBlockPos(NetMessageUtil.convert(blockComponent.getPosition()));
//            }
            send(message.build());
        }
    }



    public void messageReceived(NetData.NetMessage message) {
        int serializedSize = message.getSerializedSize();
        receivedBytes.addAndGet(serializedSize);
        receivedMessages.incrementAndGet();
        queuedIncomingMessage.offer(message);
    }

    public NetMetricSource getMetrics() {
        return metricSource;
    }

    @Override
    public void setViewDistanceMode(ViewDistance distanceMode) {
        this.viewDistance = distanceMode;
    }

    public void blockFamilyRegistered(BlockFamily family) {
        synchronized (newlyRegisteredFamilies) {
            newlyRegisteredFamilies.add(family);
        }
    }
}
