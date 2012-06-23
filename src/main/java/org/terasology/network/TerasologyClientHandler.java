/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.terasology.protobuf.NetData;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Immortius
 */
public class TerasologyClientHandler extends SimpleChannelUpstreamHandler {

    private Logger logger = Logger.getLogger(getClass().getName());

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        NetData.ServerMessage message = (NetData.ServerMessage) e.getMessage();
        logger.log(Level.INFO, "Received message: " + message.getConnection().getName());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        logger.log(Level.WARNING, "Unexpected exception from client", e.getCause());
        e.getChannel().close();
    }
}
