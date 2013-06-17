package org.terasology.world.chunks.perBlockStorage;

import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.AdvancedConfig;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.mod.Mod;
import org.terasology.logic.mod.ModManager;
import org.terasology.protobuf.ChunksProtobuf;
import org.terasology.world.chunks.Chunk;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.protobuf.ByteString;

public class PerBlockStorageManager { 

    public static final String DefaultBlockStorageFactory = "engine:16-bit-dense";
    public static final String DefaultSunlightStorageFactory = "engine:8-bit-dense";
    public static final String DefaultLightStorageFactory = "engine:8-bit-dense";
    public static final String DefaultExtraStorageFactory = "engine:8-bit-dense";
    
    private static final Logger logger = LoggerFactory.getLogger(PerBlockStorageManager.class);
    
    private Chunk.ProtobufHandler chunkHandler;

    private ModManager mods;
    private Reflections engineReflections;
    
    private Map<String, FactoryEntry> factoriesById;
    private Map<Class<? extends TeraArray>, TeraArray.SerializationHandler<? extends TeraArray>> serializersByClass;
    private Map<String, TeraArray.SerializationHandler<? extends TeraArray>> serializersByClassName;
    private Map<ChunksProtobuf.Type, TeraArray.SerializationHandler<? extends TeraArray>> serializersByProtobufType;
    
    private TeraArray.Factory<TeraArray> blockStorageFactory;
    private TeraArray.Factory<TeraArray> sunlightStorageFactory;
    private TeraArray.Factory<TeraArray> lightStorageFactory;
    private TeraArray.Factory<TeraArray> extraStorageFactory;
    
