// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.dag.nodes;

import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.naming.Name;
import org.terasology.nui.properties.Range;
import org.terasology.rendering.dag.AbstractNode;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * TODO this class is a temporary solution for propagating BasicRendering module's RefractiveReflectiveBlocksNode's values
 * TODO As more rendering related things move to the basic rendering module, this class should be addressed too and removed
 * This node proxy is read by RenderingHelper when calulating camera in water. It's water related attributes must be set
 * by a corresponding node in BasicRendering module named RefractiveReflectiveBlocksNode.
 */
public class RefractiveReflectiveBlocksNodeProxy extends AbstractNode implements PropertyChangeListener {
   public static final SimpleUri REFRACTIVE_REFLECTIVE_FBO_URI = new SimpleUri("engine:fbo.sceneReflectiveRefractive");

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

    public RefractiveReflectiveBlocksNodeProxy(String nodeUri, Context context) {
        super(nodeUri,new Name("engine"), context);
    }

    public static void updateWaterAttributes(float waveIntensityModule, float waveIntensityFalloffModule, float waveSizeModule,
                                 float waveSizeFalloffModule, float waveSpeedModule, float waveSpeedFalloffModule, float waterOffsetYModule) {
        waveIntensity = waveIntensityModule;
        waveIntensityFalloff = waveIntensityFalloffModule;
        waveSize = waveSizeModule;
        waveSizeFalloff = waveSizeFalloffModule;
        waveSpeed = waveSpeedModule;
        waveSpeedFalloff = waveSpeedFalloffModule;
        waterOffsetY = waterOffsetYModule;
    }

    @Override
    public void setDependencies(Context context) {

    }

    @Override
    public void process() {

    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {

    }
}
