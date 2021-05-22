// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.physics;

import org.terasology.joml.geom.AABBf;
import org.joml.Vector3f;
import org.terasology.engine.entitySystem.entity.EntityRef;

import java.util.List;
import java.util.Set;

/**
 */
public interface Physics {
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
}
