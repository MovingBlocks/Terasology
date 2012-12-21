package org.terasology.network;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.protobuf.NetData;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.remoteChunkProvider.RemoteChunkProvider;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;

/**
 * @author Immortius
 */
public class NetworkSystem {
    private static final Logger logger = LoggerFactory.getLogger(NetworkSystem.class);

    private NetworkMode mode = NetworkMode.NONE;
    private Set<ClientPlayer> clientPlayerList = Sets.newLinkedHashSet();

    // Shared
    private ChannelFactory factory;

    // Server only
    private ChannelGroup allChannels = new DefaultChannelGroup("tera-server");
    private NetData.ServerInfoMessage serverInfo;
    private RemoteChunkProvider remoteWorldProvider;
    private BlockingQueue<Chunk> chunkQueue = Queues.newLinkedBlockingQueue();

    public NetworkSystem() {
    }

    public void host(int port) {
        if (mode == NetworkMode.NONE) {
            factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
            ServerBootstrap bootstrap = new ServerBootstrap(factory);
            bootstrap.setPipelineFactory(new TerasologyServerPipelineFactory(this));
            bootstrap.setOption("child.tcpNoDelay", true);
            bootstrap.setOption("child.keepAlive", true);
            Channel listenChannel = bootstrap.bind(new InetSocketAddress(port));
            allChannels.add(listenChannel);
            logger.info("Started server");
            mode = NetworkMode.SERVER;
        }
    }

    public boolean join(String address, int port) {
        if (mode == NetworkMode.NONE) {
            factory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
            ClientBootstrap bootstrap = new ClientBootstrap(factory);
            bootstrap.setPipelineFactory(new TerasologyClientPipelineFactory(this));
            bootstrap.setOption("tcpNoDelay", true);
            bootstrap.setOption("keepAlive", true);
            ChannelFuture connectCheck = bootstrap.connect(new InetSocketAddress(address, port));
            connectCheck.awaitUninterruptibly();
            if (!connectCheck.isSuccess()) {
                logger.warn("Failed to connect to server", connectCheck.getCause());
                connectCheck.getChannel().getCloseFuture().awaitUninterruptibly();
                factory.releaseExternalResources();
                return false;
            } else {
                allChannels.add(connectCheck.getChannel());
                logger.info("Connected to server");
                mode = NetworkMode.CLIENT;
                return true;
            }
        }
        return false;
    }

    public void send(NetData data) {
        allChannels.write(data);
    }

    public void sendChatMessage(String message) {
        NetData.NetMessage data = NetData.NetMessage.newBuilder()
                .setType(NetData.NetMessage.Type.CONSOLE)
                .setConsole(NetData.ConsoleMessage.newBuilder().setMessage(message))
                .build();
        allChannels.write(data);
    }

    public void shutdown() {
        if (mode != NetworkMode.NONE) {
            allChannels.close().awaitUninterruptibly();
            factory.releaseExternalResources();
            logger.info("Network shutdown");
            mode = NetworkMode.NONE;
            serverInfo = null;
            remoteWorldProvider = null;
        }
    }

    public void update() {
        if (mode != NetworkMode.NONE) {
            if (remoteWorldProvider != null) {
                List<Chunk> chunks = Lists.newArrayListWithExpectedSize(chunkQueue.size());
                chunkQueue.drainTo(chunks);
                for (Chunk chunk : chunks) {
                    remoteWorldProvider.receiveChunk(chunk);
                }
            }
        }
    }

    public NetworkMode getMode() {
        return mode;
    }

    void registerChannel(Channel channel) {
        allChannels.add(channel);
    }

    void addClient(ClientPlayer client) {
        synchronized (clientPlayerList) {
            clientPlayerList.add(client);
        }
    }

    void removeClient(ClientPlayer client) {
        synchronized (clientPlayerList) {
            clientPlayerList.remove(client);
        }
    }

    void receiveChunk(Chunk chunk) {
        chunkQueue.offer(chunk);
    }

    public NetData.ServerInfoMessage getServerInfo() {
        return serverInfo;
    }

    public void setServerInfo(NetData.ServerInfoMessage serverInfo) {
        this.serverInfo = serverInfo;
    }


    public void setRemoteWorldProvider(RemoteChunkProvider remoteWorldProvider) {
        this.remoteWorldProvider = remoteWorldProvider;
    }

    public RemoteChunkProvider getRemoteWorldProvider() {
        return remoteWorldProvider;
    }


}
