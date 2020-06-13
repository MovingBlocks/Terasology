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

package org.terasology.physics.engine;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.geom.Vector3f;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.Physics;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * The physics engine provides access to physics functionality like ray tracing.
 * <br><br>
 * TODO: Move physics methods that should only be used by the engine/physics implementing system into another interface that inherits this.
 *
 */
public interface PhysicsEngine extends Physics {

    /**
     * Wakes up any rigid bodies that are in a box around the given position.
     *
     * @param pos    The position around which to wake up objects.
     * @param radius the half-length of the sides of the square.
     */
    void awakenArea(Vector3f pos, float radius);

    /**
     * Combines the flags of the given collision groups into a single flag.
     *
     * @param groups
     * @return A single flag representing all the given groups.
     */
    short combineGroups(CollisionGroup... groups);

    /**
     * Combines the flags of the given collision groups into a single flag.
     *
     * @param groups
     * @return A single flag representing all the given groups.
     */
    short combineGroups(Iterable<CollisionGroup> groups);

    /**
     * Disposes this physics engine. Afterwards this physics engine cannot be
     * used anymore.
     */
    void dispose();

    /**
     * Return a list with all CollisionPairs created since the last call to this
     * method. A collisionPair is created when a Trigger hits an other object.
     * Therefore, one of the entities in the CollisionPair should have a Trigger
     * attached to it.
     *
     * @return A newly allocated list with all pairs of entities that collided.
     */
    List<PhysicsSystem.CollisionPair> getCollisionPairs();

    /**
     * The epsilon value is the value that is considered to be so small that it
     * could just as well be zero. Objects that are closer together than this
     * value are assumes to be colliding.
     *
     * @return The simulation epsilon.
     */
    float getEpsilon();

    /**
     * @return A set with all entities that have a rigidBody that is active in
     *         the physics engine. A new set is created that is not backed by this class.
     */
    Set<EntityRef> getPhysicsEntities();

    /**
     * Get the character collider for the given entity. Will create a new
     * CharacterCollider if non exists and return that one.
     *
     * @param entity
     * @return true if the given entity has a CharacterCollider associated to it.
     */
    CharacterCollider getCharacterCollider(EntityRef entity);

    /**
     * Returns the rigid body associated with the given entity. If no such
     * RigidBody exists, a new one is created and returned.
     * <br><br>
     * Note that you should not wait with calling this method until using the
     * rigid body. As soon as the rigid body should exist in the physics engine,
     * this method should be called to create the rigid body.
     *
     * @param entity the entity to retrieve the rigid body of.
     * @return A valid RigidBody instance.
     * @throws IllegalArgumentException if there is no RigidBody in this
     *                                  PhysicsEngine for the given entity and one cannot be created because the
     *                                  given entity does not have a LocationComponent, RigidBodyComponent and
     *                                  ShapeComponent.
     */
    RigidBody getRigidBody(EntityRef entity);

    /**
     * @param entity
     * @return Returns true if there is a rigidBody in the physics engine
     *         related to the given entity, false otherwise.
     */
    boolean hasRigidBody(EntityRef entity);

    /**
     * Checks if the given entity has a trigger attached to it.
     *
     * @param entity the entity to check for.
     * @return true if the entity has a trigger, false otherwise.
     */
    boolean hasTrigger(EntityRef entity);

    /**
     * @param entity the entity to check for
     * @return true if the physics engine has a character collider for the given
     *         entity, false otherwise.
     */
    boolean hasCharacterCollider(EntityRef entity);

    /**
     * Warning: Do not remove physics entities while iterating with the returned iterator. <br>
     * You may create a list of entities to remove and remove them afterwards)
     * <br><br>
     * This method is more efficient than getPhysicsEntities().
     *
     * @return An iterator that iterates over all entities that have a rigidBody
     *         and are active in the physics engine.
     */
    Iterator<EntityRef> physicsEntitiesIterator();

    /**
     * Removes the CharacterCollider associated with the given entity from the
     * physics engine. The collider object of this entity will no longer be
     * valid.
     * <br><br>
     * If no CharacterCollider was attached to the entity, a warning is logged
     * and this method return false.
     * <br><br>
     * Make sure not to make another call to getCharacterCollider() if you are
     * destroying the entity, as this will create a new CharacterCollider for
     * the entity.
     *
     * @param entity the entity to remove the rigid body of.
     * @return true if this entity had a character collider attached to it,
     *         false otherwise.
     */
    boolean removeCharacterCollider(EntityRef entity);

    /**
     * Removes the rigid body associated with the given entity from the physics
     * engine. The RigidBody object returned by the newRigidBody(EntityRef) or
     * getRigidBody(EntityRef) method will no longer be valid for this entity un
     * till newRigidBody is called again, so be careful!
     * <br><br>
     * If no rigid body was attached to the entity, a warning is logged and this
     * method return false.
     * <br><br>
     * Make sure not to make another call to getRigidBody() if you are
     * destroying the entity, as this will create a new RigidBody for
     * the entity.
     *
     * @param entity the entity to remove the rigid body of.
     * @return true if this entity had a rigid body attached to it, false
     *         otherwise.
     */
    boolean removeRigidBody(EntityRef entity);

    /**
     * Removes the trigger associated with the given entity from the physics
     * engine.
     * <br><br>
     * If no trigger was attached to the entity, a warning is logged and this
     * method return false.
     * <br><br>
     * Make sure not to make another call to updateTrigger() if you are
     * destroying the entity, as this will create a new trigger for
     * the entity.
     *
     * @param entity the entity to remove the rigid body of.
     * @return true if this entity had a trigger attached to it, false
     *         otherwise.
     */
    boolean removeTrigger(EntityRef entity);

    /**
     * Advances the physics engine with the given amount of time in seconds. As
     * long as this time does not exceed 8/60 seconds, the game speed will be
     * constant.
     *
     * @param delta amount of time to advance the engine in seconds.
     */
    void update(float delta);

    /**
     * Updates the shape and settings of the rigidBody belonging to the given
     * entity. If the given entity had no rigidBody in the physics engine, it
     * will be created. Updating an entity without RigidBody is seen as bad
     * practise and hence a warning is logged.
     * <br><br>
     * This method also updates the position of the rigid body in line with
     * the location of the entity. If this method is not called, then the
     * position of the rigid body would be overwritten by the next physics
     * update.
     *
     * @param entity the entity of which the rigidBody needs updating.
     * @return true if there was already a rigidBody registered for the entity
     *         (which is now updated), false otherwise.
     */
    boolean updateRigidBody(EntityRef entity);

    /**
     * Updates or creates the trigger of the given object. The entity must have
     * a TriggerComponent, LocationComponent and ShapeComponent to have a
     * trigger. When updating an existing trigger the location and scale are
     * updated.
     * <br><br>
     * An entity with a trigger attached to it will generate collision pairs
     * when it collides or intersects with other objects. A good example of its
     * usage is picking up items. By creating a trigger for a player, a
     * collision event will be generated when the shape of the player collides
     * with the shape of a dropped item. This event is not send by the
     * PhysicsEngine class. Instead it is stored and can be retrieved by the
     * getCollisionPairs() method.
     *
     * @param entity the entity of which the trigger may need updating.
     * @return true if there was already a trigger for the given entity, false
     *         otherwise.
     * @throws IllegalArgumentException if a new trigger must be made, but the
     *                                  entity does not have a LocationComponent, TriggerComponent and
     *                                  ShapeComponent.
     */
    boolean updateTrigger(EntityRef entity);
}
