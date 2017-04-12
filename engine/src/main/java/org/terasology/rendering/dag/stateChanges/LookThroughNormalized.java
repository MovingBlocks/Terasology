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

import com.google.common.base.Objects;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.dag.StateChange;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;

// TODO: implement bobbing via multiple cameras and different steady/bobbing attachment points
/**
 * Instances of this class set the ModelView and Projection matrices
 * so that the scene can be rendered from a given camera.
 *
 * Differently from the LookThrough state change, a normalized camera does not bob up and down
 * when the player moves and bobbing is enabled.
 *
 * The default instance of this class resets both matrices to identity matrices, opengl's default.
 */
public class LookThroughNormalized implements StateChange {
    private static LookThroughNormalized defaultInstance = new LookThroughNormalized();

    private Camera camera;

    /**
     * Constructs an instance of this class initialised with the given camera.
     *
     * @param camera An instance implementing the Camera interface.
     */
    public LookThroughNormalized(Camera camera) {
        this.camera = checkNotNull(camera);
    }

    private LookThroughNormalized() {
        this.camera = null;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(camera);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof LookThroughNormalized) && camera == ((LookThroughNormalized) obj).camera;
    }

    /**
     * Returns an instance of this class configured to generate a task resetting the ModelView and
     * Projection matrices back to opengl's default (identity matrices).
     *
     * @return the default instance of the LookThroughNormalized class
     */
    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    @Override
    public String toString() {
        if (this.isTheDefaultInstance()) {
            return String.format("%30s: %s", this.getClass().getSimpleName(), "default opengl camera");
        } else {
            return String.format("%30s: %s", this.getClass().getSimpleName(), camera.toString());
        }
    }

    @Override
    public void process() {
        if (camera == null) {
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();

            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();
        } else {
            camera.lookThroughNormalized();
        }
    }
}
