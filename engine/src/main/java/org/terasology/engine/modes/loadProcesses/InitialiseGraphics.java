// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.modes.loadProcesses;


import org.joml.Vector4f;
import org.terasology.assets.ResourceUrn;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.modes.ExpectedCost;
import org.terasology.engine.modes.SingleStepLoadProcess;
import org.terasology.naming.Name;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.internal.NUIManagerInternal;
import org.terasology.rendering.primitives.Tessellator;
import org.terasology.rendering.primitives.TessellatorHelper;

/**
 *
 */
@ExpectedCost(1)
public class InitialiseGraphics extends SingleStepLoadProcess {

    @In
    NUIManager nuiManager;

    @Override
    public String getMessage() {
        return "Initialising Graphics";
    }

    @Override
    public boolean step() {
        // Refresh widget library after modules got laoded:
        ((NUIManagerInternal) nuiManager).refreshWidgetsLibrary();

        // TODO: This should be elsewhere
        // Create gelatinousCubeMesh
        Tessellator tessellator = new Tessellator();
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), 0.8f, 0.8f, 0.6f, 0f, 0f, 0f);
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(1.0f, 1.0f, 1.0f, 0.6f), 1.0f, 1.0f, 0.8f, 0f, 0f, 0f);
        tessellator.generateMesh(new ResourceUrn(TerasologyConstants.ENGINE_MODULE, new Name("gelatinousCube")));
        return true;
    }
}
