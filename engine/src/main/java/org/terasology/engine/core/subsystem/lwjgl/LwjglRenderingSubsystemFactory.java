// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.lwjgl;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.subsystem.RenderingSubsystemFactory;
import org.terasology.engine.rendering.world.WorldRenderer;
import org.terasology.engine.rendering.world.WorldRendererImpl;

public class LwjglRenderingSubsystemFactory implements RenderingSubsystemFactory {


    public LwjglRenderingSubsystemFactory() {

    }

    @Override
    public WorldRenderer createWorldRenderer(Context context) {
        return new WorldRendererImpl(context);
    }
}
