// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.dag.stateChanges;

import com.google.common.base.Objects;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.rendering.opengl.FBO;
import org.terasology.engine.rendering.dag.StateChange;

import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

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
        glBindFramebuffer(GL_FRAMEBUFFER, fboId);
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
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }
    }
}
