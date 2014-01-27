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

package org.terasology.engine.modes.loadProcesses;

import org.terasology.config.Config;
import org.terasology.registry.CoreRegistry;
import org.terasology.game.GameManifest;
import org.terasology.network.NetworkSystem;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamilyFactoryRegistry;
import org.terasology.world.block.internal.BlockManagerImpl;
import org.terasology.world.block.loader.WorldAtlas;
import org.terasology.world.block.loader.WorldAtlasImpl;

/**
 * @author Immortius
 */
public class RegisterBlocks extends SingleStepLoadProcess {

    private GameManifest gameManifest;

    public RegisterBlocks(GameManifest gameManifest) {
        this.gameManifest = gameManifest;
    }

    @Override
    public String getMessage() {
        return "Registering Blocks...";
    }

    @Override
    public boolean step() {
        NetworkSystem networkSystem = CoreRegistry.get(NetworkSystem.class);
        WorldAtlas atlas = new WorldAtlasImpl(CoreRegistry.get(Config.class).getRendering().getMaxTextureAtlasResolution());
        CoreRegistry.put(WorldAtlas.class, atlas);

        BlockManagerImpl blockManager;
        BlockFamilyFactoryRegistry blockFamilyFactoryRegistry = CoreRegistry.get(BlockFamilyFactoryRegistry.class);
        if (networkSystem.getMode().isAuthority()) {
            blockManager = new BlockManagerImpl(atlas, gameManifest.getRegisteredBlockFamilies(), gameManifest.getBlockIdMap(), true, blockFamilyFactoryRegistry);
            blockManager.subscribe(CoreRegistry.get(NetworkSystem.class));
        } else {
            blockManager = new BlockManagerImpl(atlas, gameManifest.getRegisteredBlockFamilies(), gameManifest.getBlockIdMap(), false, blockFamilyFactoryRegistry);
        }
        CoreRegistry.put(BlockManager.class, blockManager);

        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }

}
