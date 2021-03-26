// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.dag;

import org.terasology.engine.config.RenderingDebugConfig;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Instances of this class toggles the wireframe mode ON or OFF
 * in an object implementing the WireframeCapable interface.
 * The toggle is triggered monitoring the Rendering Debug Config.
 */
public class WireframeTrigger implements PropertyChangeListener {

    private final RenderingDebugConfig renderingDebugConfig;
    private final WireframeCapable wireframeCapable;

    /**
     * Constructs and fully initialise an instance of WireframeTrigger.
     *
     * @param renderingDebugConfig The RenderingDebugConfig instance to monitor for wireframe mode changes
     * @param wireframeCapable An instance implementing the WireframeCapable interface
     */
    public WireframeTrigger(RenderingDebugConfig renderingDebugConfig, WireframeCapable wireframeCapable) {

        this.renderingDebugConfig = renderingDebugConfig;
        this.wireframeCapable = wireframeCapable;

        renderingDebugConfig.subscribe(RenderingDebugConfig.WIREFRAME, this);
        if (renderingDebugConfig.isWireframe()) {
            wireframeCapable.enableWireframe();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (renderingDebugConfig.isWireframe()) {
            wireframeCapable.enableWireframe();
        } else {
            wireframeCapable.disableWireframe();
        }
    }

}
