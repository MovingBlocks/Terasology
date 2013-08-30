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
     * @return A single flag representing all the given groups.
     */
    short combineGroups(CollisionGroup... groups);

    /**
     * Combines the flags of the given collision groups into a single flag.
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
     * the physics engine. A new set is created that is not backed by this class.
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
     * </p>
     * Note that you should not wait with calling this method until using the
     * rigid body. As soon as the rigid body should exist in the physics engine,
     * this method should be called to create the rigid body.
     *
     * @param entity the entity to retrieve the rigid body of.
     * @return A valid RigidBody instance.
     * @throws IllegalArgumentException if there is no RigidBody in this
     * PhysicsEngine for the given entity and one cannot be created because the
     * given entity does not have a LocationComponent, RigidBodyComponent and
     * ShapeComponent.
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
     * @param collisionGroups the collision groups to collide with. Only if an
     * object of any of these groups is hit it will be registered.
     * @return A HitResult object that contains the info about the ray trace.
     */
    HitResult rayTrace(Vector3f from, Vector3f direction, float distance, CollisionGroup... collisionGroups);

    /**
     * Removes the CharacterCollider associated with the given entity from the
     * physics engine. The collider object of this entity will no longer be
     * valid.
     * </p>
     * If no CharacterCollider was attached to the entity, a warning is logged
     * and this method return false.
     * </p>
     * Make sure not to make another call to getCharacterCollider() if you are
     * destroying the entity, as this will create a new CharacterCollider for
     * the entity.
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
     * </p>
     * Make sure not to make another call to getRigidBody() if you are
     * destroying the entity, as this will create a new RigidBody for
     * the entity.
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
     * </p>
     * Make sure not to make another call to updateTrigger() if you are
     * destroying the entity, as this will create a new trigger for
     * the entity.
     * @param entity the entity to remove the rigid body of.
     * @return true if this entity had a trigger attached to it, false
     * otherwise.
     */
    boolean removeTrigger(EntityRef entity);

    /**
     * Scans the given area for physics objects of the given groups and returns
     * a list of the entities of the physics objects in the given area.
     * </p>
     * If an Entity has multiple physics objects with the right collision group
     * in this area, this entity will be found multiple times in the returned
     * list. Although it is not likely to happen since a singe entity can only
     * have a singel RigidBody, CharacterCollider or and Trigger, the
     * collisionFilter may check for all three.
     *
     * @param area The area to scan
     * @param collisionFilter only objects in these collision groups are
     * returned.
     * @return A valid, non null List with EntityRefs. Each entity in this list
     * has an associated RigidBody, CharacterCollider or Trigger in any of the
     * given collision groups.
     */
    List<EntityRef> scanArea(AABB area, CollisionGroup... collisionFilter);

    /**
     * See the overloaded version for proper documentation.
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
     * </p>
     * Usually the LocationComponent (location) and RigidBodyComponent
     * (velocity) are updated by the physics engine. If this dataflow needs to
     * be turned around and the physics position needs to be updated, this
     * method should be called. It is therefor used by the PhysicsSystem class,
     * which captures the event which should occur when a LocationComponent is
     * saved. Hence this method should not be needed by anyone else then the
     * PhysicsSystem class. Instead change the LocationComponent and save it.
     *
     * @param entity the entity of which the rigidBody needs updating.
     * @return true if there was already a rigidBody registered for the entity
     * (which is now updated), false otherwise.
     */
    boolean updateRigidBody(EntityRef entity);
    
    /**
     * Updates or creates the trigger of the given object. The entity must have
     * a TriggerComponent, LocationComponent and ShapeComponent to have a
     * trigger. When updating an existing trigger the location and scale are
     * updated.
     * </p>
     * An entity with a trigger attached to it will generate collision pairs
     * when it collides or intersects with other objects. A good example of its
     * usage is picking up items. By creating a trigger for a player, a
     * collision event will be generated when the shape of the player collides
     * with the shape of a dropped item. This event is not send by the
     * PhysicsEngine class. Instead it is stored and can be retrieved by the
     * getCollisionPairs() method. The PhysicsSystem class uses this mechanism
     * to generate normal Terasology events.
     * </p>TODO: update if detectGroups changed
     *
     * @param entity the entity of which the trigger may need updating.
     * @return true if there was already a trigger for the given entity, false
     * otherwise.
     * @throws IllegalArgumentException if a new trigger must be made, but the
     * entity does not have a LocationComponent, TriggerComponent and
     * ShapeComponent.
     */
    boolean updateTrigger(EntityRef entity);
}
