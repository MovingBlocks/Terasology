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

package org.terasology.physics;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.vecmath.Vector3f;
import org.terasology.entitySystem.EntityRef;
import org.terasology.math.AABB;
import org.terasology.physics.CharacterCollider;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.HitResult;
import org.terasology.physics.events.PhysicsSystem;
import org.terasology.physics.RigidBody;

/**
 * This is the main interface for the physics engine. Any other sub-interfaces 
 * can be accessed through this one.
 * 
 * @author Xanhou
 */
public interface PhysicsEngine {

    /**
     * Wakes up any rigid bodies that are in a square around the given position.
     * @param pos The position around which to wake up objects.
     * @param radius the half-length of the sides of the square.
     */
    void awakenArea(Vector3f pos, float radius);

    /**
     * Combines the flags of the given collision groups into a single flag.
     * @param groups
     * @return 
     */
    short combineGroups(CollisionGroup... groups);

    /**
     * Combines the flags of the given collision groups into a single flag.
     * @param groups
     * @return 
     */
    short combineGroups(Iterable<CollisionGroup> groups);

    /**
     * Disposes this physics engine. Afterwards this physics engine cannot be
     * used anymore.
     */
    void dispose();

    /**
     * Creates a Collider for the given entity based on the LocationComponent
     * and CharacterMovementComponent.
     * All collision flags are set right for a character movement component.
     *
     * @param owner the entity to create the collider for.
     * @return
     */
    CharacterCollider createCharacterCollider(EntityRef owner);
    
    /**
     * Return a list with all CollisionPairs that occurred in the previous
     * physics simulation step.
     * TODO: alter this method to return all collision pairs since the last call to this method.
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
     * the physics engine. A new set is created that is not backed by this class.
     */
    Set<EntityRef> getPhysicsEntities();
    
    /**
     * Get the character collider for the given entity. Returns null of no such
     * collider exists. After calling createCharacterCollider(EntityRef owner)
     * this method should return true (for the same entity).
     *
     * @param entity
     * @return true if the given entity has a CharacterCollider associated to it.
     */
    CharacterCollider getCharacterCollider(EntityRef entity);

    /**
     * Returns the rigid body associated with the given entity.
     *
     * @param entity the entity to retrieve the rigid body of.
     * @return A valid RigidBody instance.
     * @throws IllegalStateException if there is no RigidBody in this
     * PhysicsEngine for the given entity
     */
    RigidBody getRigidBody(EntityRef entity);

    /**
     * @param entity
     * @return Returns true if there is a rigidBody in the physics engine
     * related to the given entity, false otherwise.
     */
    boolean hasRigidBody(EntityRef entity);

    /**
     * Checks if the given entity has a trigger attached to it.
     * @param entity the entity to check for.
     * @return true if the entity has a trigger, false otherwise.
     */
    boolean hasTrigger(EntityRef entity);
    
    /**
     * @param entity the entity to check for
     * @return true if the physics engine has a character collider for the given
     * entity, false otherwise.
     */
    boolean hasCharacterCollider(EntityRef entity);

    /**
     * Creates a new rigid body and adds it to the physics engine. The returned
     * RigidBody can be used for various operations. Most of these operations
     * can also be executed by method of this class by giving the entity the
     * body belongs to as additional parameter. If the given entity already had
     * a rigid body attached to it, this body is removed from the physics
     * engine and will no longer be valid.
     *
     * @param entity the entity to create a rigid body for. Must have a
     * LocationComponent, RigidBodyComponent and ShapeComponent. If not an
     * exception is thrown.
     * @return The newly created RigidBody. All exposed methods are ready to be
     * used.
     * @throws IlligalArgumentException if the given entity does not have a
     * LocationComponent, ShapeComponent and RigidBodyComponent.
     */
    RigidBody newRigidBody(EntityRef entity);

    /**
     * Creates a new trigger. An entity with a trigger attached to it will
     * generate collision pairs when it collides or intersects with other
     * objects. A good example of its usage is picking up items. By creating a
     * trigger for a player, a collision event will be generated when the shape
     * of the player collides with the shape of a dropped item. This event is
     * send by the physics class. Instead it is stored and can be retrieved by
     * the getCollisionPairs() method. The PhysicsSystem class uses this
     * mechanism to generate normal Terasolegy events.
     *
     * @param entity the entity to create a trigger for.
     * @return true of there was already a trigger for this entity.
     */
    boolean newTrigger(EntityRef entity);

