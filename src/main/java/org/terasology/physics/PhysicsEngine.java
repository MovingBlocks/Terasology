/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import org.terasology.physics.PhysicsSystem;
import org.terasology.physics.RigidBody;

/**
 *
 * @author Administrator
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
     * Creates a Collider for the given entity based on the LocationComponent
     * and CharacterMovementComponent.
     * All collision flags are set right for a character movement component.
     *
     * @param owner the entity to create the collider for.
     * @return
     */
    CharacterCollider createCharacterCollider(EntityRef owner);

    /**
     * Disposes this physics engine. Afterwards this physics engine cannot be
     * used anymore.
     */
    void dispose();

    /**
     * Get the character collider for the given entity. Returns null of no such
     * collider exists. After calling createCharacterCollider(EntityRef owner)
     * this method should return true (for the same entity).
     *
     * @param entity
     * @return true if the given entity has a CharacterCollider associated to it.
     */
    CharacterCollider getCharacterCollider(EntityRef entity);

    //*****************Physics Interface methods******************\\
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
     * Returns the rigid body associated with the given entity.
     *
     * @param entity
     * @return null if there is no rigid body for the given entity (yet),
     * otherwise it returns the requested RigidBody instance.
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
     */
    void newTrigger(EntityRef entity);

    /**
     * Warning: Using this iterator to remove elements has an unpredictable
     * behaviour. Do not use this functionality! Instead, store the elements and
     * remove them later with removeRigidBody(EntityRef), or retrieve the
     * entities using getPhysicsEntities(), which is not backed hence you can
     * call removeRigibBody while iterating over all elements.
     *
     * @return An iterator that iterates over all entities that have a rigidBody
     * that is active in the physics engine.
     */
    Iterator<EntityRef> physicsEntitiesIterator();

    /**
     * Executes a rayTrace on the physics engine.
     * @param from Place to start tracing
     * @param direction Directing in which to trace
     * @param distance maximum distance to trace before giving up
     * @return A HitResult that contains the info about the ray trace.
     */
    HitResult rayTrace(Vector3f from, Vector3f direction, float distance);

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

    void removeCharacterCollider(EntityRef entity);

    /**
     * Removes the rigid body associated with the given entity from the physics
     * engine. The RigidBody object returned by the newRigidBody(EntityRef)
     * method will no longer be valid, so be careful!
     * @param entity the entity to remove the rigid body of.
     */
    void removeRigidBody(EntityRef entity);

    void removeTrigger(EntityRef entity);

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
     * entity. If the given entity had no rigidBody in the physics engine,
     * nothing will happen. The return value can be used to see whether or not
     * something has happened.
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
     * If no trigger exists for the given entity, nothing is done by this
     * method, except log a warning. TODO: update if detectGroups changed
     *
     * @param entity the entity of which the trigger may need updating.
     */
    void updateTrigger(EntityRef entity);
    
}
