/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.rendering.dag.nodes;

import org.terasology.assets.ResourceUrn;
import org.terasology.context.Context;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.dag.ConditionDependentNode;
import org.terasology.rendering.dag.stateChanges.BindFbo;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;
import org.terasology.rendering.opengl.FBO;

import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;

/**
 * A BlurNode takes the content of the color buffer attached to the input FBO and generates
 * a blurred version of it in the color buffer attached to the output FBO.
 */
public class BlurNode extends ConditionDependentNode {
    private static final ResourceUrn BLUR_MATERIAL_URN = new ResourceUrn("engine:prog.blur");

    protected float blurRadius;

    private Material blurMaterial;

    private FBO inputFbo;
    private FBO outputFbo;

    /**
     * Constructs a BlurNode instance.
     *
     * @param inputFbo The input fbo, containing the image to be blurred.
     * @param outputFbo The output fbo, to store the blurred image.
     * @param blurRadius the blur radius: higher values cause higher blur. The shader's default is 16.0f.
     */
    public BlurNode(String nodeUri, Context context, FBO inputFbo, FBO outputFbo, float blurRadius) {
        super(nodeUri, context);

        this.blurRadius = blurRadius;

        this.inputFbo = inputFbo;
        this.outputFbo = outputFbo;
        addDesiredStateChange(new BindFbo(outputFbo));
        addDesiredStateChange(new SetViewportToSizeOf(outputFbo));

        addDesiredStateChange(new EnableMaterial(BLUR_MATERIAL_URN));
        this.blurMaterial = getMaterial(BLUR_MATERIAL_URN);
    }

    /**
     * Performs the blur.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/" + getUri());

        // TODO: these shader-related operations should go in their own StateChange implementations
        blurMaterial.setFloat("radius", blurRadius, true);
        blurMaterial.setFloat2("texelSize", 1.0f / outputFbo.width(), 1.0f / outputFbo.height(), true);

        // TODO: binding the color buffer of an FBO should also be done in its own StateChange implementation
        inputFbo.bindTexture();

        renderFullscreenQuad();

        PerformanceMonitor.endActivity();
    }
}
