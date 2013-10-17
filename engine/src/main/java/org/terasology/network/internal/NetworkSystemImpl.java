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
import com.google.common.collect.Multimap;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.protobuf.ByteString;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
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
import org.terasology.classMetadata.ClassLibrary;
import org.terasology.classMetadata.ClassMetadata;
import org.terasology.classMetadata.FieldMetadata;
import org.terasology.config.Config;
import org.terasology.config.NetworkConfig;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.EngineTime;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.Module;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.entity.internal.EntityChangeSubscriber;
import org.terasology.entitySystem.entity.internal.OwnershipHelper;
import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.metadata.ComponentMetadata;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.entitySystem.metadata.EventMetadata;
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
import org.terasology.persistence.typeSerialization.TypeSerializationLibrary;
import org.terasology.protobuf.NetData;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.chunks.remoteChunkProvider.RemoteChunkProvider;

import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;

/**
 * Implementation of the Network System using Netty and TCP/IP
 *
 * @author Immortius
 */
public class NetworkSystemImpl implements EntityChangeSubscriber, NetworkSystem {
    private static final Logger logger = LoggerFactory.getLogger(NetworkSystemImpl.class);
    private static final int OWNER_DEPTH_LIMIT = 50;
    private static final int NET_TICK_RATE = 50;
    private static final int NULL_NET_ID = 0;

    // Shared
    private NetworkConfig config;
    private NetworkMode mode = NetworkMode.NONE;
    private EngineEntityManager entityManager;
    private EntitySystemLibrary entitySystemLibrary;
    private EventSerializer eventSerializer;
    private NetworkEntitySerializer entitySerializer;
    private BlockManager blockManager;
    private OwnershipHelper ownershipHelper;

    private ChannelFactory factory;
    private TIntIntMap netIdToEntityId = new TIntIntHashMap();

    private EngineTime time;
    private long nextNetworkTick;


    // Server only
    private ChannelGroup allChannels = new DefaultChannelGroup("tera-channels");
    private BlockingQueue<NetClient> newClients = Queues.newLinkedBlockingQueue();
    private BlockingQueue<NetClient> disconnectedClients = Queues.newLinkedBlockingQueue();
    private int nextNetId = 1;
    private final Set<Client> clientList = Sets.newLinkedHashSet();
    private final Set<NetClient> netClientList = Sets.newLinkedHashSet();
    private Map<EntityRef, Client> clientPlayerLookup = Maps.newHashMap();
    private Map<EntityRef, EntityRef> ownerLookup = Maps.newHashMap();
    private Multimap<EntityRef, EntityRef> ownedLookup = HashMultimap.create();
    private StorageManager storageManager;

    // Client only
    private ServerImpl server;

    public NetworkSystemImpl(EngineTime time) {
        this.time = time;
        this.config = CoreRegistry.get(Config.class).getNetwork();
    }

    @Override
    public void host(int port) throws HostingFailedException {
        if (mode == NetworkMode.NONE) {
            try {
                mode = NetworkMode.SERVER;
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
                logger.info("Started server");

                nextNetworkTick = time.getRawTimeInMs();
            } catch (ChannelException e) {
                if (e.getCause() instanceof BindException) {
                    throw new HostingFailedException("Port already in use (are you already hosting a game?)");
                } else {
                    throw new HostingFailedException("Failed to host game");
                }

            }
        }
    }

    @Override
    public JoinStatus join(String address, int port) {
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
                return new JoinStatusImpl("Failed to connect to server - " + connectCheck.getCause().getMessage());
            } else {
                allChannels.add(connectCheck.getChannel());
                mode = NetworkMode.CLIENT;
                nextNetworkTick = time.getRawTimeInMs();
                logger.info("Connected to server");
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
        if (mode != NetworkMode.NONE) {
            allChannels.close().awaitUninterruptibly();
            factory.releaseExternalResources();
        }
        processPendingDisconnects();
        for (Client client : clientList) {
            processRemovedClient(client);
        }
        mode = NetworkMode.NONE;
        server = null;
        nextNetId = 1;
        netIdToEntityId.clear();
        if (this.entityManager != null) {
            for (EntityRef entity : entityManager.getEntitiesWith(NetworkComponent.class)) {
                NetworkComponent netComp = entity.getComponent(NetworkComponent.class);
                netComp.setNetworkId(0);
                entity.saveComponent(netComp);
            }
            this.entityManager.unsubscribe(this);
        }
        entityManager = null;
        entitySystemLibrary = null;
        eventSerializer = null;
        entitySerializer = null;
        clientList.clear();
        netClientList.clear();
        blockManager = null;
        ownerLookup.clear();
        ownedLookup.clear();
        ownershipHelper = null;
        logger.info("Network shutdown");
    }

