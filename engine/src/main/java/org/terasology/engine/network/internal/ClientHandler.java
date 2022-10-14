// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network.internal;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.modes.StateMainMenu;

import java.util.Optional;

import static org.terasology.protobuf.NetData.NetMessage;

/**
 * This Netty handler is used on the client side to send and receive messages.
 */
public class ClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    private final GameEngine gameEngine;
    private final NetworkSystemImpl networkSystem;

    private ServerImpl server;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public ClientHandler(NetworkSystemImpl networkSystem, Optional<GameEngine> gameEngine) {
        this.gameEngine = gameEngine.orElse(null);
        this.networkSystem = networkSystem;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
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
