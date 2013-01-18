/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.network;


import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.game.CoreRegistry;
import org.terasology.math.Vector3i;
import org.terasology.model.structures.TeraArray;
import org.terasology.model.structures.TeraSmartArray;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.Chunk;

import static org.terasology.protobuf.NetData.BlockChangeMessage;
import static org.terasology.protobuf.NetData.ChunkMessage;
import static org.terasology.protobuf.NetData.ClientConnectMessage;
import static org.terasology.protobuf.NetData.InvalidateChunkMessage;
import static org.terasology.protobuf.NetData.NetMessage;
import static org.terasology.protobuf.NetData.ServerInfoMessage;

/**
 * @author Immortius
 */
public class TerasologyClientHandler extends SimpleChannelUpstreamHandler {

    private static final Logger logger = LoggerFactory.getLogger(TerasologyClientHandler.class);

    private NetworkSystem networkSystem;
    private Server server;
    private boolean awaitingServerInfo = true;

    public TerasologyClientHandler(NetworkSystem networkSystem) {
        this.networkSystem = networkSystem;
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        this.server = new Server(networkSystem, e.getChannel());
        networkSystem.setServer(server);
        e.getChannel().write(NetMessage.newBuilder()
                .setType(NetMessage.Type.CLIENT_CONNECT)
                .setClientConnect(ClientConnectMessage.newBuilder()
                        .setName(CoreRegistry.get(Config.class).getPlayerConfig().getName()))
                .build());
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        NetMessage message = (NetMessage) e.getMessage();
        server.receivedMessageWithSize(message.getSerializedSize());
        switch (message.getType()) {
            case SERVER_INFO:
                receivedServerInfo(message.getServerInfo());
                break;
            case CHUNK:
                receivedChunk(message.getChunkInfo());
                break;
            case INVALIDATE_CHUNK:
                invalidateChunk(message.getInvalidateChunk());
                break;
            case BLOCK_CHANGED:
                blockChanged(message.getBlockChange());
                break;
            case CREATE_ENTITY:
            case UPDATE_ENTITY:
            case REMOVE_ENTITY:
                server.queueMessage(message);
                break;
            case EVENT:
                server.queueEvent(message.getEvent());
                break;
            default:
                logger.warn("Received unexpected message: {}", message.getType());
        }
    }

    private void blockChanged(BlockChangeMessage blockChange) {
        // TODO: Store changes to blocks that aren't ready to be modified (the surrounding chunks aren't available)
        WorldProvider worldProvider = CoreRegistry.get(WorldProvider.class);
        Vector3i pos = NetworkUtil.convert(blockChange.getPos());
        Block oldBlock = worldProvider.getBlock(pos);
        Block newBlock = BlockManager.getInstance().getBlock((byte) blockChange.getNewBlock());
        if (!worldProvider.setBlock(pos, newBlock, oldBlock)) {
            logger.error("Failed to enact block update from server - {} to {}", pos, newBlock);
        }
    }

    // TODO: Threading (need to deal with this coming from a background thread correctly)
    private void invalidateChunk(InvalidateChunkMessage invalidateChunk) {
        server.invalidateChunks(NetworkUtil.convert(invalidateChunk.getPos()));
    }

    private void receivedChunk(ChunkMessage chunkInfo) {
        logger.debug("Received chunk {}", NetworkUtil.convert(chunkInfo.getPos()));
        TeraArray blocks = new TeraArray(Chunk.SIZE_X, Chunk.SIZE_Y, Chunk.SIZE_Z, chunkInfo.getBlockData().toByteArray());
        TeraSmartArray sunlight = new TeraSmartArray(Chunk.SIZE_X, Chunk.SIZE_Y, Chunk.SIZE_Z, chunkInfo.getSunlightData().toByteArray());
        TeraSmartArray light = new TeraSmartArray(Chunk.SIZE_X, Chunk.SIZE_Y, Chunk.SIZE_Z, chunkInfo.getLightData().toByteArray());
        TeraSmartArray liquid = new TeraSmartArray(Chunk.SIZE_X, Chunk.SIZE_Y, Chunk.SIZE_Z, chunkInfo.getLiquidData().toByteArray());
        Chunk chunk = new Chunk(NetworkUtil.convert(chunkInfo.getPos()), blocks, sunlight, light, liquid);
        server.receiveChunk(chunk);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        logger.warn("Unexpected exception from client", e.getCause());
        e.getChannel().close();
    }

    private void receivedServerInfo(ServerInfoMessage message) {
        if (awaitingServerInfo) {
            awaitingServerInfo = false;
            server.setServerInfo(message);
        }
    }
}
