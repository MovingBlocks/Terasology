/*
 * Copyright (c) 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.config;

import java.lang.reflect.Type;

import org.terasology.world.chunks.perBlockStorage.PerBlockStorageManager;

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
    
    private String blocksFactory, sunlightFactory, lightFactory, extraFactory;
    private boolean chunkDeflationEnabled, chunkDeflationLoggingEnabled;
    private boolean advancedMonitoringEnabled, advancedMonitorVisibleAtStartup;
    
    private AdvancedConfig() {}
    
    public String getBlocksFactoryName() {
        return blocksFactory;
    }
    
    public AdvancedConfig setBlocksFactory(String factory) {
        blocksFactory = factory;
        return this;
    }
    
    public String getSunlightFactoryName() {
        return sunlightFactory;
    }
    
    public AdvancedConfig setSunlightFactory(String factory) {
        sunlightFactory = factory;
        return this;
    }
    
    public String getLightFactoryName() {
        return lightFactory;
    }
    
    public AdvancedConfig setLightFactory(String factory) {
        lightFactory = factory;
        return this;
    }
    
    public String getExtraFactoryName() {
        return extraFactory;
    }
    
    public AdvancedConfig setExtraFactory(String factory) {
        extraFactory = factory;
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
    

    public boolean isAdvancedMonitoringEnabled() {
        return advancedMonitoringEnabled;
    }

    public AdvancedConfig setAdvancedMonitoringEnabled(boolean enabled) {
        advancedMonitoringEnabled = enabled;
        return this;
    }

    public boolean isAdvancedMonitorVisibleAtStartup() {
        return advancedMonitorVisibleAtStartup;
    }

    public AdvancedConfig setAdvancedMonitorVisibleAtStartup(boolean visible) {
        advancedMonitorVisibleAtStartup = visible;
        return this;
    }

    public static AdvancedConfig createDefault() {
        return new AdvancedConfig()
        .setBlocksFactory(PerBlockStorageManager.DefaultBlockStorageFactory)
        .setSunlightFactory(PerBlockStorageManager.DefaultSunlightStorageFactory)
        .setLightFactory(PerBlockStorageManager.DefaultLightStorageFactory)
        .setExtraFactory(PerBlockStorageManager.DefaultExtraStorageFactory)
        .setChunkDeflationEnabled(true)
        .setChunkDeflationLoggingEnabled(false)
        .setAdvancedMonitoringEnabled(false)
        .setAdvancedMonitorVisibleAtStartup(false);
    }

    public static class Handler implements JsonSerializer<AdvancedConfig>, JsonDeserializer<AdvancedConfig> {

        @Override
        public AdvancedConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

            AdvancedConfig config = AdvancedConfig.createDefault();
            JsonObject input = json.getAsJsonObject();
            if (input.has("blocksFactory")) 
                config.setBlocksFactory(input.get("blocksFactory").getAsString());
            if (input.has("sunlightFactory")) 
                config.setSunlightFactory(input.get("sunlightFactory").getAsString());
            if (input.has("lightFactory")) 
                config.setLightFactory(input.get("lightFactory").getAsString());
            if (input.has("extraFactory")) 
                config.setExtraFactory(input.get("extraFactory").getAsString());
            if (input.has("chunkDeflationEnabled"))
                config.setChunkDeflationEnabled(input.get("chunkDeflationEnabled").getAsBoolean());
            if (input.has("chunkDeflationLoggingEnabled"))
                config.setChunkDeflationLoggingEnabled(input.get("chunkDeflationLoggingEnabled").getAsBoolean());
            if (input.has("advancedMonitoringEnabled"))
                config.setAdvancedMonitoringEnabled(input.get("advancedMonitoringEnabled").getAsBoolean());
            if (input.has("advancedMonitorVisibleAtStartup"))
                config.setAdvancedMonitorVisibleAtStartup(input.get("advancedMonitorVisibleAtStartup").getAsBoolean());
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
            result.addProperty("advancedMonitoringEnabled", src.advancedMonitoringEnabled);
            result.addProperty("advancedMonitorVisibleAtStartup", src.advancedMonitorVisibleAtStartup);
            return result;
        }
    }
}
