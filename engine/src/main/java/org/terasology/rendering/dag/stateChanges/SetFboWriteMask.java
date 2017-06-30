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
package org.terasology.rendering.dag.stateChanges;

import com.google.common.base.Objects;
import org.terasology.rendering.dag.StateChange;
import org.terasology.rendering.opengl.FBO;

/**
 * Sets an FBO's write mask.
 *
 * A write mask is useful to render to an FBO leaving some of its attachments untouched.
 *
 * This particular state change independently enables/disables writing to the color, depth and light accumulation
 * attachments of an FBO. At this stage this functionality makes sense only in the context of the readOnly/writeOnly
 * gBuffers as only those buffers have all the attachments mentioned.
 *
 * The behaviour of this state change in relation to FBOs that do not have all the relevant attachments has not been
 * investigated.
 */
public final class SetFboWriteMask implements StateChange {
    private SetFboWriteMask defaultInstance;

    private FBO fbo;

    private boolean renderToColorBuffer;
    private boolean renderToDepthBuffer;
    private boolean renderToLightBuffer;

    /**
     * The constructor, to be used in the initialise method of a node.
     *
     * Sample use:
     *      addDesiredStateChange(new SetFboWriteMask(true, false, false, "engine:sceneOpaque", displayResolutionDependentFBOManager));
     *
     * @param renderToColorBuffer A boolean indicating whether the Color buffer of the given FBO should be written to.
     * @param renderToDepthBuffer A boolean indicating whether the DepthStencil buffer of the given FBO should be written to.
     * @param renderToLightBuffer A boolean indicating whether the Light Accumulation buffer of the given FBO should be written to.
     * @param fbo
     */
    public SetFboWriteMask(boolean renderToColorBuffer, boolean renderToDepthBuffer, boolean renderToLightBuffer, FBO fbo) {
        this.renderToColorBuffer = renderToColorBuffer;
        this.renderToDepthBuffer = renderToDepthBuffer;
        this.renderToLightBuffer = renderToLightBuffer;

        this.fbo = fbo;
    }

    /**
     * Creates the default instance of this class for the given FBO, resetting all masks to true.
     *
     * @param fbo
     */
    private SetFboWriteMask(FBO fbo) {
        this.renderToColorBuffer = true;
        this.renderToDepthBuffer = true;
        this.renderToLightBuffer = true;

        this.fbo = fbo;

        defaultInstance = this;
    }

    @Override
    public StateChange getDefaultInstance() {
        if (defaultInstance == null)
            defaultInstance = new SetFboWriteMask(fbo);
        return defaultInstance;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fbo.fboId);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof SetFboWriteMask)
                && fbo.fboId == ((SetFboWriteMask) obj).fbo.fboId
                && renderToColorBuffer == ((SetFboWriteMask) obj).renderToColorBuffer
                && renderToDepthBuffer == ((SetFboWriteMask) obj).renderToDepthBuffer
                && renderToLightBuffer == ((SetFboWriteMask) obj).renderToLightBuffer;
    }

    @Override
    public String toString() {
        return String.format("%30s: %s, %b, %b, %b", this.getClass().getSimpleName(), fbo.fboId, renderToColorBuffer, renderToDepthBuffer, renderToLightBuffer);
    }

    @Override
    public void process() {
        fbo.setRenderBufferMask(renderToColorBuffer, renderToDepthBuffer, renderToLightBuffer);
    }
}
