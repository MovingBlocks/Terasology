// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem;

import org.terasology.engine.context.Context;
import org.terasology.gestalt.di.ServiceRegistry;

@FunctionalInterface
public interface RenderingSubsystemFactory {
    void registerWorldRenderer(Context context, ServiceRegistry serviceRegistry);
}
