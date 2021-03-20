// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.dag.stateChanges;

import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.rendering.opengl.FBO;
import org.terasology.engine.rendering.opengl.fbms.DisplayResolutionDependentFbo;
import org.terasology.engine.rendering.dag.StateChange;

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
