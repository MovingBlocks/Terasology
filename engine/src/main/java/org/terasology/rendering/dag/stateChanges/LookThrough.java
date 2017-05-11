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

import com.google.common.base.Objects;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.dag.StateChange;

import static com.google.common.base.Preconditions.checkNotNull;

// TODO: implement bobbing via multiple cameras and different steady/bobbing attachment points
/**
 * Set the ModelView and Projection matrices so that the scene can be rendered from a given camera.
 *
 * Differently from the LookThroughNormalized state change, a normal player camera bobs up and down
 * when the player moves and bobbing is enabled.
 *
 * The default instance of this class resets both matrices to identity matrices, OpenGL's default.
 */
public class LookThrough implements StateChange {
    private static StateChange defaultInstance = new LookThroughDefault();

    private Camera camera;

    /**
     * The constructor, to be used in the initialise method of a node.
     *
     * Sample use:
     *      addDesiredStateChange(new LookThrough(activeCamera));
     *
     * @param camera An instance implementing the Camera interface.
     */
    public LookThrough(Camera camera) {
        this.camera = checkNotNull(camera);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(camera);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof LookThrough) && camera == ((LookThrough) obj).camera;
    }

    /**
     * Returns an instance of this class configured to generate a task resetting the ModelView and
     * Projection matrices back to opengl's default (identity matrices).
     *
     * @return the default instance of the LookThrough class
     */
    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    @Override
    public String toString() {
        return String.format("%30s: %s", this.getClass().getSimpleName(), camera.toString());
    }

    @Override
    public void process() {
        camera.lookThrough();
    }
}
