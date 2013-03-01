package org.terasology.network;

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
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Event;
import org.terasology.entitySystem.PersistableEntityManager;
import org.terasology.entitySystem.metadata.ClassLibrary;
import org.terasology.entitySystem.metadata.ClassMetadata;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.ComponentMetadata;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.entitySystem.metadata.EventLibrary;
import org.terasology.entitySystem.metadata.EventMetadata;
import org.terasology.entitySystem.metadata.FieldMetadata;
import org.terasology.entitySystem.metadata.TypeHandler;
import org.terasology.entitySystem.metadata.TypeHandlerLibraryBuilder;
import org.terasology.entitySystem.metadata.internal.EntitySystemLibraryImpl;
import org.terasology.entitySystem.persistence.EventSerializer;
import org.terasology.entitySystem.persistence.PackedEntitySerializer;
import org.terasology.game.ComponentSystemManager;
import org.terasology.game.CoreRegistry;
import org.terasology.game.Timer;
import org.terasology.logic.manager.MessageManager;
import org.terasology.logic.mod.Mod;
import org.terasology.logic.mod.ModManager;
import org.terasology.network.events.ConnectedEvent;
import org.terasology.network.events.DisconnectedEvent;
import org.terasology.network.pipelineFactory.TerasologyClientPipelineFactory;
import org.terasology.network.pipelineFactory.TerasologyServerPipelineFactory;
import org.terasology.network.serialization.NetComponentSerializeCheck;
import org.terasology.network.serialization.NetEntityRefTypeHandler;
import org.terasology.protobuf.NetData;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.remoteChunkProvider.RemoteChunkProvider;

