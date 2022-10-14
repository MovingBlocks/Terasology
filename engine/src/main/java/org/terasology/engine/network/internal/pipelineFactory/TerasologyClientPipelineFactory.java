// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network.internal.pipelineFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.compression.Lz4FrameDecoder;
import io.netty.handler.codec.compression.Lz4FrameEncoder;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import org.terasology.engine.context.Context;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.network.internal.ClientConnectionHandler;
import org.terasology.engine.network.internal.ClientHandler;
import org.terasology.engine.network.internal.ClientHandshakeHandler;
import org.terasology.engine.network.internal.JoinStatusImpl;
import org.terasology.engine.network.internal.MetricRecordingHandler;
import org.terasology.protobuf.NetData;

import static org.terasology.engine.registry.InjectionHelper.createWithConstructorInjection;


/**
 * Netty pipeline for Clients
 */
public class TerasologyClientPipelineFactory extends ChannelInitializer<Channel> {

    private final Context parentContext;

    public TerasologyClientPipelineFactory(Context parentContext) {
        this.parentContext = parentContext;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        var context = new ContextImpl(parentContext);
        context.put(JoinStatusImpl.class, new JoinStatusImpl());
        ChannelPipeline p = ch.pipeline();
        p.addLast(MetricRecordingHandler.NAME, new MetricRecordingHandler());

        p.addLast("inflateDecoder", new Lz4FrameDecoder());
        p.addLast("lengthFrameDecoder", new LengthFieldBasedFrameDecoder(8388608, 0, 3, 0, 3));
        p.addLast("protobufDecoder", new ProtobufDecoder(NetData.NetMessage.getDefaultInstance()));

        p.addLast("deflateEncoder", new Lz4FrameEncoder(true));
        p.addLast("frameLengthEncoder", new LengthFieldPrepender(3));
        p.addLast("protobufEncoder", new ProtobufEncoder());

        p.addLast("authenticationHandler", createWithConstructorInjection(ClientHandshakeHandler.class, context));
        p.addLast("connectionHandler", createWithConstructorInjection(ClientConnectionHandler.class, context));
        p.addLast("handler", createWithConstructorInjection(ClientHandler.class, context));
    }
}
