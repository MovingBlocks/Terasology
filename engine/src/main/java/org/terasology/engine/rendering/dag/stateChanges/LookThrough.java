// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.dag.stateChanges;

import com.google.common.base.Objects;
import org.terasology.engine.rendering.cameras.Camera;
import org.terasology.engine.rendering.dag.StateChange;

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
@Deprecated
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
