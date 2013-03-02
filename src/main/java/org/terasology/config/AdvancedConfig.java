/*
 * Copyright 2013 Moving Blocks
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

package org.terasology.config;

import com.google.common.base.Preconditions;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.world.chunks.blockdata.TeraArray;
import org.terasology.world.chunks.blockdata.TeraArrays;
import org.terasology.world.chunks.blockdata.TeraDenseArray8Bit;

import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * Allows to configure internal details of the Terasology engine.
 *
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 */
@SuppressWarnings("rawtypes")
public final class AdvancedConfig {

    private static final Logger logger = LoggerFactory.getLogger(AdvancedConfig.class);

    private String blocksFactory;
    private String sunlightFactory;
    private String lightFactory;
    private String extraFactory;
    private boolean chunkDeflationEnabled, chunkDeflationLoggingEnabled;

    private AdvancedConfig() {
    }

    public String getBlocksFactoryName() {
        return blocksFactory;
    }

    public TeraArray.Factory getBlocksFactory() {
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

    public TeraArray.Factory getSunlightFactory() {
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

    public TeraArray.Factory getLightFactory() {
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

    public String getExtraFactoryName() {
        return extraFactory;
    }

    public TeraArray.Factory getExtraFactory() {
        return requireTeraArrayFactory(extraFactory);
    }

    public AdvancedConfig setExtraFactory(String factory) {
        checkContainsTeraArrayFactory(factory);
        extraFactory = factory;
        return this;
    }

    public AdvancedConfig setExtraFactoryDontThrow(String factory) {
        if (containsTeraArrayFactory(factory)) {
            extraFactory = factory;
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
                .setExtraFactory(TeraDenseArray8Bit.class.getName())
                .setChunkDeflationEnabled(true)
                .setChunkDeflationLoggingEnabled(false);
    }

    public static class Handler implements JsonSerializer<AdvancedConfig>, JsonDeserializer<AdvancedConfig> {

        @Override
        public AdvancedConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            AdvancedConfig config = AdvancedConfig.createDefault();
            JsonObject input = json.getAsJsonObject();
            if (input.has("blocksFactory")) {
                config.setBlocksFactoryDontThrow(input.get("blocksFactory").getAsString());
            }
            if (input.has("sunlightFactory")) {
                config.setSunlightFactoryDontThrow(input.get("sunlightFactory").getAsString());
            }
            if (input.has("lightFactory")) {
                config.setLightFactoryDontThrow(input.get("lightFactory").getAsString());
            }
            if (input.has("extraFactory")) {
                config.setExtraFactoryDontThrow(input.get("extraFactory").getAsString());
            }
            if (input.has("chunkDeflationEnabled")) {
                config.setChunkDeflationEnabled(input.get("chunkDeflationEnabled").getAsBoolean());
            }
            if (input.has("chunkDeflationLoggingEnabled")) {
                config.setChunkDeflationLoggingEnabled(input.get("chunkDeflationLoggingEnabled").getAsBoolean());
            }
            return config;
        }

        @Override
        public JsonElement serialize(AdvancedConfig src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            result.addProperty("blocksFactory", src.blocksFactory);
            result.addProperty("sunlightFactory", src.sunlightFactory);
            result.addProperty("lightFactory", src.lightFactory);
            result.addProperty("extraFactory", src.extraFactory);
            result.addProperty("chunkDeflationEnabled", src.chunkDeflationEnabled);
            result.addProperty("chunkDeflationLoggingEnabled", src.chunkDeflationLoggingEnabled);
            return result;
        }

    }

    public static TeraArray.Factory getTeraArrayFactory(String factory) {
        Preconditions.checkNotNull(factory, "The parameter 'factory' must not be null");
        final TeraArrays.Entry entry = TeraArrays.getInstance().getEntry(factory);
        if (entry != null) {
            return entry.factory;
        }
        return null;
    }

    public static TeraArray.Factory requireTeraArrayFactory(String factory) {
        Preconditions.checkNotNull(factory, "Parameter 'factory' must no be null");
        return Preconditions.checkNotNull(getTeraArrayFactory(factory), "Factory does not exist: '" + factory + "'");
    }

    public static void checkContainsTeraArrayFactory(String factory) {
        Preconditions.checkNotNull(factory, "Parameter 'factory' must not be null");
        Preconditions.checkState(containsTeraArrayFactory(factory), "Factory does not exist: '" + factory + "'");
    }

    public static boolean containsTeraArrayFactory(String factory) {
        return factory != null && getTeraArrayFactory(factory) != null;
    }

    public static String[] getTeraArrayFactories() {
        final TeraArrays.Entry[] entries = TeraArrays.getInstance().getCoreArrayEntries();
        final String[] factories = new String[entries.length];
        for (int i = 0; i < entries.length; i++) {
            factories[i] = entries[i].arrayClassName;
        }
        Arrays.sort(factories);
        return factories;
    }
}
