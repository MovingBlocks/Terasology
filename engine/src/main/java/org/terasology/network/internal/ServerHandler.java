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
