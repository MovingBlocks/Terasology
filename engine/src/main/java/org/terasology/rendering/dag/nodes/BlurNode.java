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
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.dag.ConditionDependentNode;
import org.terasology.rendering.dag.stateChanges.BindFBO;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;
import org.terasology.rendering.opengl.BaseFBOsManager;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.rendering.opengl.FBOManagerSubscriber;

import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;

/**
 * TODO
 */
public class BlurNode extends ConditionDependentNode implements FBOManagerSubscriber {

    private Material blurShader;
    private float blurRadius;
    private String performanceMonitorlabel;

    private BaseFBOsManager fboManager;

    private FBOConfig inputFBOConfig;
    private FBOConfig outputFBOConfig;

    private FBO inputFBO;
    private FBO outputFBO;

    /**
     * Throws a RuntimeException if invoked. Use initialise(...) instead.
     */
    @Override
    public void initialise() {
        throw new RuntimeException("Please do not use initialise(). For this class use initialise(...) instead.");
    }

    /**
     *
     * @param inputConfig
     * @param outputConfig
     * @param anfboManager
     * @param aRadius
     * @param aLabel
     */
    public void initialise(FBOConfig inputConfig, FBOConfig outputConfig, BaseFBOsManager anfboManager, float aRadius, String aLabel) {

        this.inputFBOConfig = inputConfig;
        this.outputFBOConfig = outputConfig;
        this.fboManager = anfboManager;

        requiresFBO(inputFBOConfig, fboManager);
        requiresFBO(outputFBOConfig, fboManager);
        addDesiredStateChange(new BindFBO(outputFBOConfig.getName(), fboManager));
        addDesiredStateChange(new SetViewportToSizeOf(outputFBOConfig.getName(), fboManager));

        update();
        fboManager.subscribe(this);

        setupConditions();

        addDesiredStateChange(new EnableMaterial("engine:prog.blur"));
        this.blurShader = getMaterial(new ResourceUrn("engine:prog.blur"));

        this.blurRadius = aRadius;
        this.performanceMonitorlabel = aLabel;
    }

    protected void setupConditions() {
        // This method should be overridden if inheriting classes wish to trigger the blur in certain conditions
        // Otherwise this method does nothing and the blur always occurr.
    }

    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/" + performanceMonitorlabel);

        blurShader.setFloat("radius", blurRadius, true);
        blurShader.setFloat2("texelSize", 1.0f / outputFBO.width(), 1.0f / outputFBO.height(), true);
        inputFBO.bindTexture();

        renderFullscreenQuad();

        PerformanceMonitor.endActivity();
    }

    public void update() {
        inputFBO = fboManager.get(inputFBOConfig.getName());
        outputFBO = fboManager.get(outputFBOConfig.getName());
    }

}
