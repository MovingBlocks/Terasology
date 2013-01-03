package org.terasology.network;

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
import org.terasology.entitySystem.PersistableEntityManager;
import org.terasology.entitySystem.metadata.extension.EntityRefTypeHandler;
import org.terasology.entitySystem.persistence.EntitySerializer;
import org.terasology.entitySystem.persistence.EventSerializer;
import org.terasology.game.CoreRegistry;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.protobuf.EntityData;
import org.terasology.protobuf.NetData;
import org.terasology.world.WorldChangeListener;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.ChunkRegionListener;
import org.terasology.world.chunks.ChunkUnloadedEvent;

import java.util.Set;

/**
 * @author Immortius
 */
public class ClientPlayer implements ChunkRegionListener, WorldChangeListener, EventReceiver<ChunkUnloadedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(ClientPlayer.class);

    private NetworkSystem networkSystem;
    private EntityManager entityManager;
    private EntityRef playerEntity;
    private Channel channel;
    private ChunkProvider chunkProvider;
    private EventSerializer eventSerializer;

    private Set<Vector3i> relevantChunks = Sets.newHashSet();
    private TIntSet netInitial = new TIntHashSet();
    private TIntSet netDirty = new TIntHashSet();
    private TIntSet netRelevant = new TIntHashSet();

    private boolean awaitingConnectMessage = true;

    private EntitySerializer serializer;

    public ClientPlayer(Channel channel, NetworkSystem networkSystem) {
        this.channel = channel;
        this.networkSystem = networkSystem;
        this.chunkProvider = CoreRegistry.get(ChunkProvider.class);
        this.entityManager = CoreRegistry.get(EntityManager.class);
        this.serializer = new EntitySerializer((PersistableEntityManager) entityManager);
        serializer.setIgnoringEntityId(true);
        this.playerEntity = entityManager.create("engine:client");
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
        }
    }

    public void setNetDirty(int netId) {
        if (netRelevant.contains(netId) && !netInitial.contains(netId)) {
            logger.debug("Marking dirty: {}" + netId);
            netDirty.add(netId);
        }
    }

    public boolean isAwaitingConnectMessage() {
        return awaitingConnectMessage;
    }

    public void setName(String name) {
        DisplayInformationComponent displayInfo = playerEntity.getComponent(DisplayInformationComponent.class);
        if (displayInfo != null) {
            displayInfo.name = name;
            playerEntity.saveComponent(displayInfo);
        }
    }

    public void setConnected() {
        awaitingConnectMessage = false;
        playerEntity.addComponent(new LocationComponent());
        CoreRegistry.get(EventSystem.class).registerEventReceiver(this, ChunkUnloadedEvent.class, WorldComponent.class);
        chunkProvider.addRegionEntity(playerEntity, 7, this);
    }

    public String getName() {
        DisplayInformationComponent displayInfo = playerEntity.getComponent(DisplayInformationComponent.class);
        if (displayInfo != null) {
            return displayInfo.name;
        }
        return "Unknown";
    }

    public void disconnect() {
        playerEntity.destroy();
        CoreRegistry.get(EventSystem.class).unregisterEventReceiver(this, ChunkUnloadedEvent.class, WorldComponent.class);
        CoreRegistry.get(WorldProvider.class).unregisterListener(this);
    }

    public void send(Event event, int targetId) {
        NetData.NetMessage message = NetData.NetMessage.newBuilder()
                .setType(NetData.NetMessage.Type.EVENT)
                .setEvent(NetData.EventMessage.newBuilder()
                        .setTargetId(targetId)
                        .setEvent(eventSerializer.serialize(event)))
                .build();
        channel.write(message);
    }

    public void send(NetData.NetMessage data) {
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
        EntityRefTypeHandler.setNetworkMode(networkSystem);

        // For now, send everything all at once
        TIntIterator initialIterator = netInitial.iterator();
        while (initialIterator.hasNext()) {
            int netId = initialIterator.next();
            EntityRef entity = networkSystem.getEntity(netId);
            EntityData.Entity entityData = serializer.serialize(entity, new NetworkComponentFieldCheck());
            NetData.NetMessage message = NetData.NetMessage.newBuilder()
                    .setType(NetData.NetMessage.Type.CREATE_ENTITY)
                    .setCreateEntity(NetData.CreateEntityMessage.newBuilder().setEntity(entityData))
                    .build();
            send(message);
        }

        TIntIterator dirtyIterator = netDirty.iterator();
        while (dirtyIterator.hasNext()) {
            int netId = dirtyIterator.next();
            EntityRef entity = networkSystem.getEntity(netId);
            EntityData.Entity entityData = serializer.serialize(entity, false, new NetworkComponentFieldCheck());
            NetData.NetMessage message = NetData.NetMessage.newBuilder()
                    .setType(NetData.NetMessage.Type.UPDATE_ENTITY)
                    .setUpdateEntity(NetData.UpdateEntityMessage.newBuilder().setEntity(entityData).setNetId(netId))
                    .build();
            send(message);
        }

        netInitial.clear();
        netDirty.clear();

        EntityRefTypeHandler.setEntityManagerMode((PersistableEntityManager) entityManager);
    }

    void setEventSerializer(EventSerializer eventSerializer) {
        this.eventSerializer = eventSerializer;
    }

    public EntityRef getEntity() {
        return playerEntity;
    }
}
