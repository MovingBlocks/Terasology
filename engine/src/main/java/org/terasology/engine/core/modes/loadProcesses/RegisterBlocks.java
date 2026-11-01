// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.context.Lifetime;
import org.terasology.engine.config.Config;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.persistence.typeHandling.extensionTypes.BlockFamilyTypeHandler;
import org.terasology.engine.persistence.typeHandling.extensionTypes.BlockTypeHandler;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.engine.world.block.family.BlockFamilyLibrary;
import org.terasology.engine.world.block.internal.BlockManagerImpl;
import org.terasology.engine.world.block.tiles.WorldAtlas;
import org.terasology.engine.world.block.tiles.WorldAtlasImpl;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.di.ServiceRegistry;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;

public class RegisterBlocks extends SingleStepLoadProcess {
    private final Context context;
    private final ServiceRegistry serviceRegistry;

    public RegisterBlocks(Context context, ServiceRegistry serviceRegistry) {
        this.context = context;
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public String getMessage() {
        return "Registering Blocks...";
    }

    @Override
    public boolean step() {
        NetworkSystem networkSystem = context.get(NetworkSystem.class);
        WorldAtlas atlas = new WorldAtlasImpl(context.get(Config.class).getRendering().getMaxTextureAtlasResolution());
        serviceRegistry.with(WorldAtlas.class).lifetime(Lifetime.Singleton).use(() -> atlas);

        serviceRegistry.with(BlockFamilyLibrary.class).lifetime(Lifetime.Singleton).use(BlockFamilyLibrary.class);

        BlockManagerImpl blockManager;
        if (networkSystem.getMode().isAuthority()) {
            blockManager = new BlockManagerImpl(atlas, context.get(AssetManager.class), true);
            blockManager.subscribe(context.get(NetworkSystem.class));
        } else {
            blockManager = new BlockManagerImpl(atlas, context.get(AssetManager.class), false);
        }
        serviceRegistry.with(BlockManager.class).lifetime(Lifetime.Singleton).use(() -> blockManager);
        context.get(TypeHandlerLibrary.class).addTypeHandler(Block.class, new BlockTypeHandler(blockManager));
        context.get(TypeHandlerLibrary.class).addTypeHandler(BlockFamily.class, new BlockFamilyTypeHandler(blockManager));
        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }

}
