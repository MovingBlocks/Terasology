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
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityChangeSubscriber;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Event;
import org.terasology.entitySystem.PersistableEntityManager;
import org.terasology.entitySystem.metadata.ClassMetadata;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.entitySystem.metadata.EventLibrary;
import org.terasology.entitySystem.metadata.TypeHandler;
import org.terasology.entitySystem.metadata.TypeHandlerLibraryBuilder;
import org.terasology.entitySystem.metadata.internal.EntitySystemLibraryImpl;
import org.terasology.entitySystem.persistence.EntitySerializer;
import org.terasology.entitySystem.persistence.EventSerializer;
import org.terasology.game.CoreRegistry;
import org.terasology.network.events.ConnectedEvent;
import org.terasology.network.pipelineFactory.TerasologyClientPipelineFactory;
import org.terasology.network.pipelineFactory.TerasologyServerPipelineFactory;
import org.terasology.network.serialization.NetComponentSerializeCheck;
import org.terasology.network.serialization.NetEntityRefTypeHandler;
import org.terasology.protobuf.NetData;
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

    // Shared
    private NetworkMode mode = NetworkMode.NONE;
    private PersistableEntityManager entityManager;
    private EntitySystemLibrary entitySystemLibrary;
    private EventSerializer eventSerializer;
    private EntitySerializer entitySerializer;

    private ChannelFactory factory;
    private TIntIntMap netIdToEntityId = new TIntIntHashMap();

    // Server only
    private ChannelGroup allChannels = new DefaultChannelGroup("tera-channels");
    private BlockingQueue<Client> newClients = Queues.newLinkedBlockingQueue();
    private int nextNetId = 1;
    private final Set<Client> clientList = Sets.newLinkedHashSet();
    private Map<EntityRef, Client> clientPlayerLookup = Maps.newHashMap();

    // Client only
    private Server server;

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
            nextNetId = 1;
            netIdToEntityId.clear();
            entityManager = null;
            entitySystemLibrary = null;
            eventSerializer = null;
            entitySerializer = null;
        }
    }

    public void update() {
        if (mode != NetworkMode.NONE) {
            if (entityManager != null) {
                if (!newClients.isEmpty()) {
                    List<Client> newPlayers = Lists.newArrayListWithExpectedSize(newClients.size());
                    newClients.drainTo(newPlayers);
                    for (Client client : newPlayers) {
                        processNewClient(client);
                    }
                }
                for (Client client : clientList) {
                    client.update();
                }
                if (server != null) {
                    server.update();
                }
            }
        }
    }

    public NetworkMode getMode() {
        return mode;
    }

    public Server getServer() {
        return this.server;
    }

    public Iterable<Client> getPlayers() {
        return this.clientList;
    }

    public Client getOwner(EntityRef entity) {
        Client result = clientPlayerLookup.get(entity);
        if (result == null) {
            NetworkComponent netComp = entity.getComponent(NetworkComponent.class);
            if (netComp != null && netComp.owner.exists()) {
                result = clientPlayerLookup.get(netComp.owner);
            }
        }
        return result;
    }

    public void setRemoteWorldProvider(RemoteChunkProvider remoteWorldProvider) {
        server.setRemoteWorldProvider(remoteWorldProvider);
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
                    Client clientPlayer = getOwner(entity);
                    if (clientPlayer != null) {
                        clientPlayer.setNetInitial(netComponent.networkId);
                    }
                    break;
                default:
                    for (Client client : clientList) {
                        // TODO: Relevance Check
                        client.setNetInitial(netComponent.networkId);
                    }
                    break;
            }

        }
    }

    public void unregisterNetworkEntity(EntityRef entity) {
        NetworkComponent netComponent = entity.getComponent(NetworkComponent.class);
        if (netComponent != null) {
            netIdToEntityId.remove(netComponent.networkId);
            if (mode == NetworkMode.SERVER) {
                NetData.NetMessage message = NetData.NetMessage.newBuilder()
                        .setType(NetData.NetMessage.Type.REMOVE_ENTITY)
                        .setRemoveEntity(
                                NetData.RemoveEntityMessage.newBuilder().setNetId(netComponent.networkId).build())
                        .build();
                for (Client client : clientList) {
                    client.setNetRemoved(netComponent.networkId, message);
                }
            }
        }
    }

    public void connectToEntitySystem(PersistableEntityManager entityManager, EntitySystemLibrary library) {
        if (this.entityManager != null) {
            this.entityManager.unsubscribe(this);
        }
        this.entityManager = entityManager;
        this.entityManager.subscribe(this);

        TypeHandlerLibraryBuilder builder = new TypeHandlerLibraryBuilder();
        for (Map.Entry<Class<?>, TypeHandler<?>> entry : library.getTypeHandlerLibrary()) {
            builder.addRaw(entry.getKey(), entry.getValue());
        }
        builder.add(EntityRef.class, new NetEntityRefTypeHandler(this));
        // TODO: Add network override types here

        this.entitySystemLibrary = new EntitySystemLibraryImpl(builder.build());
        EventLibrary eventLibrary = entitySystemLibrary.getEventLibrary();
        for (ClassMetadata<? extends Event> eventMetadata : library.getEventLibrary()) {
            eventLibrary.register(eventMetadata.getType(), eventMetadata.getNames());
        }
        ComponentLibrary componentLibrary = entitySystemLibrary.getComponentLibrary();
        for (ClassMetadata<? extends Component> componentMetadata : library.getComponentLibrary()) {
            componentLibrary.register(componentMetadata.getType(), componentMetadata.getNames());
        }

        eventSerializer = new EventSerializer(eventLibrary);
        entitySerializer = new EntitySerializer(entityManager, componentLibrary);
        entitySerializer.setIgnoringEntityId(true);
        entitySerializer.setComponentSerializeCheck(new NetComponentSerializeCheck());

        if (server != null) {
            server.connectToEntitySystem(entityManager, entitySerializer, eventSerializer);
        }
    }


    @Override
    public void onEntityChange(EntityRef entity) {
        NetworkComponent netComp = entity.getComponent(NetworkComponent.class);
        if (netComp != null) {
            for (Client client : clientList) {
                client.setNetDirty(netComp.networkId);
            }
            if (server != null) {
                server.setNetDirty(netComp.networkId);
            }
        }
    }

    /**
     * @return The number of received messages since last request
     */
    public int getIncomingMessagesDelta() {
        switch (mode) {
            case SERVER:
                int total = 0;
                for (Client client : clientList) {
                    total += client.getReceivedMessagesSinceLastCall();
                }
                return total;
            case CLIENT:
                if (server != null) {
                    return server.getReceivedMessagesSinceLastCall();
                }
            default:
                return 0;
        }
    }

    /**
     * @return The number of received bytes since last request
     */
    public int getIncomingBytesDelta() {
        switch (mode) {
            case SERVER:
                int total = 0;
                for (Client client : clientList) {
                    total += client.getReceivedBytesSinceLastCall();
                }
                return total;
            case CLIENT:
                if (server != null) {
                    return server.getReceivedBytesSinceLastCall();
                }
            default:
                return 0;
        }
    }

    public int getOutgoingMessagesDelta() {
        switch (mode) {
            case SERVER:
                int total = 0;
                for (Client client : clientList) {
                    total += client.getSentMessagesSinceLastCall();
                }
                return total;
            case CLIENT:
                if (server != null) {
                    return server.getSentMessagesSinceLastCall();
                }
            default:
                return 0;
        }
    }

    public int getOutgoingBytesDelta() {
        switch (mode) {
            case SERVER:
                int total = 0;
                for (Client client : clientList) {
                    total += client.getSentBytesSinceLastCall();
                }
                return total;
            case CLIENT:
                if (server != null) {
                    return server.getSentBytesSinceLastCall();
                }
            default:
                return 0;
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

    void addClient(Client client) {
        newClients.offer(client);
    }

    private void processNewClient(Client client) {
        logger.info("New client connected: {}", client.getName());
        client.connected(entityManager, entitySerializer, eventSerializer);
        clientList.add(client);
        clientPlayerLookup.put(client.getEntity(), client);
        client.getEntity().send(new ConnectedEvent());
        logger.info("New client entity: {}", client.getEntity());
        for (EntityRef netEntity : entityManager.iteratorEntities(NetworkComponent.class)) {
            NetworkComponent netComp = netEntity.getComponent(NetworkComponent.class);
            switch (netComp.replicateMode) {
                case OWNER:
                    if (client.equals(getOwner(netEntity))) {
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

    void removeClient(Client client) {
        synchronized (clientList) {
            clientList.remove(client);
        }
    }

    void setServer(Server server) {
        this.server = server;
    }

}