    /**
     * Warning: Using this iterator to remove elements has an unpredictable
     * behaviour. Do not use this functionality! Instead, store the elements and
     * remove them later with removeRigidBody(EntityRef), or retrieve the
     * entities using getPhysicsEntities(), which is not backed hence you can
     * call removeRigibBody while iterating over all elements. This method is
     * however more efficient than getPhysicsEntities().
     *
     * @return An iterator that iterates over all entities that have a rigidBody
     * that is active in the physics engine.
     */
    Iterator<EntityRef> physicsEntitiesIterator();

    /**
     * Executes a rayTrace on the physics engine.
     *
     * @param from Place to start tracing
     * @param direction Directing in which to trace
     * @param distance maximum distance to trace before giving up
     * @param collisionGroups the collision groups to test for. Only if an
     * object of any of these groups is hit it will be registered.
     * @return A HitResult that contains the info about the ray trace.
     */
    HitResult rayTrace(Vector3f from, Vector3f direction, float distance, CollisionGroup... collisionGroups);

    /**
     * Removes the character collider associated with the given entity from the
     * physics engine. The collider object of this entity will no longer be
     * valid.
     * </p>
     * If no character collider was attached to the entity, a warning is logged
     * and this method return false.
     *
     * @param entity the entity to remove the rigid body of.
     * @return true if this entity had a character collider attached to it,
     * false otherwise.
     */
    boolean removeCharacterCollider(EntityRef entity);

    /**
     * Removes the rigid body associated with the given entity from the physics
     * engine. The RigidBody object returned by the newRigidBody(EntityRef) or
     * getRigifBody(EntityRef) method will no longer be valid for this entity un
     * till newRigidBody is called again, so be careful!
     * </p>
     * If no rigid body was attached to the entity, a warning is logged and this
     * method return false.
     *
     * @param entity the entity to remove the rigid body of.
     * @return true if this entity had a rigid body attached to it, false
     * otherwise.
     */
    boolean removeRigidBody(EntityRef entity);

    /**
     * Removes the trigger associated with the given entity from the physics
     * engine.
     * </p>
     * If no trigger was attached to the entity, a warning is logged and this
     * method return false.
     *
     * @param entity the entity to remove the rigid body of.
     * @return true if this entity had a trigger attached to it, false
     * otherwise.
     */
    boolean removeTrigger(EntityRef entity);

    /**
     * Scans the given area for physics objects of the given groups and returns
     * a list of the entities of the physics objects in the given area.
     *
     * @param area
     * @param collisionFilter
     * @return A valid, non null List with EntityRefs. Each entity in this list
     * has an associated RigidBody, CharacterCollider or Trigger.
     */
    List<EntityRef> scanArea(AABB area, CollisionGroup... collisionFilter);

    /**
     * Scans the given area for physics objects of the given groups and returns
     * a list of the entities of the physics objects in the given area.
     *
     * @param area
     * @param collisionFilter
     * @return A valid, non null List with EntityRefs. Each entity in this list
     * has an associated RigidBody, CharacterCollider or Trigger.
     */
    List<EntityRef> scanArea(AABB area, Iterable<CollisionGroup> collisionFilter);

    /**
     * Advances the physics engine with the given amount of time in seconds. As
     * long as this time does not exceed 8/60 seconds, the game speed will be
     * constant.
     * @param delta amount of time to advance the engine in seconds.
     */
    void update(float delta);

    /**
     * Updates the shape and position of the rigidBody belonging to the given
     * entity. If the given entity had no rigidBody in the physics engine, it
     * will be created. Updating an entity without RigidBody is seen as bad
     * practise and hence a warning is logged.
     *
     * @param entity the entity of which the rigidBody needs updating.
     * @return true if there was already a rigidBody registered for the entity
     * (which is now updated), false otherwise.
     */
    boolean updateRigidBody(EntityRef entity);

    /**
     * Updates the trigger of the given object. If the ShapeComponent has
     * changed, the shape of the trigger will also change. If the location
     * stored in the location component has changed, this will also be updated.
     * If no trigger exists for the given entity, a new one is created. This is
     * however seen as bad behaviour and logged as warning.
     * </p>TODO: update if detectGroups changed
     *
     * @param entity the entity of which the trigger may need updating.
     */
    boolean updateTrigger(EntityRef entity);
    
}
