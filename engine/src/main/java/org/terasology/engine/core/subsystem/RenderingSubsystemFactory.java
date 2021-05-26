// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem;

import org.terasology.engine.context.Context;
import org.terasology.engine.rendering.world.WorldRenderer;

@FunctionalInterface
public interface RenderingSubsystemFactory {

    WorldRenderer createWorldRenderer(Context context);

}
