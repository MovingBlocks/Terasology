// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network.internal;

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
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.NetworkConfig;
import org.terasology.engine.context.Context;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.core.EngineTime;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.core.module.StandardModuleExtension;
import org.terasology.engine.core.subsystem.common.hibernation.HibernationManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.entity.internal.EntityChangeSubscriber;
import org.terasology.engine.entitySystem.entity.internal.OwnershipHelper;
import org.terasology.engine.entitySystem.metadata.ComponentLibrary;
import org.terasology.engine.entitySystem.metadata.ComponentMetadata;
import org.terasology.engine.entitySystem.metadata.EventLibrary;
import org.terasology.engine.entitySystem.metadata.EventMetadata;
import org.terasology.engine.monitoring.PerformanceMonitor;
import org.terasology.engine.network.Client;
import org.terasology.engine.network.JoinStatus;
import org.terasology.engine.network.NetworkComponent;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.network.Server;
import org.terasology.engine.network.events.ConnectedEvent;
import org.terasology.engine.network.events.DisconnectedEvent;
import org.terasology.engine.network.exceptions.HostingFailedException;
import org.terasology.engine.network.internal.pipelineFactory.TerasologyClientPipelineFactory;
import org.terasology.engine.network.internal.pipelineFactory.TerasologyServerPipelineFactory;
import org.terasology.engine.network.serialization.NetComponentSerializeCheck;
import org.terasology.engine.network.serialization.NetEntityRefTypeHandler;
import org.terasology.engine.persistence.PlayerStore;
import org.terasology.engine.persistence.StorageManager;
import org.terasology.engine.persistence.serializers.EventSerializer;
import org.terasology.engine.persistence.serializers.NetworkEntitySerializer;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.engine.world.chunks.remoteChunkProvider.RemoteChunkProvider;
import org.terasology.engine.world.generator.WorldGenerator;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.gestalt.entitysystem.event.Event;
import org.terasology.gestalt.module.Module;
import org.terasology.nui.Color;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.protobuf.NetData;
import org.terasology.reflection.metadata.ClassLibrary;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.FieldMetadata;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.FutureMono;

import java.net.BindException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;
import static org.terasology.engine.registry.InjectionHelper.createWithConstructorInjection;


/**
 * Implementation of the Network System using Netty and TCP/IP
 */
public class NetworkSystemImpl implements EntityChangeSubscriber, NetworkSystem {
    public static int shutdownQuietMs = 2_000;
    public static int shutdownTimeoutMs = 15_000;

    private static final Logger logger = LoggerFactory.getLogger(NetworkSystemImpl.class);
    private static final int OWNER_DEPTH_LIMIT = 50;
    private static final int NET_TICK_RATE = 50;
    private static final int NULL_NET_ID = 0;

    private final Set<Client> clientList = Sets.newLinkedHashSet();
    private final Set<NetClient> netClientList = Sets.newLinkedHashSet();
    // Shared
    private ContextImpl context;
    private final Optional<HibernationManager> hibernationSettings;
    private final NetworkConfig config;
    private NetworkMode mode = NetworkMode.NONE;
    private EngineEntityManager entityManager;
    private ComponentLibrary componentLibrary;
    private EventLibrary eventLibrary;
    private EventSerializer eventSerializer;
    private NetworkEntitySerializer entitySerializer;
    private BlockManager blockManager;
    private OwnershipHelper ownershipHelper;
    private final TIntLongMap netIdToEntityId = new TIntLongHashMap();
    private final EngineTime time;
    private long nextNetworkTick;
    private boolean kicked;
    // Server only
    private final ChannelGroup allChannels = new DefaultChannelGroup("tera-channels", GlobalEventExecutor.INSTANCE);
    private ChannelFuture serverChannelFuture;
    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;

    private final BlockingQueue<NetClient> newClients = Queues.newLinkedBlockingQueue();
    private final BlockingQueue<NetClient> disconnectedClients = Queues.newLinkedBlockingQueue();
    private int nextNetId = 1;
    private final Map<EntityRef, Client> clientPlayerLookup = Maps.newHashMap();
    private final Map<EntityRef, EntityRef> ownerLookup = Maps.newHashMap();
    private final SetMultimap<EntityRef, EntityRef> ownedLookup = HashMultimap.create();
    private StorageManager storageManager;

