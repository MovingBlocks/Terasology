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
import org.terasology.context.Context;
import org.terasology.input.cameraTarget.CameraTargetSystem;
import org.terasology.math.geom.Vector3f;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureUtil;
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.cameras.SubmersibleCamera;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.stateChanges.BindFbo;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.ScreenGrabber;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.Assets;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.WorldProvider;

import static org.lwjgl.opengl.GL11.glBindTexture;
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
public class FinalPostProcessingNode extends AbstractNode {
    private static final ResourceUrn POST_MATERIAL_URN = new ResourceUrn("engine:prog.post");

    private WorldRenderer worldRenderer;
    private ScreenGrabber screenGrabber;

    private EnableMaterial enablePostMaterial;

    private Material postMaterial;

    private Random rand = new FastRandom();

    @Range(min = 0.0f, max = 1.0f)
    private float filmGrainIntensity = 0.05f;

    FBO lastUpdatedGBuffer;

    public FinalPostProcessingNode(Context context) {
        worldRenderer = context.get(WorldRenderer.class);
        screenGrabber = context.get(ScreenGrabber.class);

        postMaterial = getMaterial(POST_MATERIAL_URN);

        enablePostMaterial = new EnableMaterial(POST_MATERIAL_URN);

        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
        FBO finalBuffer = displayResolutionDependentFBOs.get(FINAL_BUFFER);
        addDesiredStateChange(new BindFbo(finalBuffer));
        addDesiredStateChange(new SetViewportToSizeOf(finalBuffer));

        lastUpdatedGBuffer = displayResolutionDependentFBOs.getGBufferPair().getLastUpdatedFbo();
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

        postMaterial.setFloat("viewingDistance", CoreRegistry.get(Config.class).getRendering().getViewDistance().getChunkDistance().x * 8.0f, true);

        // TODO: obtain once in superclass?
        WorldRenderer worldRenderer = CoreRegistry.get(WorldRenderer.class);
        BackdropProvider backdropProvider = CoreRegistry.get(BackdropProvider.class);

        // TODO: move into BaseMaterial?
        if (worldRenderer != null && backdropProvider != null) {
            postMaterial.setFloat("daylight", backdropProvider.getDaylight(), true);
            postMaterial.setFloat("tick", worldRenderer.getMillisecondsSinceRenderingStart(), true);
            postMaterial.setFloat("sunlightValueAtPlayerPos", worldRenderer.getTimeSmoothedMainLightIntensity(), true);

            SubmersibleCamera activeCamera = worldRenderer.getActiveCamera();
            if (activeCamera != null) {
                final Vector3f cameraDir = activeCamera.getViewingDirection();
                final Vector3f cameraPosition = activeCamera.getPosition();

                postMaterial.setFloat("swimming", activeCamera.isUnderWater() ? 1.0f : 0.0f, true);
                postMaterial.setFloat3("cameraPosition", cameraPosition.x, cameraPosition.y, cameraPosition.z, true);
                postMaterial.setFloat3("cameraDirection", cameraDir.x, cameraDir.y, cameraDir.z, true);
                postMaterial.setFloat3("cameraParameters", activeCamera.getzNear(), activeCamera.getzFar(), 0.0f, true);
            }

            Vector3f sunDirection = backdropProvider.getSunDirection(false);
            postMaterial.setFloat3("sunVec", sunDirection.x, sunDirection.y, sunDirection.z, true);
        }

        WorldProvider worldProvider = CoreRegistry.get(WorldProvider.class);
        if (worldProvider != null) {
            postMaterial.setFloat("time", worldProvider.getTime().getDays(), true);
        }

        // TODO: obtain these only once in superclass and monitor from there?
        CameraTargetSystem cameraTargetSystem = CoreRegistry.get(CameraTargetSystem.class);
        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = CoreRegistry.get(DisplayResolutionDependentFBOs.class); // TODO: switch from CoreRegistry to Context.

        // TODO: move into node
        int texId = 0;
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        displayResolutionDependentFBOs.bindFboColorTexture(ToneMappingNode.TONE_MAPPING_FBO_URI);
        postMaterial.setInt("texScene", texId++, true);

        // TODO: monitor property rather than check every frame
        if (CoreRegistry.get(Config.class).getRendering().getBlurIntensity() != 0) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            displayResolutionDependentFBOs.get(LateBlurNode.SECOND_LATE_BLUR_FBO_URI).bindTexture();
            postMaterial.setInt("texBlur", texId++, true);

            if (cameraTargetSystem != null) {
                postMaterial.setFloat("focalDistance", cameraTargetSystem.getFocalDistance(), true); //for use in DOF effect
            }
        }

        // TODO: move to node - obtain only once and then subscribe to it
        // TODO: take advantage of Texture.subscribeToDisposal(Runnable) to reobtain the asset only if necessary
        Texture colorGradingLut = Assets.getTexture("engine:colorGradingLut1").get();

        if (colorGradingLut != null) { // TODO: review need for null check
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            glBindTexture(GL12.GL_TEXTURE_3D, colorGradingLut.getId());
            postMaterial.setInt("texColorGradingLut", texId++, true);
        }

        if (lastUpdatedGBuffer != null) { // TODO: review need for null check
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            lastUpdatedGBuffer.bindDepthTexture();
            postMaterial.setInt("texDepth", texId++, true);

            // TODO: review - is this loading a noise texture every frame? And why is it not in the IF(grain) block?
            // TODO:          and must it be monitored like a standard texture?
            ResourceUrn noiseTextureUri = TextureUtil.getTextureUriForWhiteNoise(1024, 0x1234, 0, 512);
            Texture filmGrainNoiseTexture = Assets.getTexture(noiseTextureUri).get();

            // TODO: monitor property rather than check every frame
            if (CoreRegistry.get(Config.class).getRendering().isFilmGrain()) {
                // TODO: move into node
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                glBindTexture(GL11.GL_TEXTURE_2D, filmGrainNoiseTexture.getId());

                // TODO: move into material?
                postMaterial.setInt("texNoise", texId++, true);
                postMaterial.setFloat("grainIntensity", filmGrainIntensity, true);
                postMaterial.setFloat("noiseOffset", rand.nextFloat(), true);

                postMaterial.setFloat2("noiseSize", filmGrainNoiseTexture.getWidth(), filmGrainNoiseTexture.getHeight(), true);
                postMaterial.setFloat2("renderTargetSize", lastUpdatedGBuffer.width(), lastUpdatedGBuffer.height(), true);
            }
        }

        // TODO: monitor property rather than check every frame
        SubmersibleCamera activeCamera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();
        if (activeCamera != null && CoreRegistry.get(Config.class).getRendering().isMotionBlur()) {
            // TODO: move into material?
            postMaterial.setMatrix4("invViewProjMatrix", activeCamera.getInverseViewProjectionMatrix(), true);
            postMaterial.setMatrix4("prevViewProjMatrix", activeCamera.getPrevViewProjectionMatrix(), true);
        }


        renderFullscreenQuad();

        if (!screenGrabber.isNotTakingScreenshot()) {
            screenGrabber.saveScreenshot();
        }

        PerformanceMonitor.endActivity();
    }
}
