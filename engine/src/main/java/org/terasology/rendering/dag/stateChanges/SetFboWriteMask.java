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
 * attachments of an FBO. At this stage this functionality makes sense only in the context of the gBuffers,
 * as only those buffers have all the attachments mentioned.
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
     * Creates an instance of this StateChange, that can be added to a Node's list of desired StateChanges.
     *
     * Sample use:
     *      addDesiredStateChange(new SetFboWriteMask(fbo, true, false, false));
     *
     * @param fbo The FBO whose render masks have to be modified - usually only the lastUpdatedGBuffer.
     * @param renderToColorBuffer A boolean indicating whether the Color buffer of the given FBO should be written to.
     * @param renderToDepthBuffer A boolean indicating whether the DepthStencil buffer of the given FBO should be written to.
     * @param renderToLightBuffer A boolean indicating whether the Light Accumulation buffer of the given FBO should be written to.
     */
    public SetFboWriteMask(FBO fbo, boolean renderToColorBuffer, boolean renderToDepthBuffer, boolean renderToLightBuffer) {
        this.fbo = fbo;
        this.renderToColorBuffer = renderToColorBuffer;
        this.renderToDepthBuffer = renderToDepthBuffer;
        this.renderToLightBuffer = renderToLightBuffer;
    }

    /**
     * Creates the default instance of this class for the given FBO, resetting all masks to true.
     *
     * @param fbo The FBO whose render masks have to be modified - usually only the lastUpdatedGBuffer.
     */
    private SetFboWriteMask(FBO fbo) {
        this.fbo = fbo;
        this.renderToColorBuffer = true;
        this.renderToDepthBuffer = true;
        this.renderToLightBuffer = true;

        defaultInstance = this;
    }

    @Override
    public StateChange getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = new SetFboWriteMask(fbo);
        }
        return defaultInstance;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fbo);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof SetFboWriteMask)
                && fbo.equals(((SetFboWriteMask) obj).fbo)
                && renderToColorBuffer == ((SetFboWriteMask) obj).renderToColorBuffer
                && renderToDepthBuffer == ((SetFboWriteMask) obj).renderToDepthBuffer
                && renderToLightBuffer == ((SetFboWriteMask) obj).renderToLightBuffer;
    }

    @Override
    public String toString() {
        return String.format("%30s: %s (fboId: %s), %b, %b, %b", this.getClass().getSimpleName(), fbo.getName(), fbo.getId(), renderToColorBuffer, renderToDepthBuffer, renderToLightBuffer);
    }

    @Override
    public void process() {
        fbo.setRenderBufferMask(renderToColorBuffer, renderToDepthBuffer, renderToLightBuffer);
    }
}
