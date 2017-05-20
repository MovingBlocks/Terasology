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

package org.terasology.config;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.terasology.engine.SimpleUri;
import org.terasology.input.Input;
import org.terasology.naming.Name;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User binds configuration. This holds the key/mouse binding for Button Binds. They are sorted by package.
 *
 */
public final class BindsConfig {

    private ListMultimap<SimpleUri, Input> data = ArrayListMultimap.create();

    public BindsConfig() {
    }

    /**
     * Returns true if an input has already been bound to another key
     *
     * @param newInput The input to check if it has been bound already
     * @return True if newInput has been bound. False otherwise.
     */
    public boolean isBound(Input newInput) {
        return newInput != null && data.containsValue(newInput);
    }

    /**
     * Sets this BindsConfig to be identical to other
     *
     * @param other The BindsConfig to copy
     */
    public void setBinds(BindsConfig other) {
        data.clear();
        data.putAll(other.data);
    }

    public List<Input> getBinds(SimpleUri uri) {
        return data.get(uri);
    }

    /**
     * Returns whether an input bind has been registered with the BindsConfig.
     * It may just have trivial None input.
     *
     * @param uri The bind's uri
     * @return Whether the given bind has been registered with the BindsConfig
     */
    public boolean hasBinds(SimpleUri uri) {
        return !data.get(uri).isEmpty();
    }

    /**
     * Sets the inputs for a given bind, replacing any previous inputs
     *
     * @param bindUri
     * @param inputs
     */

    public void setBinds(SimpleUri bindUri, Input ... inputs) {
        setBinds(bindUri, Arrays.asList(inputs));
    }

    public void setBinds(SimpleUri bindUri, Iterable<Input> inputs) {
        Set<Input> uniqueInputs = Sets.newLinkedHashSet(inputs);

        // Clear existing usages of the given inputs
        Iterator<Input> iterator = data.values().iterator();
        while (iterator.hasNext()) {
            Input i = iterator.next();
            if (uniqueInputs.contains(i)) {
                iterator.remove();
            }
        }
        data.replaceValues(bindUri, uniqueInputs);
    }

    static class Handler implements JsonSerializer<BindsConfig>, JsonDeserializer<BindsConfig> {

        @Override
        public BindsConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            BindsConfig result = new BindsConfig();
            JsonObject inputObj = json.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : inputObj.entrySet()) {
                SetMultimap<String, Input> map = context.deserialize(entry.getValue(), SetMultimap.class);
                for (String id : map.keySet()) {
                    SimpleUri uri = new SimpleUri(new Name(entry.getKey()), id);
                    result.data.putAll(uri, map.get(id));
                }
            }
            return result;
        }

        @Override
        public JsonElement serialize(BindsConfig src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            SetMultimap<Name, SimpleUri> bindByModule = HashMultimap.create();
            for (SimpleUri key : src.data.keySet()) {
                bindByModule.put(key.getModuleName(), key);
            }
            List<Name> sortedModules = Lists.newArrayList(bindByModule.keySet());
            Collections.sort(sortedModules);
            for (Name moduleId : sortedModules) {
                SetMultimap<String, Input> moduleBinds = HashMultimap.create();
                for (SimpleUri bindUri : bindByModule.get(moduleId)) {
                    moduleBinds.putAll(bindUri.getObjectName().toString(), src.data.get(bindUri));
                }
                JsonElement map = context.serialize(moduleBinds, SetMultimap.class);
                result.add(moduleId.toString(), map);
            }
            return result;
        }
    }

    public Collection<Input> values() {
        return data.values();
    }

}
