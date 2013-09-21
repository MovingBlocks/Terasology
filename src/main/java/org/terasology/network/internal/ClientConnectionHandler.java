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

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.EngineTime;
import org.terasology.engine.Time;
import org.terasology.protobuf.NetData;

/**
 * @author Immortius
 */
public class ClientConnectionHandler extends SimpleChannelUpstreamHandler {

    private static final Logger logger = LoggerFactory.getLogger(ClientConnectionHandler.class);

    private NetworkSystemImpl networkSystem;
    private ServerImpl server;
    private ChannelHandlerContext channelHandlerContext;

    public ClientConnectionHandler(NetworkSystemImpl networkSystem) {
        this.networkSystem = networkSystem;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        NetData.NetMessage message = (NetData.NetMessage) e.getMessage();
        if (message.hasServerInfo()) {
            receivedServerInfo(message.getServerInfo());
        } else if (message.hasJoinComplete()) {
            completeJoin(message.getJoinComplete());
        } else {
            logger.error("Received unexpected message");
        }
    }

    private void completeJoin(NetData.JoinCompleteMessage joinComplete) {
        logger.info("Join complete received");
        server.setClientId(joinComplete.getClientId());

        channelHandlerContext.getPipeline().remove(this);
        channelHandlerContext.getPipeline().get(ClientHandler.class).joinComplete(server);
    }

    private void receivedServerInfo(NetData.ServerInfoMessage message) {
        logger.info("Received server info");
        ((EngineTime) CoreRegistry.get(Time.class)).setGameTime(message.getTime());
        this.server = new ServerImpl(networkSystem, channelHandlerContext.getChannel());
        server.setServerInfo(message);

        Config config = CoreRegistry.get(Config.class);
        channelHandlerContext.getChannel().write(NetData.NetMessage.newBuilder().setJoin(NetData.JoinMessage.newBuilder().setName(config.getPlayer().getName())
                .setViewDistanceLevel(config.getRendering().getViewDistance().getIndex())).build());

    }

    public void channelAuthenticated(ChannelHandlerContext ctx) {
        channelHandlerContext = ctx;
        ctx.getChannel().write(NetData.NetMessage.newBuilder()
                .setServerInfoRequest(NetData.ServerInfoRequest.newBuilder()).build());
    }
}
