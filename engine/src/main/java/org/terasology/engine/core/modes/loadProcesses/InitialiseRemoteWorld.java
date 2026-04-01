// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.context.Lifetime;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.core.subsystem.RenderingSubsystemFactory;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.game.GameManifest;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.recording.DirectionAndOriginPosRecorderList;
import org.terasology.engine.recording.RecordAndReplayCurrentStatus;
import org.terasology.engine.rendering.backdrop.BackdropProvider;
import org.terasology.engine.rendering.backdrop.Skysphere;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.ChunkProvider;
import org.terasology.engine.world.chunks.remoteChunkProvider.RemoteChunkProvider;
import org.terasology.engine.world.internal.EntityAwareWorldProvider;
import org.terasology.engine.world.internal.WorldInfo;
import org.terasology.engine.world.internal.WorldProviderCore;
import org.terasology.engine.world.internal.WorldProviderCoreImpl;
import org.terasology.engine.world.internal.WorldProviderWrapper;
import org.terasology.engine.world.sun.BasicCelestialModel;
import org.terasology.engine.world.sun.CelestialModel;
import org.terasology.engine.world.sun.CelestialSystem;
import org.terasology.engine.world.sun.DefaultCelestialSystem;
import org.terasology.gestalt.di.ServiceRegistry;

import javax.inject.Inject;

public class InitialiseRemoteWorld extends SingleStepLoadProcess {
    private final Context context;
    private final ServiceRegistry serviceRegistry;
    private final GameManifest gameManifest;

    public InitialiseRemoteWorld(Context context, ServiceRegistry serviceRegistry, GameManifest gameManifest) {
        this.context = context;
        this.serviceRegistry = serviceRegistry;
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

        serviceRegistry.with(LocalPlayer.class).lifetime(Lifetime.Singleton).use(() -> localPlayer);
        serviceRegistry.with(ChunkProvider.class).lifetime(Lifetime.Singleton).use(RemoteChunkProvider.class);

        WorldInfo worldInfo = gameManifest.getWorldInfo(TerasologyConstants.MAIN_WORLD);
        serviceRegistry.with(WorldInfo.class).lifetime(Lifetime.Singleton).use(() -> worldInfo);

        // Provides both WorldProviderCore and BlockEntityRegistry
        serviceRegistry.with(WorldProviderCoreWorkAround.class).lifetime(Lifetime.Singleton);
        serviceRegistry.with(WorldProvider.class).lifetime(Lifetime.Singleton).use(WorldProviderWrapper.class);

        serviceRegistry.with(CelestialModel.class).lifetime(Lifetime.Singleton).use(BasicCelestialModel.class);
        serviceRegistry.with(CelestialSystem.class).lifetime(Lifetime.Singleton).use(DefaultCelestialSystem.class);

        // Init. a new world
        serviceRegistry.with(BackdropProvider.class).lifetime(Lifetime.Singleton).use(Skysphere.class);

        RenderingSubsystemFactory engineSubsystemFactory = context.get(RenderingSubsystemFactory.class);
        engineSubsystemFactory.registerWorldRenderer(context, serviceRegistry);

        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }

    /**
     * This work-around exists because you cannot easily have two instances of {@link WorldProviderCore} in the context at a given time.
     * One of these instances exists purely to wrap the other, so we instantiate both at the same time here to leave only one instance.
     */
    public static final class WorldProviderCoreWorkAround extends EntityAwareWorldProvider
            implements WorldProviderCore, BlockEntityRegistry {
        @Inject
        public WorldProviderCoreWorkAround(WorldInfo info, ChunkProvider chunkProvider, BlockManager blockManager,
                                           EngineEntityManager entityManager, ComponentSystemManager componentSystemManager) {
            super(new WorldProviderCoreImpl(info, chunkProvider, blockManager, entityManager), entityManager, componentSystemManager);
        }
    }
}
