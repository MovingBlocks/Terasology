// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.lwjgl;

import org.terasology.context.Lifetime;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.subsystem.RenderingSubsystemFactory;
import org.terasology.engine.rendering.cameras.Camera;
import org.terasology.engine.rendering.cameras.PerspectiveCamera;
import org.terasology.engine.rendering.dag.RenderGraph;
import org.terasology.engine.rendering.dag.RenderTaskListGenerator;
import org.terasology.engine.rendering.opengl.ScreenGrabber;
import org.terasology.engine.rendering.opengl.fbms.DisplayResolutionDependentFbo;
import org.terasology.engine.rendering.primitives.ChunkTessellator;
import org.terasology.engine.rendering.world.RenderableWorld;
import org.terasology.engine.rendering.world.RenderableWorldImpl;
import org.terasology.engine.rendering.world.WorldRenderer;
import org.terasology.engine.rendering.world.WorldRendererImpl;
import org.terasology.engine.world.chunks.LodChunkProvider;
import org.terasology.gestalt.di.ServiceRegistry;

public class LwjglRenderingSubsystemFactory implements RenderingSubsystemFactory {
    public LwjglRenderingSubsystemFactory() {
    }

    @Override
    public void registerWorldRenderer(Context context, ServiceRegistry serviceRegistry) {
        ChunkTessellator chunkTessellator = new ChunkTessellator();
        serviceRegistry.with(ChunkTessellator.class).lifetime(Lifetime.Singleton).use(() -> chunkTessellator);
        serviceRegistry.with(RenderGraph.class).lifetime(Lifetime.Singleton).use(RenderGraph.class);

        serviceRegistry.with(ScreenGrabber.class).lifetime(Lifetime.Singleton).use(ScreenGrabber.class);
        serviceRegistry.with(DisplayResolutionDependentFbo.class).lifetime(Lifetime.Singleton).use(DisplayResolutionDependentFbo.class);

        serviceRegistry.with(LodChunkProvider.class).lifetime(Lifetime.Singleton).use(LodChunkProvider.class);
        serviceRegistry.with(RenderableWorld.class).lifetime(Lifetime.Singleton).use(RenderableWorldImpl.class);

        RenderTaskListGenerator renderTaskListGenerator = new RenderTaskListGenerator();
        serviceRegistry.with(RenderTaskListGenerator.class).lifetime(Lifetime.Singleton).use(() -> renderTaskListGenerator);
        serviceRegistry.with(Camera.class).lifetime(Lifetime.Singleton).use(PerspectiveCamera.class);
        serviceRegistry.with(WorldRenderer.class).lifetime(Lifetime.Singleton).use(WorldRendererImpl.class);
    }
}
