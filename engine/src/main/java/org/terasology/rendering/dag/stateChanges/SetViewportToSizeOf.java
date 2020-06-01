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
package org.terasology.rendering.dag.stateChanges;

import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.dag.StateChange;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFbo;

import java.util.Objects;

import static org.lwjgl.opengl.GL11.glViewport;

/**
 * TODO: Add javadocs
 */
public final class SetViewportToSizeOf implements StateChange {
    private static SetViewportToSizeOf defaultInstance;

    private FBO fbo;

    /**
     * The constructor, to be used in the initialise method of a node.
     *
     * Sample use:
     *      addDesiredStateChange(new SetViewportToSizeOf(fbo);
     *
     * @param fbo The FBO whose dimensions the viewport will be resized to.
     */
    public SetViewportToSizeOf(FBO fbo) {
        this.fbo = fbo;
    }

    @Override
    public StateChange getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = new SetViewportToSizeOf(CoreRegistry.get(DisplayResolutionDependentFbo.class).getGBufferPair().getLastUpdatedFbo());
        }
        return defaultInstance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fbo.width(), fbo.height());
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof SetViewportToSizeOf) && (this.fbo.width() == ((SetViewportToSizeOf) obj).fbo.width())
                                                    && (this.fbo.height() == ((SetViewportToSizeOf) obj).fbo.height());
    }

    @Override
    public String toString() { // TODO: used for logging purposes at the moment, investigate different methods
        return String.format("%30s: %s (fboId: %s) (%sx%s)", this.getClass().getSimpleName(), fbo.getName(), fbo.getId(), fbo.width(), fbo.height());
    }

    public static void disposeDefaultInstance() {
        defaultInstance = null;
    }

    @Override
    public void process() {
        glViewport(0, 0, fbo.width(), fbo.height());
    }
}
