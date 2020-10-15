// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.modes.loadProcesses;

import org.jetbrains.annotations.NotNull;
import org.terasology.assets.management.AssetManager;
import org.terasology.engine.modes.ExpectedCost;
import org.terasology.engine.modes.SingleStepLoadProcess;
import org.terasology.game.GameManifest;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.persistence.typeHandling.extensionTypes.BlockFamilyTypeHandler;
import org.terasology.persistence.typeHandling.extensionTypes.BlockTypeHandler;
import org.terasology.registry.ContextAwareClassFactory;
import org.terasology.registry.In;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.family.BlockFamilyLibrary;
import org.terasology.world.block.internal.BlockManagerImpl;
import org.terasology.world.block.tiles.WorldAtlas;
import org.terasology.world.block.tiles.WorldAtlasImpl;

@ExpectedCost(1)
public class RegisterBlocks extends SingleStepLoadProcess {

    @In
    private ContextAwareClassFactory classFactory;
    @In
    private GameManifest gameManifest;
    @In
    private NetworkSystem networkSystem;
    @In
    private TypeHandlerLibrary typeHandlerLibrary;
    @In
    private AssetManager assetManager;
    @In
    private NetworkMode networkMode;

    @Override
    public String getMessage() {
        return "Registering Blocks...";
    }

    @Override
    public boolean step() {
        WorldAtlas atlas = classFactory.createToContext(WorldAtlasImpl.class, WorldAtlas.class);
        classFactory.createToContext(BlockFamilyLibrary.class);

        BlockManagerImpl blockManager = classFactory.createToContext(BlockManager.class,
                () -> createBlockManager(atlas));

        typeHandlerLibrary.addTypeHandler(Block.class, new BlockTypeHandler(blockManager));
        typeHandlerLibrary.addTypeHandler(BlockFamily.class, new BlockFamilyTypeHandler(blockManager));

        blockManager.initialise(gameManifest.getRegisteredBlockFamilies(), gameManifest.getBlockIdMap());

        return true;
    }

    @NotNull
    private BlockManagerImpl createBlockManager(WorldAtlas atlas) {
        BlockManagerImpl blockManager;
        if (networkMode.isAuthority()) {
            blockManager = new BlockManagerImpl(atlas, assetManager, true);
            blockManager.subscribe(networkSystem);
        } else {
            blockManager = new BlockManagerImpl(atlas, assetManager, false);
        }
        return blockManager;
    }
}
