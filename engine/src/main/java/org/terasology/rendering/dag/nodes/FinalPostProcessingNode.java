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

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.context.Context;
import org.terasology.input.cameraTarget.CameraTargetSystem;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureUtil;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.stateChanges.BindFbo;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo;
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.ScreenGrabber;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.Assets;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static org.lwjgl.opengl.GL11.glBindTexture;
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

    private RenderingConfig renderingConfig;
    private ScreenGrabber screenGrabber;

    private Material postMaterial;

    private Random rand = new FastRandom();

    private CameraTargetSystem cameraTargetSystem;
    private Camera activeCamera;

    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 1.0f)
    private float filmGrainIntensity = 0.05f;

    private FBO lastUpdatedGBuffer;

    private boolean isFilmGrainEnabled;
    private boolean isMotionBlurEnabled;

    public FinalPostProcessingNode(Context context) {
        activeCamera = context.get(WorldRenderer.class).getActiveCamera();
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
        isMotionBlurEnabled = renderingConfig.isMotionBlur();

        lastUpdatedGBuffer = displayResolutionDependentFBOs.getGBufferPair().getLastUpdatedFbo();

        int texId = 0;
        addDesiredStateChange(new SetInputTextureFromFbo(texId++, TONE_MAPPING_FBO_URI, ColorTexture, displayResolutionDependentFBOs, POST_MATERIAL_URN, "texScene"));
        addDesiredStateChange(new SetInputTextureFromFbo(texId++, lastUpdatedGBuffer, DepthStencilTexture, displayResolutionDependentFBOs, POST_MATERIAL_URN, "texDepth"));
        if (renderingConfig.getBlurIntensity() != 0) {
            addDesiredStateChange(new SetInputTextureFromFbo(texId, SECOND_LATE_BLUR_FBO_URI, ColorTexture, displayResolutionDependentFBOs, POST_MATERIAL_URN, "texBlur"));
        }

        // TODO: take advantage of Texture.subscribeToDisposal(Runnable) to reobtain the asset only if necessary
    }

    /**
     * Execute the final post processing on the rendering of the scene obtained so far.
     *
     * It uses the GBUFFER as input and the FINAL FBO to store its output, rendering
     * everything to a quad.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/finalPostProcessing");

        postMaterial.setFloat("focalDistance", cameraTargetSystem.getFocalDistance(), true); //for use in DOF effect

        // TODO: convert to StateChange
        Texture colorGradingLut = Assets.getTexture("engine:colorGradingLut1").get();
        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        glBindTexture(GL12.GL_TEXTURE_3D, colorGradingLut.getId());
        postMaterial.setInt("texColorGradingLut", 2, true);

        if (isFilmGrainEnabled) {
            // TODO: review - is this loading a noise texture every frame?
            // TODO:          and must it be monitored like a standard texture?
            // TODO: convert to StateChange
            ResourceUrn noiseTextureUri = TextureUtil.getTextureUriForWhiteNoise(1024, 0x1234, 0, 512);
            Texture filmGrainNoiseTexture = Assets.getTexture(noiseTextureUri).get();
            GL13.glActiveTexture(GL13.GL_TEXTURE3);
            glBindTexture(GL11.GL_TEXTURE_2D, filmGrainNoiseTexture.getId());
            postMaterial.setInt("texNoise", 3, true);

            postMaterial.setFloat("grainIntensity", filmGrainIntensity, true);
            postMaterial.setFloat("noiseOffset", rand.nextFloat(), true);

            postMaterial.setFloat2("noiseSize", filmGrainNoiseTexture.getWidth(), filmGrainNoiseTexture.getHeight(), true);
            postMaterial.setFloat2("renderTargetSize", lastUpdatedGBuffer.width(), lastUpdatedGBuffer.height(), true);
        }

        if (isMotionBlurEnabled) {
            postMaterial.setMatrix4("invViewProjMatrix", activeCamera.getInverseViewProjectionMatrix(), true);
            postMaterial.setMatrix4("prevViewProjMatrix", activeCamera.getPrevViewProjectionMatrix(), true);
        }

        renderFullscreenQuad();

        if (!screenGrabber.isNotTakingScreenshot()) {
            screenGrabber.saveScreenshot();
        }

        PerformanceMonitor.endActivity();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        // This method is only called when oldValue != newValue.
        if (event.getPropertyName().equals(RenderingConfig.FILM_GRAIN)) {
            isFilmGrainEnabled = renderingConfig.isFilmGrain();
        } else if (event.getPropertyName().equals(RenderingConfig.MOTION_BLUR)) {
            isMotionBlurEnabled = renderingConfig.isMotionBlur();
        } // else: no other cases are possible - see subscribe operations in initialize().

        // TODO: Do we have to monitor BlurIntensity?
    }
}
