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

import org.terasology.assets.ResourceUrn;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.opengl.BaseFBOsManager;
import org.terasology.rendering.opengl.FBOManagerSubscriber;
import java.util.Objects;
import org.terasology.rendering.dag.StateChange;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;

import static org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs.READONLY_GBUFFER;
import static org.lwjgl.opengl.GL11.glViewport;

/**
 * TODO: Add javadocs
 */
public final class SetViewportToSizeOf implements FBOManagerSubscriber, StateChange {
    private static SetViewportToSizeOf defaultInstance;

    private BaseFBOsManager fboManager;
    private ResourceUrn fboName;
    private int fboWidth;
    private int fboHeight;
    
    @SuppressWarnings("FieldCanBeLocal")
    private FBO fbo;

    public SetViewportToSizeOf(ResourceUrn fboName, BaseFBOsManager frameBuffersManager) {
        this.fboManager = frameBuffersManager;
        this.fboName = fboName;

        update(); // Cheeky way to initialise fbo, fboWidth, fboHeight
        fboManager.subscribe(this);
    }

    @Override
    public StateChange getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = new SetViewportToSizeOf(READONLY_GBUFFER, CoreRegistry.get(DisplayResolutionDependentFBOs.class));
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
        fbo = fboManager.get(fboName);

        fboWidth = fbo.width();
        fboHeight = fbo.height();
    }

    @Override
    public String toString() { // TODO: used for logging purposes at the moment, investigate different methods
        return String.format("%30s: %s (%sx%s)", this.getClass().getSimpleName(), fboName, fboWidth, fboHeight);
    }

    public static void disposeDefaultInstance() {
        defaultInstance = null;
    }

    @Override
    public void process() {
        glViewport(0, 0, fboWidth, fboHeight);
    }
}
