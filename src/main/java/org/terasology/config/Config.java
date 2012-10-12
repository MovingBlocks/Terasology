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
import org.terasology.game.CoreRegistry;
import org.terasology.input.Input;
import org.terasology.input.InputType;
import org.terasology.logic.manager.PathManager;
import org.terasology.logic.mod.ModManager;
import org.terasology.utilities.gson.InputHandler;
import org.terasology.utilities.gson.MultimapHandler;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Immortius
 */
public class Config {
    private InputConfig input = new InputConfig();

    public Config() {
    }

    public InputConfig getInputConfig() {
        return input;
    }

    public static void save(File toFile, Config config) throws IOException {
        FileWriter writer = new FileWriter(toFile);
        try {
            new GsonBuilder()
                    .registerTypeAdapter(InputConfig.class, new InputConfig.Handler())
                    .registerTypeAdapter(Multimap.class, new MultimapHandler<Input>(Input.class))
                    .registerTypeAdapter(Input.class, new InputHandler())
                    .setPrettyPrinting().create().toJson(config, writer);
        } finally {
            // JAVA7: better closing support
            writer.close();
        }
    }

    public static Config load(File fromFile) throws IOException {
        FileReader reader = new FileReader(fromFile);
        try {
            return new GsonBuilder()
                    .registerTypeAdapter(InputConfig.class, new InputConfig.Handler())
                    .registerTypeAdapter(Multimap.class, new MultimapHandler<Input>(Input.class))
                    .registerTypeAdapter(Input.class, new InputHandler())
                    .create().fromJson(reader, Config.class);
        } finally {
            reader.close();
        }
    }
}
