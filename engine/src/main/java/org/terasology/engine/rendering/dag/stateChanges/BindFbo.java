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
import org.terasology.engine.SimpleUri;
import org.terasology.rendering.dag.StateChange;
import org.terasology.rendering.opengl.FBO;

import static org.lwjgl.opengl.EXTFramebufferObject.GL_FRAMEBUFFER_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glBindFramebufferEXT;

/**
 * Binds the given FBO, setting it as the FBO to read from and write to.
 *
 * In practice in Terasology this is normally used only to set the FBO to write to. Using an FBO to read from
 * is usually achieved by binding one of its attachments via the SetInputFromFBO state change.
 *
 * When this state change is reset OpenGL's default framebuffer (usually the display) is bound again.
 * Similarly, nodes that do not take advantage of this state change will normally write to the default framebuffer.
 */
public final class BindFbo implements StateChange {
    private static StateChange defaultInstance = new UnbindFbo();

    private int fboId;
    private SimpleUri fboName;

    /**
     * The constructor, to be used in the initialise method of a node.
     *
     * Sample use:
     *      addDesiredStateChange(new BindFbo(fbo));
     */
    public BindFbo(FBO fbo) {
        fboId = fbo.getId();
        fboName = fbo.getName();
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fboId);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof BindFbo) && fboId == ((BindFbo) obj).fboId;
    }

    @Override
    public String toString() { // TODO: used for logging purposes at the moment, investigate different methods
        return String.format("%30s: %s (fboId: %s)", this.getClass().getSimpleName(), fboName, fboId);
    }

    @Override
    public void process() {
        // TODO: change the target argument to GL_DRAW_FRAMEBUFFER when we switch to OpenGL 3.0 and beyond.
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, fboId);
    }

    private static final class UnbindFbo implements StateChange {
        @Override
        public StateChange getDefaultInstance() {
            return this;
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof UnbindFbo);
        }

        @Override
        public int hashCode() {
            return UnbindFbo.class.hashCode();
        }

        @Override
        public String toString() {
            return String.format("%30s", this.getClass().getSimpleName());
        }

        @Override
        public void process() {
            glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
        }
    }
}
