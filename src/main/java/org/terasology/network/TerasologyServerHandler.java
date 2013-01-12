/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.network;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.logic.manager.MessageManager;
import org.terasology.protobuf.NetData;

import static org.terasology.protobuf.NetData.ClientConnectMessage;
import static org.terasology.protobuf.NetData.NetMessage;

/**
 * @author Immortius
 */
public class TerasologyServerHandler extends SimpleChannelUpstreamHandler {
    private static final Logger logger = LoggerFactory.getLogger(TerasologyServerHandler.class);

    private NetworkSystem networkSystem;
    private Client client;

    public TerasologyServerHandler(NetworkSystem networkSystem) {
        this.networkSystem = networkSystem;
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
        networkSystem.registerChannel(e.getChannel());
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        client = new Client(e.getChannel(), networkSystem);
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        MessageManager.getInstance().addMessage("Client disconnected: " + client.getName());
        networkSystem.removeClient(client);
        client.disconnect();
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        NetMessage message = (NetMessage) e.getMessage();
        client.receivedMessageWithSize(message.getSerializedSize());
        switch (message.getType()) {
            case CLIENT_CONNECT:
                receivedConnect(message.getClientConnect());
                break;
            case EVENT:
                receivedEvent(message.getEvent());
                break;
            case UPDATE_ENTITY:
                receivedEntityUpdate(message.getUpdateEntity());
                break;
        }
        logger.trace("Received message: {}", message.getType());
    }

    private void receivedEntityUpdate(NetData.UpdateEntityMessage message) {
        client.queueEntityUpdate(message);
    }

    private void receivedEvent(NetData.EventMessage message) {
        client.queueEvent(message);
    }

    private void receivedConnect(ClientConnectMessage message) {
        if (client.isAwaitingConnectMessage()) {
            client.setName(message.getName());
            networkSystem.addClient(client);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        logger.warn("Unexpected exception from client", e.getCause());
        e.getChannel().close();
    }
}
