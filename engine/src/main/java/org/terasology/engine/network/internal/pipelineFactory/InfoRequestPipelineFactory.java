// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network.internal.pipelineFactory;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.compression.ZlibDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.terasology.engine.network.internal.ClientHandshakeHandler;
import org.terasology.engine.network.internal.ServerInfoRequestHandler;
import org.terasology.engine.network.internal.JoinStatusImpl;
import org.terasology.engine.network.internal.MetricRecordingHandler;
import org.terasology.engine.protobuf.NetData;

/**
 * A pipeline that requests {@link org.terasology.network.ServerInfoMessage} before it auto-disconnects.
 * This is similar to {@link TerasologyClientPipelineFactory}.
 */
public class InfoRequestPipelineFactory implements ChannelPipelineFactory {

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        JoinStatusImpl joinStatus = new JoinStatusImpl();
        ChannelPipeline p = Channels.pipeline();

        p.addLast(MetricRecordingHandler.NAME, new MetricRecordingHandler());

        p.addLast("lengthFrameDecoder", new LengthFieldBasedFrameDecoder(8388608, 0, 3, 0, 3));
        p.addLast("inflateDecoder", new ZlibDecoder());
        p.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
        p.addLast("protobufDecoder", new ProtobufDecoder(NetData.NetMessage.getDefaultInstance()));

        p.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
        p.addLast("protobufEncoder", new ProtobufEncoder());
        p.addLast("authenticationHandler", new ClientHandshakeHandler(joinStatus));
        p.addLast("connectionHandler", new ServerInfoRequestHandler());

        return p;
    }
}
