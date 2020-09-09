// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network.internal;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.terasology.engine.network.NetMetricSource;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A generic Netty handler for recording metrics on sent and received bytes and messages.
 *
 */
public class MetricRecordingHandler extends SimpleChannelHandler implements NetMetricSource {

    public static final String NAME = "metrics";

    private final AtomicInteger receivedMessages = new AtomicInteger();
    private final AtomicInteger receivedBytes = new AtomicInteger();
    private final AtomicInteger sentMessages = new AtomicInteger();
    private final AtomicInteger sentBytes = new AtomicInteger();

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
