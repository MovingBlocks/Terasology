/*
 * Copyright 2017 MovingBlocks
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

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.paths.PathManager;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;

public class FlexibleConfigManagerImpl implements FlexibleConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(FlexibleConfigManagerImpl.class);

    private Map<SimpleUri, FlexibleConfig> flexibleConfigs = Maps.newHashMap();

    @Override
    public void addConfig(SimpleUri configId, FlexibleConfig config) {
        if (flexibleConfigs.containsKey(configId)) {
            throw new RuntimeException("Attempting to add another config with id " + configId);
        }

        flexibleConfigs.put(configId, config);
    }

    @Override
    public FlexibleConfig removeConfig(SimpleUri configId) {
        return flexibleConfigs.remove(configId);
    }

    @Override
    public FlexibleConfig getConfig(SimpleUri configId) {
        return flexibleConfigs.get(configId);
    }

    @Override
    public void loadAllConfigs() {
        for (Entry<SimpleUri, FlexibleConfig> entry : flexibleConfigs.entrySet()) {
            SimpleUri flexibleConfigId = entry.getKey();
            FlexibleConfig flexibleConfig = entry.getValue();

            Path configPath = getPathForFlexibleConfig(flexibleConfigId);
            if (Files.exists(configPath)) {
                try (Reader reader = Files.newBufferedReader(configPath, TerasologyConstants.CHARSET)) {
                    flexibleConfig.load(reader);
                } catch (IOException e) {
                    throw new RuntimeException("Exception loading config file for configId " + entry.getKey());
                }
            } // else: The config does not exist, so the default values will be used.

        }
    }

    private void ensureDirectoryExists(Path filePath) {
        try {
            Files.createDirectories(filePath.getParent());
        } catch (Exception e) {
            throw new RuntimeException("Cannot create directory for flexibleConfig " + filePath.getFileName() + "!");
        }
    }

    @Override
    public void saveAllConfigs() {
        for (Entry<SimpleUri, FlexibleConfig> entry : flexibleConfigs.entrySet()) {
            SimpleUri flexibleConfigId = entry.getKey();
            FlexibleConfig flexibleConfig = entry.getValue();

            try (BufferedWriter writer = Files.newBufferedWriter(getPathForFlexibleConfig(flexibleConfigId), TerasologyConstants.CHARSET)) {
                flexibleConfig.save(writer);
            } catch (IOException e) {
                logger.error("Failed to save config", e);
            }
        }
    }

    private Path getPathForFlexibleConfig(SimpleUri flexibleConfigId) {
        Path filePath = PathManager.getInstance()
                                   .getConfigsPath()
                                   .resolve(flexibleConfigId.getModuleName().toString())
                                   .resolve(flexibleConfigId.getObjectName().toString() + ".cfg");
        // This call ensures that the entire directory structure (like configs/engine/) exists.
        ensureDirectoryExists(filePath);
        return filePath;
    }
}
