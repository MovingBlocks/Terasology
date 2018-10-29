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
package org.terasology.logic.players;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.Owns;
import org.terasology.entitySystem.entity.EntityRef;
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
