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

import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.registry.CoreRegistry;
import org.terasology.engine.TerasologyConstants;
import org.terasology.rendering.ShaderManager;
import org.terasology.rendering.primitives.Tessellator;
import org.terasology.rendering.primitives.TessellatorHelper;

import javax.vecmath.Vector4f;

/**
 * @author Immortius
 */
public class InitialiseGraphics extends SingleStepLoadProcess {
    @Override
    public String getMessage() {
        return "Initialising Graphics";
    }

    @Override
    public boolean step() {
        CoreRegistry.get(ShaderManager.class).initShaders();

        // TODO: This should be elsewhere
        // Create gelatinousCubeMesh
        Tessellator tessellator = new Tessellator();
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), 0.8f, 0.8f, 0.6f, 0f, 0f, 0f);
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(1.0f, 1.0f, 1.0f, 0.6f), 1.0f, 1.0f, 0.8f, 0f, 0f, 0f);
        tessellator.generateMesh(new AssetUri(AssetType.MESH, TerasologyConstants.ENGINE_MODULE, "gelatinousCube"));
        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
