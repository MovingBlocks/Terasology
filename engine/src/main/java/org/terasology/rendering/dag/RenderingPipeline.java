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

import com.google.common.collect.Maps;
import org.terasology.context.Context;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.InjectionHelper;
import org.terasology.rendering.dag.nodes.AmbientOcclusionPassesNode;
import org.terasology.rendering.dag.nodes.BackdropNode;
import org.terasology.rendering.dag.nodes.BloomPassesNode;
import org.terasology.rendering.dag.nodes.BlurPassesNode;
import org.terasology.rendering.dag.nodes.ChunksAlphaRejectNode;
import org.terasology.rendering.dag.nodes.ChunksOpaqueNode;
import org.terasology.rendering.dag.nodes.ChunksRefractiveReflectiveNode;
import org.terasology.rendering.dag.nodes.DirectionalLightsNode;
import org.terasology.rendering.dag.nodes.DownSampleSceneAndUpdateExposureNode;
import org.terasology.rendering.dag.nodes.FinalPostProcessingNode;
import org.terasology.rendering.dag.nodes.FirstPersonViewNode;
import org.terasology.rendering.dag.nodes.InitialPostProcessingNode;
import org.terasology.rendering.dag.nodes.LightGeometryNode;
import org.terasology.rendering.dag.nodes.LightShaftsNode;
import org.terasology.rendering.dag.nodes.ObjectsOpaqueNode;
import org.terasology.rendering.dag.nodes.OutlineNode;
import org.terasology.rendering.dag.nodes.OverlaysNode;
import org.terasology.rendering.dag.nodes.PrePostCompositeNode;
import org.terasology.rendering.dag.nodes.ShadowMapNode;
import org.terasology.rendering.dag.nodes.SimpleBlendMaterialsNode;
import org.terasology.rendering.dag.nodes.SkyBandsNode;
import org.terasology.rendering.dag.nodes.ToneMappedSceneNode;
import org.terasology.rendering.dag.nodes.WorldReflectionNode;

import java.util.Collection;
import java.util.Map;

/**
 * TODO: Add javadocs
 */
public class RenderingPipeline {
    private Context context;
    private Map<String, Node> nodeMap;

    public RenderingPipeline(Context context) {
        this.context = context;
        this.nodeMap = Maps.newLinkedHashMap();
    }

    public static RenderingPipeline createDefault(Context context) {
        RenderingPipeline pipeline = new RenderingPipeline(context);
        pipeline.add("shadowmap", ShadowMapNode.class);
        pipeline.add("worldreflective", WorldReflectionNode.class);
        pipeline.add("backdrop", BackdropNode.class);
        pipeline.add("skybands", SkyBandsNode.class);
        pipeline.add("objectsopaque", ObjectsOpaqueNode.class);
        pipeline.add("chunksopaque", ChunksOpaqueNode.class);
        pipeline.add("chunksalphareject", ChunksAlphaRejectNode.class);
        pipeline.add("overlays", OverlaysNode.class);
        pipeline.add("firstpersonview", FirstPersonViewNode.class);
        pipeline.add("lightgeometry", LightGeometryNode.class);
        pipeline.add("directionallights", DirectionalLightsNode.class);
        pipeline.add("chunksrefractivereflective", ChunksRefractiveReflectiveNode.class);
        pipeline.add("outline", OutlineNode.class);
        pipeline.add("ambientocclusionpasses", AmbientOcclusionPassesNode.class);
        pipeline.add("prepostcomposite", PrePostCompositeNode.class);
        pipeline.add("simpleblendmaterials", SimpleBlendMaterialsNode.class);
        pipeline.add("lightshafts", LightShaftsNode.class);
        pipeline.add("initialpostprocessing", InitialPostProcessingNode.class);
        pipeline.add("downsamplesceneandupdateexposure", DownSampleSceneAndUpdateExposureNode.class); // TODO: a rename needed :)
        pipeline.add("tonemappedscene", ToneMappedSceneNode.class);
        pipeline.add("bloompasses", BloomPassesNode.class);
        pipeline.add("blurpasses", BlurPassesNode.class);
        pipeline.add("finalpostprocessing", FinalPostProcessingNode.class);
        return pipeline;
    }

    public void add(Node node) {
        nodeMap.put(node.getIdentifier(), node);
    }


    public <T extends Node> void add(String identifier, Class<T> type) {
        // Attempt constructor-based injection first
        T node = InjectionHelper.createWithConstructorInjection(type, context);
        // Then fill @In fields
        InjectionHelper.inject(node, context);
        node.initialise(identifier);
        this.add(type.cast(node));
    }

    public Node get(String identifier) {
        return nodeMap.get(identifier);
    }

    public void process() {
        for (Node node : getTopologicalOrder()) {
            PerformanceMonitor.startActivity("rendering/" + node.getIdentifier());
            node.process();
            PerformanceMonitor.endActivity();
        }
    }

    public Collection<Node> getTopologicalOrder() {
        return nodeMap.values();
    }

}
