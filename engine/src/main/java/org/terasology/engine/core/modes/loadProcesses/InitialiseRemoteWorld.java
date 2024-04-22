// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.engine.config.Config;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.core.subsystem.RenderingSubsystemFactory;
import org.terasology.engine.game.GameManifest;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.recording.DirectionAndOriginPosRecorderList;
import org.terasology.engine.recording.RecordAndReplayCurrentStatus;
import org.terasology.engine.rendering.backdrop.BackdropProvider;
import org.terasology.engine.rendering.backdrop.Skysphere;
import org.terasology.engine.rendering.cameras.Camera;
import org.terasology.engine.rendering.world.WorldRenderer;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.engine.world.chunks.remoteChunkProvider.RemoteChunkProvider;
import org.terasology.engine.world.internal.EntityAwareWorldProvider;
import org.terasology.engine.world.internal.WorldProviderCoreImpl;
import org.terasology.engine.world.internal.WorldProviderWrapper;
import org.terasology.engine.world.sun.BasicCelestialModel;
import org.terasology.engine.world.sun.CelestialSystem;
import org.terasology.engine.world.sun.DefaultCelestialSystem;

public class InitialiseRemoteWorld extends SingleStepLoadProcess {
    private final Context context;
    private final GameManifest gameManifest;


    public InitialiseRemoteWorld(Context context, GameManifest gameManifest) {
        this.context = context;
        this.gameManifest = gameManifest;
    }

    @Override
    public String getMessage() {
        return "Setting up remote world...";
    }

    @Override
    public boolean step() {

        // TODO: These shouldn't be done here, nor so strongly tied to the world renderer
        LocalPlayer localPlayer = new LocalPlayer();
        localPlayer.setRecordAndReplayClasses(context.get(DirectionAndOriginPosRecorderList.class), context.get(RecordAndReplayCurrentStatus.class));
        context.put(LocalPlayer.class, localPlayer);
        BlockManager blockManager = context.get(BlockManager.class);
        ExtraBlockDataManager extraDataManager = context.get(ExtraBlockDataManager.class);

        RemoteChunkProvider chunkProvider = new RemoteChunkProvider(blockManager, localPlayer, context.get(Config.class));

        WorldProviderCoreImpl worldProviderCore = new WorldProviderCoreImpl(gameManifest.getWorldInfo(TerasologyConstants.MAIN_WORLD), chunkProvider,
                blockManager.getBlock(BlockManager.UNLOADED_ID), context);
        EntityAwareWorldProvider entityWorldProvider = new EntityAwareWorldProvider(worldProviderCore, context);
        WorldProvider worldProvider = new WorldProviderWrapper(entityWorldProvider, extraDataManager);
        context.put(WorldProvider.class, worldProvider);
        context.put(BlockEntityRegistry.class, entityWorldProvider);
        context.get(ComponentSystemManager.class).register(entityWorldProvider, "engine:BlockEntityRegistry");

        DefaultCelestialSystem celestialSystem = new DefaultCelestialSystem(new BasicCelestialModel(), context);
        context.put(CelestialSystem.class, celestialSystem);
        context.get(ComponentSystemManager.class).register(celestialSystem);

        // Init. a new world
        context.put(BackdropProvider.class, new Skysphere(context));

        RenderingSubsystemFactory engineSubsystemFactory = context.get(RenderingSubsystemFactory.class);
        WorldRenderer worldRenderer = engineSubsystemFactory.createWorldRenderer(context);
        float reflectionHeight = context.get(NetworkSystem.class).getServer().getInfo().getReflectionHeight();
        worldRenderer.getActiveCamera().setReflectionHeight(reflectionHeight);
        context.put(WorldRenderer.class, worldRenderer);
        // TODO: These shouldn't be done here, nor so strongly tied to the world renderer
        context.put(Camera.class, worldRenderer.getActiveCamera());
        context.get(NetworkSystem.class).setRemoteWorldProvider(chunkProvider);

        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }

}
