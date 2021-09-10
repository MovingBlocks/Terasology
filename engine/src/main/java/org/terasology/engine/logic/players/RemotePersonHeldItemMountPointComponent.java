// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.players;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.terasology.engine.entitySystem.Owns;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.component.Component;

/**
 * Only used by the client side so that held items of other players can be positioned in line with them.
 */
public class RemotePersonHeldItemMountPointComponent implements Component<RemotePersonHeldItemMountPointComponent> {

    @Owns
    public EntityRef mountPointEntity = EntityRef.NULL;
    public Vector3f rotateDegrees = new Vector3f();
    public Vector3f translate = new Vector3f();
    public Quaternionf rotationQuaternion;
    public float scale = 1f;

    @Override
    public void copyFrom(RemotePersonHeldItemMountPointComponent other) {
        this.mountPointEntity = other.mountPointEntity;
        this.rotateDegrees = new Vector3f(other.rotateDegrees);
        this.translate = new Vector3f(other.translate);
        this.rotationQuaternion = new Quaternionf(other.rotationQuaternion);
        this.scale = other.scale;
    }
}
