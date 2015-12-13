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

import com.google.common.collect.Sets;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.engine.EngineTime;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.Time;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.paths.PathManager;
import org.terasology.module.ModuleLoader;
import org.terasology.naming.Name;
import org.terasology.naming.Version;
import org.terasology.network.JoinStatus;
import org.terasology.protobuf.NetData;
import org.terasology.registry.CoreRegistry;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Set;

/**
 */
public class ClientConnectionHandler extends SimpleChannelUpstreamHandler {

    private static final Logger logger = LoggerFactory.getLogger(ClientConnectionHandler.class);

    private final JoinStatusImpl joinStatus;
    private NetworkSystemImpl networkSystem;
    private ServerImpl server;
    private ModuleManager moduleManager;

    private Set<String> missingModules = Sets.newHashSet();
    private NetData.ModuleDataHeader receivingModule;
    private Path tempModuleLocation;
    private BufferedOutputStream downloadingModule;
    private long lengthReceived;

    public ClientConnectionHandler(JoinStatusImpl joinStatus, NetworkSystemImpl networkSystem) {
        this.networkSystem = networkSystem;
        this.joinStatus = joinStatus;
        this.moduleManager = CoreRegistry.get(ModuleManager.class);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        NetData.NetMessage message = (NetData.NetMessage) e.getMessage();
        if (message.hasServerInfo()) {
            receivedServerInfo(ctx, message.getServerInfo());
        } else if (message.hasModuleDataHeader()) {
            receiveModuleStart(ctx, message.getModuleDataHeader());
        } else if (message.hasModuleData()) {
            receiveModule(ctx, message.getModuleData());
        } else if (message.hasJoinComplete()) {
            completeJoin(ctx, message.getJoinComplete());
        } else {
            logger.error("Received unexpected message");
        }
    }

    private void receiveModuleStart(ChannelHandlerContext channelHandlerContext, NetData.ModuleDataHeader moduleDataHeader) {
        if (receivingModule != null) {
            joinStatus.setErrorMessage("Module download error");
            channelHandlerContext.getChannel().close();
            return;
        }
        String moduleId = moduleDataHeader.getId();
        if (missingModules.remove(moduleId.toLowerCase(Locale.ENGLISH))) {
            if (moduleDataHeader.hasError()) {
                joinStatus.setErrorMessage("Module download error: " + moduleDataHeader.getError());
                channelHandlerContext.getChannel().close();
            } else {
                String sizeString = getSizeString(moduleDataHeader.getSize());
                joinStatus.setCurrentActivity("Downloading " + moduleDataHeader.getId() + ":" + moduleDataHeader.getVersion() + " (" + sizeString + ")");
                receivingModule = moduleDataHeader;
                lengthReceived = 0;
                try {
                    tempModuleLocation = Files.createTempFile("terasologyDownload", ".tmp");
                    tempModuleLocation.toFile().deleteOnExit();
                    downloadingModule = new BufferedOutputStream(Files.newOutputStream(tempModuleLocation, StandardOpenOption.WRITE));
                } catch (IOException e) {
                    logger.error("Failed to write received module", e);
                    joinStatus.setErrorMessage("Module download error");
                    channelHandlerContext.getChannel().close();
                }
            }
        } else {
            logger.error("Received unwanted module {}:{} from server", moduleDataHeader.getId(), moduleDataHeader.getVersion());
            joinStatus.setErrorMessage("Module download error");
            channelHandlerContext.getChannel().close();
        }
    }

    private String getSizeString(long size) {
        if (size < 1024) {
            return size + " bytes";
        } else if (size < 1048576) {
            return String.format("%.2f KB", (float) size / 1024);
        } else {
            return String.format("%.2f MB", (float) size / 1048576);
        }
    }

