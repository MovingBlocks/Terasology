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

import org.terasology.assets.management.AssetManager;
import org.terasology.config.Config;
import org.terasology.context.Context;
import org.terasology.game.GameManifest;
import org.terasology.network.NetworkSystem;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.persistence.typeHandling.extensionTypes.BlockFamilyTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.BlockTypeHandler;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.internal.BlockManagerImpl;
import org.terasology.world.block.tiles.WorldAtlas;
import org.terasology.world.block.tiles.WorldAtlasImpl;

/**
 */
public class RegisterBlocks extends SingleStepLoadProcess {
    private final Context context;
    private final GameManifest gameManifest;

    public RegisterBlocks(Context context, GameManifest gameManifest) {
        this.context = context;
        this.gameManifest = gameManifest;
    }

    @Override
    public String getMessage() {
        return "Registering Blocks...";
    }

    @Override
    public boolean step() {
        NetworkSystem networkSystem = context.get(NetworkSystem.class);
        WorldAtlas atlas = new WorldAtlasImpl(context.get(Config.class).getRendering().getMaxTextureAtlasResolution());
        context.put(WorldAtlas.class, atlas);

        BlockManagerImpl blockManager;
        if (networkSystem.getMode().isAuthority()) {
            blockManager = new BlockManagerImpl(atlas, context.get(AssetManager.class), true);
            blockManager.subscribe(context.get(NetworkSystem.class));
        } else {
            blockManager = new BlockManagerImpl(atlas, context.get(AssetManager.class), false);
        }
        context.put(BlockManager.class, blockManager);
        context.get(TypeSerializationLibrary.class).add(Block.class, new BlockTypeHandler(blockManager));
        context.get(TypeSerializationLibrary.class).add(BlockFamily.class, new BlockFamilyTypeHandler(blockManager));

        blockManager.initialise(gameManifest.getRegisteredBlockFamilies(), gameManifest.getBlockIdMap());

        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }

}
