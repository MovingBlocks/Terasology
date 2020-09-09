// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.network.internal;

import com.google.protobuf.ByteString;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.resources.ArchiveFileSource;
import org.terasology.gestalt.module.resources.FileReference;
import org.terasology.gestalt.naming.Name;
import org.terasology.engine.identity.PublicIdentityCertificate;
import org.terasology.nui.Color;
import org.terasology.engine.protobuf.NetData;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.rendering.world.viewDistance.ViewDistance;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 */
public class ServerConnectionHandler extends SimpleChannelUpstreamHandler {

    private static final Logger logger = LoggerFactory.getLogger(ServerConnectionHandler.class);

    private final NetworkSystemImpl networkSystem;
    private ServerHandler serverHandler;
    private ChannelHandlerContext channelHandlerContext;

    private PublicIdentityCertificate identity;

    private final ModuleManager moduleManager = CoreRegistry.get(ModuleManager.class);

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
            if (!(module.getResources() instanceof ArchiveFileSource)) { //TODO: gestaltv7 restore module downloading for maximum possibles
                result.setError("Module not available for download (gestaltv7 reintegrate - archive modules only)");
            } else {
                FileReference fileReference = module.getResources().getFiles().iterator().next();
                try (InputStream stream = fileReference.open()) {
                    ByteString byteString = ByteString.readFrom(stream,1024);
                    channelHandlerContext.getChannel().write(
                            NetData.NetMessage.newBuilder().setModuleData(
                                    NetData.ModuleData.newBuilder().setModule(byteString)
                            ).build()
                    );
                    result.setVersion(module.getVersion().toString());
                    result.setSize(byteString.size());
                    channelHandlerContext.getChannel().write(NetData.NetMessage.newBuilder().setModuleDataHeader(result).build());
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
