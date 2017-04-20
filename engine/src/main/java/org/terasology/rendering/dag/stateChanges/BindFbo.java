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
import org.terasology.rendering.opengl.FBOManagerSubscriber;
import com.google.common.base.Objects;
import org.terasology.rendering.dag.StateChange;

import static org.lwjgl.opengl.EXTFramebufferObject.GL_FRAMEBUFFER_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glBindFramebufferEXT;

/**
 * Binds the given FBO setting it as the FBO to read from and write to.
 *
 * In practice in Terasology this is normally used only to set the FBO to write to. Using an FBO to read from
 * is usually achieved by binding one of its attachments via the SetInputFromFBO state change.
 *
 * When this state change is reset opengl's default framebuffer (usually the display) is bound again.
 * Similarly, nodes that do not take advantage of this state change will normally write to the default framebuffer.
 */
public final class BindFbo implements FBOManagerSubscriber, StateChange {
    private static StateChange defaultInstance = new UnbindFbo();

    private ResourceUrn fboName;
    private BaseFBOsManager fboManager;
    private int fboId;

    public BindFbo(ResourceUrn fboName, BaseFBOsManager fboManager) {
        this.fboName = fboName;
        this.fboManager = fboManager;

        update(); // Cheeky way to initialise fboId
        fboManager.subscribe(this);
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fboName);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof BindFbo) && fboName.equals(((BindFbo) obj).fboName);
    }

    @Override
    public void update() {
        fboId = fboManager.get(fboName).fboId;
    }

    @Override
    public String toString() { // TODO: used for logging purposes at the moment, investigate different methods
        return String.format("%30s: %s (fboId:%s)", this.getClass().getSimpleName(), fboName, fboId);
    }

    @Override
    public void process() {
        // TODO: change the target argument to GL_DRAW_FRAMEBUFFER when we switch to OpenGL 3.0 and beyond.
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, fboId);
    }
}
