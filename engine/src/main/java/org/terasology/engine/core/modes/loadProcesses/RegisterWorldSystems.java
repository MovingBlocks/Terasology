// Copyright 2024 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.entitySystem.systems.ComponentSystem;
import org.terasology.engine.game.GameManifest;
import org.terasology.engine.registry.InjectionHelper;
import org.terasology.engine.rendering.world.WorldRenderer;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.chunks.ChunkProvider;
import org.terasology.engine.world.chunks.localChunkProvider.LocalChunkProvider;
import org.terasology.engine.world.chunks.localChunkProvider.RelevanceSystem;
import org.terasology.engine.world.generator.WorldGenerator;
import org.terasology.engine.world.internal.EntityAwareWorldProvider;
import org.terasology.engine.world.sun.CelestialSystem;

public class RegisterWorldSystems extends SingleStepLoadProcess {
    private final Context context;
    private final GameManifest gameManifest;

    public RegisterWorldSystems(GameManifest gameManifest, Context context) {
        this.context = context;
        this.gameManifest = gameManifest;
    }

    @Override
    public String getMessage() {
        return "Registering World Systems...";
    }

    @Override
    public boolean step() {
        WorldGenerator worldGenerator = context.get(WorldGenerator.class);
        InjectionHelper.inject(worldGenerator, context);
        // setting the world seed will create the world builder
        worldGenerator.setWorldSeed(gameManifest.getSeed());

        ComponentSystemManager componentSystemManager = context.get(ComponentSystemManager.class);
        context.get(ComponentSystemManager.class).register(context.get(RelevanceSystem.class), "engine:relevanceSystem");
        componentSystemManager.register((ComponentSystem) context.get(BlockEntityRegistry.class), "engine:BlockEntityRegistry");
        componentSystemManager.register((ComponentSystem) context.get(CelestialSystem.class));
        ((LocalChunkProvider) context.get(ChunkProvider.class)).setRelevanceSystem(context.get(RelevanceSystem.class));
        context.get(LocalChunkProvider.class).setBlockEntityRegistry(context.get(EntityAwareWorldProvider.class));

        context.get(WorldRenderer.class).init();
        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
