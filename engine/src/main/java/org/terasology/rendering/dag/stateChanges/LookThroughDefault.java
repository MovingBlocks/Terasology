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

import org.terasology.rendering.dag.StateChange;

import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;

/**
 * This StateChange resets the ModelView and Projection matrices to identity matrices, OpenGL's default.
 *
 * This StateChange is used to reset the effects of LookThrough or LookThroughNormalized.
 */
class LookThroughDefault implements StateChange {
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof LookThroughDefault);
    }

    @Override
    public int hashCode() {
        return LookThroughDefault.class.hashCode();
    }

    @Override
    public StateChange getDefaultInstance() {
        return this;
    }

    @Override
    public String toString() {
        return String.format("%30s", this.getClass().getSimpleName());
    }

    @Override
    public void process() {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
    }
}
