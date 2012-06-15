/*
 * Copyright 2012
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

package org.terasology.network;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Immortius
 */
public class Client {
    private Logger logger = Logger.getLogger(getClass().getName());

    private ChannelFactory factory;
    private Channel clientChannel;

    public void join(String host, int port) {
        factory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
        ClientBootstrap bootstrap = new ClientBootstrap(factory);
        bootstrap.setPipelineFactory(new TerasologyClientPipelineFactory());
        bootstrap.setOption("tcpNoDelay", true);
        bootstrap.setOption("keepAlive", true);
        ChannelFuture connectCheck = bootstrap.connect(new InetSocketAddress(host, port));
        connectCheck.awaitUninterruptibly();
        if (!connectCheck.isSuccess()) {
            logger.log(Level.SEVERE, "Failed to connect to server", connectCheck.getCause());
            connectCheck.getChannel().getCloseFuture().awaitUninterruptibly();
            factory.releaseExternalResources();
        }
        clientChannel = connectCheck.getChannel();
    }

    public void disconnect() {
        clientChannel.close().awaitUninterruptibly();
        factory.releaseExternalResources();
    }

}
