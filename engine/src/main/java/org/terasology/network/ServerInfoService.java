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

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.terasology.network.internal.InfoRequestHandler;
import org.terasology.network.internal.pipelineFactory.InfoRequestPipelineFactory;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * Performs temporary connections to one or more game servers.
 * @author Martin Steiger
 */
public class ServerInfoService implements AutoCloseable {

    private final ExecutorService pool;
    private ClientBootstrap bootstrap;
    private NioClientSocketChannelFactory factory;

    public ServerInfoService() {
        pool = Executors.newFixedThreadPool(2);
        factory = new NioClientSocketChannelFactory(pool, pool, 1, 1);
        bootstrap = new ClientBootstrap(factory);
        bootstrap.setPipelineFactory(new InfoRequestPipelineFactory());
        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("keepAlive", true);
    }

    public ListenableFuture<ServerInfoMessage> requestInfo(String address, int port) {
        ChannelFuture connectCheck = bootstrap.connect(new InetSocketAddress(address, port));
        InfoRequestHandler handler = connectCheck.getChannel().getPipeline().get(InfoRequestHandler.class);

        return handler.getServerInfoFuture();
    }

    @Override
    public void close() {
        factory.releaseExternalResources();
    }
}
