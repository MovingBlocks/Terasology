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
import org.terasology.rendering.dag.RenderPipelineTask;
import org.terasology.rendering.dag.StateChange;

/**
 * Instances of this class generate task setting and resetting an FBO's write mask.
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
    private ResourceUrn fboName;
    private BaseFBOsManager fboManager;

    private boolean renderToColorBuffer;
    private boolean renderToDepthBuffer;
    private boolean renderToLightBuffer;

    private SetFboWriteMask defaultInstance;
    private SetFboWriteMaskTask task;

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

        defaultInstance = this;
    }

    public ResourceUrn getFboName() {
        return fboName;
    }

    @Override
    public StateChange getDefaultInstance() {
        if (defaultInstance == null)
            defaultInstance = new SetFboWriteMask(fboName, fboManager);
        return defaultInstance;
    }

    @Override
    public RenderPipelineTask generateTask() {
        if (task == null) {
            task = new SetFboWriteMaskTask(fboManager.get(fboName), renderToColorBuffer, renderToDepthBuffer, renderToLightBuffer);
            fboManager.subscribe(this);
        }
        return task;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fboName);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof SetFboWriteMask)
                && fboName.equals(((SetFboWriteMask) obj).getFboName())
                && renderToColorBuffer == ((SetFboWriteMask) obj).renderToColorBuffer
                && renderToDepthBuffer == ((SetFboWriteMask) obj).renderToDepthBuffer
                && renderToLightBuffer == ((SetFboWriteMask) obj).renderToLightBuffer;
    }

    @Override
    public void update() {
        task.setFbo(fboManager.get(fboName));
    }

    @Override
    public String toString() {
        return String.format("%30s: %s, %b, %b, %b", this.getClass().getSimpleName(), fboName, renderToColorBuffer, renderToDepthBuffer, renderToLightBuffer);
    }

    private final class SetFboWriteMaskTask implements RenderPipelineTask {
        private FBO fbo;
        private boolean renderToColorBuffer;
        private boolean renderToDepthBuffer;
        private boolean renderToLightBuffer;

        private SetFboWriteMaskTask(FBO fbo, boolean renderToColorBuffer, boolean renderToDepthBuffer, boolean renderToLightBuffer) {
            this.fbo = fbo;

            this.renderToColorBuffer = renderToColorBuffer;
            this.renderToDepthBuffer = renderToDepthBuffer;
            this.renderToLightBuffer = renderToLightBuffer;
        }

        @Override
        public void execute() {
            fbo.setRenderBufferMask(renderToColorBuffer, renderToDepthBuffer, renderToLightBuffer);
        }

        private void setFbo(FBO fbo) {
            this.fbo = fbo;
        }

        @Override
        public String toString() {
            return String.format("%30s: %s, %b, %b, %b", this.getClass().getSimpleName(), fboName, renderToColorBuffer, renderToDepthBuffer, renderToLightBuffer);
        }
    }
}
