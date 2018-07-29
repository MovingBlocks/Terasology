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

import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.protobuf.ByteString;
import gnu.trove.map.TIntLongMap;
import gnu.trove.map.hash.TIntLongHashMap;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.config.NetworkConfig;
import org.terasology.context.Context;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.Time;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.module.StandardModuleExtension;
import org.terasology.engine.subsystem.common.hibernation.HibernationManager;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.entity.internal.EntityChangeSubscriber;
import org.terasology.entitySystem.entity.internal.OwnershipHelper;
import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.ComponentMetadata;
import org.terasology.entitySystem.metadata.EventLibrary;
import org.terasology.entitySystem.metadata.EventMetadata;
import org.terasology.module.Module;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.network.Client;
import org.terasology.network.JoinStatus;
import org.terasology.network.NetworkComponent;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.network.Server;
import org.terasology.network.events.ConnectedEvent;
import org.terasology.network.events.DisconnectedEvent;
import org.terasology.network.exceptions.HostingFailedException;
import org.terasology.network.internal.pipelineFactory.TerasologyClientPipelineFactory;
import org.terasology.network.internal.pipelineFactory.TerasologyServerPipelineFactory;
import org.terasology.network.serialization.NetComponentSerializeCheck;
import org.terasology.network.serialization.NetEntityRefTypeHandler;
import org.terasology.persistence.PlayerStore;
import org.terasology.persistence.StorageManager;
import org.terasology.persistence.serializers.EventSerializer;
import org.terasology.persistence.serializers.NetworkEntitySerializer;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.protobuf.NetData;
import org.terasology.reflection.metadata.ClassLibrary;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.Color;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.biomes.Biome;
import org.terasology.world.biomes.BiomeManager;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.chunks.remoteChunkProvider.RemoteChunkProvider;
import org.terasology.world.generator.WorldGenerator;

import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;

/**
 * Implementation of the Network System using Netty and TCP/IP
 *
 */
public class NetworkSystemImpl implements EntityChangeSubscriber, NetworkSystem {
    private static final Logger logger = LoggerFactory.getLogger(NetworkSystemImpl.class);
    private static final int OWNER_DEPTH_LIMIT = 50;
    private static final int NET_TICK_RATE = 50;
    private static final int NULL_NET_ID = 0;

    // Shared
    private Context context;
    private Optional<HibernationManager> hibernationSettings = Optional.empty();
    private NetworkConfig config;
    private NetworkMode mode = NetworkMode.NONE;
    private EngineEntityManager entityManager;
    private ComponentLibrary componentLibrary;
    private EventLibrary eventLibrary;
    private EventSerializer eventSerializer;
    private NetworkEntitySerializer entitySerializer;
    private BlockManager blockManager;
    private BiomeManager biomeManager;
    private OwnershipHelper ownershipHelper;

    private ChannelFactory factory;
    private TIntLongMap netIdToEntityId = new TIntLongHashMap();

    private Time time;
    private long nextNetworkTick;

    private boolean kicked;

    // Server only
    private ChannelGroup allChannels = new DefaultChannelGroup("tera-channels");
    private BlockingQueue<NetClient> newClients = Queues.newLinkedBlockingQueue();
    private BlockingQueue<NetClient> disconnectedClients = Queues.newLinkedBlockingQueue();
    private int nextNetId = 1;
    private final Set<Client> clientList = Sets.newLinkedHashSet();
    private final Set<NetClient> netClientList = Sets.newLinkedHashSet();
    private Map<EntityRef, Client> clientPlayerLookup = Maps.newHashMap();
    private Map<EntityRef, EntityRef> ownerLookup = Maps.newHashMap();
    private SetMultimap<EntityRef, EntityRef> ownedLookup = HashMultimap.create();
    private StorageManager storageManager;

    // Client only
    private ServerImpl server;

    public NetworkSystemImpl(Time time, Context context) {
        this.time = time;
        this.config = context.get(Config.class).getNetwork();
        this.hibernationSettings = Optional.ofNullable(context.get(HibernationManager.class));
    }

