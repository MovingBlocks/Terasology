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
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.context.Context;
import org.terasology.input.cameraTarget.CameraTargetSystem;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.texture.TextureUtil;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.StateChange;
import org.terasology.rendering.dag.stateChanges.BindFbo;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetInputTexture2D;
import org.terasology.rendering.dag.stateChanges.SetInputTexture3D;
import org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo;
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.ScreenGrabber;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static org.terasology.rendering.dag.nodes.LateBlurNode.SECOND_LATE_BLUR_FBO_URI;
import static org.terasology.rendering.dag.nodes.ToneMappingNode.TONE_MAPPING_FBO_URI;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo.FboTexturesTypes.ColorTexture;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo.FboTexturesTypes.DepthStencilTexture;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;
import static org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs.FINAL_BUFFER;

/**
 * An instance of this class adds depth of field blur, motion blur and film grain to the rendering
 * of the scene obtained so far. Furthermore, depending if a screenshot has been requested,
 * it instructs the ScreenGrabber to save it to a file.
 *
 * If RenderingDebugConfig.isEnabled() returns true, this node is instead responsible for displaying
 * the content of a number of technical buffers rather than the final, post-processed rendering
 * of the scene.
 */
public class FinalPostProcessingNode extends AbstractNode implements PropertyChangeListener {
    private static final ResourceUrn POST_MATERIAL_URN = new ResourceUrn("engine:prog.post");

    private WorldRenderer worldRenderer;
    private RenderingConfig renderingConfig;
    private ScreenGrabber screenGrabber;

    private Material postMaterial;

    private Random randomGenerator = new FastRandom();

    private CameraTargetSystem cameraTargetSystem;
    private Camera activeCamera;

    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 1.0f)
    private float filmGrainIntensity = 0.05f;

    private FBO lastUpdatedGBuffer;

    private boolean isFilmGrainEnabled;
    private boolean isMotionBlurEnabled;

    private StateChange setBlurTexture;
    private StateChange setNoiseTexture;

    private final int noiseTextureSize = 1024;

    public FinalPostProcessingNode(String nodeUri, Context context) {
        super(nodeUri, context);

        worldRenderer = context.get(WorldRenderer.class);
        activeCamera = worldRenderer.getActiveCamera();
        screenGrabber = context.get(ScreenGrabber.class);
        cameraTargetSystem = context.get(CameraTargetSystem.class);

        postMaterial = getMaterial(POST_MATERIAL_URN);

        addDesiredStateChange(new EnableMaterial(POST_MATERIAL_URN));

        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
        FBO finalBuffer = displayResolutionDependentFBOs.get(FINAL_BUFFER);
        addDesiredStateChange(new BindFbo(finalBuffer));
        addDesiredStateChange(new SetViewportToSizeOf(finalBuffer));

        renderingConfig = context.get(Config.class).getRendering();
        isFilmGrainEnabled = renderingConfig.isFilmGrain();
        renderingConfig.subscribe(RenderingConfig.FILM_GRAIN, this);
        isMotionBlurEnabled = renderingConfig.isMotionBlur();
        renderingConfig.subscribe(RenderingConfig.MOTION_BLUR, this);
        renderingConfig.subscribe(RenderingConfig.BLUR_INTENSITY, this);

        lastUpdatedGBuffer = displayResolutionDependentFBOs.getGBufferPair().getLastUpdatedFbo();

        int texId = 0;
        addDesiredStateChange(new SetInputTextureFromFbo(texId++, TONE_MAPPING_FBO_URI, ColorTexture, displayResolutionDependentFBOs, POST_MATERIAL_URN, "texScene"));
        addDesiredStateChange(new SetInputTextureFromFbo(texId++, lastUpdatedGBuffer, DepthStencilTexture, displayResolutionDependentFBOs, POST_MATERIAL_URN, "texDepth"));
        setBlurTexture = new SetInputTextureFromFbo(texId++, SECOND_LATE_BLUR_FBO_URI, ColorTexture, displayResolutionDependentFBOs, POST_MATERIAL_URN, "texBlur");
        addDesiredStateChange(new SetInputTexture3D(texId++, "engine:colorGradingLut1", POST_MATERIAL_URN, "texColorGradingLut"));
        // TODO: evaluate the possibility to use GPU-based noise algorithms instead of CPU-generated textures.
        setNoiseTexture = new SetInputTexture2D(texId, TextureUtil.getTextureUriForWhiteNoise(noiseTextureSize, 0x1234, 0, 512).toString(), POST_MATERIAL_URN, "texNoise");

        if (renderingConfig.getBlurIntensity() != 0) {
            addDesiredStateChange(setBlurTexture);
        }

        if (isFilmGrainEnabled) {
            addDesiredStateChange(setNoiseTexture);
        }
    }

    /**
     * Execute the final post processing on the rendering of the scene obtained so far.
     *
     * It uses the data stored in multiple FBOs as input and the FINAL FBO to store its output, rendering everything to a quad.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/" + getUri());

        postMaterial.setFloat("focalDistance", cameraTargetSystem.getFocalDistance(), true); //for use in DOF effect

        if (isFilmGrainEnabled) {
            postMaterial.setFloat("grainIntensity", filmGrainIntensity, true);
            postMaterial.setFloat("noiseOffset", randomGenerator.nextFloat(), true);

            postMaterial.setFloat2("noiseSize", noiseTextureSize, noiseTextureSize, true);
            postMaterial.setFloat2("renderTargetSize", lastUpdatedGBuffer.width(), lastUpdatedGBuffer.height(), true);
        }

        if (isMotionBlurEnabled) {
            postMaterial.setMatrix4("invViewProjMatrix", activeCamera.getInverseViewProjectionMatrix(), true);
            postMaterial.setMatrix4("prevViewProjMatrix", activeCamera.getPrevViewProjectionMatrix(), true);
        }

        renderFullscreenQuad();

        if (screenGrabber.isTakingScreenshot()) {
            screenGrabber.saveScreenshot();
        }

        PerformanceMonitor.endActivity();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        String propertyName = event.getPropertyName();

        switch (propertyName) {
            case RenderingConfig.FILM_GRAIN:
                isFilmGrainEnabled = renderingConfig.isFilmGrain();
                if (isFilmGrainEnabled) {
                    addDesiredStateChange(setNoiseTexture);
                } else {
                    removeDesiredStateChange(setNoiseTexture);
                }
                break;

            case RenderingConfig.MOTION_BLUR:
                isMotionBlurEnabled = renderingConfig.isMotionBlur();
                break;

            case RenderingConfig.BLUR_INTENSITY:
                if (renderingConfig.getBlurIntensity() != 0) {
                    addDesiredStateChange(setBlurTexture);
                } else {
                    removeDesiredStateChange(setBlurTexture);
                }
                break;
        }

        worldRenderer.requestTaskListRefresh();
    }
}
