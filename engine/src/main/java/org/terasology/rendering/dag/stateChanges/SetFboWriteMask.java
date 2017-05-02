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

import org.terasology.assets.ResourceUrn;
import org.terasology.rendering.opengl.BaseFBOsManager;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOManagerSubscriber;
import com.google.common.base.Objects;
import org.terasology.rendering.dag.StateChange;

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
public final class SetFboWriteMask implements FBOManagerSubscriber, StateChange {
    private SetFboWriteMask defaultInstance;

    private BaseFBOsManager fboManager;
    private ResourceUrn fboName;
    private FBO fbo;

    private boolean renderToColorBuffer;
    private boolean renderToDepthBuffer;
    private boolean renderToLightBuffer;

    /**
     * Creates an instance of this class with the given parameters.
     *
     * @param renderToColorBuffer A boolean indicating whether the Color buffer of the given FBO should be written to.
     * @param renderToDepthBuffer A boolean indicating whether the DepthStencil buffer of the given FBO should be written to.
     * @param renderToLightBuffer A boolean indicating whether the Light Accumulation buffer of the given FBO should be written to.
     * @param fboName A ResourceUrn identifying the FBO whose render masks have to be modified - usually only the writeOnlyGBuffer FBO.
     * @param fboManager The FBOManager responsible for managing the given FBO.
     */
    public SetFboWriteMask(boolean renderToColorBuffer, boolean renderToDepthBuffer, boolean renderToLightBuffer, ResourceUrn fboName, BaseFBOsManager fboManager) {
        this.renderToColorBuffer = renderToColorBuffer;
        this.renderToDepthBuffer = renderToDepthBuffer;
        this.renderToLightBuffer = renderToLightBuffer;

        this.fboName = fboName;
        this.fboManager = fboManager;

        update(); // Cheeky way to initialise fbo
        fboManager.subscribe(this);
    }

    /**
     * Creates the default instance of this class for the given FBO, resetting all masks to true.
     *
     * @param fboName A ResourceUrn identifying the FBO whose render masks have to be modified - usually only the writeOnlyGBuffer FBO.
     * @param fboManager The FBOManager responsible for managing the given FBO.
     */
    private SetFboWriteMask(ResourceUrn fboName, BaseFBOsManager fboManager) {
        this.renderToColorBuffer = true;
        this.renderToDepthBuffer = true;
        this.renderToLightBuffer = true;

        this.fboName = fboName;
        this.fboManager = fboManager;

        update(); // Cheeky way to initialise fbo
        fboManager.subscribe(this);

        defaultInstance = this;
    }

    @Override
    public StateChange getDefaultInstance() {
        if (defaultInstance == null)
            defaultInstance = new SetFboWriteMask(fboName, fboManager);
        return defaultInstance;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fboName);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof SetFboWriteMask)
                && fboName.equals(((SetFboWriteMask) obj).fboName)
                && renderToColorBuffer == ((SetFboWriteMask) obj).renderToColorBuffer
                && renderToDepthBuffer == ((SetFboWriteMask) obj).renderToDepthBuffer
                && renderToLightBuffer == ((SetFboWriteMask) obj).renderToLightBuffer;
    }

    @Override
    public void update() {
        fbo = fboManager.get(fboName);
    }

    @Override
    public String toString() {
        return String.format("%30s: %s, %b, %b, %b", this.getClass().getSimpleName(), fboName, renderToColorBuffer, renderToDepthBuffer, renderToLightBuffer);
    }

    @Override
    public void process() {
        fbo.setRenderBufferMask(renderToColorBuffer, renderToDepthBuffer, renderToLightBuffer);
    }
}
