// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.network.internal;

import com.google.common.collect.Sets;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.PlayerConfig;
import org.terasology.engine.context.Context;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.core.EngineTime;
import org.terasology.engine.core.PathManager;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.network.JoinStatus;
import org.terasology.gestalt.naming.Name;
import org.terasology.gestalt.naming.Version;
import org.terasology.protobuf.NetData;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;

import static org.terasology.engine.registry.InjectionHelper.createWithConstructorInjection;

public class ClientConnectionHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ClientConnectionHandler.class);
    private static final long TIMEOUT_THRESHOLD = 120000;

    private final Config config;
    private final Context context;
    private final JoinStatusImpl joinStatus;
    private final ModuleManager moduleManager;
    private final PlayerConfig playerConfig;
    private final EngineTime time;

    private ServerImpl server;
    private Set<String> missingModules = Sets.newHashSet();
    private NetData.ModuleDataHeader receivingModule;
    private Path tempModuleLocation;
    private BufferedOutputStream downloadingModule;
    private long lengthReceived;
    private Timer timeoutTimer = new Timer("Netty-Timeout-Timer", true);
    private long timeoutPoint = System.currentTimeMillis();
    private Channel channel;

    /**
     * Initialises: network system, join status, and module manager.
     */
    public ClientConnectionHandler(JoinStatusImpl joinStatus, Config config, Context parentContext, ModuleManager moduleManager,
                                   PlayerConfig playerConfig, EngineTime time) {
        this.config = config;
        this.context = new ContextImpl(parentContext);
        this.moduleManager = moduleManager;
        this.playerConfig = playerConfig;
        this.time = time;
        this.joinStatus = joinStatus;
        // TODO: implement translation of errorMessage in messageReceived once context is available
        // See https://github.com/MovingBlocks/Terasology/pull/3332#discussion_r187081375
    }

    /**
     * Sets timeout threshold, if client exceeds this time during connection it will automatically close the channel.
     * @param inputChannel Socket for connections to allow I/O.
     */
    private void scheduleTimeout(Channel inputChannel) {
        channel = inputChannel;
        timeoutPoint = System.currentTimeMillis() + TIMEOUT_THRESHOLD;
        timeoutTimer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                synchronized (joinStatus) {
                    if (System.currentTimeMillis() > timeoutPoint
                            && joinStatus.getStatus() != JoinStatus.Status.COMPLETE
                            && joinStatus.getStatus() != JoinStatus.Status.FAILED) {
                        joinStatus.setErrorMessage("Server stopped responding.");
                        channel.close();
                        logger.error("Server timeout threshold of {} ms exceeded.", TIMEOUT_THRESHOLD);
                    }
                }
                Thread.currentThread().stop();
            }
        }, TIMEOUT_THRESHOLD + 200);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        context.put(Channel.class, ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // If we timed out, don't handle anymore messages.
        if (joinStatus.getStatus() == JoinStatus.Status.FAILED) {
            return;
        }
        scheduleTimeout(ctx.channel());

        // Handle message
        NetData.NetMessage message = (NetData.NetMessage) msg;
        String errorMessage = message.getServerInfo().getErrorMessage();
        if (errorMessage != null && !errorMessage.isEmpty()) {
            synchronized (joinStatus) {
                joinStatus.setErrorMessage(errorMessage);
                ctx.channel().close();
                return;
            }
        }

        synchronized (joinStatus) {
            timeoutPoint = System.currentTimeMillis() + TIMEOUT_THRESHOLD;
            if (message.hasServerInfo()) {
                receivedServerInfo(ctx, message.getServerInfo());
            } else if (message.hasModuleDataHeader()) {
                receiveModuleStart(ctx, message.getModuleDataHeader());
            } else if (message.hasModuleData()) {
                receiveModule(ctx, message.getModuleData());
            } else if (message.hasJoinComplete()) {
                if (missingModules.size() > 0) {
                    logger.error(
                            "The server did not send all of the modules that were needed before ending module transmission.");
                }
                completeJoin(ctx, message.getJoinComplete());
            } else {
                logger.error("Received unexpected message");
            }
        }
    }

    /**
     * Attempts to receive a module from the server and push it to the client.
     * Creates a file on the target machine and begins preparation to write to it.
     * @param channelHandlerContext
     * @param moduleDataHeader
     */
    private void receiveModuleStart(ChannelHandlerContext channelHandlerContext,
            NetData.ModuleDataHeader moduleDataHeader) {
        if (receivingModule != null) {
            joinStatus.setErrorMessage("Module download error");
            channelHandlerContext.channel().close();
            return;
        }
        String moduleId = moduleDataHeader.getId();
        String moduleVersion = moduleDataHeader.getVersion();
        if (missingModules.remove(moduleId.toLowerCase(Locale.ENGLISH))) {
            if (moduleDataHeader.hasError()) {
                joinStatus.setErrorMessage("Module download error: " + moduleDataHeader.getError());
                channelHandlerContext.channel().close();
            } else {
                String sizeString = getSizeString(moduleDataHeader.getSize());
                int numOfMissingModules = missingModules.size();
                joinStatus.setCurrentActivity(
                        "Downloading " + moduleDataHeader.getId() + ":" + moduleVersion
                                + " (" + sizeString + "," + numOfMissingModules + " modules remain)");
                logger.info("Downloading {}: {} ({}, {} modules remain)", moduleId, moduleVersion, sizeString, numOfMissingModules);
                receivingModule = moduleDataHeader;
                lengthReceived = 0;
                try {
                    tempModuleLocation = Files.createTempFile("terasologyDownload", ".tmp");
                    tempModuleLocation.toFile().deleteOnExit();
                    downloadingModule = new BufferedOutputStream(
                            Files.newOutputStream(tempModuleLocation, StandardOpenOption.WRITE));
                } catch (IOException e) {
                    logger.error("Failed to write received module", e);
                    joinStatus.setErrorMessage("Module download error");
                    channelHandlerContext.channel().close();
                }
            }
        } else {
            logger.error("Received unwanted module {}:{} from server", moduleId, moduleVersion);
            joinStatus.setErrorMessage("Module download error");
            channelHandlerContext.channel().close();
        }
    }

    /**
     * Converts file size to a string in either bytes, KB, or MB. Dependant on the files size.
     * @param size Size of the file.
     * @return String of the file size in either bytes or KB or MB.
     */

    private String getSizeString(long size) {
        if (size < 1024) {
            return size + " bytes";
        } else if (size < 1048576) {
            return String.format("%.2f KB", (float) size / 1024);
        } else {
            return String.format("%.2f MB", (float) size / 1048576);
        }
    }

    /**
     * Converts the modules data to a byte array and writes it to a file, which then is copied from the temp directory to the correct directory.
     * @param channelHandlerContext
     * @param moduleData The data of the module.
     */
    private void receiveModule(ChannelHandlerContext channelHandlerContext, NetData.ModuleData moduleData) {
        if (receivingModule == null) {
            joinStatus.setErrorMessage("Module download error");
            channelHandlerContext.channel().close();
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
                        channelHandlerContext.channel().close();
                        return;
                    }

                    Files.copy(tempModuleLocation, finalPath);
                    moduleManager.registerArchiveModule(finalPath);

                    receivingModule = null;

                    if (missingModules.isEmpty()) {
                        sendJoin(channelHandlerContext);
                    }
                } else {
                    logger.error("Module rejected");
                    joinStatus.setErrorMessage("Module download error");
                    channelHandlerContext.channel().close();
                }
            }
        } catch (IOException e) {
            logger.error("Error saving module", e);
            joinStatus.setErrorMessage("Module download error");
            channelHandlerContext.channel().close();
        }
    }

    /**
     * Passes the join complete message to the client, and marks the entities joining as successful.
     * @param channelHandlerContext
     * @param joinComplete
     */
    private void completeJoin(ChannelHandlerContext channelHandlerContext, NetData.JoinCompleteMessage joinComplete) {
        logger.info("Join complete received");
        server.setClientId(joinComplete.getClientId());

        channelHandlerContext.pipeline().remove(this);
        channelHandlerContext.pipeline().get(ClientHandler.class).joinComplete(server);
        joinStatus.setComplete();
    }

    /**
     * Gets the server information and passes it to the client, while also checking if all required modules have been downloaded.
     * @param channelHandlerContext
     * @param message Passes the server information message to the function.
     */
    private void receivedServerInfo(ChannelHandlerContext channelHandlerContext, NetData.ServerInfoMessage message) {
        logger.info("Received server info");
        time.setGameTime(message.getTime());
        this.server = createWithConstructorInjection(ServerImpl.class, context);
        server.setServerInfo(message);

        // Request missing modules
        for (NetData.ModuleInfo info : message.getModuleList()) {
            if (null == moduleManager.getRegistry().getModule(new Name(info.getModuleId()),
                    new Version(info.getModuleVersion()))) {
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
            channelHandlerContext.channel().writeAndFlush(builder.build());
        }
    }

    /**
     * Sends a join request from the client upstream to the server.
     * @param channelHandlerContext
     */
    private void sendJoin(ChannelHandlerContext channelHandlerContext) {
        NetData.JoinMessage.Builder bldr = NetData.JoinMessage.newBuilder();
        NetData.Color.Builder clrbldr = NetData.Color.newBuilder();

        bldr.setName(playerConfig.playerName.get());
        bldr.setViewDistanceLevel(config.getRendering().getViewDistance().getIndex());
        bldr.setColor(clrbldr.setRgba(playerConfig.color.get().rgba()).build());

        channelHandlerContext.channel().writeAndFlush(NetData.NetMessage.newBuilder().setJoin(bldr).build());
    }

    /**
     * Gets the clients Join Status
     * @return Returns join status.
     */
    public JoinStatus getJoinStatus() {
        synchronized (joinStatus) {
            return joinStatus;
        }
    }
}
