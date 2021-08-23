// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.physics;

import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import org.joml.Vector3fc;
import org.terasology.engine.physics.engine.CharacterCollider;
import org.terasology.joml.geom.AABBf;
import org.joml.Vector3f;
import org.terasology.engine.entitySystem.entity.EntityRef;

import java.util.List;
import java.util.Set;

public interface Physics {
    float GRAVITY = 15f;
    long TIME_BETWEEN_NETSYNCS = 500;
    CollisionGroup[] DEFAULT_COLLISION_GROUP =
            {StandardCollisionGroup.WORLD, StandardCollisionGroup.CHARACTER, StandardCollisionGroup.DEFAULT};
    float COLLISION_DAMPENING_MULTIPLIER = 0.5f;


    /**
     * Wakes up any rigid bodies that are in a box around the given position.
     *
     * @param pos    The position around which to wake up objects.
     * @param radius the half-length of the sides of the square.
     */
    void awakenArea(Vector3fc pos, float radius);

    /**
     * Executes a rayTrace on the physics engine.
     *
     * @param from            Place to start tracing
     * @param direction       Directing in which to trace
     * @param distance        maximum distance to trace before giving up
     * @param collisionGroups the collision groups to collide with. Only if an
     *                        object of any of these groups is hit it will be registered.
     * @return A HitResult object that contains the info about the ray trace.
     */
    HitResult rayTrace(Vector3f from, Vector3f direction, float distance, CollisionGroup... collisionGroups);

    /**
     * Executes a rayTrace on the physics engine, excluding hitting entities specified
     *
     * @param from             Place to start tracing
     * @param direction        Directing in which to trace
     * @param distance         maximum distance to trace before giving up
     * @param excludedEntities entities that should not be tested during the ray trace
     * @param collisionGroups  the collision groups to collide with. Only if an
     *                         object of any of these groups is hit it will be registered.
     * @return A HitResult object that contains the info about the ray trace.
     */
    HitResult rayTrace(Vector3f from, Vector3f direction, float distance, Set<EntityRef> excludedEntities, CollisionGroup... collisionGroups);


    /**
     * Scans the given area for physics objects of the given groups and returns
     * a list of the entities of the physics objects in the given area.
     * <br><br>
     * If an Entity has multiple physics objects with the right collision group
     * in this area, this entity will be found multiple times in the returned
     * list.
     *
     * @param area            The area to scan
     * @param collisionFilter only objects in these collision groups are
     *                        returned.
     * @return A valid, non null List with EntityRefs. Each entity in this list
     *         has an associated RigidBody, CharacterCollider or Trigger in any of the
     *         given collision groups.
     */
    List<EntityRef> scanArea(AABBf area, CollisionGroup... collisionFilter);

    /**
     * Scans the given area for physics objects of the given groups and returns
     * a list of the entities of the physics objects in the given area.
     * <br><br>
     * If an Entity has multiple physics objects with the right collision group
     * in this area, this entity will be found multiple times in the returned
     * list.
     *
     * @param area            The area to scan
     * @param collisionFilter only objects in these collision groups are
     *                        returned.
     * @return A valid, non null List with EntityRefs. Each entity in this list
     *         has an associated RigidBody, CharacterCollider or Trigger in any of the
     *         given collision groups.
     */
    List<EntityRef> scanArea(AABBf area, Iterable<CollisionGroup> collisionFilter);

    /**
     * The epsilon value is the value that is considered to be so small that it
     * could just as well be zero. Objects that are closer together than this
     * value are assumes to be colliding.
     *
     * @return The simulation epsilon.
     */
    float getEpsilon();

    /**
     * Get the character collider for the given entity. Will create a new
     * CharacterCollider if non exists and return that one.
     *
     * @param entity
     * @return true if the given entity has a CharacterCollider associated to it.
     */
    CharacterCollider getCharacterCollider(EntityRef entity);

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


}