import java.io.IOException;
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
    public static final int OWNER_DEPTH_LIMIT = 50;
    private static final int NET_TICK_RATE = 50;
    private static final int NULL_NET_ID = 0;

    // Shared
    private NetworkMode mode = NetworkMode.NONE;
    private PersistableEntityManager entityManager;
    private EntitySystemLibrary entitySystemLibrary;
    private EventSerializer eventSerializer;
    private PackedEntitySerializer entitySerializer;

    private ChannelFactory factory;
    private TIntIntMap netIdToEntityId = new TIntIntHashMap();

    private Timer timer;
    private long nextNetworkTick = 0;

    // Server only
    private ChannelGroup allChannels = new DefaultChannelGroup("tera-channels");
    private BlockingQueue<Client> newClients = Queues.newLinkedBlockingQueue();
    private BlockingQueue<Client> disconnectedClients = Queues.newLinkedBlockingQueue();
    private int nextNetId = 1;
    private final Set<Client> clientList = Sets.newLinkedHashSet();
    private Map<EntityRef, Client> clientPlayerLookup = Maps.newHashMap();
    private Map<EntityRef, EntityRef> ownerLookup = Maps.newHashMap();
    private Multimap<EntityRef, EntityRef> ownedLookup = HashMultimap.create();

    // Client only
    private Server server;

    public NetworkSystem(Timer timer) {
        this.timer = timer;
    }

    public void host(int port) {
        if (mode == NetworkMode.NONE) {
            mode = NetworkMode.SERVER;
            for (EntityRef entity : entityManager.iteratorEntities(NetworkComponent.class)) {
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

            nextNetworkTick = timer.getTimeInMs();
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
                nextNetworkTick = 0;
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
                if (!disconnectedClients.isEmpty()) {
                    List<Client> removedPlayers = Lists.newArrayListWithExpectedSize(disconnectedClients.size());
                    disconnectedClients.drainTo(removedPlayers);
                    for (Client client : removedPlayers) {
                        processRemovedClient(client);
                    }
                }
                long currentTimer = timer.getRawTimeInMs();
                boolean netTick = false;
                if (currentTimer > nextNetworkTick) {
                    nextNetworkTick += NET_TICK_RATE;
                    netTick = true;
                }
                for (Client client : clientList) {
                    client.update(netTick);
                }
                if (server != null) {
                    server.update(netTick);
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
        EntityRef owner = getOwnerEntity(entity);
        return clientPlayerLookup.get(owner);
    }

    public EntityRef getOwnerEntity(EntityRef entity) {
        EntityRef owner = entity;
        NetworkComponent ownerNetComp = entity.getComponent(NetworkComponent.class);
        int i = 0;
        while (ownerNetComp != null && i++ < OWNER_DEPTH_LIMIT) {
            NetworkComponent netComp = ownerNetComp.owner.getComponent(NetworkComponent.class);
            if (netComp != null) {
                owner = ownerNetComp.owner;
                ownerNetComp = netComp;
            } else {
                ownerNetComp = null;
            }
        }
        return owner;
    }

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

        netIdToEntityId.put(netComponent.getNetworkId(), entity.getId());

        if (mode == NetworkMode.SERVER) {
            switch (netComponent.replicateMode) {
                case OWNER:
                    Client clientPlayer = getOwner(entity);
                    if (clientPlayer != null) {
                        clientPlayer.setNetInitial(netComponent.getNetworkId());
                    }
                    break;
                default:
                    for (Client client : clientList) {
                        // TODO: Relevance Check
                        client.setNetInitial(netComponent.getNetworkId());
                    }
                    break;
            }
            if (netComponent.owner.exists()) {
                ownerLookup.put(entity, netComponent.owner);
                ownedLookup.put(netComponent.owner, entity);
            }
        }

    }

    public void updateNetworkEntity(EntityRef entity) {
        NetworkComponent netComponent = entity.getComponent(NetworkComponent.class);

        EntityRef lastOwnerEntity = ownerLookup.get(entity);
        if (lastOwnerEntity == null) {
            lastOwnerEntity = EntityRef.NULL;
        }

        if (!Objects.equal(lastOwnerEntity, netComponent.owner)) {
            Client lastOwner = (lastOwnerEntity == null) ? null : getOwner(lastOwnerEntity);
            Client newOwner = getOwner(netComponent.owner);

            if (!Objects.equal(lastOwner, newOwner)) {
                recursiveUpdateOwnership(entity, lastOwner, newOwner);
            }

            if (lastOwnerEntity.exists()) {
                ownedLookup.remove(lastOwnerEntity, entity);
            }
            if (netComponent.owner.exists()) {
                ownerLookup.put(entity, netComponent.owner);
                ownedLookup.put(netComponent.owner, entity);
            } else {
                ownerLookup.remove(entity);
            }
        }
    }

    private void recursiveUpdateOwnership(EntityRef entity, Client lastOwner, Client newOwner) {
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
                for (Client client : clientList) {
                    client.setNetRemoved(netComponent.getNetworkId());
                }
            }
        }
        ownerLookup.remove(entity);
    }

    public void connectToEntitySystem(PersistableEntityManager entityManager, EntitySystemLibrary library, BlockEntityRegistry blockEntityRegistry) {
        if (this.entityManager != null) {
            this.entityManager.unsubscribe(this);
        }
        this.entityManager = entityManager;
        this.entityManager.subscribe(this);

        CoreRegistry.get(ComponentSystemManager.class).register(new NetworkEntitySystem(), "engine:networkEntitySystem");

        TypeHandlerLibraryBuilder builder = new TypeHandlerLibraryBuilder();
        for (Map.Entry<Class<?>, TypeHandler<?>> entry : library.getTypeHandlerLibrary()) {
            builder.addRaw(entry.getKey(), entry.getValue());
        }
        builder.add(EntityRef.class, new NetEntityRefTypeHandler(this, blockEntityRegistry));
        // TODO: Add network override types here (that use id lookup tables)

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
        entitySerializer = new PackedEntitySerializer(entityManager, componentLibrary);
        entitySerializer.setComponentSerializeCheck(new NetComponentSerializeCheck());

        if (mode == NetworkMode.CLIENT) {
            applySerializationTables();
        }

        if (server != null) {
            server.connectToEntitySystem(entityManager, entitySerializer, eventSerializer, blockEntityRegistry);
        }
    }

    @Override
    public void onEntityComponentAdded(EntityRef entity, Class<? extends Component> component) {
        NetworkComponent netComp = entity.getComponent(NetworkComponent.class);
        if (netComp != null && netComp.getNetworkId() != NULL_NET_ID) {
            ComponentMetadata<? extends Component> metadata = entitySystemLibrary.getComponentLibrary().getMetadata(component);
            switch (mode) {
                case SERVER:
                    if (metadata.isReplicated()) {
                        for (Client client : clientList) {
                            logger.info("Component {} added to {}", component, entity);
                            client.setComponentAdded(netComp.getNetworkId(), component);
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void onEntityComponentRemoved(EntityRef entity, Class<? extends Component> component) {
        NetworkComponent netComp = entity.getComponent(NetworkComponent.class);
        if (netComp != null && netComp.getNetworkId() != NULL_NET_ID) {
            ComponentMetadata<? extends Component> metadata = entitySystemLibrary.getComponentLibrary().getMetadata(component);
            switch (mode) {
                case SERVER:
                    if (metadata.isReplicated()) {
                        for (Client client : clientList) {
                            logger.info("Component {} removed from {}", component, entity);
                            client.setComponentRemoved(netComp.getNetworkId(), component);
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void onEntityComponentChange(EntityRef entity, Class<? extends Component> component) {
        NetworkComponent netComp = entity.getComponent(NetworkComponent.class);
        if (netComp != null && netComp.getNetworkId() != NULL_NET_ID) {
            ComponentMetadata<? extends Component> metadata = entitySystemLibrary.getComponentLibrary().getMetadata(component);
            switch (mode) {
                case SERVER:
                    if (metadata.isReplicated()) {
                        for (Client client : clientList) {
                            client.setComponentDirty(netComp.getNetworkId(), component);
                        }
                    }
                    break;
                case CLIENT:
                    if (server != null && metadata.isReplicatedFromOwner() && getOwnerEntity(entity).equals(server.getEntity())) {
                        server.setComponentDirty(netComp.getNetworkId(), component);
                    }
                    break;
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
            return entityManager.getEntity(entityId);
        }
        return EntityRef.NULL;
    }

    void registerChannel(Channel channel) {
        allChannels.add(channel);
    }

    void addClient(Client client) {
        newClients.offer(client);
    }

    void removeClient(Client client) {
        disconnectedClients.offer(client);
        synchronized (clientList) {
            clientList.remove(client);
        }
    }

    private void processRemovedClient(Client client) {
        clientList.remove(client);
        logger.info("Client disconnected: " + client.getName());
        MessageManager.getInstance().addMessage("Client disconnected: " + client.getName());
        client.getEntity().send(new DisconnectedEvent());
        client.disconnect();
    }

    private void processNewClient(Client client) {
        logger.info("New client connected: {}", client.getName());
        client.connected(entityManager, entitySerializer, eventSerializer, entitySystemLibrary);
        clientList.add(client);
        clientPlayerLookup.put(client.getEntity(), client);
        sendServerInfo(client);

        client.getEntity().send(new ConnectedEvent());
        logger.info("New client entity: {}", client.getEntity());
        for (EntityRef netEntity : entityManager.iteratorEntities(NetworkComponent.class)) {
            NetworkComponent netComp = netEntity.getComponent(NetworkComponent.class);
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

    private void sendServerInfo(Client client) {
        NetData.ServerInfoMessage.Builder serverInfoMessageBuilder = NetData.ServerInfoMessage.newBuilder();
        WorldProvider world = CoreRegistry.get(WorldProvider.class);
        if (world != null) {
            serverInfoMessageBuilder.setTime(world.getTime());
            serverInfoMessageBuilder.setWorldName(world.getTitle());
        }
        for (Mod mod : CoreRegistry.get(ModManager.class).getActiveMods()) {
            serverInfoMessageBuilder.addModule(NetData.ModuleInfo.newBuilder().setModuleId(mod.getModInfo().getId()).build());
        }
        for (Map.Entry<String, Byte> blockMapping : BlockManager.getInstance().getBlockIdMap().entrySet()) {
            serverInfoMessageBuilder.addBlockId(blockMapping.getValue());
            serverInfoMessageBuilder.addBlockName(blockMapping.getKey());
        }
        serializeComponentInfo(serverInfoMessageBuilder);
        serializeEventInfo(serverInfoMessageBuilder);

        serverInfoMessageBuilder.setClientId(client.getEntity().getComponent(NetworkComponent.class).getNetworkId());
        client.send(NetData.NetMessage.newBuilder().setTime(timer.getTimeInMs()).setServerInfo(serverInfoMessageBuilder).build());
    }

    private void serializeEventInfo(NetData.ServerInfoMessage.Builder serverInfoMessageBuilder) {
        Map<Class<? extends Event>, Integer> eventIdTable = eventSerializer.getIdMapping();
        for (Map.Entry<Class<? extends Event>, Integer> eventMapping : eventIdTable.entrySet()) {
            ByteString.Output fieldIds = ByteString.newOutput();
            EventMetadata<?> metadata = entitySystemLibrary.getEventLibrary().getMetadata(eventMapping.getKey());
            NetData.SerializationInfo.Builder info = NetData.SerializationInfo.newBuilder()
                    .setId(eventMapping.getValue())
                    .setName(metadata.getName());
            for (FieldMetadata field : metadata.iterateFields()) {
                try {
                    fieldIds.write(field.getId());
                    info.addFieldName(field.getName());
                } catch (IOException e) {
                    // TODO: Disconnect client and fail more gracefully
                    throw new RuntimeException("Failed writing field id " + field.getId() + " for field " + field.getName());
                }
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
                    .setName(metadata.getName());
            for (FieldMetadata field : metadata.iterateFields()) {
                try {
                    fieldIds.write(field.getId());
                    info.addFieldName(field.getName());
                } catch (IOException e) {
                    // TODO: Disconnect client and fail more gracefully
                    throw new RuntimeException("Failed writing field id " + field.getId() + " for field " + field.getName());
                }
            }
            info.setFieldIds(fieldIds.toByteString());
            serverInfoMessageBuilder.addComponent(info);
        }
    }

    void setServer(Server server) {
        this.server = server;
    }

    private void generateSerializationTables() {
        entitySerializer.setIdMapping(generateIds(entitySystemLibrary.getComponentLibrary()));
        eventSerializer.setIdMapping(generateIds(entitySystemLibrary.getEventLibrary()));
    }

    private <T> Map<Class<? extends T>, Integer> generateIds(ClassLibrary<T> classLibrary) {
        Map<Class<? extends T>, Integer> result = Maps.newHashMap();
        for (ClassMetadata<? extends T> metadata : classLibrary) {
            int index = result.size();
            result.put(metadata.getType(), index);

            int fieldId = 0;
            for (FieldMetadata field : metadata.iterateFields()) {
                if (fieldId >= 256) {
                    logger.error("Class {} has too many fields (>255), serialization will be incomplete", metadata.getName());
                    break;
                }
                metadata.setFieldId(field, (byte) fieldId);
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
            ClassMetadata<? extends T> metadata = classLibrary.getMetadata(info.getName());
            if (metadata != null) {
                idTable.put(metadata.getType(), info.getId());
                for (int i = 0; i < info.getFieldIds().size(); ++i) {
                    FieldMetadata field = metadata.getField(info.getFieldName(i));
                    if (field != null) {
                        metadata.setFieldId(field, info.getFieldIds().byteAt(i));
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
}
