// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.terasology.engine.network.internal.ServerInfoRequestHandler;
import org.terasology.engine.network.internal.pipelineFactory.InfoRequestPipelineFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Performs temporary connections to one or more game servers.
 */
public class ServerInfoService implements AutoCloseable {

    private final ClientBootstrap bootstrap;
    private final NioClientSocketChannelFactory factory;
    private final ExecutorService pool;

    public ServerInfoService() {
        pool = Executors.newCachedThreadPool();
        factory = new NioClientSocketChannelFactory(pool, pool, 1, 1);
        bootstrap = new ClientBootstrap(factory);
        bootstrap.setPipelineFactory(new InfoRequestPipelineFactory());
        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("keepAlive", true);
    }

    public Future<ServerInfoMessage> requestInfo(final String address, final int port) {
        return pool.submit(() -> {
            InetSocketAddress remoteAddress = new InetSocketAddress(address, port);
            ChannelFuture connectCheck = bootstrap.connect(remoteAddress);
            connectCheck.syncUninterruptibly();
            Channel channel = connectCheck.getChannel();
            channel.getCloseFuture().syncUninterruptibly();

            ServerInfoRequestHandler handler = channel.getPipeline().get(ServerInfoRequestHandler.class);
            ServerInfoMessage serverInfo = handler.getServerInfo();
            return serverInfo;

        });
    }

    @Override
    public void close() {
        factory.releaseExternalResources();
        pool.shutdown();
    }
}
