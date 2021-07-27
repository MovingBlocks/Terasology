// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.headless.renderer;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.subsystem.RenderingSubsystemFactory;
import org.terasology.engine.rendering.world.WorldRenderer;

public class HeadlessRenderingSubsystemFactory implements RenderingSubsystemFactory {

    @Override
    public WorldRenderer createWorldRenderer(Context context) {
        return new HeadlessWorldRenderer(context);
    }


}
