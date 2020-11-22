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
package org.terasology.logic.characters;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.JomlUtil;
import org.terasology.math.TeraMath;
import org.terasology.physics.engine.CharacterCollider;
import org.terasology.physics.engine.PhysicsEngine;

/**
 * Contains logic used by both {@link ClientCharacterPredictionSystem} and {@link ServerCharacterPredictionSystem}.
 */
public final class CharacterMovementSystemUtility {
    private final PhysicsEngine physics;

    public CharacterMovementSystemUtility(PhysicsEngine physicsEngine) {
        this.physics = physicsEngine;
    }


    /**
     * Sets the state of the given entity to the state represented by the
     * CharacterStateEvent. The state of the entity is determined by its
     * LocationComponent (location and orientation of physics body),
     * CharacterMovementComponent (velocity and various movement state
     * variables) and CharacterComponent for pitch and yaw (used for the camera).
     *
     * @param entity
     * @param state
     */
    public void setToState(EntityRef entity, CharacterStateEvent state) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        CharacterMovementComponent movementComp = entity.getComponent(CharacterMovementComponent.class);

        if (location == null || Float.isNaN(location.getWorldPosition().x) || movementComp == null) {
            return;
        }
        location.setWorldPosition(state.getPosition());
        location.setWorldRotation(state.getRotation());
        entity.saveComponent(location);

        movementComp.mode = state.getMode();
        movementComp.setVelocity(JomlUtil.from(state.getVelocity()));
        movementComp.grounded = state.isGrounded();
        movementComp.footstepDelta = state.getFootstepDelta();
        entity.saveComponent(movementComp);

        setPhysicsLocation(entity, JomlUtil.from(state.getPosition()));

        // set the pitch to the character's gaze entity
        Quaternionf rotation = new Quaternionf().rotationX(TeraMath.DEG_TO_RAD * state.getPitch());
        EntityRef gazeEntity = GazeAuthoritySystem.getGazeEntityForCharacter(entity);
        if (!gazeEntity.equals(entity)) {
            // Only set the gaze entity rotation if it is not the same as the main entity.
            // The character is assumed to only rotate side to side, introducing pitch makes things act strangely
            LocationComponent gazeLocation = gazeEntity.getComponent(LocationComponent.class);
            gazeLocation.setLocalRotation(JomlUtil.from(rotation));
            gazeEntity.saveComponent(gazeLocation);
        }
    }

    public void setToInterpolateState(EntityRef entity, CharacterStateEvent a, CharacterStateEvent b, long time) {
        float t = (float) (time - a.getTime()) / (b.getTime() - a.getTime());
        Vector3f newPos = JomlUtil.from(a.getPosition()).lerp(JomlUtil.from(b.getPosition()),t);
        Quaternionf newRot = JomlUtil.from(a.getRotation()).nlerp(JomlUtil.from(b.getRotation()),t);
        LocationComponent location = entity.getComponent(LocationComponent.class);
        location.setWorldPosition(JomlUtil.from(newPos));
        location.setWorldRotation(JomlUtil.from(newRot));
        entity.saveComponent(location);

        CharacterMovementComponent movementComponent = entity.getComponent(CharacterMovementComponent.class);
        movementComponent.mode = a.getMode();
        movementComponent.setVelocity(JomlUtil.from(a.getVelocity()));
        movementComponent.grounded = a.isGrounded();
        if (b.getFootstepDelta() < a.getFootstepDelta()) {
            movementComponent.footstepDelta = t * (1 + b.getFootstepDelta() - a.getFootstepDelta()) + a.getFootstepDelta();
            if (movementComponent.footstepDelta > 1) {
                movementComponent.footstepDelta -= 1;
            }
        } else {
            movementComponent.footstepDelta = t * (b.getFootstepDelta() - a.getFootstepDelta()) + a.getFootstepDelta();
        }
        entity.saveComponent(movementComponent);

        setPhysicsLocation(entity, newPos);
    }

    public void setToExtrapolateState(EntityRef entity, CharacterStateEvent state, long time) {
        float t = (time - state.getTime()) * 0.0001f;
        Vector3f newPos = new Vector3f(JomlUtil.from(state.getVelocity()));
        newPos.mul(t);
        newPos.add(JomlUtil.from(state.getPosition()));
        extrapolateLocationComponent(entity, state, newPos);

        extrapolateCharacterMovementComponent(entity, state);

        setPhysicsLocation(entity, newPos);
    }

    private void extrapolateLocationComponent(EntityRef entity, CharacterStateEvent state, Vector3f newPos) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        location.setWorldPosition(JomlUtil.from(newPos));
        location.setWorldRotation(state.getRotation());
        entity.saveComponent(location);
    }

    private void extrapolateCharacterMovementComponent(EntityRef entity, CharacterStateEvent state) {
        CharacterMovementComponent movementComponent = entity.getComponent(CharacterMovementComponent.class);
        movementComponent.mode = state.getMode();
        movementComponent.setVelocity(JomlUtil.from(state.getVelocity()));
        movementComponent.grounded = state.isGrounded();
        entity.saveComponent(movementComponent);
    }

    /**
     * Sets the location in the physics engine.
     *
     * @param entity The entity to set the location of.
     * @param newPos The new position of the entity.
     */
    private void setPhysicsLocation(EntityRef entity, Vector3f newPos) {
        CharacterCollider collider = physics.getCharacterCollider(entity);
        collider.setLocation(newPos);
    }
}
