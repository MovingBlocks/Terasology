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
import org.terasology.input.InputType;
import org.terasology.input.Keyboard.KeyId;
import org.terasology.input.RegisterBindButton;
import org.terasology.input.binds.movement.ForwardsButton;
import org.terasology.naming.Name;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;

/**
 * The binds configuration holds the mapping from binding uris to Inputs.
 * The {@link SimpleUri} for a binding contains the module id where the binding is defined and the id from the binding annotation, 
 * e.g. from {@link RegisterBindButton} as object name.
 * <p>
 * One example for a binding-input combination is the {@link ForwardsButton} 
 * which is defined in the engine with the id <code>forwards</code>.
 * The default input binding for the forward movement is the W-Key.
 * Therefore the binds for <code>engine:forwards</code> would contain an {@link Input} object with type {@link InputType#KEY} and id {@link KeyId#W}.
 */
public final class BindsConfig {

    private ListMultimap<SimpleUri, Input> uriBoundInputs = ArrayListMultimap.create();

    /**
     * Returns true if an input has already been bound to another key
     *
     * @param newInput The input to check if it has been bound already
     * @return True if newInput has been bound. False otherwise.
     */
    public boolean isBound(Input newInput) {
        return newInput != null && uriBoundInputs.containsValue(newInput);
    }

    /**
     * Sets this BindsConfig to be identical to other
     *
     * @param other The BindsConfig to copy
     */
    public void setBinds(BindsConfig other) {
        uriBoundInputs.clear();
        uriBoundInputs.putAll(other.uriBoundInputs);
    }

    public List<Input> getBinds(SimpleUri uri) {
        return uriBoundInputs.get(uri);
    }

    /**
     * Returns whether an input bind has been registered with the BindsConfig.
     * It may just have trivial None input.
     *
     * @param uri The bind's uri
     * @return Whether the given bind has been registered with the BindsConfig
     */
    public boolean hasBinds(SimpleUri uri) {
        return !uriBoundInputs.get(uri).isEmpty();
    }

    /**
     * Sets the inputs for a given bind, replacing any previous inputs
     *
     */
    public void setBinds(SimpleUri bindUri, Input ... inputs) {
        setBinds(bindUri, Arrays.asList(inputs));
    }

    /**
     * Sets the inputs for a given bind, replacing any previous inputs
     *
     */
    public void setBinds(SimpleUri bindUri, Iterable<Input> inputs) {
        Set<Input> uniqueInputs = Sets.newLinkedHashSet(inputs);

        // Clear existing usages of the given inputs
        Iterator<Input> iterator = uriBoundInputs.values().iterator();
        while (iterator.hasNext()) {
            Input i = iterator.next();
            if (uniqueInputs.contains(i)) {
                iterator.remove();
            }
        }
        uriBoundInputs.replaceValues(bindUri, uniqueInputs);
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
                    result.uriBoundInputs.putAll(uri, map.get(id));
                }
            }
            return result;
        }

        @Override
        public JsonElement serialize(BindsConfig bindsConfig, Type typeOfBindsConfig, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            SetMultimap<Name, SimpleUri> bindingByModuleName = listBindingsByModuleName(bindsConfig);
            List<Name> sortedModuleNames = bindingByModuleName.keySet().stream().sorted().collect(toList());
            for (Name moduleName : sortedModuleNames) {
                SetMultimap<String, Input> moduleBinds = HashMultimap.create();
                for (SimpleUri bindingUri : bindingByModuleName.get(moduleName)) {
                    moduleBinds.putAll(bindingUri.getObjectName().toString(), bindsConfig.uriBoundInputs.get(bindingUri));
                }
                JsonElement map = context.serialize(moduleBinds, SetMultimap.class);
                result.add(moduleName.toString(), map);
            }
            return result;
        }

        public SetMultimap<Name, SimpleUri> listBindingsByModuleName(BindsConfig src) {
            SetMultimap<Name, SimpleUri> bindingByModuleName = HashMultimap.create();
            for (SimpleUri bindingUri : src.uriBoundInputs.keySet()) {
                bindingByModuleName.put(bindingUri.getModuleName(), bindingUri);
            }
            return bindingByModuleName;
        }
    }

    public Collection<Input> getBoundInputs() {
        return uriBoundInputs.values();
    }

}
