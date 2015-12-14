/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.engine.modes.loadProcesses;


import org.terasology.assets.ResourceUrn;
import org.terasology.context.Context;
import org.terasology.engine.TerasologyConstants;
import org.terasology.math.geom.Vector4f;
import org.terasology.naming.Name;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.internal.NUIManagerInternal;
import org.terasology.rendering.primitives.Tessellator;
import org.terasology.rendering.primitives.TessellatorHelper;

/**
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
