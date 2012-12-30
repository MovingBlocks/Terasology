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
import org.terasology.game.CoreRegistry;
import org.terasology.logic.manager.MessageManager;
import org.terasology.logic.mod.Mod;
import org.terasology.logic.mod.ModManager;
import static org.terasology.protobuf.NetData.*;

import org.terasology.world.WorldProvider;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.localChunkProvider.LocalChunkProvider;

import java.util.Map;

/**
 * @author Immortius
 */
public class TerasologyServerHandler extends SimpleChannelUpstreamHandler {
    private static final Logger logger = LoggerFactory.getLogger(TerasologyServerHandler.class);

    private NetworkSystem networkSystem;
    private ClientPlayer client;

    public TerasologyServerHandler(NetworkSystem networkSystem) {
        this.networkSystem = networkSystem;
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
        networkSystem.registerChannel(e.getChannel());
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        client = new ClientPlayer(e.getChannel(), networkSystem);
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
        switch(message.getType()) {
            case CLIENT_CONNECT:
                receivedConnect(message.getClientConnect());
                break;
            case CONSOLE:
                networkSystem.sendChatMessage(message.getConsole().getMessage());
                MessageManager.getInstance().addMessage(message.getConsole().getMessage(), MessageManager.EMessageScope.PRIVATE);
                break;
        }
        logger.debug("Received message: {}", message.getType());
    }

    private void receivedConnect(ClientConnectMessage message) {
        MessageManager.getInstance().addMessage("Client connected: " + message.getName());
        client.setConnected();
        client.setName(message.getName());
        ServerInfoMessage.Builder serverInfoMessageBuilder = ServerInfoMessage.newBuilder();
        WorldProvider world = CoreRegistry.get(WorldProvider.class);
        serverInfoMessageBuilder.setTime(world.getTime());
        serverInfoMessageBuilder.setWorldName(world.getTitle());
        for (Mod mod : CoreRegistry.get(ModManager.class).getActiveMods()) {
            serverInfoMessageBuilder.addModule(ModuleInfo.newBuilder().setModuleId(mod.getModInfo().getId()).build());
        }
        for (Map.Entry<String, Byte> blockMapping : BlockManager.getInstance().getBlockIdMap().entrySet()) {
            serverInfoMessageBuilder.addBlockMapping(BlockMapping.newBuilder().setBlockId(blockMapping.getValue()).setBlockName(blockMapping.getKey()));
        }
        client.send(NetMessage.newBuilder().setType(NetMessage.Type.SERVER_INFO).setServerInfo(serverInfoMessageBuilder.build()).build());

        networkSystem.addClient(client);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        logger.warn("Unexpected exception from client", e.getCause());
        e.getChannel().close();
    }
}
