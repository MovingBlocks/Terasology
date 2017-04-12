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

import java.util.Objects;

import org.lwjgl.opengl.GL11;
import org.terasology.rendering.dag.StateChange;

import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_LINE;

/**
 * TODO: Add javadocs
 */
public final class SetWireframe implements StateChange {
    private static final int ENABLED_MODE = GL_LINE;
    private static final int DISABLED_MODE = GL_FILL;

    private static SetWireframe defaultInstance = new SetWireframe(false);

    private boolean enabled;
    private int mode;

    public SetWireframe(boolean enabled) {
        this.enabled = enabled;
        this.mode = enabled? ENABLED_MODE: DISABLED_MODE;
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(enabled);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof SetWireframe) && this.enabled == ((SetWireframe) obj).isEnabled();
    }

    public boolean isEnabled() {
        return enabled;
    }

    private String getStatus() {
        return enabled? "Enabled": "Disabled";
    }

    @Override
    public String toString() { // TODO: used for logging purposes at the moment, investigate different methods
        return String.format("%30s: %s", this.getClass().getSimpleName(), getStatus());
    }

    @Override
    public void process() {
        GL11.glPolygonMode(GL_FRONT_AND_BACK, mode);
    }
}
