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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.input.BindAxisEvent;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.BindableAxis;
import org.terasology.input.BindableButton;
import org.terasology.input.InputSystem;
import org.terasology.input.RegisterBindAxis;
import org.terasology.registry.CoreRegistry;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.Module;
import org.terasology.engine.module.ModuleManager;
import org.terasology.input.DefaultBinding;
import org.terasology.input.Input;
import org.terasology.input.RegisterBindButton;
import org.terasology.input.events.ButtonEvent;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * User binds configuration. This holds the key/mouse binding for Button Binds. They are sorted by package.
 *
 * @author Immortius
 */
public final class BindsConfig {
    private static final Logger logger = LoggerFactory.getLogger(BindsConfig.class);

    private ListMultimap<SimpleUri, Input> data = ArrayListMultimap.create();

    public BindsConfig() {
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

    public Collection<Input> getBinds(SimpleUri uri) {
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
    public void setBinds(SimpleUri bindUri, Input... inputs) {
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

    /**
     * @return A new BindsConfig, with inputs set from the DefaultBinding annotations on bind classes
     */
    public static BindsConfig createDefault() {
        ModuleManager moduleManager = CoreRegistry.get(ModuleManager.class);
        BindsConfig config = new BindsConfig();
        for (Module module : moduleManager.getModules()) {
            if (module.isCodeModule()) {
                config.addDefaultsFor(module.getId(), module.getReflections().getTypesAnnotatedWith(RegisterBindButton.class));
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
                updateInputsFor(module.getId(), module.getReflections().getTypesAnnotatedWith(RegisterBindButton.class));
            }
        }
    }

    private void updateInputsFor(String moduleId, Iterable<Class<?>> classes) {
        for (Class<?> buttonEvent : classes) {
            if (ButtonEvent.class.isAssignableFrom(buttonEvent)) {
                RegisterBindButton info = buttonEvent.getAnnotation(RegisterBindButton.class);
                SimpleUri bindUri = new SimpleUri(moduleId, info.id());
                if (!hasBinds(bindUri)) {
                    addBind(moduleId, buttonEvent, info);
                }
            }
        }
    }

    private void addDefaultsFor(String moduleId, Iterable<Class<?>> classes) {
        for (Class<?> buttonEvent : classes) {
            if (ButtonEvent.class.isAssignableFrom(buttonEvent)) {
                RegisterBindButton info = buttonEvent.getAnnotation(RegisterBindButton.class);
                addBind(moduleId, buttonEvent, info);
            }
        }
    }

    private void addBind(String moduleName, Class<?> buttonEvent, RegisterBindButton info) {
        List<Input> defaultInputs = Lists.newArrayList();
        for (Annotation annotation : buttonEvent.getAnnotations()) {
            if (annotation instanceof DefaultBinding) {
                DefaultBinding defaultBinding = (DefaultBinding) annotation;
                Input input = defaultBinding.type().getInput(defaultBinding.id());
                if (!data.values().contains(input)) {
                    defaultInputs.add(input);
                }
            }
        }
        SimpleUri bindUri = new SimpleUri(moduleName, info.id());
        setBinds(bindUri, defaultInputs.toArray(new Input[defaultInputs.size()]));
    }

    public void applyBinds(InputSystem inputSystem, ModuleManager moduleManager) {
        inputSystem.clearBinds();
        for (Module module : moduleManager.getActiveModules()) {
            if (module.isCodeModule()) {
                registerButtonBinds(inputSystem, module.getId(), module.getReflections().getTypesAnnotatedWith(RegisterBindButton.class));
                registerAxisBinds(inputSystem, module.getId(), module.getReflections().getTypesAnnotatedWith(RegisterBindAxis.class));
            }
        }
    }

    private void registerAxisBinds(InputSystem inputSystem, String packageName, Iterable<Class<?>> classes) {
        String prefix = packageName.toLowerCase(Locale.ENGLISH) + ":";
        for (Class registerBindClass : classes) {
            RegisterBindAxis info = (RegisterBindAxis) registerBindClass.getAnnotation(RegisterBindAxis.class);
            String id = prefix + info.id();
            if (BindAxisEvent.class.isAssignableFrom(registerBindClass)) {
                BindableButton positiveButton = inputSystem.getBindButton(new SimpleUri(info.positiveButton()));
                BindableButton negativeButton = inputSystem.getBindButton(new SimpleUri(info.negativeButton()));
                if (positiveButton == null) {
                    logger.warn("Failed to register axis \"{}\", missing positive button \"{}\"", id, info.positiveButton());
                    continue;
                }
                if (negativeButton == null) {
                    logger.warn("Failed to register axis \"{}\", missing negative button \"{}\"", id, info.negativeButton());
                    continue;
                }
                try {
                    BindableAxis bindAxis = inputSystem.registerBindAxis(id, (BindAxisEvent) registerBindClass.newInstance(), positiveButton, negativeButton);
                    bindAxis.setSendEventMode(info.eventMode());
                    logger.debug("Registered axis bind: {}", id);
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("Failed to register axis bind \"{}\"", id, e);
                }
            } else {
                logger.error("Failed to register axis bind \"{}\", does not extend BindAxisEvent", id);
            }
        }
    }

    private void registerButtonBinds(InputSystem inputSystem, String moduleId, Iterable<Class<?>> classes) {
        for (Class registerBindClass : classes) {
            RegisterBindButton info = (RegisterBindButton) registerBindClass.getAnnotation(RegisterBindButton.class);
            SimpleUri bindUri = new SimpleUri(moduleId, info.id());
            if (BindButtonEvent.class.isAssignableFrom(registerBindClass)) {
                try {
                    BindableButton bindButton = inputSystem.registerBindButton(bindUri, info.description(), (BindButtonEvent) registerBindClass.newInstance());
                    bindButton.setMode(info.mode());
                    bindButton.setRepeating(info.repeating());

                    for (Input input : getBinds(bindUri)) {
                        if (input != null) {
                            inputSystem.linkBindButtonToInput(input, bindUri);
                        }
                    }

                    logger.debug("Registered button bind: {}", bindUri);
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("Failed to register button bind \"{}\"", e);
                }
            } else {
                logger.error("Failed to register button bind \"{}\", does not extend BindButtonEvent", bindUri);
            }
        }
    }

    static class Handler implements JsonSerializer<BindsConfig>, JsonDeserializer<BindsConfig> {

        @Override
        public BindsConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            BindsConfig result = new BindsConfig();
            JsonObject inputObj = json.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : inputObj.entrySet()) {
                SetMultimap<String, Input> map = context.deserialize(entry.getValue(), SetMultimap.class);
                for (String id : map.keySet()) {
                    SimpleUri uri = new SimpleUri(entry.getKey(), id);
                    result.data.putAll(uri, map.get(id));
                }
            }
            return result;
        }

        @Override
        public JsonElement serialize(BindsConfig src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            SetMultimap<String, SimpleUri> bindByModule = HashMultimap.create();
            for (SimpleUri key : src.data.keySet()) {
                bindByModule.put(key.getNormalisedModuleName(), key);
            }
            List<String> sortedModules = Lists.newArrayList(bindByModule.keySet());
            Collections.sort(sortedModules);
            for (String moduleId : sortedModules) {
                SetMultimap<String, Input> moduleBinds = HashMultimap.create();
                for (SimpleUri bindUri : bindByModule.get(moduleId)) {
                    moduleBinds.putAll(bindUri.getNormalisedObjectName(), src.data.get(bindUri));
                }
                JsonElement map = context.serialize(moduleBinds, SetMultimap.class);
                result.add(moduleId, map);
            }
            return result;
        }
    }

}
