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
package org.terasology.rendering.dag;

import org.terasology.config.RenderingDebugConfig;

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
