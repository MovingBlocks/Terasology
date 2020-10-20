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
import org.jboss.netty.handler.codec.compression.ZlibEncoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.terasology.config.Config;
import org.terasology.engine.Time;
import org.terasology.engine.module.ModuleManager;
import org.terasology.logic.characters.PredictionSystem;
import org.terasology.network.internal.MetricRecordingHandler;
import org.terasology.network.internal.NetworkSystemImpl;
import org.terasology.network.internal.ServerConnectionHandler;
import org.terasology.network.internal.ServerHandler;
import org.terasology.network.internal.ServerHandshakeHandler;
import org.terasology.protobuf.NetData;
import org.terasology.world.WorldProvider;

import static org.jboss.netty.channel.Channels.pipeline;

/**
 * Netty Pipeline for the server
 *
 */
public class TerasologyServerPipelineFactory implements ChannelPipelineFactory {

    private final NetworkSystemImpl networkSystem;
    private final Config config;
    private final ModuleManager moduleManager;
    private final Time time;
    private final WorldProvider worldProvider;
    private final PredictionSystem predictionSystem;
    

    public TerasologyServerPipelineFactory(NetworkSystemImpl networkSystem, Config config, ModuleManager moduleManager, Time time, WorldProvider worldProvider, PredictionSystem predictionSystem) {
        this.networkSystem = networkSystem;
        this.config = config;
        this.moduleManager = moduleManager;
        this.time = time;
        this.worldProvider = worldProvider;
        this.predictionSystem = predictionSystem;
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline p = pipeline();
        p.addLast(MetricRecordingHandler.NAME, new MetricRecordingHandler());

        p.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
        p.addLast("protobufDecoder", new ProtobufDecoder(NetData.NetMessage.getDefaultInstance()));

        p.addLast("frameLengthEncoder", new LengthFieldPrepender(3));
        p.addLast("deflateEncoder", new ZlibEncoder());
        p.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
        p.addLast("protobufEncoder", new ProtobufEncoder());

        p.addLast("authenticationHandler", new ServerHandshakeHandler(config));
        p.addLast("connectionHandler", new ServerConnectionHandler(moduleManager, time, worldProvider, predictionSystem, networkSystem));
        p.addLast("handler", new ServerHandler(networkSystem));
        return p;
    }
}
