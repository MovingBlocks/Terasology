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
    private static final Logger logger = LoggerFactory.getLogger(FlexibleConfigManager.class);

    private Map<SimpleUri, FlexibleConfig> flexibleConfigs = Maps.newHashMap();

    @Override
    public void addConfig(SimpleUri configUri, FlexibleConfig config) {
        if (flexibleConfigs.containsKey(configUri)) {
            throw new RuntimeException("Attempting to add another config with id " + configUri);
        }

        flexibleConfigs.put(configUri, config);
    }

    @Override
    public FlexibleConfig removeConfig(SimpleUri configUri) {
        return flexibleConfigs.remove(configUri);
    }

    @Override
    public FlexibleConfig getConfig(SimpleUri configUri) {
        return flexibleConfigs.get(configUri);
    }

    @Override
    public void load() {
        for (Entry<SimpleUri, FlexibleConfig> entry : flexibleConfigs.entrySet()) {
            SimpleUri flexibleConfigUri = entry.getKey();
            FlexibleConfig flexibleConfig = entry.getValue();

            try (Reader reader = Files.newBufferedReader(getPathForFlexibleConfig(flexibleConfigUri), TerasologyConstants.CHARSET)) {
                flexibleConfig.load(reader);
            } catch (IOException e) {
                // TODO: Handle exception
            }
        }
    }

    @Override
    public void save() {
        for (Entry<SimpleUri, FlexibleConfig> entry : flexibleConfigs.entrySet()) {
            SimpleUri flexibleConfigUri = entry.getKey();
            FlexibleConfig flexibleConfig = entry.getValue();

            try (BufferedWriter writer = Files.newBufferedWriter(getPathForFlexibleConfig(flexibleConfigUri), TerasologyConstants.CHARSET)) {
                flexibleConfig.save(writer);
            } catch (IOException e) {
                logger.error("Failed to save config", e);
            }
        }
    }

    private void ensureDirectoryExists(Path filePath) {
        try {
            Files.createDirectories(filePath.getParent());
        } catch (Exception e) {
            throw new RuntimeException("Cannot create directory for FC!");
        }
    }

    private Path getPathForFlexibleConfig(SimpleUri flexibleConfigUri) {
        Path filePath = PathManager.getInstance()
                                        .getHomePath()
                                        .resolve("configs")
                                        .resolve(flexibleConfigUri.getModuleName().toString())
                                        .resolve(flexibleConfigUri.getObjectName().toString() + ".cfg");
        ensureDirectoryExists(filePath);
        return filePath;
    }
}
