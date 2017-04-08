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

import org.terasology.rendering.dag.StateChange;

import java.util.Objects;

import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;

/**
 * Base for classes wanting to affect the OpenGL state via glEnable/glDisable directives.
 *
 * See classes EnableBlending and EnableFaceCulling as working implementations.
 */
abstract class SetStateParameter implements StateChange {
    private int glParameter;
    private boolean enabled;

    /**
     * Construct an instance of this class, provided an OpenGL constant and a boolean to enable or disable
     * the indicated mode.
     *
     * @param glParameter An integer representing one of the many OpenGL constants, i.e. GL_DEPTH_TEST
     * @param enabled A boolean indicating if the mode given by the parameter above must be enabled (true) or disabled (false).
     */
    SetStateParameter(int glParameter, boolean enabled) {
        this.glParameter = glParameter;
        this.enabled = enabled;
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, glParameter);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof SetStateParameter) && this.enabled == ((SetStateParameter) obj).enabled && this.glParameter == ((SetStateParameter) obj).glParameter;
    }

    private String getStatus() {
        return enabled? "Enabled": "Disabled";
    }

    @Override
    public String toString() {
        return String.format("%30s: %s", this.getClass().getSimpleName(), getStatus());
    }

    @Override
    public void process() {
        if (enabled) {
            glEnable(glParameter);
        } else {
            glDisable(glParameter);
        }
    }
}
