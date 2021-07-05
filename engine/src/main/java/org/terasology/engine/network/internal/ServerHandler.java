// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network.internal;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.terasology.protobuf.NetData.NetMessage;

/**
 * This Netty handler is used to send and receive messages on the server end
 */
public class ServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ServerHandler.class);

    private NetworkSystemImpl networkSystem;
    private NetClient client;

    public ServerHandler(NetworkSystemImpl networkSystem) {
        this.networkSystem = networkSystem;
    }

    public void connectionComplete(NetClient netClient) {
        this.client = netClient;
        networkSystem.addClient(client);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        networkSystem.registerChannel(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (client != null) {
            networkSystem.removeClient(client);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NetMessage message = (NetMessage) msg;
        client.messageReceived(message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("Unexpected exception from client", cause);
        ctx.channel().close();
    }

}
