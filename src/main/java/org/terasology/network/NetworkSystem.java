package org.terasology.network;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import org.terasology.entitySystem.EntityManager;
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
import org.terasology.game.CoreRegistry;
import org.terasology.game.Timer;
import org.terasology.logic.mod.Mod;
import org.terasology.logic.mod.ModManager;
import org.terasology.network.events.ConnectedEvent;
import org.terasology.network.pipelineFactory.TerasologyClientPipelineFactory;
import org.terasology.network.pipelineFactory.TerasologyServerPipelineFactory;
import org.terasology.network.serialization.NetComponentSerializeCheck;
import org.terasology.network.serialization.NetEntityRefTypeHandler;
import org.terasology.protobuf.NetData;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.remoteChunkProvider.RemoteChunkProvider;
import sun.reflect.generics.tree.ReturnType;

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
    private int nextNetId = 1;
    private final Set<Client> clientList = Sets.newLinkedHashSet();
    private Map<EntityRef, Client> clientPlayerLookup = Maps.newHashMap();

    // Client only
    private Server server;

    public NetworkSystem(Timer timer) {
        this.timer = timer;
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
            generateSerializationTables();
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
                nextNetworkTick = timer.getTimeInMs();
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
                long currentTimer = timer.getTimeInMs();
                if (currentTimer > nextNetworkTick) {
                    nextNetworkTick += NET_TICK_RATE;
                    for (Client client : clientList) {
                        client.update();
                    }
                    if (server != null) {
                        server.update();
                    }
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

    public void connectToEntitySystem(PersistableEntityManager entityManager, EntitySystemLibrary library, BlockEntityRegistry blockEntityRegistry) {
        if (this.entityManager != null) {
            this.entityManager.unsubscribe(this);
        }
        this.entityManager = entityManager;
        this.entityManager.subscribe(this);

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
    public void onEntityChange(EntityRef entity, Class<? extends Component> component) {
        NetworkComponent netComp = entity.getComponent(NetworkComponent.class);
        if (netComp != null) {
            ComponentMetadata<? extends Component> metadata = entitySystemLibrary.getComponentLibrary().getMetadata(component);
            switch (mode) {
                case SERVER:
                    if (metadata.isReplicated()) {
                        for (Client client : clientList) {
                            client.setNetDirty(netComp.networkId);
                        }
                    }
                    break;
                case CLIENT:
                    if (server != null && metadata.isReplicatedFromOwner() && getOwnerEntity(entity).equals(server.getEntity())) {
                        server.setNetDirty(netComp.networkId);
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
        sendServerInfo(client);

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

    private void sendServerInfo(Client client) {
        NetData.ServerInfoMessage.Builder serverInfoMessageBuilder = NetData.ServerInfoMessage.newBuilder();
        WorldProvider world = CoreRegistry.get(WorldProvider.class);
        serverInfoMessageBuilder.setTime(world.getTime());
        serverInfoMessageBuilder.setWorldName(world.getTitle());
        for (Mod mod : CoreRegistry.get(ModManager.class).getActiveMods()) {
            serverInfoMessageBuilder.addModule(NetData.ModuleInfo.newBuilder().setModuleId(mod.getModInfo().getId()).build());
        }
        for (Map.Entry<String, Byte> blockMapping : BlockManager.getInstance().getBlockIdMap().entrySet()) {
            serverInfoMessageBuilder.addBlockId(blockMapping.getValue());
            serverInfoMessageBuilder.addBlockName(blockMapping.getKey());
        }
        serializeComponentInfo(serverInfoMessageBuilder);
        serializeEventInfo(serverInfoMessageBuilder);

        serverInfoMessageBuilder.setClientId(client.getEntity().getComponent(NetworkComponent.class).networkId);
        client.send(NetData.NetMessage.newBuilder().setType(NetData.NetMessage.Type.SERVER_INFO).setServerInfo(serverInfoMessageBuilder.build()).build());
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

    void removeClient(Client client) {
        synchronized (clientList) {
            clientList.remove(client);
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
                metadata.setFieldId(field, (byte)fieldId);
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
}
