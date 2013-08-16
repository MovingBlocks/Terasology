/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.logic.characters;

import com.bulletphysics.linearmath.QuaternionUtil;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Owns;
import org.terasology.math.Direction;
import org.terasology.math.TeraMath;
import org.terasology.network.FieldReplicateType;
import org.terasology.network.Replicate;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 * Information common to characters (the physical body of players and creatures)
 *
 * @author Immortius <immortius@gmail.com>
 */
public final class CharacterComponent implements Component {
    public Vector3f spawnPosition = new Vector3f();
    public float eyeOffset = 0.6f;
    public float interactionRange = 5f;
    public float pitch;
    public float yaw;

    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    @Owns
    public EntityRef movingItem = EntityRef.NULL;

    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    public EntityRef controller = EntityRef.NULL;

    // What inventory slot the character has selected (this currently also determines held item, will need to review based on gameplay)
    public int selectedItem = 0;
    public float handAnimation = 0;

    public Quat4f getLookRotation() {
        Quat4f lookRotation = new Quat4f();
        QuaternionUtil.setEuler(lookRotation, TeraMath.DEG_TO_RAD * yaw, TeraMath.DEG_TO_RAD * pitch, 0);
        return lookRotation;
    }

    public Vector3f getLookDirection() {
        Vector3f result = Direction.FORWARD.getVector3f();
        QuaternionUtil.quatRotate(getLookRotation(), result, result);
        return result;
    }
}
