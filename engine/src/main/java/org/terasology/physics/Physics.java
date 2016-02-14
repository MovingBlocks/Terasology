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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.AABB;
import org.terasology.math.geom.Vector3f;

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
    List<EntityRef> scanArea(AABB area, CollisionGroup... collisionFilter);

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
    List<EntityRef> scanArea(AABB area, Iterable<CollisionGroup> collisionFilter);
}
