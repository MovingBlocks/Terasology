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

    public SetViewportToSizeOf(ResourceUrn fboName, BaseFBOsManager frameBuffersManager) {
        this.fboManager = frameBuffersManager;
        this.fboName = fboName;

        // Cheeky way to initialise fboWidth, fboHeight
        update();
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
        return Objects.hash(getFbo().width(), getFbo().height());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SetViewportToSizeOf))
            return false;

        SetViewportToSizeOf other = (SetViewportToSizeOf) obj;

        FBO fbo = getFbo();
        FBO otherFbo = other.getFbo();

        return fbo.width() == otherFbo.width() && fbo.height() == otherFbo.height();
    }

    @Override
    public void update() {
        FBO fbo = getFbo();

        fboWidth = fbo.width();
        fboHeight = fbo.height();
    }

    @Override
    public String toString() { // TODO: used for logging purposes at the moment, investigate different methods
        return String.format("%30s: %s (%sx%s)", this.getClass().getSimpleName(), fboName, fboWidth, fboHeight);
    }

    private FBO getFbo() {
        return fboManager.get(fboName);
    }

    public static void disposeDefaultInstance() {
        defaultInstance = null;
    }

    @Override
    public void process() {
        glViewport(0, 0, fboWidth, fboHeight);
    }
}
