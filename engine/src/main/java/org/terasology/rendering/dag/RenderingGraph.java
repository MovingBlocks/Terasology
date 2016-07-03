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
import org.terasology.config.Config;
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
public class RenderingGraph {
    private final Context context;
    private Map<String, Node> nodeMap;
    private StateManager stateManager;
    private Collection<Node> topologicalOrder;
    private Config config;
    private boolean analyzed;

    public RenderingGraph(Context context) {
        this.context = context;
        this.nodeMap = Maps.newLinkedHashMap(); // insert order is important for now
        this.config = context.get(Config.class);
        this.stateManager = StateManager.createDefault(context);
        this.analyzed = false;
    }

    public static RenderingGraph createDefault(Context context) {
        RenderingGraph pipeline = new RenderingGraph(context);
        pipeline.add("shadowMap", ShadowMapNode.class);
        pipeline.add("worldReflective", WorldReflectionNode.class);
        pipeline.add("backdrop", BackdropNode.class);
        pipeline.add("skyBands", SkyBandsNode.class);
        pipeline.add("objectsOpaque", ObjectsOpaqueNode.class);
        pipeline.add("chunksOpaque", ChunksOpaqueNode.class);
        pipeline.add("chunksAlphaReject", ChunksAlphaRejectNode.class);
        pipeline.add("overlays", OverlaysNode.class);
        pipeline.add("firstPersonView", FirstPersonViewNode.class);
        pipeline.add("lightGeometry", LightGeometryNode.class);
        pipeline.add("directionalLights", DirectionalLightsNode.class);
        pipeline.add("chunksRefractiveReflective", ChunksRefractiveReflectiveNode.class);
        pipeline.add("outline", OutlineNode.class);
        pipeline.add("ambientOcclusionPasses", AmbientOcclusionPassesNode.class);
        pipeline.add("prePostComposite", PrePostCompositeNode.class);
        pipeline.add("simpleBlendMaterials", SimpleBlendMaterialsNode.class);
        pipeline.add("lightShafts", LightShaftsNode.class);
        pipeline.add("initialPostprocessing", InitialPostProcessingNode.class);
        pipeline.add("downSampleSceneAndUpdateExposure", DownSampleSceneAndUpdateExposureNode.class); // TODO: a rename needed :)
        pipeline.add("toneMapping", ToneMappedSceneNode.class);
        pipeline.add("bloomPasses", BloomPassesNode.class);
        pipeline.add("blurPasses", BlurPassesNode.class);
        pipeline.add("finalPostProcessing", FinalPostProcessingNode.class);
        pipeline.analyze();
        return pipeline;
    }

    public void analyze() {
        topologicalOrder = getTopologicalOrder();
        stateManager.findStateChanges(topologicalOrder);
        analyzed = true;
    }

    public void add(AbstractNode node) {
        nodeMap.put(node.getIdentifier(), node);
    }

    public <T extends AbstractNode> void add(String identifier, Class<T> type) {
        // Attempt constructor-based injection first
        context.put(String.class, identifier);
        T node = InjectionHelper.createWithConstructorInjection(type, context);
        // Then fill @In fields
        InjectionHelper.inject(node, context);
        node.initialise();
        this.add(type.cast(node));
    }

    public Node get(String identifier) {
        return nodeMap.get(identifier);
    }

    public void process() throws NotAnalyzedException {
        if (!analyzed) {
            throw new NotAnalyzedException("RenderingPipeline must be analyzed before processing step.");
        }

        for (Node node : this.topologicalOrder) {
            PerformanceMonitor.startActivity("rendering/" + node.getIdentifier());
            stateManager.prepareFor(node);
            node.process();
            PerformanceMonitor.endActivity();
        }
    }

    // TODO: getTopologicalOrder will be a complex task, therefore calling  it multiple times will be expensive
    private Collection<Node> getTopologicalOrder() {
        return nodeMap.values();
    }

}
