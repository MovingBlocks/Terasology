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
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.registry.CoreRegistry;
import org.terasology.engine.GameEngine;
import org.terasology.engine.modes.StateMainMenu;

import static org.terasology.protobuf.NetData.NetMessage;

/**
 * This Netty handler is used on the client side to send and receive messages.
 *
 */
public class ClientHandler extends SimpleChannelUpstreamHandler {

    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    private NetworkSystemImpl networkSystem;
    private ServerImpl server;

    public ClientHandler(NetworkSystemImpl networkSystem) {
        this.networkSystem = networkSystem;
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        GameEngine gameEngine = CoreRegistry.get(GameEngine.class);
        if (gameEngine != null) {
            gameEngine.changeState(new StateMainMenu("Disconnected From Server"));
        }
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        NetMessage message = (NetMessage) e.getMessage();
        server.queueMessage(message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        logger.warn("Unexpected exception from client", e.getCause());
        e.getChannel().close();
    }


    public void joinComplete(ServerImpl joinedServer) {
        this.server = joinedServer;
        networkSystem.setServer(server);
    }
}
