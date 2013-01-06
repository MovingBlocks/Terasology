package org.terasology.network;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
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
import org.terasology.entitySystem.EntityChangeSubscriber;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Event;
import org.terasology.entitySystem.PersistableEntityManager;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.entitySystem.metadata.extension.EntityRefTypeHandler;
import org.terasology.entitySystem.persistence.EntitySerializer;
import org.terasology.entitySystem.persistence.EventSerializer;
import org.terasology.game.CoreRegistry;
import org.terasology.network.pipelineFactory.TerasologyClientPipelineFactory;
import org.terasology.network.pipelineFactory.TerasologyServerPipelineFactory;
import org.terasology.protobuf.NetData;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.remoteChunkProvider.RemoteChunkProvider;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;

/**
 * @author Immortius
 */
public class NetworkSystem implements EntityChangeSubscriber {
    private static final Logger logger = LoggerFactory.getLogger(NetworkSystem.class);

    private NetworkMode mode = NetworkMode.NONE;
    private final Set<ClientPlayer> clientPlayerList = Sets.newLinkedHashSet();
    private Map<EntityRef, ClientPlayer> clientPlayerLookup = Maps.newHashMap();

    // Shared
    private PersistableEntityManager entityManager;
    private ChannelFactory factory;
    private TIntIntMap netIdToEntityId = new TIntIntHashMap();
    private BlockingQueue<NetData.NetMessage> queuedMessages = Queues.newLinkedBlockingQueue();
    private BlockingQueue<ClientPlayer> newClients = Queues.newLinkedBlockingQueue();
    private EventSerializer eventSerializer;

    // Server only
    private ChannelGroup allChannels = new DefaultChannelGroup("tera-channels");
    private NetData.ServerInfoMessage serverInfo;
    private int nextNetId = 1;

    // Client only
    private Server server;
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

    public void shutdown() {
        if (mode != NetworkMode.NONE) {
            allChannels.close().awaitUninterruptibly();
            factory.releaseExternalResources();
            logger.info("Network shutdown");
            mode = NetworkMode.NONE;
            server = null;
            serverInfo = null;
            remoteWorldProvider = null;
            nextNetId = 1;
            netIdToEntityId.clear();
            eventSerializer = null;
            entityManager = null;
        }
    }

    public void update() {
        if (mode != NetworkMode.NONE) {
            if (entityManager != null) {
                if (remoteWorldProvider != null) {
                    List<Chunk> chunks = Lists.newArrayListWithExpectedSize(chunkQueue.size());
                    chunkQueue.drainTo(chunks);
                    for (Chunk chunk : chunks) {
                        remoteWorldProvider.receiveChunk(chunk);
                    }
                }
                if (!newClients.isEmpty()) {
                    List<ClientPlayer> newPlayers = Lists.newArrayListWithExpectedSize(newClients.size());
                    newClients.drainTo(newPlayers);
                    for (ClientPlayer client : newPlayers) {
                        processNewClient(client);
                    }
                }
                for (ClientPlayer client : clientPlayerList) {
                    client.update();
                }
                if (server != null) {
                    server.update();
                }
                processMessages();
            }
        }
    }

    private void processMessages() {
        List<NetData.NetMessage> messages = Lists.newArrayListWithExpectedSize(queuedMessages.size());
        queuedMessages.drainTo(messages);
        EntitySerializer serializer = new EntitySerializer(entityManager);
        serializer.setIgnoringEntityId(true);
        EntityRefTypeHandler.setNetworkMode(this);

        for (NetData.NetMessage message : messages) {
            switch (message.getType()) {
                case CREATE_ENTITY:
                    EntityRef newEntity = serializer.deserialize(message.getCreateEntity().getEntity());
                    logger.info("Received new entity: {} with net id {}", newEntity, newEntity.getComponent(NetworkComponent.class).networkId);
                    registerNetworkEntity(newEntity);
                    break;
                case UPDATE_ENTITY:
                    EntityRef currentEntity = getEntity(message.getUpdateEntity().getNetId());
                    serializer.deserializeOnto(currentEntity, message.getUpdateEntity().getEntity());
                    break;
                case REMOVE_ENTITY:
                    int netId = message.getRemoveEntity().getNetId();
                    EntityRef entity = getEntity(netId);
                    unregisterNetworkEntity(entity);
                    entity.destroy();
                    break;
                case EVENT:
                    Event event = eventSerializer.deserialize(message.getEvent().getEvent());
                    EntityRef target = getEntity(message.getEvent().getTargetId());
                    target.send(event);
                    break;
            }
        }

        EntityRefTypeHandler.setEntityManagerMode(entityManager);
    }

    public NetworkMode getMode() {
        return mode;
    }

