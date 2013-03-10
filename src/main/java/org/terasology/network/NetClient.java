package org.terasology.network;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.components.DisplayInformationComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.config.Config;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Event;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.entitySystem.metadata.EventMetadata;
import org.terasology.entitySystem.metadata.NetworkEventType;
import org.terasology.entitySystem.persistence.EventSerializer;
import org.terasology.entitySystem.persistence.PackedEntitySerializer;
import org.terasology.game.CoreRegistry;
import org.terasology.game.Timer;
import org.terasology.logic.characters.PredictionSystem;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.network.serialization.ServerComponentFieldCheck;
import org.terasology.protobuf.EntityData;
import org.terasology.protobuf.NetData;
import org.terasology.world.WorldChangeListener;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.entity.BlockComponent;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.Chunks;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Immortius
 */
public class NetClient extends AbstractClient implements WorldChangeListener {
    private static final Logger logger = LoggerFactory.getLogger(NetClient.class);

    private Config config = CoreRegistry.get(Config.class);

    private Timer timer;
    private NetworkSystemImpl networkSystem;
    private Channel channel;
    private PackedEntitySerializer entitySerializer;
    private EventSerializer eventSerializer;
    private EntitySystemLibrary entitySystemLibrary;
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

    private boolean awaitingConnectMessage = true;
    private String name = "Unknown";
    private long lastReceivedTime = 0;
    private int viewDistance = 0;

    // Outgoing messages
    private BlockingQueue<NetData.BlockChangeMessage> queuedOutgoingBlockChanges = Queues.newLinkedBlockingQueue();
    private List<NetData.EventMessage> queuedOutgoingEvents = Lists.newArrayList();

    private Map<Vector3i, Chunk> readyChunks = Maps.newLinkedHashMap();
    private Set<Vector3i> invalidatedChunks = Sets.newLinkedHashSet();


    // Incoming messages
    private BlockingQueue<NetData.NetMessage> queuedIncomingMessage = Queues.newLinkedBlockingQueue();

    // Metrics
    private AtomicInteger receivedMessages = new AtomicInteger();
    private AtomicInteger receivedBytes = new AtomicInteger();
    private AtomicInteger sentMessages = new AtomicInteger();
    private AtomicInteger sentBytes = new AtomicInteger();

    public NetClient(Channel channel, NetworkSystemImpl networkSystem) {
        this.channel = channel;
        metricSource = (NetMetricSource) channel.getPipeline().get(MetricRecordingHandler.NAME);
        this.networkSystem = networkSystem;
        this.timer = CoreRegistry.get(Timer.class);
        CoreRegistry.get(WorldProvider.class).registerListener(this);
    }

    @Override
    public boolean isAwaitingConnectMessage() {
        return awaitingConnectMessage;
    }