    private void receiveModule(ChannelHandlerContext channelHandlerContext, NetData.ModuleData moduleData) {
        if (receivingModule == null) {
            joinStatus.setErrorMessage("Module download error");
            channelHandlerContext.getChannel().close();
            return;
        }

        try {
            downloadingModule.write(moduleData.getModule().toByteArray());
            lengthReceived += moduleData.getModule().size();
            joinStatus.setCurrentProgress((float) lengthReceived / receivingModule.getSize());
            if (lengthReceived == receivingModule.getSize()) {
                // finished
                downloadingModule.close();
                String moduleName = String.format("%s-%s.jar", receivingModule.getId(), receivingModule.getVersion());
                Path finalPath = PathManager.getInstance().getHomeModPath().normalize().resolve(moduleName);
                if (finalPath.normalize().startsWith(PathManager.getInstance().getHomeModPath())) {
                    if (Files.exists(finalPath)) {
                        logger.error("File already exists at {}", finalPath);
                        joinStatus.setErrorMessage("Module download error");
                        channelHandlerContext.getChannel().close();
                        return;
                    }

                    Files.copy(tempModuleLocation, finalPath);
                    ModuleLoader loader = new ModuleLoader(moduleManager.getModuleMetadataReader());
                    loader.setModuleInfoPath(TerasologyConstants.MODULE_INFO_FILENAME);

                    moduleManager.getRegistry().add(loader.load(finalPath));
                    receivingModule = null;

                    if (missingModules.isEmpty()) {
                        sendJoin(channelHandlerContext);
                    }
                } else {
                    logger.error("Module rejected");
                    joinStatus.setErrorMessage("Module download error");
                    channelHandlerContext.getChannel().close();
                }
            }
        } catch (IOException e) {
            logger.error("Error saving module", e);
            joinStatus.setErrorMessage("Module download error");
            channelHandlerContext.getChannel().close();
        }
    }

    private void completeJoin(ChannelHandlerContext channelHandlerContext, NetData.JoinCompleteMessage joinComplete) {
        logger.info("Join complete received");
        server.setClientId(joinComplete.getClientId());

        channelHandlerContext.getPipeline().remove(this);
        channelHandlerContext.getPipeline().get(ClientHandler.class).joinComplete(server);
        joinStatus.setComplete();
    }

    private void receivedServerInfo(ChannelHandlerContext channelHandlerContext, NetData.ServerInfoMessage message) {
        logger.info("Received server info");
        ((EngineTime) CoreRegistry.get(Time.class)).setGameTime(message.getTime());
        this.server = new ServerImpl(networkSystem, channelHandlerContext.getChannel());
        server.setServerInfo(message);

        // Request missing modules
        for (NetData.ModuleInfo info : message.getModuleList()) {
            if (null == moduleManager.getRegistry().getModule(new Name(info.getModuleId()), new Version(info.getModuleVersion()))) {
                missingModules.add(info.getModuleId().toLowerCase(Locale.ENGLISH));
            }
        }

        if (missingModules.isEmpty()) {
            joinStatus.setCurrentActivity("Finalizing join");
            sendJoin(channelHandlerContext);
        } else {
            joinStatus.setCurrentActivity("Requesting missing modules");
            NetData.NetMessage.Builder builder = NetData.NetMessage.newBuilder();
            for (String module : missingModules) {
                builder.addModuleRequest(NetData.ModuleRequest.newBuilder().setModuleId(module));
            }
            channelHandlerContext.getChannel().write(builder.build());
        }
    }

    private void sendJoin(ChannelHandlerContext channelHandlerContext) {
        Config config = CoreRegistry.get(Config.class);
        NetData.JoinMessage.Builder bldr = NetData.JoinMessage.newBuilder();
        NetData.Color.Builder clrbldr = NetData.Color.newBuilder();

        bldr.setName(config.getPlayer().getName());
        bldr.setViewDistanceLevel(config.getRendering().getViewDistance().getIndex());
        bldr.setColor(clrbldr.setRgba(config.getPlayer().getColor().rgba()).build());

        channelHandlerContext.getChannel().write(NetData.NetMessage.newBuilder().setJoin(bldr).build());
    }

    public JoinStatus getJoinStatus() {
        return joinStatus;
    }
}
