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
import org.lwjgl.input.Keyboard;
import org.reflections.Reflections;
import org.terasology.game.CoreRegistry;
import org.terasology.input.DefaultBinding;
import org.terasology.input.Input;
import org.terasology.input.InputType;
import org.terasology.input.RegisterBindButton;
import org.terasology.input.binds.ToolbarSlotButton;
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
 * User input configuration. This holds the key/mouse binding for Button Binds. They are sorted by package.
 * @author Immortius
 */
public final class InputConfig {
    private Map<String, Multimap<String, Input>> data = Maps.newHashMap();

    public InputConfig() {
    }

    /**
     * Sets this InputConfig to be identical to other
     * @param other The InputConfig to copy
     */
    public void setInputs(InputConfig other) {
        data.clear();
        for (String key : other.data.keySet()) {
            Multimap<String, Input> map = getPackageMap(key);
            map.putAll(other.data.get(key));
        }
    }

    /**
     * @param uri A uri for the bind to get inputs for
     * @return All the inputs associated with the given bind
     */
    public Collection<Input> getInputs(String uri) {
        String[] parts = uri.split(":", 2);
        if (parts.length == 2) {
            Multimap<String, Input> packageMap = getPackageMap(parts[0]);
            if (packageMap != null) {
                return packageMap.get(parts[1]);
            }
        }
        return Lists.newArrayList();
    }

    /**
     * Returns whether an input bind has been registered with the InputConfig.
     * It may just have trivial None input.
     *
     * @param packageName The name of the bind's package
     * @param id The id of the bind
     * @return Whether the given bind has been registered with the InputConfig
     */
    public boolean hasInputs(String packageName, String id) {
        Multimap<String, Input> packageMap = getPackageMap(packageName);
        if (packageMap != null) {
            return packageMap.containsKey(id);
        }
        return false;
    }

    /**
     * Returns whether an input bind has been registered with the InputConfig.
     * It may just have the trivial None input.
     *
     * @param id
     * @return Whether the given bind has been registered with the InputConfig
     */
    public boolean hasInputs(String id) {
        String[] parts = id.split(":", 2);
        if (parts.length == 2) {
            return hasInputs(parts[0], parts[1]);
        }
        return false;
    }

    /**
     * Sets the inputs for a given bind, replacing any previous inputs
     * @param packageName
     * @param bindName
     * @param inputs
     */
    public void setInputs(String packageName, String bindName, Input... inputs) {
        Multimap<String, Input> packageMap = getPackageMap(packageName);
        if (inputs.length == 0) {
            packageMap.removeAll(bindName);
            packageMap.put(bindName, new Input());
        } else {
            packageMap.replaceValues(bindName, Arrays.asList(inputs));
        }
    }

    /**
     * Sets the inputs for a given bind, replacing any previous inputs
     * @param id
     * @param inputs
     */
    public void setInputs(String id, Input... inputs) {
        String[] parts = id.split(":", 2);
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

    /**
     * @return A new InputConfig, with inputs set from the DefaultBinding annotations on bind classes
     */
    public static InputConfig createDefault() {
        ModManager modManager = CoreRegistry.get(ModManager.class);
        InputConfig config = new InputConfig();
        addDefaultsFor(ModManager.ENGINE_PACKAGE, modManager.getEngineReflections().getTypesAnnotatedWith(RegisterBindButton.class), config);
        for (Mod mod : modManager.getMods()) {
            addDefaultsFor(mod.getModInfo().getId(), mod.getReflections().getTypesAnnotatedWith(RegisterBindButton.class), config);
        }
        return config;
    }

    /**
     * Updates a config with any binds that it may be missing, through reflection over RegisterBindButton annotations
     * @param config
     */
    public static void updateForChangedMods(InputConfig config) {
        ModManager modManager = CoreRegistry.get(ModManager.class);
        updateInputsFor(ModManager.ENGINE_PACKAGE, modManager.getEngineReflections().getTypesAnnotatedWith(RegisterBindButton.class), config );
        for (Mod mod : modManager.getMods()) {
            updateInputsFor(mod.getModInfo().getId(), mod.getReflections().getTypesAnnotatedWith(RegisterBindButton.class), config);
        }
        // TODO: Better way to handle toolbar slots? Might be easiest just to make them separate classes.
        for (int i = 0; i < 10; ++i) {
            if (!config.hasInputs(ModManager.ENGINE_PACKAGE, "toolbarSlot" + i)) {
                config.setInputs(ModManager.ENGINE_PACKAGE, "toolbarSlot" + i, new Input(InputType.KEY, Keyboard.KEY_1 + i));
            }
        }
    }

    private static void updateInputsFor(String packageName, Iterable<Class<?>> classes, InputConfig config) {
        for (Class<?> buttonEvent : classes) {
            if (ButtonEvent.class.isAssignableFrom(buttonEvent)) {
                RegisterBindButton info = (RegisterBindButton) buttonEvent.getAnnotation(RegisterBindButton.class);
                if (!config.hasInputs(packageName, info.id())) {
                    addBind(packageName, config, buttonEvent, info);
                }
            }
        }
    }

    private static void addDefaultsFor(String packageName, Iterable<Class<?>> classes, InputConfig config) {
        for (Class<?> buttonEvent : classes) {
            if (ButtonEvent.class.isAssignableFrom(buttonEvent)) {
                RegisterBindButton info = (RegisterBindButton) buttonEvent.getAnnotation(RegisterBindButton.class);
                addBind(packageName, config, buttonEvent, info);
            }
        }
        // TODO: Better way to handle toolbar slots? Might be easiest just to make them separate classes.
        for (int i = 0; i < 10; ++i) {
            config.setInputs(ModManager.ENGINE_PACKAGE, "toolbarSlot" + i, new Input(InputType.KEY, Keyboard.KEY_1 + i));
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
