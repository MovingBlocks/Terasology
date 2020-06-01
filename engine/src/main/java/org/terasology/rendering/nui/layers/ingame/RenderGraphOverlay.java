/*
 * Copyright 2020 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.layers.ingame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.world.WorldRenderer;

/**
 * Displays the content of the WorldRenderer renderGraph.
 */
public class RenderGraphOverlay extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:renderGraphOverlay");

    private static final Logger logger = LoggerFactory.getLogger(RenderGraphOverlay.class);

    @In
    private WorldRenderer worldRenderer;

    @Override
    public void initialise() {
        WidgetUtil.trySubscribe(this, "close", widget -> getManager().closeScreen(RenderGraphOverlay.this));
        find("graph", DAGGraphRenderer.class).setRenderGraph(worldRenderer.getRenderGraph());
    }

    @Override
    public boolean isModal() {
        return false;
    }

    @Override
    protected boolean isEscapeToCloseAllowed() {
        return true;
    }
}
