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

import com.google.common.collect.Lists;

import org.joml.Vector3f;
import org.terasology.entitySystem.Component;
import org.terasology.network.FieldReplicateType;
import org.terasology.network.Replicate;
import org.terasology.nui.properties.Range;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.StandardCollisionGroup;

import java.util.List;

/**
 * This component is attached to all character entities. It governs movement and stores
 * associated paramenters, and can be used instead of a CapsuleShapeComponent by the physics system to define the collision shape. <br/>
 * The {@link AliveCharacterComponent} should necessarily be attached to the character entity for the movement systems to work.
 */
public final class CharacterMovementComponent implements Component {

    // Collision settings
    @Range(min = 1, max = 25)
    public float height = 1.6f;
    @Range(min = 0, max = 5)
    public float radius = 0.3f;
    public CollisionGroup collisionGroup = StandardCollisionGroup.CHARACTER;
    public List<CollisionGroup> collidesWith = Lists.<CollisionGroup>newArrayList(StandardCollisionGroup.WORLD, StandardCollisionGroup.SENSOR);
    @Range(min = 0, max = 5)
    public float pickupRadius = 1.5f;

    // Speed settings
    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    @Range(min = 0, max = 10)
    public float speedMultiplier = 1.0f;
    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    @Range(min = 0, max = 10)
    public float runFactor = 1.5f;
    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    @Range(min = 0, max = 10)
    public float jumpSpeed = 10.0f;

    // Movement settings
    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    @Range(min = 0, max = 1)
    public float stepHeight = 0.35f;
    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    @Range(min = 0, max = 1)
    public float slopeFactor = 0.6f; // Cosine of the maximum slope traversable. 1 is no slope, 0 is any slope

    // Acrobatics settings
    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    public int baseNumberOfJumpsMax = 1; // Base maximum number of jumps allowed starting from solid ground.

    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    public int numberOfJumpsMax = 1; // Maximum number of jumps allowed starting from solid ground.

    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    public int numberOfJumpsLeft = baseNumberOfJumpsMax; // Remaining number of jumps a player can perform.

    public float distanceBetweenFootsteps = 1f;
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