    // Client only
    private ServerImpl server;
    private EventLoopGroup clientGroup;

    public NetworkSystemImpl(EngineTime time, Context context) {
        this.time = time;
        this.config = context.get(Config.class).getNetwork();
        this.hibernationSettings = context.getMaybe(HibernationManager.class);
        setContext(context);
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

                // Configure the server.
                bossGroup = new NioEventLoopGroup();
                workerGroup = new NioEventLoopGroup();
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .option(ChannelOption.SO_BACKLOG, 100)
                        .localAddress(port)
                        .childOption(ChannelOption.TCP_NODELAY, true)
                        .childOption(ChannelOption.SO_KEEPALIVE, true)
                        .childHandler(createWithConstructorInjection(TerasologyServerPipelineFactory.class, context));
                // Start the server.
                serverChannelFuture = b.bind();

                logger.info("Started server on port {}", port);
                if (config.getServerMOTD() != null) {
                    logger.info("Server MOTD is \"{}\"", config.getServerMOTD()); //NOPMD
                } else {
                    logger.info("No server MOTD is defined");
                }

                if (serverChannelFuture.isSuccess()) {
                    logger.info("Server started");
                }
                serverChannelFuture.sync();
                nextNetworkTick = time.getRealTimeInMs();
            } catch (ChannelException e) {
                if (e.getCause() instanceof BindException) {
                    throw new HostingFailedException("Port already in use (are you already hosting a game?)",
                            e.getCause());
                } else {
                    throw new HostingFailedException("Failed to host game", e.getCause());
                }
            } catch (InterruptedException e) {
                shutdown();
                throw new HostingFailedException("Server has been interrupted", e.getCause());
            }
        }
    }

    @Override
    public JoinStatus join(String address, int port) throws InterruptedException {
        if (mode == NetworkMode.NONE) {
            hibernationSettings.ifPresent(hibernationManager -> hibernationManager.setHibernationAllowed(false));
            ChannelFuture connectCheck = null;

            clientGroup = new NioEventLoopGroup();
            try {
                Bootstrap clientBootstrap = new Bootstrap();

                clientBootstrap.group(clientGroup);
                clientBootstrap.channel(NioSocketChannel.class);
                clientBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
                clientBootstrap.option(ChannelOption.TCP_NODELAY, true);
                clientBootstrap.remoteAddress(new InetSocketAddress(address, port));
                clientBootstrap.handler(createWithConstructorInjection(TerasologyClientPipelineFactory.class, context));

                connectCheck = clientBootstrap.connect();
                connectCheck.sync();
                if (connectCheck.isDone()) {
                    if (connectCheck.isSuccess()) {
                        allChannels.add(connectCheck.channel());
                        ClientConnectionHandler connectionHandler =
                                connectCheck.channel().pipeline().get(ClientConnectionHandler.class);
                        if (connectionHandler == null) {
                            JoinStatusImpl status = new JoinStatusImpl();
                            status.setComplete();
                            return status;
                        } else {
                            return connectionHandler.getJoinStatus();
                        }
                    } else {

                        Throwable connectionFailureCause = connectCheck.cause();
                        logger.warn("Failed to connect to server", connectionFailureCause);
                        connectCheck.channel().closeFuture().awaitUninterruptibly();
                        clientGroup.shutdownGracefully(shutdownQuietMs, shutdownTimeoutMs, TimeUnit.MILLISECONDS)
                                .syncUninterruptibly();
                        return new JoinStatusImpl("Failed to connect to server - " + connectionFailureCause.getMessage());
                    }
                }
                connectCheck.channel().closeFuture().sync();
            } catch (Exception e) {
                shutdown();
                if (connectCheck != null) {
                    connectCheck.cancel(true);
                    connectCheck.channel().closeFuture().awaitUninterruptibly();
                }
                throw e;
            }
        }
        return new JoinStatusImpl("Network system already active");
    }

    @Override
    public void shutdown() {
        allChannels.close().awaitUninterruptibly();
        List<Future<?>> shutdowns = new ArrayList<>(3);
        if (serverChannelFuture != null) {
            // Wait until all threads are terminated.
            shutdowns.add(bossGroup.shutdownGracefully(shutdownQuietMs, shutdownTimeoutMs, TimeUnit.MILLISECONDS));
            shutdowns.add(workerGroup.shutdownGracefully(shutdownQuietMs, shutdownTimeoutMs, TimeUnit.MILLISECONDS));
        }
        if (clientGroup != null) {
            shutdowns.add(clientGroup.shutdownGracefully(shutdownQuietMs, shutdownTimeoutMs, TimeUnit.MILLISECONDS));
        }

        // Shut down all event loops to terminate all threads.
        // I want their timeouts to count in parallel, instead of blocking on one after the other,
        // but turning the netty Future into something we can do this with is a bit of a mess until
        // we switch to using reactor-netty consistently.
        Mono.whenDelayError(
            Flux.fromIterable(shutdowns)
                    .map(x -> {
                        @SuppressWarnings("unchecked") Future<Void> f = (Future<Void>) x;
                        return FutureMono.from(f);
                    })
                    .collectList()
        ).block(Duration.ofMillis(shutdownTimeoutMs));

        processPendingDisconnects();
        clientList.forEach(this::processRemovedClient);
        server = null;
        nextNetId = 1;
        netIdToEntityId.clear();
        if (mode != NetworkMode.CLIENT && this.entityManager != null) {
            for (EntityRef entity : entityManager.getEntitiesWith(NetworkComponent.class)) {
                NetworkComponent netComp = entity.getComponent(NetworkComponent.class);
                netComp.setNetworkId(0);
                entity.saveComponent(netComp);
            }
            this.entityManager.unsubscribe(this);
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
        ownerLookup.clear();
        ownedLookup.clear();
        ownershipHelper = null;
        storageManager = null;
        logger.info("Network shutdown");
    }

    @Override
    public Client joinLocal(String preferredName, Color color) {
        Client localClient = new LocalClient(preferredName, color, entityManager, context.getValue(Config.class));
        clientList.add(localClient);
        clientPlayerLookup.put(localClient.getEntity(), localClient);
        connectClient(localClient);
        return localClient;
    }

    @Override
    public void update() {
        if (mode != NetworkMode.NONE && entityManager != null) {
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

    void setServer(ServerImpl server) {
        if (server != null) {
            mode = NetworkMode.CLIENT;
            nextNetworkTick = time.getRealTimeInMs();
            logger.info("Connected to server");
        }
        this.server = server;

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
        if (!netClientList.isEmpty()) {
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
            switch (requireNonNull(netComponent.replicateMode)) {
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
                int networkId = netComponent.getNetworkId();
                logger.debug("Unregistering network entity: {} with netId {}", entity, networkId);
                netIdToEntityId.remove(networkId);
                if (mode.isServer()) {
                    for (NetClient client : netClientList) {
                        client.setNetRemoved(networkId);
                    }
                }
                netComponent.setNetworkId(NULL_NET_ID);
                entity.saveComponent(netComponent);
            }
        }
        ownerLookup.remove(entity);
    }

    @Override
    public void connectToEntitySystem(EngineEntityManager newEntityManager, EventLibrary newEventLibrary,
                                      BlockEntityRegistry blockEntityRegistry) {
        if (this.entityManager != null) {
            this.entityManager.unsubscribe(this);
        }
        this.entityManager = newEntityManager;
        this.entityManager.subscribeForChanges(this);
        this.blockManager = context.getValue(BlockManager.class);
        this.ownershipHelper = new OwnershipHelper(newEntityManager.getComponentLibrary());
        this.storageManager = context.get(StorageManager.class);
        this.eventLibrary = newEventLibrary;
        this.componentLibrary = entityManager.getComponentLibrary();

        context.get(ComponentSystemManager.class).register(new NetworkEntitySystem(this), "engine:networkEntitySystem");

        TypeHandlerLibrary typeHandlerLibrary = entityManager.getTypeSerializerLibrary().copy();
        typeHandlerLibrary.addTypeHandler(EntityRef.class, new NetEntityRefTypeHandler(this, blockEntityRegistry));
        // TODO: Add network override types here (that use id lookup tables)

        eventSerializer = new EventSerializer(eventLibrary, typeHandlerLibrary);
        entitySerializer = new NetworkEntitySerializer(newEntityManager, entityManager.getComponentLibrary(),
                typeHandlerLibrary);
        entitySerializer.setComponentSerializeCheck(new NetComponentSerializeCheck());

        if (mode == NetworkMode.CLIENT) {
            entityManager.setEntityRefStrategy(new NetworkClientRefStrategy(this));
            applySerializationTables();
        }

        if (server != null) {
            server.connectToEntitySystem(newEntityManager, entitySerializer, eventSerializer,
                    blockEntityRegistry, context);
        }
    }

    @Override
    public void onEntityComponentAdded(EntityRef entity, Class<? extends Component> component) {
        ComponentMetadata<? extends Component> metadata = componentLibrary.getMetadata(component);
        NetworkComponent netComp = entity.getComponent(NetworkComponent.class);
        if (netComp != null && netComp.getNetworkId() != NULL_NET_ID && mode.isServer() && metadata.isReplicated()) {
            for (NetClient client : netClientList) {
                logger.debug("Component {} added to {}", component, entity);
                client.setComponentAdded(netComp.getNetworkId(), component);
            }
        }
        updatedOwnedEntities(entity, component, metadata);
    }

    @Override
    public void onEntityComponentRemoved(EntityRef entity, Class<? extends Component> component) {
        ComponentMetadata<? extends Component> metadata = componentLibrary.getMetadata(component);
        NetworkComponent netComp = entity.getComponent(NetworkComponent.class);
        if (netComp != null && netComp.getNetworkId() != NULL_NET_ID && mode.isServer() && metadata.isReplicated()) {
            for (NetClient client : netClientList) {
                logger.debug("Component {} removed from {}", component, entity);
                client.setComponentRemoved(netComp.getNetworkId(), component);
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

    private void updatedOwnedEntities(EntityRef entity, Class<? extends Component> component, ComponentMetadata<?
            extends Component> metadata) {
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
     * Sets the context within which this system should operate.
     * <p>
     * As a client transitions from connecting to loading to in-game, it moves
     * through different {@link org.terasology.engine.core.modes.GameState GameStates},
     * and the context changes along the way.
     */
    @Override
    public void setContext(Context newContext) {
        if (context != null && context.isDirectDescendantOf(newContext)) {
            return;  // Already using this context!
        }
        // Our internal context gets internal views of some objects.
        context = new ContextImpl(newContext);
        context.put(NetworkSystemImpl.class, this);
        context.put(EngineTime.class, time);
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
        logger.info("Client disconnected: {}", client.getName()); //NOPMD
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
                NetData.JoinCompleteMessage.newBuilder().setClientId(client.getEntity()
                                .getComponent(NetworkComponent.class)
                                .getNetworkId())).build());
        clientList.add(client);
        netClientList.add(client);
        clientPlayerLookup.put(client.getEntity(), client);

        connectClient(client);

        // log after connect so that the name has been set:
        logger.info("New client entity: {}", client.getEntity()); //NOPMD
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
        for (Module module : context.get(ModuleManager.class).getEnvironment()) {
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
                    .setName(metadata.getId());
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
                    .setName(metadata.getId());
            for (FieldMetadata<?, ?> field : metadata.getFields()) {
                fieldIds.write(field.getId());
                info.addFieldName(field.getName());
            }
            info.setFieldIds(fieldIds.toByteString());
            serverInfoMessageBuilder.addComponent(info);
        }
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
                    logger.error("Class {} has too many fields (>255), serialization will be incomplete", metadata.getId()); //NOPMD
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

    private <T> Map<Class<? extends T>, Integer> applySerializationInfo(List<NetData.SerializationInfo> infoList,
                                                                        ClassLibrary<T> classLibrary) {
        Map<Class<? extends T>, Integer> idTable = Maps.newHashMap();
        for (NetData.SerializationInfo info : infoList) {
            String infoName = info.getName();
            ClassMetadata<? extends T, ?> metadata = classLibrary.getMetadata(infoName);
            if (metadata != null) {
                idTable.put(metadata.getType(), info.getId());
                for (int i = 0; i < info.getFieldIds().size(); ++i) {
                    String fieldName = info.getFieldName(i);
                    FieldMetadata<?, ?> field = metadata.getField(fieldName);
                    if (field != null) {
                        field.setId(info.getFieldIds().byteAt(i));
                    } else {
                        logger.error("Server has unknown field '{}' on '{}'", fieldName, infoName);
                    }
                }
            } else {
                logger.error("Server has unknown class '{}'", infoName);
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
