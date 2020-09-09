// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.players;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.Owns;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;

/**
 * Only used by the client side so that held items of other players can be positioned in line with them.
 */
public class RemotePersonHeldItemMountPointComponent implements Component {

    @Owns
    public EntityRef mountPointEntity = EntityRef.NULL;
    public Vector3f rotateDegrees = Vector3f.zero();
    public Vector3f translate = Vector3f.zero();
    public Quat4f rotationQuaternion;
    public float scale = 1f;

}
