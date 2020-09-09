// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network.internal;


import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.modes.StateMainMenu;

import static org.terasology.engine.protobuf.NetData.NetMessage;

/**
 * This Netty handler is used on the client side to send and receive messages.
 *
 */
public class ClientHandler extends SimpleChannelUpstreamHandler {

    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    private final NetworkSystemImpl networkSystem;
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
