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

package org.terasology.network;

import com.google.common.util.concurrent.SettableFuture;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.terasology.network.internal.ServerInfoRequestHandler;
import org.terasology.network.internal.pipelineFactory.InfoRequestPipelineFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Future;

/**
 * Performs temporary connections to one or more game servers.
 */
public class ServerInfoService implements AutoCloseable {

    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    public ServerInfoService() {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new InfoRequestPipelineFactory());
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
    }

    public Future<ServerInfoMessage> requestInfo(final String address, final int port) {
        SettableFuture<ServerInfoMessage> resultFuture = SettableFuture.create();
        InetSocketAddress remoteAddress = new InetSocketAddress(address, port);
        ChannelFuture connectCheck = bootstrap.connect(remoteAddress);
        connectCheck.syncUninterruptibly();
        Channel channel = connectCheck.channel();
        channel.close().addListener(channelFuture -> {
            if (channelFuture.isSuccess()) {
                ServerInfoRequestHandler handler = channel.pipeline().get(ServerInfoRequestHandler.class);
                resultFuture.set(handler.getServerInfo());
            } else {
                resultFuture.setException(channelFuture.cause());
            }
        });

        return resultFuture;
    }

    @Override
    public void close() {
        eventLoopGroup.shutdownGracefully().syncUninterruptibly();
    }
}
