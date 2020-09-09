// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.lwjgl;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.subsystem.RenderingSubsystemFactory;
import org.terasology.engine.rendering.world.WorldRenderer;
import org.terasology.engine.rendering.world.WorldRendererImpl;

public class LwjglRenderingSubsystemFactory implements RenderingSubsystemFactory {

    private final GLBufferPool bufferPool;

    public LwjglRenderingSubsystemFactory(GLBufferPool bufferPool) {
        this.bufferPool = bufferPool;
    }

    @Override
    public WorldRenderer createWorldRenderer(Context context) {
        return new WorldRendererImpl(context, bufferPool);
    }
}
