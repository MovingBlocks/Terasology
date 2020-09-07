/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.config.flexible;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.paths.PathManager;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.registry.InjectionHelper;
import org.terasology.utilities.ReflectionUtil;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

public class AutoConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(AutoConfigManager.class);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final Set<AutoConfig> loadedConfigs = Sets.newHashSet();
    private final TypeHandlerLibrary typeHandlerLibrary;

    public AutoConfigManager(TypeHandlerLibrary typeHandlerLibrary) {
        this.typeHandlerLibrary = typeHandlerLibrary;
    }

    public void loadConfigsIn(Context context) {
        ModuleEnvironment environment = context.get(ModuleManager.class).getEnvironment();

        for (Class<? extends AutoConfig> configClass : environment.getSubtypesOf(AutoConfig.class)) {
            if (context.get(configClass) != null) {
                // We've already initialized this config before
                continue;
            }

            SimpleUri configId = ReflectionUtil.getFullyQualifiedSimpleUriFor(configClass, environment);
            loadConfig(configClass, configId, context);
        }
    }

    private <T extends AutoConfig> void loadConfig(Class<T> clazz, SimpleUri id, Context context) {
        Optional<T> optionalConfig = InjectionHelper.safeCreateWithConstructorInjection(clazz, context);

        if (!optionalConfig.isPresent()) {
            logger.error("Unable to instantiate config {}", id);
            return;
        }

        T config = optionalConfig.get();
        config.setId(id);

        loadedConfigs.add(config);
        context.put(clazz, config);

        loadSettingsFromDisk(clazz, config);
    }

    private <T extends AutoConfig> void loadSettingsFromDisk(Class<T> configClass, T config) {
        AutoConfigSerializer<T> serializer = new AutoConfigSerializer<>(configClass, typeHandlerLibrary);

        Path configPath = getConfigPath(config.getId());

        if (!Files.exists(configPath)) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(configPath, TerasologyConstants.CHARSET)) {
            serializer.deserializeOnto(config, gson.fromJson(reader, JsonElement.class));
        } catch (IOException e) {
            logger.error("Error while loading config {} from disk", config.getId(), e);
        }
    }

    public void saveConfigsToDisk() {
        // TODO: Come up with uniform mechanism to save configs;
        //  currently hardcoded Config is saved right after it is modified
        for (AutoConfig loadedConfig : loadedConfigs) {
            saveConfigToDisk(loadedConfig);
        }
    }

    private <T extends AutoConfig> void saveConfigToDisk(T config) {
        // TODO: Save when screen for config closed
        Class<T> configClass = (Class<T>) config.getClass();
        AutoConfigSerializer<T> serializer = new AutoConfigSerializer<>(configClass, typeHandlerLibrary);

        Path configPath = getConfigPath(config.getId());

        try (BufferedWriter writer = Files.newBufferedWriter(configPath, TerasologyConstants.CHARSET)) {
            JsonElement json = serializer.serialize(config);
            gson.toJson(json, writer);
        } catch (IOException e) {
            logger.error("Error while saving config {} to disk", config.getId(), e);
        }
    }

    private Path getConfigPath(SimpleUri configId) {
        Path filePath = PathManager.getInstance()
                            .getConfigsPath()
                            .resolve(configId.getModuleName().toString())
                            .resolve(configId.getObjectName().toString() + ".cfg");

        // This call ensures that the entire directory structure (like configs/engine/) exists.
        ensureDirectoryExists(filePath);
        return filePath;
    }

    private void ensureDirectoryExists(Path filePath) {
        try {
            Files.createDirectories(filePath.getParent());
        } catch (Exception e) {
            throw new RuntimeException("Cannot create directory for flexibleConfig " + filePath.getFileName() + "!");
        }
    }
}
