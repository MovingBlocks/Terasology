package org.terasology.network;

import com.google.common.collect.Sets;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.components.DisplayInformationComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.components.world.WorldComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventReceiver;
import org.terasology.entitySystem.EventSystem;
import org.terasology.game.CoreRegistry;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.protobuf.NetData;
import org.terasology.world.WorldChangeListener;
import org.terasology.world.WorldProvider;
import org.terasology.world.WorldUtil;
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

    private EntityRef playerEntity;
    private Channel channel;
    private ChunkProvider chunkProvider;

    private Set<Vector3i> relevantChunks = Sets.newHashSet();

    private boolean awaitingConnectMessage = true;

    public ClientPlayer(Channel channel) {
        this.channel = channel;
        this.chunkProvider = CoreRegistry.get(ChunkProvider.class);
        playerEntity = CoreRegistry.get(EntityManager.class).create("engine:client");
        CoreRegistry.get(WorldProvider.class).registerListener(this);

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
}