    @Override
    public Client joinLocal(String name) {
        Client localClient = new LocalClient(name, entityManager);
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
                long currentTimer = time.getRawTimeInMs();
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
            for (NetClient client : removedPlayers) {
                processRemovedClient(client);
            }
        }
    }

    private void processPendingConnections() {
        if (!newClients.isEmpty()) {
            List<NetClient> newPlayers = Lists.newArrayListWithExpectedSize(newClients.size());
            newClients.drainTo(newPlayers);
            for (NetClient client : newPlayers) {
                processNewClient(client);
            }
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

    public void registerNetworkEntity(EntityRef entity) {
        if (mode == NetworkMode.NONE) {
            return;
        }

        NetworkComponent netComponent = entity.getComponent(NetworkComponent.class);
        if (mode == NetworkMode.SERVER) {
            netComponent.setNetworkId(nextNetId++);
            entity.saveComponent(netComponent);
        }

        logger.debug("Registered Network Entity: {}", entity);

        if (NULL_NET_ID != netIdToEntityId.put(netComponent.getNetworkId(), entity.getId())) {
            logger.error("Registered duplicate network id");
        }

        if (mode == NetworkMode.SERVER) {
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
                        if (entitySystemLibrary.getComponentLibrary().getMetadata(component.getClass()).isReplicated()) {
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

    public void unregisterNetworkEntity(EntityRef entity) {
        NetworkComponent netComponent = entity.getComponent(NetworkComponent.class);
        if (netComponent != null) {
            logger.debug("Unregistering network entity: {} with netId {}", entity, netComponent.getNetworkId());
            netIdToEntityId.remove(netComponent.getNetworkId());
            if (mode == NetworkMode.SERVER) {
                for (NetClient client : netClientList) {
                    client.setNetRemoved(netComponent.getNetworkId());
                }
            }
            netComponent.setNetworkId(NULL_NET_ID);
            entity.saveComponent(netComponent);
        }
        ownerLookup.remove(entity);
    }

    @Override
    public void connectToEntitySystem(EngineEntityManager newEntityManager, EntitySystemLibrary library, BlockEntityRegistry blockEntityRegistry) {
        if (this.entityManager != null) {
            this.entityManager.unsubscribe(this);
        }
        this.entityManager = newEntityManager;
        this.entityManager.subscribe(this);
        this.blockManager = CoreRegistry.get(BlockManager.class);
        this.ownershipHelper = new OwnershipHelper(newEntityManager.getComponentLibrary());
        this.storageManager = CoreRegistry.get(StorageManager.class);
        this.entitySystemLibrary = library;

        CoreRegistry.get(ComponentSystemManager.class).register(new NetworkEntitySystem(this), "engine:networkEntitySystem");

        TypeSerializationLibrary typeSerializationLibrary = new TypeSerializationLibrary(library.getSerializationLibrary());
        typeSerializationLibrary.add(EntityRef.class, new NetEntityRefTypeHandler(this, blockEntityRegistry));
        // TODO: Add network override types here (that use id lookup tables)

        eventSerializer = new EventSerializer(library.getEventLibrary(), typeSerializationLibrary);
        entitySerializer = new NetworkEntitySerializer(newEntityManager, entityManager.getComponentLibrary(), typeSerializationLibrary);
        entitySerializer.setComponentSerializeCheck(new NetComponentSerializeCheck());

        if (mode == NetworkMode.CLIENT) {
            applySerializationTables();
        }

        if (server != null) {
            server.connectToEntitySystem(newEntityManager, entitySerializer, eventSerializer, blockEntityRegistry);
        }
    }

    @Override
    public void onEntityComponentAdded(EntityRef entity, Class<? extends Component> component) {
        ComponentMetadata<? extends Component> metadata = entitySystemLibrary.getComponentLibrary().getMetadata(component);
        NetworkComponent netComp = entity.getComponent(NetworkComponent.class);
        if (netComp != null) {
            if (netComp.getNetworkId() != NULL_NET_ID) {
                switch (mode) {
                    case SERVER:
                        if (metadata.isReplicated()) {
                            for (NetClient client : netClientList) {
                                logger.info("Component {} added to {}", component, entity);
                                client.setComponentAdded(netComp.getNetworkId(), component);
                            }
                        }
                        break;
                }
            }
        }
        updatedOwnedEntities(entity, component, metadata);
    }

    @Override
    public void onEntityComponentRemoved(EntityRef entity, Class<? extends Component> component) {
        ComponentMetadata<? extends Component> metadata = entitySystemLibrary.getComponentLibrary().getMetadata(component);
        NetworkComponent netComp = entity.getComponent(NetworkComponent.class);
        if (netComp != null && netComp.getNetworkId() != NULL_NET_ID) {
            switch (mode) {
                case SERVER:
                    if (metadata.isReplicated()) {
                        for (NetClient client : netClientList) {
                            logger.info("Component {} removed from {}", component, entity);
                            client.setComponentRemoved(netComp.getNetworkId(), component);
                        }
                    }
                    break;
            }
        }
        if (mode.isAuthority() && metadata.isReferenceOwner()) {
            for (EntityRef ownedEntity : ownershipHelper.listOwnedEntities(entity.getComponent(component))) {
                ownedEntity.destroy();
            }
        }
    }

    @Override
    public void onEntityComponentChange(EntityRef entity, Class<? extends Component> component) {
        NetworkComponent netComp = entity.getComponent(NetworkComponent.class);
        ComponentMetadata<? extends Component> metadata = entitySystemLibrary.getComponentLibrary().getMetadata(component);
        if (netComp != null && netComp.getNetworkId() != NULL_NET_ID) {
            switch (mode) {
                case SERVER:
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
            case SERVER:
                int total = 0;
                for (NetClient client : netClientList) {
                    total += client.getMetrics().getReceivedMessagesSinceLastCall();
                }
                return total;
            case CLIENT:
                if (server != null) {
                    return server.getMetrics().getReceivedMessagesSinceLastCall();
                }
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
            case SERVER:
                int total = 0;
                for (NetClient client : netClientList) {
                    total += client.getMetrics().getReceivedBytesSinceLastCall();
                }
                return total;
            case CLIENT:
                if (server != null) {
                    return server.getMetrics().getReceivedBytesSinceLastCall();
                }
            default:
                return 0;
        }
    }

    @Override
    public int getOutgoingMessagesDelta() {
        switch (mode) {
            case SERVER:
                int total = 0;
                for (NetClient client : netClientList) {
                    total += client.getMetrics().getSentMessagesSinceLastCall();
                }
                return total;
            case CLIENT:
                if (server != null) {
                    return server.getMetrics().getSentMessagesSinceLastCall();
                }
            default:
                return 0;
        }
    }

    @Override
    public int getOutgoingBytesDelta() {
        switch (mode) {
            case SERVER:
                int total = 0;
                for (NetClient client : netClientList) {
                    total += client.getMetrics().getSentBytesSinceLastCall();
                }
                return total;
            case CLIENT:
                if (server != null) {
                    return server.getMetrics().getSentBytesSinceLastCall();
                }
            default:
                return 0;
        }
    }

    EntityRef getEntity(int netId) {
        int entityId = netIdToEntityId.get(netId);
        if (entityId != 0) {
            return entityManager.getEntity(entityId);
        }
        return EntityRef.NULL;
    }

    void registerChannel(Channel channel) {
        allChannels.add(channel);
    }

    void addClient(NetClient client) {
        newClients.offer(client);
    }

    void removeClient(NetClient client) {
        disconnectedClients.offer(client);
    }

    private void processRemovedClient(Client client) {
        if (client instanceof NetClient) {
            NetClient netClient = (NetClient) client;
            netClientList.remove(netClient);
        }
        clientList.remove(client);
        clientPlayerLookup.remove(client.getEntity());
        logger.info("Client disconnected: " + client.getName());
        PlayerStore playerStore = storageManager.createPlayerStoreForSave(client.getId());
        client.getEntity().send(new DisconnectedEvent(playerStore));
        playerStore.save();
        client.disconnect();
    }

    private void processNewClient(NetClient client) {
        logger.info("New client connected: {}", client.getName());
        client.connected(entityManager, entitySerializer, eventSerializer, entitySystemLibrary);
        client.send(NetData.NetMessage.newBuilder().setJoinComplete(
                NetData.JoinCompleteMessage.newBuilder().setClientId(client.getEntity().getComponent(NetworkComponent.class).getNetworkId())).build());
        clientList.add(client);
        netClientList.add(client);
        clientPlayerLookup.put(client.getEntity(), client);

        connectClient(client);

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
        NetData.ServerInfoMessage.Builder serverInfoMessageBuilder = NetData.ServerInfoMessage.newBuilder();
        serverInfoMessageBuilder.setTime(time.getGameTimeInMs());
        WorldProvider world = CoreRegistry.get(WorldProvider.class);
        if (world != null) {
            NetData.WorldInfo.Builder worldInfoBuilder = NetData.WorldInfo.newBuilder();
            worldInfoBuilder.setTime(world.getTime().getMilliseconds());
            worldInfoBuilder.setTitle(world.getTitle());
            serverInfoMessageBuilder.addWorldInfo(worldInfoBuilder);
        }
        for (Module module : CoreRegistry.get(ModuleManager.class).getActiveModules()) {
            if (!module.getModuleInfo().isServersideOnly()) {
                serverInfoMessageBuilder.addModule(NetData.ModuleInfo.newBuilder().setModuleId(module.getId()).setModuleVersion(module.getVersion().toString()).build());
            }
        }
        for (Map.Entry<String, Short> blockMapping : blockManager.getBlockIdMap().entrySet()) {
            serverInfoMessageBuilder.addBlockId(blockMapping.getValue());
            serverInfoMessageBuilder.addBlockName(blockMapping.getKey());
        }
        for (BlockFamily registeredBlockFamily : blockManager.listRegisteredBlockFamilies()) {
            serverInfoMessageBuilder.addRegisterBlockFamily(registeredBlockFamily.getURI().toString());
        }
        serializeComponentInfo(serverInfoMessageBuilder);
        serializeEventInfo(serverInfoMessageBuilder);

        return serverInfoMessageBuilder.build();
    }

    private void serializeEventInfo(NetData.ServerInfoMessage.Builder serverInfoMessageBuilder) {
        Map<Class<? extends Event>, Integer> eventIdTable = eventSerializer.getIdMapping();
        for (Map.Entry<Class<? extends Event>, Integer> eventMapping : eventIdTable.entrySet()) {
            ByteString.Output fieldIds = ByteString.newOutput();
            EventMetadata<?> metadata = entitySystemLibrary.getEventLibrary().getMetadata(eventMapping.getKey());
            NetData.SerializationInfo.Builder info = NetData.SerializationInfo.newBuilder()
                    .setId(eventMapping.getValue())
                    .setName(metadata.getUri().toString());
            for (FieldMetadata field : metadata.getFields()) {
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
            ComponentMetadata<?> metadata = entitySystemLibrary.getComponentLibrary().getMetadata(componentIdMapping.getKey());
            NetData.SerializationInfo.Builder info = NetData.SerializationInfo.newBuilder()
                    .setId(componentIdMapping.getValue())
                    .setName(metadata.getUri().toString());
            for (FieldMetadata field : metadata.getFields()) {
                fieldIds.write(field.getId());
                info.addFieldName(field.getName());
            }
            info.setFieldIds(fieldIds.toByteString());
            serverInfoMessageBuilder.addComponent(info);
        }
    }

    void setServer(ServerImpl server) {
        this.server = server;

    }

    private void generateSerializationTables() {
        entitySerializer.setIdMapping(generateIds(entitySystemLibrary.getComponentLibrary()));
        eventSerializer.setIdMapping(generateIds(entitySystemLibrary.getEventLibrary()));
    }

    private <T> Map<Class<? extends T>, Integer> generateIds(ClassLibrary<T> classLibrary) {
        Map<Class<? extends T>, Integer> result = Maps.newHashMap();
        for (ClassMetadata<? extends T, ?> metadata : classLibrary) {
            int index = result.size();
            result.put(metadata.getType(), index);

            int fieldId = 0;
            for (FieldMetadata field : metadata.getFields()) {
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
        NetData.ServerInfoMessage serverInfo = server.getInfo();
        entitySerializer.setIdMapping(applySerializationInfo(serverInfo.getComponentList(), entitySystemLibrary.getComponentLibrary()));
        eventSerializer.setIdMapping(applySerializationInfo(serverInfo.getEventList(), entitySystemLibrary.getEventLibrary()));
    }

    private <T> Map<Class<? extends T>, Integer> applySerializationInfo(List<NetData.SerializationInfo> infoList, ClassLibrary<T> classLibrary) {
        Map<Class<? extends T>, Integer> idTable = Maps.newHashMap();
        for (NetData.SerializationInfo info : infoList) {
            ClassMetadata<? extends T, ?> metadata = classLibrary.getMetadata(new SimpleUri(info.getName()));
            if (metadata != null) {
                idTable.put(metadata.getType(), info.getId());
                for (int i = 0; i < info.getFieldIds().size(); ++i) {
                    FieldMetadata field = metadata.getField(info.getFieldName(i));
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

    void mockHost() {
        mode = NetworkMode.SERVER;
    }

    @Override
    public void onBlockFamilyRegistered(BlockFamily family) {
        if (mode == NetworkMode.SERVER) {
            for (NetClient client : netClientList) {
                client.blockFamilyRegistered(family);
            }
        }
    }


}
