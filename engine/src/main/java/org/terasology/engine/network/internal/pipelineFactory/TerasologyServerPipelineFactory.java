// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network.internal.pipelineFactory;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.compression.ZlibEncoder;
import org.jboss.netty.handler.codec.frame.LengthFieldPrepender;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.terasology.engine.network.internal.MetricRecordingHandler;
import org.terasology.engine.network.internal.NetworkSystemImpl;
import org.terasology.engine.network.internal.ServerConnectionHandler;
import org.terasology.engine.network.internal.ServerHandler;
import org.terasology.engine.network.internal.ServerHandshakeHandler;
import org.terasology.engine.protobuf.NetData;

import static org.jboss.netty.channel.Channels.pipeline;

/**
 * Netty Pipeline for the server
 *
 */
public class TerasologyServerPipelineFactory implements ChannelPipelineFactory {

    private final NetworkSystemImpl networkSystem;

    public TerasologyServerPipelineFactory(NetworkSystemImpl networkSystem) {
        this.networkSystem = networkSystem;
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

        p.addLast("authenticationHandler", new ServerHandshakeHandler());
        p.addLast("connectionHandler", new ServerConnectionHandler(networkSystem));
        p.addLast("handler", new ServerHandler(networkSystem));
        return p;
    }
}