    public NetData.ServerInfoMessage getServerInfo() {
        return serverInfo;
    }

    public void setServerInfo(NetData.ServerInfoMessage serverInfo) {
        this.serverInfo = serverInfo;
    }

    public Server getServer() {
        return this.server;
    }

    public Iterable<ClientPlayer> getPlayers() {
        return this.clientPlayerList;
    }

    public ClientPlayer getClientFor(EntityRef player) {
        return clientPlayerLookup.get(player);
    }

    public void setRemoteWorldProvider(RemoteChunkProvider remoteWorldProvider) {
        this.remoteWorldProvider = remoteWorldProvider;
    }

    public RemoteChunkProvider getRemoteWorldProvider() {
        return remoteWorldProvider;
    }

    public void registerNetworkEntity(EntityRef entity) {
        if (mode == NetworkMode.NONE) {
            return;
        }

        logger.debug("Registered Network Entity: {}", entity);

        NetworkComponent netComponent = entity.getComponent(NetworkComponent.class);
        if (mode == NetworkMode.SERVER) {
            netComponent.networkId = nextNetId++;
            entity.saveComponent(netComponent);
        }

        netIdToEntityId.put(netComponent.networkId, entity.getId());

        if (mode == NetworkMode.SERVER) {
            switch (netComponent.replicateMode) {
                case OWNER:
                    ClientPlayer clientPlayer = getClientFor(entity);
                    if (clientPlayer != null) {
                        clientPlayer.setNetInitial(netComponent.networkId);
                    }
                    break;
                default:
                    for (ClientPlayer client : clientPlayerList) {
                        // TODO: Relevance Check
                        client.setNetInitial(netComponent.networkId);
                    }
                    break;
            }

        }
    }

    public void unregisterNetworkEntity(EntityRef entity) {
        NetworkComponent netComponent = entity.getComponent(NetworkComponent.class);
        netIdToEntityId.remove(netComponent.networkId);
        if (mode == NetworkMode.SERVER) {
            NetData.NetMessage message = NetData.NetMessage.newBuilder()
                    .setType(NetData.NetMessage.Type.REMOVE_ENTITY)
                    .setRemoveEntity(
                            NetData.RemoveEntityMessage.newBuilder().setNetId(netComponent.networkId).build())
                    .build();
            for (ClientPlayer client : clientPlayerList) {
                client.setNetRemoved(netComponent.networkId, message);
            }
        }
    }

    public void connectToEntitySystem(PersistableEntityManager entityManager, EntitySystemLibrary entitySystemLibrary) {
        if (this.entityManager != null) {
            this.entityManager.unsubscribe(this);
        }
        this.entityManager = entityManager;
        this.entityManager.subscribe(this);

        eventSerializer = new EventSerializer(entitySystemLibrary.getEventLibrary());

        if (server != null) {
            server.setEventSerializer(eventSerializer);
            server.setEntityManager(entityManager);
        }
        for (ClientPlayer client : clientPlayerList) {
            client.setEventSerializer(eventSerializer);
        }
    }


    @Override
    public void onEntityChange(EntityRef entity) {
        NetworkComponent netComp = entity.getComponent(NetworkComponent.class);
        if (netComp != null) {
            for (ClientPlayer client : clientPlayerList) {
                client.setNetDirty(netComp.networkId);
            }
        }
    }

    EntityRef getEntity(int netId) {
        int entityId = netIdToEntityId.get(netId);
        if (entityId != 0) {
            return CoreRegistry.get(EntityManager.class).getEntity(entityId);
        }
        return EntityRef.NULL;
    }

    void registerChannel(Channel channel) {
        allChannels.add(channel);
    }

    void addClient(ClientPlayer client) {
        newClients.offer(client);
    }

    private void processNewClient(ClientPlayer client) {
        logger.info("New client connected: {}", client.getName());
        client.setConnected();
        logger.info("New client entity: {}", client.getEntity());
        clientPlayerList.add(client);
        clientPlayerLookup.put(client.getEntity(), client);
        if (eventSerializer != null) {
            client.setEventSerializer(eventSerializer);
        }
        for (EntityRef netEntity : entityManager.iteratorEntities(NetworkComponent.class)) {
            NetworkComponent netComp = netEntity.getComponent(NetworkComponent.class);
            switch (netComp.replicateMode) {
                case OWNER:
                    if (client.equals(getClientFor(netEntity))) {
                        client.setNetInitial(netComp.networkId);
                    }
                    break;
                default:
                    // TODO: Relevance Check
                    client.setNetInitial(netComp.networkId);
                    break;
            }
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

    void queueMessage(NetData.NetMessage message) {
        queuedMessages.offer(message);
    }


    void setServer(Server server) {
        this.server = server;
    }
}
