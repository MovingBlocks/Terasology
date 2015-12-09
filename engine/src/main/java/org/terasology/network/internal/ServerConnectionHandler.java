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

import com.google.common.io.ByteStreams;
import com.google.protobuf.ByteString;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.module.ModuleManager;
import org.terasology.identity.PublicIdentityCertificate;
import org.terasology.module.Module;
import org.terasology.naming.Name;
import org.terasology.protobuf.NetData;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.world.viewDistance.ViewDistance;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 */
public class ServerConnectionHandler extends SimpleChannelUpstreamHandler {

    private static final Logger logger = LoggerFactory.getLogger(ServerConnectionHandler.class);

    private NetworkSystemImpl networkSystem;
    private ServerHandler serverHandler;
    private ChannelHandlerContext channelHandlerContext;

    private PublicIdentityCertificate identity;

    private ModuleManager moduleManager = CoreRegistry.get(ModuleManager.class);

    public ServerConnectionHandler(NetworkSystemImpl networkSystem) {
        this.networkSystem = networkSystem;
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        super.channelOpen(ctx, e);
        this.channelHandlerContext = ctx;
        serverHandler = ctx.getPipeline().get(ServerHandler.class);
    }

    public void channelAuthenticated(PublicIdentityCertificate id) {
        this.identity = id;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        NetData.NetMessage message = (NetData.NetMessage) e.getMessage();
        if (message.hasServerInfoRequest()) {
            NetData.ServerInfoMessage serverInfo = networkSystem.getServerInfoMessage();
            ctx.getChannel().write(NetData.NetMessage.newBuilder().setServerInfo(serverInfo).setTime(serverInfo.getTime()).build());
        } else if (message.hasJoin()) {
            receivedConnect(message.getJoin());
        } else if (message.getModuleRequestCount() > 0) {
            sendModules(message.getModuleRequestList());
        } else {
            logger.error("Received unexpected message");
        }
    }

    private void sendModules(List<NetData.ModuleRequest> moduleRequestList) {
        for (NetData.ModuleRequest request : moduleRequestList) {
            NetData.ModuleDataHeader.Builder result = NetData.ModuleDataHeader.newBuilder();
            result.setId(request.getModuleId());
            Module module = moduleManager.getEnvironment().get(new Name(request.getModuleId()));
            if (module.isOnClasspath() || module.getLocations().size() != 1 || !Files.isReadable(module.getLocations().get(0))) {
                result.setError("Module not available for download");
            } else {
                Path location = module.getLocations().get(0);
                try {
                    result.setVersion(module.getVersion().toString());
                    result.setSize(Files.size(location));
                    channelHandlerContext.getChannel().write(NetData.NetMessage.newBuilder().setModuleDataHeader(result).build());
                } catch (IOException e) {
                    logger.error("Error sending module data header", e);
                    channelHandlerContext.getChannel().close();
                    break;
                }

                try (InputStream stream = new BufferedInputStream(Files.newInputStream(location))) {


                    long remainingData = Files.size(location);
                    byte[] data = new byte[1024];
                    while (remainingData > 0) {
                        int nextBlock = (int) Math.min(remainingData, 1024);
                        ByteStreams.read(stream, data, 0, nextBlock);
                        channelHandlerContext.getChannel().write(
                                NetData.NetMessage.newBuilder().setModuleData(
                                        NetData.ModuleData.newBuilder().setModule(ByteString.copyFrom(data, 0, nextBlock))
                                ).build()
                        );
                        remainingData -= nextBlock;
                    }
                } catch (IOException e) {
                    logger.error("Error sending module", e);
                    channelHandlerContext.getChannel().close();
                    break;
                }
            }
        }
    }

    private void receivedConnect(NetData.JoinMessage message) {
        logger.info("Received Start Join");
        NetClient client = new NetClient(channelHandlerContext.getChannel(), networkSystem, identity);
        client.setPreferredName(message.getName());
        client.setColor(new Color(message.getColor().getRgba()));
        client.setViewDistanceMode(ViewDistance.forIndex(message.getViewDistanceLevel()));
        channelHandlerContext.getPipeline().remove(this);
        serverHandler.connectionComplete(client);
    }

}
