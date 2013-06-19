/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.logic.mod;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Information on a mod
 *
 * @author Immortius
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 */
public class ModInfo {
    
    private static final Logger logger = LoggerFactory.getLogger(ModInfo.class);
            
    private final String id;
    private final String displayName;
    private final String description;
    private final String author;
    private final Set<String> dependencies;
    private final Map<String, PerBlockStorageExtensionInfo> perBlockStorageExtensions;

    private ModInfo(JsonObject input) {
        if (input.has("id"))
            this.id = input.get("id").getAsString().trim();
        else
            this.id = "";
        if (input.has("displayName"))
            this.displayName = input.get("displayName").getAsString();
        else
            this.displayName = "";
        if (input.has("description"))
            this.description = input.get("description").getAsString();
        else
            this.description = "";
        if (input.has("author"))
            this.author = input.get("author").getAsString();
        else
            this.author = "";
        if (input.has("dependencies") && input.get("dependencies").isJsonArray()) {
            final Set<String> set = Sets.newLinkedHashSet();
            final JsonArray arr = input.get("dependencies").getAsJsonArray();
            for (final JsonElement elem : arr) 
                if (elem.isJsonPrimitive()) {
                    final JsonPrimitive p = (JsonPrimitive) elem;
                    if (p.isString())
                        set.add(p.getAsString());
                }
            this.dependencies = set;
        } else
            this.dependencies = null;
        if (input.has("perBlockStorageExtensions") && input.get("perBlockStorageExtensions").isJsonArray())
            this.perBlockStorageExtensions = PerBlockStorageExtensionInfo.load(displayName.isEmpty() ? id : displayName, input.get("perBlockStorageExtensions").getAsJsonArray());
        else
            this.perBlockStorageExtensions = null;
    }
    
    public ModInfo(String id, String displayName, String description, String author, Iterable<String> dependencies, Map<String, PerBlockStorageExtensionInfo> perBlockStorageExtensions) {
        this.id = Preconditions.checkNotNull(id, "The parameter 'id' must not be null");
        this.displayName = Preconditions.checkNotNull(displayName, "The parameter 'displayName' must not be null");
        this.description = Preconditions.checkNotNull(description, "The parameter 'description' must not be null");
        this.author = Preconditions.checkNotNull(author, "The parameter 'author' must not be null");
        if (dependencies != null)
            this.dependencies = Sets.newLinkedHashSet(dependencies);
        else
            this.dependencies = null;
        if (perBlockStorageExtensions != null)
            this.perBlockStorageExtensions = Maps.newHashMap(perBlockStorageExtensions);
        else
            this.perBlockStorageExtensions = null;
    }
    
    public static class PerBlockStorageExtensionInfo {
        
        private final String extensionId;
        private final String factoryId;
        
        private PerBlockStorageExtensionInfo(JsonObject info) {
            if (info.has("extensionId"))
                extensionId = info.get("extensionId").getAsString().trim();
            else
                extensionId = "";
            if (info.has("factoryId"))
                factoryId = info.get("factoryId").getAsString().trim();
            else
                factoryId = "";
        }
        
        public PerBlockStorageExtensionInfo(String extensionId, String factoryId) {
            this.extensionId = Preconditions.checkNotNull(extensionId, "The parameter 'extensionId' must not be null");
            this.factoryId = Preconditions.checkNotNull(factoryId, "The parameter 'factoryId' must not be null");
        }
        
        public String getExtensionId() {
            return extensionId;
        }
        
        public String getFactoryId() {
            return factoryId;
        }
        
        public static Map<String, PerBlockStorageExtensionInfo> load(String modDisplayName, JsonArray array) {
            Preconditions.checkNotNull(array, "The parameter 'array' must not be null");
            final Map<String, PerBlockStorageExtensionInfo> map = Maps.newHashMap();
            for (final JsonElement elem : array) {
                if (elem.isJsonObject()) {
                    final PerBlockStorageExtensionInfo info = new PerBlockStorageExtensionInfo((JsonObject) elem);
                    if (info.getExtensionId().isEmpty()) {
                        logger.warn("Discovered invalid per-block-storage extension for mod {}, skipping", modDisplayName);
                        continue;
                    }
                    if (info.getFactoryId().isEmpty()) {
                        logger.warn("Discovered invalid per-block-storage extension '{}' for mod {}, skipping", info.getExtensionId(), modDisplayName);
                        continue;
                    }
                    if (map.containsKey(info.getExtensionId())) {
                        logger.warn("Discovered duplicate per-block-storage extension '{}' for mod {}, skipping", info.getExtensionId(), modDisplayName);
                        continue;
                    }
                    map.put(info.getExtensionId(), info);
                }
            }
            if (map.size() == 0)
                return null;
            return map;
        }
    }
    
    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
    
    public String getAuthor() {
        return author;
    }

    public Set<String> getDependencies() {
        if (dependencies == null)
            return Sets.newLinkedHashSet();
        return Sets.newLinkedHashSet(dependencies);
    }
    
    public Map<String, PerBlockStorageExtensionInfo> getPerBlockStorageExtensions() {
        if (perBlockStorageExtensions == null)
            return Maps.newHashMap();
        return Maps.newHashMap(perBlockStorageExtensions);
    }
    
    public static ModInfo load(JsonObject modInfo) {
        Preconditions.checkNotNull(modInfo, "The parameter 'modInfo' must not be null");
        return new ModInfo(modInfo);
    }
    
    public static ModInfo load(Reader reader) throws IOException {
        Preconditions.checkNotNull(reader, "The parameter 'reader' must not be null");
        final JsonParser parser = new JsonParser();
        final JsonElement element = parser.parse(reader);
        if (element == null || !element.isJsonObject())
            throw new IOException("Invalid mod manifest");
        return load((JsonObject) element);
    }
    
    public static ModInfo load(File file) throws IOException {
        Preconditions.checkNotNull(file, "The parameter 'file' must not be null");
        final FileReader reader = new FileReader(file);
        try {
            return load(reader);
        } finally {
            reader.close();
        }
    }
}