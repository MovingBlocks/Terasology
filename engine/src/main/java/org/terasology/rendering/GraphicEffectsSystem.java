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
package org.terasology.rendering;


import org.terasology.assets.ResourceUrn;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.module.sandbox.API;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.rendering.dag.RenderGraph;
import org.terasology.rendering.dag.nodes.ShadowMapNode;
import org.terasology.rendering.shader.ShaderParameters;
import org.terasology.rendering.world.WorldRenderer;

/**
 * TODO: Add javadocs
 */

@API
@RegisterSystem
@Share(GraphicEffectsSystem.class)
public class GraphicEffectsSystem extends BaseComponentSystem {

    @In
    private WorldRenderer worldRenderer;

    @In
    private ShaderManager shaderManager;

    public void setRenderGraph(RenderGraph renderGraph) {
        worldRenderer.setRenderGraph(renderGraph);
    }

    public void setShadowNode(ShadowMapNode shadowNode) {
        worldRenderer.setShadowMapNode(shadowNode);
    }

    public void clear() {
        worldRenderer.clear();
    }

    public void loadShader(ResourceUrn shaderName, ShaderParameters shaderParameters) {
        shaderManager.loadShader(shaderName, shaderParameters);
    }
}
