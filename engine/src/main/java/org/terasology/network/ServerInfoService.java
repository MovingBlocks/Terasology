// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

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
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
    }

    public Future<ServerInfoMessage> requestInfo(final String address, final int port) {
        SettableFuture<ServerInfoMessage> resultFuture = SettableFuture.create();
        InetSocketAddress remoteAddress = new InetSocketAddress(address, port);
        ChannelFuture connectCheck = bootstrap.connect(remoteAddress)
                .addListener(connectFuture -> {
                    if (!connectFuture.isSuccess()) {
                        if (connectFuture.cause() != null && connectFuture.cause().getCause() != null) {
                            // java's network exception.
                            resultFuture.setException(connectFuture.cause().getCause());
                        } else if (connectFuture.cause() != null) {
                            // netty's exception, if it is not java's
                            resultFuture.setException(connectFuture.cause());
                        } else {
                            // fallback exception when connecting not success.
                            resultFuture.setException(new RuntimeException("Cannot connect to server"));
                        }
                    }
                });
        Channel channel = connectCheck.channel();
        channel.closeFuture().addListener(channelFuture -> {
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
