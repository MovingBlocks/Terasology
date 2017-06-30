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
import org.terasology.rendering.opengl.BaseFBOsManager;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOManagerSubscriber;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;

import java.util.Objects;

import static org.lwjgl.opengl.GL11.glViewport;

/**
 * TODO: Add javadocs
 */
public final class SetViewportToSizeOf implements FBOManagerSubscriber, StateChange {
    private static SetViewportToSizeOf defaultInstance;

    private FBO fbo;
    private int fboWidth;
    private int fboHeight;

    /**
     * The constructor, to be used in the initialise method of a node.
     *
     * Sample use:
     *      addDesiredStateChange(new SetViewportToSizeOf("engine:sceneOpaque", displayResolutionDependentFboManager);
     *
     * @param fbo
     * @param fboManager the BaseFBOsManager instance that will send change notifications via the update() method of this class.
     */
    public SetViewportToSizeOf(FBO fbo, BaseFBOsManager fboManager) {
        this.fbo = fbo;

        update(); // Cheeky way to initialise fboWidth, fboHeight
        fboManager.subscribe(this);
    }

    @Override
    public StateChange getDefaultInstance() {
        if (defaultInstance == null) {
            DisplayResolutionDependentFBOs displayResolutionDependentFBOs = CoreRegistry.get(DisplayResolutionDependentFBOs.class);
            defaultInstance = new SetViewportToSizeOf(displayResolutionDependentFBOs.getPrimaryBuffer(), displayResolutionDependentFBOs);
        }
        return defaultInstance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fboWidth, fboHeight);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof SetViewportToSizeOf) && (this.fboWidth == ((SetViewportToSizeOf) obj).fboWidth)
                                                    && (this.fboHeight == ((SetViewportToSizeOf) obj).fboHeight);
    }

    @Override
    public void update() {
        fboWidth = fbo.width();
        fboHeight = fbo.height();
    }

    @Override
    public String toString() { // TODO: used for logging purposes at the moment, investigate different methods
        return String.format("%30s: fboId %s (%sx%s)", this.getClass().getSimpleName(), fbo.fboId, fboWidth, fboHeight);
    }

    public static void disposeDefaultInstance() {
        defaultInstance = null;
    }

    @Override
    public void process() {
        glViewport(0, 0, fboWidth, fboHeight);
    }
}
