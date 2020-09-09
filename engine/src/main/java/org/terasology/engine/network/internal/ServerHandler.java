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

import static org.terasology.engine.protobuf.NetData.NetMessage;

/**
 * This Netty handler is used to send and receive messages on the server end
 *
 */
public class ServerHandler extends SimpleChannelUpstreamHandler {
    private static final Logger logger = LoggerFactory.getLogger(ServerHandler.class);

    private final NetworkSystemImpl networkSystem;
    private NetClient client;

    public ServerHandler(NetworkSystemImpl networkSystem) {
        this.networkSystem = networkSystem;
    }

    public void connectionComplete(NetClient netClient) {
        this.client = netClient;
        networkSystem.addClient(client);
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
        networkSystem.registerChannel(e.getChannel());
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        if (client != null) {
            networkSystem.removeClient(client);
        }
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        NetMessage message = (NetMessage) e.getMessage();
        client.messageReceived(message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        logger.warn("Unexpected exception from client", e.getCause());
        e.getChannel().close();
    }
}
