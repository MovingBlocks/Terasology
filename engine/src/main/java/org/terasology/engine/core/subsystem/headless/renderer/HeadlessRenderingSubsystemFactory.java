// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.headless.renderer;

import org.terasology.context.Lifetime;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.subsystem.RenderingSubsystemFactory;
import org.terasology.engine.rendering.world.WorldRenderer;
import org.terasology.gestalt.di.ServiceRegistry;

public class HeadlessRenderingSubsystemFactory implements RenderingSubsystemFactory {
    @Override
    public void registerWorldRenderer(Context context, ServiceRegistry serviceRegistry) {
        serviceRegistry.with(WorldRenderer.class).lifetime(Lifetime.Singleton).use(HeadlessWorldRenderer.class);
    }
}
