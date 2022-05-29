// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.network.internal;

import com.google.protobuf.ByteString;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.context.Context;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.identity.PublicIdentityCertificate;
import org.terasology.engine.rendering.world.viewDistance.ViewDistance;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.resources.ArchiveFileSource;
import org.terasology.gestalt.module.resources.FileReference;
import org.terasology.gestalt.naming.Name;
import org.terasology.nui.Color;
import org.terasology.protobuf.NetData;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.terasology.engine.registry.InjectionHelper.createWithConstructorInjection;


public class ServerConnectionHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ServerConnectionHandler.class);

    private final Context context;
    private final ModuleManager moduleManager;
    private final NetworkSystemImpl networkSystem;

    private ServerHandler serverHandler;
    private ChannelHandlerContext channelHandlerContext;

    public ServerConnectionHandler(NetworkSystemImpl networkSystem, Context parentContext, ModuleManager moduleManager) {
        this.networkSystem = networkSystem;
        this.context = new ContextImpl(parentContext);
        this.moduleManager = moduleManager;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.channelHandlerContext = ctx;
        context.put(Channel.class, ctx.channel());
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
        context.put(PublicIdentityCertificate.class, id);
    }

    private void sendModules(List<NetData.ModuleRequest> moduleRequestList) {
        for (NetData.ModuleRequest request : moduleRequestList) {
            NetData.ModuleDataHeader.Builder result = NetData.ModuleDataHeader.newBuilder();
            result.setId(request.getModuleId());
            Module module = moduleManager.getEnvironment().get(new Name(request.getModuleId()));

            if (!(module.getResources() instanceof ArchiveFileSource)) { //TODO: gestaltv7 restore module downloading for maximum possibles
                result.setError("Module not available for download");
            } else {
                FileReference fileReference = module.getResources().getFiles().iterator().next();
                try (InputStream stream = fileReference.open()) {
                    ByteString byteString = ByteString.readFrom(stream, 1024);
                    channelHandlerContext.channel().write(
                            NetData.NetMessage.newBuilder().setModuleData(
                                    NetData.ModuleData.newBuilder().setModule(byteString)
                            ).build()
                    );
                    result.setVersion(module.getVersion().toString());
                    result.setSize(byteString.size());
                    channelHandlerContext.channel().write(NetData.NetMessage.newBuilder().setModuleDataHeader(result).build());
                } catch (IOException e) {
                    logger.error("Error sending module", e);
                    channelHandlerContext.channel().close();
                    break;
                }
            }
        }
    }

    private void receivedConnect(NetData.JoinMessage message) {
        logger.info("Received Start Join");
        NetClient client = createWithConstructorInjection(NetClient.class, context);
        client.setPreferredName(message.getName());
        client.setColor(new Color(message.getColor().getRgba()));
        client.setViewDistanceMode(ViewDistance.forIndex(message.getViewDistanceLevel()));
        channelHandlerContext.pipeline().remove(this);
        serverHandler.connectionComplete(client);
    }
}