    @Override
    public void host(int port, boolean dedicatedServer) throws HostingFailedException {
        if (mode == NetworkMode.NONE) {
            try {
                if (hibernationSettings.isPresent()) {
                    hibernationSettings.get().setHibernationAllowed(false);
                }
                mode = dedicatedServer ? NetworkMode.DEDICATED_SERVER : NetworkMode.LISTEN_SERVER;
                for (EntityRef entity : entityManager.getEntitiesWith(NetworkComponent.class)) {
                    registerNetworkEntity(entity);
                }
                generateSerializationTables();

                factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
                ServerBootstrap bootstrap = new ServerBootstrap(factory);
                bootstrap.setPipelineFactory(new TerasologyServerPipelineFactory(this));
                bootstrap.setOption("child.tcpNoDelay", true);
                bootstrap.setOption("child.keepAlive", true);
                Channel listenChannel = bootstrap.bind(new InetSocketAddress(port));
                allChannels.add(listenChannel);
                logger.info("Started server on port {}", port);
                if (config.getServerMOTD() != null) {
                    logger.info("Server MOTD is \"{}\"", config.getServerMOTD());
                } else {
                    logger.info("No server MOTD is defined");
                }

                // enumerate all network interfaces that listen
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface ifc = interfaces.nextElement();
                    if (!ifc.isLoopback()) {
                        for (InterfaceAddress ifadr : ifc.getInterfaceAddresses()) {
                            InetAddress adr = ifadr.getAddress();
                            logger.info("Listening on network interface \"{}\", hostname \"{}\" ({})",
                                    ifc.getDisplayName(), adr.getCanonicalHostName(), adr.getHostAddress());
                        }
                    }
                }

                nextNetworkTick = time.getRealTimeInMs();
            } catch (SocketException e) {
                throw new HostingFailedException("Could not identify network interfaces", e);
            } catch (ChannelException e) {
                if (e.getCause() instanceof BindException) {
                    throw new HostingFailedException("Port already in use (are you already hosting a game?)", e.getCause());
                } else {
                    throw new HostingFailedException("Failed to host game", e.getCause());
                }

            }
        }
    }

    @Override
    public JoinStatus join(String address, int port) throws InterruptedException {
        if (mode == NetworkMode.NONE) {
            if (hibernationSettings.isPresent()) {
                hibernationSettings.get().setHibernationAllowed(false);
            }
            factory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
            ClientBootstrap bootstrap = new ClientBootstrap(factory);
            bootstrap.setPipelineFactory(new TerasologyClientPipelineFactory(this));
            bootstrap.setOption("tcpNoDelay", true);
            bootstrap.setOption("keepAlive", true);
            ChannelFuture connectCheck = bootstrap.connect(new InetSocketAddress(address, port));
            try {
                connectCheck.await();
            } catch (InterruptedException e) {
                connectCheck.cancel();
                connectCheck.getChannel().getCloseFuture().awaitUninterruptibly();
                factory.releaseExternalResources();
                throw e;
            }
            if (!connectCheck.isSuccess()) {
                logger.warn("Failed to connect to server", connectCheck.getCause());
                connectCheck.getChannel().getCloseFuture().awaitUninterruptibly();
                factory.releaseExternalResources();
                return new JoinStatusImpl("Failed to connect to server - " + connectCheck.getCause().getMessage());
            } else {
                allChannels.add(connectCheck.getChannel());
                ClientConnectionHandler connectionHandler = connectCheck.getChannel().getPipeline().get(ClientConnectionHandler.class);
                if (connectionHandler == null) {
                    JoinStatusImpl status = new JoinStatusImpl();
                    status.setComplete();
                    return status;
                } else {
                    return connectionHandler.getJoinStatus();
                }
            }
        }
        return new JoinStatusImpl("Network system already active");
    }

    @Override
    public void shutdown() {
        allChannels.close().awaitUninterruptibly();
        // Factory may be null if a local game session has happened, yet be initialized if networking has been used
        if (factory != null) {
            factory.releaseExternalResources();
        }
        processPendingDisconnects();
        clientList.forEach(this::processRemovedClient);
        server = null;
        nextNetId = 1;
        netIdToEntityId.clear();
        if (mode != NetworkMode.CLIENT) {
            if (this.entityManager != null) {
                for (EntityRef entity : entityManager.getEntitiesWith(NetworkComponent.class)) {
                    NetworkComponent netComp = entity.getComponent(NetworkComponent.class);
                    netComp.setNetworkId(0);
                    entity.saveComponent(netComp);
                }
                this.entityManager.unsubscribe(this);
            }
        }
        mode = NetworkMode.NONE;
        entityManager = null;
        eventLibrary = null;
        componentLibrary = null;
        eventSerializer = null;
        entitySerializer = null;
        clientList.clear();
        netClientList.clear();
        blockManager = null;
        biomeManager = null;
        ownerLookup.clear();
        ownedLookup.clear();
        ownershipHelper = null;
        storageManager = null;
        logger.info("Network shutdown");
    }

    @Override
    public Client joinLocal(String preferredName, Color color) {
        Client localClient = new LocalClient(preferredName, color, entityManager);
        clientList.add(localClient);
        clientPlayerLookup.put(localClient.getEntity(), localClient);
        connectClient(localClient);
        return localClient;
    }

    @Override
    public void update() {
        if (mode != NetworkMode.NONE) {
            if (entityManager != null) {
                processPendingConnections();
                processPendingDisconnects();
                long currentTimer = time.getRealTimeInMs();
                boolean netTick = false;
                if (currentTimer > nextNetworkTick) {
                    nextNetworkTick += NET_TICK_RATE;
                    netTick = true;
                }
                PerformanceMonitor.startActivity("Client update");
                for (Client client : clientList) {
                    client.update(netTick);
                }
                PerformanceMonitor.endActivity();
                if (server != null) {
                    server.update(netTick);
                }
            }
        }
    }

    private void processPendingDisconnects() {
        if (!disconnectedClients.isEmpty()) {
            List<NetClient> removedPlayers = Lists.newArrayListWithExpectedSize(disconnectedClients.size());
            disconnectedClients.drainTo(removedPlayers);
            removedPlayers.forEach(this::processRemovedClient);
        }
    }

    private void processPendingConnections() {
        if (!newClients.isEmpty()) {
            List<NetClient> newPlayers = Lists.newArrayListWithExpectedSize(newClients.size());
            newClients.drainTo(newPlayers);
            newPlayers.forEach(this::processNewClient);
        }
    }

    @Override
    public NetworkMode getMode() {
        return mode;
    }

    @Override
    public Server getServer() {
        return this.server;
    }

    @Override
    public Iterable<Client> getPlayers() {
        return this.clientList;
    }

    @Override
    public Client getOwner(EntityRef entity) {
        EntityRef owner = getOwnerEntity(entity);
        return clientPlayerLookup.get(owner);
    }

    NetClient getNetOwner(EntityRef entity) {
        Client owner = getOwner(entity);
        if (owner instanceof NetClient) {
            return (NetClient) owner;
        }
        return null;
    }

    public int getBandwidthPerClient() {
        if (netClientList.size() > 0) {
            return config.getUpstreamBandwidth() / netClientList.size();
        }
        return config.getUpstreamBandwidth();
    }

    @Override
    public EntityRef getOwnerEntity(EntityRef entity) {
        EntityRef owner = entity;
        //NetworkComponent ownerNetComp = entity.getComponent(NetworkComponent.class);
        int i = 0;
        while (i++ < OWNER_DEPTH_LIMIT) {
            EntityRef nextOwner = owner.getOwner();
            if (nextOwner.exists()) {
                owner = nextOwner;
            } else {
                break;
            }
        }
        return owner;
    }

    @Override
    public void setRemoteWorldProvider(RemoteChunkProvider remoteWorldProvider) {
        server.setRemoteWorldProvider(remoteWorldProvider);
    }

    public void registerClientNetworkEntity(int netId, long entityId) {
        long oldId = netIdToEntityId.put(netId, entityId);
        if (oldId != NULL_NET_ID && oldId != entityId) {
            logger.error("Registered duplicate network id: {}", netId);
        } else {
            logger.debug("Registered Network Entity: {}", netId);
        }
    }

    public void registerNetworkEntity(EntityRef entity) {
        if (mode == NetworkMode.NONE) {
            return;
        }

        if (mode.isServer()) {
            NetworkComponent netComponent = entity.getComponent(NetworkComponent.class);
            netComponent.setNetworkId(nextNetId++);
            entity.saveComponent(netComponent);
            netIdToEntityId.put(netComponent.getNetworkId(), entity.getId());
            switch (netComponent.replicateMode) {
                case OWNER:
                    NetClient clientPlayer = getNetOwner(entity);
                    if (clientPlayer != null) {
                        clientPlayer.setNetInitial(netComponent.getNetworkId());
                    }
                    break;
                default:
                    for (NetClient client : netClientList) {
                        // TODO: Relevance Check
                        client.setNetInitial(netComponent.getNetworkId());
                    }
                    break;
            }
            EntityRef owner = entity.getOwner();
            if (owner.exists()) {
                ownerLookup.put(entity, owner);
                ownedLookup.put(owner, entity);
            }
        }

    }

    public void updateOwnership(EntityRef entity) {
        NetworkComponent netComponent = entity.getComponent(NetworkComponent.class);
        if (netComponent == null || netComponent.getNetworkId() == NULL_NET_ID) {
            return;
        }

        EntityRef lastOwnerEntity = ownerLookup.get(entity);
        if (lastOwnerEntity == null) {
            lastOwnerEntity = EntityRef.NULL;
        }

        EntityRef newOwnerEntity = entity.getOwner();
        if (!Objects.equal(lastOwnerEntity, newOwnerEntity)) {
            NetClient lastOwner = getNetOwner(lastOwnerEntity);
            NetClient newOwner = getNetOwner(newOwnerEntity);

            if (!Objects.equal(lastOwner, newOwner)) {
                recursiveUpdateOwnership(entity, lastOwner, newOwner);
                if (newOwner != null) {
                    int id = netComponent.getNetworkId();
                    for (Component component : entity.iterateComponents()) {
                        if (componentLibrary.getMetadata(component.getClass()).isReplicated()) {
                            newOwner.setComponentDirty(id, component.getClass());
                        }
                    }
                }
            }

            if (lastOwnerEntity.exists()) {
                ownedLookup.remove(lastOwnerEntity, entity);
            }
            if (newOwnerEntity.exists()) {
                ownerLookup.put(entity, newOwnerEntity);
                ownedLookup.put(newOwnerEntity, entity);
            } else {
                ownerLookup.remove(entity);
            }
        }
    }

    private void recursiveUpdateOwnership(EntityRef entity, NetClient lastOwner, NetClient newOwner) {
        NetworkComponent networkComponent = entity.getComponent(NetworkComponent.class);
        if (networkComponent != null) {
            if (networkComponent.replicateMode == NetworkComponent.ReplicateMode.OWNER) {
                logger.debug("{}'s owner changed from {} to {}, so replicating.", entity, lastOwner, newOwner);
                // Remove from last owner
                if (lastOwner != null) {
                    lastOwner.setNetRemoved(networkComponent.getNetworkId());
                }
                // Add to new owner
                if (newOwner != null) {
                    newOwner.setNetInitial(networkComponent.getNetworkId());
                }
            }
            for (EntityRef owned : ownedLookup.get(entity)) {
                recursiveUpdateOwnership(owned, lastOwner, newOwner);
            }
        }
    }

    public void unregisterClientNetworkEntity(int netId) {
        netIdToEntityId.remove(netId);
    }

    public void unregisterNetworkEntity(EntityRef entity) {
        if (mode != NetworkMode.CLIENT) {
            NetworkComponent netComponent = entity.getComponent(NetworkComponent.class);
            if (netComponent != null) {
                logger.debug("Unregistering network entity: {} with netId {}", entity, netComponent.getNetworkId());
                netIdToEntityId.remove(netComponent.getNetworkId());
                if (mode.isServer()) {
                    for (NetClient client : netClientList) {
                        client.setNetRemoved(netComponent.getNetworkId());
                    }
                }
                netComponent.setNetworkId(NULL_NET_ID);
                entity.saveComponent(netComponent);
            }
        }
        ownerLookup.remove(entity);
    }

    @Override
    public void connectToEntitySystem(EngineEntityManager newEntityManager, EventLibrary newEventLibrary, BlockEntityRegistry blockEntityRegistry) {
        if (this.entityManager != null) {
            this.entityManager.unsubscribe(this);
        }
        this.entityManager = newEntityManager;
        this.entityManager.subscribeForChanges(this);
        this.blockManager = context.get(BlockManager.class);
        this.biomeManager = context.get(BiomeManager.class);
        this.ownershipHelper = new OwnershipHelper(newEntityManager.getComponentLibrary());
        this.storageManager = context.get(StorageManager.class);
        this.eventLibrary = newEventLibrary;
        this.componentLibrary = entityManager.getComponentLibrary();

        context.get(ComponentSystemManager.class).register(new NetworkEntitySystem(this), "engine:networkEntitySystem");

        TypeSerializationLibrary typeSerializationLibrary = new TypeSerializationLibrary(entityManager.getTypeSerializerLibrary());
        typeSerializationLibrary.addTypeHandler(EntityRef.class, new NetEntityRefTypeHandler(this, blockEntityRegistry));
        // TODO: Add network override types here (that use id lookup tables)

        eventSerializer = new EventSerializer(eventLibrary, typeSerializationLibrary);
        entitySerializer = new NetworkEntitySerializer(newEntityManager, entityManager.getComponentLibrary(), typeSerializationLibrary);
        entitySerializer.setComponentSerializeCheck(new NetComponentSerializeCheck());

        if (mode == NetworkMode.CLIENT) {
            entityManager.setEntityRefStrategy(new NetworkClientRefStrategy(this));
            applySerializationTables();
        }

        if (server != null) {
            server.connectToEntitySystem(newEntityManager, entitySerializer, eventSerializer, blockEntityRegistry);
        }
    }

    @Override
    public void onEntityComponentAdded(EntityRef entity, Class<? extends Component> component) {
        ComponentMetadata<? extends Component> metadata = componentLibrary.getMetadata(component);
        NetworkComponent netComp = entity.getComponent(NetworkComponent.class);
        if (netComp != null && netComp.getNetworkId() != NULL_NET_ID) {
            if (mode.isServer()) {
                if (metadata.isReplicated()) {
                    for (NetClient client : netClientList) {
                        logger.info("Component {} added to {}", component, entity);
                        client.setComponentAdded(netComp.getNetworkId(), component);
                    }
                }
            }
        }
        updatedOwnedEntities(entity, component, metadata);
    }

    @Override
    public void onEntityComponentRemoved(EntityRef entity, Class<? extends Component> component) {
        ComponentMetadata<? extends Component> metadata = componentLibrary.getMetadata(component);
        NetworkComponent netComp = entity.getComponent(NetworkComponent.class);
        if (netComp != null && netComp.getNetworkId() != NULL_NET_ID) {
            if (mode.isServer()) {
                if (metadata.isReplicated()) {
                    for (NetClient client : netClientList) {
                        logger.info("Component {} removed from {}", component, entity);
                        client.setComponentRemoved(netComp.getNetworkId(), component);
                    }
                }
            }
        }
        if (mode.isAuthority() && metadata.isReferenceOwner()) {
            ownershipHelper.listOwnedEntities(entity.getComponent(component)).forEach(EntityRef::destroy);
        }
    }

    @Override
    public void onReactivation(EntityRef entity, Collection<Component> components) {
        // TODO decide if reactivaton should be transfred.
    }

    @Override
    public void onBeforeDeactivation(EntityRef entity, Collection<Component> components) {
        // TODO decide if activations should be transfred.
    }

    @Override
    public void onEntityComponentChange(EntityRef entity, Class<? extends Component> component) {
        NetworkComponent netComp = entity.getComponent(NetworkComponent.class);
        ComponentMetadata<? extends Component> metadata = componentLibrary.getMetadata(component);
        if (netComp != null && netComp.getNetworkId() != NULL_NET_ID) {
            switch (mode) {
                case LISTEN_SERVER:
                case DEDICATED_SERVER:
                    if (metadata.isReplicated()) {
                        for (NetClient client : netClientList) {
                            client.setComponentDirty(netComp.getNetworkId(), component);
                        }
                    }
                    break;
                case CLIENT:
                    if (server != null && metadata.isReplicatedFromOwner() && getOwnerEntity(entity).equals(server.getClientEntity())) {
                        server.setComponentDirty(netComp.getNetworkId(), component);
                    }
                    break;
                default:
                    break;
            }
        }
        updatedOwnedEntities(entity, component, metadata);
    }

    private void updatedOwnedEntities(EntityRef entity, Class<? extends Component> component, ComponentMetadata<? extends Component> metadata) {
        if (mode.isAuthority() && metadata.isReferenceOwner()) {
            for (EntityRef ownedEntity : ownershipHelper.listOwnedEntities(entity.getComponent(component))) {
                EntityRef previousOwner = ownedEntity.getOwner();
                if (!previousOwner.equals(entity)) {
                    ownedEntity.setOwner(entity);
                }
            }
        }
    }

    /**
     * @return The number of received messages since last request
     */
    @Override
    public int getIncomingMessagesDelta() {
        switch (mode) {
            case LISTEN_SERVER:
            case DEDICATED_SERVER:
                int total = 0;
                for (NetClient client : netClientList) {
                    total += client.getMetrics().getReceivedMessagesSinceLastCall();
                }
                return total;
            case CLIENT:
                if (server != null) {
                    return server.getMetrics().getReceivedMessagesSinceLastCall();
                }
                return 0;
            default:
                return 0;
        }
    }

    /**
     * @return The number of received bytes since last request
     */
    @Override
    public int getIncomingBytesDelta() {
        switch (mode) {
            case LISTEN_SERVER:
            case DEDICATED_SERVER:
                int total = 0;
                for (NetClient client : netClientList) {
                    total += client.getMetrics().getReceivedBytesSinceLastCall();
                }
                return total;
            case CLIENT:
                if (server != null) {
                    return server.getMetrics().getReceivedBytesSinceLastCall();
                }
                return 0;
            default:
                return 0;
        }
    }

    @Override
    public int getOutgoingMessagesDelta() {
        switch (mode) {
            case LISTEN_SERVER:
            case DEDICATED_SERVER:
                int total = 0;
                for (NetClient client : netClientList) {
                    total += client.getMetrics().getSentMessagesSinceLastCall();
                }
                return total;
            case CLIENT:
                if (server != null) {
                    return server.getMetrics().getSentMessagesSinceLastCall();
                }
                return 0;
            default:
                return 0;
        }
    }

    @Override
    public int getOutgoingBytesDelta() {
        switch (mode) {
            case LISTEN_SERVER:
            case DEDICATED_SERVER:
                int total = 0;
                for (NetClient client : netClientList) {
                    total += client.getMetrics().getSentBytesSinceLastCall();
                }
                return total;
            case CLIENT:
                if (server != null) {
                    return server.getMetrics().getSentBytesSinceLastCall();
                }
                return 0;
            default:
                return 0;
        }
    }

    long getEntityId(int netId) {
        return netIdToEntityId.get(netId);
    }

    public EntityRef getEntity(int netId) {
        if (mode == NetworkMode.CLIENT) {
            if (entityManager != null) {
                return new NetEntityRef(netId, this, entityManager);
            } else {
                logger.error("Requesting entity before entity manager set up");
            }
        } else {
            long entityId = netIdToEntityId.get(netId);
            if (entityManager != null && entityId != NULL_NET_ID) {
                return entityManager.getEntity(entityId);
            }
        }
        return EntityRef.NULL;
    }

    @Override
    public void forceDisconnect(Client client) {
        if (client instanceof NetClient) {
            NetClient nc = (NetClient) client;
            removeKickedClient(nc);
        }
    }

    /**
     * Sets the context within which this system should operate
     * @param context
     */
    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    void removeKickedClient(NetClient client) {
        kicked = true;
        disconnectedClients.offer(client);
    }

    void registerChannel(Channel channel) {
        allChannels.add(channel);
    }

    void addClient(NetClient client) {
        newClients.offer(client);
    }

    void removeClient(NetClient client) {
        if (!kicked) {
            disconnectedClients.offer(client);
        }
    }

    private void processRemovedClient(Client client) {
        if (client instanceof NetClient) {
            ServerConnectListManager serverConnectListManager = context.get(ServerConnectListManager.class);
            if (!serverConnectListManager.isClientAllowedToConnect(client.getId())) {
                return;
            }
            NetClient netClient = (NetClient) client;
            netClientList.remove(netClient);
        }
        clientList.remove(client);
        clientPlayerLookup.remove(client.getEntity());
        logger.info("Client disconnected: " + client.getName());
        storageManager.deactivatePlayer(client);
        client.getEntity().send(new DisconnectedEvent());
        client.disconnect();
    }

    private void processNewClient(NetClient client) {
        ServerConnectListManager serverConnectListManager = context.get(ServerConnectListManager.class);
        if (!serverConnectListManager.isClientAllowedToConnect(client.getId())) {
            String errorMessage = serverConnectListManager.getErrorMessage(client.getId());
            client.send(NetData.NetMessage.newBuilder().setServerInfo(getServerInfoMessage(errorMessage)).build());
            forceDisconnect(client);
            // reset kicked status so the next connection is set correctly
            kicked = false;
            return;
        }

        client.connected(entityManager, entitySerializer, eventSerializer, eventLibrary);
        client.send(NetData.NetMessage.newBuilder().setJoinComplete(
                NetData.JoinCompleteMessage.newBuilder().setClientId(client.getEntity().getComponent(NetworkComponent.class).getNetworkId())).build());
        clientList.add(client);
        netClientList.add(client);
        clientPlayerLookup.put(client.getEntity(), client);

        connectClient(client);

        // log after connect so that the name has been set:
        logger.info("New client entity: {}", client.getEntity());
        for (EntityRef netEntity : entityManager.getEntitiesWith(NetworkComponent.class)) {
            NetworkComponent netComp = netEntity.getComponent(NetworkComponent.class);
            if (netComp.getNetworkId() != NULL_NET_ID) {
                switch (netComp.replicateMode) {
                    case OWNER:
                        if (client.equals(getOwner(netEntity))) {
                            client.setNetInitial(netComp.getNetworkId());
                        }
                        break;
                    default:
                        // TODO: Relevance Check
                        client.setNetInitial(netComp.getNetworkId());
                        break;
                }
            }
        }
    }

    private void connectClient(Client client) {
        PlayerStore entityStore = storageManager.loadPlayerStore(client.getId());
        client.getEntity().send(new ConnectedEvent(entityStore));
    }

    NetData.ServerInfoMessage getServerInfoMessage() {
        return getServerInfoMessage(null);
    }

    private NetData.ServerInfoMessage getServerInfoMessage(String errorMessage) {
        NetData.ServerInfoMessage.Builder serverInfoMessageBuilder = NetData.ServerInfoMessage.newBuilder();
        serverInfoMessageBuilder.setTime(time.getGameTimeInMs());
        if (config.getServerMOTD() != null) {
            serverInfoMessageBuilder.setMOTD(config.getServerMOTD());
        }
        serverInfoMessageBuilder.setOnlinePlayersAmount(clientList.size());
        WorldProvider worldProvider = context.get(WorldProvider.class);
        if (worldProvider != null) {
            NetData.WorldInfo.Builder worldInfoBuilder = NetData.WorldInfo.newBuilder();
            worldInfoBuilder.setTime(worldProvider.getTime().getMilliseconds());
            worldInfoBuilder.setTitle(worldProvider.getTitle());
            serverInfoMessageBuilder.addWorldInfo(worldInfoBuilder);
        }
        WorldGenerator worldGen = context.get(WorldGenerator.class);
        if (worldGen != null) {
            serverInfoMessageBuilder.setReflectionHeight(worldGen.getWorld().getSeaLevel() + 0.5f);
        }
        for (Module module : CoreRegistry.get(ModuleManager.class).getEnvironment()) {
            if (!StandardModuleExtension.isServerSideOnly(module)) {
                serverInfoMessageBuilder.addModule(NetData.ModuleInfo.newBuilder()
                        .setModuleId(module.getId().toString())
                        .setModuleVersion(module.getVersion().toString()).build());
            }
        }
        for (Map.Entry<String, Short> blockMapping : blockManager.getBlockIdMap().entrySet()) {
            serverInfoMessageBuilder.addBlockId(blockMapping.getValue());
            serverInfoMessageBuilder.addBlockName(blockMapping.getKey());
        }
        for (Biome biome : biomeManager.getBiomes()) {
            serverInfoMessageBuilder.addBiomeId(biome.getId());
            short shortId = biomeManager.getBiomeShortId(biome);
            serverInfoMessageBuilder.addBiomeShortId(shortId);
        }
        for (BlockFamily registeredBlockFamily : blockManager.listRegisteredBlockFamilies()) {
            serverInfoMessageBuilder.addRegisterBlockFamily(registeredBlockFamily.getURI().toString());
        }
        if (errorMessage != null && !errorMessage.isEmpty()) {
            serverInfoMessageBuilder.setErrorMessage(errorMessage);
        }
        serializeComponentInfo(serverInfoMessageBuilder);
        serializeEventInfo(serverInfoMessageBuilder);

        return serverInfoMessageBuilder.build();
    }

    private void serializeEventInfo(NetData.ServerInfoMessage.Builder serverInfoMessageBuilder) {
        Map<Class<? extends Event>, Integer> eventIdTable = eventSerializer.getIdMapping();
        for (Map.Entry<Class<? extends Event>, Integer> eventMapping : eventIdTable.entrySet()) {
            ByteString.Output fieldIds = ByteString.newOutput();
            EventMetadata<?> metadata = eventLibrary.getMetadata(eventMapping.getKey());
            NetData.SerializationInfo.Builder info = NetData.SerializationInfo.newBuilder()
                    .setId(eventMapping.getValue())
                    .setName(metadata.getUri().toString());
            for (FieldMetadata<?, ?> field : metadata.getFields()) {
                fieldIds.write(field.getId());
                info.addFieldName(field.getName());
            }
            info.setFieldIds(fieldIds.toByteString());
            serverInfoMessageBuilder.addEvent(info);
        }
    }

    private void serializeComponentInfo(NetData.ServerInfoMessage.Builder serverInfoMessageBuilder) {
        Map<Class<? extends Component>, Integer> componentIdTable = entitySerializer.getIdMapping();
        for (Map.Entry<Class<? extends Component>, Integer> componentIdMapping : componentIdTable.entrySet()) {
            ByteString.Output fieldIds = ByteString.newOutput();
            ComponentMetadata<?> metadata = componentLibrary.getMetadata(componentIdMapping.getKey());
            NetData.SerializationInfo.Builder info = NetData.SerializationInfo.newBuilder()
                    .setId(componentIdMapping.getValue())
                    .setName(metadata.getUri().toString());
            for (FieldMetadata<?, ?> field : metadata.getFields()) {
                fieldIds.write(field.getId());
                info.addFieldName(field.getName());
            }
            info.setFieldIds(fieldIds.toByteString());
            serverInfoMessageBuilder.addComponent(info);
        }
    }

    void setServer(ServerImpl server) {
        if (server != null) {
            mode = NetworkMode.CLIENT;
            nextNetworkTick = time.getRealTimeInMs();
            logger.info("Connected to server");
        }
        this.server = server;

    }

    private void generateSerializationTables() {
        entitySerializer.setIdMapping(generateIds(componentLibrary));
        eventSerializer.setIdMapping(generateIds(eventLibrary));
    }

    private <T> Map<Class<? extends T>, Integer> generateIds(ClassLibrary<T> classLibrary) {
        Map<Class<? extends T>, Integer> result = Maps.newHashMap();
        for (ClassMetadata<? extends T, ?> metadata : classLibrary) {
            int index = result.size();
            result.put(metadata.getType(), index);

            int fieldId = 0;
            for (FieldMetadata<?, ?> field : metadata.getFields()) {
                if (fieldId >= 256) {
                    logger.error("Class {} has too many fields (>255), serialization will be incomplete", metadata.getUri());
                    break;
                }
                field.setId((byte) fieldId);
                fieldId++;
            }
        }
        return result;
    }

    private void applySerializationTables() {
        NetData.ServerInfoMessage serverInfo = server.getRawInfo();
        entitySerializer.setIdMapping(applySerializationInfo(serverInfo.getComponentList(), componentLibrary));
        eventSerializer.setIdMapping(applySerializationInfo(serverInfo.getEventList(), eventLibrary));
    }

    private <T> Map<Class<? extends T>, Integer> applySerializationInfo(List<NetData.SerializationInfo> infoList, ClassLibrary<T> classLibrary) {
        Map<Class<? extends T>, Integer> idTable = Maps.newHashMap();
        for (NetData.SerializationInfo info : infoList) {
            ClassMetadata<? extends T, ?> metadata = classLibrary.getMetadata(new SimpleUri(info.getName()));
            if (metadata != null) {
                idTable.put(metadata.getType(), info.getId());
                for (int i = 0; i < info.getFieldIds().size(); ++i) {
                    FieldMetadata<?, ?> field = metadata.getField(info.getFieldName(i));
                    if (field != null) {
                        field.setId(info.getFieldIds().byteAt(i));
                    } else {
                        logger.error("Server has unknown field '{}' on '{}'", info.getFieldName(i), info.getName());
                    }
                }
            } else {
                logger.error("Server has unknown class '{}'", info.getName());
            }
        }
        return idTable;

    }

    /**
     * Used for testing only
     */
    void mockHost() {
        mode = NetworkMode.DEDICATED_SERVER;
    }

    @Override
    public void onBlockFamilyRegistered(BlockFamily family) {
        if (mode.isServer()) {
            for (NetClient client : netClientList) {
                client.blockFamilyRegistered(family);
            }
        }
    }


}
