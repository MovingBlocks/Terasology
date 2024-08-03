// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.players;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.terasology.engine.entitySystem.Owns;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.math.TeraMath;

/**
 * Only used by the client side so that held items can be positioned in line with the camera
 */
public class FirstPersonHeldItemMountPointComponent implements Component<FirstPersonHeldItemMountPointComponent> {

    @Owns
    public EntityRef mountPointEntity = EntityRef.NULL;
    public Vector3f rotateDegrees = new Vector3f();
    public Vector3f translate = new Vector3f();
    public Quaternionf rotationQuaternion;
    public float scale = 1f;

    public boolean trackingDataReceived;


    // The hand/tool models seem to have an origin other than the pivot point. This is a best-effort correction,
    // in the form of a 4x4 homogeneous transformation matrix
    public Matrix4f toolAdjustmentMatrix = new Matrix4f(
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, (float) Math.cos(230. * TeraMath.DEG_TO_RAD), (float) Math.sin(230. * TeraMath.DEG_TO_RAD), 0.0f,
            0.0f, (float) -Math.sin(230. * TeraMath.DEG_TO_RAD), (float) Math.cos(230. * TeraMath.DEG_TO_RAD), 0.0f,
            0.0f, -0.05f, -0.2f, 1.0f
    );

    /**
     * Sometimes, a tracking system (i.e. for room-scale VR) provides a pose for this mount point. In this
     * case, special handling is necessary, and this accessor gives a way to check.
     * @return true if this class is receiving tracking updates, false if not.
     */
    public boolean isTracked() {
        return trackingDataReceived;
    }

    @Override
    public void copyFrom(FirstPersonHeldItemMountPointComponent other) {
        this.mountPointEntity = other.mountPointEntity;
        this.rotateDegrees = new Vector3f(other.rotateDegrees);
        this.translate = new Vector3f(other.translate);
        this.rotationQuaternion = new Quaternionf(other.rotationQuaternion);
        this.scale = other.scale;
        this.trackingDataReceived = other.trackingDataReceived;
        this.toolAdjustmentMatrix = new Matrix4f(other.toolAdjustmentMatrix);
    }
}
