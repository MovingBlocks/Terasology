package org.terasology.world.chunks.perBlockStorage;

import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
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
import com.google.common.collect.Maps;
import com.google.protobuf.ByteString;

@SuppressWarnings({"rawtypes", "unchecked"})
public class PerBlockStorageManager { 

    public static final String DefaultBlockStorageFactory = "engine:16-bit-dense";
    public static final String DefaultSunlightStorageFactory = "engine:8-bit-dense";
    public static final String DefaultLightStorageFactory = "engine:8-bit-dense";
    public static final String DefaultExtraStorageFactory = "engine:8-bit-dense";
    
    private static final Logger logger = LoggerFactory.getLogger(PerBlockStorageManager.class);
    
    private Chunk.ProtobufHandler chunkProtobufHandler;
    private Chunk.Deflator chunkDeflator;

    private ModManager mods;
    private Reflections engineReflections;
    
    private Map<String, FactoryEntry> factoriesById;
    private Map<Class<TeraArray>, TeraArray.Deflator> deflatorsByClass;
    private Map<Class<TeraArray>, TeraArray.SerializationHandler> serializersByClass;
    private Map<String, TeraArray.SerializationHandler> serializersByClassName;
    private Map<ChunksProtobuf.Type, TeraArray.SerializationHandler> serializersByProtobufType;
    
    private TeraArray.Factory blockStorageFactory;
    private TeraArray.Factory sunlightStorageFactory;
    private TeraArray.Factory lightStorageFactory;
    private TeraArray.Factory extraStorageFactory;
    
    private boolean chunkDeflationEnabled = true;
    private boolean chunkDeflationLoggingEnabled = false;
    
    private void registerSerializerClass(Mod mod, Class<TeraArray.SerializationHandler> serializerClass) {
        Preconditions.checkNotNull(serializerClass, "The parameter 'serializerClass' must not be null");
        if (Modifier.isAbstract(serializerClass.getModifiers())) {
            return;
        }
        final String byMod = (mod != null) ? " by mod '" + mod.getModInfo().getDisplayName() + "'" : "";
        final Class<?> enclosingClass = serializerClass.getEnclosingClass();
        if (enclosingClass == null || !TeraArray.class.isAssignableFrom(enclosingClass)) {
            logger.warn("Discovered invalid per-block-storage serialization handler '{}'{}, skipping", serializerClass.getName(), byMod);
            return;
        }
        if (Modifier.isAbstract(enclosingClass.getModifiers())) {
            return;
        }
        final Class<TeraArray> arrayClass = (Class<TeraArray>) enclosingClass;
        final String className = arrayClass.getSimpleName() + "." + serializerClass.getSimpleName();
        if (serializersByClass.containsKey(arrayClass) || serializersByClassName.containsKey(arrayClass.getName())) {
            logger.warn("Discovered duplicate per-block-storage serialization handler '{}'{}, skipping", className, byMod);
            return;
        }
        try {
            final TeraArray.SerializationHandler serializer = serializerClass.newInstance();
            final ChunksProtobuf.Type protobufType = serializer.getProtobufType();
            if (protobufType == null || protobufType == ChunksProtobuf.Type.Unknown) {
                serializersByClass.put(arrayClass, serializer);
                serializersByClassName.put(arrayClass.getName(), serializer);
                logger.info("Registered per-block-storage serialization handler of class '{}'{}", className, byMod);
                return;
            }
            if (serializersByProtobufType.containsKey(protobufType)) {
                logger.warn("Discovered duplicate per-block-storage serialization handler of class '{}' for protobuf type '{}'{}, skipping", className, protobufType, byMod);
                return;
            }
            serializersByClass.put(arrayClass, serializer);
            serializersByClassName.put(arrayClass.getName(), serializer);
            serializersByProtobufType.put(protobufType, serializer);
            logger.info("Registered per-block-storage serialization handler of class '{}'{}", className, byMod);
        } catch (Exception e) {
            logger.error("Failed registering per-block-storage serialization handler of class '{}'{}", className, byMod, e);
        }
    }
    
