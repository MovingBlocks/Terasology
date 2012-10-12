/*
 * Copyright 2012  Benjamin Glatzel <benjamin.glatzel@me.com>
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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.reflections.Reflections;
import org.terasology.game.CoreRegistry;
import org.terasology.input.DefaultBinding;
import org.terasology.input.Input;
import org.terasology.input.InputType;
import org.terasology.input.RegisterBindButton;
import org.terasology.input.events.ButtonEvent;
import org.terasology.logic.mod.Mod;
import org.terasology.logic.mod.ModManager;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Immortius
 */
public class InputConfig {
    private Map<String, Multimap<String, Input>> data = Maps.newHashMap();

    public InputConfig() {
    }

    public void setInputs(InputConfig copy) {
        data.clear();
        for (String key : copy.data.keySet()) {
            Multimap<String, Input> map = getPackageMap(key);
            map.putAll(copy.data.get(key));
        }
    }

    public Collection<Input> getInputs(String id) {
        String[] parts = id.toLowerCase(Locale.ENGLISH).split(":", 2);
        if (parts.length == 2) {
            Multimap<String, Input> packageMap = getPackageMap(parts[0]);
            if (packageMap != null) {
                return packageMap.get(parts[1]);
            }
        }
        return Lists.newArrayList();
    }

    public boolean hasInputs(String packageName, String id) {
        Multimap<String, Input> packageMap = getPackageMap(packageName);
        if (packageMap != null) {
            return packageMap.containsKey(id);
        }
        return false;
    }

    public boolean hasInputs(String id) {
        String[] parts = id.toLowerCase(Locale.ENGLISH).split(":", 2);
        if (parts.length == 2) {
            return hasInputs(parts[0], parts[1]);
        }
        return false;
    }

    public boolean hasPackage(String packageName) {
        return data.containsKey(packageName);
    }

    public void setInputs(String packageName, String bindName, Input... inputs) {
        Multimap<String, Input> packageMap = getPackageMap(packageName);
        if (inputs.length == 0) {
            packageMap.removeAll(bindName);
            packageMap.put(bindName, new Input());
        } else {
            packageMap.replaceValues(bindName, Arrays.asList(inputs));
        }
    }

    public void setInputs(String id, Input... inputs) {
        String[] parts = id.toLowerCase(Locale.ENGLISH).split(":", 2);
        if (parts.length == 2) {
            setInputs(parts[0], parts[1], inputs);
        }
    }

    private Multimap<String, Input> getPackageMap(String part) {
        Multimap<String, Input> packageMap = data.get(part);
        if (packageMap == null) {
            packageMap = HashMultimap.create();
            data.put(part, packageMap);
        }
        return packageMap;
    }

    public static InputConfig createDefault() {
        ModManager modManager = CoreRegistry.get(ModManager.class);
        InputConfig config = new InputConfig();
        addDefaultsFor(ModManager.ENGINE_PACKAGE, modManager.getEngineReflections().getTypesAnnotatedWith(RegisterBindButton.class), config);
        for (Mod mod : modManager.getMods()) {
            addDefaultsFor(mod.getModInfo().getId(), mod.getReflections().getTypesAnnotatedWith(RegisterBindButton.class), config);
        }
        return config;
    }

    public static void updateForChangedMods(InputConfig config) {
        ModManager modManager = CoreRegistry.get(ModManager.class);
        updateInputsFor(ModManager.ENGINE_PACKAGE, modManager.getEngineReflections().getTypesAnnotatedWith(RegisterBindButton.class), config );
        for (Mod mod : modManager.getMods()) {
            updateInputsFor(mod.getModInfo().getId(), mod.getReflections().getTypesAnnotatedWith(RegisterBindButton.class), config);
        }
    }

    public static void updateInputsFor(String packageName, Iterable<Class<?>> classes, InputConfig config) {
        for (Class<?> buttonEvent : classes) {
            if (ButtonEvent.class.isAssignableFrom(buttonEvent)) {
                RegisterBindButton info = (RegisterBindButton) buttonEvent.getAnnotation(RegisterBindButton.class);
                if (!config.hasInputs(packageName, info.id())) {
                    addBind(packageName, config, buttonEvent, info);
                }
            }
        }
    }

    public static void addDefaultsFor(String packageName, Iterable<Class<?>> classes, InputConfig config) {
        for (Class<?> buttonEvent : classes) {
            if (ButtonEvent.class.isAssignableFrom(buttonEvent)) {
                RegisterBindButton info = (RegisterBindButton) buttonEvent.getAnnotation(RegisterBindButton.class);
                addBind(packageName, config, buttonEvent, info);
            }
        }
    }

    private static void addBind(String packageName, InputConfig config, Class<?> buttonEvent, RegisterBindButton info) {
        List<Input> defaultInputs = Lists.newArrayList();
        for (Annotation annotation : buttonEvent.getAnnotations()) {
            if (annotation instanceof DefaultBinding) {
                DefaultBinding defaultBinding = (DefaultBinding) annotation;
                defaultInputs.add(new Input(defaultBinding.type(), defaultBinding.id()));
            }
        }
        config.setInputs(packageName, info.id(), defaultInputs.toArray(new Input[defaultInputs.size()]));
    }

    static class Handler implements JsonSerializer<InputConfig>, JsonDeserializer<InputConfig> {

        @Override
        public InputConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            InputConfig result = new InputConfig();
            JsonObject inputObj = json.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : inputObj.entrySet()) {
                Multimap<String, Input> map = context.deserialize(entry.getValue(), Multimap.class);
                result.data.put(entry.getKey(), map);
            }
            return result;
        }

        @Override
        public JsonElement serialize(InputConfig src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            List<String> keys = Lists.newArrayList(src.data.keySet());
            Collections.sort(keys);
            for (String packageName : keys) {
                JsonElement map = context.serialize(src.data.get(packageName), Multimap.class);
                result.add(packageName, map);
            }
            return result;
        }
    }

}
