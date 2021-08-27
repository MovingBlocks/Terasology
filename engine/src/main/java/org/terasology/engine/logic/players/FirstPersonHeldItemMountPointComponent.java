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

    private boolean trackingDataReceived;


    // The hand/tool models seem to have an origin other than the pivot point. This is a best-effort correction,
    // in the form of a 4x4 homogeneous transformation matrix
    private Matrix4f toolAdjustmentMatrix = new Matrix4f(
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

    /**
     * A callback target for the controller listener. When this callback triggers, the pos of the mount point will
     * cuange to the value of the pose parameter. This is mainly designed as a callback, and not intended to be part
     * of the public interface of this class.
     * @param pose - the controller pose - a homogenous transformation matrix.
     * @param handIndex - the hand index - 0 for left and 1 for right.
     */
    //TODO: commented out due to a natives issue and VR not working at the moment anyway
    /*
    public void poseChanged(Matrix4f pose, int handIndex) {
        // do nothing for the second controller
        // TODO: put a hand for the second controller.
        if (handIndex != 0) {
            return;
        }
        trackingDataReceived = true;
        Matrix4f adjustedPose = pose.mul(toolAdjustmentMatrix);
        translate = new Vector3f(adjustedPose.m30(), adjustedPose.m31(), adjustedPose.m32());
        org.joml.Vector4f jomlQuaternion = org.terasology.rendering.openvrprovider.OpenVRUtil.convertToQuaternion(adjustedPose);
        if (rotationQuaternion == null) {
            rotationQuaternion = new Quat4f(jomlQuaternion.x, jomlQuaternion.y, jomlQuaternion.z, jomlQuaternion.w);
        } else {
            rotationQuaternion.set(jomlQuaternion.x, jomlQuaternion.y, jomlQuaternion.z, jomlQuaternion.w);
        }
    }
    */
}
