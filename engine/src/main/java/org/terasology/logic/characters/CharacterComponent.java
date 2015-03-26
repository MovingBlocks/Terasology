/*
 * Copyright 2013 MovingBlocks
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

import org.terasology.math.QuaternionUtil;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.Owns;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.Direction;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.network.FieldReplicateType;
import org.terasology.network.Replicate;

/**
 * Information common to characters (the physical body of players and creatures)
 *
 * @author Immortius
 */
public final class CharacterComponent implements Component {
    public Vector3f spawnPosition = new Vector3f();
    public float eyeOffset = 0.6f;
    /**
     * Specifies the maximium range at which this character is able to interact with other objects.
     * When the player leaves the range the interaction gets canceled.
     */
    public float interactionRange = 5f;
    /**
     * The current interaction target of a character which has been authorized by the authority (e.g. the server).
     * <br><br>
     * Modules should not modify this field directly.
     */
    public EntityRef authorizedInteractionTarget = EntityRef.NULL;
    /**
     * This field is only set for clients (including clients that are servers).
     * <br><br>
     * It contains the number of the activationId that caused the interaction start.
     * <br><br>
     * The field is used to tell the client which interaction got canceled. Thus if the client has started another
     * interaction when it receives the old cancel, it won't wrongly cancel the new interaction.
     */
    public int authorizedInteractionId;

    /**
     * This field is only set for clients (including clients that are servers). The clients set it
     * best to their knowledge.
     * <br><br>
     * The events {@link org.terasology.logic.characters.interactions.InteractionStartPredicted} and
     * {@link org.terasology.logic.characters.interactions.InteractionEndPredicted} inform about changes of this
     * field.
     */
    public EntityRef predictedInteractionTarget = EntityRef.NULL;

    /**
     * This field is only set for clients (including clients that are servers).
     * <br><br>
     * It contains the number of the activationId that caused the interaction start.
     * <br><br>
     * The field is used to determine if a incoming interaction cancel is for the current interaction or not.
     */
    public int predictedInteractionId;


    public float pitch;
    public float yaw;

    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    @Owns
    public EntityRef movingItem = EntityRef.NULL;

    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    public EntityRef controller = EntityRef.NULL;

    // What inventory slot the character has selected (this currently also determines held item, will need to review based on gameplay)
    public int selectedItem;
    public float handAnimation;

    public Quat4f getLookRotation() {
        Quat4f lookRotation = new Quat4f(TeraMath.DEG_TO_RAD * yaw, TeraMath.DEG_TO_RAD * pitch, 0);
        return lookRotation;
    }

    public Vector3f getLookDirection() {
        Vector3f result = Direction.FORWARD.getVector3f();
        QuaternionUtil.quatRotate(getLookRotation(), result, result);
        return result;
    }
}
