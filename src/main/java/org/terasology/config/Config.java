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

import com.google.common.collect.Multimap;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.input.Input;
import org.terasology.logic.manager.PathManager;
import org.terasology.utilities.gson.InputHandler;
import org.terasology.utilities.gson.MultimapHandler;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Terasology user config. Holds the various global configuration information that the user can modify. It can be saved
 * and loaded in a JSON format.
 *
 * @author Immortius
 */
public final class Config {
    private static final Logger logger = LoggerFactory.getLogger(Config.class);
    private InputConfig input = new InputConfig();
    private ModConfig defaultModConfig = new ModConfig();
    private AdvancedConfig advanced = AdvancedConfig.createDefault();
    private SoundConfig soundConfig = new SoundConfig();

    /**
     * Create a new, empty config
     */
    public Config() {
    }

    /**
     * @return Input configuration (mostly binds)
     */
    public InputConfig getInputConfig() {
        return input;
    }

    public ModConfig getDefaultModConfig() {
        return defaultModConfig;
    }
    
    public AdvancedConfig getAdvancedConfig() {
        return advanced;
    }

    public SoundConfig getSoundConfig() {
        return soundConfig;
    }

    /**
     * Saves this config to the default configuration file
     */
    public void save() {
        try {
            save(getConfigFile(), this);
        } catch (IOException e) {
            logger.error("Failed to save config", e);
        }
    }

    /**
     * @return The default configuration file location
     */
    public static File getConfigFile() {
        return new File(PathManager.getInstance().getWorldPath(), "config.cfg");
    }

    /**
     * Saves a Config to a file, in a JSON format
     * @param toFile
     * @param config
     * @throws IOException
     */
    public static void save(File toFile, Config config) throws IOException {
        FileWriter writer = new FileWriter(toFile);
        try {
            new GsonBuilder()
                    .registerTypeAdapter(InputConfig.class, new InputConfig.Handler())
                    .registerTypeAdapter(Multimap.class, new MultimapHandler<Input>(Input.class))
                    .registerTypeAdapter(Input.class, new InputHandler())
                    .registerTypeAdapter(AdvancedConfig.class, new AdvancedConfig.Handler())
                    .setPrettyPrinting().create().toJson(config, writer);
        } finally {
            // JAVA7: better closing support
            writer.close();
        }
    }

    /**
     * Loads a JSON format configuration file as a new Config
     * @param fromFile
     * @return The loaded configuration
     * @throws IOException
     */
    public static Config load(File fromFile) throws IOException {
        FileReader reader = new FileReader(fromFile);
        try {
            return new GsonBuilder()
                    .registerTypeAdapter(InputConfig.class, new InputConfig.Handler())
                    .registerTypeAdapter(Multimap.class, new MultimapHandler<Input>(Input.class))
                    .registerTypeAdapter(Input.class, new InputHandler())
                    .registerTypeAdapter(AdvancedConfig.class, new AdvancedConfig.Handler())
                    .create().fromJson(reader, Config.class);
        } finally {
            reader.close();
        }
    }
}
