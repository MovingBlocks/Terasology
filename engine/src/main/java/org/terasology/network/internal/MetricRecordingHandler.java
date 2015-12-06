/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.network.internal;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.terasology.network.NetMetricSource;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A generic Netty handler for recording metrics on sent and received bytes and messages.
 *
 */
public class MetricRecordingHandler extends SimpleChannelHandler implements NetMetricSource {

    public static final String NAME = "metrics";

    private AtomicInteger receivedMessages = new AtomicInteger();
    private AtomicInteger receivedBytes = new AtomicInteger();
    private AtomicInteger sentMessages = new AtomicInteger();
    private AtomicInteger sentBytes = new AtomicInteger();

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        ChannelBuffer buf = (ChannelBuffer) e.getMessage();
        receivedMessages.incrementAndGet();
        receivedBytes.addAndGet(buf.readableBytes());
        ctx.sendUpstream(e);
    }

    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        ChannelBuffer buf = (ChannelBuffer) e.getMessage();
        sentMessages.incrementAndGet();
        sentBytes.addAndGet(buf.readableBytes());
        ctx.sendDownstream(e);
    }

    @Override
    public int getReceivedMessagesSinceLastCall() {
        return receivedMessages.getAndSet(0);
    }

    @Override
    public int getReceivedBytesSinceLastCall() {
        return receivedBytes.getAndSet(0);
    }

    @Override
    public int getSentMessagesSinceLastCall() {
        return sentMessages.getAndSet(0);
    }

    @Override
    public int getSentBytesSinceLastCall() {
        return sentBytes.getAndSet(0);
    }
}
