// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.modes.loadProcesses;


import org.joml.Vector4f;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.rendering.nui.internal.NUIManagerInternal;
import org.terasology.engine.rendering.primitives.Tessellator;
import org.terasology.engine.rendering.primitives.TessellatorHelper;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.naming.Name;

/**
 *
 */
public class InitialiseGraphics extends SingleStepLoadProcess {

    private final Context context;

    public InitialiseGraphics(Context context) {
        this.context = context;
    }

    @Override
    public String getMessage() {
        return "Initialising Graphics";
    }

    @Override
    public boolean step() {
        // Refresh widget library after modules got laoded:
        NUIManager nuiManager = context.get(NUIManager.class);
        ((NUIManagerInternal) nuiManager).refreshWidgetsLibrary();

        // TODO: This should be elsewhere
        // Create gelatinousCubeMesh
        Tessellator tessellator = new Tessellator();
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), 0.8f, 0.8f, 0.6f, 0f, 0f, 0f);
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(1.0f, 1.0f, 1.0f, 0.6f), 1.0f, 1.0f, 0.8f, 0f, 0f, 0f);
        tessellator.generateMesh(new ResourceUrn(TerasologyConstants.ENGINE_MODULE, new Name("gelatinousCube")));
        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
