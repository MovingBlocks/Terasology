// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.subsystem.headless.renderer;

import org.terasology.context.Context;
import org.terasology.engine.subsystem.RenderingSubsystemFactory;
import org.terasology.registry.ContextAwareClassFactory;
import org.terasology.registry.In;
import org.terasology.rendering.world.WorldRenderer;

public class HeadlessRenderingSubsystemFactory implements RenderingSubsystemFactory {

    @In
    ContextAwareClassFactory classFactory;

    @Override
    public WorldRenderer createWorldRenderer(Context context) {
        return classFactory.createWithContext(HeadlessWorldRenderer.class);
    }

}
