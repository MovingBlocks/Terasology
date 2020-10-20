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

package org.terasology.network.internal.pipelineFactory;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.compression.ZlibDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.terasology.config.Config;
import org.terasology.engine.EngineTime;
import org.terasology.engine.GameEngine;
import org.terasology.engine.module.ModuleManager;
import org.terasology.identity.storageServiceClient.StorageServiceWorker;
import org.terasology.network.internal.ClientConnectionHandler;
import org.terasology.network.internal.ClientHandler;
import org.terasology.network.internal.ClientHandshakeHandler;
import org.terasology.network.internal.JoinStatusImpl;
import org.terasology.network.internal.MetricRecordingHandler;
import org.terasology.network.internal.NetworkSystemImpl;
import org.terasology.protobuf.NetData;

import static org.jboss.netty.channel.Channels.pipeline;

/**
 * Netty pipeline for Clients
 *
 */
public class TerasologyClientPipelineFactory implements ChannelPipelineFactory {

    private final Config config;
    private final GameEngine gameEngine;
    private final EngineTime engineTime;
    private final ModuleManager moduleManager;
    private final StorageServiceWorker storageServiceWorker;
    private final NetworkSystemImpl networkSystem;

    public TerasologyClientPipelineFactory(NetworkSystemImpl networkSystem, Config config,
                                           GameEngine gameEngine, EngineTime engineTime, ModuleManager moduleManager,
                                           StorageServiceWorker storageServiceWorker) {
        this.networkSystem = networkSystem;
        this.config = config;
        this.gameEngine = gameEngine;
        this.engineTime = engineTime;
        this.moduleManager = moduleManager;
        this.storageServiceWorker = storageServiceWorker;
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        JoinStatusImpl joinStatus = new JoinStatusImpl();
        ChannelPipeline p = pipeline();
        p.addLast(MetricRecordingHandler.NAME, new MetricRecordingHandler());

        p.addLast("lengthFrameDecoder", new LengthFieldBasedFrameDecoder(8388608, 0, 3, 0, 3));
        p.addLast("inflateDecoder", new ZlibDecoder());
        p.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
        p.addLast("protobufDecoder", new ProtobufDecoder(NetData.NetMessage.getDefaultInstance()));

        p.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
        p.addLast("protobufEncoder", new ProtobufEncoder());
        p.addLast("authenticationHandler", new ClientHandshakeHandler(config, storageServiceWorker, joinStatus));
        p.addLast("connectionHandler", new ClientConnectionHandler(
                joinStatus, networkSystem, moduleManager, config, engineTime));
        p.addLast("handler", new ClientHandler(networkSystem, gameEngine));
        return p;
    }
}
