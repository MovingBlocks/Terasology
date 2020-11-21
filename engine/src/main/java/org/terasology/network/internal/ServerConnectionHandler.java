// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.network.internal;

import com.google.common.io.ByteStreams;
import com.google.protobuf.ByteString;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.module.ModuleManager;
import org.terasology.identity.PublicIdentityCertificate;
import org.terasology.module.Module;
import org.terasology.naming.Name;
import org.terasology.nui.Color;
import org.terasology.protobuf.NetData;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.world.viewDistance.ViewDistance;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 *
 */
public class ServerConnectionHandler extends ChannelInboundHandlerAdapter {

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
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.channelHandlerContext = ctx;
        serverHandler = ctx.pipeline().get(ServerHandler.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NetData.NetMessage message = (NetData.NetMessage) msg;
        if (message.hasServerInfoRequest()) {
            NetData.ServerInfoMessage serverInfo = networkSystem.getServerInfoMessage();
            ctx.channel().writeAndFlush(NetData.NetMessage.newBuilder().setServerInfo(serverInfo).setTime(serverInfo.getTime()).build());
        } else if (message.hasJoin()) {
            receivedConnect(message.getJoin());
        } else if (message.getModuleRequestCount() > 0) {
            sendModules(message.getModuleRequestList());
        } else {
            super.channelRead(ctx, msg);
            logger.error("Received unexpected message");
        }
    }

    public void channelAuthenticated(PublicIdentityCertificate id) {
        this.identity = id;
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
                    channelHandlerContext.channel().write(NetData.NetMessage.newBuilder().setModuleDataHeader(result).build());
                } catch (IOException e) {
                    logger.error("Error sending module data header", e);
                    channelHandlerContext.channel().close();
                    break;
                }

                try (InputStream stream = new BufferedInputStream(Files.newInputStream(location))) {

                    long remainingData = Files.size(location);
                    byte[] data = new byte[1024];
                    while (remainingData > 0) {
                        int nextBlock = (int) Math.min(remainingData, 1024);
                        ByteStreams.read(stream, data, 0, nextBlock);
                        channelHandlerContext.channel().write(
                                NetData.NetMessage.newBuilder().setModuleData(
                                        NetData.ModuleData.newBuilder().setModule(ByteString.copyFrom(data, 0,
                                                nextBlock))
                                ).build()
                        );
                        remainingData -= nextBlock;
                    }
                } catch (IOException e) {
                    logger.error("Error sending module", e);
                    channelHandlerContext.channel().close();
                    break;
                }
                channelHandlerContext.flush();
            }
        }
    }

    private void receivedConnect(NetData.JoinMessage message) {
        logger.info("Received Start Join");
        NetClient client = new NetClient(channelHandlerContext.channel(), networkSystem, identity);
        client.setPreferredName(message.getName());
        client.setColor(new Color(message.getColor().getRgba()));
        client.setViewDistanceMode(ViewDistance.forIndex(message.getViewDistanceLevel()));
        channelHandlerContext.pipeline().remove(this);
        serverHandler.connectionComplete(client);
    }

}
