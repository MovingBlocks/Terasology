package org.terasology.network;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Immortius
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
