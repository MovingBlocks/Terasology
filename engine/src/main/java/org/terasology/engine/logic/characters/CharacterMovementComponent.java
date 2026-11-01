// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters;

import com.google.common.collect.Lists;
import org.joml.Vector3f;
import org.terasology.engine.network.FieldReplicateType;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.physics.CollisionGroup;
import org.terasology.engine.physics.StandardCollisionGroup;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.nui.properties.Range;

import java.util.List;

/**
 * This component is attached to all character entities. It governs movement and stores associated paramenters, and can
 * be used instead of a CapsuleShapeComponent by the physics system to define the collision shape. <br> The {@link
 * AliveCharacterComponent} should necessarily be attached to the character entity for the movement systems to work.
 */
public final class CharacterMovementComponent implements Component<CharacterMovementComponent> {

    // Collision settings
    @Range(min = 1, max = 25)
    public float height = 1.6f;
    @Range(min = 0, max = 5)
    public float radius = 0.3f;
    public CollisionGroup collisionGroup = StandardCollisionGroup.CHARACTER;
    public List<CollisionGroup> collidesWith = Lists.<CollisionGroup>newArrayList(
            StandardCollisionGroup.WORLD,
            StandardCollisionGroup.SENSOR,
            StandardCollisionGroup.CHARACTER
    );
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

    @Override
    public void copyFrom(CharacterMovementComponent other) {
        this.height = other.height;
        this.radius = other.radius;
        this.collisionGroup = other.collisionGroup;
        this.collidesWith = Lists.newArrayList(other.collidesWith);
        this.pickupRadius = other.pickupRadius;
        this.speedMultiplier = other.speedMultiplier;
        this.runFactor = other.runFactor;
        this.jumpSpeed = other.jumpSpeed;
        this.stepHeight = other.stepHeight;
        this.slopeFactor = other.slopeFactor;
        this.baseNumberOfJumpsMax = other.baseNumberOfJumpsMax;
        this.numberOfJumpsMax = other.numberOfJumpsMax;
        this.numberOfJumpsLeft = other.numberOfJumpsLeft;
        this.distanceBetweenFootsteps = other.distanceBetweenFootsteps;
        this.faceMovementDirection = other.faceMovementDirection;
        this.mode = other.mode;
        this.grounded = other.grounded;
        this.jump = other.jump;
        this.footstepDelta = other.footstepDelta;
        this.velocity = new Vector3f(other.velocity);
    }
}
