// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.network.internal;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.network.ServerInfoMessage;
import org.terasology.protobuf.NetData;

/**
 * Checks if a {@link org.terasology.protobuf.NetData.ServerInfoMessage} was received and disconnects.
 */
public class ServerInfoRequestHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ServerInfoRequestHandler.class);

    private volatile ServerInfoMessage serverInfo;

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("Could not query server info: ", cause);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NetData.NetMessage message = (NetData.NetMessage) msg;
        if (message.hasServerInfo()) {
            logger.info("Received server info");
            serverInfo = new ServerInfoMessageImpl(message.getServerInfo());
        } else {
            logger.error("Received unexpected message");
        }

        // in any case, we're done, so close the connection
        ctx.channel().close();
    }

    public ServerInfoMessage getServerInfo() {
        return serverInfo;
    }
}
