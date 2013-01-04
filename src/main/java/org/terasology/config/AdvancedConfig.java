package org.terasology.config;

import java.lang.reflect.Type;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.world.chunks.blockdata.TeraArray;
import org.terasology.world.chunks.blockdata.TeraDenseArray16Bit;
import org.terasology.world.chunks.blockdata.TeraDenseArray4Bit;
import org.terasology.world.chunks.blockdata.TeraDenseArray8Bit;
import org.terasology.world.chunks.blockdata.TeraSparseArray16Bit;
import org.terasology.world.chunks.blockdata.TeraSparseArray4Bit;
import org.terasology.world.chunks.blockdata.TeraSparseArray8Bit;
import org.terasology.world.chunks.blockdata.TeraArray.Factory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Allows to configure internal details of the Terasology engine.
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 *
 */
public final class AdvancedConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(AdvancedConfig.class);
    private static final Map<String, TeraArray.Factory> teraArrayRegistry; 

    private String blocksFactory, sunlightFactory, lightFactory, liquidFactory;
    private boolean chunkDeflationEnabled, chunkDeflationLoggingEnabled;
    
    private AdvancedConfig() {}
    
    public String getBlocksFactoryName() {
        return blocksFactory;
    }
    
    public Factory getBlocksFactory() {
        return requireTeraArrayFactory(blocksFactory);
    }
    
    public AdvancedConfig setBlocksFactory(String factory) {
        checkContainsTeraArrayFactory(factory);
        blocksFactory = factory;
        return this;
    }
    
    public AdvancedConfig setBlocksFactoryDontThrow(String factory) {
        if (containsTeraArrayFactory(factory)) {
            blocksFactory = factory;
        } else {
            logger.warn("TeraArray factory does not exist: '{}'", factory);
        }
        return this;
    }
    
    public String getSunlightFactoryName() {
        return sunlightFactory;
    }
    
    public Factory getSunlightFactory() {
        return requireTeraArrayFactory(sunlightFactory);
    }
    
    public AdvancedConfig setSunlightFactory(String factory) {
        checkContainsTeraArrayFactory(factory);
        sunlightFactory = factory;
        return this;
    }
    
    public AdvancedConfig setSunlightFactoryDontThrow(String factory) {
        if (containsTeraArrayFactory(factory)) {
            sunlightFactory = factory;
        } else {
            logger.warn("TeraArray factory does not exist: '{}'", factory);
        }
        return this;
    }
    
    public String getLightFactoryName() {
        return lightFactory;
    }
    
    public Factory getLightFactory() {
        return requireTeraArrayFactory(lightFactory);
    }
    
    public AdvancedConfig setLightFactory(String factory) {
        checkContainsTeraArrayFactory(factory);
        lightFactory = factory;
        return this;
    }
    
    public AdvancedConfig setLightFactoryDontThrow(String factory) {
        if (containsTeraArrayFactory(factory)) {
            lightFactory = factory;
        } else {
            logger.warn("TeraArray factory does not exist: '{}'", factory);
        }
        return this;
    }
    
    public String getLiquidFactoryName() {
        return liquidFactory;
    }
    
    public Factory getLiquidFactory() {
        return requireTeraArrayFactory(liquidFactory);
    }
    
    public AdvancedConfig setLiquidFactory(String factory) {
        checkContainsTeraArrayFactory(factory);
        liquidFactory = factory;
        return this;
    }
    
    public AdvancedConfig setLiquidFactoryDontThrow(String factory) {
        if (containsTeraArrayFactory(factory)) {
            liquidFactory = factory;
        } else {
            logger.warn("TeraArray factory does not exist: '{}'", factory);
        }
        return this;
    }
    
    public boolean isChunkDeflationEnabled() {
        return chunkDeflationEnabled;
    }
    
    public AdvancedConfig setChunkDeflationEnabled(boolean enabled) {
        chunkDeflationEnabled = enabled;
        return this;
    }
    
    public boolean isChunkDeflationLoggingEnabled() {
        return chunkDeflationLoggingEnabled;
    }
    
    public AdvancedConfig setChunkDeflationLoggingEnabled(boolean enabled) {
        chunkDeflationLoggingEnabled = enabled;
        return this;
    }
    
    public static AdvancedConfig createDefault() {
        return new AdvancedConfig()
        .setBlocksFactory(TeraDenseArray8Bit.class.getName())
        .setSunlightFactory(TeraDenseArray8Bit.class.getName())
        .setLightFactory(TeraDenseArray8Bit.class.getName())
        .setLiquidFactory(TeraDenseArray8Bit.class.getName())
        .setChunkDeflationEnabled(true)
        .setChunkDeflationLoggingEnabled(false);
    }

    public static class Handler implements JsonSerializer<AdvancedConfig>, JsonDeserializer<AdvancedConfig> {

        @Override
        public AdvancedConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            AdvancedConfig config = AdvancedConfig.createDefault();
            JsonObject input = json.getAsJsonObject();
            config.setBlocksFactoryDontThrow(input.get("blocksFactory").getAsString())
            .setSunlightFactoryDontThrow(input.get("sunlightFactory").getAsString())
            .setLightFactoryDontThrow(input.get("lightFactory").getAsString())
            .setLiquidFactoryDontThrow(input.get("liquidFactory").getAsString())
            .setChunkDeflationEnabled(input.get("chunkDeflationEnabled").getAsBoolean())
            .setChunkDeflationLoggingEnabled(input.get("chunkDeflationLoggingEnabled").getAsBoolean());
            return config;
        }

        @Override
        public JsonElement serialize(AdvancedConfig src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            result.addProperty("blocksFactory", src.blocksFactory);
            result.addProperty("sunlightFactory", src.sunlightFactory);
            result.addProperty("lightFactory", src.lightFactory);
            result.addProperty("liquidFactory", src.liquidFactory);
            result.addProperty("chunkDeflationEnabled", src.chunkDeflationEnabled);
            result.addProperty("chunkDeflationLoggingEnabled", src.chunkDeflationLoggingEnabled);
            return result;
        }
        
    }
    
    static {
        teraArrayRegistry = Maps.newHashMap();
        registerTeraArrayFactory(new Factory() {
            @Override
            public String getName() {return TeraDenseArray4Bit.class.getName();}
            @Override
            public TeraArray create(int sizeX, int sizeY, int sizeZ) {
                return new TeraDenseArray4Bit(sizeX, sizeY, sizeZ);
            }
        });
        registerTeraArrayFactory(new Factory() {
            @Override
            public String getName() {return TeraDenseArray8Bit.class.getName();}
            @Override
            public TeraArray create(int sizeX, int sizeY, int sizeZ) {
                return new TeraDenseArray8Bit(sizeX, sizeY, sizeZ);
            }
        });
        registerTeraArrayFactory(new Factory() {
            @Override
            public String getName() {return TeraDenseArray16Bit.class.getName();}
            @Override
            public TeraArray create(int sizeX, int sizeY, int sizeZ) {
                return new TeraDenseArray16Bit(sizeX, sizeY, sizeZ);
            }
        });
        registerTeraArrayFactory(new Factory() {
            @Override
            public String getName() {return TeraSparseArray4Bit.class.getName();}
            @Override
            public TeraArray create(int sizeX, int sizeY, int sizeZ) {
                return new TeraSparseArray4Bit(sizeX, sizeY, sizeZ);
            }
        });
        registerTeraArrayFactory(new Factory() {
            @Override
            public String getName() {return TeraSparseArray8Bit.class.getName();}
            @Override
            public TeraArray create(int sizeX, int sizeY, int sizeZ) {
                return new TeraSparseArray8Bit(sizeX, sizeY, sizeZ);
            }
        });
        registerTeraArrayFactory(new Factory() {
            @Override
            public String getName() {return TeraSparseArray16Bit.class.getName();}
            @Override
            public TeraArray create(int sizeX, int sizeY, int sizeZ) {
                return new TeraSparseArray16Bit(sizeX, sizeY, sizeZ);
            }
        });
    }
    
    public static Factory getTeraArrayFactory(String factory) {
        Preconditions.checkNotNull(factory, "The parameter 'factory' must not be null");
        return teraArrayRegistry.get(factory);
    }
    
    public static Factory requireTeraArrayFactory(String factory) {
        Preconditions.checkNotNull(factory, "Parameter 'factory' must no be null");
        return Preconditions.checkNotNull(teraArrayRegistry.get(factory), "Factory does not exist: '" + factory + "'");
    }
    
    public static void checkContainsTeraArrayFactory(String factory) {
        Preconditions.checkNotNull(factory, "Parameter 'factory' must not be null");
        Preconditions.checkState(teraArrayRegistry.containsKey(factory), "Factory does not exist: '" + factory + "'");
    }
    
    public static boolean containsTeraArrayFactory(String factory) {
        if (factory == null) return false;
        return teraArrayRegistry.containsKey(factory);
    }
    
    public static String[] getTeraArrayFactories() {
        String[] factories = new String[teraArrayRegistry.size()];
        int i = 0;
        for (String factory : teraArrayRegistry.keySet()) {
            factories[i++] = factory;
        }
        return factories;
    }
    
    public static void registerTeraArrayFactory(TeraArray.Factory factory) {
        Preconditions.checkNotNull(factory, "Parameter 'factory' must not be null");
        final String name = factory.getName();
        Preconditions.checkNotNull(name, "Factory:getName() must not return null");
        Preconditions.checkArgument(!name.isEmpty(), "Factory:getName() must not return an empty string");
        Preconditions.checkArgument(name.equals(name.trim()), "Factory:getName() contains illegal whitespaces ('" + name + "')");
        Preconditions.checkState(!teraArrayRegistry.containsKey(name), "A factory named '" + name + "' already exists");
        teraArrayRegistry.put(name, factory);
        logger.debug("Registered tera array factory: {}", name);
    }
    
}
