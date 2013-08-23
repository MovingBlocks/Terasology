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

import com.bulletphysics.collision.dispatch.PairCachingGhostObject;
import com.google.common.collect.Lists;
import org.terasology.entitySystem.Component;
import org.terasology.network.FieldReplicateType;
import org.terasology.network.Replicate;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.StandardCollisionGroup;

import javax.vecmath.Vector3f;
import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
public final class CharacterMovementComponent implements Component {

    // Collision settings
    public float height = 1.6f;
    public float radius = 0.3f;
    public CollisionGroup collisionGroup = StandardCollisionGroup.CHARACTER;
    public List<CollisionGroup> collidesWith = Lists.<CollisionGroup>newArrayList(StandardCollisionGroup.WORLD, StandardCollisionGroup.SENSOR);

    // Speed settings
    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    public float maxGroundSpeed = 5.0f;
    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    public float maxWaterSpeed = 2.0f;
    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    public float maxGhostSpeed = 5.0f;
    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    public float runFactor = 1.5f;
    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    public float jumpSpeed = 10.0f;
    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    public float maxClimbSpeed = 3.0f;

    // Movement settings
    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    public float stepHeight = 0.35f;
    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    public float slopeFactor = 0.6f; // Cosine of the maximum slope traversable. 1 is no slope, 0 is any slope

    // Determines how easily the play can change direction
    // TODO: Separate player agiliy from environmental friction, and ground from air control
    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    public float groundFriction = 8.0f;
    public float distanceBetweenFootsteps = 1f;
    public float distanceBetweenSwimStrokes = 1f;
    public boolean faceMovementDirection;

    // Current movement mode
    public MovementMode mode = MovementMode.WALKING;
    public boolean grounded;

    // Movement inputs - desired direction, etc
    public boolean jump;

    // Distance since last footstep
    public float footstepDelta;


    private Vector3f velocity = new Vector3f();
    public Vector3f getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector3f newVelocity) {
        velocity.set(newVelocity);
    }

}
