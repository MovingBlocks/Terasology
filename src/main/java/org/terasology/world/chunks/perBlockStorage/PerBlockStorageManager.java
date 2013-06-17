package org.terasology.world.chunks.perBlockStorage;

import java.lang.reflect.Modifier;
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
import org.terasology.game.CoreRegistry;
import org.terasology.logic.mod.Mod;
import org.terasology.logic.mod.ModManager;
import org.terasology.protobuf.ChunksProtobuf;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PerBlockStorageManager { 

    private static final Logger logger = LoggerFactory.getLogger(PerBlockStorageManager.class);
    
    private ModManager mods;
    private Reflections engineReflections;
    
    private Map<String, TeraArray.Factory<? extends TeraArray>> factoriesById;
    private Map<ChunksProtobuf.Type, TeraArray.Factory<? extends TeraArray>> factoriesByProtobufType;
    private Map<String, TeraArray.SerializationHandler<? extends TeraArray>> handlersById;
    private Map<ChunksProtobuf.Type, TeraArray.SerializationHandler<? extends TeraArray>> handlersByProtobufType;
    
    @SuppressWarnings("unchecked")
    private List<Class<? extends TeraArray.Factory<? extends TeraArray>>> getArrayFactoryClasses(Class<? extends TeraArray> array) {
        Preconditions.checkNotNull(array, "The parameter 'array' must not be null");
        final Class<?>[] classes = array.getClasses();
        final List<Class<? extends TeraArray.Factory<? extends TeraArray>>> result = Lists.newArrayList();
        for (Class<?> factory : classes) {
            if (TeraArray.Factory.class.isAssignableFrom(factory) && !Modifier.isAbstract(factory.getModifiers())) {
                result.add((Class<? extends TeraArray.Factory<? extends TeraArray>>) factory);
            }
        }
        return result;
    }
    
    private void registerTeraArray(Mod mod, Class<? extends TeraArray> array) {
        Preconditions.checkNotNull(array, "The parameter 'array' must not be null");
        final String pakkage = mod != null ? mod.getModInfo().getId() : "engine";
        if (!Modifier.isAbstract(array.getModifiers())) {
            final List<Class<? extends TeraArray.Factory<? extends TeraArray>>> factoryClasses = getArrayFactoryClasses(array);
            if (factoryClasses.size() == 0) {
                logger.warn("Discovered invalid per block storage type '{}', no factory discovered, skipping", array.getSimpleName());
                return;
            }
            for (final Class<? extends TeraArray.Factory<? extends TeraArray>> factoryClass : factoryClasses) {
                try {
                    final TeraArray.Factory<? extends TeraArray> factory = factoryClass.newInstance();
                    final TeraArray.SerializationHandler<? extends TeraArray> handler = factory.createSerializationHandler();
                    final ChunksProtobuf.Type protobufType = factory.getProtobufType();
                    final String arrayId = pakkage + ":" + factory.getId();
                    if (handler == null) {
                        logger.warn("Discovered invalid per block storage type '{}' of class '{}', no serialization handler returned, skipping", arrayId, array.getSimpleName());
                        continue;
                    }
                    if (protobufType == null) {
                        logger.warn("Discovered invalid per block storage type '{}' of class '{}', no protobuf type returned, skipping", arrayId, array.getSimpleName());
                        continue;
                    }
                    if (mod != null && protobufType != ChunksProtobuf.Type.Unknown) {
                        logger.warn("Discovered invalid per block storage type '{}' of class '{}', mods may not override protobuf types, skipping", arrayId, array.getSimpleName());
                        continue;
                    }
                    if (factoriesByProtobufType.containsKey(protobufType) || handlersByProtobufType.containsKey(protobufType)) {
                        logger.warn("Discovered duplicate per block storage type '{}' of class '{}' for protobuf type '{}', skipping", arrayId, array.getSimpleName(), protobufType);
                        continue;
                    }
                    if (factoriesById.containsKey(arrayId)) {
                        logger.warn("Discovered duplicate per block storage type '{}' of class '{}', skipping", arrayId, array.getSimpleName());
                        continue;
                    }
                    factoriesById.put(arrayId, factory);
                    handlersById.put(arrayId, handler);
                    if (protobufType != ChunksProtobuf.Type.Unknown) {
                        factoriesByProtobufType.put(protobufType, factory);
                        handlersByProtobufType.put(protobufType, handler);
                        logger.info("Registered per block storage type '{}' of class '{}' with protobuf type '{}'", arrayId, array.getSimpleName(), protobufType);
                    } else {
                        if (mod == null)
                            logger.info("Registered per block storage type '{}' of class '{}'", arrayId, array.getSimpleName(), protobufType);
                        else 
                            logger.info("Registered per block storage type '{}' of class '{}' by mod '{}'", arrayId, array.getSimpleName(), protobufType, mod.getModInfo().getDisplayName());
                    }
                } catch (Exception e) {
                    logger.error("Failed registering per block storage type '{}'", array.getSimpleName(), e);
                }
            }
        }
    }

    private void scanForTeraArrays() {
        factoriesById = Maps.newHashMap();
        factoriesByProtobufType = Maps.newHashMap();
        handlersById = Maps.newHashMap();
        handlersByProtobufType = Maps.newHashMap();
        
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
        if (getEngineReflections() == null) {
            logger.error("Unable to scan for available per block storage types.");
            return;
        }
        scanForTeraArrays();
    }
    
    public TeraArray.Factory<? extends TeraArray> getFactory(String id) {
        return factoriesById.get(Preconditions.checkNotNull(id, "The parameter 'id' must not be null"));
    }
    
    public TeraArray.Factory<? extends TeraArray> getFactory(ChunksProtobuf.Type protobufType) {
        return factoriesById.get(Preconditions.checkNotNull(protobufType, "The parameter 'protobufType' must not be null"));
    }
    
    public TeraArray.SerializationHandler<? extends TeraArray> getSerializationHandler(String id) {
        return handlersById.get(Preconditions.checkNotNull(id, "The parameter 'id' must not be null"));
    }
    
    public TeraArray.SerializationHandler<? extends TeraArray> getSerializationHandler(ChunksProtobuf.Type protobufType) {
        return handlersById.get(Preconditions.checkNotNull(protobufType, "The parameter 'protobufType' must not be null"));
    }
}
