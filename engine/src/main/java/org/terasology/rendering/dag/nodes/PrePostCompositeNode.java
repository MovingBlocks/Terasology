/*
 * Copyright 2016 MovingBlocks
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
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.stateChanges.BindFBO;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;
import static org.terasology.rendering.opengl.DefaultDynamicFBOs.WRITE_ONLY_GBUFFER;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import static org.terasology.rendering.opengl.ScalingFactors.FULL_SCALE;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;

/**
 * An instance of this class takes advantage of the content of a number of previously filled buffers
 * to add screen-space ambient occlusion (SSAO), outlines, reflections [1], atmospheric haze and volumetric fog [2]
 *
 * As this node does not quite use 3D geometry and only relies on 2D sources and a 2D output buffer, it
 * could be argued that, despite its name, it represents the first step of the PostProcessing portion
 * of the rendering engine. This line of thinking draws a parallel from the film industry where
 * Post-Processing (or Post-Production) is everything that happens -after- the footage for the film
 * has been shot on stage or on location.
 *
 * [1] And refractions? To be verified.
 * [2] Currently not working: the code is there but it is never enabled.
 */
public class PrePostCompositeNode extends AbstractNode {
    private static final ResourceUrn REFLECTIVE_REFRACTIVE_FBO = new ResourceUrn("engine:fbo.reflectiveRefractive");

    @In
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;

    /**
     * This method must be called once shortly after instantiation to fully initialize the node
     * and make it ready for rendering.
     */
    @Override
    public void initialise() {
        requiresFBO(new FBOConfig(REFLECTIVE_REFRACTIVE_FBO, FULL_SCALE, FBO.Type.HDR).useNormalBuffer(), displayResolutionDependentFBOs);
        addDesiredStateChange(new EnableMaterial("engine:prog.prePostComposite"));
        addDesiredStateChange(new BindFBO(WRITE_ONLY_GBUFFER));
        addDesiredStateChange(new SetViewportToSizeOf(WRITE_ONLY_GBUFFER));

        // TODO: bind input textures from ShaderParametersCombine class
    }

    /**
     * Called every frame, the shader program used by this method only composites per-pixel information from a number
     * of buffers and renders it into a full-screen quad, which is the only piece of geometry processed.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/prePostComposite");

        renderFullscreenQuad();

        // TODO: review - the following line is necessary, but at this stage it's unclear why.
        displayResolutionDependentFBOs.swapReadWriteBuffers();

        // The following line doesn't seem to have an effect: for the time being I keep it here for safety.
        // READ_ONLY_GBUFFER.attachDepthBufferTo(displayResolutionDependentFBOs.get(REFLECTIVE_REFRACTIVE_FBO));

        PerformanceMonitor.endActivity();
    }
}