    @SuppressWarnings("unchecked")
    private Iterable<Class<? extends TeraArray.Factory<? extends TeraArray>>> getArrayFactoryClasses(Class<? extends TeraArray> arrayClass) {
        Preconditions.checkNotNull(arrayClass, "The parameter 'arrayClass' must not be null");
        final Class<?>[] classes = arrayClass.getClasses();
        final List<Class<? extends TeraArray.Factory<? extends TeraArray>>> result = Lists.newArrayList();
        for (Class<?> factoryClass : classes) {
            if (TeraArray.Factory.class.isAssignableFrom(factoryClass) && !Modifier.isAbstract(factoryClass.getModifiers())) {
                result.add((Class<? extends TeraArray.Factory<? extends TeraArray>>) factoryClass);
            }
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private Class<? extends TeraArray.SerializationHandler<? extends TeraArray>> getArraySerializerClass(Class<? extends TeraArray> arrayClass) {
        Preconditions.checkNotNull(arrayClass, "The parameter 'arrayClass' must not be null");
        final Class<?>[] classes = arrayClass.getClasses();
        for (Class<?> serializerClass : classes) {
            if (TeraArray.SerializationHandler.class.isAssignableFrom(serializerClass) && !Modifier.isAbstract(serializerClass.getModifiers())) {
                return (Class<? extends TeraArray.SerializationHandler<? extends TeraArray>>) serializerClass;
            }
        }
        return null;
    }
    
    private void registerTeraArray(Mod mod, Class<? extends TeraArray> arrayClass) {
        Preconditions.checkNotNull(arrayClass, "The parameter 'arrayClass' must not be null");
        if (!Modifier.isAbstract(arrayClass.getModifiers())) {
            final Class<? extends TeraArray.SerializationHandler<? extends TeraArray>> serializerClass = getArraySerializerClass(arrayClass);
            if (serializerClass != null) {
                try {
                    final TeraArray.SerializationHandler<? extends TeraArray> serializer = serializerClass.newInstance();
                    final ChunksProtobuf.Type protobufType = serializer.getProtobufType();
                    if (protobufType != null && protobufType != ChunksProtobuf.Type.Unknown) {
                        if (!serializersByProtobufType.containsKey(protobufType)) {
                            serializersByClass.put(arrayClass, serializer);
                            serializersByClassName.put(arrayClass.getName(), serializer);
                            serializersByProtobufType.put(protobufType, serializer);
                        } else {
                            logger.error("Failed registering per block storage type '{}' for protobuf type '{}'", arrayClass.getSimpleName(), protobufType);
                            return;
                        }
                    } else {
                        serializersByClass.put(arrayClass, serializer);
                        serializersByClassName.put(arrayClass.getName(), serializer);
                    }
                } catch (Exception e) {
                    logger.error("Failed registering per block storage type '{}'", arrayClass.getSimpleName(), e);
                    return;
                }
            } else {
                logger.error("Failed registering per block storage type '{}', no serialization handler found", arrayClass.getSimpleName());
                return;
            }
            final String pakkage = mod != null ? mod.getModInfo().getId() : "engine";
            final Iterable<Class<? extends TeraArray.Factory<? extends TeraArray>>> factoryClasses = getArrayFactoryClasses(arrayClass);
            for (final Class<? extends TeraArray.Factory<? extends TeraArray>> factoryClass : factoryClasses) {
                try {
                    final TeraArray.Factory<? extends TeraArray> factory = factoryClass.newInstance();
                    final String storageId = pakkage + ":" + factory.getId();
                    final FactoryEntry entry = new FactoryEntry(mod, storageId, factory);
                    if (factoriesById.containsKey(storageId)) {
                        logger.warn("Discovered duplicate per block storage type '{}' of class '{}', skipping", storageId, arrayClass.getSimpleName());
                        continue;
                    }
                    factoriesById.put(storageId, entry);
                    if (mod == null)
                        logger.info("Registered per block storage type '{}' of class '{}'", storageId, arrayClass.getSimpleName());
                    else 
                        logger.info("Registered per block storage type '{}' of class '{}' by mod '{}'", storageId, arrayClass.getSimpleName(), mod.getModInfo().getDisplayName());
                } catch (Exception e) {
                    logger.error("Failed registering per block storage type '{}'", arrayClass.getSimpleName(), e);
                }
            }
        }
    }

    private void scan() {
        factoriesById = Maps.newHashMap();
        serializersByClass = Maps.newHashMap();
        serializersByClassName = Maps.newHashMap();
        serializersByProtobufType = Maps.newHashMap();

        // scanning engine for tera arrays
        Set<Class<? extends TeraArray>> teraArrays = engineReflections.getSubTypesOf(TeraArray.class);
        for (final Class<? extends TeraArray> array : teraArrays) 
            registerTeraArray(null, array);

        // scanning mods for tera arrays
        if (mods != null)
            for (final Mod mod : mods.getMods()) {
                final Reflections reflections = mod.getReflections();
                if (reflections != null) {
                    teraArrays = reflections.getSubTypesOf(TeraArray.class);
                    for (final Class<? extends TeraArray> array : teraArrays) 
                        registerTeraArray(mod, array);
                }
            }
    }
    
    @SuppressWarnings("rawtypes")
    public static class FactoryEntry {
        
        public final Mod mod;
        public final String id;
        public final TeraArray.Factory factory;
        
        public FactoryEntry(Mod mod, String id, TeraArray.Factory factory) {
            this.mod = mod;
            this.id = Preconditions.checkNotNull(id, "The parameter 'id' must not be null");
            this.factory = Preconditions.checkNotNull(factory, "The parameter 'factory must not be null");
        }
    }
    
    public PerBlockStorageManager() {
        refresh();
    }

    public ModManager getModManager() {
        if (mods == null) {
            mods = CoreRegistry.get(ModManager.class);
        }
        return mods;
    }
    
    public Reflections getEngineReflections() {
        if (engineReflections == null) {
            if (getModManager() != null)
                engineReflections = mods.getEngineReflections();
            else {
                logger.warn("No mod manager available. Setting up my own reflections.");
                engineReflections = new Reflections(
                        new ConfigurationBuilder()
                                .addClassLoader(getClass().getClassLoader())
                                .addUrls(ClasspathHelper.forPackage("org.terasology", getClass().getClassLoader()))
                                .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner()));
            }
        }
        return engineReflections;
    }
    
    public void refresh() {
        chunkHandler = new Chunk.ProtobufHandler(this);
        if (getEngineReflections() == null) {
            logger.error("Unable to scan for available per block storage types.");
            return;
        }
        scan();
        loadDefaultConfig(false);
    }
    
    public void loadDefaultConfig(boolean force) {
        if (blockStorageFactory == null || force)
            blockStorageFactory = getArrayFactory(DefaultBlockStorageFactory);
        if (sunlightStorageFactory == null || force)
            sunlightStorageFactory = getArrayFactory(DefaultSunlightStorageFactory);
        if (lightStorageFactory == null || force)
            lightStorageFactory = getArrayFactory(DefaultLightStorageFactory);
        if (extraStorageFactory == null || force)
            extraStorageFactory = getArrayFactory(DefaultExtraStorageFactory);
    }
    
    public void loadAdvancedConfig(AdvancedConfig config) {
        Preconditions.checkNotNull(config, "The parameter 'config' must not be null");
        blockStorageFactory = getArrayFactory(config.getBlocksFactoryName());
        if (blockStorageFactory == null) 
            config.setBlocksFactory(DefaultBlockStorageFactory);
        sunlightStorageFactory = getArrayFactory(config.getSunlightFactoryName());
        if (sunlightStorageFactory == null) 
            config.setSunlightFactory(DefaultSunlightStorageFactory);
        lightStorageFactory = getArrayFactory(config.getLightFactoryName());
        if (lightStorageFactory == null) 
            config.setLightFactory(DefaultLightStorageFactory);
        extraStorageFactory = getArrayFactory(config.getExtraFactoryName());
        if (extraStorageFactory == null) 
            config.setExtraFactory(DefaultExtraStorageFactory);
        loadDefaultConfig(false);
    }
    
    public FactoryEntry getArrayFactoryEntry(String id) {
        return factoriesById.get(Preconditions.checkNotNull(id, "The parameter 'id' must not be null"));
    }
    
    @SuppressWarnings("unchecked")
    public TeraArray.Factory<TeraArray> getArrayFactory(String id) {
        final FactoryEntry entry = getArrayFactoryEntry(id);
        if (entry != null)
            return (TeraArray.Factory<TeraArray>) entry.factory;
        return null;
    }
    
    public String[] getArrayFactoryIds() {
        return factoriesById.keySet().toArray(new String[factoriesById.keySet().size()]);
    }
    
    @SuppressWarnings("rawtypes")
    public TeraArray.SerializationHandler getArrayHandler(Class<? extends TeraArray> arrayClass) {
        return serializersByClass.get(Preconditions.checkNotNull(arrayClass, "The parameter 'arrayClass' must not be null"));
    }
    
    @SuppressWarnings("rawtypes")
    public TeraArray.SerializationHandler getArrayHandler(String arrayClassName) {
        return serializersByClassName.get(Preconditions.checkNotNull(arrayClassName, "The parameter 'arrayClassName' must not be null"));
    }
    
    @SuppressWarnings("rawtypes")
    public TeraArray.SerializationHandler getArrayHandler(ChunksProtobuf.Type protobufType) {
        return serializersByProtobufType.get(Preconditions.checkNotNull(protobufType, "The parameter 'protobufType' must not be null"));
    }
    
    @SuppressWarnings("unchecked")
    public ChunksProtobuf.TeraArray encode(TeraArray array) {
        Preconditions.checkNotNull(array, "The parameter 'array' must not be null");
        final TeraArray.SerializationHandler<TeraArray> handler = getArrayHandler(array.getClass());
        if (handler == null)
            throw new IllegalArgumentException("Unable to encode the supplied array of class: " + array.getClass().getName());
        final ChunksProtobuf.TeraArray.Builder b = ChunksProtobuf.TeraArray.newBuilder();
        final ByteBuffer buf = handler.serialize(array, null);
        buf.rewind();
        b.setData(ByteString.copyFrom(buf));
        b.setType(handler.getProtobufType());
        if (handler.getProtobufType() == ChunksProtobuf.Type.Unknown)
            b.setClassName(array.getClass().getName());
        return b.build();
    }
    
    @SuppressWarnings("unchecked")
    public TeraArray decode(ChunksProtobuf.TeraArray message) {
        Preconditions.checkNotNull(message, "The parameter 'message' must not be null");
        if (!message.hasType())
            throw new IllegalArgumentException("Illformed protobuf message. Missing type information.");
        final ChunksProtobuf.Type type = message.getType();
        final TeraArray.SerializationHandler<TeraArray> handler;
        if (type == ChunksProtobuf.Type.Unknown) {
            if (!message.hasClassName())
                throw new IllegalArgumentException("Illformed protobuf message. Missing class name.");
            handler = getArrayHandler(message.getClassName());
            if (handler == null)
                throw new IllegalArgumentException("Unable to decode protobuf message. No entry found for class name: " + message.getClassName());
        } else { 
            handler = getArrayHandler(type);
            if (handler == null)
                throw new IllegalArgumentException("Unable to decode protobuf message. No entry found for type: " + type);
        }
        if (!message.hasData()) 
            throw new IllegalArgumentException("Illformed protobuf message. Missing byte sequence.");
        final ByteString data = message.getData();
        return handler.deserialize(data.asReadOnlyByteBuffer());
    }
    
    public ChunksProtobuf.Chunk encode(Chunk chunk) {
        return chunkHandler.encode(chunk);
    }
    
    public Chunk decode(ChunksProtobuf.Chunk message) {
        return chunkHandler.decode(message);
    }
    
    public TeraArray createBlockStorage(int sizeX, int sizeY, int sizeZ) {
        Preconditions.checkState(blockStorageFactory != null, "Unable to create block storage.");
        return blockStorageFactory.create(sizeX, sizeY, sizeZ);
    }
    
    public TeraArray createSunlightStorage(int sizeX, int sizeY, int sizeZ) {
        Preconditions.checkState(sunlightStorageFactory != null, "Unable to create sunlight storage.");
        return sunlightStorageFactory.create(sizeX, sizeY, sizeZ);
    }
    
    public TeraArray createLightStorage(int sizeX, int sizeY, int sizeZ) {
        Preconditions.checkState(lightStorageFactory != null, "Unable to create light storage.");
        return lightStorageFactory.create(sizeX, sizeY, sizeZ);
    }
    
    public TeraArray createExtraStorage(int sizeX, int sizeY, int sizeZ) {
        Preconditions.checkState(extraStorageFactory != null, "Unable to create extra storage.");
        return extraStorageFactory.create(sizeX, sizeY, sizeZ);
    }
}
