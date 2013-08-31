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
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.module.Module;
import org.terasology.engine.module.ModuleManager;
import org.terasology.input.DefaultBinding;
import org.terasology.input.Input;
import org.terasology.input.InputType;
import org.terasology.input.RegisterBindButton;
import org.terasology.input.events.ButtonEvent;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * User binds configuration. This holds the key/mouse binding for Button Binds. They are sorted by package.
 *
 * @author Immortius
 */
public final class BindsConfig {
    private Map<String, Multimap<String, Input>> data = Maps.newHashMap();

    public BindsConfig() {
    }

    /**
     * Sets this BindsConfig to be identical to other
     *
     * @param other The BindsConfig to copy
     */
    public void setBinds(BindsConfig other) {
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
    public Collection<Input> getBinds(String uri) {
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
     * Returns whether an input bind has been registered with the BindsConfig.
     * It may just have trivial None input.
     *
     * @param packageName The name of the bind's package
     * @param id          The id of the bind
     * @return Whether the given bind has been registered with the BindsConfig
     */
    public boolean hasBinds(String packageName, String id) {
        Multimap<String, Input> packageMap = getPackageMap(packageName);
        if (packageMap != null) {
            return packageMap.containsKey(id);
        }
        return false;
    }

    /**
     * Returns whether an input bind has been registered with the BindsConfig.
     * It may just have the trivial None input.
     *
     * @param id
     * @return Whether the given bind has been registered with the BindsConfig
     */
    public boolean hasBinds(String id) {
        String[] parts = id.split(":", 2);
        if (parts.length == 2) {
            return hasBinds(parts[0], parts[1]);
        }
        return false;
    }

    /**
     * Sets the inputs for a given bind, replacing any previous inputs
     *
     * @param packageName
     * @param bindName
     * @param inputs
     */
    public void setBinds(String packageName, String bindName, Input... inputs) {
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
     *
     * @param id
     * @param inputs
     */
    public void setBinds(String id, Input... inputs) {
        String[] parts = id.split(":", 2);
        if (parts.length == 2) {
            setBinds(parts[0], parts[1], inputs);
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
     * @return A new BindsConfig, with inputs set from the DefaultBinding annotations on bind classes
     */
    public static BindsConfig createDefault() {
        ModuleManager moduleManager = CoreRegistry.get(ModuleManager.class);
        BindsConfig config = new BindsConfig();
        for (Module module : moduleManager.getModules()) {
            if (module.isCodeModule()) {
                config.addDefaultsFor(module.getModuleInfo().getId(), module.getReflections().getTypesAnnotatedWith(RegisterBindButton.class));
            }
        }
        return config;
    }

    /**
     * Updates a config with any binds that it may be missing, through reflection over RegisterBindButton annotations
     */
    public void updateForChangedMods() {
        ModuleManager moduleManager = CoreRegistry.get(ModuleManager.class);
        for (Module module : moduleManager.getModules()) {
            if (module.isCodeModule()) {
                updateInputsFor(module.getModuleInfo().getId(), module.getReflections().getTypesAnnotatedWith(RegisterBindButton.class));
            }
        }
        // TODO: Better way to handle toolbar slots? Might be easiest just to make them separate classes.
        for (int i = 0; i < 10; ++i) {
            if (!hasBinds(TerasologyConstants.ENGINE_MODULE, "toolbarSlot" + i)) {
                setBinds(TerasologyConstants.ENGINE_MODULE, "toolbarSlot" + i, new Input(InputType.KEY, Keyboard.KEY_1 + i));
            }
        }
    }

    private void updateInputsFor(String packageName, Iterable<Class<?>> classes) {
        for (Class<?> buttonEvent : classes) {
            if (ButtonEvent.class.isAssignableFrom(buttonEvent)) {
                RegisterBindButton info = (RegisterBindButton) buttonEvent.getAnnotation(RegisterBindButton.class);
                if (!hasBinds(packageName, info.id())) {
                    addBind(packageName, buttonEvent, info);
                }
            }
        }
    }

    private void addDefaultsFor(String packageName, Iterable<Class<?>> classes) {
        for (Class<?> buttonEvent : classes) {
            if (ButtonEvent.class.isAssignableFrom(buttonEvent)) {
                RegisterBindButton info = (RegisterBindButton) buttonEvent.getAnnotation(RegisterBindButton.class);
                addBind(packageName, buttonEvent, info);
            }
        }
        // TODO: Better way to handle toolbar slots? Might be easiest just to make them separate classes.
        for (int i = 0; i < 10; ++i) {
            setBinds(TerasologyConstants.ENGINE_MODULE, "toolbarSlot" + i, new Input(InputType.KEY, Keyboard.KEY_1 + i));
        }
    }

    private void addBind(String packageName, Class<?> buttonEvent, RegisterBindButton info) {
        List<Input> defaultInputs = Lists.newArrayList();
        for (Annotation annotation : buttonEvent.getAnnotations()) {
            if (annotation instanceof DefaultBinding) {
                DefaultBinding defaultBinding = (DefaultBinding) annotation;
                defaultInputs.add(new Input(defaultBinding.type(), defaultBinding.id()));
            }
        }
        setBinds(packageName, info.id(), defaultInputs.toArray(new Input[defaultInputs.size()]));
    }

    static class Handler implements JsonSerializer<BindsConfig>, JsonDeserializer<BindsConfig> {

        @Override
        public BindsConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            BindsConfig result = new BindsConfig();
            JsonObject inputObj = json.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : inputObj.entrySet()) {
                Multimap<String, Input> map = context.deserialize(entry.getValue(), Multimap.class);
                result.data.put(entry.getKey(), map);
            }
            return result;
        }

        @Override
        public JsonElement serialize(BindsConfig src, Type typeOfSrc, JsonSerializationContext context) {
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
