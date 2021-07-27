// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core;

import com.google.common.base.Charsets;
import org.terasology.gestalt.naming.Name;

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
    //TODO: CSG moved out of the engine repo, so this should either be removed or reference the Builder Sample Gameplay
    //      which still lives in the engine repo.
    public static final Name CORE_GAMEPLAY_MODULE = new Name("coresamplegameplay");
    /**
     * Name of a module's assets directory.
     */
    public static final String ASSETS_SUBDIRECTORY = "assets";
    /**
     * Name of a module's overrides directory.
     * Overrides replace an entire existing prefab.
     * More info on Overrides/Deltas: <a href="https://github.com/Terasology/TutorialAssetSystem/wiki/Deltas-and-Overrides">Deltas-and-Overrides</a>
     */
    public static final String OVERRIDES_SUBDIRECTORY = "overrides";
    /**
     * Name of a module's deltas directory.
     * Deltas of a prefab replace certain fields in an existing version of that prefab with their own fields.
     * More info on Overrides/Deltas: <a href="https://github.com/Terasology/TutorialAssetSystem/wiki/Deltas-and-Overrides">Deltas-and-Overrides</a>
     */
    public static final String DELTAS_SUBDIRECTORY = "deltas";
    /**
     * Path to a module's info file.
     */
    public static final Path MODULE_INFO_FILENAME = Paths.get("module.txt");

    private TerasologyConstants() {
    }
}
