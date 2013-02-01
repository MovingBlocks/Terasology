package org.terasology.network;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
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
import org.terasology.components.world.WorldComponent;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Event;
import org.terasology.entitySystem.EventReceiver;
import org.terasology.entitySystem.EventSystem;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.entitySystem.metadata.EventMetadata;
import org.terasology.entitySystem.metadata.NetworkEventType;
import org.terasology.entitySystem.persistence.EventSerializer;
import org.terasology.entitySystem.persistence.FieldSerializeCheck;
import org.terasology.entitySystem.persistence.PackedEntitySerializer;
import org.terasology.game.CoreRegistry;
import org.terasology.game.Timer;
import org.terasology.logic.characters.ClientCharacterPredictionSystem;
import org.terasology.logic.characters.PredictionSystem;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.network.serialization.ServerComponentFieldCheck;
import org.terasology.protobuf.EntityData;
import org.terasology.protobuf.NetData;
import org.terasology.world.WorldChangeListener;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkRegionListener;
import org.terasology.world.chunks.ChunkUnloadedEvent;
import org.terasology.world.chunks.Chunks;

import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Immortius
 */
public class Client implements ChunkRegionListener, WorldChangeListener, EventReceiver<ChunkUnloadedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private static final FieldSerializeCheck<Event> EVENT_FIELD_CHECK = FieldSerializeCheck.NullCheck.<Event>newInstance();

    private Timer timer;
    private NetworkSystem networkSystem;
    private EntityRef clientEntity = EntityRef.NULL;
    private Channel channel;
    private PackedEntitySerializer entitySerializer;
    private EventSerializer eventSerializer;
    private EntitySystemLibrary entitySystemLibrary;

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

    // Outgoing messages
    private BlockingQueue<NetData.BlockChangeMessage> queuedOutgoingBlockChanges = Queues.newLinkedBlockingQueue();
    private List<NetData.EventMessage> queuedOutgoingEvents = Lists.newArrayList();
    private List<NetData.InvalidateChunkMessage> queuedInvalidatedChunkEvents = Lists.newArrayList();

    // Incoming messages
    private BlockingQueue<NetData.NetMessage> queuedIncomingMessage = Queues.newLinkedBlockingQueue();

    // Metrics
    private AtomicInteger receivedMessages = new AtomicInteger();
    private AtomicInteger receivedBytes = new AtomicInteger();
    private AtomicInteger sentMessages = new AtomicInteger();
    private AtomicInteger sentBytes = new AtomicInteger();

    public Client(Channel channel, NetworkSystem networkSystem) {
        this.channel = channel;
        this.networkSystem = networkSystem;
        this.timer = CoreRegistry.get(Timer.class);
        CoreRegistry.get(WorldProvider.class).registerListener(this);
    }

    public void setNetInitial(int netId) {
        netInitial.add(netId);
    }

    public void setNetRemoved(int netId) {
        if (!netInitial.remove(netId)) {
            netDirty.add(netId);
        }
        dirtyComponents.keySet().remove(netId);
        addedComponents.keySet().remove(netId);
        removedComponents.keySet().remove(netId);
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

    public boolean isAwaitingConnectMessage() {
        return awaitingConnectMessage;
    }

    public void setName(String name) {
        this.name = name;
        ClientComponent client = clientEntity.getComponent(ClientComponent.class);
        if (client != null) {
            DisplayInformationComponent displayInfo = client.clientInfo.getComponent(DisplayInformationComponent.class);
            if (displayInfo != null) {
                displayInfo.name = name;
                client.clientInfo.saveComponent(displayInfo);
            }
        }
    }

    public void connected(EntityManager entityManager, PackedEntitySerializer entitySerializer, EventSerializer eventSerializer, EntitySystemLibrary entitySystemLibrary) {
        if (awaitingConnectMessage) {
            awaitingConnectMessage = false;

            this.entitySerializer = entitySerializer;
            this.eventSerializer = eventSerializer;
            this.entitySystemLibrary = entitySystemLibrary;

            // Create player entity
            clientEntity = entityManager.create("engine:client");
            CoreRegistry.get(EventSystem.class).registerEventReceiver(this, ChunkUnloadedEvent.class, WorldComponent.class);

            EntityRef clientInfo = entityManager.create("engine:clientInfo");
            DisplayInformationComponent displayInfo = clientInfo.getComponent(DisplayInformationComponent.class);
            displayInfo.name = name;
            clientInfo.saveComponent(displayInfo);

            ClientComponent clientComponent = clientEntity.getComponent(ClientComponent.class);
            clientComponent.clientInfo = clientInfo;
            clientEntity.saveComponent(clientComponent);
        }
    }

    public String getName() {
        ClientComponent clientComp = clientEntity.getComponent(ClientComponent.class);
        if (clientComp != null) {
            DisplayInformationComponent displayInfo = clientComp.clientInfo.getComponent(DisplayInformationComponent.class);
            if (displayInfo != null) {
                return displayInfo.name;
            }
        }
        return name;
    }

    public void disconnect() {
        clientEntity.destroy();
        CoreRegistry.get(EventSystem.class).unregisterEventReceiver(this, ChunkUnloadedEvent.class, WorldComponent.class);
        CoreRegistry.get(WorldProvider.class).unregisterListener(this);
    }

    public void send(Event event, int targetId) {
        if (netRelevant.contains(targetId)) {
            queuedOutgoingEvents.add(NetData.EventMessage.newBuilder()
                    .setTargetId(targetId)
                    .setEvent(eventSerializer.serialize(event, EVENT_FIELD_CHECK)).build());
        }
    }

    void send(NetData.NetMessage data) {
        logger.trace("Sending packet with size {}", data.getSerializedSize());
        sentMessages.incrementAndGet();
        sentBytes.addAndGet(data.getSerializedSize());
        channel.write(data);
    }

    @Override
    public void onChunkReady(Vector3i pos, Chunk chunk) {
        if (relevantChunks.add(pos)) {
            logger.debug("Sending chunk: {}", pos);
            // TODO: probably need to queue and dripfeed these to prevent flooding
            NetData.NetMessage message = NetData.NetMessage.newBuilder()
                    .setTime(timer.getTimeInMs())
                    .addChunkInfo(Chunks.getInstance().encode(chunk)).build();
            send(message);
        }
    }

    @Override
    public void onEvent(ChunkUnloadedEvent event, EntityRef entity) {
        if (relevantChunks.remove(event.getChunkPos())) {
            queuedInvalidatedChunkEvents.add(NetData.InvalidateChunkMessage.newBuilder().
                    setPos(NetworkUtil.convert(event.getChunkPos())).build());
        }
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

    public void update(boolean netTick) {
        if (netTick) {
            NetData.NetMessage.Builder message = NetData.NetMessage.newBuilder();
            message.setTime(timer.getTimeInMs());
            sendRemovedEntities(message);
            sendInitialEntities(message);
            sendDirtyEntities(message);
            sendEvents(message);
            send(message.build());
        } else if (!queuedOutgoingEvents.isEmpty() || !queuedOutgoingBlockChanges.isEmpty() || !queuedInvalidatedChunkEvents.isEmpty()) {
            NetData.NetMessage.Builder message = NetData.NetMessage.newBuilder();
            message.setTime(timer.getTimeInMs());
            sendEvents(message);
            send(message.build());
        }
        processReceivedMessages();
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
        message.addAllInvalidateChunk(queuedInvalidatedChunkEvents);
        queuedOutgoingEvents.clear();
        queuedInvalidatedChunkEvents.clear();
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
            // Note: Send owner->server fields on initial create
            EntityData.PackedEntity entityData = entitySerializer.serialize(entity, true, new ServerComponentFieldCheck(false, true));
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
            EntityRef target = networkSystem.getEntity(eventMessage.getTargetId());
            if (target.exists()) {
                if (networkSystem.getOwner(target).equals(this)) {
                    if (event instanceof NetworkEvent) {
                        ((NetworkEvent) event).setClient(clientEntity);
                    }
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

    public EntityRef getEntity() {
        return clientEntity;
    }

    public void messageReceived(NetData.NetMessage message) {
        int serializedSize = message.getSerializedSize();
        receivedBytes.addAndGet(serializedSize);
        receivedMessages.incrementAndGet();
        queuedIncomingMessage.offer(message);
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
