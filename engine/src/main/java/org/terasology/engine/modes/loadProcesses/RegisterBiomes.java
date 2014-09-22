/*
 * Copyright 2014 MovingBlocks
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

package org.terasology.engine.modes.loadProcesses;

import org.terasology.config.Config;
import org.terasology.engine.module.ModuleManager;
import org.terasology.game.GameManifest;
import org.terasology.module.ModuleEnvironment;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.biomes.BiomeManager;
import org.terasology.world.biomes.BiomeRegistrator;
import org.terasology.world.biomes.BiomeRegistry;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamilyFactoryRegistry;
import org.terasology.world.block.internal.BlockManagerImpl;
import org.terasology.world.block.loader.WorldAtlas;
import org.terasology.world.block.loader.WorldAtlasImpl;

/**
 * @author Immortius
 */
public class RegisterBiomes extends SingleStepLoadProcess {

    private GameManifest gameManifest;

    public RegisterBiomes(GameManifest gameManifest) {
        this.gameManifest = gameManifest;
    }

    @Override
    public String getMessage() {
        return "Registering Biomes...";
    }

    @Override
    public boolean step() {
        NetworkSystem networkSystem = CoreRegistry.get(NetworkSystem.class);
        ModuleEnvironment moduleEnvironment = CoreRegistry.get(ModuleManager.class).getEnvironment();

        BiomeManager biomeManager;
        if (networkSystem.getMode().isAuthority()) {
            biomeManager = new BiomeManager(moduleEnvironment, gameManifest.getBiomeIdMap());
//            biomeManager.subscribe(CoreRegistry.get(NetworkSystem.class));
            // TODO figure out what this does
        } else {
            biomeManager = new BiomeManager(moduleEnvironment, gameManifest.getBiomeIdMap());
        }
        CoreRegistry.put(BiomeManager.class, biomeManager);
        CoreRegistry.put(BiomeRegistry.class, biomeManager); // This registration is for other modules

        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }

}
