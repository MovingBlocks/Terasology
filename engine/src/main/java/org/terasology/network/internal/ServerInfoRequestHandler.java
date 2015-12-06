/*
 * Copyright 2015 MovingBlocks
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
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.network.ServerInfoMessage;
import org.terasology.protobuf.NetData;

/**
 * Checks if a {@link org.terasology.protobuf.NetData.ServerInfoMessage} was received
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
