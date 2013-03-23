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
import org.terasology.game.GameEngine;
import org.terasology.game.Timer;
import org.terasology.game.modes.StateMainMenu;
import org.terasology.math.Vector3i;
import org.terasology.protobuf.ChunksProtobuf;
import org.terasology.protobuf.NetData;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.Chunks;

import static org.terasology.protobuf.NetData.BlockChangeMessage;
import static org.terasology.protobuf.NetData.ClientConnectMessage;
import static org.terasology.protobuf.NetData.InvalidateChunkMessage;
import static org.terasology.protobuf.NetData.NetMessage;
import static org.terasology.protobuf.NetData.ServerInfoMessage;

/**
 * @author Immortius
 */
public class TerasologyClientHandler extends SimpleChannelUpstreamHandler {

    private static final Logger logger = LoggerFactory.getLogger(TerasologyClientHandler.class);

    private NetworkSystemImpl networkSystem;
    private Server server;
    private boolean awaitingServerInfo = true;

    public TerasologyClientHandler(NetworkSystemImpl networkSystem) {
        this.networkSystem = networkSystem;
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        this.server = new Server(networkSystem, e.getChannel());
        networkSystem.setServer(server);
        Config config = CoreRegistry.get(Config.class);
        e.getChannel().write(NetMessage.newBuilder()
                .setClientConnect(ClientConnectMessage.newBuilder()
                        .setName(config.getPlayer().getName())
                        .setViewDistanceLevel(config.getRendering().getActiveViewDistanceMode())
                ).build());
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        CoreRegistry.get(GameEngine.class).changeState(new StateMainMenu("Disconnected From Server"));
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        NetMessage message = (NetMessage) e.getMessage();
        if (message.hasServerInfo()) {
            CoreRegistry.get(Timer.class).updateServerTime(message.getTime(), true);
            receivedServerInfo(message.getServerInfo());
        }
        server.queueMessage(message);
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
