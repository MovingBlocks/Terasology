package org.terasology.network;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.components.DisplayInformationComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.components.world.WorldComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Event;
import org.terasology.entitySystem.EventReceiver;
import org.terasology.entitySystem.EventSystem;
import org.terasology.entitySystem.persistence.EntitySerializer;
import org.terasology.entitySystem.persistence.EventSerializer;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.mod.Mod;
import org.terasology.logic.mod.ModManager;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.network.events.ConnectedEvent;
import org.terasology.network.serialization.ServerComponentFieldCheck;
import org.terasology.network.serialization.NetworkEventFieldCheck;
import org.terasology.protobuf.EntityData;
import org.terasology.protobuf.NetData;
import org.terasology.world.WorldChangeListener;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.ChunkRegionListener;
import org.terasology.world.chunks.ChunkUnloadedEvent;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

/**
 * @author Immortius
 */
public class Client implements ChunkRegionListener, WorldChangeListener, EventReceiver<ChunkUnloadedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private static final NetworkEventFieldCheck EVENT_FIELD_CHECK = new NetworkEventFieldCheck();

    private NetworkSystem networkSystem;
    private EntityRef clientEntity = EntityRef.NULL;
    private Channel channel;
    private ChunkProvider chunkProvider;
    private EntitySerializer entitySerializer;
    private EventSerializer eventSerializer;

    private Set<Vector3i> relevantChunks = Sets.newHashSet();
    private TIntSet netInitial = new TIntHashSet();
    private TIntSet netDirty = new TIntHashSet();
    private TIntSet netRelevant = new TIntHashSet();

    private boolean awaitingConnectMessage = true;

    private String name = "Unknown";

    private BlockingQueue<NetData.EventMessage> queuedEvents = Queues.newLinkedBlockingQueue();
    private BlockingQueue<NetData.UpdateEntityMessage> queuedEntityUpdates = Queues.newLinkedBlockingQueue();

    public Client(Channel channel, NetworkSystem networkSystem) {
        this.channel = channel;
        this.networkSystem = networkSystem;
        this.chunkProvider = CoreRegistry.get(ChunkProvider.class);
        CoreRegistry.get(WorldProvider.class).registerListener(this);
    }

    public void setNetInitial(int netId) {
        netInitial.add(netId);
    }

    public void setNetRemoved(int netId, NetData.NetMessage removalMessage) {
        netInitial.remove(netId);
        netDirty.remove(netId);
        if (netRelevant.contains(netId)) {
            send(removalMessage);
            netRelevant.remove(netId);
        }
    }

    public void setNetDirty(int netId) {
        if (netRelevant.contains(netId) && !netInitial.contains(netId)) {
            logger.trace("Marking dirty: {}", netId);
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

    public void connected(EntityManager entityManager, EntitySerializer entitySerializer, EventSerializer eventSerializer) {
        if (awaitingConnectMessage) {
            awaitingConnectMessage = false;

            this.entitySerializer = entitySerializer;
            this.eventSerializer = eventSerializer;

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

            // Send server info to client
            NetData.ServerInfoMessage.Builder serverInfoMessageBuilder = NetData.ServerInfoMessage.newBuilder();
            WorldProvider world = CoreRegistry.get(WorldProvider.class);
            serverInfoMessageBuilder.setTime(world.getTime());
            serverInfoMessageBuilder.setWorldName(world.getTitle());
            for (Mod mod : CoreRegistry.get(ModManager.class).getActiveMods()) {
                serverInfoMessageBuilder.addModule(NetData.ModuleInfo.newBuilder().setModuleId(mod.getModInfo().getId()).build());
            }
            for (Map.Entry<String, Byte> blockMapping : BlockManager.getInstance().getBlockIdMap().entrySet()) {
                serverInfoMessageBuilder.addBlockMapping(NetData.BlockMapping.newBuilder().setBlockId(blockMapping.getValue()).setBlockName(blockMapping.getKey()));
            }
            serverInfoMessageBuilder.setClientId(clientEntity.getComponent(NetworkComponent.class).networkId);
            send(NetData.NetMessage.newBuilder().setType(NetData.NetMessage.Type.SERVER_INFO).setServerInfo(serverInfoMessageBuilder.build()).build());
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
            logger.info("Sending: {}", event);
            NetData.NetMessage message = NetData.NetMessage.newBuilder()
                    .setType(NetData.NetMessage.Type.EVENT)
                    .setEvent(NetData.EventMessage.newBuilder()
                            .setTargetId(targetId)
                            .setEvent(eventSerializer.serialize(event, EVENT_FIELD_CHECK)))
                    .build();
            channel.write(message);
        }
    }

    private void send(NetData.NetMessage data) {
        channel.write(data);
    }

    @Override
    public void onChunkReady(Vector3i pos, Chunk chunk) {
        if (relevantChunks.add(pos)) {
            logger.debug("Sending chunk: {}", pos);
            // TODO: probably need to queue and dripfeed these to prevent flooding
            NetData.NetMessage message = NetData.NetMessage.newBuilder().setType(NetData.NetMessage.Type.CHUNK).setChunkInfo(chunk.getChunkData()).build();
            channel.write(message);
        }
    }

    @Override
    public void onEvent(ChunkUnloadedEvent event, EntityRef entity) {
        if (relevantChunks.remove(event.getChunkPos())) {
            NetData.NetMessage message = NetData.NetMessage.newBuilder().
                    setType(NetData.NetMessage.Type.INVALIDATE_CHUNK).
                    setInvalidateChunk(NetData.InvalidateChunkMessage.newBuilder().
                            setPos(NetworkUtil.convert(event.getChunkPos())).build()).
                    build();
            channel.write(message);
        }
    }

    @Override
    public void onBlockChanged(Vector3i pos, Block newBlock, Block originalBlock) {
        Vector3i chunkPos = TeraMath.calcChunkPos(pos);
        if (relevantChunks.contains(chunkPos)) {
            NetData.NetMessage message = NetData.NetMessage.newBuilder()
                    .setType(NetData.NetMessage.Type.BLOCK_CHANGED)
                    .setBlockChange(NetData.BlockChangeMessage.newBuilder()
                            .setPos(NetworkUtil.convert(pos))
                            .setNewBlock(newBlock.getId())
                            .build()).build();
            channel.write(message);
        }
    }

    public void update() {
        // For now, send everything all at once
        sendInitialEntities();
        sendDirtyEntities();
        netDirty.clear();

        processEntityUpdates();
        processEvents();
    }

    private void processEntityUpdates() {
        List<NetData.UpdateEntityMessage> messages = Lists.newArrayListWithExpectedSize(queuedEntityUpdates.size());
        queuedEntityUpdates.drainTo(messages);
        for (NetData.UpdateEntityMessage message : messages) {

            EntityRef currentEntity = networkSystem.getEntity(message.getNetId());
            if (networkSystem.getOwner(currentEntity) == this) {
                entitySerializer.deserializeOnto(currentEntity, message.getEntity(), false, new ServerComponentFieldCheck(true));
            }
        }

    }

    private void sendDirtyEntities() {
        TIntIterator dirtyIterator = netDirty.iterator();
        while (dirtyIterator.hasNext()) {
            int netId = dirtyIterator.next();
            EntityRef entity = networkSystem.getEntity(netId);
            boolean isOwner = networkSystem.getOwner(entity) == this;
            EntityData.Entity entityData = entitySerializer.serialize(entity, false, new ServerComponentFieldCheck(isOwner));
            NetData.NetMessage message = NetData.NetMessage.newBuilder()
                    .setType(NetData.NetMessage.Type.UPDATE_ENTITY)
                    .setUpdateEntity(NetData.UpdateEntityMessage.newBuilder().setEntity(entityData).setNetId(netId))
                    .build();
            send(message);
        }
    }

    private void sendInitialEntities() {
        TIntIterator initialIterator = netInitial.iterator();
        while (initialIterator.hasNext()) {
            int netId = initialIterator.next();
            netRelevant.add(netId);
            EntityRef entity = networkSystem.getEntity(netId);
            // Note: Always send all variables on initial replication
            EntityData.Entity entityData = entitySerializer.serialize(entity, new ServerComponentFieldCheck(false));
            NetData.NetMessage message = NetData.NetMessage.newBuilder()
                    .setType(NetData.NetMessage.Type.CREATE_ENTITY)
                    .setCreateEntity(NetData.CreateEntityMessage.newBuilder().setEntity(entityData))
                    .build();
            send(message);
        }
        netInitial.clear();
    }

    private void processEvents() {
        List<NetData.EventMessage> messages = Lists.newArrayListWithExpectedSize(queuedEvents.size());
        queuedEvents.drainTo(messages);

        for (NetData.EventMessage message : messages) {
            Event event = eventSerializer.deserialize(message.getEvent());
            EntityRef target = networkSystem.getEntity(message.getTargetId());
            if (target.exists()) {
                if (event instanceof NetworkEvent) {
                    ((NetworkEvent) event).setClient(clientEntity);
                }
                target.send(event);
            } else {
                queuedEvents.offer(message);
            }
        }
    }

    public EntityRef getEntity() {
        return clientEntity;
    }

    void queueEvent(NetData.EventMessage message) {
        queuedEvents.offer(message);
    }

    public void queueEntityUpdate(NetData.UpdateEntityMessage message) {
        queuedEntityUpdates.offer(message);

    }
}
