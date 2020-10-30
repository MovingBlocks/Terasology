/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.logic.players;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.Owns;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.TeraMath;

/**
 * Only used by the client side so that held items can be positioned in line with the camera
 */
public class FirstPersonHeldItemMountPointComponent implements Component {

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
