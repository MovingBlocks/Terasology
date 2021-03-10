// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.dag.stateChanges;

import org.terasology.engine.rendering.cameras.Camera;
import org.terasology.engine.rendering.dag.StateChange;

import java.util.Objects;

/**
 * Sets or resets the reflected flag of a given camera.
 *
 * Warning: instances of this class -must- be added to the list of desired state changes in a node
 * -before- any instance of LookThrough and LookThroughNormalized.
 */
public class ReflectedCamera implements StateChange {
    private ReflectedCamera defaultInstance;
    private Camera camera;
    private boolean reflected;

    /**
     * The constructor, to be used in the initialise method of a node.
     *
     * Sample use:
     *      addDesiredStateChange(new ReflectedCamera(activeCamera));
     *
     * @param camera An instance implementing the Camera interface.
     */
    public ReflectedCamera(Camera camera) {
        this(camera, true);
    }

    private ReflectedCamera(Camera camera, boolean reflected) {
        this.camera = camera;
        this.reflected = reflected;

        if (!reflected) {
            this.defaultInstance = this;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(camera, reflected);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof ReflectedCamera) && this.camera == ((ReflectedCamera) obj).camera
                                                && (this.reflected == ((ReflectedCamera) obj).reflected);
    }

    /**
     * Returns an instance of this class configured to generate a task resetting
     * the reflected flag of the camera provided on construction.
     *
     * @return the default instance of ReflectedCamera
     */
    @Override
    public StateChange getDefaultInstance() {
        if (defaultInstance == null) {
            defaultInstance = new ReflectedCamera(camera, false);
        }
        return defaultInstance;
    }

    private String getStatus() {
        return reflected ? "True" : "False";
    }

    @Override
    public String toString() {
        return String.format("%30s: %s for %s", this.getClass().getSimpleName(), getStatus(), camera.toString());
    }

    public void process() {
        camera.setReflected(reflected);
    }
}
