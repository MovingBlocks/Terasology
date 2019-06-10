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
package org.terasology.rendering.dag.gsoc;

import org.terasology.assets.ResourceUrn;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.cameras.SubmersibleCamera;
import org.terasology.rendering.dag.stateChanges.BindFbo;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FboConfig;
import org.terasology.rendering.opengl.ScalingFactors;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFbo;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;
import org.terasology.math.geom.Vector3f;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo.FboTexturesTypes.ColorTexture;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;
import static org.terasology.rendering.opengl.ScalingFactors.FULL_SCALE;

/**
 * An instance of this node adds chromatic aberration (currently non-functional), light shafts,
 * 1/8th resolution bloom and vignette onto the rendering achieved so far, stored in the gbuffer.
 * Stores the result into the InitialPostProcessingNode.INITIAL_POST_FBO_URI, to be used at a later stage.
 */
public class TintNode extends NewAbstractNode implements PropertyChangeListener {
    private static final ResourceUrn TINT_MATERIAL_URN = new ResourceUrn("engine:prog.tint");
    private static final SimpleUri TINT_FBO_URI = new SimpleUri("engine:fbo.tint");

    private WorldProvider worldProvider;
    private WorldRenderer worldRenderer;
    private SubmersibleCamera activeCamera;

    private Material tintMaterial;

    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.f, max = 10.f)
    private Vector3f tintRgba;

    private Vector3f defaultTintRgba = new Vector3f(3.f, 1.f, 1.f);

    public TintNode(String nodeUri, Context context) {
        super(nodeUri, context);
    }

    @Override
    public void setDependencies(Context context) {
        tintRgba = defaultTintRgba;

        worldProvider = context.get(WorldProvider.class);

        worldRenderer = context.get(WorldRenderer.class);
        activeCamera = worldRenderer.getActiveCamera();
        DisplayResolutionDependentFbo displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFbo.class);
        // TODO: see if we could write this straight into a GBUFFER

        // Bind new fbo to write to
        FBO tintFbo = requiresFbo(new FboConfig(TINT_FBO_URI, FULL_SCALE, FBO.Type.DEFAULT), displayResolutionDependentFBOs);
        addDesiredStateChange(new BindFbo(tintFbo));
        addOutputFboConnection(1, tintFbo);

        addDesiredStateChange(new EnableMaterial(TINT_MATERIAL_URN));

        tintMaterial = getMaterial(TINT_MATERIAL_URN);

        int textureSlot = 0; // read input FBO
        addDesiredStateChange(new SetInputTextureFromFbo(textureSlot, this.getInputFboData(1), ColorTexture, displayResolutionDependentFBOs, TINT_MATERIAL_URN, "texScene"));
    }

    /**
     * Renders a quad, in turn filling the InitialPostProcessingNode.INITIAL_POST_FBO_URI.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/" + getUri());

        // Common Shader Parameters

        tintMaterial.setFloat("swimming", activeCamera.isUnderWater() ? 1.0f : 0.0f, true);

        // Shader Parameters

        tintMaterial.setFloat3("tint", tintRgba, true);

        // Actual Node Processing

        renderFullscreenQuad();

        PerformanceMonitor.endActivity();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        worldRenderer.requestTaskListRefresh();
    }
}
