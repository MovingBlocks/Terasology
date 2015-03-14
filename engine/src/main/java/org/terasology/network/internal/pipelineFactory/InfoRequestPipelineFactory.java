/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.network.internal.pipelineFactory;

import static org.jboss.netty.channel.Channels.pipeline;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.compression.ZlibDecoder;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.network.internal.ClientConnectionHandler;
import org.terasology.network.internal.ClientHandshakeHandler;
import org.terasology.network.internal.JoinStatusImpl;
import org.terasology.network.internal.MetricRecordingHandler;
import org.terasology.protobuf.NetData;
import org.terasology.protobuf.NetData.ServerInfoMessage;

public class InfoRequestPipelineFactory implements ChannelPipelineFactory {

    private static final Logger logger = LoggerFactory.getLogger(InfoRequestPipelineFactory.class);

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
        p.addLast("authenticationHandler", new ClientHandshakeHandler(joinStatus));
        p.addLast("connectionHandler", new ClientConnectionHandler(joinStatus, null) {

            @Override
            public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
                NetData.NetMessage message = (NetData.NetMessage) e.getMessage();
                if (message.hasServerInfo()) {
                    receivedServerInfo2(message.getServerInfo());
                } else {
                    logger.error("Received unexpected message");
                }
                ctx.getChannel().close();
            }

            private void receivedServerInfo2(ServerInfoMessage serverInfo) {
                System.out.println("RECEIVED INFO: " + serverInfo.toString());
            }

        });
        return p;
    }
}