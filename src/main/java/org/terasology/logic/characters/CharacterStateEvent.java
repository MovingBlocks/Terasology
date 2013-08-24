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

import com.bulletphysics.linearmath.Transform;
import org.terasology.entitySystem.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.network.BroadcastEvent;
import org.terasology.network.NetworkEvent;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import org.terasology.engine.CoreRegistry;
import org.terasology.physics.bullet.BulletPhysics;
import org.terasology.physics.CharacterCollider;

/**
 * @author Immortius
 */
@BroadcastEvent
public class CharacterStateEvent extends NetworkEvent {
    private long time;
    private int sequenceNumber;
    private Vector3f position = new Vector3f();
    private Quat4f rotation = new Quat4f(0, 0, 0, 1);
    private MovementMode mode = MovementMode.WALKING;
    private boolean grounded;
    private Vector3f velocity = new Vector3f();
    private float yaw;
    private float pitch;
    private float footstepDelta;

    protected CharacterStateEvent() {
    }

    public CharacterStateEvent(CharacterStateEvent previous) {
        this.time = previous.time;
        this.position.set(previous.position);
        this.rotation.set(previous.rotation);
        this.mode = previous.mode;
        this.grounded = previous.grounded;
        this.velocity.set(previous.velocity);
        this.sequenceNumber = previous.sequenceNumber + 1;
        this.pitch = previous.pitch;
        this.yaw = previous.yaw;
        this.footstepDelta = previous.footstepDelta;
    }

    public CharacterStateEvent(
            long time,
            int sequenceNumber,
            Vector3f position,
            Quat4f rotation,
            Vector3f velocity,
            float yaw,
            float pitch,
            MovementMode mode,
            boolean grounded) {
        this.time = time;
        this.position.set(position);
        this.rotation.set(rotation);
        this.velocity.set(velocity);
        this.mode = mode;
        this.grounded = grounded;
        this.sequenceNumber = sequenceNumber;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public long getTime() {
        return time;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Quat4f getRotation() {
        return rotation;
    }

    public MovementMode getMode() {
        return mode;
    }

    public boolean isGrounded() {
        return grounded;
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setMode(MovementMode mode) {
        this.mode = mode;
    }

    public void setGrounded(boolean grounded) {
        this.grounded = grounded;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    /**
     * Retrieve the pitch in degrees.
     * @return 
     */
    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    /**
     * Retrieve the yaw in degrees.
     * @return 
     */
    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getFootstepDelta() {
        return footstepDelta;
    }

    public void setFootstepDelta(float delta) {
        this.footstepDelta = delta;
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
    public static void setToState(EntityRef entity, CharacterStateEvent state) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        CharacterMovementComponent movementComp = entity.getComponent(CharacterMovementComponent.class);
        CharacterComponent characterComponent = entity.getComponent(CharacterComponent.class);
        if (location == null || movementComp == null || characterComponent == null) {
            return;
        }
        location.setWorldPosition(state.getPosition());
        location.setWorldRotation(state.getRotation());
        entity.saveComponent(location);
        movementComp.mode = state.getMode();
        movementComp.setVelocity(state.getVelocity());
        movementComp.grounded = state.isGrounded();
        movementComp.footstepDelta = state.getFootstepDelta();
        entity.saveComponent(movementComp);
        characterComponent.pitch = state.pitch;
        characterComponent.yaw = state.yaw;
        entity.saveComponent(characterComponent);
        setPhysicsLocation(entity, state.getPosition());
    }

    public static void setToInterpolateState(EntityRef entity, CharacterStateEvent a, CharacterStateEvent b, long time) {
        float t = (float) (time - a.getTime()) / (b.getTime() - a.getTime());
        Vector3f newPos = new Vector3f();
        newPos.interpolate(a.getPosition(), b.getPosition(), t);
        Quat4f newRot = new Quat4f();
        newRot.interpolate(a.getRotation(), b.getRotation(), t);
        LocationComponent location = entity.getComponent(LocationComponent.class);
        location.setWorldPosition(newPos);
        location.setWorldRotation(newRot);
        entity.saveComponent(location);

        CharacterMovementComponent movementComponent = entity.getComponent(CharacterMovementComponent.class);
        movementComponent.mode = a.getMode();
        movementComponent.setVelocity(a.getVelocity());
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

        CharacterComponent characterComponent = entity.getComponent(CharacterComponent.class);
        characterComponent.pitch = b.pitch;
        characterComponent.yaw = b.yaw;
        entity.saveComponent(characterComponent);
        setPhysicsLocation(entity, newPos);
    }

    public static void setToExtrapolateState(EntityRef entity, CharacterStateEvent state, long time) {
        float t = (time - state.getTime()) * 0.0001f;
        Vector3f newPos = new Vector3f(state.getVelocity());
        newPos.scale(t);
        newPos.add(state.getPosition());
        LocationComponent location = entity.getComponent(LocationComponent.class);
        location.setWorldPosition(newPos);
        location.setWorldRotation(state.getRotation());
        entity.saveComponent(location);

        CharacterMovementComponent movementComponent = entity.getComponent(CharacterMovementComponent.class);
        movementComponent.mode = state.getMode();
        movementComponent.setVelocity(state.getVelocity());
        movementComponent.grounded = state.isGrounded();
        entity.saveComponent(movementComponent);

        CharacterComponent characterComponent = entity.getComponent(CharacterComponent.class);
        characterComponent.pitch = state.pitch;
        characterComponent.yaw = state.yaw;
        entity.saveComponent(characterComponent);
        setPhysicsLocation(entity, newPos);
    }
    
    /**
     * Sets the location in the physics engine.
     * 
     * @param entity The entity to set the location of.
     * @param newPos The new position of the entity.
     */
    private static void setPhysicsLocation(EntityRef entity, Vector3f newPos) {
        BulletPhysics physics = CoreRegistry.get(BulletPhysics.class);
        CharacterCollider collider = physics.getCharacterCollider(entity);
        collider.setLocation(newPos);
    }
}
