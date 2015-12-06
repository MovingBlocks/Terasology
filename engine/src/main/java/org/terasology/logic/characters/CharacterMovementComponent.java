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

import org.terasology.entitySystem.Component;
import org.terasology.math.geom.Vector3f;
import org.terasology.network.FieldReplicateType;
import org.terasology.network.Replicate;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.rendering.nui.properties.Range;

import java.util.List;

/**
 */
public final class CharacterMovementComponent implements Component {

    // Collision settings
    @Range(min = 0, max = 5)
    public float height = 1.6f;
    @Range(min = 0, max = 5)
    public float radius = 0.3f;
    public CollisionGroup collisionGroup = StandardCollisionGroup.CHARACTER;
    public List<CollisionGroup> collidesWith = Lists.<CollisionGroup>newArrayList(StandardCollisionGroup.WORLD, StandardCollisionGroup.SENSOR);

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
