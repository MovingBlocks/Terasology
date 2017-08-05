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
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.SwappableFBO;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;

public class DebugNode extends AbstractNode {
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;
    private FBO lastUpdatedGBuffer;
    private FBO staleGBuffer;

    private CopyFboColorAttachmentToScreenNode outputNode;

    public DebugNode(Context context) {
    	displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
        SwappableFBO gBufferPair = displayResolutionDependentFBOs.getGBufferPair();

        lastUpdatedGBuffer = gBufferPair.getLastUpdatedFbo();
        staleGBuffer = gBufferPair.getStaleFbo();

    }

    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/debugNode");
        PerformanceMonitor.endActivity();
    }

    public void setOutputNode(CopyFboColorAttachmentToScreenNode outputNode) {
    	this.outputNode = outputNode;
    }

    public void setFbo(String fboUri) {
    	FBO fbo;

        switch (fboUri) {
            case "engine:fbo.gBuffer":
            case "engine:fbo.lastUpdatedGBuffer":
                fbo = lastUpdatedGBuffer;
                break;
            case "engine:fbo.staleGBuffer":
                fbo = staleGBuffer;
                break;
            default:
                // TODO: We should probably do some more error checking here.
                fbo = displayResolutionDependentFBOs.get(new SimpleUri(fboUri));
                break;
        }

        outputNode.setFbo(fbo);
    }
}
