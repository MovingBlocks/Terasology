// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network.internal;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.modes.StateMainMenu;
import org.terasology.engine.registry.CoreRegistry;

import static org.terasology.protobuf.NetData.NetMessage;

/**
 * This Netty handler is used on the client side to send and receive messages.
 */
public class ClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    private NetworkSystemImpl networkSystem;
    private ServerImpl server;

    public ClientHandler(NetworkSystemImpl networkSystem) {
        this.networkSystem = networkSystem;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        GameEngine gameEngine = CoreRegistry.get(GameEngine.class);
        if (gameEngine != null) {
            gameEngine.changeState(new StateMainMenu("Disconnected From Server"));
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NetMessage message = (NetMessage) msg;
        server.queueMessage(message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("Unexpected exception from client", cause);
        ctx.channel().close();
    }

    public void joinComplete(ServerImpl joinedServer) {
        this.server = joinedServer;
        networkSystem.setServer(server);
    }
}
