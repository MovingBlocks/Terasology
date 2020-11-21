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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.terasology.network.NetMetricSource;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A generic Netty handler for recording metrics on sent and received bytes and messages.
 */
public class MetricRecordingHandler extends ChannelDuplexHandler implements NetMetricSource {

    public static final String NAME = "metrics";

    private AtomicInteger receivedMessages = new AtomicInteger();
    private AtomicInteger receivedBytes = new AtomicInteger();
    private AtomicInteger sentMessages = new AtomicInteger();
    private AtomicInteger sentBytes = new AtomicInteger();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        receivedMessages.incrementAndGet();
        receivedBytes.addAndGet(buf.readableBytes());
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        sentMessages.incrementAndGet();
        sentBytes.addAndGet(buf.readableBytes());
        super.write(ctx, msg, promise);
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