    private void registerFactoryClass(Mod mod, Class<? extends TeraArray.Factory> factoryClass) {
        Preconditions.checkNotNull(factoryClass, "The parameter 'factoryClass' must not be null");
        if (Modifier.isAbstract(factoryClass.getModifiers())) {
            return;
        }
        final String byMod = (mod != null) ? " by mod '" + mod.getModInfo().getDisplayName() + "'" : "";
        final String pakkage = mod != null ? mod.getModInfo().getId() : "engine";
        final Class<?> enclosingClass = factoryClass.getEnclosingClass();
        final String className = (enclosingClass != null && TeraArray.class.isAssignableFrom(enclosingClass)) ? enclosingClass.getSimpleName() + "." + factoryClass.getSimpleName() : factoryClass.getSimpleName();
        try {
            final TeraArray.Factory factory = factoryClass.newInstance();
            final String storageId = pakkage + ":" + factory.getId();
            if (factoriesById.containsKey(storageId)) {
                logger.warn("Discovered duplicate per-block-storage factory '{}' of class '{}'{}, skipping", storageId, className, byMod);
                return;
            }
            final FactoryEntry entry = new FactoryEntry(mod, storageId, factory);
            factoriesById.put(storageId, entry);
            logger.info("Registered per-block-storage factory '{}' of class '{}'{}", storageId, className, byMod);
        } catch (Exception e) {
            logger.error("Failed registering per-block-storage factory of class '{}'{}", className, byMod, e);
        }
    }

    private void registerDeflatorClass(Mod mod, Class<? extends TeraArray.Deflator> deflatorClass) {
        Preconditions.checkNotNull(deflatorClass, "The parameter 'deflatorClass' must not be null");
        if (Modifier.isAbstract(deflatorClass.getModifiers())) {
            return;
        }
        final String byMod = (mod != null) ? " by mod '" + mod.getModInfo().getDisplayName() + "'" : "";
        final Class<?> enclosingClass = deflatorClass.getEnclosingClass();
        if (enclosingClass == null || !TeraArray.class.isAssignableFrom(enclosingClass)) {
            logger.warn("Discovered invalid per-block-storage deflator '{}'{}, skipping", deflatorClass.getName(), byMod);
            return;
        }
        if (Modifier.isAbstract(enclosingClass.getModifiers())) {
            return;
        }
        final Class<TeraArray> arrayClass = (Class<TeraArray>) enclosingClass;
        final String className = arrayClass.getSimpleName() + "." + deflatorClass.getSimpleName();
        if (deflatorsByClass.containsKey(arrayClass)) {
            logger.warn("Discovered duplicate per-block-storage deflator '{}'{}, skipping", className, byMod);
            return;
        }
        try {
            final TeraArray.Deflator deflator = deflatorClass.newInstance();
            deflatorsByClass.put(arrayClass, deflator);
            logger.info("Registered per-block-storage deflator of class '{}'{}", className, byMod);
        } catch (Exception e) {
            logger.error("Failed registering per-block-storage deflator of class '{}'{}", className, byMod, e);
        }
    }

    private void scan(Mod mod) {
        final Reflections reflections = (mod == null) ? engineReflections : mod.getReflections();
        
        if (reflections == null)
            return;
        
        Set<Class<? extends TeraArray.Factory>> factoryClasses = reflections.getSubTypesOf(TeraArray.Factory.class);
        for (final Class<? extends TeraArray.Factory> factoryClass : factoryClasses) 
            registerFactoryClass(mod, factoryClass);

        Set<Class<? extends TeraArray.Deflator>> deflatorClasses = reflections.getSubTypesOf(TeraArray.Deflator.class);
        for (final Class<? extends TeraArray.Deflator> deflatorClass : deflatorClasses) 
            registerDeflatorClass(mod, deflatorClass);

        Set<Class<? extends TeraArray.SerializationHandler>> serializerClasses = reflections.getSubTypesOf(TeraArray.SerializationHandler.class);
        for (final Class<? extends TeraArray.SerializationHandler> serializerClass : serializerClasses) 
            registerSerializerClass(mod, (Class<TeraArray.SerializationHandler>) serializerClass);
    }
    
