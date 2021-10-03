// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.debug;

import org.terasology.engine.config.Config;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.console.commandSystem.annotations.Command;
import org.terasology.engine.logic.permission.PermissionManager;
import org.terasology.engine.registry.In;

@RegisterSystem
public class RenderingDebugCommands extends BaseComponentSystem {

    @In
    private Config config;

    @Command(shortDescription = "Toggle rendering of entity bounding boxes.",
            value = "debug:renderEntityBoundingBoxes",
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String toggleRenderEntityColliders() {
        boolean wasEnabled = config.getRendering().getDebug().isRenderEntityBoundingBoxes();
        config.getRendering().getDebug().setRenderEntityBoundingBoxes(!wasEnabled);
        return "config.rendering.debug.renderEntityBoundingBoxes is now " + (wasEnabled ? "disabled" : "enabled");
    }

    @Command(shortDescription = "Toggle rendering of chunk bounding boxes.",
            value = "debug:renderChunkBoundingBoxes",
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String toggleRenderChunkBoundingBoxes() {
        boolean wasEnabled = config.getRendering().getDebug().isRenderChunkBoundingBoxes();
        config.getRendering().getDebug().setRenderChunkBoundingBoxes(!wasEnabled);
        return "config.rendering.debug.renderChunkBoundingBoxes is now " + (wasEnabled ? "disabled" : "enabled");
    }

    @Command(shortDescription = "Toggle wireframe rendering on/off.",
            value = "debug:renderWireframe",
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String toggleRenderWireframe() {
        boolean wasEnabled = config.getRendering().getDebug().isWireframe();
        config.getRendering().getDebug().setWireframe(!wasEnabled);
        return "config.rendering.debug.wireframe is now " + (wasEnabled ? "disabled" : "enabled");
    }

    @Command(shortDescription = "Toggle rendering of skeletons.",
            value = "debug:renderSkeletons",
            requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String toggleRenderSkeletons() {
        boolean wasEnabled = config.getRendering().getDebug().isRenderSkeletons();
        config.getRendering().getDebug().setRenderSkeletons(!wasEnabled);
        return "config.rendering.debug.renderSkeletons is now " + (wasEnabled ? "disabled" : "enabled");
    }
}