    @Override
    public String getName() {
        ClientComponent clientComp = getEntity().getComponent(ClientComponent.class);
        if (clientComp != null) {
            DisplayInformationComponent displayInfo = clientComp.clientInfo.getComponent(DisplayInformationComponent.class);
            if (displayInfo != null) {
                return displayInfo.name;
            }
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
        ClientComponent client = getEntity().getComponent(ClientComponent.class);
        if (client != null) {
            DisplayInformationComponent displayInfo = client.clientInfo.getComponent(DisplayInformationComponent.class);
            if (displayInfo != null) {
                displayInfo.name = name;
                client.clientInfo.saveComponent(displayInfo);
            }
        }
    }

    @Override
    public void disconnect() {
        super.disconnect();
        CoreRegistry.get(WorldProvider.class).unregisterListener(this);
    }

    @Override
    public void update(boolean netTick) {
        if (netTick) {
            NetData.NetMessage.Builder message = NetData.NetMessage.newBuilder();
            message.setTime(timer.getTimeInMs());
            sendChunkInvalidations(message);
            sendNewChunks(message);
            sendRemovedEntities(message);
            sendInitialEntities(message);
            sendDirtyEntities(message);
            sendEvents(message);
            send(message.build());
        }
        processReceivedMessages();
    }

    private void sendNewChunks(NetData.NetMessage.Builder message) {
        if (!readyChunks.isEmpty()) {
            Vector3i center = new Vector3i();
            LocationComponent loc = getEntity().getComponent(ClientComponent.class).character.getComponent(LocationComponent.class);
            if (loc != null) {
                center.set(TeraMath.calcChunkPos(new Vector3i(loc.getWorldPosition(), 0.5f)));
            }
            Vector3i pos = null;
            int distance = Integer.MAX_VALUE;
            for (Vector3i chunkPos : readyChunks.keySet()) {
                int chunkDistance = chunkPos.distanceSquared(center);
                if (pos == null || chunkDistance < distance) {
                    pos = chunkPos;
                    distance = chunkDistance;
                }
            }
            Chunk chunk = readyChunks.remove(pos);
            relevantChunks.add(pos);
            logger.debug("Sending chunk: {}", pos);
            message.addChunkInfo(Chunks.getInstance().encode(chunk, true)).build();
        }
    }

    private void sendChunkInvalidations(NetData.NetMessage.Builder message) {
        Iterator<Vector3i> i = invalidatedChunks.iterator();
        while (i.hasNext()) {
            Vector3i pos = i.next();
            i.remove();
            relevantChunks.remove(pos);
            message.addInvalidateChunk(NetData.InvalidateChunkMessage.newBuilder().setPos(NetworkUtil.convert(pos)));
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

    public void connected(EntityManager entityManager, PackedEntitySerializer entitySerializer, EventSerializer eventSerializer, EntitySystemLibrary entitySystemLibrary) {
        if (awaitingConnectMessage) {
            awaitingConnectMessage = false;

            this.entitySerializer = entitySerializer;
            this.eventSerializer = eventSerializer;
            this.entitySystemLibrary = entitySystemLibrary;

            createEntity(name, entityManager);
        }
    }

    @Override
    public void send(Event event, EntityRef target) {
        BlockComponent blockComp = target.getComponent(BlockComponent.class);
        if (blockComp != null) {
            if (relevantChunks.contains(TeraMath.calcChunkPos(blockComp.getPosition()))) {
                queuedOutgoingEvents.add(NetData.EventMessage.newBuilder()
                        .setTargetBlockPos(NetworkUtil.convert(blockComp.getPosition()))
                        .setEvent(eventSerializer.serialize(event)).build());
            }
        } else {
            NetworkComponent networkComponent = target.getComponent(NetworkComponent.class);
            if (networkComponent != null) {
                if (netRelevant.contains(networkComponent.getNetworkId()) || netInitial.contains(networkComponent.getNetworkId())) {
                    queuedOutgoingEvents.add(NetData.EventMessage.newBuilder()
                            .setTargetId(networkComponent.getNetworkId())
                            .setEvent(eventSerializer.serialize(event)).build());
                }
            }
        }
    }

    @Override
    public int getViewDistance() {
        switch (viewDistance) {
            case 1:
                return config.getRendering().getViewDistanceModerate();
            case 2:
                return config.getRendering().getViewDistanceFar();
            case 3:
                return config.getRendering().getViewDistanceUltra();
            default:
                return config.getRendering().getViewDistanceNear();
        }
    }

    void send(NetData.NetMessage data) {
        logger.trace("Sending packet with size {}", data.getSerializedSize());
        sentMessages.incrementAndGet();
        sentBytes.addAndGet(data.getSerializedSize());
        channel.write(data);
    }

    @Override
    public void onChunkRelevant(Vector3i pos, Chunk chunk) {
        invalidatedChunks.remove(pos);
        readyChunks.put(pos, chunk);
    }

    @Override
    public void onChunkIrrelevant(Vector3i pos) {
        readyChunks.remove(pos);
        invalidatedChunks.add(pos);
    }

    @Override
    public void onBlockChanged(Vector3i pos, Block newBlock, Block originalBlock) {
        Vector3i chunkPos = TeraMath.calcChunkPos(pos);
        if (relevantChunks.contains(chunkPos)) {
            queuedOutgoingBlockChanges.add(NetData.BlockChangeMessage.newBuilder()
                    .setPos(NetworkUtil.convert(pos))
                    .setNewBlock(newBlock.getId())
                    .build());
        }
    }

    private void processReceivedMessages() {
        List<NetData.NetMessage> messages = Lists.newArrayListWithExpectedSize(queuedIncomingMessage.size());
        queuedIncomingMessage.drainTo(messages);
        for (NetData.NetMessage message : messages) {
            if (message.hasTime() && message.getTime() > lastReceivedTime) {
                lastReceivedTime = message.getTime();
            }
            processEntityUpdates(message);
            processEvents(message);

        }
    }

    private void sendEvents(NetData.NetMessage.Builder message) {
        List<NetData.BlockChangeMessage> blockChanges = Lists.newArrayListWithExpectedSize(queuedOutgoingBlockChanges.size());
        queuedOutgoingBlockChanges.drainTo(blockChanges);
        message.addAllBlockChange(blockChanges);

        message.addAllEvent(queuedOutgoingEvents);
        queuedOutgoingEvents.clear();
    }

    private void processEntityUpdates(NetData.NetMessage message) {
        for (NetData.UpdateEntityMessage updateMessage : message.getUpdateEntityList()) {

            EntityRef currentEntity = networkSystem.getEntity(updateMessage.getNetId());
            if (networkSystem.getOwner(currentEntity) == this) {
                entitySerializer.deserializeOnto(currentEntity, updateMessage.getEntity(), new ServerComponentFieldCheck(false, true));
            }
        }
    }

    private void sendDirtyEntities(NetData.NetMessage.Builder message) {
        TIntIterator dirtyIterator = netDirty.iterator();
        while (dirtyIterator.hasNext()) {
            int netId = dirtyIterator.next();
            EntityRef entity = networkSystem.getEntity(netId);
            if (!entity.exists()) {
                logger.error("Sending non-existent entity update for netId {}", netId);
            }
            boolean isOwner = networkSystem.getOwner(entity) == this;
            EntityData.PackedEntity entityData = entitySerializer.serialize(entity, addedComponents.get(netId), dirtyComponents.get(netId), removedComponents.get(netId), new ServerComponentFieldCheck(isOwner, false));
            if (entityData != null) {
                message.addUpdateEntity(NetData.UpdateEntityMessage.newBuilder().setEntity(entityData).setNetId(netId));
            }
        }
        netDirty.clear();
        addedComponents.clear();
        removedComponents.clear();
        dirtyComponents.clear();
    }

    private void sendRemovedEntities(NetData.NetMessage.Builder message) {
        TIntIterator initialIterator = netRemoved.iterator();
        while (initialIterator.hasNext()) {
            message.addRemoveEntity(NetData.RemoveEntityMessage.newBuilder().setNetId(initialIterator.next()));
        }
        netRemoved.clear();
    }

    private void sendInitialEntities(NetData.NetMessage.Builder message) {
        TIntIterator initialIterator = netInitial.iterator();
        while (initialIterator.hasNext()) {
            int netId = initialIterator.next();
            netRelevant.add(netId);
            EntityRef entity = networkSystem.getEntity(netId);
            if (!entity.hasComponent(NetworkComponent.class)) {
                logger.error("Sending net entity with no network component: {} - {}", netId, entity);
                continue;
            }
            // Note: Send owner->server fields on initial create
            Client owner = networkSystem.getOwner(entity);
            EntityData.PackedEntity entityData = entitySerializer.serialize(entity, true, new ServerComponentFieldCheck(owner == this, true));
            NetData.CreateEntityMessage.Builder createMessage = NetData.CreateEntityMessage.newBuilder().setEntity(entityData);
            BlockComponent blockComponent = entity.getComponent(BlockComponent.class);
            if (blockComponent != null) {
                createMessage.setBlockPos(NetworkUtil.convert(blockComponent.getPosition()));
            }
            message.addCreateEntity(createMessage);
        }
        netInitial.clear();
    }

    private void processEvents(NetData.NetMessage message) {
        boolean lagCompensated = false;
        PredictionSystem predictionSystem = CoreRegistry.get(PredictionSystem.class);
        for (NetData.EventMessage eventMessage : message.getEventList()) {
            Event event = eventSerializer.deserialize(eventMessage.getEvent());
            EventMetadata<?> metadata = entitySystemLibrary.getEventLibrary().getMetadata(event.getClass());
            if (metadata.getNetworkEventType() != NetworkEventType.SERVER) {
                logger.warn("Received non-server event '{}' from client '{}'", metadata, getName());
                continue;
            }
            if (!lagCompensated && metadata.isLagCompensated()) {
                if (predictionSystem != null) {
                    predictionSystem.lagCompensate(getEntity(), lastReceivedTime);
                }
                lagCompensated = true;
            }
            EntityRef target = EntityRef.NULL;
            if (eventMessage.hasTargetId()) {
                target = networkSystem.getEntity(eventMessage.getTargetId());
            }
            if (target.exists()) {
                if (networkSystem.getOwner(target).equals(this)) {
                    target.send(event);
                } else {
                    logger.warn("Received event {} for non-owned entity {} from {}", event, target, this);
                }
            }
        }
        if (lagCompensated && predictionSystem != null) {
            predictionSystem.restoreToPresent();
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

    public void setViewDistanceMode(int newViewRange) {
        this.viewDistance = newViewRange;
    }
}
