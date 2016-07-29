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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.terasology.config.Config;
import org.terasology.config.RenderingDebugConfig;
import org.terasology.registry.In;
import org.terasology.rendering.dag.stateChanges.SetWireframe;

/**
 * TODO: Add javadocs
 */
public abstract class WireframeNode extends AbstractNode implements PropertyChangeListener {
    @In
    protected Config config;
    protected RenderingDebugConfig renderingDebugConfig;

    private SetWireframe wireframeStateChange;

    @Override
    public void initialise() {
        wireframeStateChange = new SetWireframe(true);

        renderingDebugConfig = config.getRendering().getDebug();
        renderingDebugConfig.subscribe(RenderingDebugConfig.WIREFRAME, this);

        if (renderingDebugConfig.isWireframe()) {
            addDesiredStateChange(wireframeStateChange);
        }
    }


    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (renderingDebugConfig.isWireframe()) {
            addDesiredStateChange(wireframeStateChange);
        } else {
            removeDesiredStateChange(wireframeStateChange);
        }
        refreshTaskList();
    }


}
