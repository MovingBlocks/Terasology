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

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.compression.JdkZlibEncoder;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.terasology.network.internal.MetricRecordingHandler;
import org.terasology.network.internal.NetworkSystemImpl;
import org.terasology.network.internal.ServerConnectionHandler;
import org.terasology.network.internal.ServerHandler;
import org.terasology.network.internal.ServerHandshakeHandler;
import org.terasology.protobuf.NetData;

/**
 * Netty Pipeline for the server
 */
public class TerasologyServerPipelineFactory extends ChannelInitializer {

    private NetworkSystemImpl networkSystem;

    public TerasologyServerPipelineFactory(NetworkSystemImpl networkSystem) {
        this.networkSystem = networkSystem;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        p.addLast(MetricRecordingHandler.NAME, new MetricRecordingHandler());

        p.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
        p.addLast("protobufDecoder", new ProtobufDecoder(NetData.NetMessage.getDefaultInstance()));

        p.addLast("frameLengthEncoder", new LengthFieldPrepender(3));
        p.addLast("deflateEncoder", new JdkZlibEncoder());
        p.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
        p.addLast("protobufEncoder", new ProtobufEncoder());

        p.addLast("authenticationHandler", new ServerHandshakeHandler());
        p.addLast("connectionHandler", new ServerConnectionHandler(networkSystem));
        p.addLast("handler", new ServerHandler(networkSystem));
    }
}
