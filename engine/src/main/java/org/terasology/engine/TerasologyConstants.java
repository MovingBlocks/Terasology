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

package org.terasology.engine;

import com.google.common.base.Charsets;
import org.terasology.naming.Name;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class contains various important constants used by the Terasology engine.
 */
public final class TerasologyConstants {
    /**
     * Name and extension of an entities data file. (Not currently in use) Used to contain state data on world entities
     * for loading/saving. See AbstractStorageManager for details.
     */
    public static final String ENTITY_DATA_FILE = "entity.dat";
    /**
     * Terasology's default server port.
     */
    public static final int DEFAULT_PORT = 25777;
    /**
     * Name and extension of a world data file.
     */
    public static final String WORLD_DATA_FILE = "world.dat";
    /**
     * The name of the default world.
     */
    public static final String MAIN_WORLD = "main";
    /**
     * Default charset used by Terasology.
     */
    public static final Charset CHARSET = Charsets.UTF_8;
    /**
     * Name of the engine module. The engine module contains engine features, without any gameplay elements.
     * Examples of specific features include the entity system, noise functions, core blocks, and utilities.
     */
    public static final Name ENGINE_MODULE = new Name("engine");
    /**
     * Name of the core gameplay module. The core gameplay module adds a minimal environment for some basic gameplay.
     */
    public static final Name CORE_GAMEPLAY_MODULE = new Name("coresamplegameplay");
    /**
     * Name of a module's assets directory.
     */
    public static final String ASSETS_SUBDIRECTORY = "assets";
    /**
     * Name of a module's overrides directory.
     * Overrides replace an entire existing prefab.
     * More info on Overrides/Deltas: <a href="https://github.com/Terasology/TutorialAssetSystem/wiki/Deltas-and-Overrides">Deltas-and-Overrides</>
     */
    public static final String OVERRIDES_SUBDIRECTORY = "overrides";
    /**
     * Name of a module's deltas directory.
     * Deltas of a prefab replace certain fields in an existing version of that prefab with their own fields.
     * More info on Overrides/Deltas: <a href="https://github.com/Terasology/TutorialAssetSystem/wiki/Deltas-and-Overrides">Deltas-and-Overrides</>
     */
    public static final String DELTAS_SUBDIRECTORY = "deltas";
    /**
     * Path to a module's info file.
     */
    public static final Path MODULE_INFO_FILENAME = Paths.get("module.txt");

    private TerasologyConstants() {
    }
}
