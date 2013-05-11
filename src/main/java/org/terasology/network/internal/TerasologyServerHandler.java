/*
 * Copyright 2013 Moving Blocks
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
import org.terasology.identity.PublicIdentityCertificate;

import static org.terasology.protobuf.NetData.ClientConnectMessage;
import static org.terasology.protobuf.NetData.NetMessage;

/**
 * @author Immortius
 */
public class TerasologyServerHandler extends SimpleChannelUpstreamHandler {
    private static final Logger logger = LoggerFactory.getLogger(TerasologyServerHandler.class);

    private NetworkSystemImpl networkSystem;
    private NetClient client;

    public TerasologyServerHandler(NetworkSystemImpl networkSystem) {
        this.networkSystem = networkSystem;
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

    public void channelAuthenticated(PublicIdentityCertificate identity, ChannelHandlerContext ctx) {
        client = new NetClient(ctx.getChannel(), networkSystem);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        NetMessage message = (NetMessage) e.getMessage();
        if (message.hasClientConnect()) {
            receivedConnect(message.getClientConnect());
        }
        client.messageReceived(message);
    }

    private void receivedConnect(ClientConnectMessage message) {
        if (client.isAwaitingConnectMessage()) {
            client.setName(message.getName());
            client.setViewDistanceMode(message.getViewDistanceLevel());
            networkSystem.addClient(client);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        logger.warn("Unexpected exception from client", e.getCause());
        e.getChannel().close();
    }
}
