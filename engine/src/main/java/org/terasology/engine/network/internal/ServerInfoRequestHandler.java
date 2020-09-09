// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.network.internal;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.network.ServerInfoMessage;
import org.terasology.engine.protobuf.NetData;

/**
 * Checks if a {@link org.terasology.engine.protobuf.NetData.ServerInfoMessage} was received
 * and disconnects.
 */
public class ServerInfoRequestHandler extends SimpleChannelUpstreamHandler {

    private static final Logger logger = LoggerFactory.getLogger(ServerInfoRequestHandler.class);

    private volatile ServerInfoMessage serverInfo;

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        logger.warn("Could not query server info: {}", e.getCause().toString());
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        NetData.NetMessage message = (NetData.NetMessage) e.getMessage();
        if (message.hasServerInfo()) {
            logger.info("Received server info");
            serverInfo = new ServerInfoMessageImpl(message.getServerInfo());
        } else {
            logger.error("Received unexpected message");
        }

        // in any case, we're done, so close the connection
        ctx.getChannel().close();
    }

    public ServerInfoMessage getServerInfo() throws Exception {
        return serverInfo;
    }
}