    private void scan() {
        // scan the engine
        scan(null);
        // scan all available mods
        if (mods != null)
            for (final Mod mod : mods.getMods()) 
                scan(mod);
    }
    
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
    
    public static class ExtensionEntry {
        
        public final Mod mod;
        public final String extensionId;
        public final String factoryId;
        public final TeraArray.Factory factory;
        
        public ExtensionEntry(Mod mod, String extensionId, FactoryEntry factoryEntry) {
            this.mod = Preconditions.checkNotNull(mod, "The parameter 'mod' must not be null");
            this.extensionId = Preconditions.checkNotNull(extensionId, "The parameter 'extensionId' must not be null");
            Preconditions.checkNotNull(factoryEntry, "The parameter 'factoryEntry' must not be null");
            this.factoryId = factoryEntry.id;
            this.factory = factoryEntry.factory;
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
        chunkProtobufHandler = new Chunk.ProtobufHandler(this);
        chunkDeflator = new Chunk.Deflator(this);
        factoriesById = Maps.newHashMap();
        deflatorsByClass = Maps.newHashMap();
        serializersByClass = Maps.newHashMap();
        serializersByClassName = Maps.newHashMap();
        serializersByProtobufType = Maps.newHashMap();
        if (getEngineReflections() == null) {
            logger.error("Unable to scan for available per-block-storage types.");
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
        if (force) {
            chunkDeflationEnabled = true;
            chunkDeflationLoggingEnabled = false;
        }
    }
    
    public void loadConfig(AdvancedConfig config) {
        if (config != null) {
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
            chunkDeflationEnabled = config.isChunkDeflationEnabled();
            chunkDeflationLoggingEnabled = config.isChunkDeflationLoggingEnabled();
        }
        loadDefaultConfig(false);
    }
    
    public FactoryEntry getArrayFactoryEntry(String id) {
        return factoriesById.get(Preconditions.checkNotNull(id, "The parameter 'id' must not be null"));
    }
    
    public TeraArray.Factory getArrayFactory(String id) {
        final FactoryEntry entry = getArrayFactoryEntry(id);
        if (entry != null)
            return entry.factory;
        return null;
    }
    
    public String[] getArrayFactoryIds() {
        return factoriesById.keySet().toArray(new String[factoriesById.keySet().size()]);
    }
    
    public TeraArray.SerializationHandler getArrayHandler(Class<? extends TeraArray> arrayClass) {
        return serializersByClass.get(Preconditions.checkNotNull(arrayClass, "The parameter 'arrayClass' must not be null"));
    }
    
    public TeraArray.SerializationHandler getArrayHandler(String arrayClassName) {
        return serializersByClassName.get(Preconditions.checkNotNull(arrayClassName, "The parameter 'arrayClassName' must not be null"));
    }
    
    public TeraArray.SerializationHandler getArrayHandler(ChunksProtobuf.Type protobufType) {
        return serializersByProtobufType.get(Preconditions.checkNotNull(protobufType, "The parameter 'protobufType' must not be null"));
    }
    
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
        return chunkProtobufHandler.encode(chunk);
    }
    
    public Chunk decode(ChunksProtobuf.Chunk message) {
        return chunkProtobufHandler.decode(message);
    }
    
    public TeraArray deflate(TeraArray array) {
        Preconditions.checkNotNull(array, "The parameter 'input' must not be null");
        final TeraArray.Deflator deflator = deflatorsByClass.get(array.getClass());
        if (deflator != null)
            return deflator.deflate(array);
        return array;
    }
    
    public boolean isChunkDeflationEnabled() {
        return chunkDeflationEnabled;
    }
    
    public boolean isChunkDeflationLoggingEnabled() {
        return chunkDeflationLoggingEnabled;
    }
    
    public void deflate(Chunk chunk) {
        chunkDeflator.deflate(chunk);
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
