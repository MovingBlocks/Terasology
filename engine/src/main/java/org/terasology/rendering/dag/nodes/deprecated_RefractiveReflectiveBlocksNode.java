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

import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.rendering.dag.gsoc.NewAbstractNode;
import org.terasology.rendering.nui.properties.Range;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * This node renders refractive/reflective blocks, i.e. water blocks.
 *
 * Reflections always include the sky but may or may not include the landscape,
 * depending on the "Reflections" video setting. Any other object currently
 * reflected is an artifact.
 *
 * Refractions distort the blocks behind the refracting surface, i.e. the bottom
 * of a lake seen from above water or the landscape above water when the player is underwater.
 * Refractions are currently always enabled.
 *
 * Note: a third "Reflections" video setting enables Screen-space Reflections (SSR),
 * an experimental feature. It produces initially appealing reflections but rotating the
 * camera partially spoils the effect showing its limits.
 */
public class deprecated_RefractiveReflectiveBlocksNode extends NewAbstractNode implements PropertyChangeListener {
 /*  public static final SimpleUri REFRACTIVE_REFLECTIVE_FBO_URI = new SimpleUri("engine:fbo.sceneReflectiveRefractive");

    // TODO: rename to more meaningful/precise variable names, like waveAmplitude or waveHeight.
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 2.0f)
    public static float waveIntensity = 2.0f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 2.0f)
    public static float waveIntensityFalloff = 0.85f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 2.0f)
    public static float waveSize = 0.1f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 2.0f)
    public static float waveSizeFalloff = 1.25f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 2.0f)
    public static float waveSpeed = 0.1f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 2.0f)
    public static float waveSpeedFalloff = 0.95f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 5.0f)
    public static float waterOffsetY;
*/
    public deprecated_RefractiveReflectiveBlocksNode(String nodeUri, Context context) {
        super(nodeUri, context);
    }/*

    public void updateWaterAttributes(float waveIntensityModule, float waveIntensityFalloffModule, float waveSizeModule,
                                 float waveSizeFalloffModule, float waveSpeedModule, float waveSpeedFalloffModule, float waterOffsetYModule) {
        waveIntensity = waveIntensityModule;
        waveIntensityFalloff = waveIntensityFalloffModule;
        waveSize = waveSizeModule;
        waveSizeFalloff = waveSizeFalloffModule;
        waveSpeed = waveSpeedModule;
        waveSpeedFalloff = waveSpeedFalloffModule;
        waterOffsetY = waterOffsetYModule;
    }
*/
    @Override
    public void setDependencies(Context context) {

    }

    /**
     * This method is where the actual rendering of refractive/reflective blocks takes place.
     *
     * Also takes advantage of the two methods
     *
     * - WorldRenderer.increaseTrianglesCount(int)
     * - WorldRenderer.increaseNotReadyChunkCount(int)
     *
     * to publish some statistics over its own activity.
     */
    @Override
    public void process() {

    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {

    }
}
